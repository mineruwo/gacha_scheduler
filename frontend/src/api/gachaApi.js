// docs/SYNC.md "가챠 API 계약"을 따르는 클라이언트.
// 확률/천장 계산은 서버 책임이며 클라이언트는 결과 표시에 집중한다.
// 기본은 실 API. 백엔드 없이 UI만 볼 때는 VITE_USE_MOCK_GACHA=true로 Mock 동작.
import { apiFetch } from './apiClient';
import { mockGachaApi } from './mockGachaApi';

const useMock = import.meta.env.VITE_USE_MOCK_GACHA === 'true';

const realGachaApi = {
  // 진행 중 배너 목록. gameId 미지정 시 전체 조회
  fetchBanners(gameId) {
    const path = gameId != null ? `/api/games/${gameId}/banners` : '/api/banners';
    return apiFetch(path);
  },

  // 배너의 뽑기 대상 캐릭터/가중치 목록
  fetchBannerCharacters(bannerId) {
    return apiFetch(`/api/banners/${bannerId}/characters`);
  },

  // 1회/10연 뽑기. currentPity는 비로그인 유저가 클라이언트에 보관 중인 천장 카운트
  pull(bannerId, { count, currentPity }) {
    return apiFetch(`/api/banners/${bannerId}/pull`, {
      method: 'POST',
      body: JSON.stringify({ count, currentPity }),
    });
  },
};

export const gachaApi = useMock ? mockGachaApi : realGachaApi;
