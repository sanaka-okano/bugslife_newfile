# tax 設計

## 概要

税率の管理を行う

### アクション

- 一覧（登録・参照・削除）
- 照会
- 作成
- 削除

### 要件

- 税率はID、税率、税込みか否か、丸目を持つ
- 登録ページ
- 登録処理を行う
  　∟数値を登録
  　∟登録と同時に、その数値の６パターンの数値を作成登録
  　∟登録済みの数値は登録できない
  　∟一覧に遷移
- 一覧
　 ∟同じ数値のものは一つだけ表示
- 参照
- 削除ボタン
　 ∟使用しているIDを削除できないこと

## モデル

- TaxType

| ID  | Rate(%) | Tax included | Rounding |
| --- | ------- | ------------ | -------- |
| 1   | 0       | No           | Floor    |
| 2   | 0       | No           | Round    |
| 3   | 0       | No           | Ceil     |
| 4   | 0       | Yes          | Floor    |
| 5   | 0       | Yes          | Round    |
| 6   | 0       | Yes          | Ceil     |
| 7   | 8       | No           | Floor    |
| 8   | 8       | No           | Round    |
| 9   | 8       | No           | Ceil     |
| 10  | 8       | Yes          | Floor    |
| 11  | 8       | Yes          | Round    |
| 12  | 8       | Yes          | Ceil     |
| 13  | 10      | No           | Floor    |
| 14  | 10      | No           | Round    |
| 15  | 10      | No           | Ceil     |
| 16  | 10      | Yes          | Floor    |
| 17  | 10      | Yes          | Round    |
| 18  | 10      | Yes          | Ceil     |
