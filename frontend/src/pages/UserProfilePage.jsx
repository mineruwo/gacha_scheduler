import React from 'react';
import { useAuth } from '../context/AuthContext';

function UserProfilePage() {
  const { user } = useAuth();

  if (!user) {
    return <div>사용자 정보를 불러오는 중...</div>;
  }

  return (
    <div>
      <h1>내 정보</h1>
      <p><strong>이름:</strong> {user.name}</p>
      <p><strong>이메일:</strong> {user.email}</p>
      {user.profilePictureUrl && (
        <p><strong>프로필 사진:</strong> <img src={user.profilePictureUrl} alt="Profile" style={{ width: '50px', height: '50px', borderRadius: '50%' }} /></p>
      )}
      <p><strong>역할:</strong> {user.role}</p>
      <p><strong>가입일:</strong> {new Date(user.createdAt).toLocaleDateString()}</p>
    </div>
  );
}

export default UserProfilePage;
