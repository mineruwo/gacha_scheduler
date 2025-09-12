import 'package:flutter/material.dart';
import 'package:http/http.dart' as http;
import 'dart:io' show Platform;

void main() {
  runApp(const MyApp());
}

class MyApp extends StatelessWidget {
  const MyApp({super.key});

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      title: 'Flutter + Spring Boot',
      theme: ThemeData(
        colorScheme: ColorScheme.fromSeed(seedColor: Colors.blue),
        useMaterial3: true,
      ),
      home: const MyHomePage(),
    );
  }
}

class MyHomePage extends StatefulWidget {
  const MyHomePage({super.key});

  @override
  State<MyHomePage> createState() => _MyHomePageState();
}

class _MyHomePageState extends State<MyHomePage> {
  String _message = 'Loading message from backend...';

  @override
  void initState() {
    super.initState();
    _fetchMessage();
  }

  Future<void> _fetchMessage() async {
    // Use 10.0.2.2 for Android Emulator to connect to host's localhost
    // For iOS Simulator, use 'localhost' or '127.0.0.1'
    final String host = Platform.isAndroid ? '10.0.2.2' : 'localhost';
    final url = Uri.parse('http://$host:8080/api/hello');
    
    String message;
    try {
      final response = await http.get(url);
      if (response.statusCode == 200) {
        message = response.body;
      } else {
        message = 'Failed to load message: ${response.statusCode}';
      }
    } catch (e) {
      message = 'Failed to connect to backend: $e';
    }

    setState(() {
      _message = message;
    });
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        backgroundColor: Theme.of(context).colorScheme.inversePrimary,
        title: const Text('Flutter + Spring Boot Demo'),
      ),
      body: Center(
        child: Padding(
          padding: const EdgeInsets.all(16.0),
          child: Column(
            mainAxisAlignment: MainAxisAlignment.center,
            children: <Widget>[
              const Text(
                'Message from backend:',
                style: TextStyle(fontSize: 18),
              ),
              const SizedBox(height: 16),
              Text(
                _message,
                style: Theme.of(context).textTheme.headlineMedium,
                textAlign: TextAlign.center,
              ),
            ],
          ),
        ),
      ),
      floatingActionButton: FloatingActionButton(
        onPressed: _fetchMessage,
        tooltip: 'Refresh',
        child: const Icon(Icons.refresh),
      ),
    );
  }
}
