package com.shiftstudio.workflowshenanigans.login

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Parcelable
import com.firebase.ui.auth.AuthMethodPickerLayout
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.FirebaseAuthUIActivityResultContract
import com.firebase.ui.auth.data.model.FirebaseAuthUIAuthenticationResult
import com.google.firebase.auth.FirebaseAuth
import com.shiftstudio.workflowshenanigans.BuildConfig
import com.shiftstudio.workflowshenanigans.R
import com.shiftstudio.workflowshenanigans.infrastructure.findActivity
import com.shiftstudio.workflowshenanigans.login.LoginWorkflow.LoginResult
import com.shiftstudio.workflowshenanigans.login.LoginWorkflowImpl.State
import com.shiftstudio.workflowshenanigans.login.domain.User
import com.shiftstudio.workflowshenanigans.login.domain.UserRepository
import com.shiftstudio.workflowshenanigans.login.domain.toUser
import com.squareup.workflow1.Snapshot
import com.squareup.workflow1.StatefulWorkflow
import com.squareup.workflow1.Worker
import com.squareup.workflow1.Workflow
import com.squareup.workflow1.action
import com.squareup.workflow1.asWorker
import com.squareup.workflow1.runningWorker
import com.squareup.workflow1.ui.toParcelable
import com.squareup.workflow1.ui.toSnapshot
import dagger.hilt.android.qualifiers.ActivityContext
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.parcelize.Parcelize
import javax.inject.Inject

interface LoginWorkflow : Workflow<Unit, LoginResult, Any> {

    sealed class LoginResult {
        data class Authorized(val user: User) : LoginResult()

        data class Failed(val cause: String) : LoginResult()

        object Canceled : LoginResult()
    }
}

data class AuthorizingRendering(val message: String = "Hold tight...")

data class AuthorizationFailed(val cause: String, val onRetry: () -> Unit)

class LoginWorkflowImpl @Inject constructor(
    @ActivityContext private val activityContext: Context,
    private val userRepository: UserRepository,
) : LoginWorkflow, StatefulWorkflow<Unit, State, LoginResult, Any>() {

    sealed class State : Parcelable {
        @Parcelize
        object Authorizing : State()

        @Parcelize
        data class Authorized(val user: User) : State()

        @Parcelize
        data class Failed(val cause: String) : State()
    }

    override fun initialState(props: Unit, snapshot: Snapshot?): State =
        snapshot?.toParcelable() ?: State.Authorizing

    override fun render(
        renderProps: Unit,
        renderState: State,
        context: RenderContext
    ): Any = when (renderState) {
        is State.Authorizing -> {
            context.runningWorker(
                callbackFlow {
                    val signInLauncher = activityContext.findActivity()
                        .activityResultRegistry.register(
                            "Key-FirebaseAuthUIActivityResultContract",
                            FirebaseAuthUIActivityResultContract()
                        ) { authResult ->
                            trySend(authResult).isSuccess
                        }

                    signInLauncher.launch(buildSignInIntent())

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

    private fun buildSignInIntent(): Intent {
        val layout = AuthMethodPickerLayout.Builder(R.layout.auth_method_picker_custom_layout)
            .setGoogleButtonId(R.id.custom_google_signin_button)
            .setAppleButtonId(R.id.custom_apple_signin_button)
            .setTosAndPrivacyPolicyId(R.id.custom_tos_pp)
            .build()

        return AuthUI.getInstance()
            .createSignInIntentBuilder()
            .setIsSmartLockEnabled(!BuildConfig.DEBUG, true)
            .setAuthMethodPickerLayout(layout)
            .setLogo(R.drawable.ic_launcher_background)
            .setTheme(R.style.Theme_WorkflowShenanigans_Login)
            .setAvailableProviders(
                listOf(
                    AuthUI.IdpConfig.GoogleBuilder().build(),
                    AuthUI.IdpConfig.AppleBuilder().build(),
                )
            )
            .build()
    }

    override fun snapshotState(state: State): Snapshot = state.toSnapshot()
}
