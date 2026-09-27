# リリース

[English](../release.md) | 日本語

リリースはリリース用の PR をマージして行います。タグを手で push する必要はありません。

1. `main` から `release/vX.Y.Z` ブランチを作る
2. `gradle.properties` の `VERSION_NAME` を `X.Y.Z` に更新し、`🔖 vX.Y.Z` でコミットする
3. `🔖 Release vX.Y.Z` というタイトルで PR を作り、`main` にマージする
4. マージされると GitHub Actions（[`create-release-tag.yml`](../../.github/workflows/create-release-tag.yml)）が
   - ブランチ名からバージョンを取り出して `vX.Y.Z` タグを push する
   - Maven Central に publish して公開する（iOS ターゲットのため macOS で `./gradlew publishAndReleaseToMavenCentral`）
   - リリースノートを自動生成して GitHub Release を作る

1〜3 は Claude Code の `/release` コマンド（[`.claude/commands/release.md`](../../.claude/commands/release.md)）で自動化しています。

## Maven Central の設定

公開には [gradle-maven-publish-plugin](https://github.com/vanniktech/gradle-maven-publish-plugin) を使い、`gradle.properties` の `mavenCentralPublishing`・`signAllPublications`・`POM_*` で設定しています。
ワークフローはリポジトリの Secrets に次を要ります:

| Secret | 値 |
|---|---|
| `MAVEN_CENTRAL_USERNAME` | [Central Portal](https://central.sonatype.com) で作ったユーザートークンのユーザー名 |
| `MAVEN_CENTRAL_PASSWORD` | そのユーザートークンのパスワード |
| `SIGNING_IN_MEMORY_KEY` | ASCII 形式の GPG 秘密鍵（`gpg --export-secret-keys --armor <KEY_ID>`） |
| `SIGNING_IN_MEMORY_KEY_PASSWORD` | GPG 鍵のパスフレーズ |

Central Portal で `io.github.kwmt` の namespace を確認済みにし、GPG の公開鍵を鍵サーバー（`keys.openpgp.org` など）に上げておく必要があります。

署名せずに手元で成果物を確かめるときは SNAPSHOT で publish します: `./gradlew publishToMavenLocal -PVERSION_NAME=0.0.10-SNAPSHOT`
