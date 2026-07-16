// 파트 06 관리자/유저 관리 API (docs/plans/06-admin-user-management.md 계약). 전부 로그인 필요.
import { apiFetch } from './apiClient';

export const userApi = {
  fetchMyProfile() {
    return apiFetch('/api/users/me');
  },

  updateMyProfile({ name, profilePictureUrl }) {
    return apiFetch('/api/users/me', {
      method: 'PUT',
      body: JSON.stringify({ name, profilePictureUrl }),
    });
  },

  fetchMyHistory() {
    return apiFetch('/api/users/me/history');
  },
};

// 관리자 전용 (SUB_ADMIN / MAIN_ADMIN, 정지는 MAIN_ADMIN까지만 접근하는 화면에서 사용)
export const adminUserApi = {
  fetchUsers(query) {
    const q = query?.trim() ? `?query=${encodeURIComponent(query.trim())}` : '';
    return apiFetch(`/api/admin/users${q}`);
  },

  updateRole(id, role) {
    return apiFetch(`/api/admin/users/${id}/role`, {
      method: 'PUT',
      body: JSON.stringify({ role }),
    });
  },

  suspendUser(id) {
    return apiFetch(`/api/admin/users/${id}`, { method: 'DELETE' });
  },
};
