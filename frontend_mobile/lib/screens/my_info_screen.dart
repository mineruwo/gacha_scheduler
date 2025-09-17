import 'package:flutter/material.dart';
import 'package:frontend_mobile/managers/localization_manager.dart';

class MyInfoScreen extends StatelessWidget {
  const MyInfoScreen({super.key});

  @override
  Widget build(BuildContext context) {
    return Center(
      child: Text(LocalizationManager.instance.getString('my_info_screen_title')),
    );
  }
}
