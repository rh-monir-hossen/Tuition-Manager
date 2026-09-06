# Technical Architecture & System Blueprint: Tuition Manager (Android)

---

## 1. Recommended Architecture

Tuition Manager is designed as a **strictly offline-first**, deterministic Android application following **Clean Architecture** and **Modern Android Architecture (MVI / MVVM with Uni-directional Data Flow)**.

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                             PRESENTATION LAYER                              │
│  Jetpack Compose Screens + Material 3 Components                            │
│  StateFlow<UiState> ◄────────────────────────────── ViewModels               │
│                                                     (UiEvents & Actions)    │
└──────────────────────────────────────┬──────────────────────────────────────┘
                                       │ calls
┌──────────────────────────────────────▼──────────────────────────────────────┐
│                                DOMAIN LAYER                                 │
│  Pure Kotlin (No Android SDK dependencies)                                  │
│  UseCases (e.g., DetectScheduleConflictUseCase, CalculateStudentBalanceUC)  │
│  Domain Models (Clean Immutable POJOs)                                      │
│  Repository Interfaces & Conflict Engine Core                               │
└──────────────────────────────────────┬──────────────────────────────────────┘
                                       │ implements
┌──────────────────────────────────────▼──────────────────────────────────────┐
│                                 DATA LAYER                                  │
│  Repository Implementations (Offline-first orchestration)                   │
│  ┌─────────────────────────────────┐   ┌──────────────────────────────────┐ │
│  │     Local Storage (Room DB)     │   │      Cloud Backup Engine         │ │
│  │   • Primary Source of Truth     │   │   • Google Drive/Sheets v4 REST  │ │
│  │   • SQLite / Room DAOs          │   │   • Snapshot & Incremental Sync  │ │
│  │   • EncryptedSharedPreferences  │   │   • WorkManager (Periodic/Manual)│ │
│  └─────────────────────────────────┘   └──────────────────────────────────┘ │
└─────────────────────────────────────────────────────────────────────────────┘
```

### Architectural Principles
1. **Single Source of Truth (SSOT):** The local Room database is the authoritative data source. The UI never queries or waits for cloud APIs.
2. **Unidirectional Data Flow (UDF):**
   * UI components emit user events (`UiEvent`) to the `ViewModel`.
   * The `ViewModel` executes domain `UseCases`.
   * `UseCases` call `Repository` interfaces.
   * `Repository` streams updates using reactive Kotlin `Flow<T>` from Room DAOs.
   * The `ViewModel` transforms `Flow<T>` into an immutable `StateFlow<UiState>` rendered by Jetpack Compose.
3. **No Mixed Financial Concepts:** Financial obligations (`MonthlyFee`), receipts (`Payment`), and general cash flows (`Income`, `Expense`) reside in distinct ledger tables. Account balances are derived, never stored as raw mutable state.
4. **Timezone Uniformity:** All persistence, comparisons, and network exchanges use **Epoch Milliseconds (UTC)**. Conversion to **Asia/Dhaka (UTC+6)** is strictly confined to presentation formatting.

---

## 2. Database ER & Data Model

```
                    ┌──────────────────┐
                    │   TutorProfile   │
                    └────────┬─────────┘
                             │ 1:N
                             ▼
┌──────────────────┐ 1:N    ┌──────────────────┐ 1:N    ┌──────────────────┐
│   MonthlyFee     │◄───────┤     Student      ├───────►│  StudentSubject  │
└──────────────────┘        └────────┬─────────┘        └────────┬─────────┘
                             │       │                           │
                             │ 1:N   │ 1:N                       │ 1:N
                             │       ▼                           ▼
                             │      ┌──────────────────────────────┐
                             │      │           Schedule           │
                             │      └──────────────┬───────────────┘
                             │                     │ 1:N
                             ▼                     ▼
┌──────────────────┐ 1:N    ┌──────────────────────────────────────┐
│     Payment      │◄───────┤             ClassSession             │
└──────────────────┘        └──────────────┬───────────────────────┘
                                           │ 1:1
                                           ├───────────────────────┐
                                           ▼                       ▼
                                ┌──────────────────┐    ┌──────────────────┐
                                │ RescheduleRecord │    │   BackupClass    │
                                └──────────────────┘    └──────────────────┘

 Independent Financial & System Entities:
 ┌──────────────────┐    ┌──────────────────┐    ┌──────────────────┐
 │      Income      │    │     Expense      │    │       Note       │
 └──────────────────┘    └──────────────────┘    └──────────────────┘
 ┌──────────────────┐    ┌──────────────────┐    ┌──────────────────┐
 │   AppSettings    │    │  BackupMetadata  │    │   SyncMetadata   │
 └──────────────────┘    └──────────────────┘    └──────────────────┘
