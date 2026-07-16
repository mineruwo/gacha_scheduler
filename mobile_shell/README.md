# Gacha Scheduler 모바일 앱 셸 (Capacitor)

React 웹([../frontend](../frontend))을 WebView로 감싸 스토어에 배포하는 하이브리드 앱 셸.
**모바일 화면을 따로 만들지 않고, 웹을 배포하면 앱 컨텐츠도 함께 갱신되는 구조**를 위해 존재한다.

기존 Flutter 앱([../frontend_mobile](../frontend_mobile))은 추후 네이티브 전환 대비용으로 보관 중이며, 현재 모바일 배포 경로는 이 셸이다.

## 요구 사항

- Node 20+
- Android Studio (내장 JDK 21 사용 — 별도 JDK 설치 불필요)

## 개발 워크플로

```bash
npm install              # 최초 1회
npm run sync             # 웹 빌드 → www/ 복사 → android 프로젝트에 동기화
npm run open             # Android Studio로 android/ 열기 (또는 직접 열기)
```

Android Studio에서 `android/` 폴더를 열고 Run 하면 에뮬레이터/실기기에서 실행된다.

터미널에서 직접 빌드하려면 (Android Studio 내장 JDK 사용):

```bash
cd android
JAVA_HOME="/Applications/Android Studio.app/Contents/jbr/Contents/Home" \
ANDROID_HOME=$HOME/Library/Android/sdk ./gradlew assembleDebug
# → app/build/outputs/apk/debug/app-debug.apk
```

## 컨텐츠 로딩 모드 2가지

### 1) 번들 모드 (현재 기본)

`npm run sync` 시점의 웹 빌드(`www/`)를 앱에 내장한다. 오프라인 첫 화면이 가능하지만,
웹이 바뀌면 앱도 다시 빌드/배포해야 한다.

### 2) 원격 URL 모드 ("컨텐츠 재배포" 모델)

웹을 서버에 배포한 뒤 `capacitor.config.json`에 주소를 지정하면, 앱은 항상 배포된 웹을 로드한다.
**웹만 재배포하면 앱 컨텐츠가 즉시 갱신**되며 스토어 재심사가 필요 없다. 웹 배포 주소가 정해지면 이 모드로 전환할 것.

```json
{
  "appId": "com.gacha.scheduler",
  "appName": "Gacha Scheduler",
  "webDir": "www",
  "server": {
    "url": "https://<웹 배포 주소>"
  }
}
```

변경 후 `npx cap sync android` 실행.

## 주의사항

- **백엔드 CORS**: 번들 모드의 WebView origin은 `https://localhost`(Capacitor 기본)라서,
  백엔드가 이 origin(그리고 웹 배포 origin)을 CORS 허용해야 API 호출이 된다. → docs/SYNC.md 요청 참고
- **로컬 백엔드 접속**: 에뮬레이터에서 호스트 PC의 백엔드는 `http://10.0.2.2:8080`.
  frontend를 `VITE_API_BASE_URL=http://10.0.2.2:8080`으로 빌드해서 sync 할 것.
  http(비HTTPS) 개발 서버를 쓰려면 `capacitor.config.json`의 `server.cleartext: true` 필요
- **스토어 심사**: 단순 WebView 래핑은 거절될 수 있으므로, 출시 전 푸시 알림(@capacitor/push-notifications),
  네이티브 스플래시(@capacitor/splash-screen) 등 앱다운 요소를 추가할 것
