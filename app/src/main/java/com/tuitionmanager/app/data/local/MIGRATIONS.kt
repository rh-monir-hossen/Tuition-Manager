package com.tuitionmanager.app.data.local

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {
        // 1. Add guardian communication columns to existing students table
        db.execSQL(
            """
            ALTER TABLE students 
            ADD COLUMN guardianPreferredChannel TEXT NOT NULL DEFAULT 'WHATSAPP'
            """.trimIndent()
        )
        db.execSQL(
            """
            ALTER TABLE students 
            ADD COLUMN isGuardianProgressSharingEnabled INTEGER NOT NULL DEFAULT 1
            """.trimIndent()
        )
        db.execSQL(
            """
            ALTER TABLE students 
            ADD COLUMN guardianReportLanguage TEXT NOT NULL DEFAULT 'EN'
            """.trimIndent()
        )
        db.execSQL(
            """
            ALTER TABLE students 
            ADD COLUMN guardianReportFormat TEXT NOT NULL DEFAULT 'TEXT_SUMMARY'
            """.trimIndent()
        )

        // 2. Create student_diaries table
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `student_diaries` (
                `id` TEXT NOT NULL,
                `studentId` TEXT NOT NULL,
                `subjectId` TEXT NOT NULL,
                `classSessionId` TEXT,
                `date` INTEGER NOT NULL,
                `topicTitle` TEXT NOT NULL,
                `whatWasTaught` TEXT NOT NULL,
                `homeworkAssigned` TEXT,
                `homeworkStatus` TEXT NOT NULL DEFAULT 'NONE',
                `practiceGiven` TEXT,
                `studentUnderstanding` TEXT NOT NULL DEFAULT 'GOOD',
                `teacherRemarks` TEXT,
                `nextClassPlan` TEXT,
                `createdAt` INTEGER NOT NULL,
                `updatedAt` INTEGER NOT NULL,
                `isDeleted` INTEGER NOT NULL DEFAULT 0,
                `deletedAt` INTEGER,
                PRIMARY KEY(`id`),
                FOREIGN KEY(`studentId`) REFERENCES `students`(`id`) ON DELETE CASCADE,
                FOREIGN KEY(`subjectId`) REFERENCES `student_subjects`(`id`) ON DELETE RESTRICT,
                FOREIGN KEY(`classSessionId`) REFERENCES `class_sessions`(`id`) ON DELETE SET NULL
            )
            """.trimIndent()
        )
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_student_diaries_studentId_date` ON `student_diaries` (`studentId`, `date`)")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_student_diaries_studentId_subjectId_date` ON `student_diaries` (`studentId`, `subjectId`, `date`)")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_student_diaries_classSessionId` ON `student_diaries` (`classSessionId`)")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_student_diaries_homeworkStatus` ON `student_diaries` (`homeworkStatus`)")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_student_diaries_isDeleted` ON `student_diaries` (`isDeleted`)")

        // 3. Create exams table
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `exams` (
                `id` TEXT NOT NULL,
                `studentId` TEXT NOT NULL,
                `subjectId` TEXT NOT NULL,
                `title` TEXT NOT NULL,
                `syllabusTopic` TEXT NOT NULL,
                `plannedDate` INTEGER NOT NULL,
                `plannedStartTimeMinutes` INTEGER NOT NULL,
                `durationMinutes` INTEGER NOT NULL,
                `totalMarks` REAL NOT NULL,
                `passingMarks` REAL,
                `examType` TEXT NOT NULL DEFAULT 'CHAPTER_TEST',
                `status` TEXT NOT NULL DEFAULT 'PLANNED',
                `notes` TEXT,
                `createdAt` INTEGER NOT NULL,
                `updatedAt` INTEGER NOT NULL,
                `isDeleted` INTEGER NOT NULL DEFAULT 0,
                `deletedAt` INTEGER,
                PRIMARY KEY(`id`),
                FOREIGN KEY(`studentId`) REFERENCES `students`(`id`) ON DELETE CASCADE,
                FOREIGN KEY(`subjectId`) REFERENCES `student_subjects`(`id`) ON DELETE RESTRICT
            )
            """.trimIndent()
        )
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_exams_studentId_plannedDate` ON `exams` (`studentId`, `plannedDate`)")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_exams_studentId_status` ON `exams` (`studentId`, `status`)")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_exams_plannedDate_status` ON `exams` (`plannedDate`, `status`)")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_exams_isDeleted` ON `exams` (`isDeleted`)")

        // 4. Create exam_results table
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `exam_results` (
                `id` TEXT NOT NULL,
                `examId` TEXT NOT NULL,
                `studentId` TEXT NOT NULL,
                `actualExamDate` INTEGER NOT NULL,
                `marksObtained` REAL NOT NULL,
                `isPassed` INTEGER NOT NULL,
                `studentStrengths` TEXT,
                `studentWeaknesses` TEXT,
                `recommendations` TEXT,
                `teacherRemarks` TEXT,
                `gradedAt` INTEGER NOT NULL,
                `createdAt` INTEGER NOT NULL,
                `updatedAt` INTEGER NOT NULL,
                `isDeleted` INTEGER NOT NULL DEFAULT 0,
                `deletedAt` INTEGER,
                PRIMARY KEY(`id`),
                FOREIGN KEY(`examId`) REFERENCES `exams`(`id`) ON DELETE CASCADE,
                FOREIGN KEY(`studentId`) REFERENCES `students`(`id`) ON DELETE CASCADE
            )
            """.trimIndent()
        )
        db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_exam_results_examId` ON `exam_results` (`examId`)")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_exam_results_studentId_actualExamDate` ON `exam_results` (`studentId`, `actualExamDate`)")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_exam_results_isDeleted` ON `exam_results` (`isDeleted`)")
    }
}

val MIGRATION_2_3 = object : Migration(2, 3) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE schedules ADD COLUMN location TEXT")
        db.execSQL("ALTER TABLE schedules ADD COLUMN notes TEXT")
    }
}
