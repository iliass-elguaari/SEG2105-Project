# StudyHub – SEG2105 Fall 2026, Group 40

Android app (Kotlin + Jetpack Compose) for organising university study groups.

Deliverable 1 covers account registration, login, session management and role-based access.
Deliverable 2 adds administrator functionality, including user management and course category management.
Deliverable 3 adds courses and tasks, with courses and their tasks scoped to the owning student.

Repository: https://github.com/SEG2105-Garzon-uOttawa/seg2105f26-group_40_project

## Team

| Member | GitHub |
| --- | --- |
| Abderrahmane Rissafi | Abderrahmane Rissafi (commit author) |
| Nevan Vu | [nevanvu](https://github.com/nevanvu) |
| Wang Dai | [wang-kg](https://github.com/wang-kg) |
| Iliass El Guaari | [iliass-elguaari](https://github.com/iliass-elguaari) |

## Features

- **Sign up** as a Student or a Group Leader, with each field checked separately (rules below).
- **Log in** with a username and password. Accounts are stored in a local SQLite database (`StudyHub.db`).
- **Welcome screen** that shows the user's first name and role ("admin", "student" or "study group leader").
- **Session**: the app remembers who is logged in and opens on the welcome screen. **Sign Out** clears the session.
- **Admin user management**: administrators can view all registered users and disable, enable or delete user accounts. Administrators cannot disable, enable or delete the admin account. Passwords are never displayed.
- **Category management**: administrators can create, view, edit and delete course categories. Category names must be unique, and category names and descriptions are validated.
- **Courses and tasks**: courses are stored per student; each course has its own tasks, which are removed when the course is deleted.
- **Role-based access control**: user management and category management are restricted to administrators. Non-admin users cannot access these screens.

## Admin account

The admin account is seeded by `UserDatabase` during database creation or upgrade. Nobody can sign up as admin. The admin account cannot be disabled, enabled or deleted through user management.

| Username | Password |
| --- | --- |
| `admin` | `XPI76SZUqyCjVxgnUjm0` |

## Sign-up validation rules

Each field shows its own error message under the input (`ValidatedTextField`). The rules live in `util/Validator.kt`. `AuthManager.register` checks them again before saving.

| Field | Rule | Error shown |
| --- | --- | --- |
| First name / Last name | Required. Letters only, with single spaces, hyphens or apostrophes between letters (e.g. `Mary Jane`, `O'Neil`, `Jean-Luc`). Accented letters are allowed. | "… is required." / "Use letters, spaces, hyphens, or apostrophes only." |
| Email | Required. Must be a valid address (Android `Patterns.EMAIL_ADDRESS`). | "Email is required." / "Please enter a valid email address." |
| Username | Required. 3–20 characters, no spaces, unique. | "Username is required." / "Username must not contain spaces." / "Username must be between 3 and 20 characters." / "This username is already taken." |
| Password | 8+ characters with at least one uppercase letter, one lowercase letter, one number and one special character. | One message per missing requirement |
| Account type | Only Student (`student`) or Group Leader (`student_group_leader`). Admin cannot be chosen, and the role is rejected if it is anything else. | "Please select a valid account type." |

Notes on how the code behaves (checked against `Validator.kt` and on the emulator):

- The username's uniqueness is checked only after every other field passes, so a duplicate-username error appears on a second tap of **Create Account**.
- Uniqueness is case-sensitive: `Admin` and `admin` count as different usernames.
- Any character that is not a letter or a digit counts as a "special character", including a space.
- Names cannot start or end with a space, hyphen or apostrophe, and cannot contain two separators in a row.
- On the sign-up screen, only the radio circle selects the account type. Tapping the "Group Leader" text does nothing.
- The login screen only checks that both fields are filled in. A wrong username or password shows "Incorrect username or password."

## Project structure

```
app/src/main/java/
├── com/group40/studyhub_project/
│   ├── LoginActivity.kt        # launcher, LoginScreen
│   ├── SignUpActivity.kt       # registration form
│   ├── WelcomeActivity.kt      # WelcomeScreen, sign out, admin entry points
│   ├── UserListActivity.kt     # admin-only user management (LazyColumn)
│   ├── CategoryActivity.kt     # admin-only category management (CRUD)
│   ├── auth/AuthManager.kt     # register / login, wraps UserDatabase
│   ├── auth/SessionManager.kt  # logged-in user in SharedPreferences
│   ├── data/UserDatabase.kt    # SQLiteOpenHelper, users, categories, courses and tasks
│   ├── model/                  # User roles, Category, Course and Task
│   └── util/                   # Validator, ValidatedTextField
docs/
├── UML_Class_Diagram.puml      # PlantUML source
└── UML_Class_Diagram.pdf       # class diagram (Deliverable 3)
```

## Build and run

Requirements: Android Studio (its bundled JBR is enough as the JDK) and the Android SDK with API 37.

**Android Studio:** open the project folder, let Gradle sync, choose an emulator and press **Run**.

**Command line (macOS):**

```bash
export JAVA_HOME="/Applications/Android Studio.app/Contents/jbr/Contents/Home"
export ANDROID_HOME="$HOME/Library/Android/sdk"
./gradlew assembleDebug     # APK: app/build/outputs/apk/debug/app-debug.apk
./gradlew test              # unit tests
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

To regenerate the UML PDF after changing classes: `plantuml -tpdf docs/UML_Class_Diagram.puml`.
