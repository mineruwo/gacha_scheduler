// 파트 03 스케줄러 API (docs/plans/03-scheduler.md 계약).
// 조회(games/schedules)는 비로그인 가능, 개인화 필터/관리자 CRUD는 apiFetch가 토큰을 첨부한다.
import { apiFetch } from './apiClient';

export const schedulerApi = {
  // 전체 게임 목록 (비로그인 가능)
  fetchGames() {
    return apiFetch('/api/games');
  },

  // 기간/게임 필터 일정 조회 (비로그인 가능). from/to 생략 시 서버 기본값(-1개월 ~ +3개월)
  fetchSchedules({ gameCodes, from, to } = {}) {
    const params = new URLSearchParams();
    if (gameCodes?.length) params.set('gameCodes', gameCodes.join(','));
    if (from) params.set('from', from);
    if (to) params.set('to', to);
    const query = params.toString();
    return apiFetch(`/api/schedules${query ? `?${query}` : ''}`);
  },

  // 로그인 유저의 관심 게임 필터 (로그인 필요)
  fetchMyGamePreferences() {
    return apiFetch('/api/users/me/game-preferences');
  },

  updateMyGamePreferences(gameCodes) {
    return apiFetch('/api/users/me/game-preferences', {
      method: 'PUT',
      body: JSON.stringify({ gameCodes }),
    });
  },
};

// 관리자 전용 (SUB_ADMIN / MAIN_ADMIN)
export const adminApi = {
  fetchGames() {
    return apiFetch('/api/admin/games');
  },
  createGame(game) {
    return apiFetch('/api/admin/games', { method: 'POST', body: JSON.stringify(game) });
  },
  updateGame(id, game) {
    return apiFetch(`/api/admin/games/${id}`, { method: 'PUT', body: JSON.stringify(game) });
  },
  deleteGame(id) {
    return apiFetch(`/api/admin/games/${id}`, { method: 'DELETE' });
  },
  createSchedule(schedule) {
    return apiFetch('/api/admin/schedules', { method: 'POST', body: JSON.stringify(schedule) });
  },
  updateSchedule(id, schedule) {
    return apiFetch(`/api/admin/schedules/${id}`, { method: 'PUT', body: JSON.stringify(schedule) });
  },
  deleteSchedule(id) {
    return apiFetch(`/api/admin/schedules/${id}`, { method: 'DELETE' });
  },
};
