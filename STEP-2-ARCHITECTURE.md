# Technical Architecture & System Blueprint: STEP 2
## Student Diary, Exam Management & Guardian Progress System

---

## 1. Architecture Changes from STEP 1

STEP 1 established an **offline-first administrative baseline** (Clean Architecture + MVVM) centered on students, recurring schedules, attendance sessions, and a double-entry financial ledger. 

STEP 2 elevates the application from an administrative utility into a **comprehensive pedagogical operating system for independent tutors**.

```
┌─────────────────────────────────────────────────────────────────────────────────────────┐
│                                   PRESENTATION LAYER                                    │
│  Compose Material 3: Diary Timeline • Exam Planner & Scorecards • Guardian Preview UI  │
│  StateFlow<UiState> ◄──────────────────────────────────────────────────── ViewModels   │
└────────────────────────────────────────────┬────────────────────────────────────────────┘
                                             │ invokes
┌────────────────────────────────────────────▼────────────────────────────────────────────┐
│                                       DOMAIN LAYER                                      │
│  • Pedagogical & Diary UseCases (Search, Filter, Topic Tracking)                        │
│  • Exam Evaluation Engine (Dynamic Score & Percentage, Grade/Mastery Resolver)          │
│  • Progress Report Aggregator (Zero-mutation in-memory compilation)                     │
│  • Guardian Narrative Formatter (Configurable templating, i18n EN/BN)                   │
│  • Pure Domain Models & Repository Contracts                                            │
└────────────────────────────────────────────┬────────────────────────────────────────────┘
                                             │ implements
┌────────────────────────────────────────────▼────────────────────────────────────────────┐
│                                       DATA LAYER                                        │
│  ┌────────────────────────────────────────┐  ┌───────────────────────────────────────┐  │
│  │       Room SQLite Database (v2)        │  │     Cloud Sync & Backup Engine        │  │
│  │ • 17 Normalized Entities               │  │ • Google Sheets API v4 (17 Worksheets)│  │
│  │ • StudentDiary & Exam/Result Relations │  │ • Incremental LMW Sync + Tombstones   │  │
│  │ • Strict Foreign Keys + CASCADE/RESTRICT│ │ • Atomic Multi-Tab Batch Update       │  │
│  └────────────────────────────────────────┘  └───────────────────────────────────────┘  │
│  ┌───────────────────────────────────────────────────────────────────────────────────┐  │
│  │              Android Platform Integrations (Offline Native)                       │  │
│  │ • Exact Alarms via AlarmManager (Exam reminders, Session tracking)                │  │
│  │ • Daily Reconciliation via WorkManager (Progress report notifications)            │  │
│  │ • Android Native ShareSheet (WhatsApp, SMS, Email text & PDF intents)             │  │
│  └───────────────────────────────────────────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────────────────────────────────────────┘
```

### Core Architecture Enhancements
1. **Introduction of the Pedagogical Domain:** Structured daily academic tracking (`StudentDiary`) replaces unstructured notes, linking topics, homework status, and student comprehension to specific sessions.
2. **Two-Tier Examination Subsystem:** Clear architectural separation between the **Plan/Event** (`Exam`) and the **Assessment/Performance** (`ExamResult`), enabling single-student focus today and multi-student/re-take scalability tomorrow.
3. **Pure Derived Reporting (No Persistence Clutter):** Monthly progress reports and guardian communication messages are **derived dynamically in memory** via Kotlin Coroutines. No bloated, stale snapshot records pollute the primary database.
4. **Offline Native Communication Broker:** Guardian updates utilize Android's `Intent.ACTION_SEND` (ShareSheet) with pre-formatted, localized text and PDF attachments. Zero external messaging servers, third-party SMS gateways, or background automated transmissions are introduced.

---

## 2. New & Modified Entity-Relationship (ER) Diagram

```
                              ┌──────────────────┐
                              │   TutorProfile   │
                              └────────┬─────────┘
                                       │ 1:N
                                       ▼
┌──────────────────┐ 1:N      ┌──────────────────┐ 1:N      ┌──────────────────┐
│   MonthlyFee     │◄─────────┤     Student      ├─────────►│  StudentSubject  │
└──────────────────┘          │ (Added Guardian  │          └────────┬─────────┘
                              │  Pref Columns)   │                   │
                               └───────┬────────┘                   │
                                       │                            │
                     ┌─────────────────┼────────────────────────┐   │
                     │ 1:N             │ 1:N                    │   │ 1:N
                     │                 ▼                        │   │
                     │        ┌──────────────────┐              │   │
                     │        │     Schedule     │              │   │
                     │        └────────┬─────────┘              │   │
                     │                 │ 1:N                    │   │
                     ▼                 ▼                        │   │
┌──────────────────┐ 1:N      ┌──────────────────┐              │   │
│     Payment      │◄─────────┤   ClassSession   │              │   │
└──────────────────┘          │  (Summary topic  │              │   │
                              │   preserved)     │              │   │
                               └───────┬────────┘               │   │
                                       │                        │   │
                     ┌─────────────────┼──────────────────┐     │   │
                     │ 1:1             │ 1:1              │     │   │
                     ▼                 ▼                  │     │   │
          ┌──────────────────┐  ┌──────────────────┐      │     │   │
          │ RescheduleRecord │  │   BackupClass    │      │     │   │
          └──────────────────┘  └──────────────────┘      │     │   │
                                                          │     │   │
                                       ┌──────────────────┘     │   │
                                       │ 1:N                    │   │
                                       ▼                        │   │
                              ┌──────────────────┐ 1:N          │   │
                              │   StudentDiary   │◄─────────────┼───┘
                              │ (Structured Day  │              │
                              │  Learning Log)   │              │
                              └──────────────────┘              │
                                                                │
                                       ┌────────────────────────┘
                                       │ 1:N
                                       ▼
                              ┌──────────────────┐ 1:N
                              │       Exam       │◄─────────────────┐
                              │ (Planned & Event)│                  │ 1:N
                              └────────┬─────────┘                  │
                                       │ 1:N (or 1:1 per attempt)   │
                                       ▼                            │
                              ┌──────────────────┐                  │
                              │    ExamResult    │                  │
                              │ (Score, Feedback,│                  │
                              │  Mastery Status) │                  │
                              └──────────────────┘                  │
                                                                    │
 Independent System Entities:                                      │
 ┌──────────────────┐  ┌──────────────────┐  ┌──────────────────┐   │
 │      Income      │  │     Expense      │  │       Note       │   │
 └──────────────────┘  └──────────────────┘  └──────────────────┘   │
 ┌──────────────────┐  ┌──────────────────┐  ┌──────────────────┐   │
 │   AppSettings    │  │  BackupMetadata  │  │   SyncMetadata   ├───┘
 └──────────────────┘  └──────────────────┘  └──────────────────┘
```

---

## 3. Complete New Entity Definitions

All new entities follow the uniform design system: **UUIDv4 string PK (`id`)**, UTC millisecond timestamps, soft deletion (`isDeleted`, `deletedAt`), and indexed query paths.

### 3.1. `StudentDiary`
Stores structured, pedagogical records of lessons, homework assignments, and pupil comprehension.

