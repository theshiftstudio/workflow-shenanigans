package com.shiftstudio.workflowshenanigans.welcome

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class WelcomeModule {

    @Binds
    abstract fun bindWelcomeWorkflow(impl: WelcomeWorkflowImpl): WelcomeWorkflow
}
