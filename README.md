# Milestone 1 - **Micro-Habit Coach** (Unit 7)

## Table of Contents

* [Overview](#overview)
  * [Description](#description)
  * [App Evaluation](#app-evaluation)
* [Product Spec](#product-spec)
  * [1. User Features (Required and Optional)](#1-user-features-required-and-optional)
  * [2. Screen Archetypes](#2-screen-archetypes)
  * [3. Navigation](#3-navigation)
* [Wireframes](#wireframes)

---

## Overview

### Description

**MicroHabit Coach** is a mobile-native behavior engine that solves two critical problems that cause habit-tracking apps to fail:

1. **Decision Fatigue**: Users don't know what habits to create or how to start. Most apps present a blank slate, forcing users to invent habits from scratch—a cognitive burden that leads to abandonment.

2. **Lack of Context-Aware Suggestions**: Traditional habit trackers are static. They remind you at fixed times regardless of your location, activity level, or current state. This creates friction when the reminder doesn't match reality (e.g., "Go for a walk" when you're already walking, or "Drink water" when you're nowhere near water).

**Our Solution**: MicroHabit Coach is a mobile-native behavior engine that:
- **Continuously suggests context-aware habits** from live API feeds (Hacker News/News API), eliminating decision fatigue
- **Auto-completes habits** when your phone detects you've already done the work (motion detection, geofencing)
- **Scores suggestions in real-time** based on your location, weather, time of day, and movement state
- **Converts any suggestion into a trackable habit** with one tap

The app doesn't just track habits—it actively generates, scores, and verifies them using your phone's sensors and contextual data. This is impossible on desktop and requires true mobile-native capabilities.

### App Evaluation

**Category:** Productivity / Health & Fitness

**Mobile:** ⭐⭐⭐⭐⭐ Very High

More than a glorified website: The app fundamentally relies on mobile sensors and background services that cannot exist on desktop.

**Two or more mobile-specific features** (we have **4+**):

1. **Activity Recognition / Motion Detection** (Android ActivityRecognitionClient)
   - Detects walking, running, stationary states
   - Auto-completes movement-based habits when thresholds are met
   - Continuously monitors in background via WorkManager

2. **Geofencing / Location Services** (FusedLocationProviderClient)
   - Place-based habit triggers (e.g., "Hydrate at Gym" activates when entering gym geofence)
   - Location-aware FitScore calculation for API suggestions
   - Low-power background monitoring

3. **Push Notifications / Real-time Alerts** (WorkManager + NotificationManager)
   - Dynamic streak countdown notifications ("2 hours left to save your streak")
   - Contextual nudges based on inactivity ("You've been still for 2 hours")
   - Geofence-triggered reminders
   - Actionable notification buttons (Mark Done, Snooze)

4. **Real-time Data Updates** (LiveData + Room + Background Services)
   - Continuous sensor data processing
   - Real-time habit completion updates
   - Background API fetching and scoring
   - Widget updates without app launch

**Story:** ⭐⭐⭐⭐ High

**The Pitch**: "MicroHabit Coach is the only habit app that suggests habits based on your phone's context and auto-completes them when your device detects you've done the work. When you run out of ideas, the app pulls real exercise discussions from Hacker News, analyzes them locally using your location and movement data, and turns them into one-tap habits that sync with your motion and geofence triggers."

**Why it's compelling**:
- Solves a real, relatable problem (decision fatigue + context mismatch)
- Demonstrates clear Android-native intelligence (not just a database)
- Shows technical sophistication (API integration, local classification, sensor fusion)
- Has a clear demo narrative: "Watch as the app suggests a workout, you tap to create it, and it auto-completes when you start walking"

**Market:** ⭐⭐⭐⭐ Large

**Target Audience**:
- **Primary**: Students and professionals (18-35) seeking structured habit reinforcement
- **Secondary**: Fitness enthusiasts and athletes who want context-aware workout suggestions
- **Tertiary**: Anyone struggling with habit consistency due to decision fatigue

**Market Size**: The productivity/health app market is massive (millions of users), but most apps are generic trackers. Our context-aware, suggestion-driven approach addresses an underserved niche.

**Unique Value**: Unlike generic habit trackers (Habitica, Streaks), we combine:
- External content discovery (API feeds)
- Local intelligence (classification, scoring)
- Sensor-based verification (auto-completion)
- Context-aware suggestions (location, weather, time, motion)

**Habit:** ⭐⭐⭐⭐ High

**Habit-forming mechanisms**:

1. **Positive Reinforcement**:
   - Streak counters and visual progress indicators
   - Completion animations (confetti, color transitions)
   - Analytics showing consistency metrics
   - "Best Day of the Week" insights

2. **Negative Reinforcement** (Urgency):
   - Streak countdown timers ("2 hours left to save your 7-day streak")
   - Home widget showing incomplete habits
   - Push notifications with actionable buttons

3. **Reduced Friction**:
   - One-tap completion
   - Auto-completion via sensors (no manual logging needed)
   - Pre-filled habit creation from API suggestions
   - Context-aware reminders (right time, right place)

4. **Continuous Engagement**:
   - Daily habit list (Today screen)
   - Fresh suggestions from API feed (Explore screen)
   - Real-time updates as habits complete
   - Weekly/monthly analytics review

**Average user opens app**: Daily (to check Today screen, mark completions, browse suggestions)  
**Average user creates content**: Regularly (converts API suggestions to habits, creates custom habits)

**Scope:** ⭐⭐⭐⭐⭐ Excellent

**Appropriately technically challenging**:
- Room database (CRUD operations)
- Retrofit for API calls (Hacker News/News API)
- Activity Recognition API integration
- Geofencing with FusedLocationProviderClient
- WorkManager for background tasks
- LiveData/ViewModel architecture
- Navigation Component
- Notification system
- Optional: AppWidget (stretch goal)

**Stripped-down version still interesting**:
- MVP: Today screen (habits list), Add/Edit habit, basic notifications, simple API feed
- Even without auto-completion, the suggestion feed + manual tracking is valuable
- Core value proposition (decision fatigue solution) remains intact

**Product is clearly defined**:
- Two Stream screens (Today = habits, Explore = API suggestions)
- Clear navigation flows
- Well-defined user features (required vs optional)
- Technical implementation path is clear

---

## Product Spec

### 1. User Features (Required and Optional)

#### Required Features (MVP)

* **Account & Onboarding**

  * Create account; choose from **smart templates** (e.g., *Hydrate at Gym*, *Move Break*, *Walk 10 min*, *Read 10 pages*) or create a custom habit.
  * No hard limit on habits; **soft guidance** to 3–6 for clarity (UI stays performant).
  * The onboarding process guides users through permission setup for motion and location. It also introduces the app’s core features and explains how automated completion works.
  * User data and settings are stored locally via Room for speed and privacy, with future compatibility for cloud sync.
  * Contextual setup ensures habits are intelligently categorized (time, movement, or location), reducing user confusion during initial configuration.

* **Today Screen (Stream #1 - Habits List)**

  * Displays all habits scheduled or active for the current day, grouped by completion state.
  * Scrollable list of discrete habit items (Stream archetype).
  * Each habit card shows:
    - Habit name and icon
    - Progress bar/ring
    - Streak count
    - Next reminder time
    - Completion status (checked/unchecked)
  * Users can complete any habit with a single tap, triggering immediate visual feedback such as confetti or color transitions.
  * Summary banner at top: "X of Y habits completed today"
  * Real-time updates as sensor-triggered completions occur (LiveData).

* **Explore Screen (Stream #2 - API Suggestions Feed)**

  * Scrollable stream of habit suggestions from Hacker News API (primary) or News API (backup).
  * Each suggestion card displays:
    - Post title and snippet
    - Source badge (Hacker News/News API)
    - FitScore (0-100) with visual indicator
    - Category badge (Fitness, Wellness, Productivity, Learning)
    - Context indicators (time-appropriate, weather-appropriate, location-appropriate icons)
    - "Turn into Habit" button
  * Stream sorted by FitScore (highest relevance first).
  * Pull-to-refresh to fetch new suggestions.
  * Tap "Turn into Habit" → opens Creation screen pre-filled with:
    - Habit name (extracted from post title)
    - Category (from classification)
    - Suggested type (Time-based, Motion-based, or Location-based)
    - Suggested parameters (duration, location, etc.)

* **Habit Classification & FitScore System**

  * **Local classification** (no AI/ML, simple keyword matching in Kotlin):
    - Parses post title and content for keywords
    - Categories: Fitness, Wellness, Productivity, Learning, General
    - Sub-classifications: Indoor/Outdoor, Short/Long duration
  * **FitScore calculation** (0-100):
    - Base score: 50
    - +20 if matches user's preferred categories
    - +15 if time-appropriate (e.g., "morning workout" at 7am)
    - +10 if weather-appropriate (e.g., "outdoor run" when sunny)
    - +5 if location-appropriate (e.g., "gym workout" near gym geofence)
    - +10 if motion-state matches (e.g., "walk" when user is walking)
  * Runs entirely on-device (no cloud processing).

* **Scheduled & Dynamic Notifications**

  * **Time-based** reminders use WorkManager for reliable background execution, even in doze mode.
  * Dynamic notifications provide streak-saving alerts (“You have 2 hours to save your streak”) and adapt based on user behavior or inactivity.
  * Each reminder can trigger actionable buttons (e.g., “Mark Done” or “Snooze”) to minimize app entry friction.
  * Duplicate or redundant notifications are automatically filtered to maintain a clean user experience.

* **Activity Recognition Assist (base)**

  * Integrates Android’s ActivityRecognitionClient to detect transitions such as walking, running, or being stationary.
  * Movement-type habits like *Walk 10 min* or *Move Break* can be automatically marked complete once motion duration or intensity meets the threshold.
  * Sensor sampling is optimized to conserve battery by reducing polling intervals when inactive.
  * Logs each recognized activity event for analytics and future machine learning recommendations.

* **Geofenced Nudges (base)**

  * Implements FusedLocationProviderClient for low-power geofencing.
  * Place-based habits such as *Hydrate at Gym* or *Stretch at Home* activate a notification when the user enters a predefined location radius.
  * Users can define radius sensitivity to minimize false triggers.
  * Permission fallback gracefully disables this feature and reverts to time-based reminders with clear in-app messaging.

* **Habit Detail & History**

  * Provides detailed streak tracking, performance summaries, and trend visualization.
  * Calendar and list modes offer both high-level and granular views of progress.
  * Each completion entry is logged in Room and rendered dynamically through LiveData for instant updates.
  * History analytics display seven-day and thirty-day completion percentages, helping users monitor adherence and growth.

* **Create/Edit/Delete Habit**

  * Users can create new habits or edit existing ones with a guided form interface that adapts to habit type.
  * Options include naming, target count, time scheduling, and optional motion or location triggers.
  * Smart validation prevents setting conflicting reminders and limits excessive notification frequency.
  * Deleting a habit removes its associated triggers, database entries, and notification schedules cleanly.

* **Settings**

  * Users can toggle notifications, configure Quiet Hours, manage permissions, and enable battery optimization.
  * Includes access to a “Data Management” section where users can clear history or reset streaks.
  * Battery-friendly mode automatically adjusts sensor update intervals and background job frequency.
  * Provides direct navigation to Android’s system settings for location or motion permissions.

#### Optional Features (Stretch)

* **Home-Screen Widget**

  * Displays remaining habits for the day with completion shortcuts.
  * Widget updates dynamically as habits are completed or auto-marked through sensors.

* **Lite Social Accountability**

  * Allows users to share habit streaks or completion milestones with friends for external motivation.
  * Designed as a read-only sharing mode to maintain privacy and simplicity.

* **Cloud Backup/Sync**

  * Optional Firebase integration for cross-device data storage.
  * Synchronizes habit configurations, completion history, and streaks to maintain continuity when reinstalling or switching devices.

* **Multi-place Geofences & Advanced Rules**

  * Allows linking multiple geofences to one habit (e.g., “Any Gym” or “Home or Park”).
  * Users can apply frequency conditions such as “only trigger once every 6 hours.”
  * Adds scheduling hierarchy for smarter trigger prioritization.

---

### 2. Screen Archetypes

* **Login / Onboarding**

  * Users authenticate through email or Google Sign-In.
  * Smart templates displayed as visual cards to help first-time users select practical examples quickly.
  * Permissions for motion and location are requested contextually with justifications for each use.
  * Includes micro-animations and clear progress indicators to maintain engagement during setup.

* **Today (Stream #1)**

  * Displays an adaptive list of active habits prioritized by urgency and streak risk.
  * Scrollable list of discrete habit items (Stream archetype).
  * Uses color-coded progress rings and completion toggles for quick scanning.
  * A summary banner at the top shows total habits completed and time remaining to preserve streaks.
  * Background service continuously updates this list based on motion and geofence triggers.
  * Real-time updates via LiveData as sensor-triggered completions occur.

* **Explore (Stream #2)**

  * Scrollable stream of API suggestions (Stream archetype).
  * Each suggestion is a discrete item with title, FitScore, category, and action button.
  * Sorted by FitScore (highest relevance first).
  * Pull-to-refresh to fetch new suggestions from Hacker News/News API.
  * Mobile-native: Uses location, weather, time, and motion state to calculate FitScore in real-time.
  * "Turn into Habit" button opens Creation screen with pre-filled data.

* **Add / Edit Habit (Creation)**

  * Includes type selector: *Time-based*, *Movement-based*, or *Location-based*.
  * Dynamic forms automatically expand or hide fields based on habit type.
  * Built-in data validation ensures that entered times, motion thresholds, and locations are realistic.
  * Each habit preview updates in real time to show how it will appear on the Today screen.

* **Habit Detail**

  * Displays a detailed breakdown of streak length, best streak, completion percentage, and trend over time.
  * Users can toggle between list and calendar history views.
  * Edit and delete options remain accessible at the top for immediate changes.
  * Designed for responsive performance even with extensive historical data.

* **Profile / Stats**

  * Summarizes user performance through aggregate statistics.
  * Highlights longest streak, completion rate, and consistency metrics.
  * Provides motivational insights such as “Most Consistent Habit” and “Best Day of the Week.”
  * Built using chart components for visual feedback on habit adherence.

* **Settings**

  * Consolidates global preferences, notifications, permissions, and backup options.
  * Quiet Hours configuration uses sliders for intuitive selection.
  * Visual permission status indicators (granted, denied, pending) improve transparency.
  * Includes system shortcuts for motion and location permission management.

---

### 3. Navigation

#### Tab Navigation (Tab to Screen)

* **Today** → Today Screen (Stream #1 - Habits List)
* **Explore** → Explore Screen (Stream #2 - API Suggestions Feed)
* **Stats** → Profile/Stats Screen
* **Settings** → Settings Screen

#### Flow Navigation (Screen to Screen)

* **Login/Onboarding → Today**  
  Directs new users to their first-day dashboard after initial setup.

* **Today → Habit Detail**  
  Opens detailed progress view of a selected habit (tap on habit card).

* **Today → Add/Edit Habit**  
  Launches creation flow via FAB or "Add Habit" button; returning automatically updates the Today list.

* **Today → Explore**  
  Navigate via tab navigation to browse API suggestions.

* **Explore → Add/Edit Habit**  
  Tap "Turn into Habit" button on any suggestion card; opens Creation screen with pre-filled data (name, category, type, parameters).

* **Explore → Today**  
  Navigate via tab navigation to return to habits list.

* **Habit Detail → Add/Edit Habit**  
  Tap Edit button; preserves data context for immediate modifications.

* **Habit Detail → Today**  
  Back button or navigation returns to Today screen.

* **Add/Edit Habit → Today**  
  After saving, returns to Today screen and updates the list.

* **Add/Edit Habit → Habit Detail**  
  If editing existing habit, can navigate to detail view.

* **Profile/Stats → Habit Detail**  
  Tap on any habit metric to see detailed analytics for that habit.

* **Profile/Stats → Settings**  
  Navigate via tab navigation or overflow menu.

* **Settings → System Permission Screens**  
  Opens Android system dialogs for adjusting key permissions (Notifications, Activity Recognition, Location).

Navigation emphasizes logical flow with minimal friction. Transitions between screens use smooth animations to maintain focus and continuity. The design prioritizes clarity, ensuring that users always understand where they are and how to return to the main dashboard.

---

## Wireframes

<img src="wireframes/sketch.png" alt="Activity 3 sketches" width="700" />

* **Onboarding**  
  Introduces templates, permission requests, and contextual guidance for first-time setup.  
  <img src="wireframes/login.png" alt="Activity 3 sketches" width="700" />

* **Today (list + big “Mark Done”)**  
  Central dashboard displaying all active habits and real-time progress.  
  <img src="wireframes/main.png" alt="Activity 3 sketches" width="700" />

* **Add/Edit (type selector → dynamic form)**  
  Context-sensitive form layout adjusting dynamically to chosen habit type.  
  <img src="wireframes/addedit.png" alt="Activity 3 sketches" width="700" />

* **Habit Detail (streak + history)**  
  Data visualization of performance trends with quick edit access.  
  <img src="wireframes/habitdetail.gif" alt="Habit detail animation" width="700" />

* **Stats (completion %, longest streak)**  
  Presents user analytics using charts and summaries.  
  <img src="wireframes/stats.gif" alt="Habit detail animation" width="700" />

* **Settings (quiet hours, permissions)**  
  Displays notification control, Quiet Hours scheduling, and permission overview.  
  <img src="wireframes/settings.gif" alt="Habit detail animation" width="700" />

**Demo walkthrough**  
Demonstrates complete user flow: onboarding, creating a habit, triggering via motion/location, marking completion, and viewing analytics.  
<img src="wireframes/walkthrough.gif" alt="Habit detail animation" width="700" />

---

# Milestone 2 - Sprint 1 (Core Habit System) (Unit 8)

<img width="1305" height="1178" alt="image" src="https://github.com/user-attachments/assets/89ecb1b7-fbe0-4b55-8e8d-997042680a1c" />
This Unit's Progress

Sprints

<img width="1169" height="870" alt="image" src="https://github.com/user-attachments/assets/4f1fe83c-6f72-4cef-9c40-1fd9221ef379" />

<img width="1211" height="876" alt="image" src="https://github.com/user-attachments/assets/40341d6f-5dd9-458f-821d-482967a74ad4" />

<img width="1157" height="881" alt="image" src="https://github.com/user-attachments/assets/d1dabe74-d24b-4d1e-a787-adbc9bc1c3c4" />



**Goal:** Build the base architecture, database, and main user flow (CRUD + Today screen).  
**Due:** End of Unit 8

---

### Setup & Project Infrastructure
- [x] Initialize Android Studio project (`com.microhabitcoach`)  
- [x] Set up GitHub repo and README (Milestone 1 complete)  
- [x] Create GitHub Project Board (To Do / In Progress / Done)  
- [x] Create three Sprint Milestones (for build weeks)  
- [x] Define coding conventions and branching strategy  

---

### Core Architecture
- [x] Implement Room Database (`Habit`, `CompletionLog` entities + DAO)  
- [ ] Create Repository Layer connecting DAO ↔ ViewModel  
- [ ] Build Habit ViewModel and LiveData for UI binding  
- [ ] Verify database CRUD operations (unit tests or logs)  

---

### UI / UX Implementation
- [ ] Build Today Dashboard UI – list of habits (grouped by completion state)  
- [ ] Add Mark Complete button (one-tap completion with visual feedback)  
- [ ] Create Add/Edit Habit screen with dynamic form for type selection  
- [ ] Add Delete Habit function (removes triggers and database entries)  
- [ ] Implement Navigation Component (Today ↔ Add/Edit ↔ Detail)  
- [ ] Create placeholder Habit Detail screen (streak and history mock)  

---

### Onboarding / Login Stub
- [ ] Create basic onboarding screens (Google Sign-In mock optional)  
- [ ] Add permission prompt stubs for motion and location (contextual UI only)  

---

### Testing & Documentation
- [ ] Test Add → Display → Complete → Delete flow  
- [ ] Update README with com



Milestone 3 - Build Sprint 2 (Unit 9)

GitHub Project board
<img width="1355" height="1014" alt="image" src="https://github.com/user-attachments/assets/1ea7a559-f959-4f5f-b114-2688576e4a7a" />


Completed User Stories (Sprint 1 - Milestone 2)
- [x] Project Setup & Architecture - Android Studio project with dependencies and MVVM structure
- [x] Room Database Setup - All entities, DAOs, and database class implemented
- [x] Repository Layer - HabitRepository and ApiRepository with LiveData observables
- [x] Navigation Setup - Navigation Component with all screen destinations and bottom navigation
- [x] Login & Onboarding UI - Login and onboarding screens with permission requests
- [x] Today Screen UI - Scrollable habit list with summary banner and empty states
- [x] Add/Edit Habit UI - Dynamic form based on habit type (Time/Motion/Location)
- [x] ViewModels - TodayViewModel and AddEditHabitViewModel with reactive LiveData
- [x] Basic CRUD Operations - Create, Read, Update, Delete habits with persistence
- [x] One-Tap Completion - Habit completion with visual feedback (confetti, animations)
Total: 10/10 Sprint 1 issues completed

<img width="272" height="850" alt="image" src="https://github.com/user-attachments/assets/237d0bfc-13f4-4758-b8b0-4921e1c337b5" />

App Demo Video
[Video](https://www.youtube.com/watch?v=k-sR59Sg97M)

Current Progress GIFs

https://github.com/user-attachments/assets/dff59336-3853-49f4-bd93-05e3bb150441

https://github.com/user-attachments/assets/4520e6b5-2b3e-4f6a-82ae-7b1150e1addd




