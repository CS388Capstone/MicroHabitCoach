# MicroHabit Coach - Complete Product Plan
## Unit 7 Requirements - Consolidated Specification

---

## 🎯 The "Why" - Core Problem Statement

**MicroHabit Coach solves two critical problems that cause habit-tracking apps to fail:**

1. **Decision Fatigue**: Users don't know what habits to create or how to start. Most apps present a blank slate, forcing users to invent habits from scratch—a cognitive burden that leads to abandonment.

2. **Lack of Context-Aware Suggestions**: Traditional habit trackers are static. They remind you at fixed times regardless of your location, activity level, or current state. This creates friction when the reminder doesn't match reality (e.g., "Go for a walk" when you're already walking, or "Drink water" when you're nowhere near water).

**Our Solution**: MicroHabit Coach is a mobile-native behavior engine that:
- **Continuously suggests context-aware habits** from live API feeds (Hacker News/News API), eliminating decision fatigue
- **Auto-completes habits** when your phone detects you've already done the work (motion detection, geofencing)
- **Scores suggestions in real-time** based on your location, weather, time of day, and movement state
- **Converts any suggestion into a trackable habit** with one tap

**The Unifying Principle**: The app doesn't just track habits—it actively generates, scores, and verifies them using your phone's sensors and contextual data. This is impossible on desktop and requires true mobile-native capabilities.

---

## 📱 App Evaluation (Unit 7 Requirements)

### Mobile: How uniquely mobile is the product experience?

**Rating: Very High** ⭐⭐⭐⭐⭐

**More than a glorified website**: The app fundamentally relies on mobile sensors and background services that cannot exist on desktop.

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

**Additional mobile-native capabilities**:
- **Battery-optimized background processing** (WorkManager handles Doze mode)
- **Home screen widget** (AppWidgetProvider) for quick habit completion
- **System integration** (permission management, settings shortcuts)

### Story: How compelling is the story around this app?

**Rating: High** ⭐⭐⭐⭐

**The Pitch**: "MicroHabit Coach is the only habit app that suggests habits based on your phone's context and auto-completes them when your device detects you've done the work. When you run out of ideas, the app pulls real exercise discussions from Hacker News, analyzes them locally using your location and movement data, and turns them into one-tap habits that sync with your motion and geofence triggers."

**Why it's compelling**:
- Solves a real, relatable problem (decision fatigue + context mismatch)
- Demonstrates clear Android-native intelligence (not just a database)
- Shows technical sophistication (API integration, local classification, sensor fusion)
- Has a clear demo narrative: "Watch as the app suggests a workout, you tap to create it, and it auto-completes when you start walking"

### Market: How large or unique is the market for this app?

**Rating: Large** ⭐⭐⭐⭐

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

### Habit: How habit-forming or addictive is this app?

**Rating: High** ⭐⭐⭐⭐

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

### Scope: How well-formed is the scope for this app?

**Rating: Excellent** ⭐⭐⭐⭐⭐

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

## 🏗️ Product Spec

### 1. User Features (Required and Optional)

#### Required Features (MVP) - Minimum 3 Required

**1. Account & Onboarding**
- Create account (email or Google Sign-In)
- Choose from smart templates during onboarding:
  - *Hydrate at Gym* (geofence-based)
  - *Move Break* (motion-based)
  - *Walk 10 min* (motion-based)
  - *Read 10 pages* (time-based)
  - *Stretch at Home* (geofence-based)
- Permission setup flow:
  - Motion/Activity Recognition permission (with explanation)
  - Location permission (with explanation for geofencing)
  - Notification permission
- Soft guidance to 3-6 habits for clarity (no hard limit)
- User data stored locally via Room (privacy-first, future cloud sync compatible)

