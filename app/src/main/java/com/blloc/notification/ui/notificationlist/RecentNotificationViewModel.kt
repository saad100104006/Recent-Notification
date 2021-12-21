package com.blloc.notification.ui.notificationlist

import android.app.NotificationManager
import android.os.Build
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.blloc.notification.data.preferences.Preferences
import com.blloc.notification.domain.entities.Notification
import com.blloc.notification.domain.repository.NotificationRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import timber.log.Timber
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId

@FlowPreview
@ExperimentalCoroutinesApi
class RecentNotificationViewModel(
    private val notificationRepository: NotificationRepository,
    private val preferences: Preferences,
    private val notificationManager: NotificationManager
) : ViewModel() {

    private val mutableState =
        MutableStateFlow(UiState(isTrackerEnable = preferences.notificationTrackerEnable))
    val state: Flow<UiState>
        get() = mutableState

    private val filterState = MutableStateFlow(FilterNotification.ALL)

    init {
        viewModelScope.launch {
            notificationRepository.observeActiveNotification()
                .flatMapLatest { list ->
                    filterState.map {
                        list.applyFilter(it) to it

                    }
                }
                .collect { (list, filter) ->
                    Timber.i("observeActive notification: $list")
                    updateState {
                        copy(
                            activeNotifications = list,
                            isEmpty = list.isEmpty(),
                            currentFilter = filter
                        )
                    }
                }
        }
        viewModelScope.launch {
            notificationRepository.observeNotification()
                .flatMapLatest { list ->
                    filterState.map {
                        list.applyFilter(it) to it
                    }
                }
                .collect { (list, filter) ->
                    updateState {
                        copy(
                            notifications = list,
                            isEmpty = list.isEmpty(),
                            currentFilter = filter
                        )
                    }
                }
        }
    }

    fun setFilter(filter: FilterNotification) {
        filterState.value = filter
    }

    fun trackerStateToggle() {
        preferences.run {
            notificationTrackerEnable = !notificationTrackerEnable
            updateState { copy(isTrackerEnable = notificationTrackerEnable) }
        }
    }

    private fun updateState(block: UiState.() -> UiState) {
        mutableState.value = block(mutableState.value)
    }

    enum class FilterNotification {
        ALL,
        ACTIVE,
    }

    data class UiState(
        val notifications: List<Notification> = emptyList(),
        val activeNotifications: List<Notification> = emptyList(),
        val isEmpty: Boolean = true,
        val currentFilter: FilterNotification = FilterNotification.ALL,
        val isTrackerEnable: Boolean = false
    )

    private fun List<Notification>.applyFilter(filter: FilterNotification): List<Notification> {

        Timber.i("active notifications: $this")
        return when (filter) {
            FilterNotification.ALL -> this
            FilterNotification.ACTIVE -> this
        }

    }

    private fun Long.toOffsetDateTime() =
        Instant.ofEpochMilli(this).atZone(ZoneId.systemDefault()).toOffsetDateTime()

    @Suppress("UNCHECKED_CAST")
    class Factory(private val viewModel: RecentNotificationViewModel) : ViewModelProvider.Factory {
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            return viewModel as T
        }
    }
}
