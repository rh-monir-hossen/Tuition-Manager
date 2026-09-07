package com.tuitionmanager.app.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import com.tuitionmanager.app.data.local.dao.*
import com.tuitionmanager.app.data.local.entity.*

@Database(
    entities = [
        TutorProfileEntity::class,
        StudentEntity::class,
        StudentSubjectEntity::class,
        ScheduleEntity::class,
        ClassSessionEntity::class,
        RescheduleRecordEntity::class,
        BackupClassEntity::class,
        StudentDiaryEntity::class,
        ExamEntity::class,
        ExamResultEntity::class,
        MonthlyFeeEntity::class,
        PaymentEntity::class,
        IncomeEntity::class,
        ExpenseEntity::class,
        NoteEntity::class,
        AppSettingsEntity::class,
        BackupMetadataEntity::class,
        SyncMetadataEntity::class
    ],
    version = 3,
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class TuitionDatabase : RoomDatabase() {

    abstract fun tutorProfileDao(): TutorProfileDao
    abstract fun studentDao(): StudentDao
    abstract fun studentSubjectDao(): StudentSubjectDao
    abstract fun scheduleDao(): ScheduleDao
    abstract fun classSessionDao(): ClassSessionDao
    abstract fun rescheduleRecordDao(): RescheduleRecordDao
    abstract fun backupClassDao(): BackupClassDao
    abstract fun studentDiaryDao(): StudentDiaryDao
    abstract fun examDao(): ExamDao
    abstract fun examResultDao(): ExamResultDao
    abstract fun monthlyFeeDao(): MonthlyFeeDao
    abstract fun paymentDao(): PaymentDao
    abstract fun incomeDao(): IncomeDao
    abstract fun expenseDao(): ExpenseDao
    abstract fun noteDao(): NoteDao
    abstract fun appSettingsDao(): AppSettingsDao
    abstract fun backupMetadataDao(): BackupMetadataDao
    abstract fun syncMetadataDao(): SyncMetadataDao

    companion object {
        const val DATABASE_NAME = "tuition_manager.db"

        fun buildDatabase(context: Context): TuitionDatabase {
            return Room.databaseBuilder(
                context.applicationContext,
                TuitionDatabase::class.java,
                DATABASE_NAME
            )
                .addMigrations(MIGRATION_1_2, MIGRATION_2_3)
                .addCallback(object : Callback() {
                    override fun onOpen(db: SupportSQLiteDatabase) {
                        super.onOpen(db)
                        db.execSQL("PRAGMA foreign_keys = ON;")
                    }
                })
                .build()
        }
    }
}
