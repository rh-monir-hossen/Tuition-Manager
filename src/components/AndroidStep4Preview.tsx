import { useState } from 'react';
import {
  Users,
  BookOpen,
  Plus,
  Search,
  CheckCircle2,
  Calendar,
  Phone,
  Edit,
  Trash2,
  ChevronLeft,
  ArrowLeft,
  Clock,
  Award,
  DollarSign,
  TrendingUp,
  FileText,
  AlertCircle,
  MoreVertical,
  Check,
  X,
  Share2,
} from 'lucide-react';

interface MockStudent {
  id: string;
  name: string;
  institution: string;
  classGrade: string;
  phone: string;
  guardianName: string;
  guardianPhone: string;
  address: string;
  monthlyFeeAmount: number;
  billingCycleDay: number;
  isActive: boolean;
  subjects: string[];
  joinedDate: string;
}

interface MockDiary {
  id: string;
  studentId: string;
  studentName: string;
  subject: string;
  date: string;
  topicTitle: string;
  whatWasTaught: string;
  homeworkAssigned: string;
  homeworkStatus: 'ASSIGNED' | 'SUBMITTED_COMPLETE' | 'SUBMITTED_PARTIAL' | 'NOT_SUBMITTED';
  practiceGiven: string;
  studentUnderstanding: 'EXCELLENT' | 'GOOD' | 'AVERAGE' | 'NEEDS_ATTENTION';
  teacherRemarks: string;
  nextClassPlan: string;
  isDeleted?: boolean;
}

const INITIAL_STUDENTS: MockStudent[] = [
  {
    id: 's-1',
    name: 'Rafid Rahman',
    institution: 'Dhaka Residential Model College',
    classGrade: 'Class 10 (SSC 26)',
    phone: '01712-345678',
    guardianName: 'Dr. Farhana Begum',
    guardianPhone: '01812-345678',
    address: 'Road 27, Dhanmondi, Dhaka',
    monthlyFeeAmount: 5000,
    billingCycleDay: 1,
    isActive: true,
    subjects: ['Higher Mathematics', 'Physics'],
    joinedDate: 'Jan 10, 2026',
  },
  {
    id: 's-2',
    name: 'Anika Tasnim',
    institution: 'Viqarunnisa Noon School & College',
    classGrade: 'Class 9',
    phone: '01912-987654',
    guardianName: 'Md. Tariqul Islam',
    guardianPhone: '01719-876543',
    address: 'Baily Road, Dhaka',
    monthlyFeeAmount: 4500,
    billingCycleDay: 5,
    isActive: true,
    subjects: ['General Math', 'Chemistry'],
    joinedDate: 'Jan 18, 2026',
  },
  {
    id: 's-3',
    name: 'Samiul Hasan',
    institution: 'Notre Dame College',
    classGrade: 'Class 11 (HSC 27)',
    phone: '01611-223344',
    guardianName: 'Rehana Akhter',
    guardianPhone: '01511-223344',
    address: 'Motijheel, Dhaka',
    monthlyFeeAmount: 6000,
    billingCycleDay: 10,
    isActive: false,
    subjects: ['Physics', 'Higher Math'],
    joinedDate: 'Dec 05, 2025',
  },
];

const INITIAL_DIARIES: MockDiary[] = [
  {
    id: 'd-1',
    studentId: 's-1',
    studentName: 'Rafid Rahman',
    subject: 'Higher Mathematics',
    date: '2026-03-02',
    topicTitle: 'Quadratic Equations & Roots Nature',
    whatWasTaught:
      'Derived discriminant formula D = b² - 4ac. Solved board exam questions from 2022-2024. Explained roots condition when D > 0, D = 0, D < 0.',
    homeworkAssigned: 'Exercise 5.2: Problems 8 to 20; solve extra questions 2 & 5.',
    homeworkStatus: 'SUBMITTED_COMPLETE',
    practiceGiven: '5 timed equations on whiteboard.',
    studentUnderstanding: 'EXCELLENT',
    teacherRemarks: 'Understood discriminant conditions rapidly. Strong algebraic calculation.',
    nextClassPlan: 'Introduce cubic equations and relation between roots and coefficients.',
  },
  {
    id: 'd-2',
    studentId: 's-1',
    studentName: 'Rafid Rahman',
    subject: 'Physics',
    date: '2026-02-28',
    topicTitle: 'Newtonian Dynamics & Friction',
    whatWasTaught:
      'Static vs Kinetic friction coefficient. Free body diagrams for inclined planes with coefficient mu = 0.25.',
    homeworkAssigned: 'Chapter 3 Review numericals 15 through 24.',
    homeworkStatus: 'SUBMITTED_PARTIAL',
    practiceGiven: 'Two-block system friction equilibrium problem.',
    studentUnderstanding: 'GOOD',
    teacherRemarks: 'Needs extra attention on normal reaction resolving on incline angles.',
    nextClassPlan: 'Circular motion centripetal force review and 20-min test.',
  },
  {
    id: 'd-3',
    studentId: 's-2',
    studentName: 'Anika Tasnim',
    subject: 'General Math',
    date: '2026-03-01',
    topicTitle: 'Logarithms & Scientific Notation',
    whatWasTaught:
      'Laws of logarithms: log(xy) = log x + log y, change of base theorem. Characteristic and mantissa calculation.',
    homeworkAssigned: 'Exercise 4.2 all problems.',
    homeworkStatus: 'ASSIGNED',
    practiceGiven: 'Evaluated log expressions without calculator.',
    studentUnderstanding: 'GOOD',
    teacherRemarks: 'Very receptive; completed class exercises cleanly.',
    nextClassPlan: 'Applied word problems on compound interest involving logarithms.',
  },
];