* **Table Name:** `student_diaries`
* **Primary Key:** `id: String` (UUIDv4)
* **Fields:**
  * `id: String` (PK, UUID)
  * `studentId: String` (FK -> `students.id`, Required)
  * `subjectId: String` (FK -> `student_subjects.id`, Required)
  * `classSessionId: String?` (FK -> `class_sessions.id`, Nullable: allows ad-hoc self-study or non-session diary records)
  * `date: Long` (Required, UTC Epoch Ms for the date of lesson at 00:00:00 UTC)
  * `topicTitle: String` (Required, e.g., "Quadratic Equations - Factorization Method")
  * `whatWasTaught: String` (Required, detailed narrative of pedagogical concepts introduced)
  * `homeworkAssigned: String?` (Optional, explicit assignment tasks, page numbers, exercise numbers)
  * `homeworkStatus: String` (Required, Enum: `NONE`, `ASSIGNED`, `SUBMITTED_COMPLETE`, `SUBMITTED_PARTIAL`, `NOT_SUBMITTED`, Default: `"NONE"`)
  * `practiceGiven: String?` (Optional, classroom exercises conducted during the lesson)
  * `studentUnderstanding: String` (Required, Enum: `EXCELLENT`, `GOOD`, `AVERAGE`, `NEEDS_ATTENTION`, Default: `"GOOD"`)
  * `teacherRemarks: String?` (Optional, behavioral or conceptual notes)
  * `nextClassPlan: String?` (Optional, preview agenda for upcoming lesson)
  * `createdAt: Long` (UTC Epoch Ms)
  * `updatedAt: Long` (UTC Epoch Ms)
  * `isDeleted: Boolean` (Default: `false`)
  * `deletedAt: Long?` (Nullable)
* **Foreign Keys:**
  * `entity = Student::class, parentColumns = ["id"], childColumns = ["studentId"], onDelete = ForeignKey.CASCADE`
  * `entity = StudentSubject::class, parentColumns = ["id"], childColumns = ["subjectId"], onDelete = ForeignKey.RESTRICT`
  * `entity = ClassSession::class, parentColumns = ["id"], childColumns = ["classSessionId"], onDelete = ForeignKey.SET_NULL`
* **Indexes:**
  * `Index(value = ["studentId", "date"])`
  * `Index(value = ["studentId", "subjectId", "date"])`
  * `Index(value = ["classSessionId"])`
  * `Index(value = ["homeworkStatus"])`
  * `Index(value = ["isDeleted"])`

---

### 3.2. `Exam`
Encapsulates planned or conducted academic evaluations, syllabi, scheduling, and target marks.

* **Table Name:** `exams`
* **Primary Key:** `id: String` (UUIDv4)
* **Fields:**
  * `id: String` (PK, UUID)
  * `studentId: String` (FK -> `students.id`, Required)
  * `subjectId: String` (FK -> `student_subjects.id`, Required)
  * `title: String` (Required, e.g., "Monthly Assessment - Chapter 4 & 5")
  * `syllabusTopic: String` (Required, chapters, formulas, or competencies evaluated)
  * `plannedDate: Long` (Required, UTC Epoch Ms for scheduled day at 00:00:00 UTC)
  * `plannedStartTimeMinutes: Int` (Required, minutes from midnight 0..1439, e.g., 10:00 AM = 600)
  * `durationMinutes: Int` (Required, duration in minutes, e.g., 60, 90, 120)
  * `totalMarks: Double` (Required, > 0.0, e.g., 50.0, 100.0)
  * `passingMarks: Double?` (Optional, baseline benchmark, e.g., 20.0 out of 50.0)
  * `examType: String` (Required, Enum: `WEEKLY_QUIZ`, `CHAPTER_TEST`, `MONTHLY_ASSESSMENT`, `MID_TERM`, `FINAL_MODEL_TEST`, `SURPRISE_TEST`, Default: `"CHAPTER_TEST"`)
  * `status: String` (Required, Enum: `PLANNED`, `COMPLETED`, `MISSED`, `CANCELLED`, `RESCHEDULED`, Default: `"PLANNED"`)
  * `notes: String?` (Optional, equipment requirements, formula sheets permitted, etc.)
  * `createdAt: Long` (UTC Epoch Ms)
  * `updatedAt: Long` (UTC Epoch Ms)
  * `isDeleted: Boolean` (Default: `false`)
  * `deletedAt: Long?` (Nullable)
* **Foreign Keys:**
  * `entity = Student::class, parentColumns = ["id"], childColumns = ["studentId"], onDelete = ForeignKey.CASCADE`
  * `entity = StudentSubject::class, parentColumns = ["id"], childColumns = ["subjectId"], onDelete = ForeignKey.RESTRICT`
* **Indexes:**
  * `Index(value = ["studentId", "plannedDate"])`
  * `Index(value = ["studentId", "status"])`
  * `Index(value = ["plannedDate", "status"])`
  * `Index(value = ["isDeleted"])`

---

### 3.3. `ExamResult`
Stores evaluated performance, marks obtained, qualitative diagnostic feedback, and mastery ratings.

* **Table Name:** `exam_results`
* **Primary Key:** `id: String` (UUIDv4)
* **Fields:**
  * `id: String` (PK, UUID)
  * `examId: String` (FK -> `exams.id`, Required)
  * `studentId: String` (FK -> `students.id`, Required: de-normalized for indexed historical aggregation)
  * `actualExamDate: Long` (Required, UTC Epoch Ms when test was administered)
  * `marksObtained: Double` (Required, $\ge 0.0$ and $\le exam.totalMarks$)
  * `isPassed: Boolean` (Derived/Recorded boolean indicator)
  * `studentStrengths: String?` (Optional, topics/question types where student excelled)
  * `studentWeaknesses: String?` (Optional, conceptual gaps or calculation errors observed)
  * `recommendations: String?` (Optional, remedial assignments or revision strategies)
  * `teacherRemarks: String?` (Optional, overall qualitative feedback)
  * `gradedAt: Long` (Required, UTC Epoch Ms when evaluation was completed)
  * `createdAt: Long` (UTC Epoch Ms)
  * `updatedAt: Long` (UTC Epoch Ms)
  * `isDeleted: Boolean` (Default: `false`)
  * `deletedAt: Long?` (Nullable)
* **Foreign Keys:**
  * `entity = Exam::class, parentColumns = ["id"], childColumns = ["examId"], onDelete = ForeignKey.CASCADE`
  * `entity = Student::class, parentColumns = ["id"], childColumns = ["studentId"], onDelete = ForeignKey.CASCADE`
* **Indexes:**
  * `Index(value = ["examId"], unique = true)` (Ensures a clean 1:1 relationship between an exam event and its outcome)
  * `Index(value = ["studentId", "actualExamDate"])`
  * `Index(value = ["isDeleted"])`

---

## 4. Modified Existing Entity Definitions

### 4.1. `ClassSession` Entity Refinement

#### Review of Existing `topicCovered: String?` Field
* **Architectural Problem:** Does `ClassSession.topicCovered` conflict with the new `StudentDiary` entity?
* **Analysis:**
  * Option A: Remove `topicCovered` entirely from `ClassSession`.
    * *Drawback:* Every calendar grid, daily timetable, and schedule widget would require an eager `LEFT OUTER JOIN` against `student_diaries` just to display a brief 3-word title on a calendar pill.
  * Option B: Retain `topicCovered` as a micro-summary header.
    * *Recommendation:* **Retain `topicCovered: String?`** as a lightweight summary field (e.g., "Trigonometry Ex 9.1").
* **Formal Decision:** 
  1. `ClassSession.topicCovered` is strictly designated as a **read-optimized micro-summary** (max 100 characters) for high-speed dashboard and schedule lists.
  2. The detailed pedagogical record (what was taught, homework description, exercises, comprehension rating) lives exclusively in `StudentDiary`.
  3. When a `StudentDiary` entry is saved for a session, `ClassSession.topicCovered` is automatically updated with `StudentDiary.topicTitle` if blank. No schema modification to `ClassSession` is needed, preserving 100% backward compatibility.

---

### 4.2. `Student` Entity Modification (Guardian Communication Channels)

