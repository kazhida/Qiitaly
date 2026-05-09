# Qiitaly

Qiitaly は、Qiita の記事を Android / iOS で読むための Kotlin Multiplatform / Compose Multiplatform アプリです。

Qiita OAuth でログインし、取得したアクセストークンを端末内に保存します。認証済みユーザーのフォロー中ユーザー、フォロー中タグ、または任意の Qiita 検索クエリを使って記事タイムラインを表示します。

## 主な機能

- 共通の Compose Multiplatform UI による Android / iOS 対応
- Qiita OAuth ログイン (`read_qiita` scope)
- ページング対応の記事タイムライン
- Pull-to-refresh
- フォロー中ユーザーの記事フィルタ
- フォロー中タグの記事フィルタ
- 任意の Qiita 記事検索クエリ
- 記事詳細画面
- サードパーティライセンス画面

## 技術スタック

- Kotlin Multiplatform
- Compose Multiplatform / Material 3
- Ktor Client
- kotlinx.serialization
- Coil 3
- Kotlin Multiplatform OIDC
- AndroidX Navigation Compose
- Hilt / Firebase Analytics (Android)

## ディレクトリ構成

```text
.
├── composeApp/
│   └── src/
│       ├── commonMain/      # 共通のモデル、Repository、UIコンポーネント、画面
│       ├── commonTest/      # 共通ユニットテスト
│       ├── androidMain/     # AndroidエントリポイントとAndroid固有実装
│       ├── androidUnitTest/ # Android向けユニットテスト
│       └── iosMain/         # iOSエントリポイントとiOS固有実装
├── iosApp/                  # XcodeプロジェクトとSwiftUIホストアプリ
├── gradle/                  # Gradle WrapperとVersion Catalog
└── build.gradle.kts
```

## 必要な環境

- JDK 11 以上
- Android Studio または IntelliJ IDEA
- Android SDK 36
- Xcode (iOSビルド時)
- Qiita OAuth アプリケーション

Qiita OAuth アプリケーションのコールバックURLには以下を設定します。

```text
qiitare://oauth/callback
```

## セットアップ

リポジトリを取得します。

```shell
git clone <repository-url>
cd Qiitaly
```

Android Studio または IntelliJ IDEA でプロジェクトを開き、Gradle Sync を実行します。

iOS を実機で動かす場合など、署名チームが必要な場合は `iosApp/Configuration/Config.xcconfig` の `TEAM_ID` を設定します。

```xcconfig
TEAM_ID=XXXXXXXXXX
```

Qiita OAuth の設定は、現在 `composeApp/src/commonMain/kotlin/com/abplus/qiitaly/OAuthConfig.kt` に定義されています。
本番運用では、クライアントシークレットをソースコードに直接置かず、各プラットフォームの安全な設定経路から注入する形にしてください。

## Androidで起動

Debug APK をビルドします。

```shell
./gradlew :composeApp:assembleDebug
```

接続済みの端末またはエミュレータにインストールします。

```shell
./gradlew :composeApp:installDebug
```

IDE の Android 実行構成から起動することもできます。

## iOSで起動

Xcode プロジェクトを開きます。

```shell
open iosApp/iosApp.xcodeproj
```

`iosApp` scheme を選択し、シミュレータまたは実機で実行します。

Kotlin Framework は Xcode 連携用の Gradle タスク `:composeApp:embedAndSignAppleFrameworkForXcode` によりビルド・埋め込みされます。

## iOSをFirebase App Distributionへ配布

Firebase CLI にログインします。

```shell
npm install -g firebase-tools
firebase login
```

テスターのメールアドレス、または Firebase App Distribution のグループを指定してビルドとアップロードを実行します。

```shell
FIREBASE_TESTERS=tester@example.com ./scripts/ios-appdistribution.sh
```

```shell
FIREBASE_GROUPS=qa-team ./scripts/ios-appdistribution.sh
```

Firebase の iOS App ID は `iosApp/iosApp/GoogleService-Info.plist` の `GOOGLE_APP_ID` から読み取ります。デフォルトの書き出し方式は `iosApp/Configuration/ExportOptions.plist` で `development` にしています。Ad Hoc 配布に切り替える場合は `method` を `ad-hoc` に変更し、Apple Developer で対象端末を含む Provisioning Profile を用意してください。

## テスト

共通テストと Android ユニットテストを実行します。

```shell
./gradlew :composeApp:test
```

利用可能なチェックをまとめて実行します。

```shell
./gradlew :composeApp:check
```

iOS Simulator 向け Kotlin テストを実行します。

```shell
./gradlew :composeApp:iosSimulatorArm64Test
```

OAuth のトークン交換を実際に行う Android ユニットテストもあります。実行には Qiita から返された新しい認可コードが必要です。

```shell
QIITA_AUTH_CODE=<code> ./gradlew :composeApp:testDebugUnitTest --tests '*AuthRepositoryLiveTest*'
```

## 補足

- Qiita API v2 (`https://qiita.com/api/v2`) を利用します。
- アクセストークンはプラットフォーム別の `QiitaTokenPreferences` 実装で端末内に保存します。
- iOS の URL scheme `qiitare` は `iosApp/iosApp/Info.plist` に登録されています。
- サードパーティライセンスは `composeApp/src/commonMain/composeResources/files/` に配置されています。
