package com.tuitionmanager.app.data.local

import androidx.room.TypeConverter
import com.tuitionmanager.app.domain.model.*

class Converters {
    @TypeConverter
    fun fromHomeworkStatus(status: HomeworkStatus?): String = status?.name ?: HomeworkStatus.NONE.name

    @TypeConverter
    fun toHomeworkStatus(value: String?): HomeworkStatus = try {
        value?.let { HomeworkStatus.valueOf(it) } ?: HomeworkStatus.NONE
    } catch (e: Exception) {
        HomeworkStatus.NONE
    }

    @TypeConverter
    fun fromStudentUnderstanding(understanding: StudentUnderstanding?): String =
        understanding?.name ?: StudentUnderstanding.GOOD.name

    @TypeConverter
    fun toStudentUnderstanding(value: String?): StudentUnderstanding = try {
        value?.let { StudentUnderstanding.valueOf(it) } ?: StudentUnderstanding.GOOD
    } catch (e: Exception) {
        StudentUnderstanding.GOOD
    }

    @TypeConverter
    fun fromExamStatus(status: ExamStatus?): String = status?.name ?: ExamStatus.PLANNED.name

    @TypeConverter
    fun toExamStatus(value: String?): ExamStatus = try {
        value?.let { ExamStatus.valueOf(it) } ?: ExamStatus.PLANNED
    } catch (e: Exception) {
        ExamStatus.PLANNED
    }

    @TypeConverter
    fun fromExamType(type: ExamType?): String = type?.name ?: ExamType.CHAPTER_TEST.name

    @TypeConverter
    fun toExamType(value: String?): ExamType = try {
        value?.let { ExamType.valueOf(it) } ?: ExamType.CHAPTER_TEST
    } catch (e: Exception) {
        ExamType.CHAPTER_TEST
    }

    @TypeConverter
    fun fromSessionStatus(status: SessionStatus?): String = status?.name ?: SessionStatus.SCHEDULED.name

    @TypeConverter
    fun toSessionStatus(value: String?): SessionStatus = try {
        value?.let { SessionStatus.valueOf(it) } ?: SessionStatus.SCHEDULED
    } catch (e: Exception) {
        SessionStatus.SCHEDULED
    }

    @TypeConverter
    fun fromGuardianChannel(channel: GuardianPreferredChannel?): String =
        channel?.name ?: GuardianPreferredChannel.WHATSAPP.name

    @TypeConverter
    fun toGuardianChannel(value: String?): GuardianPreferredChannel = try {
        value?.let { GuardianPreferredChannel.valueOf(it) } ?: GuardianPreferredChannel.WHATSAPP
    } catch (e: Exception) {
        GuardianPreferredChannel.WHATSAPP
    }

    @TypeConverter
    fun fromGuardianLanguage(language: GuardianReportLanguage?): String =
        language?.name ?: GuardianReportLanguage.EN.name

    @TypeConverter
    fun toGuardianLanguage(value: String?): GuardianReportLanguage = try {
        value?.let { GuardianReportLanguage.valueOf(it) } ?: GuardianReportLanguage.EN
    } catch (e: Exception) {
        GuardianReportLanguage.EN
    }

    @TypeConverter
    fun fromGuardianFormat(format: GuardianReportFormat?): String =
        format?.name ?: GuardianReportFormat.TEXT_SUMMARY.name

    @TypeConverter
    fun toGuardianFormat(value: String?): GuardianReportFormat = try {
        value?.let { GuardianReportFormat.valueOf(it) } ?: GuardianReportFormat.TEXT_SUMMARY
    } catch (e: Exception) {
        GuardianReportFormat.TEXT_SUMMARY
    }
}
