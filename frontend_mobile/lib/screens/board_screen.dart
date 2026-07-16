import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:frontend_mobile/managers/localization_manager.dart';
import 'package:frontend_mobile/models/channel_model.dart';
import 'package:frontend_mobile/models/comment_model.dart';
import 'package:frontend_mobile/models/post_model.dart';
import 'package:frontend_mobile/providers/auth_providers.dart';
import 'package:frontend_mobile/providers/board_providers.dart';
import 'package:frontend_mobile/screens/login_screen.dart';
import 'package:frontend_mobile/screens/post_editor_screen.dart';
import 'package:frontend_mobile/theme/app_theme.dart';

const Map<String, String> _templateLabels = {
  'GUIDE': '공략',
  'QUESTION': '질문',
  'FREE': '자유',
};

bool _canModify(AuthState auth, int authorId) {
  final user = auth.user;
  if (user == null) return false;
  return user.id == authorId || user.role == 'SUB_ADMIN' || user.role == 'MAIN_ADMIN';
}

String _formatDateTime(DateTime d) {
  final local = d.toLocal();
  String pad(int n) => n.toString().padLeft(2, '0');
  return '${local.year}.${pad(local.month)}.${pad(local.day)} ${pad(local.hour)}:${pad(local.minute)}';
}

class BoardScreen extends ConsumerStatefulWidget {
  const BoardScreen({super.key});

  @override
  ConsumerState<BoardScreen> createState() => _BoardScreenState();
}

class _BoardScreenState extends ConsumerState<BoardScreen> {
  int? _selectedChannelId;
  int _pageNumber = 0;
  String _searchQuery = '';
  int? _openPostId;
  final _searchController = TextEditingController();

  @override
  void dispose() {
    _searchController.dispose();
    super.dispose();
  }

  void _selectChannel(int channelId) {
    setState(() {
      _selectedChannelId = channelId;
      _pageNumber = 0;
      _searchQuery = '';
      _searchController.clear();
      _openPostId = null;
    });
  }

  @override
  Widget build(BuildContext context) {
    final l10n = LocalizationManager.instance;
    final channelsAsync = ref.watch(channelsProvider);

    return Scaffold(
      appBar: AppBar(title: Text(l10n.getString('board_screen_title'))),
      body: channelsAsync.when(
        loading: () => const Center(child: CircularProgressIndicator()),
        error: (_, _) => const Center(child: Text('채널 목록을 불러오지 못했습니다.')),
        data: (channels) {
          if (channels.isEmpty) {
            return const Center(child: Text('등록된 채널이 없습니다.'));
          }
          _selectedChannelId ??= channels.first.id;

          return Column(
            children: [
              _ChannelTabs(
                channels: channels,
                selectedId: _selectedChannelId!,
                onSelect: _selectChannel,
              ),
              Expanded(
                child: _openPostId == null
                    ? _PostListView(
                        channelId: _selectedChannelId!,
                        pageNumber: _pageNumber,
                        searchQuery: _searchQuery,
                        searchController: _searchController,
                        onPageChanged: (page) => setState(() => _pageNumber = page),
                        onSearch: (query) => setState(() {
                          _pageNumber = 0;
                          _searchQuery = query;
                        }),
                        onOpenPost: (id) => setState(() => _openPostId = id),
                      )
                    : _PostDetailView(
                        postId: _openPostId!,
                        onBack: () => setState(() => _openPostId = null),
                      ),
              ),
            ],
          );
        },
      ),
    );
  }
}

class _ChannelTabs extends StatelessWidget {
  final List<ChannelModel> channels;
  final int selectedId;
  final void Function(int) onSelect;

  const _ChannelTabs({required this.channels, required this.selectedId, required this.onSelect});

  @override
  Widget build(BuildContext context) {
    return SizedBox(
      height: 48,
      child: ListView.separated(
        scrollDirection: Axis.horizontal,
        padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 8),
        itemCount: channels.length,
        separatorBuilder: (_, _) => const SizedBox(width: 8),
        itemBuilder: (context, index) {
          final channel = channels[index];
          final selected = channel.id == selectedId;
          final label = channel.gameName != null ? '${channel.gameName} · ${channel.name}' : channel.name;
          return ChoiceChip(
            label: Text(label),
            selected: selected,
            onSelected: (_) => onSelect(channel.id),
            selectedColor: AppColors.primary,
            backgroundColor: Colors.white,
            labelStyle: TextStyle(
              color: selected ? Colors.white : AppColors.textPrimary,
              fontWeight: FontWeight.w600,
              fontSize: 12,
            ),
            side: BorderSide(color: selected ? AppColors.primary : const Color(0xFFE6E1F5)),
            shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(20)),
          );
        },
      ),
    );
  }
}

