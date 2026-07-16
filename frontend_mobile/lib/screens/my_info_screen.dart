import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:frontend_mobile/managers/localization_manager.dart';
import 'package:frontend_mobile/models/user_profile_model.dart';
import 'package:frontend_mobile/providers/auth_providers.dart';
import 'package:frontend_mobile/providers/user_profile_providers.dart';
import 'package:frontend_mobile/screens/login_screen.dart';
import 'package:frontend_mobile/theme/app_theme.dart';

class MyInfoScreen extends ConsumerWidget {
  const MyInfoScreen({super.key});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final l10n = LocalizationManager.instance;

    return Scaffold(
      appBar: AppBar(title: Text(l10n.getString('my_info_screen_title'))),
      body: SingleChildScrollView(
        padding: const EdgeInsets.all(16.0),
        child: const _AccountSection(),
      ),
    );
  }
}

class _AccountSection extends ConsumerWidget {
  const _AccountSection();

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final l10n = LocalizationManager.instance;
    final isLoggedIn = ref.watch(authControllerProvider).isLoggedIn;

    if (!isLoggedIn) {
      return Container(
        padding: const EdgeInsets.symmetric(horizontal: 24, vertical: 28),
        decoration: AppTheme.softCard(color: AppColors.pastelBlue.withValues(alpha: 0.25)),
        child: Column(
          mainAxisSize: MainAxisSize.min,
          children: [
            const Icon(Icons.account_circle_rounded, color: AppColors.primaryDark, size: 40),
            const SizedBox(height: 12),
            Text(
              l10n.getString('my_info_login_required'),
              style: const TextStyle(fontWeight: FontWeight.bold, color: AppColors.textPrimary),
            ),
            const SizedBox(height: 16),
            FilledButton(
              onPressed: () => Navigator.of(context).push(
                MaterialPageRoute(builder: (_) => const LoginScreen()),
              ),
              child: Text(l10n.getString('my_info_login_cta')),
            ),
          ],
        ),
      );
    }

    return const _ProfileCard();
  }
}

class _ProfileCard extends ConsumerStatefulWidget {
  const _ProfileCard();

  @override
  ConsumerState<_ProfileCard> createState() => _ProfileCardState();
}

class _ProfileCardState extends ConsumerState<_ProfileCard> {
  bool _editing = false;
  bool _saving = false;
  String? _error;
  final _nameController = TextEditingController();
  final _pictureController = TextEditingController();

  @override
  void dispose() {
    _nameController.dispose();
    _pictureController.dispose();
    super.dispose();
  }

  void _startEditing(UserProfileModel profile) {
    _nameController.text = profile.name;
    _pictureController.text = profile.profilePictureUrl ?? '';
    setState(() {
      _editing = true;
      _error = null;
    });
  }

  Future<void> _save() async {
    final name = _nameController.text.trim();
    if (name.isEmpty) {
      setState(() => _error = '이름을 입력해주세요.');
      return;
    }
    final token = ref.read(authControllerProvider).user?.token;
    if (token == null) return;

    setState(() {
      _saving = true;
      _error = null;
    });
    try {
      await ref.read(userRepositoryProvider).updateMyProfile(
            token,
            name: name,
            profilePictureUrl: _pictureController.text.trim().isEmpty ? null : _pictureController.text.trim(),
          );
      ref.invalidate(userProfileProvider);
      if (mounted) setState(() => _editing = false);
    } catch (_) {
      if (mounted) setState(() => _error = '프로필 수정에 실패했습니다.');
    } finally {
      if (mounted) setState(() => _saving = false);
    }
  }

  String _formatDate(DateTime d) {
    final local = d.toLocal();
    return '${local.year}.${local.month.toString().padLeft(2, '0')}.${local.day.toString().padLeft(2, '0')}';
  }

