@file:OptIn(WorkflowUiExperimentalApi::class)

package com.shiftstudio.workflowshenanigans

import androidx.activity.ComponentActivity
import com.shiftstudio.workflowshenanigans.ShenanigansWorkflow.ActivityAndProps
import com.shiftstudio.workflowshenanigans.account.AccountViewRegistry
import com.shiftstudio.workflowshenanigans.account.AccountWorkflow
import com.shiftstudio.workflowshenanigans.login.LoginViewRegistry
import com.shiftstudio.workflowshenanigans.welcome.WelcomeWorkflow
import com.squareup.workflow1.Snapshot
import com.squareup.workflow1.StatefulWorkflow
import com.squareup.workflow1.Workflow
import com.squareup.workflow1.action
import com.squareup.workflow1.parse
import com.squareup.workflow1.ui.NamedViewFactory
import com.squareup.workflow1.ui.ViewRegistry
import com.squareup.workflow1.ui.WorkflowUiExperimentalApi
import com.squareup.workflow1.ui.backstack.BackStackContainer
import com.squareup.workflow1.ui.backstack.BackStackScreen
import com.squareup.workflow1.ui.backstack.toBackStackScreen
import com.squareup.workflow1.ui.plus
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Inject

interface ShenanigansWorkflow : Workflow<ActivityAndProps<Unit>, Nothing, BackStackScreen<Any>> {

    data class ActivityAndProps<R>(
        val activity: ComponentActivity,
        val props: R
    )
}

val ShenanigansViewRegistry: ViewRegistry = ViewRegistry(BackStackContainer, NamedViewFactory) +
    AccountViewRegistry +
    LoginViewRegistry

class ShenanigansWorkflowImpl @Inject constructor(
    private val welcomeWorkflow: WelcomeWorkflow,
    private val accountWorkflow: AccountWorkflow,
) : ShenanigansWorkflow,
    StatefulWorkflow<ActivityAndProps<Unit>, ShenanigansWorkflowImpl.State, Nothing, BackStackScreen<Any>>() {

    sealed class State {
        object Welcome : State()

        object Account : State()
    }

    override fun initialState(props: ActivityAndProps<Unit>, snapshot: Snapshot?): State {
        return snapshot?.bytes
            ?.parse { source -> if (source.readInt() == 0) State.Welcome else State.Account }
            ?: State.Welcome
    }

    override fun render(
        renderProps: ActivityAndProps<Unit>,
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

    override fun snapshotState(state: State): Snapshot {
        return Snapshot.of(if (state == State.Welcome) 0 else 1)
    }
}

@Module
@InstallIn(SingletonComponent::class)
abstract class ShenanigansModule {

    @Binds
    abstract fun bindShenanigansWorkflow(impl: ShenanigansWorkflowImpl): ShenanigansWorkflow
}
