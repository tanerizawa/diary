package com.psy.deardiary.features.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.psy.deardiary.data.model.UserProfile
import com.psy.deardiary.data.repository.Result
import com.psy.deardiary.data.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ProfileUiState(
    val isLoading: Boolean = true,
    val email: String = "",
    val name: String = "",
    val bio: String = "",
    val message: String? = null
)

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val userRepository: UserRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState = _uiState.asStateFlow()

    init { loadProfile() }

    fun loadProfile() {
        viewModelScope.launch {
            when (val result = userRepository.getProfile()) {
                is Result.Success -> applyProfile(result.data)
                is Result.Error -> _uiState.update { it.copy(isLoading = false, message = result.message) }
            }
        }
    }

    private fun applyProfile(profile: UserProfile) {
        _uiState.update {
            it.copy(
                isLoading = false,
                email = profile.email,
                name = profile.name.orEmpty(),
                bio = profile.bio.orEmpty()
            )
        }
    }

    fun updateProfile(name: String, bio: String) {
        viewModelScope.launch {
            when (val result = userRepository.updateProfile(name, bio)) {
                is Result.Success -> {
                    applyProfile(result.data)
                    _uiState.update { it.copy(message = "Profil diperbarui") }
                }
                is Result.Error -> _uiState.update { it.copy(message = result.message) }
            }
        }
    }

    fun onMessageShown() { _uiState.update { it.copy(message = null) } }
}
