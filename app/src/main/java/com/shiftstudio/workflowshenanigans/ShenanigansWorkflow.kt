@file:OptIn(WorkflowUiExperimentalApi::class)

package com.shiftstudio.workflowshenanigans

import android.os.Parcelable
import com.shiftstudio.workflowshenanigans.account.AccountViewRegistry
import com.shiftstudio.workflowshenanigans.account.AccountWorkflow
import com.shiftstudio.workflowshenanigans.login.LoginViewRegistry
import com.shiftstudio.workflowshenanigans.welcome.WelcomeWorkflow
import com.squareup.workflow1.Snapshot
import com.squareup.workflow1.StatefulWorkflow
import com.squareup.workflow1.Workflow
import com.squareup.workflow1.action
import com.squareup.workflow1.ui.NamedViewFactory
import com.squareup.workflow1.ui.ViewRegistry
import com.squareup.workflow1.ui.WorkflowUiExperimentalApi
import com.squareup.workflow1.ui.backstack.BackStackContainer
import com.squareup.workflow1.ui.backstack.BackStackScreen
import com.squareup.workflow1.ui.backstack.toBackStackScreen
import com.squareup.workflow1.ui.plus
import com.squareup.workflow1.ui.toParcelable
import com.squareup.workflow1.ui.toSnapshot
import kotlinx.parcelize.Parcelize
import javax.inject.Inject

interface ShenanigansWorkflow : Workflow<Unit, Nothing, BackStackScreen<Any>>

val ShenanigansViewRegistry: ViewRegistry = ViewRegistry(BackStackContainer, NamedViewFactory) +
    AccountViewRegistry +
    LoginViewRegistry

class ShenanigansWorkflowImpl @Inject constructor(
    private val welcomeWorkflow: WelcomeWorkflow,
    private val accountWorkflow: AccountWorkflow,
) : ShenanigansWorkflow,
    StatefulWorkflow<Unit, ShenanigansWorkflowImpl.State, Nothing, BackStackScreen<Any>>() {

    sealed class State : Parcelable {
        @Parcelize
        object Welcome : State()

        @Parcelize
        object Account : State()
    }

    override fun initialState(props: Unit, snapshot: Snapshot?): State =
        snapshot?.toParcelable() ?: State.Welcome

    override fun render(
        renderProps: Unit,
        renderState: State,
        context: RenderContext
    ): BackStackScreen<Any> {
        val backStack = mutableListOf<Any>()

        val welcomeScreen = context.renderChild(welcomeWorkflow, Unit) { output ->
            handleWelcomeOutput(output)
        }
        backStack += welcomeScreen

        when (renderState) {
            is State.Welcome -> {
                // We always add the welcome screen to the backstack, so this is a no op.
            }
            is State.Account -> {
                val accountScreen = context.renderChild(accountWorkflow, renderProps) {
                    goToWelcome()
                }
                backStack += accountScreen
            }
        }

        return backStack.toBackStackScreen()
    }

    private fun handleWelcomeOutput(output: WelcomeWorkflow.Output) = action {
        when (output) {
            is WelcomeWorkflow.Output.GoToAccount -> {
                state = State.Account
            }
        }
    }

    private fun goToWelcome() = action {
        state = State.Welcome
    }

    override fun snapshotState(state: State): Snapshot = state.toSnapshot()
}
