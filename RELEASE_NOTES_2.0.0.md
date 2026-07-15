# KeymapKit 2.0.0

KeymapKit 2.0 introduces selective physical keyboard layout installation.

Instead of permanently exposing every bundled layout to Android, KeymapKit now asks which layouts are needed and enables only those provider components. Layouts can be added or removed later from the new searchable manager.

## Highlights

- First-run layout selection
- Locale-aware recommendations
- Search and script categories
- Add or remove layouts at any time
- Physical keyboard diagnostics
- Direct shortcut to Android physical keyboard settings
- Layout test field
- Existing-user migration protection
- Fully offline operation with no permissions, accounts, analytics, or ads

## Upgrade note

Existing users keep access to the legacy complete layout set until they open KeymapKit and confirm a selection. After confirmation, only selected layouts remain visible in Android settings.

Removing a layout currently assigned to a physical keyboard may cause Android to fall back to its default mapping. Re-open Physical keyboard settings after changes.
