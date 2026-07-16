import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:frontend_mobile/screens/splash_screen.dart';
import 'package:firebase_core/firebase_core.dart';
import 'package:frontend_mobile/firebase_options.dart';
import 'package:frontend_mobile/theme/app_theme.dart';
import 'package:firebase_crashlytics/firebase_crashlytics.dart';
import 'package:firebase_analytics/firebase_analytics.dart';
import 'package:firebase_messaging/firebase_messaging.dart';
import 'dart:async'; // runZonedGuarded를 위해 필요

// 백그라운드 메시지를 처리하기 위한 최상위 함수 정의
@pragma('vm:entry-point')
Future<void> _firebaseMessagingBackgroundHandler(RemoteMessage message) async {
  await Firebase.initializeApp(options: DefaultFirebaseOptions.currentPlatform);
  print("Handling a background message: ${message.messageId}");
  // 여기에서 UI 업데이트 또는 데이터 가져오기와 같은 무거운 작업을 수행할 수 있습니다.
}

void main() async {
  // 모든 오류를 catch하기 위해 zone 내에서 앱 실행
  runZonedGuarded(() async {
    WidgetsFlutterBinding.ensureInitialized();
    await Firebase.initializeApp(
      options: DefaultFirebaseOptions.currentPlatform,
    );

    // Crashlytics 초기화
    FlutterError.onError = FirebaseCrashlytics.instance.recordFlutterFatalError;

    // Firebase Messaging 백그라운드 핸들러 설정
    FirebaseMessaging.onBackgroundMessage(_firebaseMessagingBackgroundHandler);

    // 알림 권한 요청
    NotificationSettings settings = await FirebaseMessaging.instance.requestPermission(
      alert: true,
      announcement: false,
      badge: true,
      carPlay: false,
      criticalAlert: false,
      provisional: false,
      sound: true,
    );

    print('User granted permission: ${settings.authorizationStatus}');

    // FCM 토큰 가져오기
    String? token = await FirebaseMessaging.instance.getToken();
    print('FCM Token: $token');

    // 포그라운드 메시지 처리
    FirebaseMessaging.onMessage.listen((RemoteMessage message) {
      print('Got a message whilst in the foreground!');
      print('Message data: ${message.data}');

      if (message.notification != null) {
        print('Message also contained a notification: ${message.notification}');
      }
      // 여기에서 로컬 알림을 표시할 수 있습니다.
    });

    // 앱이 종료된 상태에서 열릴 때 메시지 처리
    FirebaseMessaging.onMessageOpenedApp.listen((RemoteMessage message) {
      print('App opened from a terminated state by a Notification!');
      print('Message data: ${message.data}');
      // 메시지 데이터에 따라 특정 화면으로 이동
    });

    runApp(const ProviderScope(child: MyApp()));
  }, (error, stack) {
    FirebaseCrashlytics.instance.recordError(error, stack, fatal: true);
  });
}

class MyApp extends StatelessWidget {
  const MyApp({super.key});

  static FirebaseAnalytics analytics = FirebaseAnalytics.instance;
  static FirebaseAnalyticsObserver observer = FirebaseAnalyticsObserver(analytics: analytics);

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      title: 'Gacha Scheduler',
      theme: AppTheme.light,
      home: const SplashScreen(),
      debugShowCheckedModeBanner: false,
      navigatorObservers: [observer],
    );
  }
}