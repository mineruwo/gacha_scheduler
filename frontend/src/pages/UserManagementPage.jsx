import React, { useEffect, useState } from 'react';
import { adminUserApi } from '../api/userApi';
import './UserManagementPage.css';

const ROLES = [
  { value: 'USER', label: '일반 유저' },
  { value: 'SUB_ADMIN', label: '서브 관리자' },
  { value: 'MAIN_ADMIN', label: '메인 관리자' },
];

function UserManagementPage() {
  const [users, setUsers] = useState([]);
  const [query, setQuery] = useState('');
  const [error, setError] = useState(null);

  const loadUsers = (q) =>
    adminUserApi
      .fetchUsers(q)
      .then(setUsers)
      .catch(() => setError('유저 목록을 불러오지 못했습니다. 관리자 권한을 확인하세요.'));

  useEffect(() => {
    loadUsers();
  }, []);

  const submitSearch = (event) => {
    event.preventDefault();
    setError(null);
    loadUsers(query);
  };

  const changeRole = async (user, role) => {
    if (role === user.role) return;
    setError(null);
    try {
      await adminUserApi.updateRole(user.id, role);
      await loadUsers(query);
    } catch {
      setError('역할 변경에 실패했습니다.');
    }
  };

  const suspend = async (user) => {
    if (!window.confirm(`'${user.name}'(${user.email}) 계정을 정지할까요?`)) return;
    setError(null);
    try {
      await adminUserApi.suspendUser(user.id);
      await loadUsers(query);
    } catch {
      setError('계정 정지에 실패했습니다.');
    }
  };

  return (
    <div className="user-mgmt">
      <h1>유저 정보 관리하기</h1>

      {error && <p className="user-mgmt-error">{error}</p>}

      <form className="user-mgmt-search" onSubmit={submitSearch}>
        <input
          type="text"
          placeholder="이메일 또는 닉네임 검색"
          value={query}
          onChange={(e) => setQuery(e.target.value)}
        />
        <button type="submit">검색</button>
      </form>

      {users.length === 0 ? (
        <p className="user-mgmt-empty">조건에 맞는 유저가 없습니다.</p>
      ) : (
        <table>
          <thead>
            <tr>
              <th>닉네임</th>
              <th>이메일</th>
              <th>가입일</th>
              <th>상태</th>
              <th>역할</th>
              <th></th>
            </tr>
          </thead>
          <tbody>
            {users.map((user) => (
              <tr key={user.id}>
                <td>{user.name}</td>
                <td>{user.email}</td>
                <td>{user.createdAt ? new Date(user.createdAt).toLocaleDateString() : ''}</td>
                <td>{user.isDeleted ? '정지됨' : '활성'}</td>
                <td>
                  <select value={user.role} onChange={(e) => changeRole(user, e.target.value)}>
                    {ROLES.map((r) => (
                      <option key={r.value} value={r.value}>{r.label}</option>
                    ))}
                  </select>
                </td>
                <td className="row-actions">
                  <button type="button" disabled={user.isDeleted} onClick={() => suspend(user)}>정지</button>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      )}
    </div>
  );
}

export default UserManagementPage;
