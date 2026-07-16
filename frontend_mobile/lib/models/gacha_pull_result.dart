import 'package:frontend_mobile/models/gacha_character.dart';

class GachaPullResult {
  final List<GachaCharacter> results;

  /// 뽑기 이후 갱신된 천장 카운트. 최고 등급 획득 시 서버(또는 Mock)에서 0으로 초기화된다.
  final int pityCount;

  const GachaPullResult({
    required this.results,
    required this.pityCount,
  });

  factory GachaPullResult.fromJson(Map<String, dynamic> json) {
    return GachaPullResult(
      results: (json['results'] as List<dynamic>)
          .map((e) => GachaCharacter.fromJson(e as Map<String, dynamic>))
          .toList(),
      pityCount: json['pityCount'] as int,
    );
  }
}
