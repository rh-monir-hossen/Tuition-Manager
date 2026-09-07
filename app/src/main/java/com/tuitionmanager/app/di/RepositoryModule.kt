package com.tuitionmanager.app.di

import com.tuitionmanager.app.data.repository.*
import com.tuitionmanager.app.domain.repository.*
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindStudentRepository(
        impl: StudentRepositoryImpl
    ): StudentRepository

    @Binds
    @Singleton
    abstract fun bindStudentSubjectRepository(
        impl: StudentSubjectRepositoryImpl
    ): StudentSubjectRepository

    @Binds
    @Singleton
    abstract fun bindStudentDiaryRepository(
        impl: StudentDiaryRepositoryImpl
    ): StudentDiaryRepository

    @Binds
    @Singleton
    abstract fun bindExamRepository(
        impl: ExamRepositoryImpl
    ): ExamRepository

    @Binds
    @Singleton
    abstract fun bindScheduleRepository(
        impl: ScheduleRepositoryImpl
    ): ScheduleRepository

    @Binds
    @Singleton
    abstract fun bindClassSessionRepository(
        impl: ClassSessionRepositoryImpl
    ): ClassSessionRepository

    @Binds
    @Singleton
    abstract fun bindRescheduleRepository(
        impl: RescheduleRepositoryImpl
    ): RescheduleRepository
}
