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
      bottomNavigationBar: BottomNavigationBar(
        items: <BottomNavigationBarItem>[
          BottomNavigationBarItem(
            icon: const Icon(Icons.calendar_today),
            label: LocalizationManager.instance.getString('schedule_label'),
          ),
          BottomNavigationBarItem(
            icon: const Icon(Icons.casino),
            label: LocalizationManager.instance.getString('gacha_label'),
          ),
          BottomNavigationBarItem(
            icon: const Icon(Icons.home),
            label: LocalizationManager.instance.getString('home_label'),
          ),
          BottomNavigationBarItem(
            icon: const Icon(Icons.article),
            label: LocalizationManager.instance.getString('board_label'),
          ),
          BottomNavigationBarItem(
            icon: const Icon(Icons.person),
            label: LocalizationManager.instance.getString('my_info_label'),
          ),
        ],
        currentIndex: _selectedIndex,
        selectedItemColor: Colors.black,
        unselectedItemColor: Colors.black,
        onTap: _onItemTapped,
        backgroundColor: Colors.grey,
        elevation: 0,
      ),
    );
  }
}
