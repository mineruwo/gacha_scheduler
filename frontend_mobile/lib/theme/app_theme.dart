import 'package:flutter/material.dart';

/// docs/design_assets/ui_concept_light_pastel_*.jpg 컨셉(클린 파스텔) 기반 색상 팔레트.
class AppColors {
  AppColors._();

  static const background = Color(0xFFF6F4FB);
  static const surface = Color(0xFFFFFFFF);
  static const textPrimary = Color(0xFF2D2A3A);
  static const textSecondary = Color(0xFF8B879C);

  static const pastelBlue = Color(0xFFAFD8FF);
  static const pastelPink = Color(0xFFFFC2D6);
  static const pastelMint = Color(0xFFB8ECD9);
  static const pastelPeach = Color(0xFFFFD9B0);
  static const pastelPurple = Color(0xFFD9C7FF);

  static const primary = Color(0xFF8FA9F3);
  static const primaryDark = Color(0xFF6C82D6);

  static const rarity5 = Color(0xFFFFC94A);
  static const rarity4 = pastelPurple;
  static const rarity3 = pastelBlue;
  static const rarityDefault = Color(0xFFCBD3E8);

  static const categoryUpdate = Color(0xFF6FA8E8);
  static const categoryEvent = Color(0xFF4FAF82);
  static const categoryMaintenance = Color(0xFFE87A6E);
}

class AppTheme {
  AppTheme._();

  static ThemeData get light {
    final colorScheme = ColorScheme.fromSeed(
      seedColor: AppColors.primary,
      brightness: Brightness.light,
      primary: AppColors.primary,
      surface: AppColors.surface,
    );

    return ThemeData(
      useMaterial3: true,
      fontFamily: 'NotoSansKR',
      colorScheme: colorScheme,
      scaffoldBackgroundColor: AppColors.background,
      appBarTheme: const AppBarTheme(
        backgroundColor: AppColors.background,
        foregroundColor: AppColors.textPrimary,
        elevation: 0,
        centerTitle: true,
        titleTextStyle: TextStyle(
          color: AppColors.textPrimary,
          fontSize: 18,
          fontWeight: FontWeight.bold,
          fontFamily: 'NotoSansKR',
        ),
      ),
      cardTheme: CardThemeData(
        color: AppColors.surface,
        elevation: 0,
        shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(20)),
        margin: EdgeInsets.zero,
      ),
      inputDecorationTheme: InputDecorationTheme(
        filled: true,
        fillColor: AppColors.surface,
        contentPadding: const EdgeInsets.symmetric(horizontal: 16, vertical: 14),
        border: OutlineInputBorder(
          borderRadius: BorderRadius.circular(16),
          borderSide: BorderSide.none,
        ),
        enabledBorder: OutlineInputBorder(
          borderRadius: BorderRadius.circular(16),
          borderSide: const BorderSide(color: Color(0xFFE6E1F5)),
        ),
        focusedBorder: OutlineInputBorder(
          borderRadius: BorderRadius.circular(16),
          borderSide: const BorderSide(color: AppColors.primary, width: 1.5),
        ),
        labelStyle: const TextStyle(color: AppColors.textSecondary),
      ),
      filledButtonTheme: FilledButtonThemeData(
        style: FilledButton.styleFrom(
          backgroundColor: AppColors.primary,
          foregroundColor: Colors.white,
          minimumSize: const Size.fromHeight(48),
          shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(16)),
          textStyle: const TextStyle(fontWeight: FontWeight.bold),
        ),
      ),
      elevatedButtonTheme: ElevatedButtonThemeData(
        style: ElevatedButton.styleFrom(
          backgroundColor: AppColors.primary,
          foregroundColor: Colors.white,
          minimumSize: const Size.fromHeight(48),
          elevation: 0,
          shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(16)),
          textStyle: const TextStyle(fontWeight: FontWeight.bold),
        ),
      ),
      outlinedButtonTheme: OutlinedButtonThemeData(
        style: OutlinedButton.styleFrom(
          foregroundColor: AppColors.primaryDark,
          minimumSize: const Size.fromHeight(48),
          side: const BorderSide(color: AppColors.primary, width: 1.5),
          shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(16)),
          textStyle: const TextStyle(fontWeight: FontWeight.bold),
        ),
      ),
      textButtonTheme: TextButtonThemeData(
        style: TextButton.styleFrom(foregroundColor: AppColors.primaryDark),
      ),
      progressIndicatorTheme: const ProgressIndicatorThemeData(
        color: AppColors.primary,
        linearTrackColor: Color(0xFFE9E5F7),
      ),
      bottomNavigationBarTheme: const BottomNavigationBarThemeData(
        type: BottomNavigationBarType.fixed,
        backgroundColor: AppColors.surface,
        selectedItemColor: AppColors.primaryDark,
        unselectedItemColor: AppColors.textSecondary,
        selectedLabelStyle: TextStyle(fontSize: 11, fontWeight: FontWeight.bold),
        unselectedLabelStyle: TextStyle(fontSize: 11),
        showUnselectedLabels: true,
        elevation: 0,
      ),
      textTheme: const TextTheme(
        headlineSmall: TextStyle(color: AppColors.textPrimary, fontWeight: FontWeight.bold),
        titleMedium: TextStyle(color: AppColors.textPrimary, fontWeight: FontWeight.bold),
        bodyMedium: TextStyle(color: AppColors.textPrimary),
        bodySmall: TextStyle(color: AppColors.textSecondary),
      ),
    );
  }

  /// 카드 전체를 감싸 은은한 파스텔 그림자를 주는 공통 데코레이션.
  static BoxDecoration softCard({Color color = AppColors.surface, double radius = 20}) {
    return BoxDecoration(
      color: color,
      borderRadius: BorderRadius.circular(radius),
      boxShadow: [
        BoxShadow(
          color: AppColors.primary.withValues(alpha: 0.08),
          blurRadius: 16,
          offset: const Offset(0, 6),
        ),
      ],
    );
  }
}
