package com.shiftstudio.workflowshenanigans.infrastructure

import android.app.Application
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

@HiltAndroidApp
class ShenanigansApplication : Application() {

    val appScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
}