```

---

## 3. Detailed Entity Definitions

All entities use **UUIDv4 strings** as primary keys (`id`) generated on device creation to ensure global uniqueness across multi-device synchronizations.

### 3.1. `TutorProfile`
Stores the profile and default preferences of the tutor.
* **Table Name:** `tutor_profiles`
* **Fields:**
  * `id: String` (PK, UUID)
  * `name: String` (Required)
  * `email: String` (Optional)
  * `phone: String` (Optional)
  * `defaultHourlyRate: Double` (Optional, Default: `0.0`)
  * `defaultMonthlyFee: Double` (Optional, Default: `0.0`)
  * `currencyCode: String` (Required, Default: `"BDT"`)
  * `createdAt: Long` (UTC Epoch Ms)
  * `updatedAt: Long` (UTC Epoch Ms)
  * `isDeleted: Boolean` (Default: `false`)
  * `deletedAt: Long?` (Nullable)
* **Indexes:** `Index(value = ["isDeleted"])`

### 3.2. `Student`
Stores core student information.
* **Table Name:** `students`
* **Fields:**
  * `id: String` (PK, UUID)
  * `name: String` (Required)
  * `institution: String` (Optional)
  * `classGrade: String` (Optional, e.g. "Class 10")
  * `phone: String` (Optional)
  * `guardianName: String` (Optional)
  * `guardianPhone: String` (Optional)
  * `address: String` (Optional)
  * `monthlyFeeAmount: Double` (Required, Default: `0.0`)
  * `billingCycleDay: Int` (Required, Default: `1`, Range: `1..28`)
  * `isActive: Boolean` (Required, Default: `true`)
  * `joinedDate: Long` (Required, UTC Epoch Ms)
  * `createdAt: Long` (UTC Epoch Ms)
  * `updatedAt: Long` (UTC Epoch Ms)
  * `isDeleted: Boolean` (Default: `false`)
  * `deletedAt: Long?` (Nullable)
* **Indexes:** `Index(value = ["isActive", "isDeleted"])`, `Index(value = ["name"])`

### 3.3. `StudentSubject`
Subjects studied by a student.
* **Table Name:** `student_subjects`
* **Fields:**
  * `id: String` (PK, UUID)
  * `studentId: String` (FK -> `students.id`, Required)
  * `subjectName: String` (Required)
  * `createdAt: Long`
  * `updatedAt: Long`
  * `isDeleted: Boolean` (Default: `false`)
  * `deletedAt: Long?`
* **Foreign Keys:**
  * `entity = Student::class, parentColumns = ["id"], childColumns = ["studentId"], onDelete = CASCADE`
* **Indexes:** `Index(value = ["studentId", "isDeleted"])`

### 3.4. `Schedule`
Defines the regular recurring weekly time slots for a student.
* **Table Name:** `schedules`
* **Fields:**
  * `id: String` (PK, UUID)
  * `studentId: String` (FK -> `students.id`, Required)
  * `subjectId: String?` (FK -> `student_subjects.id`, Optional)
  * `dayOfWeek: Int` (Required, 1 = Monday ... 7 = Sunday; ISO-8601 standard)
  * `startTimeMinutes: Int` (Required, minutes from midnight 0..1439, e.g. 17:00 = 1020)
  * `endTimeMinutes: Int` (Required, minutes from midnight 0..1439, e.g. 18:00 = 1080)
  * `effectiveStartDate: Long` (Required, UTC Epoch Ms at 00:00:00 UTC)
  * `effectiveEndDate: Long?` (Optional, Nullable UTC Epoch Ms for open-ended schedules)
  * `isActive: Boolean` (Required, Default: `true`)
  * `createdAt: Long`
  * `updatedAt: Long`
  * `isDeleted: Boolean` (Default: `false`)
  * `deletedAt: Long?`
* **Foreign Keys:**
  * `entity = Student::class, parentColumns = ["id"], childColumns = ["studentId"], onDelete = CASCADE`
  * `entity = StudentSubject::class, parentColumns = ["id"], childColumns = ["subjectId"], onDelete = SET_NULL`
* **Indexes:** `Index(value = ["studentId", "isActive", "isDeleted"])`, `Index(value = ["dayOfWeek", "isActive"])`

### 3.5. `ClassSession`
Individual class occurrences (historical, present, or planned).
* **Table Name:** `class_sessions`
* **Fields:**
  * `id: String` (PK, UUID)
  * `studentId: String` (FK -> `students.id`, Required)
  * `scheduleId: String?` (FK -> `schedules.id`, Nullable if created as ad-hoc/one-time)
  * `subjectId: String?` (FK -> `student_subjects.id`, Nullable)
  * `sessionDate: Long` (Required, Epoch Ms for the specific calendar day at 00:00:00 UTC)
  * `scheduledStartTime: Long` (Required, Exact planned start timestamp UTC Epoch Ms)
  * `scheduledEndTime: Long` (Required, Exact planned end timestamp UTC Epoch Ms)
  * `actualStartTime: Long?` (Nullable, UTC Epoch Ms)
  * `actualEndTime: Long?` (Nullable, UTC Epoch Ms)
  * `status: String` (Required, Enum: `SCHEDULED`, `COMPLETED`, `MISSED`, `CANCELLED`, `RESCHEDULED`, `BACKUP`)
  * `remarks: String?` (Optional note or reason)
  * `topicCovered: String?` (Optional learning progress record)
  * `createdAt: Long`
  * `updatedAt: Long`
  * `isDeleted: Boolean` (Default: `false`)
  * `deletedAt: Long?`
* **Foreign Keys:**
  * `entity = Student::class, parentColumns = ["id"], childColumns = ["studentId"], onDelete = RESTRICT` (Sessions cannot be orphaned)
  * `entity = Schedule::class, parentColumns = ["id"], childColumns = ["scheduleId"], onDelete = SET_NULL`
* **Indexes:**
  * `Index(value = ["studentId", "sessionDate"])`
  * `Index(value = ["scheduledStartTime", "scheduledEndTime", "status"])`
  * `Index(value = ["scheduleId", "sessionDate"], unique = true)` (Ensures no duplicate generation from recurring schedules)

### 3.6. `RescheduleRecord`
Preserves complete historical audit trail when a session is moved.
* **Table Name:** `reschedule_records`
* **Fields:**
  * `id: String` (PK, UUID)
  * `originalSessionId: String` (FK -> `class_sessions.id`, Required)
  * `newSessionId: String` (FK -> `class_sessions.id`, Required)
  * `rescheduledBy: String` (Enum: `TUTOR`, `STUDENT`)
  * `reason: String?`
  * `requestedAt: Long` (UTC Epoch Ms)
  * `createdAt: Long`
  * `updatedAt: Long`
  * `isDeleted: Boolean` (Default: `false`)
  * `deletedAt: Long?`
* **Foreign Keys:**
  * `entity = ClassSession::class, parentColumns = ["id"], childColumns = ["originalSessionId"], onDelete = CASCADE`
  * `entity = ClassSession::class, parentColumns = ["id"], childColumns = ["newSessionId"], onDelete = CASCADE`
* **Indexes:** `Index(value = ["originalSessionId"])`, `Index(value = ["newSessionId"])`

### 3.7. `BackupClass`
Links a makeup session directly to the missed or cancelled session it compensates for.
* **Table Name:** `backup_classes`
* **Fields:**
  * `id: String` (PK, UUID)
  * `missedSessionId: String` (FK -> `class_sessions.id`, Required)
  * `backupSessionId: String` (FK -> `class_sessions.id`, Required)
  * `note: String?`
  * `createdAt: Long`
  * `updatedAt: Long`
  * `isDeleted: Boolean` (Default: `false`)
  * `deletedAt: Long?`
* **Foreign Keys:**
  * `entity = ClassSession::class, parentColumns = ["id"], childColumns = ["missedSessionId"], onDelete = CASCADE`
  * `entity = ClassSession::class, parentColumns = ["id"], childColumns = ["backupSessionId"], onDelete = CASCADE`
* **Indexes:** `Index(value = ["missedSessionId"])`, `Index(value = ["backupSessionId"])`

### 3.8. `MonthlyFee`
Records the monthly financial obligation generated for a student.
* **Table Name:** `monthly_fees`
* **Fields:**
  * `id: String` (PK, UUID)
  * `studentId: String` (FK -> `students.id`, Required)
  * `year: Int` (e.g. 2026)
  * `month: Int` (1..12)
  * `baseAmount: Double` (Standard monthly fee)
  * `adjustmentAmount: Double` (Default: `0.0`, discounts or extra session fees)
  * `finalAmount: Double` (`baseAmount + adjustmentAmount`)
  * `dueDate: Long` (UTC Epoch Ms)
  * `notes: String?`
  * `createdAt: Long`
  * `updatedAt: Long`
  * `isDeleted: Boolean` (Default: `false`)
  * `deletedAt: Long?`
* **Foreign Keys:**
  * `entity = Student::class, parentColumns = ["id"], childColumns = ["studentId"], onDelete = CASCADE`
* **Indexes:**
  * `Index(value = ["studentId", "year", "month", "isDeleted"], unique = true)`

### 3.9. `Payment`
Monies received from a student.
* **Table Name:** `payments`
* **Fields:**
  * `id: String` (PK, UUID)
  * `studentId: String` (FK -> `students.id`, Required)
  * `monthlyFeeId: String?` (FK -> `monthly_fees.id`, Nullable if general unallocated payment)
  * `amountPaid: Double` (Required, > 0.0)
  * `paymentDate: Long` (Required, UTC Epoch Ms)
  * `paymentMethod: String` (Enum: `CASH`, `BKASH`, `NAGAD`, `BANK_TRANSFER`, `OTHER`)
  * `transactionReference: String?` (Optional TxID)
  * `receiptNumber: String?`
  * `notes: String?`
  * `createdAt: Long`
  * `updatedAt: Long`
  * `isDeleted: Boolean` (Default: `false`)
  * `deletedAt: Long?`
* **Foreign Keys:**
  * `entity = Student::class, parentColumns = ["id"], childColumns = ["studentId"], onDelete = RESTRICT`
  * `entity = MonthlyFee::class, parentColumns = ["id"], childColumns = ["monthlyFeeId"], onDelete = SET_NULL`
* **Indexes:** `Index(value = ["studentId", "paymentDate"])`, `Index(value = ["paymentDate"])`

### 3.10. `Income`
Tracks all income, including auto-mirrored student payments and auxiliary income.
* **Table Name:** `incomes`
* **Fields:**
  * `id: String` (PK, UUID)
  * `category: String` (Enum: `TUITION_FEE`, `EXTRA_CLASS`, `OTHER`)
  * `sourceTitle: String` (e.g. "Monthly Fee - Rahim", "Exam Prep Special Session")
  * `amount: Double` (Required, > 0.0)
  * `date: Long` (Required, UTC Epoch Ms)
  * `linkedPaymentId: String?` (FK -> `payments.id`, Optional link to student payment)
  * `remarks: String?`
  * `createdAt: Long`
  * `updatedAt: Long`
  * `isDeleted: Boolean` (Default: `false`)
  * `deletedAt: Long?`
* **Foreign Keys:**
  * `entity = Payment::class, parentColumns = ["id"], childColumns = ["linkedPaymentId"], onDelete = SET_NULL`
* **Indexes:** `Index(value = ["date", "category", "isDeleted"])`

### 3.11. `Expense`
Tracks tuition-related expenses.
* **Table Name:** `expenses`
* **Fields:**
  * `id: String` (PK, UUID)
  * `category: String` (Enum: `TRANSPORT`, `TEACHING_MATERIALS`, `BOOKS`, `STATIONERY`, `INTERNET`, `MOBILE`, `OTHER`)
  * `title: String` (Required)
  * `amount: Double` (Required, > 0.0)
  * `date: Long` (Required, UTC Epoch Ms)
  * `receiptImageUri: String?` (Local URI to optional receipt photo)
  * `remarks: String?`
  * `createdAt: Long`
  * `updatedAt: Long`
  * `isDeleted: Boolean` (Default: `false`)
  * `deletedAt: Long?`
* **Indexes:** `Index(value = ["date", "category", "isDeleted"])`

### 3.12. `Note`
General student notes, pedagogical observations, or administrative reminders.
* **Table Name:** `notes`
* **Fields:**
  * `id: String` (PK, UUID)
  * `studentId: String?` (FK -> `students.id`, Nullable for global tutor notes)
  * `title: String` (Required)
  * `content: String` (Required)
  * `isPinned: Boolean` (Default: `false`)
  * `createdAt: Long`
  * `updatedAt: Long`
  * `isDeleted: Boolean` (Default: `false`)
  * `deletedAt: Long?`
* **Foreign Keys:**
  * `entity = Student::class, parentColumns = ["id"], childColumns = ["studentId"], onDelete = CASCADE`
* **Indexes:** `Index(value = ["studentId", "isPinned"])`

### 3.13. `AppSettings`
Key-value store for app configuration.
* **Table Name:** `app_settings`
* **Fields:**
  * `key: String` (PK)
  * `value: String` (Serialized string or JSON value)
  * `updatedAt: Long`

### 3.14. `BackupMetadata`
Tracks local snapshot archives created before migrations or restore operations.
* **Table Name:** `backup_metadata`
* **Fields:**
  * `id: String` (PK, UUID)
  * `backupType: String` (Enum: `LOCAL_SQLITE`, `LOCAL_JSON`, `GOOGLE_SHEET`)
  * `filePathOrUrl: String`
  * `fileSizeBytes: Long`
  * `itemCount: Int`
  * `createdAt: Long`
  * `status: String` (Enum: `COMPLETED`, `FAILED`)
  * `errorMessage: String?`

### 3.15. `SyncMetadata`
Maintains synchronization watermarks for change tracking.
* **Table Name:** `sync_metadata`
* **Fields:**
  * `entityType: String` (PK, e.g. "STUDENT", "CLASS_SESSION", "PAYMENT")
  * `lastSyncedAt: Long` (UTC Epoch Ms timestamp of last confirmed sync)
  * `lastSyncStatus: String` (Enum: `SUCCESS`, `PARTIAL`, `FAILED`)
  * `pendingChangesCount: Int`
  * `updatedAt: Long`

---

## 4. Google Sheets Schema

A single Google Spreadsheet will be created with **14 worksheets (tabs)**. Row 1 of each tab is frozen and contains exact headers. Data rows start at Row 2.

```
Worksheet Tab 1: AppInfo
Columns: [app_name, app_identifier, schema_version, created_at, last_backup_at, device_id]

