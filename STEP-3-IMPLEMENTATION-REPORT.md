# STEP 3 — Android Foundation & Database Layer Implementation Report

**Application Name:** Tuition Manager  
**Package:** `com.tuitionmanager.app`  
**Architecture:** Clean Architecture + MVVM + Offline-First SSOT (Single Source of Truth)  
**Database:** Android Jetpack Room Version 2 (with non-destructive `MIGRATION_1_2`)  
**DI Framework:** Dagger Hilt  
**Language & UI:** Kotlin, Kotlin Coroutines & Flow, Jetpack Compose Material 3  

---

## 1. Executive Summary

In accordance with the architectural blueprints established in **`STEP-1-ARCHITECTURE.md`** and **`STEP-2-ARCHITECTURE.md`**, the core Android foundation and local database layer have been implemented:

* **Android Project Hierarchy**: Configured root-level Gradle (`settings.gradle.kts`, `build.gradle.kts`, `gradle/libs.versions.toml`) and app module (`app/build.gradle.kts`, `app/src/main/AndroidManifest.xml`) with Jetpack Compose, Material 3, Room v2.6.1, Hilt 2.51.1, and Coroutines 1.8.1.
* **Room Database Version 2 (`TuitionDatabase.kt`)**: Implemented with SQLite Foreign Key enforcement (`PRAGMA foreign_keys = ON;`), UUIDv4 primary keys for multi-device sync, soft-delete tombstone semantics (`isDeleted = 0`, `deletedAt`), and UTC millisecond timestamps.
* **Safe Migration 1 -> 2 (`MIGRATION_1_2.kt`)**: Added non-destructive schema migrations that preserve all STEP 1 user data while introducing guardian communication preferences on `students`, alongside the complete `student_diaries`, `exams`, and `exam_results` tables.
* **18 Room Entities & 18 DAOs**: Complete implementation across core, schedule, academic, financial, and metadata categories.
* **Clean Architecture Domain & Data Layers**: Pure domain models (`Student`, `StudentDiary`, `Exam`, `ExamResult`), decoupled repository interfaces, and robust implementations with Kotlin Flow reactivity.
* **Domain Engines & Validation**: Strict mathematical overlap detection in `ScheduleConflictEngine`, deterministic percentage and grade categorization in `ExamEvaluationEngine`, and domain-level validators.
* **Unit Tests & Migration Verification**: Automated test suites covering schedule conflicts, exam grading rules, input validators, and schema migration integrity.

---

## 2. Room Database Architecture (Version 2)

### 2.1 Configuration
* **Database Class**: `com.tuitionmanager.app.data.local.TuitionDatabase`
* **Current Version**: `2`
* **File Name**: `tuition_manager.db`
* **Foreign Key Support**: Automatically enforced via `PRAGMA foreign_keys = ON;` in `RoomDatabase.Callback.onOpen`.
* **Export Schema**: Enabled (`schemas/` output configured in `app/build.gradle.kts`).

### 2.2 Migration 1 -> 2 Strategy
The migration is strictly **non-destructive** (zero `DROP TABLE` or destructive truncation):
1. **`students` table extension**:
   ```sql
   ALTER TABLE students ADD COLUMN guardianPreferredChannel TEXT NOT NULL DEFAULT 'WHATSAPP';
   ALTER TABLE students ADD COLUMN isGuardianProgressSharingEnabled INTEGER NOT NULL DEFAULT 1;
   ALTER TABLE students ADD COLUMN guardianReportLanguage TEXT NOT NULL DEFAULT 'EN';
   ALTER TABLE students ADD COLUMN guardianReportFormat TEXT NOT NULL DEFAULT 'TEXT_SUMMARY';
   ```
2. **`student_diaries` table creation**:
   Creates table with foreign keys `CASCADE` on `students`, `RESTRICT` on `student_subjects`, and `SET_NULL` on `class_sessions`, complete with composite indexes on `(studentId, date)`, `(studentId, subjectId, date)`, and index on `homeworkStatus`.
3. **`exams` table creation**:
   Creates table with foreign key `CASCADE` on `students`, `RESTRICT` on `student_subjects`, and indexes on `(studentId, plannedDate)` and status.
4. **`exam_results` table creation**:
   Creates table with foreign key `CASCADE` on `exams` and `students`, a `UNIQUE` index on `examId`, and composite index on `(studentId, actualExamDate)`.

---

## 3. Implemented Room Entities Inventory (18 Tables)

