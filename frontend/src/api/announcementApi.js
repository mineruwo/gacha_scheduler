// 파트 06 Phase 2 공지사항(NOTICE)/팝업 배너(POPUP) API.
// 조회는 비로그인 가능(현재 활성 기간인 것만), 등록/수정/삭제는 apiFetch가 토큰을 첨부한다.
import { apiFetch } from './apiClient';

export const announcementApi = {
  // 현재 활성(isActive + 기간 내) 공지/팝업만 조회 (비로그인 가능). type 생략 시 전체
  fetchActive(type) {
    const query = type ? `?type=${type}` : '';
    return apiFetch(`/api/announcements${query}`);
  },
};

// 관리자 전용 (SUB_ADMIN / MAIN_ADMIN) — 활성 기간과 무관하게 전체 관리
export const adminAnnouncementApi = {
  fetchAll(type) {
    const query = type ? `?type=${type}` : '';
    return apiFetch(`/api/admin/announcements${query}`);
  },
  create(announcement) {
    return apiFetch('/api/admin/announcements', { method: 'POST', body: JSON.stringify(announcement) });
  },
  update(id, announcement) {
    return apiFetch(`/api/admin/announcements/${id}`, { method: 'PUT', body: JSON.stringify(announcement) });
  },
  delete(id) {
    return apiFetch(`/api/admin/announcements/${id}`, { method: 'DELETE' });
  },
};