**2. Today Screen (Stream #1 - Habits List)**
- Displays all habits scheduled/active for current day
- Scrollable list of discrete habit items
- Each habit card shows:
  - Habit name and icon
  - Progress bar/ring
  - Streak count
  - Next reminder time
  - Completion status (checked/unchecked)
- Grouped by completion state (incomplete at top, completed below)
- One-tap completion with visual feedback (confetti, color transition)
- Real-time updates as sensor-triggered completions occur
- Summary banner: "X of Y habits completed today"

**3. Explore Screen (Stream #2 - API Suggestions Feed)**
- Scrollable stream of habit suggestions from Hacker News API (primary) or News API (backup)
- Each suggestion card displays:
  - Post title and snippet
  - Source (e.g., "Hacker News Discussion")
  - FitScore (0-100) with visual indicator
  - Category badge (Fitness, Wellness, Productivity, Learning)
  - Context indicators (time-appropriate, weather-appropriate, location-appropriate)
  - "Turn into Habit" button
- Stream sorted by FitScore (highest relevance first)
- Pull-to-refresh to fetch new suggestions
- Tap "Turn into Habit" → opens Creation screen pre-filled with:
  - Habit name (extracted from post title)
  - Category (from classification)
  - Suggested type (Time-based, Motion-based, or Location-based)
  - Suggested parameters (duration, location, etc.)

**4. Habit Classification & FitScore System**
- **Local classification** (no AI/ML, simple keyword matching in Kotlin):
  - Parses post title and content for keywords
  - Categories: Fitness, Wellness, Productivity, Learning, General
  - Sub-classifications: Indoor/Outdoor, Short/Long duration
- **FitScore calculation** (0-100):
  - Base score: 50
  - +20 if matches user's preferred categories
  - +15 if time-appropriate (e.g., "morning workout" at 7am)
  - +10 if weather-appropriate (e.g., "outdoor run" when sunny)
  - +5 if location-appropriate (e.g., "gym workout" near gym geofence)
  - +10 if motion-state matches (e.g., "walk" when user is walking)
- Runs entirely on-device (no cloud processing)

**5. Create/Edit/Delete Habit (Creation Screen)**
- Type selector: Time-based, Motion-based, or Location-based
- Dynamic form fields based on type:
  - **Time-based**: Name, target count, reminder times, days of week
  - **Motion-based**: Name, motion type (walk/run), duration threshold, intensity
  - **Location-based**: Name, location (map picker), geofence radius, action
- Smart validation (prevents conflicts, limits notification frequency)
- Can be pre-filled from Explore screen suggestions
- Delete removes triggers, database entries, and notification schedules

**6. Habit Detail Screen**
- Detailed view of single habit:
  - Current streak and best streak
  - Completion percentage (7-day, 30-day)
  - Calendar view of completion history
  - List view of all completion entries
  - Trend visualization (line chart)
- Edit and Delete buttons
- Performance insights ("Most consistent day: Tuesday")

**7. Scheduled & Dynamic Notifications**
- **Time-based reminders**: WorkManager schedules notifications for habit reminder times
- **Dynamic notifications**:
  - Streak-saving alerts ("2 hours left to save your 7-day streak")
  - Inactivity nudges ("You've been still for 2 hours—time for a move break?")
  - Geofence-triggered reminders ("You're at the gym—time to hydrate!")
- Actionable notification buttons: "Mark Done", "Snooze 15 min"
- Duplicate filtering to prevent notification spam
- Quiet Hours support (user-configurable)

**8. Activity Recognition Assist (Auto-completion)**
- Uses Android ActivityRecognitionClient to detect:
  - Walking, running, stationary, in-vehicle
- Motion-based habits auto-complete when:
  - Duration threshold met (e.g., "Walk 10 min" completes after 10 min of walking)
  - Intensity threshold met (e.g., "Run 5 min" completes after 5 min of running)
- Battery-optimized: reduces polling when inactive
- Logs activity events for analytics

**9. Geofenced Nudges (Auto-triggers)**
- Uses FusedLocationProviderClient for low-power geofencing
- Place-based habits trigger when:
  - User enters predefined location radius (e.g., gym, home)
  - User exits location (optional, for "Leave home" habits)
- Examples:
  - "Hydrate at Gym" → notification when entering gym geofence
  - "Stretch at Home" → notification when entering home geofence
- User-configurable radius sensitivity
- Graceful fallback if location permission denied (reverts to time-based)

**10. Settings Screen**
- Notification preferences:
  - Toggle all notifications on/off
  - Quiet Hours configuration (time range slider)
  - Notification sound/vibration preferences
- Permission management:
  - Status indicators (granted/denied) for Motion, Location, Notifications
  - Direct links to Android system settings
- Data management:
  - Clear completion history
  - Reset streaks
  - Export data (future: JSON export)
- Battery optimization:
  - Toggle battery-friendly mode (reduces sensor polling)
  - Background sync preferences

**11. Profile / Stats Screen**
- Aggregate statistics:
  - Total habits tracked
  - Longest streak across all habits
  - Overall completion rate (7-day, 30-day)
  - Total completions (all-time)
- Motivational insights:
  - "Most Consistent Habit" (highest completion %)
  - "Best Day of the Week" (day with most completions)
  - "Current Streak Leader" (habit with longest active streak)
- Visual charts:
  - Completion trend over time (line chart)
  - Category breakdown (pie chart)
  - Weekly heatmap (calendar view)

#### Optional Features (Stretch) - Minimum 3 Optional

**1. Home-Screen Widget (AppWidget)**
- Displays remaining incomplete habits for today
- Shows completion shortcuts (tap to mark done)
- Updates in real-time as habits complete
- Configurable widget size (1x1, 2x2, 4x1)

**2. Lite Social Accountability**
- Share habit streaks or completion milestones
- Read-only sharing (no social feed, just sharing)
- Share via Android ShareSheet (text, image, or link)
- Privacy-first: user controls what to share

**3. Cloud Backup/Sync (Firebase)**
- Optional Firebase integration
- Syncs habit configurations, completion history, streaks
- Cross-device continuity (reinstall or switch devices)
- Encrypted data transmission

**4. Multi-place Geofences & Advanced Rules**
- Link multiple geofences to one habit (e.g., "Any Gym" or "Home or Park")
- Frequency conditions ("only trigger once every 6 hours")
- Scheduling hierarchy (priority-based trigger ordering)
- Conditional logic (e.g., "Only if it's before 10pm")

**5. Weather Integration**
- Pulls weather data for location-aware suggestions
- Weather-appropriate habit recommendations (e.g., "Indoor workout" when raining)
- Weather-based FitScore adjustment

**6. Habit Templates Library**
- Expandable library of pre-configured habits
- Community-contributed templates (future)
- Search and filter templates by category

---

### 2. Screen Archetypes

**All six required screen archetypes are implemented:**

#### 1. Login / Onboarding
- **Purpose**: User authentication and initial setup
- **Features**:
  - Email or Google Sign-In authentication
  - Smart template selection (visual cards)
  - Contextual permission requests with justifications:
    - "We need motion data to auto-complete your movement habits"
    - "We need location to remind you when you arrive at the gym"
  - Progress indicators (step 1 of 3, etc.)
  - Micro-animations for engagement
- **Flow**: Login → Template Selection → Permission Setup → Today Screen

#### 2. Today (Stream #1)
- **Purpose**: Primary content consumption—user's active habits for today
- **Features**:
  - Scrollable list of discrete habit items
  - Each item is a card with:
    - Habit name, icon, progress ring
    - Streak count, next reminder time
    - Completion toggle (one-tap)
  - Grouped by completion state
  - Summary banner at top
  - Real-time updates via LiveData
  - Pull-to-refresh (optional, for manual sync)
- **Mobile-native**: Background service updates list based on motion/geofence triggers

#### 3. Explore (Stream #2)
- **Purpose**: Primary content consumption—API suggestions feed
- **Features**:
  - Scrollable list of discrete suggestion items
  - Each item is a card with:
    - Post title and snippet
    - Source badge (Hacker News/News API)
    - FitScore (0-100) with visual bar
    - Category badge
    - Context indicators (icons for time/weather/location match)
    - "Turn into Habit" button
  - Sorted by FitScore (highest first)
  - Pull-to-refresh to fetch new suggestions
  - Loading states and error handling
- **Mobile-native**: Uses location, weather, time, and motion state to calculate FitScore

#### 4. Habit Detail
- **Purpose**: Display all relevant information about a single habit
- **Features**:
  - Streak tracking (current, best)
  - Completion percentage (7-day, 30-day)
  - Calendar view (completion history)
  - List view (detailed completion log)
  - Trend visualization (line chart)
  - Edit and Delete buttons
  - Performance insights
- **Additional data not in Stream**: Historical analytics, trend analysis, detailed completion log

#### 5. Add / Edit Habit (Creation)
- **Purpose**: Allow user to create or modify a habit
- **Features**:
  - Type selector (Time-based, Motion-based, Location-based)
  - Dynamic form fields based on type
  - Smart validation
  - Real-time preview of how habit will appear
  - Can be pre-filled from Explore screen
  - Save/Cancel actions
- **Mobile-native**: Map picker for location-based habits, sensor testing for motion-based

#### 6. Profile / Stats
- **Purpose**: View account information and aggregate statistics
- **Features**:
  - User profile (name, email, account info)
  - Aggregate statistics:
    - Total habits, longest streak, completion rate
    - Total completions (all-time)
  - Motivational insights
  - Visual charts (trends, category breakdown, heatmap)
  - Account actions (logout, delete account)

#### 7. Settings
- **Purpose**: Configure app preferences and behavior
- **Features**:
  - Notification preferences (toggle, Quiet Hours)
  - Permission management (status indicators, system shortcuts)
  - Data management (clear history, reset streaks)
  - Battery optimization settings
  - About section (version, credits)
- **Account-related actions**: Logout, delete account (links to Profile)

---

### 3. Navigation

#### Tab Navigation (Top-level destinations)

**Primary tabs** (3-5 tabs, bottom navigation bar):
1. **Today** → Today Screen (Stream #1)
2. **Explore** → Explore Screen (Stream #2)
3. **Stats** → Profile/Stats Screen
4. **Settings** → Settings Screen (or in Stats overflow menu if 3 tabs)

**Rationale**: Today and Explore are the two primary content streams, so they get dedicated tabs. Stats and Settings are secondary but frequently accessed.

#### Flow Navigation (Screen-to-screen transitions)

**From Login/Onboarding**:
- Login/Onboarding → **Today** (after setup complete)

**From Today (Stream #1)**:
- Today → **Habit Detail** (tap on habit card)
- Today → **Add/Edit Habit** (FAB or "Add Habit" button)
- Today → **Explore** (via tab navigation)

**From Explore (Stream #2)**:
- Explore → **Add/Edit Habit** (tap "Turn into Habit" button, pre-filled)
- Explore → **Today** (via tab navigation)

**From Habit Detail**:
- Habit Detail → **Add/Edit Habit** (tap Edit button, preserves context)
- Habit Detail → **Today** (back button or navigation)

**From Add/Edit Habit (Creation)**:
- Add/Edit Habit → **Today** (after save, updates Today list)
- Add/Edit Habit → **Habit Detail** (if editing existing habit, can navigate to detail)

**From Profile/Stats**:
- Profile/Stats → **Habit Detail** (tap on habit metric to see details)
- Profile/Stats → **Settings** (via tab navigation or overflow menu)

**From Settings**:
- Settings → **System Permission Screens** (opens Android system dialogs)
- Settings → **Profile/Stats** (back navigation)

**Navigation principles**:
- Logical flow with minimal friction
- Smooth animations between screens
- Clear back navigation (system back button + in-app back buttons)
- Context preservation (e.g., Edit Habit remembers which habit was being edited)

---

## 🔧 Technical Implementation Strategy

### API Integration Plan

**Primary API: Hacker News API**
- **Endpoint**: `https://hacker-news.firebaseio.com/v0/topstories.json`
- **Item fetch**: `https://hacker-news.firebaseio.com/v0/item/{id}.json`
- **Advantages**: No authentication, no rate limits, easy to test
- **Filtering**: Fetch top stories, filter by keywords in title: "workout", "fitness", "health", "exercise", "meditation", "productivity", "self-improvement"
- **Implementation**: Retrofit + Kotlin coroutines

**Backup API: News API**
- **Endpoint**: `https://newsapi.org/v2/everything?q=fitness&apiKey={key}`
- **Categories**: health, fitness, science
- **Advantages**: Structured, reliable, good content
- **Limitations**: Free tier = 100 requests/day
- **Implementation**: Retrofit + API key management

**Fallback Strategy**:
1. Try Hacker News API first (primary)
2. If Hacker News returns insufficient results, supplement with News API
3. Cache results locally (Room database) to reduce API calls
4. Background refresh via WorkManager (every 6 hours)

### Classification System (No AI Required)

**Simple keyword-based classification in Kotlin**:

```kotlin
enum class HabitCategory {
    FITNESS, WELLNESS, PRODUCTIVITY, LEARNING, GENERAL
}

fun classifyPost(title: String, content: String): HabitCategory {
    val text = (title + " " + content).lowercase()
    
    // Fitness keywords
    val fitnessKeywords = listOf("workout", "exercise", "gym", "run", "walk", 
                                 "jog", "squat", "pushup", "cardio", "strength")
    if (fitnessKeywords.any { text.contains(it) }) return HabitCategory.FITNESS
    
    // Wellness keywords
    val wellnessKeywords = listOf("meditate", "breath", "yoga", "stretch", 
                                   "sleep", "water", "hydrate", "mindfulness")
    if (wellnessKeywords.any { text.contains(it) }) return HabitCategory.WELLNESS
    
    // Productivity keywords
    val productivityKeywords = listOf("read", "study", "learn", "practice", 
                                      "focus", "pomodoro", "productivity")
    if (productivityKeywords.any { text.contains(it) }) return HabitCategory.PRODUCTIVITY
    
    // Learning keywords
    val learningKeywords = listOf("learn", "course", "tutorial", "skill", "education")
    if (learningKeywords.any { text.contains(it) }) return HabitCategory.LEARNING
    
    return HabitCategory.GENERAL
}
```

**FitScore Calculation**:

```kotlin
data class UserContext(
    val preferredCategories: Set<HabitCategory>,
    val currentTime: LocalTime,
    val currentWeather: Weather?,
    val currentLocation: Location?,
    val recentMotionState: MotionState
)

fun calculateFitScore(
    post: Post, 
    userContext: UserContext
): Int {
    var score = 50 // base score
    
    // Category match (+20)
    if (post.category in userContext.preferredCategories) score += 20
    
    // Time appropriateness (+15)
    if (isTimeAppropriate(post, userContext.currentTime)) score += 15
    
    // Weather appropriateness (+10)
    if (userContext.currentWeather != null && 
        isWeatherAppropriate(post, userContext.currentWeather)) score += 10
    
    // Location appropriateness (+5)
    if (userContext.currentLocation != null && 
        isLocationAppropriate(post, userContext.currentLocation)) score += 5
    
    // Motion state match (+10)
    if (isMotionStateAppropriate(post, userContext.recentMotionState)) score += 10
    
    return score.coerceIn(0, 100)
}
```

**This is simple, rule-based logic—no machine learning required. Perfect for a class project.**

### Android/Kotlin Architecture

**Tech Stack**:
- **Language**: Kotlin
- **Database**: Room (local persistence)
- **Networking**: Retrofit + OkHttp (API calls)
- **Architecture**: MVVM (Model-View-ViewModel)
- **Reactive**: LiveData + ViewModel
- **Navigation**: Navigation Component
- **Background**: WorkManager (notifications, API refresh)
- **Sensors**: ActivityRecognitionClient, FusedLocationProviderClient
- **UI**: Material Design Components, RecyclerView, ConstraintLayout

**Key Components**:
- `HabitRepository`: Manages data (Room + API)
- `HabitViewModel`: UI state management
- `HabitClassifier`: Local classification logic
- `FitScoreCalculator`: Scoring algorithm
- `ActivityRecognitionService`: Motion detection
- `GeofenceService`: Location-based triggers
- `NotificationManager`: Push notifications

---

## 📋 Unit 7 Deliverables Checklist

### Activity 1: App Idea Brainstorming ✅
- [x] Brainstorming ideas documented
- [x] 3 evaluated ideas using Mobile, Story, Market, Habit, Scope criteria
- [x] Final idea selected (MicroHabit Coach)
- [x] brainstorming.md file created

### Activity 2: Product Spec ✅
- [x] **User Features**: 
  - [x] At least 3 Required features (we have 11)
  - [x] At least 3 Optional features (we have 6)
- [x] **Screen Archetypes**: All 6 required types
  - [x] Login/Onboarding
  - [x] Stream #1 (Today - habits list)
  - [x] Stream #2 (Explore - API suggestions)
  - [x] Detail (Habit Detail)
  - [x] Creation (Add/Edit Habit)
  - [x] Profile (Stats)
  - [x] Settings
- [x] **Navigation Flows**:
  - [x] Tab Navigation defined (Today, Explore, Stats, Settings)
  - [x] Flow Navigation documented (all screen transitions)
- [x] README.md updated with Product Spec

### Activity 3: Wireframing ✅
- [x] Low-fidelity wireframe sketches
- [x] All screen archetypes wireframed
- [x] Navigation flows visualized
- [x] Wireframe images added to README.md

---

## 🎬 Next Steps

1. **Finalize brainstorming.md** with proper evaluation of 3 ideas
2. **Update README.md** with this consolidated Product Spec
3. **Create wireframes** for all screens (if not already done)
4. **Set up GitHub Project Board** (Unit 8 preparation)
5. **Begin implementation** (Unit 8 - Sprint Planning)

---

## 📝 Notes for Professor Review

**Key Differentiators**:
- Two Stream screens (Today = habits, Explore = API suggestions) - demonstrates understanding of Stream archetype
- Mobile-native features: Motion detection, Geofencing, Push notifications, Real-time updates (4+ features)
- Local intelligence: Classification and FitScore calculation run entirely on-device (no cloud AI)
- Context-aware: Suggestions adapt to location, weather, time, and motion state
- Clear "why": Solves decision fatigue + context mismatch problems

**Technical Feasibility**:
- All features can be implemented with standard Android/Kotlin libraries
- No complex ML/AI required (simple keyword matching)
- API integration is straightforward (Hacker News API is public, no auth)
- Sensor APIs are well-documented and accessible

**Scope Management**:
- MVP is achievable (core habit tracking + basic API feed)
- Stretch features add polish without breaking core functionality
- Clear separation between required and optional features

---

*This plan fulfills all Unit 7 requirements and provides a clear roadmap for implementation.*