Worksheet Tab 2: TutorProfile
Columns: [id, name, email, phone, default_hourly_rate, default_monthly_fee, currency_code, created_at, updated_at, is_deleted, deleted_at]

Worksheet Tab 3: Students
Columns: [id, name, institution, class_grade, phone, guardian_name, guardian_phone, address, monthly_fee_amount, billing_cycle_day, is_active, joined_date, created_at, updated_at, is_deleted, deleted_at]

Worksheet Tab 4: StudentSubjects
Columns: [id, student_id, subject_name, created_at, updated_at, is_deleted, deleted_at]

Worksheet Tab 5: Schedules
Columns: [id, student_id, subject_id, day_of_week, start_time_minutes, end_time_minutes, effective_start_date, effective_end_date, is_active, created_at, updated_at, is_deleted, deleted_at]

Worksheet Tab 6: ClassSessions
Columns: [id, student_id, schedule_id, subject_id, session_date, scheduled_start_time, scheduled_end_time, actual_start_time, actual_end_time, status, remarks, topic_covered, created_at, updated_at, is_deleted, deleted_at]

Worksheet Tab 7: RescheduleRecords
Columns: [id, original_session_id, new_session_id, rescheduled_by, reason, requested_at, created_at, updated_at, is_deleted, deleted_at]

