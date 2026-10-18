package com.example.doodleslots.di

import android.content.Context
import com.example.doodleslots.data.DailyQuestRepositoryImpl
import com.example.doodleslots.data.GameRepositoryImpl
import com.example.doodleslots.data.TunableParamsRepositoryImpl
import com.example.doodleslots.domain.repository.DailyQuestRepository
import com.example.doodleslots.domain.repository.GameRepository
import com.example.doodleslots.domain.repository.TunableParamsRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideGameRepository(@ApplicationContext context: Context): GameRepository =
        GameRepositoryImpl(context)

    @Provides
    @Singleton
    fun provideTunableParamsRepository(@ApplicationContext context: Context): TunableParamsRepository =
        TunableParamsRepositoryImpl(context)

    @Provides
    @Singleton
    fun provideDailyQuestRepository(@ApplicationContext context: Context): DailyQuestRepository =
        DailyQuestRepositoryImpl(context)
}