#### Motivation for Change
To support guardian communication without external APIs, the application requires persistent user preferences regarding how and when progress is formatted and dispatched.

#### Revised `Student` Entity Definition
* **Table Name:** `students`
* **Changes from STEP 1:** Added 4 persistent configuration fields (with safe defaults):
  1. `guardianPreferredChannel: String` (Default: `"WHATSAPP"`, Enum: `WHATSAPP`, `SMS`, `MESSENGER`, `EMAIL`, `OTHER`)
  2. `isGuardianProgressSharingEnabled: Boolean` (Default: `true`)
  3. `guardianReportLanguage: String` (Default: `"EN"`, Enum: `EN`, `BN`)
  4. `guardianReportFormat: String` (Default: `"TEXT_SUMMARY"`, Enum: `TEXT_SUMMARY`, `DETAILED_PDF`)

#### Complete Revised Fields Listing:
* `id: String` (PK, UUID)
* `name: String` (Required)
* `institution: String?`
* `classGrade: String?`
* `phone: String?`
* `guardianName: String?`
* `guardianPhone: String?`
* `address: String?`
* `monthlyFeeAmount: Double` (Default: `0.0`)
* `billingCycleDay: Int` (Default: `1`)
* `isActive: Boolean` (Default: `true`)
* `joinedDate: Long` (UTC Epoch Ms)
* **`guardianPreferredChannel: String`** *(NEW, Default: `"WHATSAPP"`)*
* **`isGuardianProgressSharingEnabled: Boolean`** *(NEW, Default: `true`)*
* **`guardianReportLanguage: String`** *(NEW, Default: `"EN"`)*
* **`guardianReportFormat: String`** *(NEW, Default: `"TEXT_SUMMARY"`)*
* `createdAt: Long` (UTC Epoch Ms)
* `updatedAt: Long` (UTC Epoch Ms)
* `isDeleted: Boolean` (Default: `false`)
* `deletedAt: Long?` (Nullable)

---

## 5. Student Diary Data Model & Query Mechanics

### Relationship Hierarchy
$$\text{Student } (1) \longrightarrow (N) \text{ StudentSubject } (1) \longrightarrow (N) \text{ StudentDiary}$$
$$\text{ClassSession } (0..1) \longrightarrow (N) \text{ StudentDiary}$$

A `StudentDiary` entry belongs directly to a `Student` and a `StudentSubject`. It optionally references a `ClassSession`. If a class was conducted, `classSessionId` links the pedagogical work directly to attendance. If the tutor assigns revision on an off-day, `classSessionId` remains `null`.

### Core DAO Query Specifications (`StudentDiaryDao`)
```kotlin
// 1. Reactive stream of all diary entries for a student
@Query("""
    SELECT * FROM student_diaries 
    WHERE studentId = :studentId AND isDeleted = 0 
    ORDER BY date DESC, createdAt DESC
""")
fun observeStudentDiaryHistory(studentId: String): Flow<List<StudentDiaryEntity>>

// 2. Filtered search by Subject and Date Range
@Query("""
    SELECT * FROM student_diaries 
    WHERE studentId = :studentId 
      AND (:subjectId IS NULL OR subjectId = :subjectId)
      AND date >= :startDateEpochMs 
      AND date <= :endDateEpochMs 
      AND isDeleted = 0
    ORDER BY date DESC
""")
fun filterDiaryEntries(
    studentId: String,
    subjectId: String?,
    startDateEpochMs: Long,
    endDateEpochMs: Long
): Flow<List<StudentDiaryEntity>>

// 3. Search query across topic, content, and homework
@Query("""
    SELECT * FROM student_diaries 
    WHERE studentId = :studentId 
      AND isDeleted = 0
      AND (topicTitle LIKE '%' || :query || '%' 
           OR whatWasTaught LIKE '%' || :query || '%' 
           OR homeworkAssigned LIKE '%' || :query || '%')
    ORDER BY date DESC
""")
fun searchDiaryEntries(studentId: String, query: String): Flow<List<StudentDiaryEntity>>

// 4. Retrieve diary entries for a specific class session
@Query("""
    SELECT * FROM student_diaries 
    WHERE classSessionId = :sessionId AND isDeleted = 0
    ORDER BY createdAt ASC
""")
suspend fun getDiaryEntriesForSession(sessionId: String): List<StudentDiaryEntity>
```

---

## 6. Exam & ExamResult Architecture Decision

### Structural Decision: Separate `Exam` and `ExamResult` Entities

#### Justification for Separation:
1. **Lifecycle Integrity:** An `Exam` is created days or weeks prior with status `PLANNED`. At planning time, obtained marks, strengths, weaknesses, and teacher grading remarks do not exist. Combining them into one entity would force 7 columns to be nullable and violate relational invariants.
2. **Deterministic Status Transitions:** 
   * Planned: `Exam` exists, `status = PLANNED`, zero records in `ExamResult`.
   * Completed: `ExamResult` inserted, `Exam.status` transitions atomically to `COMPLETED`.
   * Missed/Cancelled: `Exam.status` transitions to `MISSED` or `CANCELLED`, `ExamResult` remains uncreated.
3. **Future-Proofing for Small Groups / Syllabi:** If the tutor prepares an exam syllabus for 2 or 3 students simultaneously, a single `Exam` event can link to multiple `ExamResult` records without schema modifications.
4. **Calculated Metrics Rule:** Percentage is **derived on-demand** in domain models and DAOs:
   $$\text{Percentage} = \left(\frac{\text{marksObtained}}{\text{Exam.totalMarks}}\right) \times 100.0$$
   This guarantees that if an exam's total marks are corrected (e.g., from 40 to 50), all student percentages automatically recalculate accurately without database inconsistencies.

### Domain POJO with Room Embedded Query
```kotlin
data class ExamWithResult(
    @Embedded val exam: ExamEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "examId"
    )
    val result: ExamResultEntity?
) {
    val percentage: Double?
        get() = result?.let { (it.marksObtained / exam.totalMarks) * 100.0 }

    val gradeLevel: String?
        get() = percentage?.let { pct ->
            when {
                pct >= 80.0 -> "A+ (Outstanding)"
                pct >= 70.0 -> "A (Excellent)"
                pct >= 60.0 -> "A- (Very Good)"
                pct >= 50.0 -> "B (Satisfactory)"
                pct >= 40.0 -> "C (Pass)"
                else -> "F (Needs Immediate Attention)"
            }
        }
}
```

---

## 7. Monthly Progress Report Data Model

The Monthly Progress Report is **dynamically aggregated in memory** by a domain UseCase (`GenerateMonthlyProgressReportUseCase`) from live Room entities. No mutable cache table is stored.

