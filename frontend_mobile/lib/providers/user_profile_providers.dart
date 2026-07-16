import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:frontend_mobile/models/user_profile_model.dart';
import 'package:frontend_mobile/providers/auth_providers.dart';
import 'package:frontend_mobile/repositories/user_repository.dart';

final userRepositoryProvider = Provider<UserRepository>((ref) {
  return ApiUserRepository();
});

// 로그인 상태(토큰)가 바뀔 때마다 자동으로 다시 조회된다.
final userProfileProvider = FutureProvider<UserProfileModel>((ref) async {
  final token = ref.watch(authControllerProvider).user?.token;
  if (token == null) {
    throw StateError('로그인이 필요합니다.');
  }
  try {
    return await ref.watch(userRepositoryProvider).fetchMyProfile(token);
  } on UserApiException catch (e) {
    // 토큰이 다른 서버/DB 기준으로 발급됐거나 만료된 경우(401) 로컬 로그인 상태를 정리한다.
    // 그렇지 않으면 "로그인된 것처럼 보이지만 모든 요청이 실패하는" 상태에 갇히게 된다.
    if (e.statusCode == 401) {
      ref.read(authControllerProvider.notifier).logout();
    }
    rethrow;
  }
});
