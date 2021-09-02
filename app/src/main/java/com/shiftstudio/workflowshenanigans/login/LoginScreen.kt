@file:OptIn(WorkflowUiExperimentalApi::class)

package com.shiftstudio.workflowshenanigans.login

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.squareup.workflow1.ui.ViewFactory
import com.squareup.workflow1.ui.ViewRegistry
import com.squareup.workflow1.ui.WorkflowUiExperimentalApi
import com.squareup.workflow1.ui.compose.composeViewFactory

val AuthorizingRenderingBinding = composeViewFactory<AuthorizingRendering> { rendering, _ ->
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = rendering.message)
    }
}

val AuthorizationFailedBinding: ViewFactory<AuthorizationFailed> =
    composeViewFactory { rendering, _ ->
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = rendering.cause)
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = { rendering.onRetry() }) {
                Text(text = "Retry")
            }
        }
    }

val LoginViewRegistry = ViewRegistry(AuthorizingRenderingBinding, AuthorizationFailedBinding)
