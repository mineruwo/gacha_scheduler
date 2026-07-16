class UserModel {
  final int id;
  final String email;
  final String name;
  final String? profilePictureUrl;
  final String userCode;
  final String role;
  final String token;

  const UserModel({
    required this.id,
    required this.email,
    required this.name,
    this.profilePictureUrl,
    required this.userCode,
    required this.role,
    required this.token,
  });

  factory UserModel.fromJson(Map<String, dynamic> json) {
    return UserModel(
      id: json['id'] as int,
      email: json['email'] as String,
      name: json['name'] as String,
      profilePictureUrl: json['profilePictureUrl'] as String?,
      userCode: json['userCode'] as String,
      role: json['role'] as String,
      token: json['token'] as String,
    );
  }

  Map<String, dynamic> toJson() {
    return {
      'id': id,
      'email': email,
      'name': name,
      'profilePictureUrl': profilePictureUrl,
      'userCode': userCode,
      'role': role,
      'token': token,
    };
  }
}