class _PostListView extends ConsumerWidget {
  final int channelId;
  final int pageNumber;
  final String searchQuery;
  final TextEditingController searchController;
  final void Function(int) onPageChanged;
  final void Function(String) onSearch;
  final void Function(int) onOpenPost;

  const _PostListView({
    required this.channelId,
    required this.pageNumber,
    required this.searchQuery,
    required this.searchController,
    required this.onPageChanged,
    required this.onSearch,
    required this.onOpenPost,
  });

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final query = PostsQuery(channelId: channelId, page: pageNumber, query: searchQuery);
    final postsAsync = ref.watch(postsProvider(query));
    final isLoggedIn = ref.watch(authControllerProvider).isLoggedIn;

    return Column(
      children: [
        Padding(
          padding: const EdgeInsets.fromLTRB(16, 8, 16, 8),
          child: Row(
            children: [
              Expanded(
                child: TextField(
                  controller: searchController,
                  decoration: const InputDecoration(
                    hintText: '제목/내용 검색',
                    isDense: true,
                  ),
                  onSubmitted: onSearch,
                ),
              ),
              const SizedBox(width: 8),
              IconButton(
                onPressed: () => onSearch(searchController.text),
                icon: const Icon(Icons.search_rounded, color: AppColors.primaryDark),
              ),
              if (isLoggedIn)
                IconButton(
                  onPressed: () async {
                    final created = await Navigator.of(context).push<bool>(
                      MaterialPageRoute(builder: (_) => PostEditorScreen(channelId: channelId)),
                    );
                    if (created == true) ref.invalidate(postsProvider(query));
                  },
                  icon: const Icon(Icons.edit_note_rounded, color: AppColors.primaryDark),
                ),
            ],
          ),
        ),
        Expanded(
          child: postsAsync.when(
            loading: () => const Center(child: CircularProgressIndicator()),
            error: (_, _) => const Center(child: Text('게시글 목록을 불러오지 못했습니다.')),
            data: (page) {
              if (page.content.isEmpty) {
                return Center(
                  child: Text(
                    searchQuery.isNotEmpty ? "'$searchQuery' 검색 결과가 없습니다." : '게시글이 없습니다.',
                    style: const TextStyle(color: AppColors.textSecondary),
                  ),
                );
              }
              return ListView.separated(
                padding: const EdgeInsets.symmetric(horizontal: 16),
                itemCount: page.content.length,
                separatorBuilder: (_, _) => const SizedBox(height: 8),
                itemBuilder: (context, index) => _PostListTile(
                  post: page.content[index],
                  onTap: () => onOpenPost(page.content[index].id),
                ),
              );
            },
          ),
        ),
        postsAsync.maybeWhen(
          data: (page) => Padding(
            padding: const EdgeInsets.symmetric(vertical: 8),
            child: Row(
              mainAxisAlignment: MainAxisAlignment.center,
              children: [
                IconButton(
                  onPressed: pageNumber == 0 ? null : () => onPageChanged(pageNumber - 1),
                  icon: const Icon(Icons.chevron_left_rounded),
                ),
                Text('${page.number + 1} / ${page.totalPages < 1 ? 1 : page.totalPages}'),
                IconButton(
                  onPressed: pageNumber + 1 >= page.totalPages ? null : () => onPageChanged(pageNumber + 1),
                  icon: const Icon(Icons.chevron_right_rounded),
                ),
              ],
            ),
          ),
          orElse: () => const SizedBox.shrink(),
        ),
      ],
    );
  }
}

class _PostListTile extends StatelessWidget {
  final PostModel post;
  final VoidCallback onTap;

  const _PostListTile({required this.post, required this.onTap});

  @override
  Widget build(BuildContext context) {
    return InkWell(
      borderRadius: BorderRadius.circular(16),
      onTap: onTap,
      child: Container(
        padding: const EdgeInsets.all(14),
        decoration: AppTheme.softCard(),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Row(
              children: [
                Container(
                  padding: const EdgeInsets.symmetric(horizontal: 8, vertical: 2),
                  decoration: BoxDecoration(
                    color: AppColors.pastelBlue.withValues(alpha: 0.4),
                    borderRadius: BorderRadius.circular(8),
                  ),
                  child: Text(
                    _templateLabels[post.templateType] ?? post.templateType,
                    style: const TextStyle(fontSize: 11, color: AppColors.textPrimary, fontWeight: FontWeight.w600),
                  ),
                ),
                const SizedBox(width: 8),
                Expanded(
                  child: Text(
                    post.title,
                    maxLines: 1,
                    overflow: TextOverflow.ellipsis,
                    style: const TextStyle(fontWeight: FontWeight.bold, color: AppColors.textPrimary),
                  ),
                ),
              ],
            ),
            const SizedBox(height: 6),
            Text(
              '${post.authorName} · 조회 ${post.viewCount} · ${_formatDateTime(post.createdAt)}',
              style: const TextStyle(fontSize: 11, color: AppColors.textSecondary),
            ),
          ],
        ),
      ),
    );
  }
}

