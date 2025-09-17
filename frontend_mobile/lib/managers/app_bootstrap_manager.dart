import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:frontend_mobile/enums/init_state.dart';
import 'package:frontend_mobile/managers/localization_manager.dart';

class AppBootstrapManager extends StateNotifier<InitState> {
  AppBootstrapManager() : super(InitState.INIT);

  static const Duration _minSplashDuration = Duration(seconds: 3);

  Future<void> _executeStateLogic(InitState targetState) async {
    switch (targetState) {
      case InitState.INIT:
        break;
      case InitState.LOCALIZATION:
        await LocalizationManager.instance.init();
        break;
      case InitState.REQUIRE_INITIALIZED_DONE:
        break;
      case InitState.DONE:
        break;
    }
  }

  Future<void> initialize() async {
    final startTime = DateTime.now();

    state = InitState.LOCALIZATION;
    await _executeStateLogic(InitState.LOCALIZATION);

    final endTimeRequired = DateTime.now();
    final elapsedRequired = endTimeRequired.difference(startTime);
    if (elapsedRequired < _minSplashDuration) {
      await Future.delayed(_minSplashDuration - elapsedRequired);
    }

    state = InitState.REQUIRE_INITIALIZED_DONE;
    await _executeStateLogic(InitState.REQUIRE_INITIALIZED_DONE);

    state = InitState.DONE;
    await _executeStateLogic(InitState.DONE);
  }
}

final appBootstrapProvider = StateNotifierProvider<AppBootstrapManager, InitState>((ref) {
  return AppBootstrapManager();
});