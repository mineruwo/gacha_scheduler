import 'package:flutter/material.dart';
import 'package:frontend_mobile/managers/localization_manager.dart';
import 'package:frontend_mobile/managers/user_data_manager.dart';

class MyInfoScreen extends StatefulWidget {
  const MyInfoScreen({super.key});

  @override
  State<MyInfoScreen> createState() => _MyInfoScreenState();
}

class _MyInfoScreenState extends State<MyInfoScreen> {
  final TextEditingController _keyController = TextEditingController();
  final TextEditingController _valueController = TextEditingController();
  String? _loadedKey;
  String? _loadedValue;

  @override
  void dispose() {
    _keyController.dispose();
    _valueController.dispose();
    super.dispose();
  }

  Future<void> _saveData() async {
    final key = _keyController.text;
    final value = _valueController.text;
    if (key.isNotEmpty && value.isNotEmpty) {
      await UserDataManager.instance.setString(key, value);
      ScaffoldMessenger.of(context).showSnackBar(
        SnackBar(content: Text('데이터 저장됨: $key = $value')),
      );
    } else {
      ScaffoldMessenger.of(context).showSnackBar(
        const SnackBar(content: Text('키와 값을 모두 입력해주세요.')),
      );
    }
  }

  Future<void> _loadData() async {
    final key = _keyController.text;
    if (key.isNotEmpty) {
      final value = UserDataManager.instance.getString(key);
      setState(() {
        _loadedKey = key;
        _loadedValue = value;
      });
      ScaffoldMessenger.of(context).showSnackBar(
        SnackBar(content: Text('데이터 로드됨: $key = $value')),
      );
    } else {
      ScaffoldMessenger.of(context).showSnackBar(
        const SnackBar(content: Text('조회할 키를 입력해주세요.')),
      );
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: Text(LocalizationManager.instance.getString('my_info_screen_title')),
      ),
      body: Padding(
        padding: const EdgeInsets.all(16.0),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.stretch,
          children: [
            TextField(
              controller: _keyController,
              decoration: const InputDecoration(
                labelText: '키 (Key)',
                border: OutlineInputBorder(),
              ),
            ),
            const SizedBox(height: 16.0),
            TextField(
              controller: _valueController,
              decoration: const InputDecoration(
                labelText: '값 (Value)',
                border: OutlineInputBorder(),
              ),
            ),
            const SizedBox(height: 24.0),
            ElevatedButton(
              onPressed: _saveData,
              child: const Text('데이터 저장'),
            ),
            const SizedBox(height: 16.0),
            ElevatedButton(
              onPressed: _loadData,
              child: const Text('데이터 조회'),
            ),
            const SizedBox(height: 24.0),
            if (_loadedKey != null) ...[
              Text('조회된 키: $_loadedKey', style: const TextStyle(fontSize: 16)),
              const SizedBox(height: 8.0),
              Text('조회된 값: ${_loadedValue ?? '데이터 없음'}', style: const TextStyle(fontSize: 16)),
            ],
          ],
        ),
      ),
    );
  }
}