| # | Entity Name | Table Name | Key Constraints & Indices | Soft Delete | Description |
|---|-------------|------------|---------------------------|-------------|-------------|
| 1 | `TutorProfileEntity` | `tutor_profile` | Primary Key: `id` | Yes | Tutor credentials, hourly rates, default fees, currency |
| 2 | `StudentEntity` | `students` | Indices: `isActive`, `isDeleted` | Yes | Student registry, contact, guardian preferences, billing |
| 3 | `StudentSubjectEntity` | `student_subjects` | FK -> `students` (CASCADE), Index: `studentId` | Yes | Subjects associated with individual students |
| 4 | `ScheduleEntity` | `schedules` | FK -> `students` (CASCADE), FK -> `student_subjects` (RESTRICT), Indices: `dayOfWeek`, `isActive` | Yes | Weekly recurring schedule rules |
| 5 | `ClassSessionEntity` | `class_sessions` | FK -> `students` (CASCADE), FK -> `schedules` (SET_NULL), FK -> `student_subjects` (RESTRICT) | Yes | Realized class occurrences and attendance |
| 6 | `RescheduleRecordEntity` | `reschedule_records` | FK -> `class_sessions` (CASCADE on original & new) | Yes | Rescheduled session tracking and audit audit trail |
| 7 | `BackupClassEntity` | `backup_classes` | FK -> `class_sessions` (CASCADE on missed & backup) | Yes | Makeup / substitute class pairings |
| 8 | `StudentDiaryEntity` | `student_diaries` | FK -> `students` (CASCADE), FK -> `student_subjects` (RESTRICT), FK -> `class_sessions` (SET_NULL) | Yes | Pedagogical class log, homework, practice, comprehension |
| 9 | `ExamEntity` | `exams` | FK -> `students` (CASCADE), FK -> `student_subjects` (RESTRICT), Indices: `studentId, plannedDate` | Yes | Exam scope, date, time, total marks, type |
| 10 | `ExamResultEntity` | `exam_results` | FK -> `exams` (CASCADE), FK -> `students` (CASCADE), Unique Index: `examId` | Yes | Scored marks, pass/fail, diagnostic strengths/weaknesses |
| 11 | `MonthlyFeeEntity` | `monthly_fees` | FK -> `students` (CASCADE), Unique Index: `(studentId, year, month)` | Yes | Monthly generated tuition fees and ledger debts |
| 12 | `PaymentEntity` | `payments` | FK -> `students` (CASCADE), FK -> `monthly_fees` (RESTRICT) | Yes | Financial transactions, receipts, payment modes |
| 13 | `IncomeEntity` | `income` | FK -> `payments` (SET_NULL), Index: `date, category` | Yes | Ledger income records |
| 14 | `ExpenseEntity` | `expenses` | Index: `date, category` | Yes | Operational expenses and receipt media URIs |
| 15 | `NoteEntity` | `notes` | FK -> `students` (CASCADE), Index: `studentId, isPinned` | Yes | Pinned tutor notes and student remarks |
| 16 | `AppSettingsEntity` | `app_settings` | Primary Key: `key` | No | Key-value application runtime configurations |
| 17 | `BackupMetadataEntity` | `backup_metadata` | Primary Key: `id`, Index: `timestamp` | No | Local & cloud backup audit records |
| 18 | `SyncMetadataEntity` | `sync_metadata` | Primary Key: `entityType` | No | Synchronization watermarks and device lineage |

---

## 4. DAOs & Reactive Query Architecture

All DAOs are located in `app/src/main/java/com/tuitionmanager/app/data/local/dao/Daos.kt`:
* **Reactive Kotlin Flows**: Read queries return `Flow<List<T>>` or `Flow<T?>` which automatically emit updated states when underlying database tables change.
* **Coroutines Suspend Functions**: All write operations (`insert`, `update`, `softDelete`) are marked `suspend`.
* **Soft Deletes Default Filtering**: Read queries explicitly enforce `WHERE isDeleted = 0` to ensure deleted records are invisible to UI while remaining preserved for cloud synchronization.
* **Date-Range & Substring Search**:
  * `StudentDiaryDao.filterDiaryEntries`: Range query filtering by `studentId`, optional `subjectId`, and `[startDate, endDate]`.
  * `StudentDiaryDao.searchDiaryEntries`: Full-text substring search across `topicTitle`, `whatWasTaught`, and `homeworkAssigned`.
  * `ExamDao.observeUpcomingExams`: Queries planned exams where `plannedDate >= todayEpochMs`.
  * `ExamResultDao.observeResultsByDateRange`: Generates chronological progress evaluation data for guardian reporting.

---

## 5. Domain Engines & Math Logic

### 5.1 Schedule Conflict Engine (`ScheduleConflictEngine.kt`)
* Implements the strict mathematical interval overlap rule:
  $$\text{Overlap} \iff (\text{newStart} < \text{existingEnd}) \land (\text{newEnd} > \text{existingStart})$$
* Tested against edge conditions:
  - Exact adjacent times (e.g. 10:00-11:00 and 11:00-12:00) do **not** conflict.
  - Inactive or soft-deleted schedules are automatically disregarded.

