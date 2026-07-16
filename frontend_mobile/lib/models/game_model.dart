class GameModel {
  final int id;
  final String gameCode;
  final String title;

  const GameModel({required this.id, required this.gameCode, required this.title});

  factory GameModel.fromJson(Map<String, dynamic> json) {
    return GameModel(
      id: json['id'] as int,
      gameCode: json['gameCode'] as String,
      title: json['title'] as String,
    );
  }
}