```kotlin
data class MonthlyProgressReport(
    // 1. Student Metadata
    val studentId: String,
    val studentName: String,
    val institution: String,
    val classGrade: String,
    val guardianName: String,
    val guardianPhone: String,
    val year: Int,
    val month: Int, // 1..12
    val reportGeneratedDateEpochMs: Long,

    // 2. Class & Attendance Activity
    val classStats: AttendanceStats,

    // 3. Learning & Diary Activity
    val learningStats: LearningStats,

    // 4. Exam Performance Activity
    val examStats: ExamPerformanceStats,

    // 5. Financial Summary
    val financialStats: FinancialBalanceSummary,

    // 6. Qualitative Pedagogical Assessment
    val qualitativeAssessment: QualitativeAssessment
)

data class AttendanceStats(
    val expectedClasses: Int,
    val completedClasses: Int,
    val missedClasses: Int,
    val cancelledClasses: Int,
    val backupClassesCompleted: Int,
    val attendanceRatePercentage: Double // (completed / expected) * 100.0
)

data class LearningStats(
    val totalLessonsRecorded: Int,
    val subjectsCovered: List<String>,
    val majorTopicsCompleted: List<String>,
    val homeworkAssignedCount: Int,
    val homeworkCompletedCount: Int,
    val homeworkCompletionRate: Double, // (completed / assigned) * 100.0
    val averageUnderstandingRating: String // "EXCELLENT", "GOOD", etc.
)

data class ExamPerformanceStats(
    val totalExamsConducted: Int,
    val totalMarksPossible: Double,
    val totalMarksObtained: Double,
    val averagePercentage: Double,
    val highestPercentage: Double,
    val lowestPercentage: Double,
    val subjectBreakdown: List<SubjectExamMetric>,
    val trendVsPreviousMonth: PerformanceTrend // IMPROVED, STABLE, DECLINED, INSUFFICIENT_DATA
)

data class SubjectExamMetric(
    val subjectName: String,
    val examsCount: Int,
    val averageScorePercentage: Double
)

enum class PerformanceTrend { IMPROVED, STABLE, DECLINED, INSUFFICIENT_DATA }

data class FinancialBalanceSummary(
    val monthlyBilledFee: Double,
    val amountPaidInMonth: Double,
    val netCarriedForwardDue: Double,
    val isPaymentFullyCleared: Boolean
)

data class QualitativeAssessment(
    val keyStrengthsObserved: List<String>,
    val areasNeedingImprovement: List<String>,
    val recommendedActionsForGuardian: List<String>,
    val tutorOverallRemark: String,
    val nextMonthFocusSummary: String
)
```

### Entity Traceability Matrix
| Report Section | Source Database Tables | Aggregation Logic |
| :--- | :--- | :--- |
| **Student Info** | `students` | Simple retrieval where `id = studentId`. |
| **Attendance** | `class_sessions` | Count where `sessionDate` falls in month, group by `status`. |
| **Learning** | `student_diaries`, `student_subjects` | Count entries, collect unique `topicTitle`, ratio of `homeworkStatus`. |
| **Exams** | `exams`, `exam_results` | Join on `id = examId` where `actualExamDate` in month; `AVG`, `MAX`, `MIN`. |
| **Finances** | `monthly_fees`, `payments` | Sum of `payments.amountPaid` in month vs `monthly_fees.finalAmount`. |
| **Remarks** | `exam_results`, `student_diaries`, `notes` | Aggregated qualitative comments synthesized by UseCase. |

---

## 8. Guardian Communication Architecture

### Native Share Architecture (No Unsolicited SDKs / APIs)
```
┌─────────────────────────┐     ┌────────────────────────┐     ┌────────────────────────┐
│ Progress Preview Screen │ ──► │ GuardianMessageBuilder │ ──► │  Native Android Intent │
│ (Tutor reviews metrics) │     │ (Constructs message)   │     │  (ACTION_SEND / Sheet) │
└─────────────────────────┘     └────────────────────────┘     └───────────┬────────────┘
                                                                           │
                     ┌───────────────────────┬─────────────────────────────┼────────────────────────┐
                     ▼                       ▼                             ▼                        ▼
              [ WhatsApp ]           [ System SMS ]                [ Email App ]             [ Copy Text ]
```

### 1. Architectural Guardrails
* **No Background Automation:** The app **must never** automatically send an SMS or WhatsApp payload in the background. Every communication requires an explicit tutor tap in the Android UI.
* **Native Android ShareSheet:** Uses standard `Intent(Intent.ACTION_SEND)` targeting `text/plain` for summaries and `application/pdf` for printable report cards.
* **Direct Deep-Linking (Optional Quick Action):**
  ```kotlin
  // WhatsApp Direct Intent (Fallback to general ShareSheet if not installed)
  val cleanPhone = student.guardianPhone?.replace("[^0-9+]".toRegex(), "")
  val intent = Intent(Intent.ACTION_VIEW).apply {
      data = Uri.parse("https://api.whatsapp.com/send?phone=$cleanPhone&text=${Uri.encode(message)}")
  }
  ```

### 2. Concise Guardian Message Templates
The message structure is generated via a localized domain builder (`GuardianMessageFormatter`).

#### English Template (`guardianReportLanguage = "EN"`):
```text
📘 Monthly Progress Update: Rahim
🗓️ Period: September 2026
👨‍🏫 Tutor: Ahmed Karim (Tuition Manager)

--- CLASS ATTENDANCE ---
• Completed: 11 / 12 classes (91.7%)
• Missed / Rescheduled: 1 (Backup class completed)

--- ACADEMIC & HOMEWORK ---
• Subjects Covered: Mathematics, General Science
• Topics Mastered: Quadratic Equations, Light Reflection & Refraction
• Homework Compliance: 9 / 10 tasks completed (90%)
• Classroom Engagement: Excellent

--- EXAM RESULTS ---
• Tests Conducted: 2
• Average Score: 84.5%
• Highest Score: 89.0% (General Science - Chapter 5)
• Strong Areas: Problem-solving in Algebra, ray diagrams
• Focus Areas: Formula derivation precision

--- FINANCIAL SUMMARY ---
• Monthly Fee: 4,000 BDT
• Received: 4,000 BDT (Paid in full)

--- TUTOR'S OVERALL REMARKS ---
"Rahim showed strong dedication this month, especially in Mathematics. Continual revision of Science formulas before next month's midterm is advised."

Thank you for your cooperation!
```

#### Bengali Template (`guardianReportLanguage = "BN"`):
```text
📘 মাসিক অগ্রগতি প্রতিবেদন: রহিম
🗓️ সময়কাল: সেপ্টেম্বর ২০২৬
👨‍🏫 শিক্ষক: আহমেদ করিম

--- ক্লাসের উপস্থিতি ---
• সম্পন্ন ক্লাস: ১১ / ১২ (৯১.৭%)
• বাতিল/অনুপস্থিত: ১ (বিকল্প ক্লাস সম্পন্ন)

--- পড়াশোনা ও হোমওয়ার্ক ---
• বিষয়সমূহ: গণিত, সাধারণ বিজ্ঞান
• অধ্যায়: দ্বিঘাত সমীকরণ, আলোর প্রতিফলন
• হোমওয়ার্ক সম্পন্ন: ৯ / ১০ (৯০%)
• ক্লাসে মনোযোগ: খুব ভালো

--- পরীক্ষার ফলাফল ---
• মোট পরীক্ষা: ২টি
• গড় নম্বর: ৮৪.৫%
• সর্বোচ্চ নম্বর: ৮৯% (বিজ্ঞান - অধ্যায় ৫)
• শক্তিশালী দিক: বীজগণিতের সমস্যা সমাধান
• উন্নতির ক্ষেত্র: বিজ্ঞানের সূত্রের নির্ভুলতা

--- টিউশন ফি তথ্য ---
• মাসিক ফি: ৪,০০০ টাকা
• পরিশোধিত: ৪,০০০ টাকা (পরিশোধ সম্পন্ন)

--- শিক্ষকের মন্তব্য ---
"রহিম এই মাসে গণিতে বেশ ভালো অগ্রগতি করেছে। আগামী মাসের মিডটার্মের জন্য বিজ্ঞানের সূত্রগুলো নিয়মিত রিভিশন করার পরামর্শ রইল।"

ধন্যবাদান্তে,
গৃহশিক্ষক
```

---

## 9. Updated Google Sheets Schema (17 Worksheets)

The single backup spreadsheet expands from **14 tabs to 17 tabs**. Tab 1 (`AppInfo`) schema version is bumped to `2`. Tab 3 (`Students`) gains 4 new columns.

