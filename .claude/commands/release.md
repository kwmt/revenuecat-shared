# Release Command

新しいバージョンをリリースする。

## 手順

### Phase 1: バージョン確認
1. `gradle.properties` の `VERSION_NAME` の現在のバージョンを確認する
2. 最新のgitタグ (`git tag --sort=-v:refname | head -1`) を確認する
3. ユーザーに次のリリースバージョンを確認する（現在のバージョンを提示し、patch/minor/majorの選択肢を出す）

### Phase 2: リリースブランチ作成 & PR
4. mainブランチから `release/v{VERSION}` ブランチを作成する
5. `gradle.properties` の `VERSION_NAME` を新しいバージョンに更新する
6. バージョン更新をコミットする（メッセージ: `🔖 v{VERSION}` ）
7. ブランチをリモートにプッシュする
8. PRを作成する（タイトル: `🔖 Release v{VERSION}`）
9. auto-mergeを有効化する（`gh pr merge --auto --merge`）

### Phase 3: タグ作成（PRマージ後）
10. PRがマージされたら、mainブランチに切り替えて最新を取得する
11. `v{VERSION}` のタグを作成してプッシュする（`git tag v{VERSION} && git push --tags`）
12. GitHub Actionsのpublishワークフローがタグプッシュで自動実行されることを伝える

## 注意事項
- mainブランチにいることを確認してから実行する
- mainブランチ以外にいる場合は警告を出してユーザーに確認する
- タグのフォーマットは `v` プレフィックス付き（例: `v0.2.0`）
- Phase 3はPRがマージされた後に実行する。マージ待ちの場合はPhase 2で終了し、マージ後に再度 `/release` を実行するよう案内する
