import 'package:flutter/material.dart';
import 'package:frontend_mobile/managers/localization_manager.dart';

class HomeTabScreen extends StatelessWidget {
  const HomeTabScreen({super.key});

  @override
  Widget build(BuildContext context) {
    return Center(
      child: Text(LocalizationManager.instance.getString('app_title')),
    );
  }
}