class _PostDetailView extends ConsumerStatefulWidget {
  final int postId;
  final VoidCallback onBack;

  const _PostDetailView({required this.postId, required this.onBack});

  @override
  ConsumerState<_PostDetailView> createState() => _PostDetailViewState();
}

class _PostDetailViewState extends ConsumerState<_PostDetailView> {
  final _commentController = TextEditingController();
  int? _replyTargetId;
  bool _submittingComment = false;

  @override
  void dispose() {
    _commentController.dispose();
    super.dispose();
  }

  Future<void> _submitComment() async {
    final content = _commentController.text.trim();
    if (content.isEmpty) return;
    final token = ref.read(authControllerProvider).user?.token;
    if (token == null) return;

    setState(() => _submittingComment = true);
    try {
      await ref.read(boardRepositoryProvider).createComment(
            token,
            widget.postId,
            content: content,
            parentCommentId: _replyTargetId,
          );
      _commentController.clear();
      setState(() => _replyTargetId = null);
      ref.invalidate(commentsProvider(widget.postId));
    } catch (_) {
      if (mounted) {
        ScaffoldMessenger.of(context).showSnackBar(const SnackBar(content: Text('댓글 작성에 실패했습니다.')));
      }
    } finally {
      if (mounted) setState(() => _submittingComment = false);
    }
  }

  Future<void> _deleteComment(int commentId) async {
    final confirmed = await _confirm(context, '댓글을 삭제할까요?');
    if (!confirmed) return;
    final token = ref.read(authControllerProvider).user?.token;
    if (token == null) return;
    try {
      await ref.read(boardRepositoryProvider).deleteComment(token, commentId);
      ref.invalidate(commentsProvider(widget.postId));
    } catch (_) {
      if (mounted) {
        ScaffoldMessenger.of(context).showSnackBar(const SnackBar(content: Text('댓글 삭제에 실패했습니다.')));
      }
    }
  }

  Future<void> _deletePost() async {
    final confirmed = await _confirm(context, '게시글을 삭제할까요?');
    if (!confirmed) return;
    final token = ref.read(authControllerProvider).user?.token;
    if (token == null) return;
    try {
      await ref.read(boardRepositoryProvider).deletePost(token, widget.postId);
      widget.onBack();
    } catch (_) {
      if (mounted) {
        ScaffoldMessenger.of(context).showSnackBar(const SnackBar(content: Text('게시글 삭제에 실패했습니다.')));
      }
    }
  }

  Future<bool> _confirm(BuildContext context, String message) async {
    final result = await showDialog<bool>(
      context: context,
      builder: (context) => AlertDialog(
        content: Text(message),
        actions: [
          TextButton(onPressed: () => Navigator.of(context).pop(false), child: const Text('취소')),
          TextButton(onPressed: () => Navigator.of(context).pop(true), child: const Text('삭제')),
        ],
      ),
    );
    return result ?? false;
  }

  @override
  Widget build(BuildContext context) {
    final postAsync = ref.watch(postDetailProvider(widget.postId));
    final auth = ref.watch(authControllerProvider);

    return postAsync.when(
      loading: () => const Center(child: CircularProgressIndicator()),
      error: (_, _) => const Center(child: Text('게시글을 불러오지 못했습니다.')),
      data: (post) {
        return SingleChildScrollView(
          padding: const EdgeInsets.all(16),
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              TextButton.icon(
                onPressed: widget.onBack,
                icon: const Icon(Icons.arrow_back_rounded, size: 18),
                label: const Text('목록으로'),
              ),
              Text(post.title, style: const TextStyle(fontSize: 18, fontWeight: FontWeight.bold, color: AppColors.textPrimary)),
              const SizedBox(height: 6),
              Text(
                '${_templateLabels[post.templateType] ?? post.templateType} · ${post.authorName} · ${_formatDateTime(post.createdAt)} · 조회 ${post.viewCount}',
                style: const TextStyle(fontSize: 12, color: AppColors.textSecondary),
              ),
              const SizedBox(height: 16),
              Container(
                width: double.infinity,
                padding: const EdgeInsets.all(16),
                decoration: AppTheme.softCard(),
                child: Text(post.content, style: const TextStyle(color: AppColors.textPrimary, height: 1.5)),
              ),
              if (_canModify(auth, post.authorId)) ...[
                const SizedBox(height: 12),
                OutlinedButton(onPressed: _deletePost, child: const Text('삭제')),
              ],
              const SizedBox(height: 24),
              _CommentSection(
                postId: widget.postId,
                replyTargetId: _replyTargetId,
                onReply: (id) => setState(() => _replyTargetId = id),
                onCancelReply: () => setState(() => _replyTargetId = null),
                onDeleteComment: _deleteComment,
              ),
              const SizedBox(height: 12),
              if (auth.isLoggedIn) ...[
                if (_replyTargetId != null)
                  Padding(
                    padding: const EdgeInsets.only(bottom: 8),
                    child: Row(
                      children: [
                        const Text('답글 작성 중', style: TextStyle(fontSize: 12, color: AppColors.textSecondary)),
                        TextButton(
                          onPressed: () => setState(() => _replyTargetId = null),
                          child: const Text('취소'),
                        ),
                      ],
                    ),
                  ),
                TextField(
                  controller: _commentController,
                  decoration: const InputDecoration(hintText: '댓글을 입력하세요'),
                  maxLines: 3,
                ),
                const SizedBox(height: 8),
                FilledButton(
                  onPressed: _submittingComment ? null : _submitComment,
                  child: const Text('등록'),
                ),
              ] else
                TextButton(
                  onPressed: () => Navigator.of(context).push(MaterialPageRoute(builder: (_) => const LoginScreen())),
                  child: const Text('로그인 후 댓글을 작성할 수 있습니다'),
                ),
            ],
          ),
        );
      },
    );
  }
}

