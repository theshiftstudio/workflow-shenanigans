package com.shiftstudio.workflowshenanigans.account

import android.util.Log
import com.shiftstudio.workflowshenanigans.ShenanigansWorkflow.ActivityAndProps
import com.shiftstudio.workflowshenanigans.account.AccountWorkflow.Back
import com.shiftstudio.workflowshenanigans.account.AccountWorkflowImpl.State
import com.shiftstudio.workflowshenanigans.login.LoginWorkflow
import com.shiftstudio.workflowshenanigans.login.domain.User
import com.shiftstudio.workflowshenanigans.login.domain.UserRepository
import com.squareup.workflow1.Snapshot
import com.squareup.workflow1.StatefulWorkflow
import com.squareup.workflow1.Workflow
import com.squareup.workflow1.action
import com.squareup.workflow1.asWorker
import com.squareup.workflow1.parse
import com.squareup.workflow1.readBooleanFromInt
import com.squareup.workflow1.readByteStringWithLength
import com.squareup.workflow1.readUtf8WithLength
import com.squareup.workflow1.runningWorker
import com.squareup.workflow1.writeBooleanAsInt
import com.squareup.workflow1.writeByteStringWithLength
import com.squareup.workflow1.writeUtf8WithLength
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.delayFlow
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import okio.ByteString
import javax.inject.Inject

interface AccountWorkflow : Workflow<ActivityAndProps<Unit>, Back, Any> {

    object Back
}

object LoadingUserRendering

data class UserRendering(val user: User, val onSignOut: () -> Unit)

class AccountWorkflowImpl @Inject constructor(
    private val userRepository: UserRepository,
    private val loginWorkflow: LoginWorkflow,
) : AccountWorkflow, StatefulWorkflow<ActivityAndProps<Unit>, State, Back, Any>() {

    sealed class State {
        object Loading : State()

        object Unauthorized : State()

        data class Authorized(val user: User, val signingOut: Boolean = false) : State()

        fun toSnapshot(): Snapshot = Snapshot.write { sink ->
            sink.writeUtf8WithLength(this::class.java.name)

            if (this is Authorized) {
                sink.writeByteStringWithLength(user.toSnapshot().bytes)
                sink.writeBooleanAsInt(signingOut)
            }
        }

        companion object {

            fun fromSnapshot(byteString: ByteString): State = byteString.parse { source ->
                when (val className = source.readUtf8WithLength()) {
                    Loading::class.java.name -> Loading
                    Unauthorized::class.java.name -> Unauthorized
                    Authorized::class.java.name -> Authorized(
                        user = User.fromSnapshot(source.readByteStringWithLength()),
                        signingOut = source.readBooleanFromInt(),
                    )
                    else -> throw IllegalArgumentException("Unknown type $className")
                }
            }
        }
    }

    override fun initialState(props: ActivityAndProps<Unit>, snapshot: Snapshot?): State =
        snapshot?.bytes?.let { State.fromSnapshot(it) }
            ?: State.Loading

    override fun render(
        renderProps: ActivityAndProps<Unit>,
        renderState: State,
        context: RenderContext
    ): Any {
        context.runningWorker(
            userRepository.currentUser
                .onEach {
                    Log.d("plm", "userRepository.currentUser: ${it?.name}")
                }
                .asWorker()
        ) { maybeUser -> handleMaybeUser(maybeUser) }

        return when (renderState) {
            is State.Loading -> {
                LoadingUserRendering
            }

            is State.Unauthorized -> {
                // to the whole FirebaseUI stuff here
                val loginScreen = context.renderChild(loginWorkflow, renderProps) { loginResult ->
                    handleLoginResult(loginResult)
                }
                loginScreen
            }

            is State.Authorized -> {
                if (renderState.signingOut) {
                    context.runningSideEffect("sideEffect-signOut") {
                        userRepository.signOut()
                    }
                }
                UserRendering(
                    user = renderState.user,
                    onSignOut = context.eventHandler {
                        (state as? State.Authorized)?.let { oldState ->
                            state = oldState.copy(signingOut = true)
                        }
                    }
                )
            }
        }
    }

    private fun handleMaybeUser(user: User?) = action {
        if (user != null) {
            state = State.Authorized(user)
        } else {
            // if we were authorized and now user is null, we wanna go back and not start the auth flow again,
            // the user probably logged-out
            if (state is State.Authorized) {
                setOutput(Back)
            } else {
                state = State.Unauthorized
            }
        }
    }

    private fun handleLoginResult(result: LoginWorkflow.LoginResult) = action {
        when (result) {
            is LoginWorkflow.LoginResult.Authorized -> {
                state = State.Authorized(result.user)
            }
            is LoginWorkflow.LoginResult.Canceled -> {
                setOutput(Back)
            }
            is LoginWorkflow.LoginResult.Failed -> {
                // should this be a no-op? or not exist at all
            }
        }
    }

    override fun snapshotState(state: State): Snapshot = state.toSnapshot()
}
