package com.example.eudayan.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.eudayan.data.AuthRepository
import com.example.eudayan.network.AuthResponse
import com.example.eudayan.network.LoginRequest
import com.example.eudayan.network.SignupRequest
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(private val repository: AuthRepository) : ViewModel() {

    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState: StateFlow<AuthState> = _authState

    fun signup(name: String, email: String, password: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            try {
                val response = repository.signup(SignupRequest(name, email, password))
                if (response.isSuccessful && response.body() != null) {
                    _authState.value = AuthState.Success(response.body()!!)
                } else {
                    _authState.value = AuthState.Error("Could not create your account. Check your details and try again.")
                }
            } catch (e: Exception) {
                _authState.value = AuthState.Error("The account service is unavailable. Please try again later or continue as a guest.")
            }
        }
    }

    fun login(email: String, password: String, role: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            try {
                val response = repository.login(LoginRequest(email, password, role))
                if (response.isSuccessful && response.body() != null) {
                    _authState.value = AuthState.Success(response.body()!!)
                } else {
                    _authState.value = AuthState.Error("Could not sign in. Check your email and password.")
                }
            } catch (e: Exception) {
                _authState.value = AuthState.Error("The account service is unavailable. Please try again later or continue as a guest.")
            }
        }
    }

    fun resetAuthState() {
        _authState.value = AuthState.Idle
    }
}

sealed class AuthState {
    object Idle : AuthState()
    object Loading : AuthState()
    data class Success(val data: AuthResponse) : AuthState()
    data class Error(val message: String) : AuthState()
}
