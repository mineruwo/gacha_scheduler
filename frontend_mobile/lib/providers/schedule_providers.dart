import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:frontend_mobile/managers/user_data_manager.dart';
import 'package:frontend_mobile/models/game_model.dart';
import 'package:frontend_mobile/models/schedule_event_model.dart';
import 'package:frontend_mobile/providers/auth_providers.dart';
import 'package:frontend_mobile/repositories/schedule_repository.dart';

const _localFilterKey = 'scheduler_game_filter';

final scheduleRepositoryProvider = Provider<ScheduleRepository>((ref) {
  return ApiScheduleRepository();
});

final gamesProvider = FutureProvider<List<GameModel>>((ref) {
  return ref.watch(scheduleRepositoryProvider).fetchGames();
});

DateTime _addMonths(DateTime date, int months) {
  return DateTime(date.year, date.month + months, date.day);
}

DateTimeRange _defaultRange() {
  final now = DateTime.now();
  return DateTimeRange(start: _addMonths(now, -1), end: _addMonths(now, 3));
}

class ScheduleRangeController extends StateNotifier<DateTimeRange> {
  ScheduleRangeController() : super(_defaultRange());

  void shift(int months) {
    state = DateTimeRange(start: _addMonths(state.start, months), end: _addMonths(state.end, months));
  }

  void reset() {
    state = _defaultRange();
  }
}

final scheduleRangeProvider = StateNotifierProvider<ScheduleRangeController, DateTimeRange>((ref) {
  return ScheduleRangeController();
});

final schedulesProvider = FutureProvider<List<ScheduleEventModel>>((ref) {
  final range = ref.watch(scheduleRangeProvider);
  return ref.watch(scheduleRepositoryProvider).fetchSchedules(from: range.start, to: range.end);
});

// 비로그인 유저는 관심 게임 필터를 로컬(shared_preferences)에 보관하고,
// 로그인 유저는 서버(계정)에 저장해 기기 간 유지되도록 한다 (웹 SchedulerPage와 동일한 정책).
class GameFilterController extends StateNotifier<List<String>> {
  final Ref _ref;

  GameFilterController(this._ref) : super(const []) {
    _load();
    _ref.listen(authControllerProvider, (previous, next) {
      if (previous?.isLoggedIn != next.isLoggedIn) _load();
    });
  }

  Future<void> _load() async {
    final auth = _ref.read(authControllerProvider);
    if (auth.isLoggedIn) {
      try {
        state = await _ref.read(scheduleRepositoryProvider).fetchMyGamePreferences(auth.user!.token);
        return;
      } catch (_) {
        // 서버 조회 실패 시 로컬 값으로 대체
      }
    }
    state = UserDataManager.instance.getStringList(_localFilterKey) ?? const [];
  }

  Future<void> toggle(String gameCode) async {
    final next = state.contains(gameCode)
        ? state.where((c) => c != gameCode).toList()
        : [...state, gameCode];
    state = next;

    final auth = _ref.read(authControllerProvider);
    if (auth.isLoggedIn) {
      try {
        await _ref.read(scheduleRepositoryProvider).updateMyGamePreferences(auth.user!.token, next);
      } catch (_) {
        // 저장 실패해도 화면 상 필터는 유지 (다음 토글 시 재시도됨)
      }
    } else {
      await UserDataManager.instance.setStringList(_localFilterKey, next);
    }
  }
}

final gameFilterProvider = StateNotifierProvider<GameFilterController, List<String>>((ref) {
  return GameFilterController(ref);
});