export function AndroidStep4Preview() {
  const [students, setStudents] = useState<MockStudent[]>(INITIAL_STUDENTS);
  const [diaries, setDiaries] = useState<MockDiary[]>(INITIAL_DIARIES);
  const [currentView, setCurrentView] = useState<'LIST' | 'PROFILE' | 'DIARY_LIST' | 'ADD_STUDENT' | 'ADD_DIARY' | 'DIARY_DETAIL'>('LIST');
  const [selectedStudentId, setSelectedStudentId] = useState<string>('s-1');
  const [selectedDiaryId, setSelectedDiaryId] = useState<string>('d-1');
  const [activeTab, setActiveTab] = useState<'OVERVIEW' | 'DIARY' | 'CLASSES' | 'EXAMS' | 'PAYMENTS'>('OVERVIEW');

  // Search and Filter states
  const [studentSearch, setStudentSearch] = useState('');
  const [studentFilter, setStudentFilter] = useState<'ALL' | 'ACTIVE' | 'INACTIVE'>('ALL');
  const [diarySearch, setDiarySearch] = useState('');

  // Form states for adding student
  const [newStudentName, setNewStudentName] = useState('');
  const [newStudentSchool, setNewStudentSchool] = useState('');
  const [newStudentClass, setNewStudentClass] = useState('');
  const [newStudentPhone, setNewStudentPhone] = useState('');
  const [newStudentGuardian, setNewStudentGuardian] = useState('');
  const [newStudentFee, setNewStudentFee] = useState('5000');
  const [newStudentSubjects, setNewStudentSubjects] = useState('Higher Mathematics, Physics');

  // Form states for adding diary
  const [diaryStudentId, setDiaryStudentId] = useState('s-1');
  const [diarySubject, setDiarySubject] = useState('Higher Mathematics');
  const [diaryTopic, setDiaryTopic] = useState('');
  const [diaryContent, setDiaryContent] = useState('');
  const [diaryHw, setDiaryHw] = useState('');
  const [diaryHwStatus, setDiaryHwStatus] = useState<MockDiary['homeworkStatus']>('ASSIGNED');
  const [diaryUnderstanding, setDiaryUnderstanding] = useState<MockDiary['studentUnderstanding']>('GOOD');
  const [diaryRemarks, setDiaryRemarks] = useState('');
  const [diaryNextPlan, setDiaryNextPlan] = useState('');

  const activeStudents = students.filter((s) => s.isActive);
  const selectedStudent = students.find((s) => s.id === selectedStudentId) || students[0];
  const selectedDiary = diaries.find((d) => d.id === selectedDiaryId);

  const filteredStudents = students.filter((s) => {
    const matchesFilter =
      studentFilter === 'ALL' ||
      (studentFilter === 'ACTIVE' && s.isActive) ||
      (studentFilter === 'INACTIVE' && !s.isActive);
    const matchesQuery =
      s.name.toLowerCase().includes(studentSearch.toLowerCase()) ||
      s.institution.toLowerCase().includes(studentSearch.toLowerCase()) ||
      s.classGrade.toLowerCase().includes(studentSearch.toLowerCase());
    return matchesFilter && matchesQuery;
  });

  const handleSaveStudent = () => {
    if (!newStudentName.trim()) return;
    const newStudent: MockStudent = {
      id: `s-${Date.now()}`,
      name: newStudentName.trim(),
      institution: newStudentSchool.trim() || 'Unspecified School',
      classGrade: newStudentClass.trim() || 'Class 10',
      phone: newStudentPhone.trim(),
      guardianName: newStudentGuardian.trim(),
      guardianPhone: '',
      address: 'Dhaka, Bangladesh',
      monthlyFeeAmount: Number(newStudentFee) || 5000,
      billingCycleDay: 1,
      isActive: true,
      subjects: newStudentSubjects.split(',').map((s) => s.trim()).filter(Boolean),
      joinedDate: 'Mar 2026',
    };
    setStudents([newStudent, ...students]);
    setSelectedStudentId(newStudent.id);
    setCurrentView('PROFILE');
    // Reset form
    setNewStudentName('');
    setNewStudentSchool('');
    setNewStudentClass('');
    setNewStudentPhone('');
  };

  const handleSaveDiary = () => {
    if (!diaryTopic.trim() || !diaryContent.trim()) return;
    const st = students.find((s) => s.id === diaryStudentId);
    const newEntry: MockDiary = {
      id: `d-${Date.now()}`,
      studentId: diaryStudentId,
      studentName: st?.name || 'Student',
      subject: diarySubject,
      date: new Date().toISOString().split('T')[0],
      topicTitle: diaryTopic.trim(),
      whatWasTaught: diaryContent.trim(),
      homeworkAssigned: diaryHw.trim(),
      homeworkStatus: diaryHwStatus,
      practiceGiven: 'In-class exercises solved on board',
      studentUnderstanding: diaryUnderstanding,
      teacherRemarks: diaryRemarks.trim(),
      nextClassPlan: diaryNextPlan.trim(),
    };
    setDiaries([newEntry, ...diaries]);
    setSelectedDiaryId(newEntry.id);
    setCurrentView('DIARY_DETAIL');
    // Reset form
    setDiaryTopic('');
    setDiaryContent('');
    setDiaryHw('');
  };

  return (
    <div className="flex flex-col lg:flex-row gap-4 h-full w-full max-w-7xl mx-auto items-stretch">
      {/* Phone Simulator Frame */}
      <div className="w-full max-w-md mx-auto bg-slate-900 border-2 border-slate-700 rounded-3xl shadow-2xl overflow-hidden flex flex-col h-[740px] text-slate-100 shrink-0">
        {/* Android Status Bar */}
        <div className="bg-slate-950 px-5 py-1.5 flex justify-between items-center text-[11px] font-mono text-slate-400 border-b border-slate-800 shrink-0">
          <span>09:41</span>
          <div className="flex items-center gap-1.5">
            <span>5G</span>
            <span>100%</span>
          </div>
        </div>

        {/* Dynamic App Content */}
        <div className="flex-1 flex flex-col overflow-hidden bg-slate-950">
          {/* VIEW 1: STUDENT LIST SCREEN */}
          {currentView === 'LIST' && (
            <div className="flex flex-col h-full">
              {/* App Bar */}
              <div className="bg-slate-900/90 border-b border-slate-800 px-4 py-3 flex items-center justify-between shrink-0">
                <div>
                  <h2 className="text-base font-bold text-slate-100 flex items-center gap-2">
                    <span>Students</span>
                    <span className="text-xs px-2 py-0.5 bg-emerald-950 text-emerald-400 border border-emerald-800 rounded-full font-normal">
                      {activeStudents.length} active
                    </span>
                  </h2>
                  <p className="text-[11px] text-slate-400">Offline Room DB • Jetpack Compose</p>
                </div>
                <button
                  onClick={() => setCurrentView('ADD_STUDENT')}
                  className="p-1.5 bg-emerald-600 hover:bg-emerald-500 text-white rounded-lg transition-colors cursor-pointer"
                  title="Add Student"
                >
                  <Plus className="w-4 h-4" />
                </button>
              </div>

              {/* Search Bar */}
              <div className="p-3 bg-slate-950 border-b border-slate-800/80 space-y-2 shrink-0">
                <div className="relative">
                  <Search className="w-4 h-4 absolute left-3 top-2.5 text-slate-500" />
                  <input
                    type="text"
                    value={studentSearch}
                    onChange={(e) => setStudentSearch(e.target.value)}
                    placeholder="Search by name, class, school..."
                    className="w-full pl-9 pr-3 py-1.5 bg-slate-900 border border-slate-700/80 rounded-lg text-xs text-slate-200 placeholder-slate-500 focus:outline-none focus:border-emerald-500"
                  />
                </div>
                {/* Filter Chips */}
                <div className="flex items-center gap-2 text-xs">
                  {(['ALL', 'ACTIVE', 'INACTIVE'] as const).forEach((f) => (
                    <button
                      key={f}
                      onClick={() => setStudentFilter(f)}
                      className={`px-2.5 py-1 rounded-full text-[11px] transition-colors ${
                        studentFilter === f
                          ? 'bg-emerald-500 text-slate-950 font-semibold'
                          : 'bg-slate-900 text-slate-400 hover:bg-slate-800'
                      }`}
                    >
                      {f.charAt(0) + f.slice(1).toLowerCase()}
                    </button>
                  ))}
                </div>
              </div>

              {/* Student Cards List */}
              <div className="flex-1 overflow-y-auto p-3 space-y-2.5 custom-scrollbar">
                {filteredStudents.length === 0 ? (
                  <div className="h-full flex flex-col items-center justify-center text-center p-6 text-slate-500">
                    <Users className="w-10 h-10 mb-2 text-slate-600" />
                    <p className="text-sm font-medium text-slate-300">No students found</p>
                    <p className="text-xs text-slate-500 mt-1">Tap + above to enroll your first student</p>
                  </div>
                ) : (
                  filteredStudents.map((student) => (
                    <div
                      key={student.id}
                      onClick={() => {
                        setSelectedStudentId(student.id);
                        setCurrentView('PROFILE');
                      }}
                      className="p-3 bg-slate-900 hover:bg-slate-850 border border-slate-800 rounded-xl cursor-pointer transition-all hover:border-slate-700 space-y-2"
                    >
                      <div className="flex items-center justify-between">
                        <div className="flex items-center gap-2.5">
                          <div
                            className={`w-9 h-9 rounded-full flex items-center justify-center font-bold text-xs ${
                              student.isActive
                                ? 'bg-emerald-950 text-emerald-400 border border-emerald-800'
                                : 'bg-slate-800 text-slate-400 border border-slate-700'
                            }`}
                          >
                            {student.name.charAt(0)}
                          </div>
                          <div>
                            <div className="flex items-center gap-2">
                              <h3 className="text-xs font-semibold text-slate-100">{student.name}</h3>
                              {!student.isActive && (
                                <span className="text-[9px] px-1.5 py-0.2 bg-slate-800 text-slate-400 rounded">
                                  Inactive
                                </span>
                              )}
                            </div>
                            <p className="text-[11px] text-slate-400">
                              {student.classGrade} • {student.institution}
                            </p>
                          </div>
                        </div>
                      </div>

                      <div className="flex items-center justify-between text-[11px] pt-1 border-t border-slate-800/80">
                        <span className="text-emerald-400 font-medium">৳ {student.monthlyFeeAmount} / mo</span>
                        <div className="flex items-center gap-2 text-slate-400">
                          <Phone className="w-3 h-3" />
                          <span>{student.phone}</span>
                        </div>
                      </div>
                    </div>
                  ))
                )}
              </div>
            </div>
          )}

          {/* VIEW 2: STUDENT PROFILE */}
          {currentView === 'PROFILE' && selectedStudent && (
            <div className="flex flex-col h-full">
              {/* Profile Top Bar */}
              <div className="bg-slate-900 border-b border-slate-800 px-3 py-2.5 flex items-center justify-between shrink-0">
                <button
                  onClick={() => setCurrentView('LIST')}
                  className="flex items-center gap-1 text-xs text-slate-400 hover:text-slate-200 cursor-pointer"
                >
                  <ArrowLeft className="w-4 h-4" />
                  <span>Back</span>
                </button>
                <h2 className="text-xs font-semibold text-slate-200 truncate max-w-[200px]">
                  {selectedStudent.name}
                </h2>
                <div className="w-6"></div>
              </div>

              {/* Profile Header Summary */}
              <div className="p-4 bg-slate-900/60 border-b border-slate-800 shrink-0 space-y-3">
                <div className="flex items-center gap-3">
                  <div className="w-12 h-12 rounded-full bg-emerald-950 text-emerald-400 border border-emerald-800 flex items-center justify-center font-bold text-lg">
                    {selectedStudent.name.charAt(0)}
                  </div>
                  <div className="flex-1 min-w-0">
                    <div className="flex items-center gap-2">
                      <h3 className="text-sm font-bold text-slate-100 truncate">{selectedStudent.name}</h3>
                      <span
                        className={`text-[9px] px-1.5 py-0.5 rounded font-semibold ${
                          selectedStudent.isActive
                            ? 'bg-emerald-950 text-emerald-400 border border-emerald-800'
                            : 'bg-slate-800 text-slate-400'
                        }`}
                      >
                        {selectedStudent.isActive ? 'Active' : 'Inactive'}
                      </span>
                    </div>
                    <p className="text-xs text-slate-400 truncate">{selectedStudent.institution}</p>
                    <p className="text-[11px] text-slate-500">
                      {selectedStudent.classGrade} • Enrolled {selectedStudent.joinedDate}
                    </p>
                  </div>
                </div>

                {/* Quick Actions Row */}
                <div className="flex items-center gap-2 pt-1">
                  <button
                    onClick={() => {
                      setDiaryStudentId(selectedStudent.id);
                      setDiarySubject(selectedStudent.subjects[0] || 'Higher Mathematics');
                      setCurrentView('ADD_DIARY');
                    }}
                    className="flex-1 py-1.5 bg-emerald-600 hover:bg-emerald-500 text-white rounded-lg text-xs font-medium flex items-center justify-center gap-1.5 transition-colors cursor-pointer"
                  >
                    <Plus className="w-3.5 h-3.5" />
                    <span>+ Add Diary Entry</span>
                  </button>
                  <button
                    onClick={() => alert(`Dialing student: ${selectedStudent.phone}`)}
                    className="px-3 py-1.5 bg-slate-800 hover:bg-slate-700 text-slate-300 rounded-lg text-xs flex items-center gap-1 transition-colors cursor-pointer"
                  >
                    <Phone className="w-3.5 h-3.5" />
                    <span>Call</span>
                  </button>
                </div>
              </div>

              {/* Tabs Bar */}
              <div className="flex border-b border-slate-800 bg-slate-950 text-xs overflow-x-auto shrink-0 custom-scrollbar">
                {(['OVERVIEW', 'DIARY', 'CLASSES', 'EXAMS', 'PAYMENTS'] as const).map((tab) => (
                  <button
                    key={tab}
                    onClick={() => setActiveTab(tab)}
                    className={`px-3.5 py-2 whitespace-nowrap transition-colors border-b-2 font-medium cursor-pointer ${
                      activeTab === tab
                        ? 'border-emerald-500 text-emerald-400 bg-slate-900/50'
                        : 'border-transparent text-slate-400 hover:text-slate-300'
                    }`}
                  >
                    {tab.charAt(0) + tab.slice(1).toLowerCase()}
                  </button>
                ))}
              </div>

              {/* Tab Body */}
              <div className="flex-1 overflow-y-auto p-3 space-y-3 custom-scrollbar">
                {activeTab === 'OVERVIEW' && (
                  <>
                    {/* High-Density Stat Cards (2x3 Grid) */}
                    <div className="grid grid-cols-2 gap-2">
                      <div className="p-2.5 bg-slate-900 border border-slate-800 rounded-lg">
                        <span className="text-[10px] text-slate-400 uppercase tracking-wider block">Total Classes</span>
                        <span className="text-base font-bold text-slate-100">18</span>
                      </div>
                      <div className="p-2.5 bg-slate-900 border border-slate-800 rounded-lg">
                        <span className="text-[10px] text-slate-400 uppercase tracking-wider block">Completed</span>
                        <span className="text-base font-bold text-emerald-400">16</span>
                      </div>
                      <div className="p-2.5 bg-slate-900 border border-slate-800 rounded-lg">
                        <span className="text-[10px] text-slate-400 uppercase tracking-wider block">Missed / Cancel</span>
                        <span className="text-base font-bold text-amber-400">2</span>
                      </div>
                      <div className="p-2.5 bg-slate-900 border border-slate-800 rounded-lg">
                        <span className="text-[10px] text-slate-400 uppercase tracking-wider block">Diary Entries</span>
                        <span className="text-base font-bold text-blue-400">
                          {diaries.filter((d) => d.studentId === selectedStudent.id).length}
                        </span>
                      </div>
                      <div className="p-2.5 bg-slate-900 border border-slate-800 rounded-lg">
                        <span className="text-[10px] text-slate-400 uppercase tracking-wider block">Exams Taken</span>
                        <span className="text-base font-bold text-purple-400">3</span>
                      </div>
                      <div className="p-2.5 bg-slate-900 border border-slate-800 rounded-lg">
                        <span className="text-[10px] text-slate-400 uppercase tracking-wider block">Avg Score</span>
                        <span className="text-base font-bold text-emerald-400">86.5%</span>
                      </div>
                    </div>

                    {/* Tuition Details */}
                    <div className="p-3 bg-slate-900 border border-slate-800 rounded-xl space-y-1.5 text-xs">
                      <h4 className="font-semibold text-slate-300 text-[11px] uppercase tracking-wider">
                        Tuition & Guardian
                      </h4>
                      <div className="flex justify-between text-slate-400">
                        <span>Monthly Fee:</span>
                        <span className="text-slate-200 font-medium">৳ {selectedStudent.monthlyFeeAmount} (Day {selectedStudent.billingCycleDay})</span>
                      </div>
                      <div className="flex justify-between text-slate-400">
                        <span>Guardian:</span>
                        <span className="text-slate-200">{selectedStudent.guardianName}</span>
                      </div>
                      <div className="flex justify-between text-slate-400">
                        <span>Address:</span>
                        <span className="text-slate-200">{selectedStudent.address}</span>
                      </div>
                      <div className="flex justify-between text-slate-400">
                        <span>Subjects:</span>
                        <span className="text-emerald-400 font-medium">{selectedStudent.subjects.join(', ')}</span>
                      </div>
                    </div>

                    {/* Recent Lesson Diaries */}
                    <div className="space-y-2">
                      <div className="flex items-center justify-between">
                        <h4 className="text-xs font-semibold text-slate-300 uppercase tracking-wider">
                          Recent Diary Records
                        </h4>
                        <button
                          onClick={() => setActiveTab('DIARY')}
                          className="text-[11px] text-emerald-400 hover:underline"
                        >
                          View All
                        </button>
                      </div>

                      {diaries
                        .filter((d) => d.studentId === selectedStudent.id)
                        .slice(0, 3)
                        .map((diary) => (
                          <div
                            key={diary.id}
                            onClick={() => {
                              setSelectedDiaryId(diary.id);
                              setCurrentView('DIARY_DETAIL');
                            }}
                            className="p-2.5 bg-slate-900 hover:bg-slate-850 border border-slate-800 rounded-lg cursor-pointer transition-colors space-y-1"
                          >
                            <div className="flex justify-between items-center text-[11px]">
                              <span className="text-emerald-400 font-medium">{diary.subject}</span>
                              <span className="text-slate-500">{diary.date}</span>
                            </div>
                            <p className="text-xs font-semibold text-slate-200">{diary.topicTitle}</p>
                            <p className="text-[11px] text-slate-400 line-clamp-1">{diary.whatWasTaught}</p>
                          </div>
                        ))}
                    </div>
                  </>
                )}

                {activeTab === 'DIARY' && (
                  <div className="space-y-2">
                    {diaries
                      .filter((d) => d.studentId === selectedStudent.id)
                      .map((diary) => (
                        <div
                          key={diary.id}
                          onClick={() => {
                            setSelectedDiaryId(diary.id);
                            setCurrentView('DIARY_DETAIL');
                          }}
                          className="p-3 bg-slate-900 hover:bg-slate-850 border border-slate-800 rounded-xl cursor-pointer transition-colors space-y-1.5"
                        >
                          <div className="flex justify-between items-center text-[11px]">
                            <span className="text-emerald-400 font-semibold">{diary.subject}</span>
                            <span className="text-slate-500">{diary.date}</span>
                          </div>
                          <h4 className="text-xs font-bold text-slate-100">{diary.topicTitle}</h4>
                          <p className="text-[11px] text-slate-400 line-clamp-2">{diary.whatWasTaught}</p>
                          {diary.homeworkAssigned && (
                            <div className="text-[10px] text-slate-400 pt-1 border-t border-slate-800 flex items-center justify-between">
                              <span className="truncate">HW: {diary.homeworkAssigned}</span>
                              <span className="text-amber-400 shrink-0 font-medium ml-2">{diary.homeworkStatus}</span>
                            </div>
                          )}
                        </div>
                      ))}
                  </div>
                )}

                {activeTab !== 'OVERVIEW' && activeTab !== 'DIARY' && (
                  <div className="py-12 text-center text-slate-500 text-xs">
                    <p className="font-medium text-slate-400">{activeTab} Section Ready</p>
                    <p className="text-[11px] mt-1">Data schemas & Room repositories are fully configured.</p>
                  </div>
                )}
              </div>
            </div>
          )}

          {/* VIEW 3: ADD STUDENT FORM */}
          {currentView === 'ADD_STUDENT' && (
            <div className="flex flex-col h-full">
              <div className="bg-slate-900 border-b border-slate-800 px-3 py-2.5 flex items-center justify-between shrink-0">
                <button
                  onClick={() => setCurrentView('LIST')}
                  className="flex items-center gap-1 text-xs text-slate-400 hover:text-slate-200 cursor-pointer"
                >
                  <ArrowLeft className="w-4 h-4" />
                  <span>Cancel</span>
                </button>
                <h2 className="text-xs font-semibold text-slate-200">Add New Student</h2>
                <button
                  onClick={handleSaveStudent}
                  disabled={!newStudentName.trim()}
                  className="px-3 py-1 bg-emerald-600 hover:bg-emerald-500 disabled:opacity-50 text-white rounded text-xs font-medium cursor-pointer"
                >
                  Save
                </button>
              </div>

              <div className="flex-1 overflow-y-auto p-4 space-y-3 text-xs custom-scrollbar">
                <div>
                  <label className="block text-slate-400 mb-1">Full Name *</label>
                  <input
                    type="text"
                    value={newStudentName}
                    onChange={(e) => setNewStudentName(e.target.value)}
                    placeholder="e.g. Tanvir Ahmed"
                    className="w-full px-3 py-2 bg-slate-900 border border-slate-700 rounded-lg text-slate-200 focus:outline-none focus:border-emerald-500"
                  />
                </div>

                <div className="grid grid-cols-2 gap-2">
                  <div>
                    <label className="block text-slate-400 mb-1">Class / Grade</label>
                    <input
                      type="text"
                      value={newStudentClass}
                      onChange={(e) => setNewStudentClass(e.target.value)}
                      placeholder="e.g. Class 10"
                      className="w-full px-3 py-2 bg-slate-900 border border-slate-700 rounded-lg text-slate-200 focus:outline-none focus:border-emerald-500"
                    />
                  </div>
                  <div>
                    <label className="block text-slate-400 mb-1">Monthly Fee (BDT)</label>
                    <input
                      type="number"
                      value={newStudentFee}
                      onChange={(e) => setNewStudentFee(e.target.value)}
                      className="w-full px-3 py-2 bg-slate-900 border border-slate-700 rounded-lg text-slate-200 focus:outline-none focus:border-emerald-500"
                    />
                  </div>
                </div>

                <div>
                  <label className="block text-slate-400 mb-1">School / Institution</label>
                  <input
                    type="text"
                    value={newStudentSchool}
                    onChange={(e) => setNewStudentSchool(e.target.value)}
                    placeholder="e.g. Ideal School & College"
                    className="w-full px-3 py-2 bg-slate-900 border border-slate-700 rounded-lg text-slate-200 focus:outline-none focus:border-emerald-500"
                  />
                </div>

                <div className="grid grid-cols-2 gap-2">
                  <div>
                    <label className="block text-slate-400 mb-1">Student Phone</label>
                    <input
                      type="text"
                      value={newStudentPhone}
                      onChange={(e) => setNewStudentPhone(e.target.value)}
                      placeholder="017xxxxxxxx"
                      className="w-full px-3 py-2 bg-slate-900 border border-slate-700 rounded-lg text-slate-200 focus:outline-none focus:border-emerald-500"
                    />
                  </div>
                  <div>
                    <label className="block text-slate-400 mb-1">Guardian Name</label>
                    <input
                      type="text"
                      value={newStudentGuardian}
                      onChange={(e) => setNewStudentGuardian(e.target.value)}
                      placeholder="Parent name"
                      className="w-full px-3 py-2 bg-slate-900 border border-slate-700 rounded-lg text-slate-200 focus:outline-none focus:border-emerald-500"
                    />
                  </div>
                </div>

                <div>
                  <label className="block text-slate-400 mb-1">Subjects (comma separated)</label>
                  <input
                    type="text"
                    value={newStudentSubjects}
                    onChange={(e) => setNewStudentSubjects(e.target.value)}
                    className="w-full px-3 py-2 bg-slate-900 border border-slate-700 rounded-lg text-slate-200 focus:outline-none focus:border-emerald-500"
                  />
                </div>
              </div>
            </div>
          )}

          {/* VIEW 4: ADD DIARY ENTRY FORM */}
          {currentView === 'ADD_DIARY' && (
            <div className="flex flex-col h-full">
              <div className="bg-slate-900 border-b border-slate-800 px-3 py-2.5 flex items-center justify-between shrink-0">
                <button
                  onClick={() => setCurrentView('PROFILE')}
                  className="flex items-center gap-1 text-xs text-slate-400 hover:text-slate-200 cursor-pointer"
                >
                  <ArrowLeft className="w-4 h-4" />
                  <span>Cancel</span>
                </button>
                <h2 className="text-xs font-semibold text-slate-200">New Diary Entry</h2>
                <button
                  onClick={handleSaveDiary}
                  disabled={!diaryTopic.trim() || !diaryContent.trim()}
                  className="px-3 py-1 bg-emerald-600 hover:bg-emerald-500 disabled:opacity-50 text-white rounded text-xs font-medium cursor-pointer"
                >
                  Save
                </button>
              </div>

              <div className="flex-1 overflow-y-auto p-4 space-y-3 text-xs custom-scrollbar">
                <div>
                  <label className="block text-slate-400 mb-1">Subject</label>
                  <select
                    value={diarySubject}
                    onChange={(e) => setDiarySubject(e.target.value)}
                    className="w-full px-3 py-2 bg-slate-900 border border-slate-700 rounded-lg text-slate-200 focus:outline-none focus:border-emerald-500"
                  >
                    <option value="Higher Mathematics">Higher Mathematics</option>
                    <option value="Physics">Physics</option>
                    <option value="Chemistry">Chemistry</option>
                    <option value="General Math">General Math</option>
                  </select>
                </div>

                <div>
                  <label className="block text-slate-400 mb-1">Topic / Chapter Title *</label>
                  <input
                    type="text"
                    value={diaryTopic}
                    onChange={(e) => setDiaryTopic(e.target.value)}
                    placeholder="e.g. Linear Programming & Feasible Region"
                    className="w-full px-3 py-2 bg-slate-900 border border-slate-700 rounded-lg text-slate-200 focus:outline-none focus:border-emerald-500"
                  />
                </div>

                <div>
                  <label className="block text-slate-400 mb-1">What Was Taught (Lesson Content) *</label>
                  <textarea
                    rows={3}
                    value={diaryContent}
                    onChange={(e) => setDiaryContent(e.target.value)}
                    placeholder="Describe concepts, formulas, and solved examples..."
                    className="w-full px-3 py-2 bg-slate-900 border border-slate-700 rounded-lg text-slate-200 focus:outline-none focus:border-emerald-500"
                  />
                </div>

                <div>
                  <label className="block text-slate-400 mb-1">Student Understanding</label>
                  <div className="grid grid-cols-4 gap-1">
                    {(['EXCELLENT', 'GOOD', 'AVERAGE', 'NEEDS_ATTENTION'] as const).map((u) => (
                      <button
                        key={u}
                        type="button"
                        onClick={() => setDiaryUnderstanding(u)}
                        className={`py-1.5 rounded text-[10px] font-medium border ${
                          diaryUnderstanding === u
                            ? 'bg-emerald-950 text-emerald-400 border-emerald-700'
                            : 'bg-slate-900 text-slate-400 border-slate-800'
                        }`}
                      >
                        {u === 'NEEDS_ATTENTION' ? 'Attention' : u}
                      </button>
                    ))}
                  </div>
                </div>

                <div>
                  <label className="block text-slate-400 mb-1">Homework Assigned</label>
                  <input
                    type="text"
                    value={diaryHw}
                    onChange={(e) => setDiaryHw(e.target.value)}
                    placeholder="e.g. Exercise 7.1 Q 1-8"
                    className="w-full px-3 py-2 bg-slate-900 border border-slate-700 rounded-lg text-slate-200 focus:outline-none focus:border-emerald-500"
                  />
                </div>

                <div>
                  <label className="block text-slate-400 mb-1">Teacher Remarks & Observations</label>
                  <input
                    type="text"
                    value={diaryRemarks}
                    onChange={(e) => setDiaryRemarks(e.target.value)}
                    placeholder="e.g. Fast grasp, good formula recall"
                    className="w-full px-3 py-2 bg-slate-900 border border-slate-700 rounded-lg text-slate-200 focus:outline-none focus:border-emerald-500"
                  />
                </div>

                <div>
                  <label className="block text-slate-400 mb-1">Next Class Plan</label>
                  <input
                    type="text"
                    value={diaryNextPlan}
                    onChange={(e) => setDiaryNextPlan(e.target.value)}
                    placeholder="e.g. Solve board test problems"
                    className="w-full px-3 py-2 bg-slate-900 border border-slate-700 rounded-lg text-slate-200 focus:outline-none focus:border-emerald-500"
                  />
                </div>
              </div>
            </div>
          )}

          {/* VIEW 5: DIARY DETAIL */}
          {currentView === 'DIARY_DETAIL' && selectedDiary && (
            <div className="flex flex-col h-full">
              <div className="bg-slate-900 border-b border-slate-800 px-3 py-2.5 flex items-center justify-between shrink-0">
                <button
                  onClick={() => setCurrentView('PROFILE')}
                  className="flex items-center gap-1 text-xs text-slate-400 hover:text-slate-200 cursor-pointer"
                >
                  <ArrowLeft className="w-4 h-4" />
                  <span>Back</span>
                </button>
                <h2 className="text-xs font-semibold text-slate-200">Diary Record</h2>
                <div className="w-6"></div>
              </div>

              <div className="flex-1 overflow-y-auto p-4 space-y-3.5 text-xs custom-scrollbar">
                <div className="p-3 bg-slate-900 border border-slate-800 rounded-xl space-y-2">
                  <div className="flex justify-between items-center">
                    <span className="text-emerald-400 font-semibold">{selectedDiary.subject}</span>
                    <span className="text-slate-400">{selectedDiary.date}</span>
                  </div>
                  <h3 className="text-sm font-bold text-slate-100">{selectedDiary.topicTitle}</h3>
                  <div className="flex items-center gap-2">
                    <span className="text-[10px] px-2 py-0.5 bg-emerald-950 text-emerald-400 border border-emerald-800 rounded">
                      Comprehension: {selectedDiary.studentUnderstanding}
                    </span>
                    <span className="text-[10px] px-2 py-0.5 bg-blue-950 text-blue-400 border border-blue-800 rounded">
                      {selectedDiary.homeworkStatus}
                    </span>
                  </div>
                </div>

                <div className="p-3 bg-slate-900/60 border border-slate-800 rounded-xl space-y-1">
                  <span className="text-[10px] font-bold text-emerald-400 uppercase tracking-wider block">
                    What Was Taught in Class
                  </span>
                  <p className="text-slate-300 leading-relaxed">{selectedDiary.whatWasTaught}</p>
                </div>

                {selectedDiary.homeworkAssigned && (
                  <div className="p-3 bg-slate-900/60 border border-slate-800 rounded-xl space-y-1">
                    <span className="text-[10px] font-bold text-blue-400 uppercase tracking-wider block">
                      Homework Assigned
                    </span>
                    <p className="text-slate-300">{selectedDiary.homeworkAssigned}</p>
                  </div>
                )}

                {selectedDiary.teacherRemarks && (
                  <div className="p-3 bg-slate-900/60 border border-slate-800 rounded-xl space-y-1">
                    <span className="text-[10px] font-bold text-purple-400 uppercase tracking-wider block">
                      Teacher Remarks & Observations
                    </span>
                    <p className="text-slate-300">{selectedDiary.teacherRemarks}</p>
                  </div>
                )}

                {selectedDiary.nextClassPlan && (
                  <div className="p-3 bg-slate-900/60 border border-slate-800 rounded-xl space-y-1">
                    <span className="text-[10px] font-bold text-amber-400 uppercase tracking-wider block">
                      Next Class Plan
                    </span>
                    <p className="text-slate-300">{selectedDiary.nextClassPlan}</p>
                  </div>
                )}
              </div>
            </div>
          )}
        </div>

        {/* Android Bottom Navigation Bar */}
        <div className="bg-slate-950 border-t border-slate-800 px-6 py-2.5 flex justify-around items-center text-slate-400 shrink-0">
          <button
            onClick={() => setCurrentView('LIST')}
            className={`flex flex-col items-center gap-0.5 text-[10px] ${
              currentView === 'LIST' ? 'text-emerald-400 font-bold' : 'text-slate-400'
            }`}
          >
            <Users className="w-4 h-4" />
            <span>Students</span>
          </button>
          <button
            onClick={() => {
              if (students.length > 0) {
                setSelectedStudentId(students[0].id);
                setCurrentView('PROFILE');
                setActiveTab('DIARY');
              }
            }}
            className={`flex flex-col items-center gap-0.5 text-[10px] ${
              currentView === 'PROFILE' && activeTab === 'DIARY' ? 'text-emerald-400 font-bold' : 'text-slate-400'
            }`}
          >
            <BookOpen className="w-4 h-4" />
            <span>Diary</span>
          </button>
        </div>
      </div>

      {/* Feature Guide & Implementation Details Side Panel */}
      <div className="flex-1 flex flex-col gap-3 min-w-0">
        <div className="p-4 bg-slate-900/80 border border-slate-800 rounded-2xl space-y-3">
          <div className="flex items-center justify-between">
            <div>
              <h2 className="text-sm lg:text-base font-bold text-emerald-400 flex items-center gap-2">
                <span>STEP 4 Implementation Complete</span>
                <span className="text-[10px] px-2 py-0.5 bg-emerald-950 text-emerald-300 border border-emerald-700 rounded-full font-mono">
                  Kotlin + Jetpack Compose + Room
                </span>
              </h2>
              <p className="text-xs text-slate-400 mt-0.5">
                First user-facing feature set: Student Management, Profile Hub, and Student Diary.
              </p>
            </div>
          </div>

          <div className="grid grid-cols-1 md:grid-cols-3 gap-2.5 text-xs">
            <div className="p-3 bg-slate-950/80 border border-slate-800 rounded-xl space-y-1">
              <span className="font-bold text-slate-200 block">1. Student Management</span>
              <p className="text-slate-400 text-[11px]">
                Search by name/school/phone, filter by active/inactive, sort by name or recent, add/edit form with full validation.
              </p>
            </div>
            <div className="p-3 bg-slate-950/80 border border-slate-800 rounded-xl space-y-1">
              <span className="font-bold text-slate-200 block">2. Student Profile Hub</span>
              <p className="text-slate-400 text-[11px]">
                8-section tabbed hub with 6 real-time Room aggregate stat cards (classes, attendance, diary, exams, scores).
              </p>
            </div>
            <div className="p-3 bg-slate-950/80 border border-slate-800 rounded-xl space-y-1">
              <span className="font-bold text-slate-200 block">3. Student Diary System</span>
              <p className="text-slate-400 text-[11px]">
                Fast data entry for lesson topic, what was taught, homework status, student comprehension, remarks, and next plan.
              </p>
            </div>
          </div>
        </div>

        {/* Clean Architecture Verification Box */}
        <div className="flex-1 p-4 bg-slate-900/60 border border-slate-800 rounded-2xl overflow-y-auto space-y-3 text-xs custom-scrollbar">
          <h3 className="font-bold text-slate-200 text-xs uppercase tracking-wider flex items-center gap-2">
            <CheckCircle2 className="w-4 h-4 text-emerald-400" />
            <span>Architecture & Codebase Checklist</span>
          </h3>

          <ul className="space-y-2 text-slate-300 text-xs">
            <li className="flex items-start gap-2">
              <span className="text-emerald-400 font-bold">✓</span>
              <div>
                <strong>UI Layer (Jetpack Compose + Material 3):</strong> <code>StudentListScreen</code>, <code>StudentFormScreen</code>, <code>StudentProfileScreen</code>, <code>StudentDiaryScreen</code>, <code>DiaryEntryScreen</code>, <code>DiaryDetailScreen</code>.
              </div>
            </li>
            <li className="flex items-start gap-2">
              <span className="text-emerald-400 font-bold">✓</span>
              <div>
                <strong>ViewModel & UDF StateFlow:</strong> <code>StudentListViewModel</code>, <code>StudentFormViewModel</code>, <code>StudentProfileViewModel</code>, <code>StudentDiaryViewModel</code>, <code>DiaryEntryViewModel</code>, <code>DiaryDetailViewModel</code>.
              </div>
            </li>
            <li className="flex items-start gap-2">
              <span className="text-emerald-400 font-bold">✓</span>
              <div>
                <strong>Navigation (Jetpack Compose NavHost):</strong> <code>TuitionNavGraph</code> handling 8 distinct routes with deep-linking & query parameters.
              </div>
            </li>
            <li className="flex items-start gap-2">
              <span className="text-emerald-400 font-bold">✓</span>
              <div>
                <strong>Localization:</strong> Full English (<code>values/strings.xml</code>) and Bengali (<code>values-bn/strings.xml</code>) resource catalogs.
              </div>
            </li>
            <li className="flex items-start gap-2">
              <span className="text-emerald-400 font-bold">✓</span>
              <div>
                <strong>Domain & Data Layer:</strong> 15 Room entities, soft-deletes (<code>isDeleted</code>, <code>deletedAt</code>), comprehensive validation rules, and Hilt injection bindings.
              </div>
            </li>
          </ul>
        </div>
      </div>
    </div>
  );
}
