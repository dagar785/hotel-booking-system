package com.hbs.feature.booking.ui

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hbs.feature.booking.domain.BookingSummary
import com.hbs.feature.booking.domain.FormErrors
import com.hbs.feature.booking.domain.GetBookingSummaryUseCase
import com.hbs.feature.booking.domain.GuestForm
import com.hbs.feature.booking.domain.GuestFormValidator
import com.hbs.feature.booking.domain.PayForBookingUseCase
import com.hbs.feature.booking.domain.PaymentState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

data class BookingUiState(
    val summary: BookingSummary? = null,
    val loadError: String? = null,
    val form: GuestForm = GuestForm(),
    val errors: FormErrors = FormErrors(),
    val payment: PaymentState = PaymentState.Idle,
) {
    val isPaid get() = payment is PaymentState.Success
    val canSubmit get() = summary != null && payment !is PaymentState.Processing && !isPaid
}

/** MVVM: the View observes one StateFlow and calls plain methods (no intent objects). */
@HiltViewModel
class BookingViewModel @Inject constructor(
    handle: SavedStateHandle,
    private val getSummary: GetBookingSummaryUseCase,
    private val payForBooking: PayForBookingUseCase,
    private val validator: GuestFormValidator,
) : ViewModel() {
    private val _state = MutableStateFlow(BookingUiState())
    val state: StateFlow<BookingUiState> = _state.asStateFlow()
    private var payJob: Job? = null

    init {
        val hotelId = checkNotNull(handle.get<String>(EXTRA_HOTEL_ID))
        val roomId = checkNotNull(handle.get<String>(EXTRA_ROOM_ID))
        val nights = handle.get<Int>(EXTRA_NIGHTS) ?: 1
        viewModelScope.launch {
            getSummary(hotelId, roomId, nights)
                .onSuccess { s -> _state.update { it.copy(summary = s) } }
                .onFailure { e -> _state.update { it.copy(loadError = e.message) } }
        }
    }

    fun onFormChanged(transform: (GuestForm) -> GuestForm) = _state.update { it.copy(form = transform(it.form)) }

    fun onPayClicked() {
        val s = _state.value
        val summary = s.summary ?: return
        if (!s.canSubmit || payJob?.isActive == true) return // double-tap guard
        val errors = validator.validate(s.form)
        _state.update { it.copy(errors = errors) }
        if (errors.hasErrors) return
        payJob = viewModelScope.launch {
            // A new key per attempt: a retry after a timeout can charge twice (TASKS.md T4).
            payForBooking(UUID.randomUUID().toString(), summary.total, s.form.cardNumber)
                .collect { p -> _state.update { it.copy(payment = p) } }
        }
    }

    companion object {
        const val EXTRA_HOTEL_ID = "hotelId"
        const val EXTRA_ROOM_ID = "roomId"
        const val EXTRA_NIGHTS = "nights"
    }
}
