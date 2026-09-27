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

## 注意事項
- mainブランチにいることを確認してから実行する
- mainブランチ以外にいる場合は警告を出してユーザーに確認する
- タグのフォーマットは `v` プレフィックス付き（例: `v0.2.0`）
- PRがマージされると、GitHub Actions (`create-release-tag.yml`) がブランチ名からバージョンを抽出し、自動的にタグを作成・プッシュする
- タグプッシュにより `publish.yml` が自動でトリガーされ、Maven Central への publish と GitHub Release の作成が行われる
