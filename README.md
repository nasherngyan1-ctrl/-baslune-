# BASLUNE OS — Business Operating System (Android)

**BASLUNE OS** is the native Android Business Operating System for **BASLUNE Studio** (*Your Vision. Our Digital Craft.* — Erbil, Kurdistan Region, Iraq).

It is built natively in **Kotlin + Jetpack Compose (Material Design 3)** with a local-first **Room (SQLite)** database and **Jetpack DataStore** preferences, supporting **Central Kurdish (Sorani, RTL)**, **Arabic (RTL)**, and **English (LTR)**.

> **Architecture Note:** Because this codebase is already a native Android Kotlin/Jetpack Compose application (not a React/Vite web bundle), it builds directly into an Android APK with Gradle—no Capacitor web wrapper or Node.js runtime is required.

---

## Application Identity

- **App Name:** `BASLUNE OS`
- **Package / Application ID:** `com.baslune.os`
- **Minimum Android SDK:** `24` (Android 7.0)
- **Target / Compile SDK:** `36`
- **Languages & Direction:** Central Kurdish (Sorani, default RTL), Arabic (RTL), English (LTR)

---

## Core Modules

- **Dashboard:** Executive financial KPIs (IQD & USD kept separated), action center, charts, and recently updated projects.
- **Clients & CRM:** Client profiles, status, classification, and communication history.
- **Projects:** Cards, List, Kanban, and Calendar views; full create/edit/delete with confirmation, milestones, linked task progress, profitability, and internal notes.
- **Tasks:** Priority, deadlines, recurring tasks, and automatic project progress recalculation.
- **Services & Sales Pipeline:** Service catalog and opportunity conversion to clients & projects.
- **Finance & Invoices:** Multi-currency (IQD/USD) transactions, invoices, line items, payments, and cash-flow summaries.
- **Marketing, Documents, Knowledge & Reports:** Campaigns, content calendar, document links, SOP articles, and analytics/CSV export.
- **Team & RBAC:** Role-based access control (`OWNER`, `STAFF`, `FINANCE`, etc.).
- **Backup Center:** Full and selective JSON backup/restore via Android Storage Access Framework (`CreateDocument` / `OpenDocument`).

---

## Repository Structure

```text
.
├── .github/
│   └── workflows/
│       ├── android-apk.yml        # Automated CI test + Debug APK build
│       └── android-release.yml    # Manual CI Signed Release APK build (uses Secrets)
├── app/
│   ├── build.gradle.kts           # Android app module config (applicationId: com.baslune.os)
│   ├── proguard-rules.pro
│   └── src/
│       ├── main/
│       │   ├── AndroidManifest.xml
│       │   ├── java/com/example/
│       │   │   ├── BasluneApp.kt
│       │   │   ├── MainActivity.kt
│       │   │   ├── core/          # Design system, localization (ckb/ar/en), RBAC, formatters
│       │   │   ├── data/          # Room database, DAOs, DataStore, repositories, BackupManager
│       │   │   ├── domain/        # Models, financial & analytics engines, notifications
│       │   │   └── ui/            # Navigation shell, Compose screens, ViewModel, Theme
│       │   └── res/               # Adaptive icon, splash theme, bundled fonts, XML backup rules
│       └── test/                  # JVM unit tests & Robolectric Compose UI tests
├── gradle/
│   ├── libs.versions.toml         # Centralized Gradle version catalog
│   └── wrapper/
│       └── gradle-wrapper.properties
├── build.gradle.kts
├── gradle.properties
├── settings.gradle.kts
├── .env.example
├── .gitignore
└── README.md
```

---

## Local Build & Test Setup

### Prerequisites
- **JDK 21** (Temurin / OpenJDK 21 — required for Robolectric Android SDK 36 tests, AGP 9.1.1, and Gradle 9.3.1)
- **Android SDK** (`compileSdk 36`, Android Build-Tools)
- **Gradle 9.3.1** (or Android Studio Narwhal+ / Ladybug+ with Gradle 9.3.1)

### 1. Create a Local Debug Keystore (if not present)
`app/build.gradle.kts` signs `debug` builds using `debug.keystore` in the repository root (which is excluded from git via `.gitignore`):

```bash
keytool -genkeypair -v \
  -keystore debug.keystore \
  -storepass android \
  -alias androiddebugkey \
  -keypass android \
  -keyalg RSA \
  -keysize 2048 \
  -validity 10000 \
  -dname "CN=Android Debug,O=Android,C=US"
```

### 2. Run Unit & Robolectric Tests
```bash
gradle :app:testDebugUnitTest
```

### 3. Build Installable Debug APK
```bash
gradle :app:assembleDebug
```
Output APK:
```text
app/build/outputs/apk/debug/app-debug.apk
```

---

## Automated APK Build with GitHub Actions

1. Export or push this repository to GitHub (`main` branch).
2. Open your GitHub repository → **Actions** tab.
3. Select **Build Android Debug APK** (`.github/workflows/android-apk.yml`).
   - Runs automatically on every push to `main`, or manually via **Run workflow**.
4. Once the workflow succeeds, open the completed run and download the **`baslune-os-debug-apk`** artifact (`app-debug.apk`).

---

## Signed Release APK Preparation

Never commit keystores (`*.jks`, `*.keystore`) or passwords into GitHub. `app/build.gradle.kts` is pre-configured to read release signing credentials from environment variables (`KEYSTORE_PATH`, `STORE_PASSWORD`, `KEY_PASSWORD`, with `keyAlias = "upload"`).

### 1. Generate a Release Keystore Locally
```bash
keytool -genkeypair -v \
  -keystore release-keystore.jks \
  -alias upload \
  -keyalg RSA \
  -keysize 2048 \
  -validity 10000
```
Store `release-keystore.jks` and your passwords securely outside git (e.g., password manager / encrypted vault).

### 2. Encode the Keystore for GitHub Secrets
```bash
base64 -w 0 release-keystore.jks
```
(On macOS, use `base64 -i release-keystore.jks | tr -d '\n'`.)

### 3. Add GitHub Repository Secrets
In GitHub: **Settings → Secrets and variables → Actions → New repository secret**:
- `RELEASE_KEYSTORE_BASE64`: Base64-encoded contents of `release-keystore.jks`
- `STORE_PASSWORD`: Your keystore password
- `KEY_PASSWORD`: Your `upload` key password

### 4. Run the Release Workflow
Open **Actions → Build Signed Release APK** (`.github/workflows/android-release.yml`) and click **Run workflow**. The signed APK is uploaded as **`baslune-os-release-apk`**.

---

## Data Safety, Persistence & Backup Limitations

- **Local-First Persistence:** All business records are stored on-device in Room (`baslune_os.db`, version `1` with no `fallbackToDestructiveMigration`) and Jetpack DataStore (`baslune_settings`). App restarts do not reset data.
- **Export & Restore:** Use **Backup Center** inside the app to export full or module-filtered `.json` backups via Android's Storage Access Framework (`CreateDocument`) and restore them (`OpenDocument`) with explicit confirmation before overwriting existing records.
- **Important Limitations:**
  - Local SQLite/DataStore storage does **not** automatically sync across multiple devices or teams.
  - Uninstalling the app permanently removes on-device storage unless the user has exported a `.json` backup file beforehand or Android Auto Backup (`allowBackup="true"`) restores device data via the user's Google account (subject to Android OS backup limits).
  - Always export a `.json` backup from **Backup Center** before uninstalling, changing devices, or switching between debug and release signing keys (Android blocks upgrading an APK in-place if the signing certificate changes).
