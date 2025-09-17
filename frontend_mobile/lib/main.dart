import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart'; // Riverpod import 추가
import 'package:frontend_mobile/screens/splash_screen.dart';

void main() {
  runApp(
    // 앱 전체를 ProviderScope로 감싸서 Riverpod를 활성화합니다.
    const ProviderScope(
      child: MyApp(),
    ),
  );
}

class MyApp extends StatelessWidget {
  const MyApp({super.key});

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      title: 'Gacha Scheduler',
      theme: ThemeData(
        colorScheme: ColorScheme.fromSeed(seedColor: Colors.blue),
        useMaterial3: true,
        fontFamily: 'NotoSansKR',
      ),
      home: const SplashScreen(),
    );
  }
}
