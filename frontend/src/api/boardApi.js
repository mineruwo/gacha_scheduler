// 파트 05 공략 게시판 API (docs/plans/05-strategy-board.md 계약).
// 채널/게시글/댓글 조회는 비로그인 가능, 작성/수정/삭제는 apiFetch가 토큰을 첨부한다.
import { apiFetch } from './apiClient';

export const boardApi = {
  // 채널 목록 (gameId 생략 시 전체, 비로그인 가능)
  fetchChannels(gameId) {
    const query = gameId != null ? `?gameId=${gameId}` : '';
    return apiFetch(`/api/channels${query}`);
  },

  // 채널별 게시글 목록, Page<PostResponseDto> 그대로 반환 (비로그인 가능)
  fetchPosts(channelId, { page = 0, size = 20 } = {}) {
    return apiFetch(`/api/channels/${channelId}/posts?page=${page}&size=${size}`);
  },

  // 글쓰기 (로그인 필요)
  createPost(channelId, { title, content, templateType }) {
    return apiFetch(`/api/channels/${channelId}/posts`, {
      method: 'POST',
      body: JSON.stringify({ title, content, templateType }),
    });
  },

  // 게시글 상세 (비로그인 가능, 조회 시 viewCount +1)
  fetchPost(postId) {
    return apiFetch(`/api/posts/${postId}`);
  },

  updatePost(postId, { title, content, templateType }) {
    return apiFetch(`/api/posts/${postId}`, {
      method: 'PUT',
      body: JSON.stringify({ title, content, templateType }),
    });
  },

  deletePost(postId) {
    return apiFetch(`/api/posts/${postId}`, { method: 'DELETE' });
  },

  // 댓글 (조회는 비로그인 가능, 작성은 로그인 필요)
  fetchComments(postId) {
    return apiFetch(`/api/posts/${postId}/comments`);
  },

  createComment(postId, { content, parentCommentId }) {
    return apiFetch(`/api/posts/${postId}/comments`, {
      method: 'POST',
      body: JSON.stringify({ content, parentCommentId: parentCommentId ?? null }),
    });
  },

  deleteComment(commentId) {
    return apiFetch(`/api/comments/${commentId}`, { method: 'DELETE' });
  },
};

// 관리자 전용 (SUB_ADMIN / MAIN_ADMIN)
export const adminBoardApi = {
  createChannel(channel) {
    return apiFetch('/api/admin/channels', { method: 'POST', body: JSON.stringify(channel) });
  },
  updateChannel(id, channel) {
    return apiFetch(`/api/admin/channels/${id}`, { method: 'PUT', body: JSON.stringify(channel) });
  },
  deleteChannel(id) {
    return apiFetch(`/api/admin/channels/${id}`, { method: 'DELETE' });
  },
};
