package com.shiftstudio.workflowshenanigans.infrastructure

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.shiftstudio.workflowshenanigans.ShenanigansViewRegistry
import com.shiftstudio.workflowshenanigans.ShenanigansWorkflow
import com.shiftstudio.workflowshenanigans.ShenanigansWorkflow.ActivityAndProps
import com.shiftstudio.workflowshenanigans.infrastructure.theme.WorkflowShenanigansTheme
import com.squareup.workflow1.ui.ViewEnvironment
import com.squareup.workflow1.ui.ViewRegistry
import com.squareup.workflow1.ui.WorkflowUiExperimentalApi
import com.squareup.workflow1.ui.compose.WorkflowRendering
import com.squareup.workflow1.ui.compose.renderAsState
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
@OptIn(WorkflowUiExperimentalApi::class)
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var shenanigansWorkflow: ShenanigansWorkflow

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            WorkflowShenanigansTheme(darkTheme = false) {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colors.background
                ) {

                    val rendering by shenanigansWorkflow.renderAsState(
                        props = ActivityAndProps(this, Unit), onOutput = {}
                    )

                    WorkflowRendering(
                        rendering,
                        viewEnvironment = ViewEnvironment(
                            mapOf(ViewRegistry to ShenanigansViewRegistry)
                        )
                    )
                }
            }
        }
    }
}