Worksheet Tab 8: BackupClasses
Columns: [id, missed_session_id, backup_session_id, note, created_at, updated_at, is_deleted, deleted_at]

Worksheet Tab 9: Payments
Columns: [id, student_id, monthly_fee_id, amount_paid, payment_date, payment_method, transaction_reference, receipt_number, notes, created_at, updated_at, is_deleted, deleted_at]

Worksheet Tab 10: MonthlyFees
Columns: [id, student_id, year, month, base_amount, adjustment_amount, final_amount, due_date, notes, created_at, updated_at, is_deleted, deleted_at]

Worksheet Tab 11: Income
Columns: [id, category, source_title, amount, date, linked_payment_id, remarks, created_at, updated_at, is_deleted, deleted_at]

Worksheet Tab 12: Expenses
Columns: [id, category, title, amount, date, receipt_image_uri, remarks, created_at, updated_at, is_deleted, deleted_at]

Worksheet Tab 13: Notes
Columns: [id, student_id, title, content, is_pinned, created_at, updated_at, is_deleted, deleted_at]

Worksheet Tab 14: SyncMetadata
Columns: [entity_type, last_synced_at, last_sync_status, device_id, updated_at]
```

### Format Standards for Google Sheets
* **Timestamps:** Formatted as ISO-8601 strings in UTC (e.g. `2026-09-01T11:00:00Z`) for human readability in Sheets, accompanied by epoch milliseconds internally.
* **Numbers:** Standard raw numeric values (no currency symbols in raw data cells).
* **Booleans:** `TRUE` / `FALSE`.
* **Null Values:** Blank/empty cells (never string `"null"`).

---

## 5. Synchronization Architecture

### Workflow Diagram
```
  [Room Database]
        │
        ├── 1. Query entities WHERE updatedAt > lastSyncedAt
        │
        ▼
  [Sync Engine] ─────── Validates network & OAuth token
        │
        ├── 2. Batch format into Google Sheets ValueRange payload
        ▼
  [Google Sheets API v4] ─── Appends or updates rows by UUID match
        ▲
        │
        ├── 3. Read cloud records WHERE updated_at > local_watermark
        ▼
  [Conflict Engine] ────── Evaluates (Cloud vs Local) via Last-Modified-Wins
        │
        ▼
  [Room Transaction] ───── Writes merged records & updates SyncMetadata
```

### Sync Operations Supported
1. **Full Backup:** Reads all local tables (including tombstones) and overwrites/populates all worksheets. Updates `AppInfo.last_backup_at`.
2. **Full Restore:**
   * **Step A:** Mandatory pre-restore local SQLite snapshot creation saved to app-private cache.
   * **Step B:** Download all rows across all 14 tabs from Google Sheets.
   * **Step C (Replace Mode):** Clear local tables and populate directly in dependency order (Parents before Children).
   * **Step D (Merge Mode):** Run entity-by-entity Last-Modified-Wins comparison.
3. **Incremental Upload:** Identifies local records where `updatedAt > syncMetadata.lastSyncedAt`. Performs batch update via Sheets `spreadsheets.values.batchUpdate`.
4. **Incremental Download:** Queries rows in Sheets where `updated_at > syncMetadata.lastSyncedAt` and applies changes locally.

---

## 6. Conflict Resolution Strategy

### Algorithm: Last Modified Wins (LMW) with Tombstone Precedence
For any record $R$ existing both in local Room DB ($R_{local}$) and cloud sheet ($R_{cloud}$):

```
IF (R_cloud.id == R_local.id):
    IF (R_local.isDeleted == TRUE AND R_cloud.isDeleted == FALSE):
        IF (R_local.deletedAt >= R_cloud.updatedAt):
            WINNER = R_local (Propagate deletion to Cloud)
        ELSE:
            WINNER = R_cloud (Resurrect locally because cloud was modified after local deletion)
    ELSE IF (R_local.isDeleted == FALSE AND R_cloud.isDeleted == TRUE):
        IF (R_cloud.deletedAt >= R_local.updatedAt):
            WINNER = R_cloud (Mark deleted locally)
        ELSE:
            WINNER = R_local (Propagate update to Cloud)
    ELSE:
        // Neither or both are deleted: standard timestamp tie-break
        IF (R_cloud.updatedAt > R_local.updatedAt):
            WINNER = R_cloud
        ELSE:
            WINNER = R_local