### Detailed Table of All 17 Tabs
| Tab # | Worksheet Name | Columns (Exact Frozen Row 1 Order) | Purpose |
| :--- | :--- | :--- | :--- |
| **1** | `AppInfo` | `app_name, app_identifier, schema_version, created_at, last_backup_at, device_id` | Metadata & version tracking (`schema_version = 2`) |
| **2** | `TutorProfile` | `id, name, email, phone, default_hourly_rate, default_monthly_fee, currency_code, created_at, updated_at, is_deleted, deleted_at` | Tutor profile details |
| **3** | `Students` | `id, name, institution, class_grade, phone, guardian_name, guardian_phone, address, monthly_fee_amount, billing_cycle_day, is_active, joined_date, guardian_preferred_channel, is_guardian_progress_sharing_enabled, guardian_report_language, guardian_report_format, created_at, updated_at, is_deleted, deleted_at` | Student profiles + Guardian preferences (Cols 13-16 new) |
| **4** | `StudentSubjects` | `id, student_id, subject_name, created_at, updated_at, is_deleted, deleted_at` | Assigned student subjects |
| **5** | `Schedules` | `id, student_id, subject_id, day_of_week, start_time_minutes, end_time_minutes, effective_start_date, effective_end_date, is_active, created_at, updated_at, is_deleted, deleted_at` | Recurring timetable |
| **6** | `ClassSessions` | `id, student_id, schedule_id, subject_id, session_date, scheduled_start_time, scheduled_end_time, actual_start_time, actual_end_time, status, remarks, topic_covered, created_at, updated_at, is_deleted, deleted_at` | Individual class occurrences |
| **7** | `RescheduleRecords` | `id, original_session_id, new_session_id, rescheduled_by, reason, requested_at, created_at, updated_at, is_deleted, deleted_at` | Session reschedule audit logs |
| **8** | `BackupClasses` | `id, missed_session_id, backup_session_id, note, created_at, updated_at, is_deleted, deleted_at` | Compensation class links |
| **9** | `StudentDiary` *(NEW)* | `id, student_id, subject_id, class_session_id, date, topic_title, what_was_taught, homework_assigned, homework_status, practice_given, student_understanding, teacher_remarks, next_class_plan, created_at, updated_at, is_deleted, deleted_at` | Structured daily learning & homework records (17 cols) |
| **10** | `Exams` *(NEW)* | `id, student_id, subject_id, title, syllabus_topic, planned_date, planned_start_time_minutes, duration_minutes, total_marks, passing_marks, exam_type, status, notes, created_at, updated_at, is_deleted, deleted_at` | Planned and conducted exams (17 cols) |
| **11** | `ExamResults` *(NEW)* | `id, exam_id, student_id, actual_exam_date, marks_obtained, is_passed, student_strengths, student_weaknesses, recommendations, teacher_remarks, graded_at, created_at, updated_at, is_deleted, deleted_at` | Evaluation scores and diagnostic remarks (15 cols) |
| **12** | `MonthlyFees` | `id, student_id, year, month, base_amount, adjustment_amount, final_amount, due_date, notes, created_at, updated_at, is_deleted, deleted_at` | Monthly tuition obligations |
| **13** | `Payments` | `id, student_id, monthly_fee_id, amount_paid, payment_date, payment_method, transaction_reference, receipt_number, notes, created_at, updated_at, is_deleted, deleted_at` | Monies collected |
| **14** | `Income` | `id, category, source_title, amount, date, linked_payment_id, remarks, created_at, updated_at, is_deleted, deleted_at` | Auxiliary & tuition revenues |
| **15** | `Expenses` | `id, category, title, amount, date, receipt_image_uri, remarks, created_at, updated_at, is_deleted, deleted_at` | Operational costs |
| **16** | `Notes` | `id, student_id, title, content, is_pinned, created_at, updated_at, is_deleted, deleted_at` | Freeform student notes |
| **17** | `SyncMetadata` | `entity_type, last_synced_at, last_sync_status, device_id, updated_at` | Watermarks across all entity types |

---

## 10. Updated Synchronization Architecture

### Dependency Order for Foreign-Key-Safe Operations

```
                   1. TutorProfile
                         │
                         ▼
                    2. Students
                         │
        ┌────────────────┼────────────────┐
        ▼                ▼                ▼
3. StudentSubjects  12. MonthlyFees   16. Notes
        │                │
        ├────────────────┤
        ▼                ▼
   4. Schedules     13. Payments
        │                │
        ▼                ▼
 5. ClassSessions   14. Income (linked to payment)
        │
  ┌─────┴────────────────────────┐
  │                              ▼
  │                     15. Expenses
  ▼
6. RescheduleRecords
7. BackupClasses
8. StudentDiary (Refs: Student, Subject, ClassSession)
9. Exams (Refs: Student, Subject)
10. ExamResults (Refs: Exam, Student)
```

### Strict Insertion & Restoration Sequence
When restoring a full spreadsheet from Google Drive onto a new device, Room must insert records in this exact topological order inside an atomic transaction:
1. `TutorProfile`
2. `Students`
3. `StudentSubjects`
4. `Schedules`
5. `ClassSessions`
6. `RescheduleRecords`
7. `BackupClasses`
8. `StudentDiary` *(Depends on 2, 3, 5)*
9. `Exams` *(Depends on 2, 3)*
10. `ExamResults` *(Depends on 9, 2)*
11. `MonthlyFees` *(Depends on 2)*
12. `Payments` *(Depends on 2, 11)*
13. `Income` *(Depends on 12)*
14. `Expenses`
15. `Notes` *(Depends on 2)*
16. `SyncMetadata`

*Reversing this sequence during deletion/purge guarantees zero foreign-key constraint violations.*

---

## 11. Updated Conflict-Resolution Strategy

### Multi-Device Last-Modified-Wins (LMW) with Tombstone Precedence
The conflict resolution engine established in STEP 1 applies seamlessly to `StudentDiary`, `Exam`, and `ExamResult`:

1. **Deterministic Primary Keys:** All records across all tables use client-generated **UUIDv4** strings.
2. **Evaluation Algorithm:**
   * If record $R$ exists locally and in cloud:
     * Check `isDeleted` tombstones:
       * If local is deleted and cloud is not: If $R_{local}.deletedAt \ge R_{cloud}.updatedAt$, local deletion wins and cloud row is updated to `is_deleted = TRUE`.
       * If cloud was modified *after* local deletion ($R_{cloud}.updatedAt > R_{local}.deletedAt$), cloud wins and local row is resurrected.
     * If neither or both are deleted: The entity with the highest `updatedAt` timestamp overwrites the older record.
3. **Exam Result Cascading Rules:**
   * If an `Exam` is marked `isDeleted = true` via conflict resolution, its corresponding `ExamResult` is automatically soft-deleted locally:
     ```sql
     UPDATE exam_results 
     SET isDeleted = 1, deletedAt = :timestamp 
     WHERE examId = :examId AND isDeleted = 0;
     ```

---

## 12. Updated Navigation & Screen Architecture

The UI architecture expands with dedicated academic and evaluation destinations, accessible directly through both the main navigation and the contextual **Student Profile Hub**.

