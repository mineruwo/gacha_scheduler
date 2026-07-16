// 공통 API 클라이언트: Authorization 헤더 자동 첨부 + 401 시 자동 로그아웃.
// AuthContext와 동일하게 localStorage의 'token'을 신뢰 원천으로 사용한다.
const API_BASE_URL = import.meta.env.VITE_API_BASE_URL ?? 'http://localhost:8080';

export class ApiError extends Error {
  constructor(status, body) {
    super(`API 오류 (${status}): ${body}`);
    this.status = status;
    this.body = body;
  }
}

function handleUnauthorized() {
  // 토큰 만료/무효 → 저장된 인증 정보를 지우고 로그인 페이지로 이동.
  // 전체 리로드로 AuthContext 상태도 함께 초기화된다.
  localStorage.removeItem('user');
  localStorage.removeItem('token');
  localStorage.removeItem('role');
  if (window.location.pathname !== '/login') {
    window.location.assign('/login');
  }
}

export async function apiFetch(path, options = {}) {
  const token = localStorage.getItem('token');
  const headers = { ...options.headers };
  if (token) {
    headers.Authorization = `Bearer ${token}`;
  }
  if (options.body != null && headers['Content-Type'] == null) {
    headers['Content-Type'] = 'application/json';
  }

  const response = await fetch(`${API_BASE_URL}${path}`, { ...options, headers });

  if (response.status === 401) {
    handleUnauthorized();
    throw new ApiError(401, 'Unauthorized');
  }
  if (!response.ok) {
    throw new ApiError(response.status, await response.text());
  }
  if (response.status === 204) return null;
  return response.json();
}
