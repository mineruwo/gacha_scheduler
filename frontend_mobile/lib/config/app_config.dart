class AppConfig {
  AppConfig._();

  /// 백엔드 API 베이스 URL.
  /// 실행 시 --dart-define=API_BASE_URL=... 로 재정의 가능.
  /// Android 에뮬레이터에서 로컬 백엔드 접속 시에는 http://10.0.2.2:8080 사용.
  static const String apiBaseUrl = String.fromEnvironment(
    'API_BASE_URL',
    defaultValue: 'http://localhost:8080',
  );

  /// 기본은 실 API(백엔드 가챠 API 구현 완료). 백엔드 없이 UI만 볼 때는
  /// --dart-define=USE_MOCK_GACHA=true 로 Mock 동작.
  static const bool useMockGacha = bool.fromEnvironment(
    'USE_MOCK_GACHA',
    defaultValue: false,
  );
}