```
                           ┌─────────────────────────┐
                           │     Main Dashboard      │
                           └────────────┬────────────┘
                                        │
      ┌──────────────────┬──────────────┼────────────────┬──────────────────┐
      ▼                  ▼              ▼                ▼                  ▼
┌───────────┐      ┌───────────┐  ┌───────────┐    ┌───────────┐      ┌───────────┐
│ Students  │      │ Schedule  │  │  Classes  │    │  Finance  │      │ Settings  │
└─────┬─────┘      └───────────┘  └───────────┘    └───────────┘      └─────┬─────┘
      │                                                                     │
      ▼                                                                     ▼
┌──────────────────────────────────────┐                       ┌─────────────────────────┐
│      Student Profile Hub Screen      │                       │ Cloud Backup & Restore  │
│  Tabs:                               │                       │ Google Sheets v4 Config │
│  • [Overview] Quick bio & metrics    │                       └─────────────────────────┘
│  • [Diary] Structured learning log   │
│  • [Exams] Planner & score history   │
│  • [Classes] Attendance & schedule   │
│  • [Payments] Billing ledger         │
│  • [Progress] Monthly report generator
│  • [Notes] Pedagogical notes         │
└──────────────────┬───────────────────┘
                   │
    ┌──────────────┴────────────────────────────┐
    ▼                                           ▼
┌─────────────────────────┐           ┌─────────────────────────┐
│  Diary Entry Edit/Add   │           │    Exam Edit / Record   │
│  • Topic & Syllabus     │           │    • Plan Date & Marks  │
│  • What was taught      │           │    • Record Results     │
│  • Homework & Practice  │           │    • Diagnostic Feedback│
│  • Understanding rating │           └─────────────────────────┘
└─────────────────────────┘
    │
    ▼
┌──────────────────────────────────────────────────────────────┐
│             Guardian Report & Preview Screen                 │
│  • Month Selector                                            │
│  • Auto-aggregated Metrics & Qualitative Feedback            │
│  • Live Narrative Message Preview (EN / BN)                  │
│  • [Copy Text] • [Send via WhatsApp] • [Share Native Sheet]  │
└──────────────────────────────────────────────────────────────┘
```

---

## 13. Updated Dashboard Architecture

To avoid visual clutter while providing immediate operational utility, the Dashboard is organized into **4 functional tiers**:

```
┌────────────────────────────────────────────────────────────────────────────────────────┐
│                                    TOP APP BAR                                         │
│  Tuition Manager  •  September 2026                 [Cloud Sync Icon] [Settings Icon] │
├────────────────────────────────────────────────────────────────────────────────────────┤
│ TIER 1: CRITICAL TIMETABLE & TODAY'S ACTION ITEMS                                      │
│ ┌────────────────────────────────────────┐  ┌────────────────────────────────────────┐ │
│ │ 📅 Today's Classes (e.g., 2 Scheduled) │  │ 📝 Today's / Upcoming Exams (e.g., 1)  │ │
│ │ • 4:00 PM: Rahim (Math) - Start / Mark │  │ • 6:00 PM: Tanvir (Physics Quiz Ch 3)  │ │
│ └────────────────────────────────────────┘  └────────────────────────────────────────┘ │
├────────────────────────────────────────────────────────────────────────────────────────┤
│ TIER 2: ACADEMIC PROGRESS & HOMEWORK ALERTS                                            │
│ ┌────────────────────────────────────────────────────────────────────────────────────┐ │
│ │ ⚠️ Academic Attention Needed                                                       │ │
│ │ • 2 Homework submissions overdue (Rahim, Ayesha)                                   │ │
│ │ • Tanvir scored < 50% on Recent Math Quiz - Remedial action recommended            │ │
│ └────────────────────────────────────────────────────────────────────────────────────┘ │
├────────────────────────────────────────────────────────────────────────────────────────┤
│ TIER 3: MONTH-TO-DATE TEACHING SUMMARY                                                 │
│ ┌───────────────────────┐  ┌────────────────────────┐  ┌─────────────────────────────┐ │
│ │ Classes Conducted     │  │ Exams Graded           │  │ Average Student Mastery     │ │
│ │ 28 / 32 (87.5%)       │  │ 6 Tests Completed      │  │ 81.4% Across All Subjects   │ │
│ └───────────────────────┘  └────────────────────────┘  └─────────────────────────────┘ │
├────────────────────────────────────────────────────────────────────────────────────────┤
│ TIER 4: FINANCIAL PULSE                                                                │
│ ┌────────────────────────────────────────────────────────────────────────────────────┐ │
│ │ 💰 Collected: 24,000 BDT  •  Pending Dues: 6,000 BDT  •  Total Students: 8 Active   │ │
│ └────────────────────────────────────────────────────────────────────────────────────┘ │
└────────────────────────────────────────────────────────────────────────────────────────┘
```

---

## 14. Notification & Reminder Architecture (100% Offline)

All notifications operate natively on-device without cloud services (Firebase-free) using Android's `AlarmManager` and `WorkManager`.

```
┌─────────────────────────────────────────────────────────────────────────────────────────┐
│                                LOCAL NOTIFICATION ENGINE                                │
│                                                                                         │
│  ┌────────────────────────────────────────┐  ┌────────────────────────────────────────┐  │
│  │       AlarmManager (Exact Alarms)      │  │      WorkManager (Periodic Tasks)      │  │
│  │ • 24h & 2h before Planned Exam         │  │ • Daily 9:00 PM Attendance check       │  │
│  │ • 30m before Scheduled Class Session   │  │ • Monthly 1st: Progress Report Reminder│  │
│  │ • Direct Intent to NotificationManager │  │ • Evaluates unrecorded sessions & tests│  │
│  └────────────────────────────────────────┘  └────────────────────────────────────────┘  │
│                                       │                      │                          │
│                                       ▼                      ▼                          │
│                     ┌──────────────────────────────────────────────────┐                │
│                     │ Android NotificationManager (Channels: ALERTS,   │                │
│                     │ REMINDERS, EXAMS, FINANCIAL)                     │                │
│                     └──────────────────────────────────────────────────┘                │
└─────────────────────────────────────────────────────────────────────────────────────────┘
```

### Notification Trigger Channels
1. **Channel `EXAM_REMINDERS` (High Importance):**
   * Scheduled upon creating an `Exam` with status `PLANNED`.
   * Alarm 1: 24 hours prior to `plannedDate` at 08:00 AM.
   * Alarm 2: 2 hours prior to planned start time.
2. **Channel `CLASS_ATTENDANCE` (Default Importance):**
   * Scheduled 30 minutes before class session start.
3. **Channel `PROGRESS_REPORTS` (Low/Default Importance):**
   * Scheduled by `WorkManager` on the 1st day of every month at 10:00 AM:
     *"Monthly progress reports for August are ready to review and share with guardians."*

---

## 15. Database Migration Plan (Version 1 to Version 2)

### Room Database Migration: `MIGRATION_1_2`
The schema changes from Database v1 to Database v2 are non-destructive and preserve 100% of existing user records.

```kotlin
val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {
        // 1. Add new columns to students table
        db.execSQL("""
            ALTER TABLE students 
            ADD COLUMN guardianPreferredChannel TEXT NOT NULL DEFAULT 'WHATSAPP'
        """)
        db.execSQL("""
            ALTER TABLE students 
            ADD COLUMN isGuardianProgressSharingEnabled INTEGER NOT NULL DEFAULT 1
        """)
        db.execSQL("""
            ALTER TABLE students 
            ADD COLUMN guardianReportLanguage TEXT NOT NULL DEFAULT 'EN'
        """)
        db.execSQL("""
            ALTER TABLE students 
            ADD COLUMN guardianReportFormat TEXT NOT NULL DEFAULT 'TEXT_SUMMARY'
        """)

        // 2. Create student_diaries table
        db.execSQL("""
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
        """)
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_student_diaries_studentId_date` ON `student_diaries` (`studentId`, `date`)")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_student_diaries_studentId_subjectId_date` ON `student_diaries` (`studentId`, `subjectId`, `date`)")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_student_diaries_classSessionId` ON `student_diaries` (`classSessionId`)")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_student_diaries_homeworkStatus` ON `student_diaries` (`homeworkStatus`)")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_student_diaries_isDeleted` ON `student_diaries` (`isDeleted`)")

        // 3. Create exams table
        db.execSQL("""
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
        """)
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_exams_studentId_plannedDate` ON `exams` (`studentId`, `plannedDate`)")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_exams_studentId_status` ON `exams` (`studentId`, `status`)")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_exams_plannedDate_status` ON `exams` (`plannedDate`, `status`)")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_exams_isDeleted` ON `exams` (`isDeleted`)")

        // 4. Create exam_results table
        db.execSQL("""
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
        """)
        db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_exam_results_examId` ON `exam_results` (`examId`)")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_exam_results_studentId_actualExamDate` ON `exam_results` (`studentId`, `actualExamDate`)")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_exam_results_isDeleted` ON `exam_results` (`isDeleted`)")
    }
}
```