### 5.2 Exam Evaluation Engine (`ExamEvaluationEngine.kt`)
* Computes on-demand percentage:
  $$\text{Percentage} = \frac{\text{marksObtained}}{\text{totalMarks}} \times 100$$
* Enforces strict validation: $0.0 \le \text{marksObtained} \le \text{totalMarks}$.
* Deterministic Grade Mapping:
  - $\ge 80\% \rightarrow \mathbf{A+\ (Outstanding)}$
  - $\ge 70\% \rightarrow \mathbf{A\ (Excellent)}$
  - $\ge 60\% \rightarrow \mathbf{A-\ (Very\ Good)}$
  - $\ge 50\% \rightarrow \mathbf{B\ (Satisfactory)}$
  - $\ge 40\% \rightarrow \mathbf{C\ (Pass)}$
  - $< 40\% \rightarrow \mathbf{F\ (Needs\ Attention)}$
* Pass/Fail Determination:
  - Evaluated against `passingMarks` if explicitly specified.
  - Defaults to $40\%$ benchmark if `passingMarks` is null.

---

## 6. Dependency Injection & Project Structure

The project employs Hilt for zero-boilerplate singleton and scoped dependency graph management:
* **`DatabaseModule.kt`**: Provides the singleton `TuitionDatabase` and exposes every individual DAO.
* **`RepositoryModule.kt`**: Binds clean domain repository interfaces (`StudentRepository`, `StudentDiaryRepository`, `ExamRepository`) to their concrete Room data implementations.
* **`EngineModule.kt`**: Injects stateless domain engines (`ScheduleConflictEngine`, `ExamEvaluationEngine`).

### Directory Layout
```
app/src/main/java/com/tuitionmanager/app/
├── TuitionManagerApp.kt            # Hilt Application entry point
├── MainActivity.kt                 # Single Activity with Compose
├── data/
│   ├── local/
│   │   ├── TuitionDatabase.kt      # Room Database Version 2
│   │   ├── Converters.kt           # Room Enum & Type Converters
│   │   ├── MIGRATIONS.kt           # Migration 1 -> 2
│   │   ├── dao/
│   │   │   └── Daos.kt             # 18 DAOs with Flow & Suspend methods
│   │   └── entity/
│   │       └── Entities.kt         # 18 Room SQLite Entities
│   └── repository/
│       └── RepositoryImpls.kt      # Offline-first repositories + Entity <-> Domain mappers
├── di/
│   ├── DatabaseModule.kt           # Hilt Database & DAO providers
│   ├── RepositoryModule.kt         # Hilt Repository bindings
│   └── EngineModule.kt             # Hilt Engine providers
├── domain/
│   ├── engine/
│   │   ├── ScheduleConflictEngine.kt
│   │   └── ExamEvaluationEngine.kt
│   ├── model/
│   │   ├── Enums.kt                # Type-safe enums
│   │   └── DomainModels.kt         # Pure Kotlin domain data classes
│   ├── repository/
│   │   └── Repositories.kt         # Repository contracts
│   ├── usecase/
│   │   └── UseCases.kt             # Student, Diary, Exam Use Cases
│   └── validation/
│       └── Validators.kt           # Domain validation rules
├── ui/
│   ├── HomeScreen.kt               # Verification Screen
│   └── theme/
│       ├── Color.kt
│       ├── Theme.kt
│       └── Type.kt
└── utils/
    ├── AppResult.kt                # Functional Success / Error wrapper
    └── TimeUtils.kt                # UTC timestamp & local formatting utilities
```

---

## 7. Test Suite Verification

Four test suites were created under `app/src/test/java/com/tuitionmanager/app/`:
1. **`ScheduleConflictEngineTest.kt`**: Confirmed overlap detection, adjacent slot safety, and inactive schedule bypass.
2. **`ExamEvaluationEngineTest.kt`**: Confirmed exact percentage calculation, out-of-bounds error throwing, grade level assignment, and custom passing threshold handling.
3. **`ValidatorsTest.kt`**: Confirmed domain-level input validation across Student, Diary, Exam, and ExamResult models.
4. **`Migration1To2Test.kt`**: Verified start version 1 and end version 2 contracts and confirmed non-destructive schema commands.

---

## 8. Readiness for STEP 4

The Android foundation and local Room database layer are in place. The codebase is prepared for:
* **STEP 4A**: Jetpack Compose UI Screens (Student List, Student Profile, Diary Entry Form, Exam Scheduler, Exam Grading Sheet, Guardian Progress Share Sheet).
* **STEP 4B**: Google Sheets API v4 Integration (17-tab synchronization with Last-Write-Wins and Tombstone sync).
* **STEP 4C**: Guardian Progress Report Generator (Multilingual text and formatted PDF exports).
