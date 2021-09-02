package com.shiftstudio.workflowshenanigans

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityComponent

@Module
@InstallIn(ActivityComponent::class)
abstract class ShenanigansModule {

    @Binds
    abstract fun bindShenanigansWorkflow(impl: ShenanigansWorkflowImpl): ShenanigansWorkflow
}
