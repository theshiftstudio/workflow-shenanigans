@file:OptIn(WorkflowUiExperimentalApi::class)

package com.shiftstudio.workflowshenanigans.welcome

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
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
import com.squareup.workflow1.ui.AndroidViewRendering
import com.squareup.workflow1.ui.ViewEnvironment
import com.squareup.workflow1.ui.WorkflowUiExperimentalApi
import com.squareup.workflow1.ui.compose.ComposeRendering
import com.squareup.workflow1.ui.compose.WorkflowRendering
import com.squareup.workflow1.ui.compose.renderAsState
import javax.inject.Inject

interface WelcomeWorkflow : Workflow<Unit, Output, AndroidViewRendering<*>> {

    sealed class Output {
        object GoToAccount : Output()
    }
}

class WelcomeWorkflowImpl @Inject constructor() : WelcomeWorkflow,
    StatelessWorkflow<Unit, Output, AndroidViewRendering<*>>() {

    override fun render(renderProps: Unit, context: RenderContext): AndroidViewRendering<*> =
        ComposeRendering {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Spacer(modifier = Modifier.height(126.dp))
                Button(onClick = context.eventHandler { setOutput(Output.GoToAccount) }) {
                    Text(text = "Account")
                }
            }
        }
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
