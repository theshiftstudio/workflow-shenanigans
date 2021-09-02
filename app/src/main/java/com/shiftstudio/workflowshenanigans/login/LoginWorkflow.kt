package com.shiftstudio.workflowshenanigans.login

import android.app.Activity
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.FirebaseAuthUIActivityResultContract
import com.firebase.ui.auth.data.model.FirebaseAuthUIAuthenticationResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.shiftstudio.workflowshenanigans.ShenanigansWorkflow.ActivityAndProps
import com.shiftstudio.workflowshenanigans.login.LoginWorkflow.LoginResult
import com.shiftstudio.workflowshenanigans.login.LoginWorkflowImpl.State
import com.squareup.workflow1.Snapshot
import com.squareup.workflow1.StatefulWorkflow
import com.squareup.workflow1.Worker
import com.squareup.workflow1.Workflow
import com.squareup.workflow1.action
import com.squareup.workflow1.asWorker
import com.squareup.workflow1.parse
import com.squareup.workflow1.readByteStringWithLength
import com.squareup.workflow1.readUtf8WithLength
import com.squareup.workflow1.runningWorker
import com.squareup.workflow1.writeByteStringWithLength
import com.squareup.workflow1.writeUtf8WithLength
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow
import okio.ByteString
import javax.inject.Inject

interface LoginWorkflow : Workflow<ActivityAndProps<Unit>, LoginResult, Any> {

    sealed class LoginResult {
        data class Authorized(val user: User) : LoginResult()

        data class Failed(val cause: String) : LoginResult()

        object Canceled : LoginResult()
    }
}

data class AuthorizingRendering(val message: String = "Hold tight...")

data class AuthorizationFailed(val cause: String, val onRetry: () -> Unit)

class LoginWorkflowImpl @Inject constructor(
    private val userRepository: UserRepository,
) : LoginWorkflow, StatefulWorkflow<ActivityAndProps<Unit>, State, LoginResult, Any>() {

    sealed class State {
        object Authorizing : State()

        data class Authorized(val user: User) : State()

        data class Failed(val cause: String) : State()

        fun toSnapshot(): Snapshot = Snapshot.write { sink ->
            sink.writeUtf8WithLength(this::class.java.name)
            when (this) {
                is Authorizing -> {
                    // no-op
                }
                is Authorized -> sink.writeByteStringWithLength(user.toSnapshot().bytes)
                is Failed -> sink.writeUtf8WithLength(cause)
            }
        }

        companion object {
            fun fromSnapshot(byteString: ByteString): State = byteString.parse { source ->
                when (val className = source.readUtf8WithLength()) {
                    Authorizing::class.java.name -> Authorizing
                    Authorized::class.java.name -> Authorized(user = User.fromSnapshot(source.readByteStringWithLength()))
                    Failed::class.java.name -> Failed(cause = source.readUtf8WithLength())
                    else -> throw IllegalArgumentException("Unknown type $className")
                }
            }
        }
    }

    override fun initialState(props: ActivityAndProps<Unit>, snapshot: Snapshot?): State {
        return snapshot?.bytes?.let { State.fromSnapshot(it) }
            ?: State.Authorizing
    }

    override fun render(
        renderProps: ActivityAndProps<Unit>,
        renderState: State,
        context: RenderContext
    ): Any = when (renderState) {
        is State.Authorizing -> {
            context.runningWorker(
                callbackFlow {
                    val signInLauncher = renderProps.activity.activityResultRegistry.register(
                        "Key-FirebaseAuthUIActivityResultContract",
                        FirebaseAuthUIActivityResultContract()
                    ) { authResult ->
                        trySend(authResult).isSuccess
                    }

                    val intent = AuthUI.getInstance()
                        .createSignInIntentBuilder()
                        .setAvailableProviders(listOf(AuthUI.IdpConfig.GoogleBuilder().build()))
                        .build()
                    signInLauncher.launch(intent)

                    awaitClose {
                        signInLauncher.unregister()
                    }
                }.asWorker()
            ) { authResult -> handleFirebaseResult(authResult) }

            AuthorizingRendering()
        }

        is State.Authorized -> {
            context.runningWorker(
                Worker.from {
                    userRepository.setCurrentUser(renderState.user)
                },
                "worker-save-user"
            ) {
                action {
                    setOutput(LoginResult.Authorized(renderState.user))
                }
            }
            context.runningSideEffect("sideEffect-save-user") {
                userRepository.setCurrentUser(renderState.user)
            }
            AuthorizingRendering(message = "")
        }

        is State.Failed -> {
            AuthorizationFailed(
                cause = renderState.cause,
                onRetry = context.eventHandler {
                    state = State.Authorizing
                }
            )
        }
    }

    private fun handleFirebaseResult(result: FirebaseAuthUIAuthenticationResult) = action {
        val response = result.idpResponse
        if (result.resultCode == Activity.RESULT_OK) {
            // Successfully signed in
            val firebaseUser = FirebaseAuth.getInstance().currentUser
            // maybe...
            if (firebaseUser != null) {
                val user = firebaseUser.toUser()
                state = State.Authorized(user)
            } else {
                val cause = "Auth successful but FirebaseAuth.getInstance().currentUser is null."
                state = State.Failed(cause)
                setOutput(LoginResult.Failed(cause))
            }
        } else {
            // Sign in failed. If response is null the user canceled the
            // sign-in flow using the back button. Otherwise check
            // response.getError().getErrorCode() and handle the error.
            if (response == null) {
                setOutput(LoginResult.Canceled)
            } else {
                val cause = response.error?.message
                    ?: "Login failed, code: ${response.error?.errorCode}"
                state = State.Failed(cause)
                setOutput(LoginResult.Failed(cause))
            }
        }
    }

    private fun FirebaseUser.toUser(): User = User(
        uid = this.uid,
        name = this.displayName ?: "",
        email = this.email ?: "",
    )

    override fun snapshotState(state: State): Snapshot = state.toSnapshot()
}

@Module
@InstallIn(SingletonComponent::class)
abstract class LoginModule {

    @Binds
    abstract fun bindLoginWorkflow(impl: LoginWorkflowImpl): LoginWorkflow
}
