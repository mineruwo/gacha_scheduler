class GachaCharacter {
  final int id;
  final int gameId;
  final String name;
  final int rarity;
  final String? iconUrl;
  final double weight;

  const GachaCharacter({
    required this.id,
    required this.gameId,
    required this.name,
    required this.rarity,
    this.iconUrl,
    required this.weight,
  });

  factory GachaCharacter.fromJson(Map<String, dynamic> json) {
    return GachaCharacter(
      id: json['id'] as int,
      gameId: json['gameId'] as int,
      name: json['name'] as String,
      rarity: json['rarity'] as int,
      iconUrl: json['iconUrl'] as String?,
      weight: (json['weight'] as num).toDouble(),
    );
  }

  Map<String, dynamic> toJson() {
    return {
      'id': id,
      'gameId': gameId,
      'name': name,
      'rarity': rarity,
      'iconUrl': iconUrl,
      'weight': weight,
    };
  }
}
