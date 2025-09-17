import 'package:flutter/material.dart';
import 'package:frontend_mobile/managers/localization_manager.dart';

class GachaScreen extends StatelessWidget {
  const GachaScreen({super.key});

  @override
  Widget build(BuildContext context) {
    return Center(
      child: Text(LocalizationManager.instance.getString('gacha_screen_title')),
    );
  }
}
