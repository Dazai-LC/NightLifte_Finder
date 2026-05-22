# NightLife Finder - PROJECT_SCOPE.md

## 1. Project Identity

Project name: NightLife Finder

Project type:
- Native Android App
- Android Studio project
- Java + XML
- Firebase backend

Main purpose:
NightLife Finder is an Android application that helps users find night food places, late-night stores, and nearby open places. The app focuses on simple, stable features suitable for a student project.

This project must prioritize:
- Simple architecture
- Stable build
- Clear UI flow
- Firebase integration
- Easy demo
- Minimal unnecessary complexity

---

## 2. Current Project Situation

The current project is not a brand-new project.

Important context:
- The project already has more than 50% of implementation.
- The project may already build successfully or almost successfully.
- Multiple people have worked on the project.
- Some UI screens may be duplicated, incomplete, inconsistent, or outside the official scope.
- Some files may be redundant or incorrectly connected.
- The goal is NOT to rebuild everything from scratch.

Main goal for the AI agent:
- Audit the existing project.
- Compare current code with this scope document.
- Keep useful existing files.
- Fix missing parts.
- Remove or disconnect unnecessary parts carefully.
- Standardize UI, Firebase, data model, package structure, and app flow.
- Make minimal changes whenever possible.

---

## 3. Mandatory Technology Stack

Use these technologies:

- Java
- XML Layout
- Android Studio
- Firebase Authentication
- Firebase Firestore
- Firebase Storage
- Firebase SDK for Android
- RecyclerView
- Glide
- Material Design components if already used or easy to add
- OSMDroid or existing map library in the project
- Android Location Services
- Google Maps Intent for directions

Important rule:
If the existing project already uses Java + XML, keep Java + XML.

Do not convert the project to Kotlin.

---

## 4. Technologies NOT Allowed

Do NOT add or migrate to:

- NodeJS backend
- MySQL server
- REST API backend
- Retrofit backend architecture
- Room database unless it already exists and is absolutely necessary
- Machine Learning
- AI recommendation
- Payment system
- Booking system
- Chat system
- Social feed
- Complex admin dashboard
- Complex review/rating system
- Realtime GPS tracking system
- Overcomplicated MVVM/Clean Architecture if the current project is simple

The project should stay simple and suitable for a student Android + Firebase demo.

---

## 5. Official Feature Scope

The app should include only these main feature groups:

### 5.1 Authentication

Required:
- Register account with email and password
- Login with email and password
- Logout
- Save basic user data to Firestore after successful registration

Optional only if already implemented simply:
- Forgot password

Not allowed:
- Google Login
- Facebook Login
- Phone number login
- Role-based complex admin login
- OAuth login system

---

### 5.2 Home / Places List

Required:
- Show list of night food places / late-night stores
- Search bar if already available or easy to implement
- Show place image
- Show place name
- Show address or short location
- Show category
- Show open time
- Show distance if GPS/location calculation already exists or easy to connect

The Home screen should use RecyclerView.

Home item click:
- Open Place Detail screen

Not allowed:
- Booking button
- Payment button
- Review/comment system
- Complex rating system
- AI recommendation feed

---

### 5.3 Place Detail

Required:
- Show place name
- Show image
- Show address
- Show category
- Show open time
- Show description if the field already exists
- Show latitude/longitude only if useful for debug or map integration
- Favorite button
- Direction button

Direction button:
- Open Google Maps using Intent
- Do not implement complex route rendering inside the app

Not allowed:
- Booking
- Payment
- Review/comment
- Complex rating
- Chat with owner

---

### 5.4 Favorite Places

Required:
- User can add a place to favorites
- User can remove a place from favorites
- Favorite screen shows saved places
- Favorite data is stored under the current user in Firestore

Recommended data approach:
- Store favorite place IDs in `users/{uid}.favorites`

Do not create a separate complex favorite system unless the current project already depends on it.

---

### 5.5 Map / GPS

Required:
- Request location permission
- Show map
- Show user's current location if permission is granted
- Load places from Firestore
- Show place markers on the map
- Marker click shows basic place information
- Direction action opens Google Maps Intent

Allowed:
- OSMDroid
- Existing map library already present in the project
- Android Location Services

Not allowed:
- Paid map API requirement
- Complex in-app route drawing
- Continuous realtime tracking
- Background tracking unless already required and simple
- AI nearby recommendation

---

### 5.6 Profile

Required simple profile features:
- Show current user's email
- Logout button

Optional:
- Show username if already implemented

Not allowed:
- Complex user profile
- Avatar upload unless already implemented simply
- Social profile system

---

## 6. Official App Flow

The app should follow this flow:

```text
LoginActivity
    -> RegisterActivity if user needs to register
    -> MainActivity after successful login

MainActivity
    -> Bottom Navigation

Bottom Navigation tabs:
    1. Home
    2. Map
    3. Favorite
    4. Profile

Home item click:
    -> PlaceDetailActivity

PlaceDetailActivity:
    -> Favorite / Unfavorite
    -> Open Google Maps Intent for directions