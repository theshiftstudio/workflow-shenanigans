package com.shiftstudio.workflowshenanigans.login

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityComponent

@Module
@InstallIn(ActivityComponent::class)
abstract class LoginModule {

    @Binds
    abstract fun bindLoginWorkflow(impl: LoginWorkflowImpl): LoginWorkflow
}