---

## 16. Updated Project Folder Structure

```
app/src/main/java/com/tuitionmanager/
├── TuitionManagerApp.kt
├── di/
│   ├── DatabaseModule.kt
│   ├── RepositoryModule.kt
│   ├── UseCaseModule.kt
│   └── NetworkModule.kt
├── data/
│   ├── local/
│   │   ├── TuitionDatabase.kt           # RoomDatabase (Version 2)
│   │   ├── MIGRATIONS.kt                # MIGRATION_1_2 implementation
│   │   ├── Converters.kt
│   │   ├── dao/
│   │   │   ├── StudentDao.kt            # Updated with guardian fields
│   │   │   ├── ScheduleDao.kt
│   │   │   ├── ClassSessionDao.kt
│   │   │   ├── PaymentDao.kt
│   │   │   ├── MonthlyFeeDao.kt
│   │   │   ├── FinanceDao.kt
│   │   │   ├── NoteDao.kt
│   │   │   ├── StudentDiaryDao.kt       # [NEW]
│   │   │   ├── ExamDao.kt               # [NEW]
│   │   │   ├── ExamResultDao.kt         # [NEW]
│   │   │   └── SyncMetadataDao.kt
│   │   └── entity/
│   │       ├── StudentEntity.kt         # Modified (guardian columns)
│   │       ├── StudentDiaryEntity.kt    # [NEW]
│   │       ├── ExamEntity.kt            # [NEW]
│   │       └── ExamResultEntity.kt      # [NEW]
│   ├── remote/
│   │   └── google/
│   │       ├── GoogleSheetsClient.kt    # Updated with 17 tabs
│   │       └── SheetsSchemaValidator.kt # Tab verification for new schemas
│   ├── repository/
│   │   ├── StudentRepositoryImpl.kt
│   │   ├── StudentDiaryRepositoryImpl.kt # [NEW]
│   │   ├── ExamRepositoryImpl.kt         # [NEW]
│   │   └── SyncRepositoryImpl.kt
│   └── sync/
│       └── SyncEngine.kt
├── domain/
│   ├── model/
│   │   ├── StudentDiary.kt              # [NEW]
│   │   ├── Exam.kt                      # [NEW]
│   │   ├── ExamResult.kt                # [NEW]
│   │   ├── ExamWithResult.kt            # [NEW]
│   │   ├── MonthlyProgressReport.kt     # [NEW]
│   │   └── GuardianMessageConfig.kt     # [NEW]
│   ├── repository/
│   │   ├── StudentDiaryRepository.kt    # [NEW]
│   │   └── ExamRepository.kt            # [NEW]
│   ├── engine/
│   │   ├── ScheduleConflictEngine.kt
│   │   ├── ExamEvaluationEngine.kt      # [NEW] Percentage & grading math
│   │   └── GuardianMessageFormatter.kt  # [NEW] EN/BN template renderer
│   └── usecase/
│       ├── diary/                       # [NEW]
│       │   ├── AddDiaryEntryUseCase.kt
│       │   ├── FilterDiaryHistoryUseCase.kt
│       │   └── SearchDiaryUseCase.kt
│       ├── exam/                        # [NEW]
│       │   ├── PlanExamUseCase.kt
│       │   ├── RecordExamResultUseCase.kt
│       │   └── CalculateStudentExamStatsUseCase.kt
│       └── report/                      # [NEW]
│           ├── GenerateMonthlyProgressReportUseCase.kt
│           └── BuildGuardianShareIntentUseCase.kt
├── ui/
│   ├── navigation/
│   │   └── Screen.kt                    # Added Diary, Exam, Report routes
│   ├── dashboard/                       # Updated with Exam & Progress tiles
│   ├── diary/                           # [NEW] Diary UI
│   │   ├── DiaryHistoryScreen.kt
│   │   ├── DiaryEntryScreen.kt
│   │   └── components/DiaryCard.kt
│   ├── exam/                            # [NEW] Exam UI
│   │   ├── ExamListScreen.kt
│   │   ├── ExamPlannerScreen.kt
│   │   ├── RecordResultScreen.kt
│   │   └── components/ExamScorecard.kt
│   ├── report/                          # [NEW] Progress & Guardian UI
│   │   ├── MonthlyReportScreen.kt
│   │   ├── GuardianPreviewScreen.kt
│   │   └── components/AttendanceGraph.kt
│   └── students/
│       └── StudentProfileScreen.kt      # Upgraded to 7-Tab Layout
└── utils/
    ├── AlarmScheduler.kt                # [NEW] Offline Exam reminders
    └── PdfReportGenerator.kt            # Upgraded for Student Progress PDF
```

---

## 17. Updated Implementation Phases

Building upon the 20 phases from STEP 1, the expanded roadmap integrates the new academic and reporting capabilities:

* **Phases 1–8:** Foundation, Room Database, Conflict Engine, Navigation Shell, Student Management, Schedules, Conflict Integration, and Class Attendance *(from STEP 1)*.
* **Phase 9: Student Diary Engine & UI *(NEW)***
  * Implement `StudentDiaryEntity`, `StudentDiaryDao`, Repository, and UseCases.
  * Build `DiaryEntryScreen`, `DiaryHistoryScreen`, and session-to-diary integration.
* **Phase 10: Exam Planning & Evaluation Subsystem *(NEW)***
  * Implement `ExamEntity`, `ExamResultEntity`, DAOs, and `ExamEvaluationEngine`.
  * Build `ExamPlannerScreen` (dates, syllabus, marks) and `RecordResultScreen` (scores, strengths, weaknesses).
* **Phase 11: Payments & Billing System *(formerly Phase 9)***
* **Phase 12: General Finance (Income & Expense) *(formerly Phase 10)***
* **Phase 13: Monthly Progress Aggregator & Analytics Engine *(NEW)***
  * Implement `GenerateMonthlyProgressReportUseCase` (zero-mutation dynamic aggregator).
  * Build student progress analytics view with historical trend comparison.
* **Phase 14: Guardian Communication & Native Share Sheet *(NEW)***
  * Build `GuardianPreviewScreen` with language toggles (EN / BN).
  * Implement Android native `ACTION_SEND` intent dispatch for WhatsApp, SMS, and clipboard.
* **Phase 15: Unified Dashboard Integration *(Enhanced Phase 11)***
  * Integrate today's upcoming exams, homework alerts, and monthly academic progress.
* **Phase 16: Local Notification Scheduler *(Enhanced Phase 15)***
  * Exact alarm triggers for upcoming exams (24h/2h prior) and monthly report reminders.
* **Phase 17: Google Sheets Synchronization Extension *(Enhanced Phase 14)***
  * Expand `GoogleSheetsClient` to support all 17 worksheets.
  * Verify multi-tab batch atomic updates and restore order for diary/exam data.
