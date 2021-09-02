@file:OptIn(WorkflowUiExperimentalApi::class)

package com.shiftstudio.workflowshenanigans.account

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.squareup.workflow1.ui.ViewRegistry
import com.squareup.workflow1.ui.WorkflowUiExperimentalApi
import com.squareup.workflow1.ui.compose.composeViewFactory

val LoadingUserBinding = composeViewFactory<LoadingUserRendering> { rendering, _ ->
    // probably no-op
}

val UserRenderingBinding = composeViewFactory<UserRendering> { rendering, _ ->
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = rendering.user.name)
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = { rendering.onSignOut() }) {
            Text(text = "Sign out")
        }
    }
}

val AccountViewRegistry = ViewRegistry(LoadingUserBinding, UserRenderingBinding)
