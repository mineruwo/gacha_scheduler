import React, { useEffect, useState } from 'react';
import { userApi } from '../api/userApi';
import './HistoryPage.css';

function formatDateTime(iso) {
  if (!iso) return '';
  const d = new Date(iso);
  const pad = (n) => String(n).padStart(2, '0');
  return `${d.getFullYear()}.${pad(d.getMonth() + 1)}.${pad(d.getDate())} ${pad(d.getHours())}:${pad(d.getMinutes())}`;
}

function HistoryPage() {
  const [history, setHistory] = useState(null);
  const [tab, setTab] = useState('posts');
  const [error, setError] = useState(null);

  useEffect(() => {
    userApi
      .fetchMyHistory()
      .then(setHistory)
      .catch(() => setError('활동 기록을 불러오지 못했습니다.'));
  }, []);

  return (
    <div className="history-page">
      <h1>내 활동 기록</h1>
      <p className="history-hint">
        가챠 뽑기 기록은 서버에 저장되지 않아 여기에 표시되지 않습니다. 작성한 글/댓글만 조회할 수 있습니다.
      </p>

      {error && <p className="history-error">{error}</p>}

      <div className="history-tabs">
        <button type="button" className={tab === 'posts' ? 'active' : ''} onClick={() => setTab('posts')}>
          작성한 글 {history ? `(${history.posts.length})` : ''}
        </button>
        <button type="button" className={tab === 'comments' ? 'active' : ''} onClick={() => setTab('comments')}>
          작성한 댓글 {history ? `(${history.comments.length})` : ''}
        </button>
      </div>

      {history == null ? (
        <p className="history-empty">불러오는 중...</p>
      ) : tab === 'posts' ? (
        history.posts.length === 0 ? (
          <p className="history-empty">작성한 글이 없습니다.</p>
        ) : (
          <ul className="history-list">
            {history.posts.map((post) => (
              <li key={post.id}>
                <span className="history-item-channel">{post.channelName}</span>
                <span className="history-item-title">{post.title}</span>
                <span className="history-item-date">{formatDateTime(post.createdAt)}</span>
              </li>
            ))}
          </ul>
        )
      ) : history.comments.length === 0 ? (
        <p className="history-empty">작성한 댓글이 없습니다.</p>
      ) : (
        <ul className="history-list">
          {history.comments.map((comment) => (
            <li key={comment.id}>
              <span className="history-item-title">{comment.content}</span>
              <span className="history-item-date">{formatDateTime(comment.createdAt)}</span>
            </li>
          ))}
        </ul>
      )}
    </div>
  );
}

export default HistoryPage;