```

### Safety Guarantees
* **Deterministic Clock:** Server/Google response HTTP headers provide standard time verification if device clock is detected to have drifted significantly (> 5 minutes).
* **Foreign Key Insertion Order:**
  `TutorProfile` -> `Student` -> `StudentSubject` -> `Schedule` -> `MonthlyFee` -> `ClassSession` -> `Payment` -> `RescheduleRecord` -> `BackupClass` -> `Income` -> `Expense` -> `Note`.
* **Deletion Order (Reverse):** Children deleted before parents to prevent SQLite constraint failures.

---

## 7. Schedule Conflict Engine Specification

### Overlap Detection Math
Two time intervals $[S_1, E_1)$ and $[S_2, E_2)$ conflict if and only if:
$$\text{Conflict} \iff (S_{new} < E_{existing}) \land (E_{new} > S_{existing})$$

This condition handles:
* Exact overlap: $S_{new} = S_{existing} \land E_{new} = E_{existing}$
* Start time collision: $S_{new} = S_{existing}$
* End time collision: $E_{new} = E_{existing}$
* New inside existing: $S_{new} \ge S_{existing} \land E_{new} \le E_{existing}$
* Existing inside new: $S_{new} \le S_{existing} \land E_{new} \ge E_{existing}$
* Partial overlap left: $S_{new} < S_{existing} \land E_{new} > S_{existing}$
* Partial overlap right: $S_{new} < E_{existing} \land E_{new} > E_{existing}$

### Reusable Interface Contract
```kotlin
data class TimeInterval(
    val startEpochMs: Long,
    val endEpochMs: Long
)

sealed interface ConflictResult {
    data object NoConflict : ConflictResult
    data class HasConflict(
        val conflictingSession: ClassSession?,
        val conflictingSchedule: Schedule?,
        val reason: String
    ) : ConflictResult
}

interface ScheduleConflictEngine {
    suspend fun checkSessionConflict(
        candidateStart: Long,
        candidateEnd: Long,
        excludeSessionId: String? = null
    ): ConflictResult

    suspend fun checkWeeklyScheduleConflict(
        dayOfWeek: Int,
        startMinutes: Int,
        endMinutes: Int,
        effectiveStartDate: Long,
        effectiveEndDate: Long?,
        excludeScheduleId: String? = null
    ): ConflictResult
}
```

---

## 8. Financial Engine & Ledger Mechanics

Financial calculations are derived dynamically using indexed aggregate queries:

$$\text{Total Expected Fee}(student) = \sum \text{MonthlyFee.finalAmount}$$
$$\text{Total Paid}(student) = \sum \text{Payment.amountPaid}$$
$$\text{Balance} = \text{Total Paid} - \text{Total Expected Fee}$$

* **If $\text{Balance} < 0$:** $\text{Due Amount} = |\text{Balance}|$, $\text{Advance} = 0.0$
* **If $\text{Balance} > 0$:** $\text{Due Amount} = 0.0$, $\text{Advance} = \text{Balance}$
* **If $\text{Balance} = 0$:** $\text{Due Amount} = 0.0$, $\text{Advance} = 0.0$

### Cash Flow Calculations
* $\text{Total Income} = \sum \text{Income.amount}$
* $\text{Tuition Income} = \sum \text{Income.amount [category = TUITION\_FEE]}$
* $\text{Other Income} = \sum \text{Income.amount [category} \neq \text{TUITION\_FEE]}$
* $\text{Total Expense} = \sum \text{Expense.amount}$
* $\text{Net Income} = \text{Total Income} - \text{Total Expense}$

---

## 9. Project Folder Structure

```
app/src/main/java/com/tuitionmanager/
├── TuitionManagerApp.kt                # Application subclass (initializes Timber, WorkManager)
├── di/                                 # Dependency Injection modules (Hilt / Manual DI)
│   ├── DatabaseModule.kt
│   ├── RepositoryModule.kt
│   ├── UseCaseModule.kt
│   └── NetworkModule.kt
├── data/
│   ├── local/
│   │   ├── TuitionDatabase.kt          # RoomDatabase definition (entities, version, converters)
│   │   ├── Converters.kt               # TypeConverters (Enums, Dates)
│   │   ├── dao/                        # Room Data Access Objects
│   │   │   ├── StudentDao.kt
│   │   │   ├── ScheduleDao.kt
│   │   │   ├── ClassSessionDao.kt
│   │   │   ├── PaymentDao.kt
│   │   │   ├── MonthlyFeeDao.kt
│   │   │   ├── FinanceDao.kt
│   │   │   ├── NoteDao.kt
│   │   │   └── SyncMetadataDao.kt
│   │   └── entity/                     # Room Entities
│   │       ├── TutorProfileEntity.kt
│   │       ├── StudentEntity.kt
│   │       ├── StudentSubjectEntity.kt
│   │       ├── ScheduleEntity.kt
│   │       ├── ClassSessionEntity.kt
│   │       ├── RescheduleRecordEntity.kt
│   │       ├── BackupClassEntity.kt
│   │       ├── MonthlyFeeEntity.kt
│   │       ├── PaymentEntity.kt
│   │       ├── IncomeEntity.kt
│   │       ├── ExpenseEntity.kt
│   │       ├── NoteEntity.kt
│   │       ├── AppSettingsEntity.kt
│   │       ├── BackupMetadataEntity.kt
│   │       └── SyncMetadataEntity.kt
│   ├── remote/
│   │   ├── google/
│   │   │   ├── GoogleAuthManager.kt    # Credential Manager & OAuth tokens
│   │   │   ├── GoogleSheetsClient.kt   # Sheets v4 REST API client
│   │   │   ├── GoogleDriveClient.kt    # Spreadsheet file search & creation
│   │   │   └── SheetsSchemaValidator.kt# Tab and column verification
│   ├── repository/                     # Repository Implementations
│   │   ├── StudentRepositoryImpl.kt
│   │   ├── ScheduleRepositoryImpl.kt
│   │   ├── ClassSessionRepositoryImpl.kt
│   │   ├── FinanceRepositoryImpl.kt
│   │   └── SyncRepositoryImpl.kt
│   └── sync/
│       ├── SyncEngine.kt               # Orchestrator for change upload/download
│       └── SyncWorker.kt               # Periodic WorkManager task
├── domain/
│   ├── model/                          # Clean Domain POJOs
│   │   ├── Student.kt
│   │   ├── Schedule.kt
│   │   ├── ClassSession.kt
│   │   ├── FinancialSummary.kt
│   │   └── Enums.kt
│   ├── repository/                     # Domain Repository Interfaces
│   │   ├── StudentRepository.kt
│   │   ├── ScheduleRepository.kt
│   │   ├── ClassSessionRepository.kt
│   │   ├── FinanceRepository.kt
│   │   └── SyncRepository.kt
│   ├── engine/
│   │   ├── ScheduleConflictEngine.kt   # Overlap detection implementation
│   │   └── SessionGeneratorEngine.kt   # Safe recurring date generator
│   └── usecase/
│       ├── student/
│       ├── schedule/
│       ├── session/
│       ├── finance/
│       └── sync/
├── ui/
│   ├── navigation/
│   │   ├── Screen.kt                   # Sealed destination routes
│   │   └── AppNavHost.kt               # NavHost setup with Compose transitions
│   ├── theme/
│   │   ├── Color.kt
│   │   ├── Type.kt
│   │   └── Theme.kt                    # Material 3 Dynamic Theme
│   ├── components/                     # Reusable Material 3 widgets
│   │   ├── AppTopBar.kt
│   │   ├── ConflictDialog.kt
│   │   ├── DatePickerField.kt
│   │   └── MetricSummaryCard.kt
│   ├── dashboard/
│   ├── students/
│   ├── schedule/
│   ├── classes/
│   ├── payments/
│   ├── finance/
│   ├── reports/
│   └── settings/
└── utils/
    ├── DateTimeFormatter.kt            # BD timezone (+06:00) helper functions
    ├── CsvExporter.kt                  # Local CSV generation
    ├── PdfReportGenerator.kt           # Android native Canvas/Print PDF writer
    └── SecurityUtils.kt                # BiometricPrompt & PIN encryption
