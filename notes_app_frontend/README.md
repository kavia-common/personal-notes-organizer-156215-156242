# Personal Notes App (Android, Kotlin - Traditional Views)

A modern, light-themed notes app built with traditional Android Views (no Jetpack Compose). It uses a bottom navigation with three tabs (Home, Search, Categories), a right-side drawer for note details, and a floating action button to add notes. Notes are stored locally using SQLite via a small repository layer.

Features:
- Create, edit, delete notes
- Organize notes by categories
- Search notes (title and content)
- Share notes with other apps
- Modern light theme with specified colors

Build:
- ./gradlew build

Install & run:
- :app:installDebug
- Launch "Personal Notes" on your device.

Tech highlights:
- AndroidX AppCompat/Fragments
- Material Components (BottomNavigationView, MaterialCardView)
- RecyclerView lists
- DrawerLayout-based right-side detail panel
- SQLiteOpenHelper-backed persistence (no Room to keep it lightweight)