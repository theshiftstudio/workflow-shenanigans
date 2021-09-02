package com.shiftstudio.workflowshenanigans.account

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class AccountModule {

    @Binds
    abstract fun bindAccountWorkflow(impl: AccountWorkflowImpl): AccountWorkflow
}