package com.shiftstudio.workflowshenanigans.login

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class LoginModule {

    @Binds
    abstract fun bindLoginWorkflow(impl: LoginWorkflowImpl): LoginWorkflow
}