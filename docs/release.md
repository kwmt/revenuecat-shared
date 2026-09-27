# Release

English | [日本語](ja/release.md)

Releases are made by merging a release PR. You don't push tags by hand.

1. From `main`, create a `release/vX.Y.Z` branch.
2. Update `VERSION_NAME` in `gradle.properties` to `X.Y.Z` and commit it as `🔖 vX.Y.Z`.
3. Open a PR titled `🔖 Release vX.Y.Z` and merge it into `main`.
4. On merge, GitHub Actions ([`create-release-tag.yml`](../.github/workflows/create-release-tag.yml))
   - reads the version from the branch name and pushes the `vX.Y.Z` tag,
   - publishes to GitHub Packages (`./gradlew publish`, on macOS for the iOS targets),
   - creates a GitHub Release with generated notes.

Steps 1–3 are automated by the `/release` Claude Code command ([`.claude/commands/release.md`](../.claude/commands/release.md)).
