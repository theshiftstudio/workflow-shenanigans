package com.shiftstudio.workflowshenanigans.account

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityComponent

@Module
@InstallIn(ActivityComponent::class)
abstract class AccountModule {

    @Binds
    abstract fun bindAccountWorkflow(impl: AccountWorkflowImpl): AccountWorkflow
}
