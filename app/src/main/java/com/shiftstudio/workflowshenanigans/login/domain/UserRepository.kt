package com.shiftstudio.workflowshenanigans.login.domain

import android.content.Context
import com.firebase.ui.auth.AuthUI
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserRepository @Inject constructor(
    @ApplicationContext private val context: Context,
) {

    private val _currentUser = MutableStateFlow(FirebaseAuth.getInstance().currentUser?.toUser())
    val currentUser: Flow<User?> = _currentUser.asStateFlow()

    fun setCurrentUser(user: User) {
        _currentUser.value = user
    }

    suspend fun signOut() {
        runCatching {
            AuthUI.getInstance()
                .signOut(context)
                .await()
            _currentUser.value = null
        }
    }
}

fun FirebaseUser.toUser(): User = User(
    uid = this.uid,
    name = this.displayName ?: "",
    email = this.email ?: "",
)
