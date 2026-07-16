class GachaBanner {
  final int id;
  final int gameId;
  final String gameName;
  final String name;
  final DateTime? startAt;
  final DateTime? endAt;
  final List<int> pickupCharacterIds;
  final int pityThreshold;
  final double rateUpRate;

  const GachaBanner({
    required this.id,
    required this.gameId,
    required this.gameName,
    required this.name,
    this.startAt,
    this.endAt,
    required this.pickupCharacterIds,
    required this.pityThreshold,
    required this.rateUpRate,
  });

  factory GachaBanner.fromJson(Map<String, dynamic> json) {
    return GachaBanner(
      id: json['id'] as int,
      gameId: json['gameId'] as int,
      gameName: json['gameName'] as String? ?? '',
      name: json['name'] as String,
      startAt: json['startAt'] != null ? DateTime.parse(json['startAt'] as String) : null,
      endAt: json['endAt'] != null ? DateTime.parse(json['endAt'] as String) : null,
      pickupCharacterIds: (json['pickupCharacterIds'] as List<dynamic>? ?? []).map((e) => e as int).toList(),
      pityThreshold: json['pityThreshold'] as int,
      rateUpRate: (json['rateUpRate'] as num? ?? 0).toDouble(),
    );
  }

  // Dropdown 등에서 refetch로 인스턴스가 바뀌어도 같은 배너로 취급되도록 id 기준 동등성 사용
  @override
  bool operator ==(Object other) => other is GachaBanner && other.id == id;

  @override
  int get hashCode => id.hashCode;

  Map<String, dynamic> toJson() {
    return {
      'id': id,
      'gameId': gameId,
      'gameName': gameName,
      'name': name,
      'startAt': startAt?.toIso8601String(),
      'endAt': endAt?.toIso8601String(),
      'pickupCharacterIds': pickupCharacterIds,
      'pityThreshold': pityThreshold,
      'rateUpRate': rateUpRate,
    };
  }
}
