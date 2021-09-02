package com.shiftstudio.workflowshenanigans.login.domain

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class User(
    val uid: String,
    val name: String = "",
    val email: String = "",
) : Parcelable
