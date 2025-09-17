import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:frontend_mobile/enums/init_state.dart';
import 'package:frontend_mobile/managers/app_bootstrap_manager.dart';
import 'package:frontend_mobile/screens/home_screen.dart';

class SplashScreen extends ConsumerWidget {
  const SplashScreen({super.key});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    ref.listen(appBootstrapProvider, (previous, next) {
      if (next == InitState.REQUIRE_INITIALIZED_DONE) {
        Navigator.of(context).pushReplacement(
          MaterialPageRoute(builder: (context) => const HomeScreen()),
        );
      }
    });

    WidgetsBinding.instance.addPostFrameCallback((_) {
      ref.read(appBootstrapProvider.notifier).initialize();
    });

    return const Scaffold(
      body: Center(
        child: Text(
          'Gacha Scheduler',
          style: TextStyle(
            fontSize: 32,
            fontWeight: FontWeight.bold,
            color: Colors.blue,
          ),
        ),
      ),
    );
  }
}