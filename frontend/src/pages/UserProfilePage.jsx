import React, { useEffect, useState } from 'react';
import { useAuth } from '../context/AuthContext';
import { userApi } from '../api/userApi';
import './UserProfilePage.css';

function UserProfilePage() {
  const { user, token, login } = useAuth();
  const [profile, setProfile] = useState(null);
  const [form, setForm] = useState({ name: '', profilePictureUrl: '' });
  const [editing, setEditing] = useState(false);
  const [error, setError] = useState(null);
  const [saving, setSaving] = useState(false);

  useEffect(() => {
    userApi
      .fetchMyProfile()
      .then((data) => {
        setProfile(data);
        setForm({ name: data.name ?? '', profilePictureUrl: data.profilePictureUrl ?? '' });
      })
      .catch(() => setError('프로필을 불러오지 못했습니다.'));
  }, []);

  const submit = async (event) => {
    event.preventDefault();
    setError(null);
    setSaving(true);
    try {
      const updated = await userApi.updateMyProfile(form);
      setProfile(updated);
      setEditing(false);
      // AuthContext/localStorage에 저장된 유저 정보도 함께 갱신 (헤더 등에서 즉시 반영되도록)
      login({ ...user, name: updated.name, profilePictureUrl: updated.profilePictureUrl }, token);
    } catch {
      setError('프로필 수정에 실패했습니다.');
    } finally {
      setSaving(false);
    }
  };

  if (profile == null) {
    return <div>{error ?? '사용자 정보를 불러오는 중...'}</div>;
  }

  return (
    <div className="profile-page">
      <h1>내 정보</h1>

      {error && <p className="profile-error">{error}</p>}

      {!editing ? (
        <div className="profile-view">
          {profile.profilePictureUrl && (
            <img className="profile-avatar" src={profile.profilePictureUrl} alt="프로필" />
          )}
          <p><strong>이름:</strong> {profile.name}</p>
          <p><strong>이메일:</strong> {profile.email}</p>
          <p><strong>역할:</strong> {profile.role}</p>
          <p><strong>가입일:</strong> {new Date(profile.createdAt).toLocaleDateString()}</p>
          <button type="button" onClick={() => setEditing(true)}>정보 수정</button>
          <p className="profile-hint">
            구글 로그인으로 재로그인하면 닉네임/프로필 사진이 구글 계정 정보로 되돌아갈 수 있습니다.
          </p>
        </div>
      ) : (
        <form className="profile-form" onSubmit={submit}>
          <label>
            이름
            <input
              type="text"
              required
              value={form.name}
              onChange={(e) => setForm({ ...form, name: e.target.value })}
            />
          </label>
          <label>
            프로필 사진 URL
            <input
              type="text"
              value={form.profilePictureUrl}
              onChange={(e) => setForm({ ...form, profilePictureUrl: e.target.value })}
            />
          </label>
          <div className="profile-actions">
            <button type="submit" className="submit" disabled={saving}>
              {saving ? '저장 중...' : '저장'}
            </button>
            <button type="button" onClick={() => setEditing(false)}>취소</button>
          </div>
        </form>
      )}
    </div>
  );
}

export default UserProfilePage;
