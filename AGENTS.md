# Working in this repository

## Vendored code: `com.google.*`

Everything under `src/main/java/com/google/` (and any test counterpart
under `src/test/java/com/google/`) is vendored third-party code, not
code owned by this project:

- `com.google.gerrit.*` is a copy of `com.google.gerrit.extensions` kept
  here because not all extensions to that API have been merged into the
  Gerrit repository yet (see the README's "Usage" section).
- `com.google.gwtorm.*` is likewise vendored from a third-party project
  (note the "Copyright Google Inc." header), not written by this
  project's author.

Do not modify these files for style, lint, or SonarCloud clean-up
purposes. Only touch them to:
- keep them in sync with an upstream change, or
- add a new extension that isn't merged upstream yet.

Everything under `com.urswolfer.*` is this project's own code and is
fair game for normal fixes and cleanup.
