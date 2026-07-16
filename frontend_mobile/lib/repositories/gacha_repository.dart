import 'dart:convert';

import 'package:http/http.dart' as http;
import 'package:frontend_mobile/config/app_config.dart';
import 'package:frontend_mobile/models/gacha_banner.dart';
import 'package:frontend_mobile/models/gacha_character.dart';
import 'package:frontend_mobile/models/gacha_pull_result.dart';

/// 가챠 시뮬레이터 데이터 소스 추상화.
/// 확률/천장 계산은 서버 책임이며(클라이언트 조작 방지), 클라이언트는 결과 표시에 집중한다.
abstract class GachaRepository {
  /// 진행 중 배너 목록. gameId를 넘기지 않으면 전체 게임의 배너를 조회한다.
  Future<List<GachaBanner>> fetchBanners({int? gameId});

  /// 배너의 뽑기 대상 캐릭터/가중치 목록.
  Future<List<GachaCharacter>> fetchBannerCharacters(int bannerId);

  /// 1회/10연 뽑기 실행.
  /// [currentPity]는 비로그인 유저가 클라이언트에 보관 중인 천장 카운트.
  /// 로그인 유저는 서버가 보관한 카운트를 사용하므로 서버에서 무시될 수 있다.
  Future<GachaPullResult> pull(int bannerId, {required int count, required int currentPity});
}

/// docs/plans/04-gacha-simulator.md 의 API 계약을 따르는 구현.
class ApiGachaRepository implements GachaRepository {
  final http.Client _client;

  ApiGachaRepository({http.Client? client}) : _client = client ?? http.Client();

  Uri _uri(String path) => Uri.parse('${AppConfig.apiBaseUrl}$path');

  @override
  Future<List<GachaBanner>> fetchBanners({int? gameId}) async {
    final path = gameId != null ? '/api/games/$gameId/banners' : '/api/banners';
    final response = await _client.get(_uri(path));
    _ensureOk(response);
    final List<dynamic> body = jsonDecode(utf8.decode(response.bodyBytes));
    return body.map((e) => GachaBanner.fromJson(e as Map<String, dynamic>)).toList();
  }

  @override
  Future<List<GachaCharacter>> fetchBannerCharacters(int bannerId) async {
    final response = await _client.get(_uri('/api/banners/$bannerId/characters'));
    _ensureOk(response);
    final List<dynamic> body = jsonDecode(utf8.decode(response.bodyBytes));
    return body.map((e) => GachaCharacter.fromJson(e as Map<String, dynamic>)).toList();
  }

  @override
  Future<GachaPullResult> pull(int bannerId, {required int count, required int currentPity}) async {
    final response = await _client.post(
      _uri('/api/banners/$bannerId/pull'),
      headers: {'Content-Type': 'application/json'},
      body: jsonEncode({'count': count, 'currentPity': currentPity}),
    );
    _ensureOk(response);
    final Map<String, dynamic> body = jsonDecode(utf8.decode(response.bodyBytes));
    return GachaPullResult.fromJson(body);
  }

  void _ensureOk(http.Response response) {
    if (response.statusCode < 200 || response.statusCode >= 300) {
      throw GachaApiException(response.statusCode, response.body);
    }
  }
}

class GachaApiException implements Exception {
  final int statusCode;
  final String body;

  GachaApiException(this.statusCode, this.body);

  @override
  String toString() => 'GachaApiException($statusCode): $body';
}
