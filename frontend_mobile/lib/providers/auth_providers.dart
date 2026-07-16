import 'dart:convert';

import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:frontend_mobile/managers/user_data_manager.dart';
import 'package:frontend_mobile/models/user_model.dart';
import 'package:frontend_mobile/repositories/auth_repository.dart';

const _authUserKey = 'auth_user_json';

final authRepositoryProvider = Provider<AuthRepository>((ref) {
  return ApiAuthRepository();
});

class AuthState {
  final UserModel? user;
  final bool isLoading;
  final String? error;

  const AuthState({this.user, this.isLoading = false, this.error});

  bool get isLoggedIn => user != null;

  AuthState copyWith({UserModel? user, bool? isLoading, String? error, bool clearError = false, bool clearUser = false}) {
    return AuthState(
      user: clearUser ? null : (user ?? this.user),
      isLoading: isLoading ?? this.isLoading,
      error: clearError ? null : (error ?? this.error),
    );
  }
}

class AuthController extends StateNotifier<AuthState> {
  final AuthRepository _repository;

  AuthController(this._repository) : super(const AuthState());

  // 앱 부트스트랩 단계에서 저장된 세션을 복원한다. 토큰 유효성은 인증이 필요한
  // 첫 API 호출에서 401로 확인되며, 그 시점에 로그아웃 처리하면 된다.
  Future<void> restoreSession() async {
    final json = UserDataManager.instance.getString(_authUserKey);
    if (json == null) return;
    try {
      state = state.copyWith(user: UserModel.fromJson(jsonDecode(json) as Map<String, dynamic>));
    } catch (_) {
      await UserDataManager.instance.remove(_authUserKey);
    }
  }

  Future<bool> login({required String email, required String password}) async {
    state = state.copyWith(isLoading: true, clearError: true);
    try {
      final user = await _repository.login(email: email, password: password);
      await _persist(user);
      state = state.copyWith(user: user, isLoading: false);
      return true;
    } catch (e) {
      state = state.copyWith(isLoading: false, error: e.toString());
      return false;
    }
  }

  Future<bool> signup({required String email, required String password, required String name}) async {
    state = state.copyWith(isLoading: true, clearError: true);
    try {
      final user = await _repository.signup(email: email, password: password, name: name);
      await _persist(user);
      state = state.copyWith(user: user, isLoading: false);
      return true;
    } catch (e) {
      state = state.copyWith(isLoading: false, error: e.toString());
      return false;
    }
  }

  Future<void> logout() async {
    await UserDataManager.instance.remove(_authUserKey);
    state = const AuthState();
  }

  Future<void> _persist(UserModel user) {
    return UserDataManager.instance.setString(_authUserKey, jsonEncode(user.toJson()));
  }
}

final authControllerProvider = StateNotifierProvider<AuthController, AuthState>((ref) {
  return AuthController(ref.watch(authRepositoryProvider));
});
