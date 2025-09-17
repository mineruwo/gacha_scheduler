import 'dart:convert';
import 'package:flutter/material.dart';

class CommonUtils {
  static String toJson(Map<String, dynamic> data) {
    return jsonEncode(data);
  }

  static String objectToString(Object? obj) {
    return obj?.toString() ?? 'null';
  }

  // Email validation regex
  static bool isValidEmail(String email) {
    final RegExp emailRegExp = RegExp(
        r"^[a-zA-Z0-9.a-zA-Z0-9.!#$%&'*+-/=?^_`{|}~]+@[a-zA-Z0-9]+\.[a-zA-Z]+"
    );
    return emailRegExp.hasMatch(email);
  }

  // Korean Phone number validation regex (e.g., 010-1234-5678 or 01012345678)
  static bool isValidPhoneNumber(String phoneNumber) {
    final RegExp phoneRegExp = RegExp(r"^01(?:0|1|[6-9])(?:\d{3}|\d{4})\d{4}$");
    return phoneRegExp.hasMatch(phoneNumber.replaceAll('-', '')); // Remove hyphens for validation
  }

  // ID validation regex (alphanumeric, 4-20 characters)
  static bool isValidId(String id) {
    final RegExp idRegExp = RegExp(r"^[a-zA-Z0-9]{4,20}$");
    return idRegExp.hasMatch(id);
  }

  // Password validation regex (at least 8 characters, one uppercase, one lowercase, one number, one special character)
  static bool isValidPassword(String password) {
    final RegExp passwordRegExp = RegExp(
        r"^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[!@#$%^&*()_+])[A-Za-z\d!@#$%^&*()_+]{8,}$"
    );
    return passwordRegExp.hasMatch(password);
  }

  // Check if the current system theme is dark mode
  static bool isDarkMode(BuildContext context) {
    return MediaQuery.of(context).platformBrightness == Brightness.dark;
  }

  // Check if the current system theme is light mode
  static bool isLightMode(BuildContext context) {
    return MediaQuery.of(context).platformBrightness == Brightness.light;
  }

  // Show a simple loading dialog
  static Future<void> showLoadingDialog(BuildContext context) async {
    return showDialog<void>(
      context: context,
      barrierDismissible: false, // User must not be able to dismiss it
      builder: (BuildContext context) {
        return const AlertDialog(
          content: Row(
            mainAxisSize: MainAxisSize.min,
            children: [
              CircularProgressIndicator(),
              SizedBox(width: 20),
              Text("Loading..."),
            ],
          ),
        );
      },
    );
  }

  // Hide the currently shown loading dialog
  static void hideLoadingDialog(BuildContext context) {
    Navigator.of(context, rootNavigator: true).pop();
  }

  // Show a custom modal dialog with provided content
  static Future<T?> showCustomModal<T>(BuildContext context, Widget content) {
    return showDialog<T>(
      context: context,
      builder: (BuildContext context) {
        return content;
      },
    );
  }

  // Show an error dialog with a message and optional title
  static Future<void> showErrorDialog(BuildContext context, String message, {String title = '오류'}) async {
    return showDialog<void>(
      context: context,
      builder: (BuildContext context) {
        return AlertDialog(
          title: Text(title),
          content: Text(message),
          actions: <Widget>[
            TextButton(
              child: const Text('확인'),
              onPressed: () {
                Navigator.of(context).pop();
              },
            ),
          ],
        );
      },
    );
  }
}