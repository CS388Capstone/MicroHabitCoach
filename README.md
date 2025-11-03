
---

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

Ultra-low-friction habit app with **Android-native intelligence**. Users create habits (unlimited, sane defaults shown). The app uses **Activity Recognition** (movement), **Geofencing** (place-based nudges), and **dynamic notifications** (streak-loss countdown) so tasks happen at the **right time and place**, not just at 9:00 PM. One tap to complete; streaks and history keep you honest.

### App Evaluation

**Category:** Productivity / Health & Fitness
**Mobile:** Very high — **Activity Recognition**, **Geofencing**, **WorkManager** notifications, optional **AppWidget**; clearly beyond a website.
**Story:** “It nags you cleverly until you actually do the thing.” Demo shows sensors, geo, and countdowns in action.
**Market:** Broad (students/pros/athletes).
**Habit:** Daily by design; streaks + countdowns + context create repeat use.
**Scope:** MVP is focused but native: Room + sensors + geofence + notifications. Extras are additive, not required.

---

## Product Spec

### 1. User Features (Required and Optional)

#### Required Features (MVP)

* **Account & Onboarding**

  * Create account; choose from **smart templates** (e.g., *Hydrate at Gym*, *Move Break*, *Walk 10 min*, *Read 10 pages*) or create a custom habit.
  * No hard limit on habits; **soft guidance** to 3–6 for clarity (UI stays performant).

* **Today (Mark Complete)**

  * See today’s habits and status; **one-tap** completion; progress/streak updates immediately.

* **Scheduled & Dynamic Notifications**

  * **Time-based** reminders via WorkManager **plus** a **streak-loss countdown** (“You’ve got 2 hours to save your streak.”).

* **Activity Recognition Assist (base)**

  * Movement-type habits (e.g., *Walk 10 min*, *Move Break*) **auto-complete** when device reports sufficient activity or the user is no longer sedentary for N minutes.

* **Geofenced Nudges (base)**

  * Place-based habits (e.g., *Hydrate at Gym*, *Stretch at Home*) trigger on **geofence ENTER** events.

* **Habit Detail & History**

  * View streaks, 7/30-day completion, calendar/list history.

* **Create/Edit/Delete Habit**

  * Configure **habit type** (Time / Movement / Location), target (count/min/pages), reminder rules, and (for location) choose a place. Enforce sane notification caps.

* **Settings**

  * Global notifications toggle, **Quiet Hours**, sensor/geo permission status, battery-friendly mode.

#### Optional Features (Stretch)

* **Home-Screen Widget**

  * Remaining habits; tap to complete from widget.
* **Lite Social Accountability**

  * Share a single habit’s streak with a friend (read-only).
* **Cloud Backup/Sync**

  * Firebase sync of Room data.
* **Multi-place geofences & advanced rules**

  * E.g., gym A or B; “only trigger once per 6 hours.”

---

### 2. Screen Archetypes

* **Login / Onboarding**

  * Auth; pick smart templates; grant **Activity Recognition** and **Location** permissions if selecting those habit types.

* **Today (Stream)**

  * List of today’s habits with status, progress, and quick actions (Mark Done).

* **Add / Edit Habit (Creation)**

  * **Type selector**: *Time-based*, *Movement-based*, *Location-based*
  * Fields adapt by type:

    * Time: name, target, reminder time(s)
    * Movement: name, target minutes/steps, **motion threshold**
    * Location: name, target (e.g., “1 glass”), **pick place (map/search)**, optional photo cue

* **Habit Detail**

  * Streak, history (7/30 days), calendar/list; edit/delete.

* **Profile / Stats**

  * Completion rate, longest streak, days active; breakdown by habit type.

* **Settings**

  * Global notification toggle, **Quiet Hours**, permissions status (motion/location), battery mode, data reset.

---

### 3. Navigation

#### Tab Navigation (Tab to Screen)

* **Today**
* **Add** (or FAB if using 3 tabs)
* **Stats**
* **Settings** *(can live under Stats overflow if 3 tabs)*

#### Flow Navigation (Screen to Screen)

* **Login/Onboarding → Today**
* **Today → Habit Detail**
* **Today → Add/Edit Habit**
* **Habit Detail → Edit Habit**
* **Stats → Habit Detail**
* **Settings → System permission screens** (Notifications/Activity/Location)

---

## Wireframes

*(Add hand-sketched images here in Activity 3.)*

* Today (list + big “Mark Done”)
* Add/Edit (type selector → dynamic form)
* Habit Detail (streak + history)
* Stats (completion %, longest streak)
* Settings (quiet hours, permissions)
* Onboarding (template picker + permission prompts)

---

****
