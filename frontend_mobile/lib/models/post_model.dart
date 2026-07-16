class PostModel {
  final int id;
  final int channelId;
  final String? channelName;
  final int authorId;
  final String authorName;
  final String title;
  final String content;
  final String templateType;
  final int viewCount;
  final DateTime createdAt;

  const PostModel({
    required this.id,
    required this.channelId,
    this.channelName,
    required this.authorId,
    required this.authorName,
    required this.title,
    required this.content,
    required this.templateType,
    required this.viewCount,
    required this.createdAt,
  });

  factory PostModel.fromJson(Map<String, dynamic> json) {
    return PostModel(
      id: json['id'] as int,
      channelId: json['channelId'] as int,
      channelName: json['channelName'] as String?,
      authorId: json['authorId'] as int,
      authorName: json['authorName'] as String,
      title: json['title'] as String,
      content: json['content'] as String,
      templateType: json['templateType'] as String,
      viewCount: json['viewCount'] as int? ?? 0,
      createdAt: DateTime.parse(json['createdAt'] as String),
    );
  }
}

class PostPageModel {
  final List<PostModel> content;
  final int number;
  final int totalPages;

  const PostPageModel({required this.content, required this.number, required this.totalPages});

  factory PostPageModel.fromJson(Map<String, dynamic> json) {
    return PostPageModel(
      content: (json['content'] as List<dynamic>)
          .map((e) => PostModel.fromJson(e as Map<String, dynamic>))
          .toList(),
      number: json['number'] as int,
      totalPages: json['totalPages'] as int,
    );
  }
}
