package com.shiftstudio.workflowshenanigans.login

import android.content.Context
import com.firebase.ui.auth.AuthUI
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

    // TODO: for now, we start with null so auth starts everytime time app is open, for testing stuff
    private val _currentUser = MutableStateFlow<User?>(null)
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
