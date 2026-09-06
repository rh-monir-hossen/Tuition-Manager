package com.tuitionmanager.app.di

import com.tuitionmanager.app.domain.engine.ExamEvaluationEngine
import com.tuitionmanager.app.domain.engine.ScheduleConflictEngine
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object EngineModule {

    @Provides
    @Singleton
    fun provideScheduleConflictEngine(): ScheduleConflictEngine {
        return ScheduleConflictEngine()
    }

    @Provides
    @Singleton
    fun provideExamEvaluationEngine(): ExamEvaluationEngine {
        return ExamEvaluationEngine()
    }
}
