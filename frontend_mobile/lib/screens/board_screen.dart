import 'package:flutter/material.dart';
import 'package:frontend_mobile/managers/localization_manager.dart';

class BoardScreen extends StatelessWidget {
  const BoardScreen({super.key});

  @override
  Widget build(BuildContext context) {
    return Center(
      child: Text(LocalizationManager.instance.getString('board_screen_title')),
    );
  }
}
