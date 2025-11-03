---

# Mobile App Dev – App Brainstorming

## Favorite Existing Apps – List

1. Notion
2. Apple Fitness
3. Duolingo
4. Life360
5. Google Calendar
6. Spotify
7. Robinhood
8. Headspace
9. Strava
10. WhatsApp

---

## Favorite Existing Apps – Categorize and Evaluate

### Notion

* **Category:** Productivity / Organization
* **Mobile:** Works seamlessly across devices but really shines on desktop; mobile version uses offline caching and local notifications.
* **Story:** The idea of a single space for notes, databases, and tasks—customized entirely by the user—creates deep engagement.
* **Market:** Professionals, students, and productivity enthusiasts.
* **Habit:** Extremely habit-forming due to daily journaling, task management, and personal dashboards.
* **Scope:** Originally a note app, now a full ecosystem; shows how a small MVP can evolve.

### Duolingo

* **Category:** Education / Gamification
* **Mobile:** Designed for bite-sized on-the-go use; push notifications, haptics, and streaks make it purely mobile.
* **Story:** Learn languages in minutes a day through repetition and rewards.
* **Market:** Anyone interested in learning a language; global.
* **Habit:** Daily streaks and notifications drive repeat engagement.
* **Scope:** Simple loop (lesson + XP + streak) scaled with social and content depth.

---

## New App Ideas – List

1. **Micro-Habit Coach** – An ultra-low-friction habit tracker that uses sensors and geofencing to make habits automatic.
2. **Food Budget Splitter** – Smart expense tracker for roommates to automatically split grocery and meal costs.
3. **Spots** – Personal map for saving and categorizing your favorite places.
4. **Mood x Energy Journal** – Daily reflection on mood, energy, and performance correlations.
5. **LineWatch** – Real-time wait time tracker for popular venues.
6. **Check Me** – Outfit and wardrobe log that helps users plan looks and avoid repeats.

---

## Top 3 New App Ideas

1. **Micro-Habit Coach**
2. **Food Budget Splitter**
3. **Spots**

---

## New App Ideas – Evaluate and Categorize

### 1. Micro-Habit Coach

* **Description:** Tracks small daily habits using **Activity Recognition** (movement detection) and **Geofencing** (location-based nudges). Sends dynamic notifications like “2 hours left to save your streak.” Auto-completes tasks such as “Walk 10 min” or “Hydrate at Gym.”
* **Category:** Health & Productivity
* **Mobile:** Deeply mobile; uses Android sensors (motion, GPS, notifications, background jobs). Wouldn’t make sense on desktop.
* **Story:** Simplifies consistency. Instead of reminders you ignore, it *detects* when you’ve done the thing. Visual streaks make progress tangible.
* **Market:** Students, professionals, and gym-goers aiming to stay consistent.
* **Habit:** Opens daily by nature. Streak-loss countdown and smart nudges ensure engagement.
* **Scope:** Clear MVP—Room database, Firebase Auth, Activity Recognition, Geofencing, Notifications.
* **AI opportunity:** future predictive habit reminders (“You usually do pushups at 6pm—ready?”).

---

### 2. Food Budget Splitter

* **Description:** Tracks shared food purchases among roommates or partners. Snap a receipt, tag who participated, and the app auto-calculates debts and balances.
* **Category:** Finance / Lifestyle
* **Mobile:** Uses **camera intents**, **push notifications**, and **local storage**.
* **Story:** Everyone’s had roommate money tension. This app automates fairness.
* **Market:** College students, young couples, friend groups.
* **Habit:** Used every time a group purchase happens—recurring weekly.
* **Scope:** Simple CRUD app with math logic and potential Firestore sync.

---

### 3. Spots

* **Description:** Your personal map of favorite restaurants, viewpoints, and date spots. Attach photos, tags, and notes, then filter by vibe.
* **Category:** Travel / Lifestyle
* **Mobile:** Uses **GPS**, **camera**, and **Google Maps SDK**.
* **Story:** Everyone forgets where that one great ramen place was—this fixes that.
* **Market:** City explorers, travelers, couples.
* **Habit:** Used when exploring or planning outings.
* **Scope:** Manageable MVP—map view, CRUD spots, attach photos, basic filters.

---

## Final Decision

**Chosen App:** **Micro-Habit Coach**
**Reason:**
It’s the most *Android-native*, implementable within semester scope, and clearly demonstrates contextual awareness (motion, geo, and notifications). It also has wide appeal and a clean technical story for presentation.

---

Would you like me to add a short **“Favorite Existing Apps – List & Evaluation”** section for 2–3 more apps (like Google Fit or Headspace) to pad the file for completeness before submission? It would make it look more polished for grading.
