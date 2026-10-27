package com.hbs.feature.booking.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.EditText
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.getValue
import androidx.compose.runtime.collectAsState
import androidx.core.view.isVisible
import androidx.core.widget.doAfterTextChanged
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.hbs.feature.booking.databinding.ActivityBookingBinding
import com.hbs.feature.booking.domain.PaymentState
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class BookingActivity : AppCompatActivity() {
    private val viewModel: BookingViewModel by viewModels()
    private lateinit var binding: ActivityBookingBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBookingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.summaryCompose.setContent {
            MaterialTheme {
                val s by viewModel.state.collectAsState()
                BookingSummaryCard(s.summary, s.loadError)
            }
        }
        binding.etName.onChange { v -> viewModel.onFormChanged { it.copy(name = v) } }
        binding.etEmail.onChange { v -> viewModel.onFormChanged { it.copy(email = v) } }
        binding.etCard.onChange { v -> viewModel.onFormChanged { it.copy(cardNumber = v) } }
        binding.btnPay.setOnClickListener { viewModel.onPayClicked() }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) { viewModel.state.collect(::render) }
        }
    }

    private fun render(state: BookingUiState) {
        binding.layoutName.error = state.errors.name
        binding.layoutEmail.error = state.errors.email
        binding.layoutCard.error = state.errors.card
        binding.btnPay.isEnabled = state.canSubmit
        binding.progress.isVisible = state.payment is PaymentState.Processing
        binding.tvStatus.text = when (val p = state.payment) {
            PaymentState.Idle, PaymentState.Processing -> ""
            is PaymentState.Success -> "Booking confirmed! Reference ${p.transactionId}"
            is PaymentState.Failed -> p.message
        }
        if (state.isPaid) binding.stepper.transitionToEnd() else binding.stepper.transitionToStart()
    }

    private fun EditText.onChange(block: (String) -> Unit) = doAfterTextChanged { block(it?.toString().orEmpty()) }

    companion object {
        fun intent(context: Context, hotelId: String, roomId: String, nights: Int) =
            Intent(context, BookingActivity::class.java)
                .putExtra(BookingViewModel.EXTRA_HOTEL_ID, hotelId)
                .putExtra(BookingViewModel.EXTRA_ROOM_ID, roomId)
                .putExtra(BookingViewModel.EXTRA_NIGHTS, nights)
    }
}
