import 'dart:convert';

import 'package:http/http.dart' as http;
import 'package:frontend_mobile/config/app_config.dart';
import 'package:frontend_mobile/models/game_model.dart';
import 'package:frontend_mobile/models/schedule_event_model.dart';

abstract class ScheduleRepository {
  Future<List<GameModel>> fetchGames();

  Future<List<ScheduleEventModel>> fetchSchedules({required DateTime from, required DateTime to});

  Future<List<String>> fetchMyGamePreferences(String token);

  Future<void> updateMyGamePreferences(String token, List<String> gameCodes);
}

class ApiScheduleRepository implements ScheduleRepository {
  final http.Client _client;

  ApiScheduleRepository({http.Client? client}) : _client = client ?? http.Client();

  Uri _uri(String path, [Map<String, String>? query]) =>
      Uri.parse('${AppConfig.apiBaseUrl}$path').replace(queryParameters: query);

  @override
  Future<List<GameModel>> fetchGames() async {
    final response = await _client.get(_uri('/api/games'));
    _ensureOk(response);
    final List<dynamic> body = jsonDecode(utf8.decode(response.bodyBytes));
    return body.map((e) => GameModel.fromJson(e as Map<String, dynamic>)).toList();
  }

  @override
  Future<List<ScheduleEventModel>> fetchSchedules({required DateTime from, required DateTime to}) async {
    // Spring의 ISO.DATE_TIME 파서는 오프셋/Z가 없는 문자열을 거부하므로 반드시 UTC로 변환해서 보낸다.
    final response = await _client.get(_uri('/api/schedules', {
      'from': from.toUtc().toIso8601String(),
      'to': to.toUtc().toIso8601String(),
    }));
    _ensureOk(response);
    final List<dynamic> body = jsonDecode(utf8.decode(response.bodyBytes));
    return body.map((e) => ScheduleEventModel.fromJson(e as Map<String, dynamic>)).toList();
  }

  @override
  Future<List<String>> fetchMyGamePreferences(String token) async {
    final response = await _client.get(
      _uri('/api/users/me/game-preferences'),
      headers: {'Authorization': 'Bearer $token'},
    );
    _ensureOk(response);
    final List<dynamic> body = jsonDecode(utf8.decode(response.bodyBytes));
    return body.map((e) => (e as Map<String, dynamic>)['gameCode'] as String).toList();
  }

  @override
  Future<void> updateMyGamePreferences(String token, List<String> gameCodes) async {
    final response = await _client.put(
      _uri('/api/users/me/game-preferences'),
      headers: {'Content-Type': 'application/json', 'Authorization': 'Bearer $token'},
      body: jsonEncode({'gameCodes': gameCodes}),
    );
    _ensureOk(response);
  }

  void _ensureOk(http.Response response) {
    if (response.statusCode < 200 || response.statusCode >= 300) {
      throw ScheduleApiException(response.statusCode, response.body);
    }
  }
}

class ScheduleApiException implements Exception {
  final int statusCode;
  final String body;

  ScheduleApiException(this.statusCode, this.body);

  @override
  String toString() => 'ScheduleApiException($statusCode): $body';
}
