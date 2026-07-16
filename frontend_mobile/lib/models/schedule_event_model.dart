class ScheduleEventModel {
  final int id;
  final String gameCode;
  final String? gameTitle;
  final String title;
  final String category;
  final DateTime startAt;
  final DateTime? endAt;
  final String? description;

  const ScheduleEventModel({
    required this.id,
    required this.gameCode,
    this.gameTitle,
    required this.title,
    required this.category,
    required this.startAt,
    this.endAt,
    this.description,
  });

  factory ScheduleEventModel.fromJson(Map<String, dynamic> json) {
    return ScheduleEventModel(
      id: json['id'] as int,
      gameCode: json['gameCode'] as String,
      gameTitle: json['gameTitle'] as String?,
      title: json['title'] as String,
      category: json['category'] as String,
      startAt: DateTime.parse(json['startAt'] as String),
      endAt: json['endAt'] != null ? DateTime.parse(json['endAt'] as String) : null,
      description: json['description'] as String?,
    );
  }
}
