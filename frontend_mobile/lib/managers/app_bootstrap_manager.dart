import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:frontend_mobile/enums/init_state.dart';
import 'package:frontend_mobile/managers/localization_manager.dart';

class AppBootstrapManager extends StateNotifier<InitState> {
  AppBootstrapManager() : super(InitState.INIT);

  Future<void> initialize() async {
    state = InitState.LOCALIZATION;
    await LocalizationManager.instance.init();

    state = InitState.REQUIRE_INITIALIZED_DONE;

    // ...

    state = InitState.DONE;
    print('모든 매니저 초기화 완료!');
  }
}

final appBootstrapProvider = StateNotifierProvider<AppBootstrapManager, InitState>((ref) {
  return AppBootstrapManager();
});
