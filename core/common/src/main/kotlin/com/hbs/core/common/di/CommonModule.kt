package com.hbs.core.common.di

import com.hbs.core.common.DefaultDispatcherProvider
import com.hbs.core.common.DispatcherProvider
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object CommonModule {
    @Provides @Singleton
    fun dispatchers(): DispatcherProvider = DefaultDispatcherProvider
}
