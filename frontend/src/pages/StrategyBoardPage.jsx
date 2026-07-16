import React, { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { boardApi } from '../api/boardApi';
import './StrategyBoardPage.css';

const TEMPLATE_LABELS = { GUIDE: '공략', QUESTION: '질문', FREE: '자유' };

function canModify(user, role, authorId) {
  return user != null && (user.id === authorId || role === 'SUB_ADMIN' || role === 'MAIN_ADMIN');
}

function formatDateTime(iso) {
  if (!iso) return '';
  const d = new Date(iso);
  const pad = (n) => String(n).padStart(2, '0');
  return `${d.getFullYear()}.${pad(d.getMonth() + 1)}.${pad(d.getDate())} ${pad(d.getHours())}:${pad(d.getMinutes())}`;
}

function StrategyBoardPage() {
  const { isAuthenticated, user, role } = useAuth();

  const [channels, setChannels] = useState(null);
  const [selectedChannelId, setSelectedChannelId] = useState(null);
  const [postsPage, setPostsPage] = useState(null);
  const [pageNumber, setPageNumber] = useState(0);
  const [error, setError] = useState(null);

  const [selectedPost, setSelectedPost] = useState(null);
  const [comments, setComments] = useState(null);
  const [newComment, setNewComment] = useState('');
  const [replyTargetId, setReplyTargetId] = useState(null);

  useEffect(() => {
    boardApi
      .fetchChannels()
      .then((data) => {
        setChannels(data);
        if (data.length > 0) setSelectedChannelId(data[0].id);
      })
      .catch(() => setError('채널 목록을 불러오지 못했습니다.'));
  }, []);

  useEffect(() => {
    if (selectedChannelId == null) return;
    setPostsPage(null);
    setSelectedPost(null);
    boardApi
      .fetchPosts(selectedChannelId, { page: pageNumber })
      .then(setPostsPage)
      .catch(() => setError('게시글 목록을 불러오지 못했습니다.'));
  }, [selectedChannelId, pageNumber]);

  const openPost = (postId) => {
    setError(null);
    boardApi
      .fetchPost(postId)
      .then(setSelectedPost)
      .catch(() => setError('게시글을 불러오지 못했습니다.'));
    setComments(null);
    boardApi
      .fetchComments(postId)
      .then(setComments)
      .catch(() => setError('댓글을 불러오지 못했습니다.'));
  };

  const closePost = () => {
    setSelectedPost(null);
    setComments(null);
    setReplyTargetId(null);
    setNewComment('');
  };

  const submitComment = async (event) => {
    event.preventDefault();
    if (!newComment.trim()) return;
    setError(null);
    try {
      await boardApi.createComment(selectedPost.id, {
        content: newComment.trim(),
        parentCommentId: replyTargetId,
      });
      setNewComment('');
      setReplyTargetId(null);
      const refreshed = await boardApi.fetchComments(selectedPost.id);
      setComments(refreshed);
    } catch {
      setError('댓글 작성에 실패했습니다.');
    }
  };

  const removeComment = async (commentId) => {
    if (!window.confirm('댓글을 삭제할까요?')) return;
    setError(null);
    try {
      await boardApi.deleteComment(commentId);
      const refreshed = await boardApi.fetchComments(selectedPost.id);
      setComments(refreshed);
    } catch {
      setError('댓글 삭제에 실패했습니다. 본인 댓글만 삭제할 수 있습니다.');
    }
  };

  const removePost = async () => {
    if (!window.confirm('게시글을 삭제할까요?')) return;
    setError(null);
    try {
      await boardApi.deletePost(selectedPost.id);
      closePost();
      const refreshed = await boardApi.fetchPosts(selectedChannelId, { page: pageNumber });
      setPostsPage(refreshed);
    } catch {
      setError('게시글 삭제에 실패했습니다. 본인 글만 삭제할 수 있습니다.');
    }
  };

  const topLevelComments = comments?.filter((c) => c.parentCommentId == null) ?? [];
  const repliesOf = (parentId) => comments?.filter((c) => c.parentCommentId === parentId) ?? [];

  return (
    <div className="board-page">
      <h1>공략 게시판</h1>

      {error && <p className="board-error">{error}</p>}

      {channels != null && channels.length > 0 && (
        <div className="board-channel-tabs">
          {channels.map((ch) => (
            <button
              key={ch.id}
              type="button"
              className={ch.id === selectedChannelId ? 'active' : ''}
              onClick={() => {
                setSelectedChannelId(ch.id);
                setPageNumber(0);
              }}
            >
              {ch.gameName ? `${ch.gameName} · ` : ''}{ch.name}
            </button>
          ))}
        </div>
      )}

      {channels != null && channels.length === 0 && <p className="board-empty">등록된 채널이 없습니다.</p>}

      {selectedChannelId != null && (selectedPost == null ? (
        <>
          {isAuthenticated && (
            <div className="board-write-link">
              <Link to="/admin/notice">글쓰기</Link>
            </div>
          )}

          {postsPage == null ? (
            <p className="board-empty">게시글을 불러오는 중...</p>
          ) : postsPage.content.length === 0 ? (
            <p className="board-empty">게시글이 없습니다.</p>
          ) : (
            <>
              <table className="board-post-list">
                <thead>
                  <tr>
                    <th>템플릿</th>
                    <th>제목</th>
                    <th>작성자</th>
                    <th>조회수</th>
                    <th>작성일</th>
                  </tr>
                </thead>
                <tbody>
                  {postsPage.content.map((post) => (
                    <tr key={post.id} onClick={() => openPost(post.id)} className="clickable-row">
                      <td>{TEMPLATE_LABELS[post.templateType] ?? post.templateType}</td>
                      <td>{post.title}</td>
                      <td>{post.authorName}</td>
                      <td>{post.viewCount}</td>
                      <td>{formatDateTime(post.createdAt)}</td>
                    </tr>
                  ))}
                </tbody>
              </table>

              <div className="board-pagination">
                <button type="button" disabled={pageNumber === 0} onClick={() => setPageNumber((p) => p - 1)}>
                  ◀ 이전
                </button>
                <span>{postsPage.number + 1} / {Math.max(postsPage.totalPages, 1)}</span>
                <button
                  type="button"
                  disabled={pageNumber + 1 >= postsPage.totalPages}
                  onClick={() => setPageNumber((p) => p + 1)}
                >
                  다음 ▶
                </button>
              </div>
            </>
          )}
        </>
      ) : (
        <div className="board-detail">
          <button type="button" className="board-back" onClick={closePost}>← 목록으로</button>

          <h2>{selectedPost.title}</h2>
          <div className="board-detail-meta">
            <span>{TEMPLATE_LABELS[selectedPost.templateType] ?? selectedPost.templateType}</span>
            <span>{selectedPost.authorName}</span>
            <span>{formatDateTime(selectedPost.createdAt)}</span>
            <span>조회 {selectedPost.viewCount}</span>
          </div>
          <p className="board-detail-content">{selectedPost.content}</p>

          {canModify(user, role, selectedPost.authorId) && (
            <div className="board-detail-actions">
              <button type="button" onClick={removePost}>삭제</button>
            </div>
          )}

          <section className="board-comments">
            <h3>댓글 {comments?.length ?? 0}</h3>

            {comments == null ? (
              <p className="board-empty">댓글을 불러오는 중...</p>
            ) : topLevelComments.length === 0 ? (
              <p className="board-empty">첫 댓글을 남겨보세요.</p>
            ) : (
              <ul className="board-comment-list">
                {topLevelComments.map((comment) => (
                  <li key={comment.id}>
                    <div className="board-comment">
                      <span className="board-comment-author">{comment.authorName}</span>
                      <span className="board-comment-content">{comment.content}</span>
                      <span className="board-comment-date">{formatDateTime(comment.createdAt)}</span>
                      {isAuthenticated && (
                        <button type="button" onClick={() => setReplyTargetId(comment.id)}>답글</button>
                      )}
                      {canModify(user, role, comment.authorId) && (
                        <button type="button" onClick={() => removeComment(comment.id)}>삭제</button>
                      )}
                    </div>
                    {repliesOf(comment.id).length > 0 && (
                      <ul className="board-reply-list">
                        {repliesOf(comment.id).map((reply) => (
                          <li key={reply.id} className="board-comment">
                            <span className="board-comment-author">{reply.authorName}</span>
                            <span className="board-comment-content">{reply.content}</span>
                            <span className="board-comment-date">{formatDateTime(reply.createdAt)}</span>
                            {canModify(user, role, reply.authorId) && (
                              <button type="button" onClick={() => removeComment(reply.id)}>삭제</button>
                            )}
                          </li>
                        ))}
                      </ul>
                    )}
                  </li>
                ))}
              </ul>
            )}

            {isAuthenticated ? (
              <form className="board-comment-form" onSubmit={submitComment}>
                {replyTargetId != null && (
                  <div className="board-reply-hint">
                    답글 작성 중
                    <button type="button" onClick={() => setReplyTargetId(null)}>취소</button>
                  </div>
                )}
                <textarea
                  value={newComment}
                  onChange={(e) => setNewComment(e.target.value)}
                  placeholder="댓글을 입력하세요"
                  required
                />
                <button type="submit">등록</button>
              </form>
            ) : (
              <p className="board-empty"><Link to="/login">로그인</Link> 후 댓글을 작성할 수 있습니다.</p>
            )}
          </section>
        </div>
      ))}
    </div>
  );
}

export default StrategyBoardPage;
