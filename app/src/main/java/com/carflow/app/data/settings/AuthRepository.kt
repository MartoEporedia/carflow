package com.carflow.app.data.settings

interface AuthRepository {
    fun getAuthToken(): String?
    fun isAuthenticated(): Boolean
}
