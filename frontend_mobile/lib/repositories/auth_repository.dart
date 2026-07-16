import 'dart:convert';

import 'package:http/http.dart' as http;
import 'package:frontend_mobile/config/app_config.dart';
import 'package:frontend_mobile/models/user_model.dart';

abstract class AuthRepository {
  Future<UserModel> login({required String email, required String password});

  Future<UserModel> signup({required String email, required String password, required String name});
}

class ApiAuthRepository implements AuthRepository {
  final http.Client _client;

  ApiAuthRepository({http.Client? client}) : _client = client ?? http.Client();

  Uri _uri(String path) => Uri.parse('${AppConfig.apiBaseUrl}$path');

  @override
  Future<UserModel> login({required String email, required String password}) async {
    final response = await _client.post(
      _uri('/api/auth/login'),
      headers: {'Content-Type': 'application/json'},
      body: jsonEncode({'email': email, 'password': password}),
    );
    if (response.statusCode == 401) {
      throw AuthApiException(response.statusCode, '이메일 또는 비밀번호가 올바르지 않습니다.');
    }
    _ensureOk(response);
    return UserModel.fromJson(jsonDecode(utf8.decode(response.bodyBytes)));
  }

  @override
  Future<UserModel> signup({required String email, required String password, required String name}) async {
    final response = await _client.post(
      _uri('/api/auth/signup'),
      headers: {'Content-Type': 'application/json'},
      body: jsonEncode({'email': email, 'password': password, 'name': name}),
    );
    if (response.statusCode == 409) {
      throw AuthApiException(response.statusCode, '이미 가입된 이메일입니다.');
    }
    if (response.statusCode == 400) {
      throw AuthApiException(response.statusCode, '입력값을 확인해주세요 (비밀번호 8자 이상).');
    }
    _ensureOk(response);
    return UserModel.fromJson(jsonDecode(utf8.decode(response.bodyBytes)));
  }

  void _ensureOk(http.Response response) {
    if (response.statusCode < 200 || response.statusCode >= 300) {
      throw AuthApiException(response.statusCode, '서버와 통신 중 오류가 발생했습니다.');
    }
  }
}

class AuthApiException implements Exception {
  final int statusCode;
  final String message;

  AuthApiException(this.statusCode, this.message);

  @override
  String toString() => message;
}
