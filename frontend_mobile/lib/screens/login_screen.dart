import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:frontend_mobile/managers/localization_manager.dart';
import 'package:frontend_mobile/providers/auth_providers.dart';
import 'package:frontend_mobile/theme/app_theme.dart';

enum _AuthMode { login, signup }

class LoginScreen extends ConsumerStatefulWidget {
  const LoginScreen({super.key});

  @override
  ConsumerState<LoginScreen> createState() => _LoginScreenState();
}

class _LoginScreenState extends ConsumerState<LoginScreen> {
  _AuthMode _mode = _AuthMode.login;
  final _emailController = TextEditingController();
  final _passwordController = TextEditingController();
  final _nameController = TextEditingController();

  @override
  void dispose() {
    _emailController.dispose();
    _passwordController.dispose();
    _nameController.dispose();
    super.dispose();
  }

  Future<void> _submit() async {
    final l10n = LocalizationManager.instance;
    final controller = ref.read(authControllerProvider.notifier);
    final email = _emailController.text.trim();
    final password = _passwordController.text;

    bool success;
    if (_mode == _AuthMode.login) {
      success = await controller.login(email: email, password: password);
    } else {
      success = await controller.signup(email: email, password: password, name: _nameController.text.trim());
    }

    if (!mounted) return;
    if (success) {
      ScaffoldMessenger.of(context).showSnackBar(
        SnackBar(content: Text(l10n.getString('login_success'))),
      );
      Navigator.of(context).maybePop();
    }
  }

  @override
  Widget build(BuildContext context) {
    final l10n = LocalizationManager.instance;
    final authState = ref.watch(authControllerProvider);
    final isSignup = _mode == _AuthMode.signup;

    return Scaffold(
      appBar: AppBar(title: Text(l10n.getString('login_screen_title'))),
      body: SingleChildScrollView(
        padding: const EdgeInsets.all(16.0),
        child: Container(
          padding: const EdgeInsets.all(20.0),
          decoration: AppTheme.softCard(),
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.stretch,
            children: [
              Row(
                children: [
                  Expanded(child: _ModeTab(
                    label: l10n.getString('login_tab_login'),
                    selected: !isSignup,
                    onTap: () => setState(() => _mode = _AuthMode.login),
                  )),
                  const SizedBox(width: 8),
                  Expanded(child: _ModeTab(
                    label: l10n.getString('login_tab_signup'),
                    selected: isSignup,
                    onTap: () => setState(() => _mode = _AuthMode.signup),
                  )),
                ],
              ),
              const SizedBox(height: 20),
              TextField(
                controller: _emailController,
                keyboardType: TextInputType.emailAddress,
                decoration: InputDecoration(labelText: l10n.getString('login_email_label')),
              ),
              if (isSignup) ...[
                const SizedBox(height: 16),
                TextField(
                  controller: _nameController,
                  decoration: InputDecoration(labelText: l10n.getString('login_name_label')),
                ),
              ],
              const SizedBox(height: 16),
              TextField(
                controller: _passwordController,
                obscureText: true,
                decoration: InputDecoration(labelText: l10n.getString('login_password_label')),
              ),
              if (authState.error != null) ...[
                const SizedBox(height: 12),
                Text(
                  authState.error!,
                  style: const TextStyle(color: Colors.redAccent, fontSize: 13),
                ),
              ],
              const SizedBox(height: 24),
              FilledButton(
                onPressed: authState.isLoading ? null : _submit,
                child: authState.isLoading
                    ? const SizedBox(
                        width: 20,
                        height: 20,
                        child: CircularProgressIndicator(strokeWidth: 2, color: Colors.white),
                      )
                    : Text(isSignup ? l10n.getString('login_submit_signup') : l10n.getString('login_submit_login')),
              ),
            ],
          ),
        ),
      ),
    );
  }
}

class _ModeTab extends StatelessWidget {
  final String label;
  final bool selected;
  final VoidCallback onTap;

  const _ModeTab({required this.label, required this.selected, required this.onTap});

  @override
  Widget build(BuildContext context) {
    return GestureDetector(
      onTap: onTap,
      child: AnimatedContainer(
        duration: const Duration(milliseconds: 150),
        padding: const EdgeInsets.symmetric(vertical: 12),
        decoration: BoxDecoration(
          color: selected ? AppColors.primary : AppColors.background,
          borderRadius: BorderRadius.circular(14),
        ),
        alignment: Alignment.center,
        child: Text(
          label,
          style: TextStyle(
            color: selected ? Colors.white : AppColors.textSecondary,
            fontWeight: FontWeight.bold,
          ),
        ),
      ),
    );
  }
}
