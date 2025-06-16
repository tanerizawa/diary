// File BARU: app/src/main/java/com/psy/deardiary/features/crisis_support/CrisisSupportViewModel.kt

package com.psy.deardiary.features.crisis_support.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.psy.deardiary.data.datastore.UserPreferencesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class CrisisSupportViewModel @Inject constructor(
    userPreferencesRepository: UserPreferencesRepository
) : ViewModel() {

    val emergencyContact = userPreferencesRepository.emergencyContact
        .map { it }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = null
        )
}