* **Phase 18: Security, Biometrics & PIN Lock *(formerly Phase 16)***
* **Phase 19: PDF Progress Report Generator *(Enhanced Phase 17)***
  * Generate printable, branded PDF progress report cards for guardian sharing.
* **Phase 20: Comprehensive Quality Assurance, Stress Testing & Release Package**
  * Migration test from v1 to v2, large dataset stress testing, and final APK build.

---

## 18. Comprehensive Testing Strategy

### 1. Student Diary Test Matrix
* **Validation:** Verify `topicTitle` and `whatWasTaught` cannot be empty strings.
* **Filter Integrity:** Test date range boundary conditions (e.g., `date = startDate` and `date = endDate`).
* **Cascade Deletion:** Verify deleting a `Student` cascades and removes all associated `StudentDiary` entries.
* **Null Reference Safety:** Verify deleting a `ClassSession` sets `classSessionId` in linked diaries to `NULL` without deleting the diary entry.

### 2. Exam & Evaluation Test Matrix
* **Mark Validation:** Verify saving an `ExamResult` with `marksObtained > totalMarks` or `marksObtained < 0` throws an `IllegalArgumentException`.
* **Percentage Calculation:** Verify exact floating-point division ($42.0 / 50.0 = 84.0\%$).
* **Status Transitions:** Test transitions: `PLANNED -> COMPLETED`, `PLANNED -> MISSED`, and `PLANNED -> CANCELLED`.
* **Syllabus Search:** Test SQL index performance when searching exam syllabi.

### 3. Progress Report Aggregation Test Matrix
* **Boundary Months:** Verify calculating statistics for February in leap years vs non-leap years.
* **Zero-Data Resilience:** Test report generation for a newly enrolled student with 0 completed classes, 0 diary records, and 0 exams. Must produce clean 0% values without `NaN` or `ArithmeticException`.
* **Financial Parity:** Verify `financialStats.netCarriedForwardDue` matches derived balance from the `Payment` ledger.

### 4. Cloud Backup & Sync Test Matrix
* **Schema Integrity:** Verify `SheetsSchemaValidator` confirms all 17 worksheets exist with correct Row 1 frozen headers.
* **Foreign-Key-Safe Restore:** Emulate restoring a fresh phone from Google Sheets and confirm all 17 tabs populate without foreign-key constraint failures.
* **Tombstone Sync:** Verify soft-deleted exams and diary entries propagate deletion to the cloud sheet.

### 5. Guardian Communication Test Matrix
* **No Background Dispatch:** Verify tapping "Share" opens the Android system Chooser dialog (`Intent.createChooser`) and does not invoke background network calls.
* **Localization Verification:** Verify language switch renders correct Bengali unicode text and English templates.

---

## 19. Security & Privacy Considerations

1. **Child & Student Privacy:** Student names, guardian contact details, and performance evaluations are stored solely on-device in private app storage. No analytics or advertising SDKs have access to this data.
2. **Restricted Cloud Storage:** Google Sheets backups are stored exclusively within the user's personal Google Drive using the narrow `drive.file` OAuth scope (the app can only access spreadsheets it created).
3. **No Unencrypted External APIs:** Guardian communications avoid insecure third-party SMS gateway intermediaries; message delivery is mediated entirely by the user's installed, authenticated communication apps (WhatsApp, default SMS, Gmail).

---

## 20. Risks, Edge Cases & Technical Mitigations

| Identified Risk / Edge Case | Impact | Technical Mitigation |
| :--- | :--- | :--- |
| **Mark Obtained Exceeds Total Marks** | Distorted percentage metrics (>100%) and broken graphs | Domain validation in `RecordExamResultUseCase` strictly enforces $0.0 \le \text{marksObtained} \le \text{totalMarks}$ prior to DAO insertion. |
| **Orphaned Diary on Session Deletion** | Loss of valuable pedagogical records when a class session is deleted | Enforce `ForeignKey.SET_NULL` on `StudentDiary.classSessionId`. The diary record is preserved as an independent learning entry. |
| **Zero Exams in Reporting Month** | Division by zero crashing report generator | Domain aggregator guards calculations with Kotlin Elvis checks: `if (totalExams == 0) 0.0 else (sum / total)`. |
| **Huge Text in What Was Taught** | Exceeding Google Sheets single-cell character limit (50,000 chars) | Input text fields in UI cap entry length at 5,000 characters (more than sufficient for lesson notes). |
| **Guardian Communication Misdirection** | Sending a student's report to the wrong phone number | Phone number is pulled dynamically from the verified `Student` record, and preview screen explicitly displays the recipient before sharing. |

---

## 21. Final Recommended Implementation Order

When proceeding from architecture into coding, execute the STEP 2 additions in this strict sequence:

1. **Schema Migration:** Apply `MIGRATION_1_2` to `TuitionDatabase.kt` and add entities `StudentDiaryEntity`, `ExamEntity`, and `ExamResultEntity`.
2. **DAOs & Repository Core:** Implement and unit test `StudentDiaryDao`, `ExamDao`, and `ExamResultDao`.
3. **Pedagogical UI (Diary):** Build `DiaryEntryScreen` and `DiaryHistoryScreen` to enable day-to-day lesson logging.
4. **Evaluation Engine & UI (Exams):** Implement `ExamEvaluationEngine`, `ExamPlannerScreen`, and `RecordResultScreen`.
5. **Dynamic Progress Reporting:** Implement `GenerateMonthlyProgressReportUseCase` to aggregate attendance, diary, and exams.
6. **Guardian Communication:** Build `GuardianPreviewScreen` and the Android `ACTION_SEND` share broker.
7. **Dashboard Integration:** Surface upcoming exams and academic alerts on the main dashboard.
8. **Offline Notification Alarms:** Register `AlarmManager` reminders for upcoming exams.
9. **Google Sheets Sync Expansion:** Update `GoogleSheetsClient` to backup and restore all 17 worksheets.

---

## STEP 2 ARCHITECTURE DECISIONS

1. **Separation of `Exam` and `ExamResult` Entities:**
   * *Decision:* Maintain two distinct entities linked by a 1:1 foreign key.
   * *Rationale:* An exam is scheduled in advance (`PLANNED`), whereas results exist only post-evaluation. Combining them would force numerous evaluation fields to be permanently nullable during the planning phase. Separation also allows future support for re-takes or multi-student syllabus sharing without altering table structures.
2. **Preservation of `ClassSession.topicCovered`:**
   * *Decision:* Keep `ClassSession.topicCovered` as a micro-summary string while housing comprehensive teaching narratives in `StudentDiary`.
   * *Rationale:* Avoids expensive SQL joins when rendering fast calendar grids or daily schedule cards, while maintaining clean normalized data for detailed pedagogical logs.
3. **Zero-Mutation Dynamic Progress Reporting:**
   * *Decision:* Generate monthly progress reports on-demand in memory rather than persisting immutable snapshot tables.
   * *Rationale:* Prevents database bloat, eliminates stale caching issues if historical records are edited, and guarantees that any corrected exam score or rescheduled class is instantly reflected in the report.
4. **Pure Native Share Architecture for Guardians:**
   * *Decision:* Utilize Android's native `Intent.ACTION_SEND` (ShareSheet) with pre-formatted localized templates (EN/BN) instead of external SMS/WhatsApp APIs.
   * *Rationale:* Retains 100% offline functionality, incurs zero messaging fees, protects student privacy, and eliminates accidental background spamming by requiring explicit tutor confirmation.
5. **Deterministic Topological Sync Order:**
   * *Decision:* Enforce strict foreign-key dependency ordering across all 17 Google Sheets worksheets during full backup and restore.
   * *Rationale:* Guarantees that parents are inserted before children during a fresh restore on a new device, eliminating SQLite foreign key constraint failures.
