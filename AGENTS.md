# Protected Repository Files & Operational Rules

## 🔒 Protected Files (STRICTLY PRESERVED)

The following files are **CRITICAL** and must **NEVER** be deleted, modified, overwritten, excluded, or regenerated in any future change to this repository, unless explicitly requested by the user by name:

1. **`debug.keystore`** (Root: `/debug.keystore`)
   - **Purpose:** Android debug signing keystore used by `build.gradle.kts` (`signingConfigs.debugConfig`).
   - **Rule:** Preserves signature consistency across APK installations on test devices. Under NO circumstances should this binary file be deleted, replaced, or regenerated.
2. **`debug.keystore.base64`** (Root: `/debug.keystore.base64`)
   - **Purpose:** Base64 reference backup of the committed `debug.keystore`.
   - **Rule:** Must be preserved unchanged.

## ⚙️ Build and Signing Rules
- Never remove the `debugConfig` signing configuration in `app/build.gradle.kts` pointing to `${rootDir}/debug.keystore`.
- All builds and commits must ensure `debug.keystore` is present and unchanged.
