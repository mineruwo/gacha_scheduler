import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:frontend_mobile/providers/auth_providers.dart';
import 'package:frontend_mobile/providers/board_providers.dart';
import 'package:frontend_mobile/theme/app_theme.dart';

const Map<String, String> _templateLabels = {
  'GUIDE': '공략',
  'QUESTION': '질문',
  'FREE': '자유',
};

class PostEditorScreen extends ConsumerStatefulWidget {
  final int channelId;

  const PostEditorScreen({super.key, required this.channelId});

  @override
  ConsumerState<PostEditorScreen> createState() => _PostEditorScreenState();
}

class _PostEditorScreenState extends ConsumerState<PostEditorScreen> {
  final _titleController = TextEditingController();
  final _contentController = TextEditingController();
  String _templateType = 'FREE';
  bool _submitting = false;
  String? _error;

  @override
  void dispose() {
    _titleController.dispose();
    _contentController.dispose();
    super.dispose();
  }

  Future<void> _submit() async {
    final title = _titleController.text.trim();
    final content = _contentController.text.trim();
    if (title.isEmpty || content.isEmpty) {
      setState(() => _error = '제목과 내용을 모두 입력해주세요.');
      return;
    }
    final token = ref.read(authControllerProvider).user?.token;
    if (token == null) return;

    setState(() {
      _submitting = true;
      _error = null;
    });
    try {
      await ref.read(boardRepositoryProvider).createPost(
            token,
            widget.channelId,
            title: title,
            content: content,
            templateType: _templateType,
          );
      if (!mounted) return;
      Navigator.of(context).pop(true);
    } catch (_) {
      setState(() => _error = '글 작성에 실패했습니다.');
    } finally {
      if (mounted) setState(() => _submitting = false);
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: const Text('글쓰기')),
      body: SingleChildScrollView(
        padding: const EdgeInsets.all(16.0),
        child: Container(
          padding: const EdgeInsets.all(20.0),
          decoration: AppTheme.softCard(),
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.stretch,
            children: [
              DropdownButtonFormField<String>(
                initialValue: _templateType,
                isExpanded: true,
                decoration: const InputDecoration(labelText: '템플릿'),
                items: _templateLabels.entries
                    .map((e) => DropdownMenuItem(value: e.key, child: Text(e.value)))
                    .toList(),
                onChanged: (value) {
                  if (value != null) setState(() => _templateType = value);
                },
              ),
              const SizedBox(height: 16),
              TextField(
                controller: _titleController,
                decoration: const InputDecoration(labelText: '제목'),
              ),
              const SizedBox(height: 16),
              TextField(
                controller: _contentController,
                decoration: const InputDecoration(labelText: '내용'),
                maxLines: 8,
              ),
              if (_error != null) ...[
                const SizedBox(height: 12),
                Text(_error!, style: const TextStyle(color: Colors.redAccent, fontSize: 13)),
              ],
              const SizedBox(height: 24),
              FilledButton(
                onPressed: _submitting ? null : _submit,
                child: _submitting
                    ? const SizedBox(
                        width: 20,
                        height: 20,
                        child: CircularProgressIndicator(strokeWidth: 2, color: Colors.white),
                      )
                    : const Text('등록'),
              ),
            ],
          ),
        ),
      ),
    );
  }
}
