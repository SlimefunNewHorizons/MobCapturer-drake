# Proactive work log

## 2026-11-26 — Ticket 461: isolated candidate validation

- Candidate source: `b5394f46e01fc600e2df39ffd278722c3f0f7416`, branch `port-26x`.
- Candidate SHA-256: `4db715d7146c5d20472d521f1933e8f6c44853142a760b466abc0461ecdf5397`.
- Java 25 `mvn clean package`, executed through the host build guard: passed in 11.569 seconds. No Maven test sources existed.
- Contract ABI gate: passed, 775 classes scanned, no violations.
- Added the reusable [runtime probe](../runtime-probe.md), kept outside the production Maven source set.
- Final isolated run: Paper 26.2-133, Java 25, Slimefun SHA-256 `d4f62c5ff700bf6c0f8606f03974ee3d697f0792883fa35d27dc1e1c8d298dcc`; exit 0 in 82.01 seconds, 18 assertions passed, 76 adapters registered, zero error/exception lines. The server stopped normally after testing both capture-data round trips.
- The initial exploratory fixture emitted a flat-world generator configuration error. Explicit terrain layers corrected the fixture; the final reusable probe passed without that error.
- Read-only production comparison: live SHA-256 `92bf372012f4ca12cfa3c3261b3c15d0d46ccd3a05b6da3a2367b431477e6193` matches the candidate manifest's baseline. The candidate differs in 139 archive entries, with no changed classes under the MobCapturer package. No pending MobCapturer update was present.
- Deployment remains on hold: no MobCapturer error or warning requiring a production change was established. The current production policy permits changes for errors, warnings or security fixes. Successful candidate validation alone is insufficient.
- No production upload, configuration edit or restart occurred. No rollback was needed. Revert the test commit to remove the probe; disposable worlds contain no player data.
- Remaining coverage: player interaction and protection checks, inventory-bearing entities, and the other entity adapters.
