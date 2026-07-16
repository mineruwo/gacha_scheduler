import 'dart:convert';

import 'package:http/http.dart' as http;
import 'package:frontend_mobile/config/app_config.dart';
import 'package:frontend_mobile/models/channel_model.dart';
import 'package:frontend_mobile/models/comment_model.dart';
import 'package:frontend_mobile/models/post_model.dart';

abstract class BoardRepository {
  Future<List<ChannelModel>> fetchChannels();

  Future<PostPageModel> fetchPosts(int channelId, {required int page, String? query});

  Future<PostModel> createPost(
    String token,
    int channelId, {
    required String title,
    required String content,
    required String templateType,
  });

  Future<PostModel> fetchPost(int postId);

  Future<void> deletePost(String token, int postId);

  Future<List<CommentModel>> fetchComments(int postId);

  Future<CommentModel> createComment(String token, int postId, {required String content, int? parentCommentId});

  Future<void> deleteComment(String token, int commentId);
}

class ApiBoardRepository implements BoardRepository {
  final http.Client _client;

  ApiBoardRepository({http.Client? client}) : _client = client ?? http.Client();

  Uri _uri(String path, [Map<String, String>? query]) =>
      Uri.parse('${AppConfig.apiBaseUrl}$path').replace(queryParameters: query);

  Map<String, String> _authHeaders(String token) => {
        'Content-Type': 'application/json',
        'Authorization': 'Bearer $token',
      };

  @override
  Future<List<ChannelModel>> fetchChannels() async {
    final response = await _client.get(_uri('/api/channels'));
    _ensureOk(response);
    final List<dynamic> body = jsonDecode(utf8.decode(response.bodyBytes));
    return body.map((e) => ChannelModel.fromJson(e as Map<String, dynamic>)).toList();
  }

  @override
  Future<PostPageModel> fetchPosts(int channelId, {required int page, String? query}) async {
    final params = {'page': '$page', 'size': '20'};
    if (query != null && query.trim().isNotEmpty) params['query'] = query.trim();
    final response = await _client.get(_uri('/api/channels/$channelId/posts', params));
    _ensureOk(response);
    return PostPageModel.fromJson(jsonDecode(utf8.decode(response.bodyBytes)));
  }

  @override
  Future<PostModel> createPost(
    String token,
    int channelId, {
    required String title,
    required String content,
    required String templateType,
  }) async {
    final response = await _client.post(
      _uri('/api/channels/$channelId/posts'),
      headers: _authHeaders(token),
      body: jsonEncode({'title': title, 'content': content, 'templateType': templateType}),
    );
    _ensureOk(response);
    return PostModel.fromJson(jsonDecode(utf8.decode(response.bodyBytes)));
  }

  @override
  Future<PostModel> fetchPost(int postId) async {
    final response = await _client.get(_uri('/api/posts/$postId'));
    _ensureOk(response);
    return PostModel.fromJson(jsonDecode(utf8.decode(response.bodyBytes)));
  }

  @override
  Future<void> deletePost(String token, int postId) async {
    final response = await _client.delete(_uri('/api/posts/$postId'), headers: _authHeaders(token));
    _ensureOk(response);
  }

  @override
  Future<List<CommentModel>> fetchComments(int postId) async {
    final response = await _client.get(_uri('/api/posts/$postId/comments'));
    _ensureOk(response);
    final List<dynamic> body = jsonDecode(utf8.decode(response.bodyBytes));
    return body.map((e) => CommentModel.fromJson(e as Map<String, dynamic>)).toList();
  }

  @override
  Future<CommentModel> createComment(String token, int postId, {required String content, int? parentCommentId}) async {
    final response = await _client.post(
      _uri('/api/posts/$postId/comments'),
      headers: _authHeaders(token),
      body: jsonEncode({'content': content, 'parentCommentId': parentCommentId}),
    );
    _ensureOk(response);
    return CommentModel.fromJson(jsonDecode(utf8.decode(response.bodyBytes)));
  }

  @override
  Future<void> deleteComment(String token, int commentId) async {
    final response = await _client.delete(_uri('/api/comments/$commentId'), headers: _authHeaders(token));
    _ensureOk(response);
  }

  void _ensureOk(http.Response response) {
    if (response.statusCode < 200 || response.statusCode >= 300) {
      throw BoardApiException(response.statusCode, response.body);
    }
  }
}

class BoardApiException implements Exception {
  final int statusCode;
  final String body;

  BoardApiException(this.statusCode, this.body);

  @override
  String toString() => 'BoardApiException($statusCode): $body';
}
