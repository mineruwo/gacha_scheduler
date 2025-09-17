import 'dart:convert';
import 'package:flutter/services.dart';
import 'package:frontend_mobile/managers/manager.dart';

class LocalizationManager extends Manager {
  // --- Singleton Pattern Start ---
  LocalizationManager._();
  static final LocalizationManager instance = LocalizationManager._();
  // --- Singleton Pattern End ---

  // 로드된 번역 텍스트를 저장할 변수
  late Map<String, String> _localizedStrings;

  @override
  Future<void> init() async {
    // 우선 한국어(ko)로 고정해서 파일을 불러옵니다.
    await _loadLocalization('ko');
    print('LocalizationManager 초기화 완료!');
  }

  // 파일을 읽고 파싱하는 내부 메서드
  Future<void> _loadLocalization(String locale) async {
    final String jsonString = await rootBundle.loadString('assets/json/l10n/$locale.json');
    final Map<String, dynamic> jsonMap = json.decode(jsonString);

    _localizedStrings = jsonMap.map((key, value) {
      return MapEntry(key, value.toString());
    });
  }

  @override
  void release() {
    _localizedStrings.clear();
    print('LocalizationManager 해제 완료!');
  }

  // 키를 사용해 번역된 문자열을 가져오는 메서드
  String getString(String key) {
    return _localizedStrings[key] ?? 'Key not found: $key';
  }
}