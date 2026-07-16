import 'package:flutter/material.dart';
import 'package:frontend_mobile/managers/localization_manager.dart';
import 'package:frontend_mobile/screens/schedule_screen.dart';
import 'package:frontend_mobile/screens/gacha_screen.dart';
import 'package:frontend_mobile/screens/board_screen.dart';
import 'package:frontend_mobile/screens/my_info_screen.dart';
import 'package:frontend_mobile/screens/home_tab_screen.dart';

class HomeScreen extends StatefulWidget {
  const HomeScreen({super.key});

  @override
  State<HomeScreen> createState() => _HomeScreenState();
}

class _HomeScreenState extends State<HomeScreen> {
  int _selectedIndex = 2;

  static final List<Widget> _widgetOptions = <Widget>[
    const ScheduleScreen(),
    const GachaScreen(),
    const HomeTabScreen(),
    const BoardScreen(),
    const MyInfoScreen(),
  ];

  void _onItemTapped(int index) {
    setState(() {
      _selectedIndex = index;
    });
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      body: Center(
        child: _widgetOptions.elementAt(_selectedIndex),
      ),
      bottomNavigationBar: Container(
        decoration: BoxDecoration(
          color: Theme.of(context).bottomNavigationBarTheme.backgroundColor,
          boxShadow: [
            BoxShadow(
              color: Colors.black.withValues(alpha: 0.06),
              blurRadius: 16,
              offset: const Offset(0, -4),
            ),
          ],
        ),
        child: SafeArea(
          child: BottomNavigationBar(
            items: <BottomNavigationBarItem>[
              BottomNavigationBarItem(
                icon: const Icon(Icons.calendar_today_rounded),
                label: LocalizationManager.instance.getString('schedule_label'),
              ),
              BottomNavigationBarItem(
                icon: const Icon(Icons.casino_rounded),
                label: LocalizationManager.instance.getString('gacha_label'),
              ),
              BottomNavigationBarItem(
                icon: const Icon(Icons.home_rounded),
                label: LocalizationManager.instance.getString('home_label'),
              ),
              BottomNavigationBarItem(
                icon: const Icon(Icons.article_rounded),
                label: LocalizationManager.instance.getString('board_label'),
              ),
              BottomNavigationBarItem(
                icon: const Icon(Icons.person_rounded),
                label: LocalizationManager.instance.getString('my_info_label'),
              ),
            ],
            currentIndex: _selectedIndex,
            onTap: _onItemTapped,
          ),
        ),
      ),
    );
  }
}
