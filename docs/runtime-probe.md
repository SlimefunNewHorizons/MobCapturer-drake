# Isolated runtime probe

The probe checks the candidate on a real Paper 26.2 server with Slimefun, using a new world and no players. It verifies plugin enablement, at least 60 registered entity adapters, the cannon and pellet registrations, and two cow capture-data round trips through serialized ItemStacks. Each round trip checks health, name, scoreboard tags, potion effects, AI and maximum health; the second includes an attribute modifier.

Run with JDK 25 on a development host, through the host's build lock and resource limits:

```sh
python3 scripts/runtime_probe.py \
  --paper "$PAPER_JAR" \
  --paper-api "$PAPER_API_JAR" \
  --cache-dir "$PAPER_CACHE" \
  --libraries-dir "$PAPER_LIBRARIES" \
  --slimefun "$SLIMEFUN_JAR" \
  --candidate "$CANDIDATE_JAR" \
  --work-dir "$SCRATCH_DIRECTORY"
```

The runner copies the server dependencies and candidate into a fresh directory, binds an ephemeral port on loopback, disables addon auto-updates and metrics, and stops the server after the assertions. It never copies existing worlds, player databases or production configuration. Running it accepts the Minecraft EULA for this disposable instance.

Success requires a zero server exit code, `MOB_CAPTURER_DONE failures=0`, and no error or exception lines. The result records SHA-256 hashes of Paper, Slimefun and the candidate. Keep the printed scratch directory for `console.log` and `result.json`. A timeout requests a normal stop, then terminates only the fixture process if needed.

This probe validates registration and capture-data persistence. It does not exercise player interactions, protection integration, every entity adapter, inventory-bearing entities, or production performance. A successful result does not authorize production deployment.
