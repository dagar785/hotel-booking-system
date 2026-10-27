package com.hbs.core.data.di

import com.hbs.core.data.FakeHotelRepository
import com.hbs.core.data.HotelRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class DataModule {
    @Binds @Singleton
    abstract fun hotelRepository(impl: FakeHotelRepository): HotelRepository
}