```

---

## 10. Dependencies (Gradle Configuration)

```kotlin
// Android Version Catalog / build.gradle.kts
dependencies {
    // Jetpack Compose & Material 3
    val composeBom = platform("androidx.compose:compose-bom:2024.09.00")
    implementation(composeBom)
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-extended")
    implementation("androidx.navigation:navigation-compose:2.8.0")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.5")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.8.5")

    // Room Database
    val roomVersion = "2.6.1"
    implementation("androidx.room:room-runtime:$roomVersion")
    implementation("androidx.room:room-ktx:$roomVersion")
    ksp("androidx.room:room-compiler:$roomVersion")

    // Kotlin Coroutines
    val coroutinesVersion = "1.8.1"
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:$coroutinesVersion")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:$coroutinesVersion")

    // Background WorkManager
    implementation("androidx.work:work-runtime-ktx:2.9.1")

    // Google Identity, OAuth & Sheets API
    implementation("androidx.credentials:credentials:1.3.0")
    implementation("androidx.credentials:credentials-play-services-auth:1.3.0")
    implementation("com.google.android.libraries.identity.googleid:googleid:1.1.1")
    implementation("com.google.api-client:google-api-client-android:2.6.0")
    implementation("com.google.apis:google-api-services-sheets:v4-rev20240822-2.0.0")
    implementation("com.google.apis:google-api-services-drive:v3-rev20240822-2.0.0")

    // Serialization & Utilities
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.7.2")
    implementation("androidx.security:security-crypto:1.1.0-alpha06")
    implementation("com.jakewharton.timber:timber:5.0.1")

    // Testing
    testImplementation("junit:junit:4.13.2")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:$coroutinesVersion")
    testImplementation("org.mockito.kotlin:mockito-kotlin:5.4.0")
    androidTestImplementation("androidx.room:room-testing:$roomVersion")
    androidTestImplementation("androidx.test.ext:junit:1.2.1")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.6.1")
}
```

---

## 11. Step-by-Step Development Phases

### Phase 1: Project Foundation & Core Infrastructure
* **Objective:** Establish Gradle configurations, Kotlin serialization, base architecture, theme, and logging.
* **Files Affected:** `build.gradle.kts`, `TuitionManagerApp.kt`, `Theme.kt`, `Color.kt`.
* **Database Changes:** None.
* **Features Completed:** Clean build setup, Material 3 baseline, Coroutine dispatchers configuration.
* **Testing:** Compilation check, dependency validation.
* **Exit Criteria:** App boots into empty Material 3 scaffold without runtime warnings.

### Phase 2: Room Database & Core Entities
* **Objective:** Implement complete SQLite data persistence layer with Room.
* **Files Affected:** `TuitionDatabase.kt`, all 15 Entities, all DAOs, `Converters.kt`.
* **Database Changes:** Database version 1 initialized with all tables, indices, and foreign keys.
* **Features Completed:** CRUD operations for all database entities with soft-deletion support.
* **Testing:** Room in-memory SQLite integration tests (`Room.inMemoryDatabaseBuilder`).
* **Exit Criteria:** 100% passing tests for entity foreign keys, cascade triggers, and indices.

### Phase 3: Domain Layer & Schedule Conflict Engine
* **Objective:** Build business domain rules, pure models, and the mathematical conflict engine.
* **Files Affected:** `ScheduleConflictEngine.kt`, `ConflictResult.kt`, `SessionGeneratorEngine.kt`.
* **Database Changes:** None.
* **Features Completed:** Overlap calculations ($S_1 < E_2 \land E_1 > S_2$), recurring session generation.
* **Testing:** Unit tests verifying all 7 conflict combinations (partial overlap, containment, boundaries).
* **Exit Criteria:** Zero false positives/negatives in conflict detection test suite.

### Phase 4: Navigation Shell & Root Dashboard Structure
* **Objective:** Setup Compose Navigation, top bars, bottom navigation bar, and screen transitions.
* **Files Affected:** `Screen.kt`, `AppNavHost.kt`, `MainScaffold.kt`.
* **Database Changes:** None.
* **Features Completed:** Navigation structure connecting Dashboard, Students, Schedule, Finance, and Settings.
* **Testing:** Compose navigation test verifying backstack transitions.
* **Exit Criteria:** Fluid navigation across all major hub screens.

### Phase 5: Student Management Module
* **Objective:** Complete UI and ViewModels for adding, editing, archiving, and viewing students.
* **Files Affected:** `StudentListScreen.kt`, `StudentDetailScreen.kt`, `StudentViewModel.kt`, `StudentRepository.kt`.
* **Database Changes:** None.
* **Features Completed:** Student list with search, detail profile, subject assignment, and fee configuration.
* **Testing:** ViewModel state tests with simulated student insertions and updates.
* **Exit Criteria:** Seamless creation and modification of student profiles stored in Room.

### Phase 6: Schedule Management System
* **Objective:** Interface to manage weekly schedules per student with day/time selectors.
* **Files Affected:** `ScheduleScreen.kt`, `ScheduleViewModel.kt`, `TimePickerComponent.kt`.
* **Database Changes:** None.
* **Features Completed:** Weekly calendar view, add/edit schedule slots, day-of-week recurrence.
* **Testing:** Verification that recurring patterns map to ISO-8601 day-of-week standards.
* **Exit Criteria:** Schedules saved with valid minutes-from-midnight and foreign key constraints.

### Phase 7: Conflict Engine Integration & Session Generation
* **Objective:** Connect conflict engine to schedule editing and trigger automatic session generation.
* **Files Affected:** `ScheduleViewModel.kt`, `GenerateSessionsUseCase.kt`, `ConflictDialog.kt`.
* **Database Changes:** None.
* **Features Completed:** Conflict prompt prevents accidental double-booking. Session generator populates `class_sessions` safely within a given date range without duplicating existing entries.
* **Testing:** Edge-case test on duplicate schedule generation over existing session dates.
* **Exit Criteria:** Zero duplicate `class_sessions` created upon repeated schedule saves.

### Phase 8: Class Session Management
* **Objective:** Day-by-day class attendance logger (Completed, Missed, Cancelled, Rescheduled, Backup).
* **Files Affected:** `ClassSessionScreen.kt`, `RescheduleDialog.kt`, `BackupClassDialog.kt`.
* **Database Changes:** None.
* **Features Completed:** Quick status toggling, reschedule record creation, backup session linking.
* **Testing:** Verification that original session history is preserved when rescheduling.
* **Exit Criteria:** Audit trail entries (`reschedule_records`) created without destroying original records.

### Phase 9: Payment & Billing System
* **Objective:** Handle monthly fee generation, payment logging, and balance derivation.
* **Files Affected:** `PaymentScreen.kt`, `PaymentViewModel.kt`, `CalculateBalanceUseCase.kt`.
* **Database Changes:** None.
* **Features Completed:** Partial/Full/Advance payments, automated due calculations, receipt logging.
* **Testing:** Numerical tests on multiple partial payments, overpayments (advances), and discounts.
* **Exit Criteria:** Exact balance matching: $\text{Balance} = \sum \text{Payments} - \sum \text{MonthlyFees}$.

### Phase 10: General Finance (Income & Expense)
* **Objective:** Independent ledger for overall tutor finances.
* **Files Affected:** `FinanceScreen.kt`, `FinanceViewModel.kt`, `ExpenseCategoryPicker.kt`.
* **Database Changes:** None.
* **Features Completed:** Add expense with category, add manual income, mirror tuition payments to income.
* **Testing:** Aggregation tests for monthly net income calculation.
* **Exit Criteria:** Reports correctly reflect net balance $(\text{Income} - \text{Expense})$.

### Phase 11: Real-time Dashboard
* **Objective:** High-level dashboard displaying today's classes, monthly income, and pending dues.
* **Files Affected:** `DashboardScreen.kt`, `DashboardViewModel.kt`.
* **Database Changes:** None.
* **Features Completed:** Today's timetable card, quick attendance toggle, financial snapshot tiles.
* **Testing:** UI benchmark test verifying recomposition performance.
* **Exit Criteria:** Dashboard loads in under 100ms from local Room storage.

### Phase 12: Analytical Reports
* **Objective:** Detailed reporting for student attendance, financial summaries, and monthly stats.
* **Files Affected:** `ReportsScreen.kt`, `ReportsViewModel.kt`, `StatGraphComponent.kt`.
* **Database Changes:** None.
* **Features Completed:** Attendance percentages, monthly breakdown table, student ledger view.
* **Testing:** Validation of mathematical formulas against expected statistical metrics.
* **Exit Criteria:** Correct calculations across multi-month historical datasets.

### Phase 13: Local Backup & Disaster Recovery
* **Objective:** Offline database export and import via SQLite snapshot and JSON format.
* **Files Affected:** `LocalBackupManager.kt`, `BackupRestoreScreen.kt`.
* **Database Changes:** None.
* **Features Completed:** One-tap export to user storage, emergency pre-restore snapshotting.
* **Testing:** Corrupted file restore failure testing, round-trip backup/restore integrity test.
* **Exit Criteria:** Database successfully restored from local backup file on a fresh install.

### Phase 14: Google Sheets Integration & Sync Engine
* **Objective:** Cloud backup and synchronization using Google Sheets API v4.
* **Files Affected:** `GoogleAuthManager.kt`, `GoogleSheetsClient.kt`, `SyncEngine.kt`, `SyncScreen.kt`.
* **Database Changes:** None.
* **Features Completed:** OAuth login, create/select spreadsheet, schema verification, incremental sync, full restore.
* **Testing:** End-to-end sync mock test (Local -> Cloud -> Fresh Local).
* **Exit Criteria:** Clean bi-directional synchronization without losing local data.

### Phase 15: Background Reminders & Notifications
* **Objective:** Local notification reminders for upcoming classes and overdue fees.
* **Files Affected:** `AlarmReceiver.kt`, `NotificationHelper.kt`, `ScheduleAlarmUseCase.kt`.
* **Database Changes:** None.
* **Features Completed:** Exact alarm scheduling for class times, daily reminder for unrecorded sessions.
* **Testing:** AlarmManager trigger verification.
* **Exit Criteria:** Notifications fire reliably on time in offline mode.

### Phase 16: Security, Biometrics & PIN Lock
* **Objective:** Protect financial and student records.
* **Files Affected:** `SecurityManager.kt`, `PinLockScreen.kt`, `BiometricHelper.kt`.
* **Database Changes:** Encrypted storage keys in `AppSettings`.
* **Features Completed:** Biometric prompt (fingerprint/face) and fallback 4-digit PIN lock.
* **Testing:** Verification that backgrounding the app locks the screen.
* **Exit Criteria:** App data cannot be viewed without authentication when security is enabled.

### Phase 17: CSV & PDF Export
* **Objective:** Export reports and student statements to shareable PDF and CSV documents.
* **Files Affected:** `CsvExporter.kt`, `PdfReportGenerator.kt`.
* **Database Changes:** None.
* **Features Completed:** Generate fee receipt PDF, student payment history PDF, and CSV tables.
* **Testing:** Verify exported PDF rendering and CSV syntax conformance.
* **Exit Criteria:** Valid PDF/CSV files generated and shareable via Android Share Intent.

### Phase 18: Comprehensive Quality Assurance & Stress Testing
* **Objective:** System testing, edge cases, large dataset load testing.
* **Files Affected:** All test suites.
* **Database Changes:** None.
* **Features Completed:** Test with 1,000+ students and 10,000+ sessions to verify index performance.
* **Testing:** Database query execution time profiling.
* **Exit Criteria:** All queries execute in under 16ms (60 FPS fluid rendering).

### Phase 19: UI Polish & Accessibility
* **Objective:** Visual refinements, micro-interactions, dark mode, high contrast, and accessibility.
* **Files Affected:** All Compose UI screens.
* **Database Changes:** None.
* **Features Completed:** Smooth state transitions, screen reader labeling, touch target validation (>= 48dp).
* **Testing:** Android Accessibility Scanner audit.
* **Exit Criteria:** Zero accessibility violations; smooth 60/120 FPS transitions.

### Phase 20: Release Packaging & Verification
* **Objective:** Proguard/R8 optimization, release signing configuration, and APK generation.
* **Files Affected:** `proguard-rules.pro`, `build.gradle.kts`.
* **Database Changes:** None.
* **Features Completed:** Minified and obfuscated release APK/AAB bundle.
* **Testing:** Smoke testing on clean physical Android device (Android 10 through 15).
* **Exit Criteria:** Fully functional release build ready for daily personal use or distribution.

---

## 12. Security Considerations

1. **No Stored Plaintext Secrets:** Google OAuth tokens are acquired via Google Play Services Credential Manager. No client secrets, user passwords, or raw refresh tokens are persisted in Room.
2. **Encrypted Storage:** Sensitive app preferences (PIN hashes, biometric states) use AndroidX `EncryptedSharedPreferences` backed by the Android Keystore system (`MasterKeys.AES256_GCM`).
3. **Restricted OAuth Scopes:** Only the minimal Google Drive scope required to read and write app-specific spreadsheets is requested (`drive.file`, not full `drive` access).
4. **Token Exposure Mitigation:** Access tokens and personal information are stripped from all logging utilities (`Timber` release tree disables debug logs).
5. **Pre-Restore Local Snapshot:** Every restore or merge operation automatically creates an uncompressed local SQLite snapshot in app-private sandbox storage before applying changes.

---

## 13. Risks & Solutions

| Identified Risk | Potential Impact | Technical Mitigation / Solution |
| :--- | :--- | :--- |
| **Clock Skew / Inaccurate Device Time** | Misordered Last-Modified-Wins conflict resolution | Use device boot time or Google Sheets response HTTP date header as a reference anchor. Warn user if system clock drifts > 5 minutes. |
| **Orphaned Sessions on Student Deletion** | Inconsistent attendance and financial reports | Enforce `ForeignKey.RESTRICT` on `ClassSession.studentId` and `Payment.studentId`. Mandate soft-delete (`isDeleted = true`) rather than SQLite hard deletion. |
| **Duplicate Session Generation** | Cluttered calendar from repeatedly saving recurring schedules | Unique index on `(scheduleId, sessionDate)` in `class_sessions` with `OnConflictStrategy.IGNORE` or check-before-insert logic. |
| **Interrupted Google Sheets Sync** | Partial data upload leading to out-of-sync state | Wrap batch updates in atomic multi-tab requests (`spreadsheets.values.batchUpdate`). Update `SyncMetadata` only upon receiving a successful 200 OK from Google APIs. |
| **Large Sheet Memory Consumption** | App crashes during full restore | Process rows using streaming JSON parsers or paginated chunk reads. Use Room `@Transaction` for batch insertions. |

---

## 14. Final Recommended Implementation Order

When proceeding to implementation, execute the phases in this strict sequence:
1. **Foundation & Persistence:** Complete **Phase 1** (Setup) followed immediately by **Phase 2** (Room Entities & DAOs).
2. **Core Logic:** Implement **Phase 3** (Conflict Engine & Recurring Date Mathematics) with comprehensive unit tests before building any UI.
3. **Application Shell & Data Entry:** Implement **Phase 4** (Navigation) and **Phases 5–8** (Student, Schedule, Conflict Integration, and Session Attendance).
4. **Financial Core:** Implement **Phase 9** (Billing & Payments) and **Phase 10** (General Finance Ledger).
5. **Views & Insights:** Implement **Phase 11** (Dashboard) and **Phase 12** (Reporting).
6. **Storage & Cloud:** Implement **Phase 13** (Local Backup) first, followed by **Phase 14** (Google Sheets Sync).
7. **Refinements:** Complete **Phases 15–20** (Notifications, Security, Export, Testing, Polish, and Release).
