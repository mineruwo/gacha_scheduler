import 'dart:convert';

import 'package:http/http.dart' as http;
import 'package:frontend_mobile/config/app_config.dart';
import 'package:frontend_mobile/models/user_profile_model.dart';

abstract class UserRepository {
  Future<UserProfileModel> fetchMyProfile(String token);

  Future<UserProfileModel> updateMyProfile(String token, {required String name, String? profilePictureUrl});
}

class ApiUserRepository implements UserRepository {
  final http.Client _client;

  ApiUserRepository({http.Client? client}) : _client = client ?? http.Client();

  Uri _uri(String path) => Uri.parse('${AppConfig.apiBaseUrl}$path');

  Map<String, String> _authHeaders(String token) => {
        'Content-Type': 'application/json',
        'Authorization': 'Bearer $token',
      };

  @override
  Future<UserProfileModel> fetchMyProfile(String token) async {
    final response = await _client.get(_uri('/api/users/me'), headers: _authHeaders(token));
    _ensureOk(response);
    return UserProfileModel.fromJson(jsonDecode(utf8.decode(response.bodyBytes)));
  }

  @override
  Future<UserProfileModel> updateMyProfile(String token, {required String name, String? profilePictureUrl}) async {
    final response = await _client.put(
      _uri('/api/users/me'),
      headers: _authHeaders(token),
      body: jsonEncode({'name': name, 'profilePictureUrl': profilePictureUrl}),
    );
    _ensureOk(response);
    return UserProfileModel.fromJson(jsonDecode(utf8.decode(response.bodyBytes)));
  }

  void _ensureOk(http.Response response) {
    if (response.statusCode < 200 || response.statusCode >= 300) {
      throw UserApiException(response.statusCode, response.body);
    }
  }
}

class UserApiException implements Exception {
  final int statusCode;
  final String body;

  UserApiException(this.statusCode, this.body);

  @override
  String toString() => 'UserApiException($statusCode): $body';
}
