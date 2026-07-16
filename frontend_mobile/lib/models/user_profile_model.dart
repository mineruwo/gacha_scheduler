class UserProfileModel {
  final int id;
  final String email;
  final String name;
  final String? profilePictureUrl;
  final String userCode;
  final String role;
  final DateTime createdAt;

  const UserProfileModel({
    required this.id,
    required this.email,
    required this.name,
    this.profilePictureUrl,
    required this.userCode,
    required this.role,
    required this.createdAt,
  });

  factory UserProfileModel.fromJson(Map<String, dynamic> json) {
    return UserProfileModel(
      id: json['id'] as int,
      email: json['email'] as String,
      name: json['name'] as String,
      profilePictureUrl: json['profilePictureUrl'] as String?,
      userCode: json['userCode'] as String,
      role: json['role'] as String,
      createdAt: DateTime.parse(json['createdAt'] as String),
    );
  }
}
