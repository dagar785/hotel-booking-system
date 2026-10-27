package com.hbs.payment.di

import com.hbs.payment.FakePaymentGateway
import com.hbs.payment.PaymentGateway
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
object PaymentModule {
    @Provides
    fun paymentGateway(): PaymentGateway = FakePaymentGateway()
}
