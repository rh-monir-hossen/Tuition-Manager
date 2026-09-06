package com.tuitionmanager.app.di

import android.content.Context
import com.tuitionmanager.app.data.local.TuitionDatabase
import com.tuitionmanager.app.data.local.dao.*
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideTuitionDatabase(
        @ApplicationContext context: Context
    ): TuitionDatabase {
        return TuitionDatabase.buildDatabase(context)
    }

    @Provides
    fun provideStudentDao(db: TuitionDatabase): StudentDao = db.studentDao()

    @Provides
    fun provideStudentSubjectDao(db: TuitionDatabase): StudentSubjectDao = db.studentSubjectDao()

    @Provides
    fun provideScheduleDao(db: TuitionDatabase): ScheduleDao = db.scheduleDao()

    @Provides
    fun provideClassSessionDao(db: TuitionDatabase): ClassSessionDao = db.classSessionDao()

    @Provides
    fun provideStudentDiaryDao(db: TuitionDatabase): StudentDiaryDao = db.studentDiaryDao()

    @Provides
    fun provideExamDao(db: TuitionDatabase): ExamDao = db.examDao()

    @Provides
    fun provideExamResultDao(db: TuitionDatabase): ExamResultDao = db.examResultDao()

    @Provides
    fun providePaymentDao(db: TuitionDatabase): PaymentDao = db.paymentDao()

    @Provides
    fun provideMonthlyFeeDao(db: TuitionDatabase): MonthlyFeeDao = db.monthlyFeeDao()

    @Provides
    fun provideIncomeDao(db: TuitionDatabase): IncomeDao = db.incomeDao()

    @Provides
    fun provideExpenseDao(db: TuitionDatabase): ExpenseDao = db.expenseDao()

    @Provides
    fun provideNoteDao(db: TuitionDatabase): NoteDao = db.noteDao()

    @Provides
    fun provideTutorProfileDao(db: TuitionDatabase): TutorProfileDao = db.tutorProfileDao()

    @Provides
    fun provideRescheduleRecordDao(db: TuitionDatabase): RescheduleRecordDao = db.rescheduleRecordDao()

    @Provides
    fun provideBackupClassDao(db: TuitionDatabase): BackupClassDao = db.backupClassDao()

    @Provides
    fun provideAppSettingsDao(db: TuitionDatabase): AppSettingsDao = db.appSettingsDao()

    @Provides
    fun provideBackupMetadataDao(db: TuitionDatabase): BackupMetadataDao = db.backupMetadataDao()

    @Provides
    fun provideSyncMetadataDao(db: TuitionDatabase): SyncMetadataDao = db.syncMetadataDao()
}
