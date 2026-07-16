class ChannelModel {
  final int id;
  final int? gameId;
  final String? gameName;
  final String name;
  final String? description;

  const ChannelModel({
    required this.id,
    this.gameId,
    this.gameName,
    required this.name,
    this.description,
  });

  factory ChannelModel.fromJson(Map<String, dynamic> json) {
    return ChannelModel(
      id: json['id'] as int,
      gameId: json['gameId'] as int?,
      gameName: json['gameName'] as String?,
      name: json['name'] as String,
      description: json['description'] as String?,
    );
  }
}
