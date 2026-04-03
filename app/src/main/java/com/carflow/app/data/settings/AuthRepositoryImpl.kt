package com.carflow.app.data.settings

import com.carflow.app.data.settings.AuthRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepositoryImpl @Inject constructor() : AuthRepository {
    private var authToken: String? = null

    override fun getAuthToken(): String? = authToken

    override fun isAuthenticated(): Boolean = !authToken.isNullOrBlank()

    fun setAuthToken(token: String) {
        authToken = token
    }

    fun clearAuthToken() {
        authToken = null
    }
}
