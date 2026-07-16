import 'dart:math';

import 'package:frontend_mobile/models/gacha_banner.dart';
import 'package:frontend_mobile/models/gacha_character.dart';
import 'package:frontend_mobile/models/gacha_pull_result.dart';
import 'package:frontend_mobile/repositories/gacha_repository.dart';

/// 개발용 Mock 저장소. 백엔드 가챠 API(파트 02~04)가 완성되기 전까지
/// 서버가 수행할 가중치/천장 계산을 로컬에서 흉내 낸다.
/// 실서비스 로직은 반드시 서버에 있어야 하며, 이 클래스는 UI 개발/검증 용도로만 사용한다.
class MockGachaRepository implements GachaRepository {
  final Random _random;

  MockGachaRepository({Random? random}) : _random = random ?? Random();

  static final List<GachaBanner> _banners = [
    GachaBanner(
      id: 1,
      gameId: 1,
      gameName: '스타레일 (샘플)',
      name: '픽업: 은하의 기사',
      startAt: DateTime(2026, 7, 1),
      endAt: DateTime(2026, 7, 31),
      pickupCharacterIds: const [101],
      pityThreshold: 90,
      rateUpRate: 0.5,
    ),
    GachaBanner(
      id: 2,
      gameId: 2,
      gameName: '블루 아카이브 (샘플)',
      name: '픽업: 수영복 세나',
      startAt: DateTime(2026, 7, 10),
      endAt: DateTime(2026, 8, 10),
      pickupCharacterIds: const [201],
      pityThreshold: 200,
      rateUpRate: 0.7,
    ),
  ];

  static const Map<int, List<GachaCharacter>> _charactersByBanner = {
    1: [
      GachaCharacter(id: 101, gameId: 1, name: '은하의 기사', rarity: 5, weight: 0.6),
      GachaCharacter(id: 102, gameId: 1, name: '별의 여행자', rarity: 5, weight: 0.6),
      GachaCharacter(id: 103, gameId: 1, name: '유성 사수', rarity: 4, weight: 5.1),
      GachaCharacter(id: 104, gameId: 1, name: '혜성 검사', rarity: 4, weight: 5.1),
      GachaCharacter(id: 105, gameId: 1, name: '위성 정비공', rarity: 3, weight: 44.3),
      GachaCharacter(id: 106, gameId: 1, name: '우주 화물선', rarity: 3, weight: 44.3),
    ],
    2: [
      GachaCharacter(id: 201, gameId: 2, name: '수영복 세나', rarity: 3, weight: 0.7),
      GachaCharacter(id: 202, gameId: 2, name: '체육복 히나', rarity: 3, weight: 0.7),
      GachaCharacter(id: 203, gameId: 2, name: '방과후 유즈', rarity: 2, weight: 18.5),
      GachaCharacter(id: 204, gameId: 2, name: '도서부 코토리', rarity: 2, weight: 18.5),
      GachaCharacter(id: 205, gameId: 2, name: '신입생 모모', rarity: 1, weight: 30.8),
      GachaCharacter(id: 206, gameId: 2, name: '신입생 카에데', rarity: 1, weight: 30.8),
    ],
  };

  @override
  Future<List<GachaBanner>> fetchBanners({int? gameId}) async {
    await Future.delayed(const Duration(milliseconds: 300));
    if (gameId == null) return _banners;
    return _banners.where((b) => b.gameId == gameId).toList();
  }

  @override
  Future<List<GachaCharacter>> fetchBannerCharacters(int bannerId) async {
    await Future.delayed(const Duration(milliseconds: 200));
    return _charactersByBanner[bannerId] ?? [];
  }

  @override
  Future<GachaPullResult> pull(int bannerId, {required int count, required int currentPity}) async {
    await Future.delayed(const Duration(milliseconds: 400));

    final banner = _banners.firstWhere((b) => b.id == bannerId);
    final pool = _charactersByBanner[bannerId];
    if (pool == null || pool.isEmpty) {
      throw GachaApiException(404, 'No characters for banner $bannerId');
    }

    final maxRarity = pool.map((c) => c.rarity).reduce(max);
    final results = <GachaCharacter>[];
    var pity = currentPity;

    for (var i = 0; i < count; i++) {
      pity++;
      GachaCharacter picked;
      if (pity >= banner.pityThreshold) {
        // 확정 천장: 최고 등급 풀에서 가중치 랜덤 (픽업 확률 rateUpRate 적용)
        picked = _pickHighestRarity(pool, banner, maxRarity);
      } else {
        picked = _pickWeighted(pool);
      }
      if (picked.rarity == maxRarity) {
        pity = 0;
      }
      results.add(picked);
    }

    return GachaPullResult(results: results, pityCount: pity);
  }

  GachaCharacter _pickWeighted(List<GachaCharacter> pool) {
    final totalWeight = pool.fold<double>(0, (sum, c) => sum + c.weight);
    var roll = _random.nextDouble() * totalWeight;
    for (final c in pool) {
      roll -= c.weight;
      if (roll <= 0) return c;
    }
    return pool.last;
  }

  GachaCharacter _pickHighestRarity(List<GachaCharacter> pool, GachaBanner banner, int maxRarity) {
    final top = pool.where((c) => c.rarity == maxRarity).toList();
    final pickup = top.where((c) => banner.pickupCharacterIds.contains(c.id)).toList();
    if (pickup.isNotEmpty && _random.nextDouble() < banner.rateUpRate) {
      return pickup[_random.nextInt(pickup.length)];
    }
    return _pickWeighted(top);
  }
}
