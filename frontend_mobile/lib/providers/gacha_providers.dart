import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:frontend_mobile/config/app_config.dart';
import 'package:frontend_mobile/managers/user_data_manager.dart';
import 'package:frontend_mobile/models/gacha_banner.dart';
import 'package:frontend_mobile/models/gacha_character.dart';
import 'package:frontend_mobile/repositories/gacha_repository.dart';
import 'package:frontend_mobile/repositories/mock_gacha_repository.dart';

final gachaRepositoryProvider = Provider<GachaRepository>((ref) {
  if (AppConfig.useMockGacha) {
    return MockGachaRepository();
  }
  return ApiGachaRepository();
});

final bannersProvider = FutureProvider<List<GachaBanner>>((ref) {
  return ref.watch(gachaRepositoryProvider).fetchBanners();
});

class GachaState {
  final GachaBanner? selectedBanner;
  final List<GachaCharacter> lastResults;
  final int pityCount;
  final bool isPulling;
  final Object? error;

  const GachaState({
    this.selectedBanner,
    this.lastResults = const [],
    this.pityCount = 0,
    this.isPulling = false,
    this.error,
  });

  GachaState copyWith({
    GachaBanner? selectedBanner,
    List<GachaCharacter>? lastResults,
    int? pityCount,
    bool? isPulling,
    Object? error,
    bool clearError = false,
  }) {
    return GachaState(
      selectedBanner: selectedBanner ?? this.selectedBanner,
      lastResults: lastResults ?? this.lastResults,
      pityCount: pityCount ?? this.pityCount,
      isPulling: isPulling ?? this.isPulling,
      error: clearError ? null : (error ?? this.error),
    );
  }
}

class GachaController extends StateNotifier<GachaState> {
  final GachaRepository _repository;

  GachaController(this._repository) : super(const GachaState());

  // 비로그인 유저의 천장 카운트는 배너별로 로컬(shared_preferences)에 보관한다.
  // 로그인 연동(파트 02) 이후에는 서버가 반환하는 카운트가 우선한다.
  String _pityKey(int bannerId) => 'gacha_pity_$bannerId';

  void selectBanner(GachaBanner banner) {
    final savedPity = UserDataManager.instance.getInt(_pityKey(banner.id)) ?? 0;
    state = GachaState(selectedBanner: banner, pityCount: savedPity);
  }

  Future<void> pull(int count) async {
    final banner = state.selectedBanner;
    if (banner == null || state.isPulling) return;

    state = state.copyWith(isPulling: true, clearError: true);
    try {
      final result = await _repository.pull(
        banner.id,
        count: count,
        currentPity: state.pityCount,
      );
      await UserDataManager.instance.setInt(_pityKey(banner.id), result.pityCount);
      state = state.copyWith(
        lastResults: result.results,
        pityCount: result.pityCount,
        isPulling: false,
      );
    } catch (e) {
      state = state.copyWith(isPulling: false, error: e);
    }
  }
}

final gachaControllerProvider = StateNotifierProvider<GachaController, GachaState>((ref) {
  return GachaController(ref.watch(gachaRepositoryProvider));
});
