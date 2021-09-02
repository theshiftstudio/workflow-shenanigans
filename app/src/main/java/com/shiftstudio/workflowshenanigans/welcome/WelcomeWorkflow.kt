@file:OptIn(WorkflowUiExperimentalApi::class)

package com.shiftstudio.workflowshenanigans.welcome

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.shiftstudio.workflowshenanigans.welcome.WelcomeWorkflow.Output
import com.squareup.workflow1.StatelessWorkflow
import com.squareup.workflow1.Workflow
import com.squareup.workflow1.ui.ViewEnvironment
import com.squareup.workflow1.ui.ViewRegistry
import com.squareup.workflow1.ui.WorkflowUiExperimentalApi
import com.squareup.workflow1.ui.compose.WorkflowRendering
import com.squareup.workflow1.ui.compose.composeViewFactory
import com.squareup.workflow1.ui.compose.renderAsState
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Inject

interface WelcomeWorkflow : Workflow<Unit, Output, WelcomeRendering> {

    sealed class Output {
        object GoToAccount : Output()
    }
}

data class WelcomeRendering(val onAccountTapped: () -> Unit)

val WelcomeBinding = composeViewFactory<WelcomeRendering> { rendering, _ ->

    Column(modifier = Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally) {
        Spacer(modifier = Modifier.height(126.dp))
        Button(onClick = rendering.onAccountTapped) {
            Text(text = "Account")
        }
    }
}

val WelcomeViewRegistry = ViewRegistry(WelcomeBinding)

class WelcomeWorkflowImpl @Inject constructor() : WelcomeWorkflow, StatelessWorkflow<Unit, Output, WelcomeRendering>() {

    override fun render(renderProps: Unit, context: RenderContext): WelcomeRendering =
        WelcomeRendering(
            onAccountTapped = context.eventHandler { setOutput(Output.GoToAccount) }
        )
    // ComposeRendering {
    //     Column(horizontalAlignment = Alignment.CenterHorizontally) {
    //         Spacer(modifier = Modifier.height(126.dp))
    //         Button(onClick = context.eventHandler { setOutput(Output.GoToAccount) }) {
    //             Text(text = "Account")
    //         }
    //     }
    // }
}

@Module
@InstallIn(SingletonComponent::class)
abstract class WelcomeModule {

    @Binds
    abstract fun bindWelcomeWorkflow(impl: WelcomeWorkflowImpl): WelcomeWorkflow
}

@Preview
@Composable
fun WelcomePreview() {
    val workflow = remember {
        WelcomeWorkflowImpl()
    }
    val rendering by workflow.renderAsState(props = Unit, onOutput = {})
    WorkflowRendering(rendering, ViewEnvironment())
}
