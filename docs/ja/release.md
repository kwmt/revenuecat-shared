# リリース

[English](../release.md) | 日本語

リリースはリリース用の PR をマージして行います。タグを手で push する必要はありません。

1. `main` から `release/vX.Y.Z` ブランチを作る
2. `gradle.properties` の `VERSION_NAME` を `X.Y.Z` に更新し、`🔖 vX.Y.Z` でコミットする
3. `🔖 Release vX.Y.Z` というタイトルで PR を作り、`main` にマージする
4. マージされると GitHub Actions（[`create-release-tag.yml`](../../.github/workflows/create-release-tag.yml)）が
   - ブランチ名からバージョンを取り出して `vX.Y.Z` タグを push する
   - GitHub Packages に publish する（iOS ターゲットのため macOS で `./gradlew publish`）
   - リリースノートを自動生成して GitHub Release を作る

1〜3 は Claude Code の `/release` コマンド（[`.claude/commands/release.md`](../../.claude/commands/release.md)）で自動化しています。
