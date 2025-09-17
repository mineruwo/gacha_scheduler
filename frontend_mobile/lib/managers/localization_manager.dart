import 'dart:convert';
import 'package:flutter/services.dart';
import 'package:frontend_mobile/managers/manager.dart';

class LocalizationManager extends Manager {
  LocalizationManager._();
  static final LocalizationManager instance = LocalizationManager._();

  late Map<String, String> _localizedStrings;

  @override
  Future<void> init() async {
    await _loadLocalization('ko');
  }

  Future<void> _loadLocalization(String locale) async {
    final String jsonString = await rootBundle.loadString(
      'assets/json/l10n/$locale.json',
    );
    final Map<String, dynamic> jsonMap = json.decode(jsonString);

    _localizedStrings = jsonMap.map((key, value) {
      return MapEntry(key, value.toString());
    });
  }

  @override
  void release() {
    _localizedStrings.clear();
  }

  String getString(String key) {
    return _localizedStrings[key] ?? 'Key not found: $key';
  }
}