class _CommentSection extends ConsumerWidget {
  final int postId;
  final int? replyTargetId;
  final void Function(int) onReply;
  final VoidCallback onCancelReply;
  final void Function(int) onDeleteComment;

  const _CommentSection({
    required this.postId,
    required this.replyTargetId,
    required this.onReply,
    required this.onCancelReply,
    required this.onDeleteComment,
  });

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final commentsAsync = ref.watch(commentsProvider(postId));
    final auth = ref.watch(authControllerProvider);

    return commentsAsync.when(
      loading: () => const Center(child: CircularProgressIndicator()),
      error: (_, _) => const Text('댓글을 불러오지 못했습니다.'),
      data: (comments) {
        final topLevel = comments.where((c) => c.parentCommentId == null).toList();
        List<CommentModel> repliesOf(int parentId) =>
            comments.where((c) => c.parentCommentId == parentId).toList();

        return Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Text('댓글 ${comments.length}', style: const TextStyle(fontWeight: FontWeight.bold, color: AppColors.textPrimary)),
            const SizedBox(height: 8),
            if (topLevel.isEmpty)
              const Text('첫 댓글을 남겨보세요.', style: TextStyle(color: AppColors.textSecondary))
            else
              ...topLevel.expand((comment) => [
                    _CommentTile(
                      comment: comment,
                      canDelete: _canModify(auth, comment.authorId),
                      canReply: auth.isLoggedIn,
                      onReply: () => onReply(comment.id),
                      onDelete: () => onDeleteComment(comment.id),
                    ),
                    ...repliesOf(comment.id).map((reply) => Padding(
                          padding: const EdgeInsets.only(left: 24),
                          child: _CommentTile(
                            comment: reply,
                            canDelete: _canModify(auth, reply.authorId),
                            canReply: false,
                            onReply: null,
                            onDelete: () => onDeleteComment(reply.id),
                          ),
                        )),
                  ]),
          ],
        );
      },
    );
  }
}

class _CommentTile extends StatelessWidget {
  final CommentModel comment;
  final bool canDelete;
  final bool canReply;
  final VoidCallback? onReply;
  final VoidCallback onDelete;

  const _CommentTile({
    required this.comment,
    required this.canDelete,
    required this.canReply,
    required this.onReply,
    required this.onDelete,
  });

  @override
  Widget build(BuildContext context) {
    return Padding(
      padding: const EdgeInsets.symmetric(vertical: 6),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Row(
            children: [
              Text(comment.authorName, style: const TextStyle(fontWeight: FontWeight.bold, fontSize: 12, color: AppColors.textPrimary)),
              const SizedBox(width: 8),
              Text(_formatDateTime(comment.createdAt), style: const TextStyle(fontSize: 11, color: AppColors.textSecondary)),
            ],
          ),
          const SizedBox(height: 2),
          Text(comment.content, style: const TextStyle(color: AppColors.textPrimary)),
          Row(
            children: [
              if (canReply)
                TextButton(onPressed: onReply, style: _tinyButtonStyle, child: const Text('답글')),
              if (canDelete)
                TextButton(onPressed: onDelete, style: _tinyButtonStyle, child: const Text('삭제')),
            ],
          ),
        ],
      ),
    );
  }

  static final _tinyButtonStyle = TextButton.styleFrom(
    minimumSize: Size.zero,
    padding: const EdgeInsets.symmetric(horizontal: 8, vertical: 0),
    tapTargetSize: MaterialTapTargetSize.shrinkWrap,
  );
}
