import 'package:flutter/material.dart';
import 'package:animated_splash_screen/animated_splash_screen.dart';
import 'package:page_transition/page_transition.dart';
import 'package:frontend_mobile/managers/localization_manager.dart';
import 'home_screen.dart';

class SplashScreen extends StatelessWidget {
  const SplashScreen({super.key});

  Future<void> _initializeManagers() async {
    // 앞으로 초기화가 필요한 모든 매니저들을 여기서 호출합니다.
    await LocalizationManager.instance.init();
    // await UserDataManager.instance.init();
    // await GameDataManager.instance.init();
  }

  @override
  Widget build(BuildContext context) {
    return AnimatedSplashScreen.withScreenFunction(
      splash: const Text(
        'Gacha Scheduler',
        style: TextStyle(
          fontSize: 32,
          fontWeight: FontWeight.bold,
          color: Colors.blue,
        ),
      ),
      screenFunction: () async {
        await _initializeManagers();
        // 초기화가 모두 끝나면 홈 스크린을 반환하여 화면을 전환합니다.
        return const HomeScreen();
      },
      splashTransition: SplashTransition.fadeTransition,
      pageTransitionType: PageTransitionType.fade,
      backgroundColor: Colors.white,
    );
  }
}