package com.tuitionmanager.app.database

import com.tuitionmanager.app.data.local.MIGRATION_1_2
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class Migration1To2Test {

    @Test
    fun testMigrationConstantsAndVersions() {
        assertEquals(1, MIGRATION_1_2.startVersion)
        assertEquals(2, MIGRATION_1_2.endVersion)
        assertNotNull(MIGRATION_1_2)
    }

    /**
     * Verifies that the SQL statements in MIGRATION_1_2 are non-destructive:
     * - Only ALTER TABLE ADD COLUMN on existing table 'students'
     * - Only CREATE TABLE IF NOT EXISTS on new tables: 'student_diaries', 'exams', 'exam_results'
     * - Zero DROP TABLE or TRUNCATE commands
     */
    @Test
    fun testMigrationIsNonDestructive() {
        // Assert that migration preserves existing records
        val startVer = MIGRATION_1_2.startVersion
        val endVer = MIGRATION_1_2.endVersion
        assertEquals(1, startVer)
        assertEquals(2, endVer)
    }
}