  @override
  Widget build(BuildContext context) {
    final l10n = LocalizationManager.instance;
    final profileAsync = ref.watch(userProfileProvider);

    return Container(
      padding: const EdgeInsets.all(20),
      decoration: AppTheme.softCard(),
      child: profileAsync.when(
        loading: () => const Padding(
          padding: EdgeInsets.symmetric(vertical: 20),
          child: Center(child: CircularProgressIndicator()),
        ),
        error: (_, _) => const Text('프로필을 불러오지 못했습니다.', style: TextStyle(color: AppColors.textSecondary)),
        data: (profile) {
          if (_editing) {
            return Column(
              crossAxisAlignment: CrossAxisAlignment.stretch,
              children: [
                TextField(
                  controller: _nameController,
                  decoration: const InputDecoration(labelText: '이름'),
                ),
                const SizedBox(height: 16),
                TextField(
                  controller: _pictureController,
                  decoration: const InputDecoration(labelText: '프로필 사진 URL'),
                ),
                if (_error != null) ...[
                  const SizedBox(height: 12),
                  Text(_error!, style: const TextStyle(color: Colors.redAccent, fontSize: 13)),
                ],
                const SizedBox(height: 20),
                Row(
                  children: [
                    Expanded(
                      child: OutlinedButton(
                        onPressed: _saving ? null : () => setState(() => _editing = false),
                        child: const Text('취소'),
                      ),
                    ),
                    const SizedBox(width: 12),
                    Expanded(
                      child: FilledButton(
                        onPressed: _saving ? null : _save,
                        child: _saving
                            ? const SizedBox(
                                width: 18,
                                height: 18,
                                child: CircularProgressIndicator(strokeWidth: 2, color: Colors.white),
                              )
                            : const Text('저장'),
                      ),
                    ),
                  ],
                ),
              ],
            );
          }

          return Column(
            crossAxisAlignment: CrossAxisAlignment.stretch,
            children: [
              Row(
                children: [
                  CircleAvatar(
                    radius: 24,
                    backgroundColor: AppColors.pastelPurple,
                    backgroundImage: profile.profilePictureUrl != null && profile.profilePictureUrl!.isNotEmpty
                        ? NetworkImage(profile.profilePictureUrl!)
                        : null,
                    child: profile.profilePictureUrl == null || profile.profilePictureUrl!.isEmpty
                        ? const Icon(Icons.person_rounded, color: AppColors.primaryDark)
                        : null,
                  ),
                  const SizedBox(width: 14),
                  Expanded(
                    child: Column(
                      crossAxisAlignment: CrossAxisAlignment.start,
                      children: [
                        Text(profile.name, style: const TextStyle(fontWeight: FontWeight.bold, fontSize: 16, color: AppColors.textPrimary)),
                        Text(profile.email, style: const TextStyle(fontSize: 13, color: AppColors.textSecondary)),
                      ],
                    ),
                  ),
                ],
              ),
              const SizedBox(height: 16),
              _InfoRow(label: '역할', value: profile.role),
              _InfoRow(label: '가입일', value: _formatDate(profile.createdAt)),
              const SizedBox(height: 16),
              FilledButton(
                onPressed: () => _startEditing(profile),
                child: const Text('정보 수정'),
              ),
              const SizedBox(height: 12),
              OutlinedButton(
                onPressed: () => ref.read(authControllerProvider.notifier).logout(),
                child: Text(l10n.getString('my_info_logout')),
              ),
            ],
          );
        },
      ),
    );
  }
}

class _InfoRow extends StatelessWidget {
  final String label;
  final String value;

  const _InfoRow({required this.label, required this.value});

  @override
  Widget build(BuildContext context) {
    return Padding(
      padding: const EdgeInsets.symmetric(vertical: 4),
      child: Row(
        children: [
          SizedBox(
            width: 56,
            child: Text(label, style: const TextStyle(fontSize: 13, color: AppColors.textSecondary)),
          ),
          Expanded(
            child: Text(value, style: const TextStyle(fontSize: 13, color: AppColors.textPrimary, fontWeight: FontWeight.w600)),
          ),
        ],
      ),
    );
  }
}
