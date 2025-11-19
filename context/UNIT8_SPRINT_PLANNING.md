# Unit 8: Sprint Planning Guide
## MicroHabit Coach - Complete Issue Breakdown

---

## 📋 Overview

**This document contains ALL 29 issues for the ENTIRE PROJECT (all 3 sprints).**

- **Milestone 1 (Sprint 1 - Unit 8)**: 10 issues - Foundation & Core Architecture
- **Milestone 2 (Sprint 2 - Unit 9)**: 9 issues - Core Features & API Integration  
- **Milestone 3 (Sprint 3 - Unit 10)**: 10 issues - Advanced Features & Polish

**Total: 29 issues covering the complete app from start to finish.**

Each issue includes:
- Title
- Description
- Assignee recommendation
- Dependencies
- Acceptance criteria

**Team Members:**
- **Devon** (Frontend-focused)
- **Dylan** (Backend-focused)
- **Hung** (Medium complexity tasks)

---

## ✅ **Will This Complete the Entire App?**

**YES!** These 29 issues cover:
- ✅ All required features from Unit 7 Product Spec
- ✅ All 6 screen archetypes (Login, Stream #1, Stream #2, Detail, Creation, Profile, Settings)
- ✅ All navigation flows
- ✅ API integration (Hacker News)
- ✅ Classification & FitScore systems
- ✅ Activity Recognition (auto-completion)
- ✅ Geofencing
- ✅ Notifications
- ✅ Complete database architecture
- ✅ Testing and polish

**This is your complete roadmap from Unit 8 through Unit 10.**

---

## 📅 Sprint Overview

### **Sprint 1 (Unit 8) - Foundation & Core Architecture**
**Issues: 1-10** | **Due: End of Unit 8** | **Focus: Foundation that everything else builds on**

### **Sprint 2 (Unit 9) - Core Features & API Integration**
**Issues: 11-19** | **Due: End of Unit 9** | **Focus: Core functionality that makes the app unique**

### **Sprint 3 (Unit 10) - Advanced Features & Polish**
**Issues: 20-29** | **Due: End of Unit 10** | **Focus: Mobile-native features and final polish**

---

## 📋 Quick Reference: Issues by Sprint

| Sprint | Issue # | Title | Assignee | Priority |
|--------|---------|-------|----------|----------|
| **Sprint 1** | 1 | Project Setup & Architecture | Dylan | Critical |
| **Sprint 1** | 2 | Room Database Setup | Dylan | Critical |
| **Sprint 1** | 3 | Repository Layer | Dylan | Critical |
| **Sprint 1** | 4 | Navigation Setup | Devon | High |
| **Sprint 1** | 5 | Login & Onboarding UI | Devon | High |
| **Sprint 1** | 6 | Today Screen UI (Stream #1) | Devon | High |
| **Sprint 1** | 7 | Add/Edit Habit UI (Creation Screen) | Devon | High |
| **Sprint 1** | 8 | ViewModels for Today & Add/Edit | Hung | High |
| **Sprint 1** | 9 | Basic Habit CRUD Operations | Hung | High |
| **Sprint 1** | 10 | Mark Habit Complete (Manual) | Hung | High |
| **Sprint 2** | 11 | Hacker News API Integration | Dylan | Critical |
| **Sprint 2** | 12 | Habit Classification System | Dylan | High |
| **Sprint 2** | 13 | FitScore Calculation System | Dylan | High |
| **Sprint 2** | 14 | Explore Screen UI (Stream #2) | Devon | High |
| **Sprint 2** | 15 | Explore ViewModel & Integration | Hung | High |
| **Sprint 2** | 16 | Pre-fill Habit from Explore | Hung | High |
| **Sprint 2** | 17 | Habit Detail Screen UI | Devon | Medium |
| **Sprint 2** | 18 | Habit Detail ViewModel & Data | Hung | Medium |
| **Sprint 2** | 19 | Basic Time-Based Notifications | Dylan | Medium |
| **Sprint 3** | 20 | Activity Recognition Service | Dylan | High |
| **Sprint 3** | 21 | Geofencing Service | Dylan | High |
| **Sprint 3** | 22 | Dynamic Notifications | Hung | Medium |
| **Sprint 3** | 23 | Profile/Stats Screen UI | Devon | Medium |
| **Sprint 3** | 24 | Profile/Stats ViewModel & Analytics | Hung | Medium |
| **Sprint 3** | 25 | Settings Screen | Devon | Medium |
| **Sprint 3** | 26 | Settings ViewModel & Logic | Hung | Medium |
| **Sprint 3** | 27 | User Preferences Storage | Hung | Low |
| **Sprint 3** | 28 | Testing & Bug Fixes | All | High |
| **Sprint 3** | 29 | Documentation & README Updates | Devon | Medium |

---

# 🚀 SPRINT 1 (Unit 8) - Foundation & Core Architecture

**Goal**: Set up project infrastructure, database, and basic UI structure  
**Due Date**: End of Unit 8  
**Focus**: Foundation that everything else builds on

**Issues in this Sprint: 1, 2, 3, 4, 5, 6, 7, 8, 9, 10**

---

### Issue 1: Project Setup & Architecture
**Assignee**: Dylan (Backend)  
**Priority**: Critical  
**Labels**: `setup`, `architecture`

**Description**:
Set up the Android Studio project with proper architecture and dependencies.

**Tasks**:
- [ ] Initialize Android Studio project (`com.microhabitcoach`)
- [ ] Add all required dependencies to `build.gradle`:
  - Room database
  - Retrofit + OkHttp
  - Kotlin Coroutines
  - Navigation Component
  - WorkManager
  - Location Services
  - Activity Recognition
  - Material Design Components
- [ ] Set up MVVM architecture structure (packages: `data`, `domain`, `ui`)
- [ ] Create base classes (BaseFragment, BaseViewModel)
- [ ] Configure ProGuard rules if needed

**Acceptance Criteria**:
- Project builds successfully
- All dependencies are added and synced
- Package structure is organized
- Team can clone and build the project

**Dependencies**: None (foundation)

---

### Issue 2: Room Database Setup
**Assignee**: Dylan (Backend)  
**Priority**: Critical  
**Labels**: `database`, `backend`

**Description**:
Implement Room database with all required entities, DAOs, and database class.

**Tasks**:
- [ ] Create `Habit` entity with fields:
  - id, name, category, type (TIME/MOTION/LOCATION)
  - targetDuration, location, reminderTimes
  - streakCount, createdAt
- [ ] Create `Completion` entity with fields:
  - id, habitId, completedAt, autoCompleted
- [ ] Create `ApiSuggestion` entity for caching:
  - id, title, content, category, fitScore, cachedAt
- [ ] Create `UserPreferences` entity (or use SharedPreferences)
- [ ] Create DAOs for each entity with CRUD operations
- [ ] Create `AppDatabase` class with migrations
- [ ] Write unit tests for database operations

**Acceptance Criteria**:
- All entities are defined
- DAOs have insert, update, delete, query methods
- Database can be created and accessed
- Unit tests pass

**Dependencies**: Issue 1

---

### Issue 3: Repository Layer
**Assignee**: Dylan (Backend)  
**Priority**: Critical  
**Labels**: `repository`, `backend`

**Description**:
Create repository classes that abstract data sources (Room + API).

**Tasks**:
- [ ] Create `HabitRepository` class
  - Methods: getAllHabits(), getHabitById(), insertHabit(), updateHabit(), deleteHabit()
  - Methods: completeHabit(), getCompletionsForHabit()
- [ ] Create `ApiRepository` class (stub for now, implement API calls in Sprint 2)
  - Methods: fetchSuggestions(), cacheSuggestions()
- [ ] Implement LiveData observables for reactive updates
- [ ] Handle error cases and edge cases

**Acceptance Criteria**:
- Repository pattern is implemented
- All CRUD operations work through repository
- LiveData updates UI reactively
- Error handling is in place

**Dependencies**: Issue 2

---

### Issue 4: Navigation Setup
**Assignee**: Devon (Frontend)  
**Priority**: High  
**Labels**: `navigation`, `frontend`

**Description**:
Set up Navigation Component with all screen destinations and navigation graph.

**Tasks**:
- [ ] Create `nav_graph.xml` with all destinations:
  - LoginFragment
  - OnboardingFragment
  - TodayFragment
  - ExploreFragment
  - AddEditHabitFragment
  - HabitDetailFragment
  - ProfileStatsFragment
  - SettingsFragment
- [ ] Define navigation actions between screens
- [ ] Set up Safe Args for passing data between fragments
- [ ] Configure bottom navigation bar (Today, Explore, Stats, Settings)
- [ ] Test navigation flows

**Acceptance Criteria**:
- All screens are in navigation graph
- Navigation between screens works
- Bottom navigation bar switches tabs correctly
- Safe Args pass data correctly

**Dependencies**: Issue 1

---

### Issue 5: Login & Onboarding UI
**Assignee**: Devon (Frontend)  
**Priority**: High  
**Labels**: `ui`, `frontend`, `onboarding`

**Description**:
Create login and onboarding screens with permission requests.

**Tasks**:
- [ ] Design and implement `activity_login.xml` layout
- [ ] Create `LoginFragment` with email/Google Sign-In (mock for now)
- [ ] Create `OnboardingFragment` with:
  - Welcome screens
  - Smart template selection (visual cards)
  - Permission request flow (Motion, Location, Notifications)
  - Progress indicators
- [ ] Implement permission request logic with explanations
- [ ] Add micro-animations for engagement
- [ ] Handle permission denial gracefully

**Acceptance Criteria**:
- Login screen is functional (can be mock)
- Onboarding flow guides user through setup
- Permissions are requested with clear explanations
- UI matches wireframes

**Dependencies**: Issue 4

---

### Issue 6: Today Screen UI (Stream #1)
**Assignee**: Devon (Frontend)  
**Priority**: High  
**Labels**: `ui`, `frontend`, `stream`

**Description**:
Create the Today screen that displays user's habits in a scrollable list.

**Tasks**:
- [ ] Design `fragment_today.xml` layout
- [ ] Create `HabitItemAdapter` for RecyclerView
- [ ] Create `HabitItemViewHolder` with habit card layout:
  - Habit name, icon, progress ring
  - Streak count, next reminder time
  - Completion toggle button
- [ ] Create `TodayFragment` with RecyclerView
- [ ] Add summary banner at top ("X of Y habits completed")
- [ ] Implement pull-to-refresh (optional)
- [ ] Add empty state (when no habits)
- [ ] Style with Material Design components

**Acceptance Criteria**:
- Today screen displays list of habits
- Each habit card shows all required information
- UI matches wireframes
- List is scrollable
- Empty state is handled

**Dependencies**: Issue 4, Issue 3

---

### Issue 7: Add/Edit Habit UI (Creation Screen)
**Assignee**: Devon (Frontend)  
**Priority**: High  
**Labels**: `ui`, `frontend`, `creation`

**Description**:
Create the Add/Edit Habit screen with dynamic form based on habit type.

**Tasks**:
- [ ] Design `fragment_add_edit_habit.xml` layout
- [ ] Create type selector (Time-based, Motion-based, Location-based)
- [ ] Implement dynamic form fields that show/hide based on type:
  - Time-based: name, reminder times, days of week
  - Motion-based: name, motion type, duration threshold
  - Location-based: name, location picker, geofence radius
- [ ] Create `AddEditHabitFragment` with form validation
- [ ] Implement pre-filling from Explore screen (Safe Args)
- [ ] Add real-time preview of how habit will appear
- [ ] Handle save/cancel actions

**Acceptance Criteria**:
- Form adapts to habit type
- Validation prevents invalid inputs
- Can create new habits
- Can edit existing habits
- Pre-filling from Explore works

**Dependencies**: Issue 4, Issue 3

---

### Issue 8: ViewModels for Today & Add/Edit
**Assignee**: Hung (Medium)  
**Priority**: High  
**Labels**: `viewmodel`, `mvvm`

**Description**:
Create ViewModels to manage UI state for Today and Add/Edit screens.

**Tasks**:
- [ ] Create `TodayViewModel`:
  - Observable LiveData for habits list
  - Methods: loadHabits(), completeHabit(habitId)
  - Handle loading/error states
- [ ] Create `AddEditHabitViewModel`:
  - Methods: saveHabit(), loadHabit(id), validateForm()
  - Handle form state
- [ ] Connect ViewModels to Repository
- [ ] Test ViewModel logic

**Acceptance Criteria**:
- ViewModels expose LiveData
- UI updates reactively when data changes
- Form validation works
- Error states are handled

**Dependencies**: Issue 3, Issue 6, Issue 7

---

### Issue 9: Basic Habit CRUD Operations
**Assignee**: Hung (Medium)  
**Priority**: High  
**Labels**: `crud`, `backend`

**Description**:
Implement basic Create, Read, Update, Delete operations for habits.

**Tasks**:
- [ ] Connect Add/Edit screen to repository (create habit)
- [ ] Connect Today screen to repository (read habits)
- [ ] Implement edit habit functionality
- [ ] Implement delete habit functionality
- [ ] Test all CRUD operations
- [ ] Handle edge cases (empty name, invalid data)

**Acceptance Criteria**:
- Can create new habits from Add screen
- Can view habits in Today screen
- Can edit existing habits
- Can delete habits
- All operations persist to database

**Dependencies**: Issue 8, Issue 6, Issue 7

---

### Issue 10: Mark Habit Complete (Manual)
**Assignee**: Hung (Medium)  
**Priority**: High  
**Labels**: `feature`, `completion`

**Description**:
Implement one-tap habit completion with visual feedback.

**Tasks**:
- [ ] Add completion button/toggle to habit card
- [ ] Implement `completeHabit()` in repository
- [ ] Create Completion entry in database
- [ ] Update streak count logic
- [ ] Add visual feedback (confetti animation, color transition)
- [ ] Update UI immediately after completion
- [ ] Handle same-day duplicate completions

**Acceptance Criteria**:
- One tap completes habit
- Visual feedback is satisfying
- Streak count updates correctly
- Completion is logged in database
- UI updates immediately

**Dependencies**: Issue 9

---

# 🚀 SPRINT 2 (Unit 9) - Core Features & API Integration

**Goal**: Implement Explore screen, API integration, classification, and habit detail  
**Due Date**: End of Unit 9  
**Focus**: Core functionality that makes the app unique

**Issues in this Sprint: 11, 12, 13, 14, 15, 16, 17, 18, 19**

---

### Issue 11: Hacker News API Integration
**Assignee**: Dylan (Backend)  
**Priority**: Critical  
**Labels**: `api`, `backend`, `explore`

**Description**:
Integrate Hacker News API to fetch and parse posts for the Explore screen.

**Tasks**:
- [ ] Create Retrofit interface for Hacker News API
- [ ] Create data models (HackerNewsItem, Post)
- [ ] Implement API service:
  - Fetch top stories
  - Fetch individual item details
  - Filter by keywords (workout, fitness, health, exercise, meditation, productivity)
- [ ] Add error handling and retry logic
- [ ] Implement caching strategy (Room database)
- [ ] Add loading states

**Acceptance Criteria**:
- Can fetch posts from Hacker News API
- Posts are filtered by relevant keywords
- API errors are handled gracefully
- Results are cached locally

**Dependencies**: Issue 3

---

### Issue 12: Habit Classification System
**Assignee**: Dylan (Backend)  
**Priority**: High  
**Labels**: `classification`, `backend`, `algorithm`

**Description**:
Implement local keyword-based classification system (no AI/ML).

**Tasks**:
- [ ] Create `HabitClassifier` class
- [ ] Implement keyword matching logic:
  - Fitness keywords: workout, exercise, gym, run, walk, etc.
  - Wellness keywords: meditate, breath, yoga, stretch, etc.
  - Productivity keywords: read, study, learn, focus, etc.
  - Learning keywords: course, tutorial, skill, education
- [ ] Return `HabitCategory` enum (FITNESS, WELLNESS, PRODUCTIVITY, LEARNING, GENERAL)
- [ ] Add sub-classification (Indoor/Outdoor, Short/Long duration)
- [ ] Write unit tests for classification

**Acceptance Criteria**:
- Classification correctly categorizes posts
- All keyword lists are comprehensive
- Unit tests pass with various inputs
- Classification runs on-device (no cloud)

**Dependencies**: Issue 11

---

### Issue 13: FitScore Calculation System
**Assignee**: Dylan (Backend)  
**Priority**: High  
**Labels**: `algorithm`, `backend`, `scoring`

**Description**:
Implement FitScore calculation that ranks suggestions based on user context.

**Tasks**:
- [ ] Create `FitScoreCalculator` class
- [ ] Create `UserContext` data class:
  - preferredCategories, currentTime, currentLocation, recentMotionState
  - currentWeather (optional for MVP)
- [ ] Implement scoring logic:
  - Base score: 50
  - +20 if matches preferred categories
  - +15 if time-appropriate
  - +10 if weather-appropriate (optional)
  - +5 if location-appropriate
  - +10 if motion-state matches
- [ ] Implement helper functions:
  - `isTimeAppropriate()`, `isLocationAppropriate()`, `isMotionStateAppropriate()`
- [ ] Write unit tests

**Acceptance Criteria**:
- FitScore calculates correctly (0-100 range)
- All scoring factors work
- Weather is optional (doesn't break if null)
- Unit tests verify scoring logic

**Dependencies**: Issue 12, Issue 3

---

### Issue 14: Explore Screen UI (Stream #2)
**Assignee**: Devon (Frontend)  
**Priority**: High  
**Labels**: `ui`, `frontend`, `stream`, `explore`

**Description**:
Create the Explore screen that displays API suggestions in a scrollable stream.

**Tasks**:
- [ ] Design `fragment_explore.xml` layout
- [ ] Create `SuggestionItemAdapter` for RecyclerView
- [ ] Create `SuggestionItemViewHolder` with suggestion card:
  - Post title and snippet
  - Source badge (Hacker News)
  - FitScore visual indicator (progress bar or number)
  - Category badge
  - Context indicators (icons for time/weather/location match)
  - "Turn into Habit" button
- [ ] Create `ExploreFragment` with RecyclerView
- [ ] Implement pull-to-refresh
- [ ] Add loading and error states
- [ ] Sort suggestions by FitScore (highest first)
- [ ] Style with Material Design

**Acceptance Criteria**:
- Explore screen displays API suggestions
- Each card shows FitScore and category
- Suggestions are sorted by FitScore
- Pull-to-refresh works
- "Turn into Habit" button is functional

**Dependencies**: Issue 4, Issue 11, Issue 13

---

### Issue 15: Explore ViewModel & Integration
**Assignee**: Hung (Medium)  
**Priority**: High  
**Labels**: `viewmodel`, `mvvm`, `explore`

**Description**:
Create ViewModel for Explore screen and connect API, classification, and scoring.

**Tasks**:
- [ ] Create `ExploreViewModel`:
  - Observable LiveData for suggestions list
  - Methods: loadSuggestions(), refreshSuggestions()
  - Handle loading/error states
- [ ] Integrate API calls → Classification → FitScore → UI
- [ ] Implement caching logic (store in Room, refresh periodically)
- [ ] Connect to UserContext (location, motion state, preferences)
- [ ] Handle API errors gracefully

**Acceptance Criteria**:
- ViewModel fetches and processes suggestions
- Classification and scoring run automatically
- Suggestions update reactively
- Caching reduces API calls

**Dependencies**: Issue 14, Issue 12, Issue 13

---

### Issue 16: Pre-fill Habit from Explore
**Assignee**: Hung (Medium)  
**Priority**: High  
**Labels**: `navigation`, `feature`, `explore`

**Description**:
Implement navigation from Explore to Add/Edit screen with pre-filled data.

**Tasks**:
- [ ] Extract habit data from suggestion:
  - Name (from post title)
  - Category (from classification)
  - Type (infer from content: motion/time/location)
  - Suggested parameters
- [ ] Pass data via Safe Args to AddEditHabitFragment
- [ ] Pre-fill form fields in Add/Edit screen
- [ ] Allow user to modify before saving
- [ ] Test navigation flow

**Acceptance Criteria**:
- Tapping "Turn into Habit" opens Add/Edit screen
- Form is pre-filled with suggestion data
- User can modify before saving
- Navigation works smoothly

**Dependencies**: Issue 14, Issue 7

---

### Issue 17: Habit Detail Screen UI
**Assignee**: Devon (Frontend)  
**Priority**: Medium  
**Labels**: `ui`, `frontend`, `detail`

**Description**:
Create Habit Detail screen showing streak, history, and analytics.

**Tasks**:
- [ ] Design `fragment_habit_detail.xml` layout
- [ ] Create `HabitDetailFragment` with:
  - Streak display (current, best)
  - Completion percentage (7-day, 30-day)
  - Calendar view (completion history)
  - List view (detailed completion log)
  - Trend visualization (simple line chart or placeholder)
  - Edit and Delete buttons
- [ ] Implement toggle between calendar and list views
- [ ] Style with Material Design
- [ ] Add empty states

**Acceptance Criteria**:
- Detail screen shows all habit information
- Calendar and list views work
- Edit/Delete buttons are functional
- UI matches wireframes

**Dependencies**: Issue 4, Issue 3

---

### Issue 18: Habit Detail ViewModel & Data
**Assignee**: Hung (Medium)  
**Priority**: Medium  
**Labels**: `viewmodel`, `detail`, `analytics`

**Description**:
Implement ViewModel and data logic for Habit Detail screen.

**Tasks**:
- [ ] Create `HabitDetailViewModel`:
  - Load habit by ID
  - Calculate streak (current, best)
  - Calculate completion percentages (7-day, 30-day)
  - Load completion history
- [ ] Implement analytics calculations:
  - Completion rate
  - Trend analysis
  - Best day of week
- [ ] Format data for calendar and list views
- [ ] Handle loading/error states

**Acceptance Criteria**:
- ViewModel loads habit data correctly
- Streak calculations are accurate
- Completion percentages are correct
- History data is formatted properly

**Dependencies**: Issue 17, Issue 3

---

### Issue 19: Basic Time-Based Notifications
**Assignee**: Dylan (Backend)  
**Priority**: Medium  
**Labels**: `notifications`, `backend`, `workmanager`

**Description**:
Implement basic time-based notifications using WorkManager.

**Tasks**:
- [ ] Set up WorkManager for background tasks
- [ ] Create `NotificationWorker` class
- [ ] Implement scheduling logic:
  - Schedule notifications for habit reminder times
  - Handle recurring notifications (daily)
- [ ] Create notification channel and style
- [ ] Add notification actions (Mark Done, Snooze)
- [ ] Test notifications trigger correctly

**Acceptance Criteria**:
- Notifications are scheduled for habit reminders
- Notifications appear at correct times
- Action buttons work (Mark Done, Snooze)
- Notifications persist after app close

**Dependencies**: Issue 3

---

# 🚀 SPRINT 3 (Unit 10) - Advanced Features & Polish

**Goal**: Implement sensor-based features, advanced notifications, and polish  
**Due Date**: End of Unit 10  
**Focus**: Mobile-native features and final polish

**Issues in this Sprint: 20, 21, 22, 23, 24, 25, 26, 27, 28, 29**

---

### Issue 20: Activity Recognition Service
**Assignee**: Dylan (Backend)  
**Priority**: High  
**Labels**: `sensors`, `backend`, `auto-completion`

**Description**:
Implement Activity Recognition to detect motion and auto-complete habits.

**Tasks**:
- [ ] Set up Activity Recognition API
- [ ] Create `ActivityRecognitionService` (ForegroundService or WorkManager)
- [ ] Implement motion detection:
  - Detect WALKING, RUNNING, STATIONARY states
  - Track duration of activities
- [ ] Implement auto-completion logic:
  - Query Room DB for motion-based habits matching detected activity
  - Check if duration threshold is met
  - Auto-complete habit if conditions met
- [ ] Optimize battery usage (reduce polling when inactive)
- [ ] Handle permissions gracefully

**Acceptance Criteria**:
- Motion is detected correctly
- Auto-completion triggers when thresholds met
- Battery usage is optimized
- Works in background

**Dependencies**: Issue 3, Issue 19

---

### Issue 21: Geofencing Service
**Assignee**: Dylan (Backend)  
**Priority**: High  
**Labels**: `sensors`, `backend`, `geofencing`

**Description**:
Implement geofencing for location-based habit triggers.

**Tasks**:
- [ ] Set up FusedLocationProviderClient
- [ ] Create `GeofenceService` class
- [ ] Implement geofence creation:
  - Create geofences for location-based habits
  - Set radius and transition types
- [ ] Implement geofence triggers:
  - Detect when user enters geofence
  - Send notification or auto-complete
- [ ] Add map picker UI for selecting locations (in Add/Edit screen)
- [ ] Handle permissions and errors gracefully
- [ ] Test geofence triggers

**Acceptance Criteria**:
- Geofences are created for location habits
- Notifications trigger when entering geofence
- Map picker works for selecting locations
- Handles permission denial gracefully

**Dependencies**: Issue 7, Issue 19

---

### Issue 22: Dynamic Notifications
**Assignee**: Hung (Medium)  
**Priority**: Medium  
**Labels**: `notifications`, `feature`

**Description**:
Implement dynamic notifications (streak countdown, inactivity nudges).

**Tasks**:
- [ ] Implement streak countdown notifications:
  - Calculate time remaining to save streak
  - Send notification when < 2 hours remaining
- [ ] Implement inactivity detection:
  - Monitor motion state
  - Send nudge if stationary for 2+ hours
- [ ] Implement geofence-triggered notifications
- [ ] Add notification actions (Mark Done, Snooze)
- [ ] Prevent duplicate notifications

**Acceptance Criteria**:
- Streak countdown notifications work
- Inactivity nudges trigger correctly
- Geofence notifications work
- No duplicate notifications

**Dependencies**: Issue 19, Issue 20

---

### Issue 23: Profile/Stats Screen UI
**Assignee**: Devon (Frontend)  
**Priority**: Medium  
**Labels**: `ui`, `frontend`, `profile`

**Description**:
Create Profile/Stats screen with aggregate statistics and charts.

**Tasks**:
- [ ] Design `fragment_profile_stats.xml` layout
- [ ] Create `ProfileStatsFragment` with:
  - User profile section (name, email)
  - Aggregate statistics:
    - Total habits, longest streak, completion rate
    - Total completions (all-time)
  - Motivational insights:
    - "Most Consistent Habit"
    - "Best Day of the Week"
  - Visual charts (trends, category breakdown)
- [ ] Implement chart components (use library like MPAndroidChart or simple custom views)
- [ ] Style with Material Design

**Acceptance Criteria**:
- Profile screen displays all statistics
- Charts visualize data correctly
- Insights are calculated and displayed
- UI matches wireframes

**Dependencies**: Issue 4, Issue 3

---

### Issue 24: Profile/Stats ViewModel & Analytics
**Assignee**: Hung (Medium)  
**Priority**: Medium  
**Labels**: `viewmodel`, `analytics`, `profile`

**Description**:
Implement ViewModel and analytics calculations for Profile/Stats screen.

**Tasks**:
- [ ] Create `ProfileStatsViewModel`:
  - Load aggregate statistics
  - Calculate completion rates
  - Calculate insights (most consistent habit, best day)
- [ ] Implement analytics calculations:
  - Total habits tracked
  - Longest streak across all habits
  - Overall completion rate (7-day, 30-day)
  - Category breakdown
  - Weekly heatmap data
- [ ] Format data for charts
- [ ] Handle loading/error states

**Acceptance Criteria**:
- All statistics are calculated correctly
- Insights are accurate
- Chart data is formatted properly
- Performance is good with large datasets

**Dependencies**: Issue 23, Issue 3

---

### Issue 25: Settings Screen
**Assignee**: Devon (Frontend)  
**Priority**: Medium  
**Labels**: `ui`, `frontend`, `settings`

**Description**:
Create Settings screen for app preferences and permissions.

**Tasks**:
- [ ] Design `fragment_settings.xml` layout
- [ ] Create `SettingsFragment` with:
  - Notification preferences (toggle, Quiet Hours)
  - Permission management (status indicators, system shortcuts)
  - Data management (clear history, reset streaks)
  - Battery optimization settings
  - About section
- [ ] Implement preference toggles (SharedPreferences)
- [ ] Add links to Android system settings
- [ ] Style with Material Design

**Acceptance Criteria**:
- Settings screen displays all options
- Toggles save preferences
- Permission status is shown correctly
- Links to system settings work

**Dependencies**: Issue 4

---

### Issue 26: Settings ViewModel & Logic
**Assignee**: Hung (Medium)  
**Priority**: Medium  
**Labels**: `viewmodel`, `settings`

**Description**:
Implement ViewModel and logic for Settings screen.

**Tasks**:
- [ ] Create `SettingsViewModel`:
  - Load current preferences
  - Save preference changes
  - Check permission status
- [ ] Implement preference management:
  - Notification preferences
  - Quiet Hours configuration
  - Battery optimization mode
- [ ] Implement data management:
  - Clear completion history
  - Reset streaks
- [ ] Handle permission status checks

**Acceptance Criteria**:
- Preferences are saved and loaded correctly
- Permission status is accurate
- Data management actions work
- Changes persist

**Dependencies**: Issue 25, Issue 3

---

### Issue 27: User Preferences Storage
**Assignee**: Hung (Medium)  
**Priority**: Low  
**Labels**: `backend`, `preferences`

**Description**:
Implement storage for user preferences (categories, notification times, etc.).

**Tasks**:
- [ ] Create `UserPreferences` entity or use SharedPreferences
- [ ] Store preferred categories (for FitScore)
- [ ] Store default notification preferences
- [ ] Store Quiet Hours settings
- [ ] Implement getter/setter methods
- [ ] Initialize defaults on first launch

**Acceptance Criteria**:
- Preferences are stored and retrieved correctly
- Defaults are set on first launch
- Preferences persist across app restarts

**Dependencies**: Issue 2, Issue 13

---

### Issue 28: Testing & Bug Fixes
**Assignee**: All Team Members  
**Priority**: High  
**Labels**: `testing`, `bug-fixes`

**Description**:
Comprehensive testing and bug fixing before final submission.

**Tasks**:
- [ ] Test all user flows end-to-end
- [ ] Test edge cases:
  - Empty states
  - Permission denials
  - Network errors
  - Invalid inputs
- [ ] Fix any bugs found
- [ ] Test on different Android versions
- [ ] Performance testing (large datasets)
- [ ] Battery usage testing
- [ ] UI/UX polish

**Acceptance Criteria**:
- All features work correctly
- Edge cases are handled
- No critical bugs
- App performs well
- UI is polished

**Dependencies**: All previous issues

---

### Issue 29: Documentation & README Updates
**Assignee**: Devon (Frontend)  
**Priority**: Medium  
**Labels**: `documentation`

**Description**:
Update README with build progress, GIFs, and completed features.

**Tasks**:
- [ ] Update README with Milestone 2 progress
- [ ] Create GIFs showing build progress
- [ ] Document completed user stories
- [ ] Update Project Board screenshots
- [ ] Document any challenges faced
- [ ] Add setup instructions

**Acceptance Criteria**:
- README is up to date
- GIFs show clear progress
- All completed features are documented
- Project Board is updated

**Dependencies**: All previous issues

---

## 📊 Issue Summary by Sprint & Assignee

### **Sprint 1 (Unit 8) - 10 Issues**

**Dylan (Backend) - 3 Issues**:
- Issue 1: Project Setup & Architecture
- Issue 2: Room Database Setup
- Issue 3: Repository Layer

**Devon (Frontend) - 4 Issues**:
- Issue 4: Navigation Setup
- Issue 5: Login & Onboarding UI
- Issue 6: Today Screen UI (Stream #1)
- Issue 7: Add/Edit Habit UI (Creation Screen)

**Hung (Medium) - 3 Issues**:
- Issue 8: ViewModels for Today & Add/Edit
- Issue 9: Basic Habit CRUD Operations
- Issue 10: Mark Habit Complete (Manual)

---

### **Sprint 2 (Unit 9) - 9 Issues**

**Dylan (Backend) - 4 Issues**:
- Issue 11: Hacker News API Integration
- Issue 12: Habit Classification System
- Issue 13: FitScore Calculation System
- Issue 19: Basic Time-Based Notifications

**Devon (Frontend) - 2 Issues**:
- Issue 14: Explore Screen UI (Stream #2)
- Issue 17: Habit Detail Screen UI

**Hung (Medium) - 3 Issues**:
- Issue 15: Explore ViewModel & Integration
- Issue 16: Pre-fill Habit from Explore
- Issue 18: Habit Detail ViewModel & Data

---

### **Sprint 3 (Unit 10) - 10 Issues**

**Dylan (Backend) - 2 Issues**:
- Issue 20: Activity Recognition Service
- Issue 21: Geofencing Service

**Devon (Frontend) - 3 Issues**:
- Issue 23: Profile/Stats Screen UI
- Issue 25: Settings Screen
- Issue 29: Documentation & README Updates

**Hung (Medium) - 4 Issues**:
- Issue 22: Dynamic Notifications
- Issue 24: Profile/Stats ViewModel & Analytics
- Issue 26: Settings ViewModel & Logic
- Issue 27: User Preferences Storage

**All Team Members - 1 Issue**:
- Issue 28: Testing & Bug Fixes (shared)

---

## 📝 How to Implement This in GitHub

### Step 1: Create Project Board
1. Go to your GitHub repository
2. Click "Projects" tab
3. Click "New project"
4. Choose "Board" template
5. Name it: "MicroHabit Coach Development"
6. Add columns: "To Do", "In Progress", "Done"
7. Make sure `codepathreview` has access

### ⚠️ **Temporarily Removing Tickets from Board (For Requirements)**

**If you need to show "up now" status for Unit 8 requirements:**

1. **Option 1: Archive Cards (Recommended)**
   - Go to your Project Board
   - Click the three dots (⋯) on any card
   - Select "Archive"
   - Card is removed from board but issue remains open
   - To restore: Go to issue → click "Add to project" → select your board

2. **Option 2: Remove from Project (Temporary)**
   - Go to your Project Board
   - Click the three dots (⋯) on any card
   - Select "Remove from project"
   - Card disappears from board
   - Issue still exists and can be re-added later

3. **Option 3: Move to "Done" Column**
   - Simply drag cards to "Done" column
   - Shows they're completed
   - Can move back to "To Do" later

**To Restore Cards Later:**
- Go to the issue page
- On the right sidebar, find "Projects" section
- Click "Add to project" → Select your board
- Card reappears in "To Do" column

**Note**: For Unit 8 submission, you can temporarily archive or remove cards, then restore them when you're ready to start Sprint 1 work.

### Step 2: Create Milestones
1. Go to "Issues" → "Milestones"
2. Click "New milestone"
3. Create three milestones:
   - **Milestone 1: Sprint 1 (Unit 8)** - Due: End of Unit 8
   - **Milestone 2: Sprint 2 (Unit 9)** - Due: End of Unit 9
   - **Milestone 3: Sprint 3 (Unit 10)** - Due: End of Unit 10

### Step 3: Create Issues
For each issue in this document:

1. Go to "Issues" → "New issue"
2. **Title**: Copy the issue title (e.g., "Project Setup & Architecture")
3. **Description**: Copy the full description, tasks, and acceptance criteria
4. **Assignee**: Assign to the recommended team member
5. **Labels**: Add the suggested labels (create labels if needed)
6. **Milestone**: Select the appropriate milestone (1, 2, or 3)
7. **Project**: Add to "MicroHabit Coach Development" project board
8. Click "Submit new issue"

### Step 4: Organize Project Board
1. Go to your Project Board
2. At the start of each sprint, drag all issues from the milestone into "To Do" column
3. As work progresses, move issues to "In Progress" → "Done"
4. Update issue status regularly

### Step 5: Update Throughout Sprint
- Update issue descriptions with progress
- Move cards between columns
- Add comments to issues
- Link related issues
- Close issues when complete

---

## 🎯 Execution Order & Dependencies

### **Critical Path (Must Do First)**
These issues block everything else - start here:

**Week 1 (Sprint 1 Start)**:
1. **Issue 1** (Dylan) - Project Setup → **START IMMEDIATELY**
2. **Issue 2** (Dylan) - Room Database → **After Issue 1**
3. **Issue 3** (Dylan) - Repository Layer → **After Issue 2**
4. **Issue 4** (Devon) - Navigation Setup → **Can start parallel with Issue 1**

### **Parallel Work Opportunities**
These can be done simultaneously by different team members:

**Sprint 1 Parallel Work**:
- **Devon** (Frontend): Issues 4, 5, 6, 7 (UI screens)
- **Dylan** (Backend): Issues 1, 2, 3 (Database/Architecture)
- **Hung** (Integration): Issues 8, 9, 10 (ViewModels, CRUD) - *After Issues 3, 6, 7*

**Sprint 2 Parallel Work**:
- **Dylan** (Backend): Issues 11, 12, 13 (API, Classification, FitScore)
- **Devon** (Frontend): Issues 14, 17 (Explore UI, Detail UI)
- **Hung** (Integration): Issues 15, 16, 18 (ViewModels, Navigation, Data)

**Sprint 3 Parallel Work**:
- **Dylan** (Backend): Issues 20, 21 (Sensors)
- **Devon** (Frontend): Issues 23, 25 (Profile UI, Settings UI)
- **Hung** (Integration): Issues 22, 24, 26, 27 (Notifications, ViewModels)

### **Recommended Execution Order by Sprint**

#### **Sprint 1 (Unit 8) - Week 1-2**
**Day 1-2**: 
- Dylan: Issue 1 (Setup)
- Devon: Issue 4 (Navigation) - *parallel*

**Day 3-4**:
- Dylan: Issue 2 (Database)
- Devon: Issue 5 (Login/Onboarding UI) - *parallel*

**Day 5-6**:
- Dylan: Issue 3 (Repository)
- Devon: Issue 6 (Today UI) - *parallel*

**Day 7-8**:
- Devon: Issue 7 (Add/Edit UI)
- Hung: Issue 8 (ViewModels) - *after Issue 3*

**Day 9-10**:
- Hung: Issue 9 (CRUD)
- Hung: Issue 10 (Mark Complete)

#### **Sprint 2 (Unit 9) - Week 3-4**
**Week 3**:
- Dylan: Issues 11, 12, 13 (API, Classification, FitScore) - *sequential*
- Devon: Issue 14 (Explore UI) - *parallel with Issue 11*

**Week 4**:
- Hung: Issues 15, 16 (Explore ViewModel, Pre-fill)
- Devon: Issue 17 (Detail UI)
- Hung: Issue 18 (Detail ViewModel)
- Dylan: Issue 19 (Notifications)

#### **Sprint 3 (Unit 10) - Week 5-6**
**Week 5**:
- Dylan: Issues 20, 21 (Activity Recognition, Geofencing)
- Devon: Issues 23, 25 (Profile UI, Settings UI)

**Week 6**:
- Hung: Issues 22, 24, 26, 27 (Notifications, ViewModels, Preferences)
- All: Issue 28 (Testing)
- Devon: Issue 29 (Documentation)

---

## 🤝 Collaboration Strategy

### **Git Branching Strategy**
To prevent conflicts, use feature branches:

```bash
# Each team member works on their own branch
git checkout -b devon/today-screen-ui
git checkout -b dylan/database-setup
git checkout -b hung/viewmodels

# Regular merges to main
git checkout main
git merge devon/today-screen-ui
```

### **Daily Standup Structure**
**Quick 5-minute check-ins**:
1. What did you complete yesterday?
2. What are you working on today?
3. Any blockers?

### **Communication Channels**
- **GitHub Issues**: Use comments for questions, updates, blockers
- **Pull Requests**: Review each other's code before merging
- **Slack/Group Chat**: Quick questions, coordination

### **Preventing Conflicts**
1. **Separate Files**: Frontend (Devon) and Backend (Dylan) work in different files initially
2. **Clear Interfaces**: Define data models early (Issue 2) so everyone knows structure
3. **Regular Syncs**: Merge to main daily, don't let branches diverge too much
4. **Code Reviews**: Review PRs before merging

### **When Conflicts Happen**
1. Communicate immediately
2. One person resolves (usually the one who touched the file last)
3. Test together after resolution
4. Update documentation if interfaces change

---

## 🎯 Sprint Planning Tips

1. **Start with Dependencies**: Work on issues with no dependencies first (Issue 1, 4)
2. **Parallel Work**: Some issues can be worked on simultaneously:
   - Frontend (Devon) and Backend (Dylan) can work in parallel
   - UI screens can be built while APIs are being integrated
3. **Daily Standups**: Check in daily on progress and blockers
4. **Scope Management**: If behind, prioritize critical path issues
5. **Testing**: Test as you go, don't wait until the end
6. **Git Hygiene**: Commit often, write clear commit messages, push daily

---

## ✅ Checklist for Unit 8 Submission

- [ ] GitHub Project Board created
- [ ] 3 Milestones created with due dates
- [ ] All 29 issues created
- [ ] Issues assigned to team members
- [ ] Issues added to Project Board
- [ ] Issues assigned to correct milestones
- [ ] Project Board shared with `codepathreview`

---

*This document should be your guide for creating all GitHub issues. Copy each issue description exactly as written, and adjust assignments if needed based on team preferences.*

