# Release

English | [日本語](ja/release.md)

Releases are made by merging a release PR. You don't push tags by hand.

1. From `main`, create a `release/vX.Y.Z` branch.
2. Update `VERSION_NAME` in `gradle.properties` to `X.Y.Z` and commit it as `🔖 vX.Y.Z`.
3. Open a PR titled `🔖 Release vX.Y.Z` and merge it into `main`.
4. On merge, GitHub Actions ([`create-release-tag.yml`](../.github/workflows/create-release-tag.yml))
   - reads the version from the branch name and pushes the `vX.Y.Z` tag,
   - publishes to Maven Central and releases it (`./gradlew publishAndReleaseToMavenCentral`, on macOS for the iOS targets),
   - creates a GitHub Release with generated notes.

Steps 1–3 are automated by the `/release` Claude Code command ([`.claude/commands/release.md`](../.claude/commands/release.md)).

## Maven Central setup

Publishing uses [gradle-maven-publish-plugin](https://github.com/vanniktech/gradle-maven-publish-plugin), configured with the `mavenCentralPublishing`, `signAllPublications`, and `POM_*` properties in `gradle.properties`.
The workflow needs these repository secrets:

| Secret | Value |
|---|---|
| `MAVEN_CENTRAL_USERNAME` | Username of a user token generated on [Central Portal](https://central.sonatype.com) |
| `MAVEN_CENTRAL_PASSWORD` | Password of that user token |
| `SIGNING_IN_MEMORY_KEY` | ASCII-armored GPG secret key (`gpg --export-secret-keys --armor <KEY_ID>`) |
| `SIGNING_IN_MEMORY_KEY_PASSWORD` | Passphrase of the GPG key |

The `io.github.kwmt` namespace must be verified on Central Portal, and the GPG public key must be uploaded to a key server (e.g. `keys.openpgp.org`).

To check the artifacts locally without signing, publish a SNAPSHOT: `./gradlew publishToMavenLocal -PVERSION_NAME=0.0.10-SNAPSHOT`.
