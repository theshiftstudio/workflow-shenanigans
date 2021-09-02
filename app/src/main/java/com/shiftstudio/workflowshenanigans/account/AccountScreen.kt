@file:OptIn(WorkflowUiExperimentalApi::class)

package com.shiftstudio.workflowshenanigans.account

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material.Button
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.squareup.workflow1.ui.ViewRegistry
import com.squareup.workflow1.ui.WorkflowUiExperimentalApi
import com.squareup.workflow1.ui.compose.composeViewFactory

private val LoadingUserBinding = composeViewFactory<LoadingUserRendering> { _, _ ->
    Surface(color = MaterialTheme.colors.background) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            CircularProgressIndicator()
        }
    }
}

private val UserRenderingBinding = composeViewFactory<UserRendering> { rendering, _ ->
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = rendering.user.name)
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = { rendering.onSignOut() }) {
            Text(text = "Sign out")
        }
    }
}

val AccountViewRegistry = ViewRegistry(LoadingUserBinding, UserRenderingBinding)
