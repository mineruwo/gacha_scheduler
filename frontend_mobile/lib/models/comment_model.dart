class CommentModel {
  final int id;
  final int postId;
  final int authorId;
  final String authorName;
  final String content;
  final int? parentCommentId;
  final DateTime createdAt;

  const CommentModel({
    required this.id,
    required this.postId,
    required this.authorId,
    required this.authorName,
    required this.content,
    this.parentCommentId,
    required this.createdAt,
  });

  factory CommentModel.fromJson(Map<String, dynamic> json) {
    return CommentModel(
      id: json['id'] as int,
      postId: json['postId'] as int,
      authorId: json['authorId'] as int,
      authorName: json['authorName'] as String,
      content: json['content'] as String,
      parentCommentId: json['parentCommentId'] as int?,
      createdAt: DateTime.parse(json['createdAt'] as String),
    );
  }
}
