import React, { useEffect, useState } from 'react';
import { Link, useOutletContext } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { userApi } from '../api/userApi';
import useMediaQuery from '../hooks/useMediaQuery';
import './UserProfilePage.css';

function UserProfilePage() {
  const { user, token, login, role, logout } = useAuth();
  const { theme, toggleTheme } = useOutletContext();
  const isMobile = useMediaQuery('(max-width: 768px)');
  const isSubAdmin = role === 'SUB_ADMIN' || role === 'MAIN_ADMIN';
  const isMainAdmin = role === 'MAIN_ADMIN';
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

  return (
    <div className="profile-page">
      <h1>내 정보</h1>

      {error && <p className="profile-error">{error}</p>}

      {profile == null ? (
        !error && <p>사용자 정보를 불러오는 중...</p>
      ) : !editing ? (
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

      {/* 하단 탭바에는 '기록'이 없으므로(Flutter 원본 탭 구성에 맞춰 '홈'을 넣음), 모바일에서는 여기서 접근 */}
      {isMobile && (
        <div className="profile-admin-menu">
          <h2>내 활동</h2>
          <ul>
            <li><Link to="/history">뽑기 기록</Link></li>
          </ul>
        </div>
      )}

      {/* 데스크톱은 Sidebar에 관리자 메뉴가 이미 있으므로, 하단 탭바만 쓰는 모바일 폭에서만 노출 */}
      {isMobile && (isSubAdmin || isMainAdmin) && (
        <div className="profile-admin-menu">
          <h2>관리자 메뉴</h2>
          <ul>
            {isSubAdmin && (
              <>
                <li><Link to="/admin/game">게임 관리</Link></li>
                <li><Link to="/admin/gacha-banner">가챠 배너 관리</Link></li>
                <li><Link to="/admin/announcements">공지/팝업 관리</Link></li>
                <li><Link to="/admin/notice">공지사항 작성</Link></li>
                <li><Link to="/admin/channel">채널 관리</Link></li>
              </>
            )}
            {isMainAdmin && <li><Link to="/admin/users">유저 정보 관리</Link></li>}
          </ul>
        </div>
      )}

      {/* 로그아웃/테마 토글을 헤더에서 빼서 내정보 안으로 이동 */}
      <div className="profile-admin-menu">
        <h2>설정</h2>
        <div className="profile-settings-row">
          <span>다크 모드</span>
          <label className="toggle-switch">
            <input
              type="checkbox"
              checked={theme === 'dark'}
              onChange={toggleTheme}
            />
            <span className="toggle-switch-track" />
          </label>
        </div>
        <button type="button" className="profile-logout" onClick={logout}>로그아웃</button>
      </div>
    </div>
  );
}

export default UserProfilePage;
