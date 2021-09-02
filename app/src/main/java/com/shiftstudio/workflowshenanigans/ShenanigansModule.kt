package com.shiftstudio.workflowshenanigans

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class ShenanigansModule {

    @Binds
    abstract fun bindShenanigansWorkflow(impl: ShenanigansWorkflowImpl): ShenanigansWorkflow
}