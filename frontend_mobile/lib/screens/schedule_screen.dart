import 'package:flutter/material.dart';
import 'package:frontend_mobile/managers/localization_manager.dart';

class ScheduleScreen extends StatelessWidget {
  const ScheduleScreen({super.key});

  @override
  Widget build(BuildContext context) {
    return Center(
      child: Text(LocalizationManager.instance.getString('schedule_screen_title')),
    );
  }
}
