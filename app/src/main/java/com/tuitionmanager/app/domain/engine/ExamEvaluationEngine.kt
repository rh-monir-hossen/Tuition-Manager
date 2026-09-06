package com.tuitionmanager.app.domain.engine

class ExamEvaluationEngine {

    fun calculatePercentage(marksObtained: Double, totalMarks: Double): Double {
        require(totalMarks > 0.0) { "Total marks must be strictly positive" }
        require(marksObtained >= 0.0) { "Marks obtained cannot be negative" }
        require(marksObtained <= totalMarks) { "Marks obtained ($marksObtained) cannot exceed total marks ($totalMarks)" }

        return (marksObtained / totalMarks) * 100.0
    }

    fun determineIsPassed(marksObtained: Double, totalMarks: Double, passingMarks: Double?): Boolean {
        return if (passingMarks != null) {
            marksObtained >= passingMarks
        } else {
            calculatePercentage(marksObtained, totalMarks) >= 40.0
        }
    }

    fun determineGradeLevel(percentage: Double): String {
        return when {
            percentage >= 80.0 -> "A+ (Outstanding)"
            percentage >= 70.0 -> "A (Excellent)"
            percentage >= 60.0 -> "A- (Very Good)"
            percentage >= 50.0 -> "B (Satisfactory)"
            percentage >= 40.0 -> "C (Pass)"
            else -> "F (Needs Attention)"
        }
    }
}
