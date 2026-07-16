import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:frontend_mobile/models/channel_model.dart';
import 'package:frontend_mobile/models/comment_model.dart';
import 'package:frontend_mobile/models/post_model.dart';
import 'package:frontend_mobile/repositories/board_repository.dart';

final boardRepositoryProvider = Provider<BoardRepository>((ref) {
  return ApiBoardRepository();
});

final channelsProvider = FutureProvider<List<ChannelModel>>((ref) {
  return ref.watch(boardRepositoryProvider).fetchChannels();
});

class PostsQuery {
  final int channelId;
  final int page;
  final String query;

  const PostsQuery({required this.channelId, required this.page, this.query = ''});

  @override
  bool operator ==(Object other) =>
      other is PostsQuery && other.channelId == channelId && other.page == page && other.query == query;

  @override
  int get hashCode => Object.hash(channelId, page, query);
}

final postsProvider = FutureProvider.family<PostPageModel, PostsQuery>((ref, query) {
  return ref.watch(boardRepositoryProvider).fetchPosts(query.channelId, page: query.page, query: query.query);
});

final postDetailProvider = FutureProvider.family<PostModel, int>((ref, postId) {
  return ref.watch(boardRepositoryProvider).fetchPost(postId);
});

final commentsProvider = FutureProvider.family<List<CommentModel>, int>((ref, postId) {
  return ref.watch(boardRepositoryProvider).fetchComments(postId);
});
