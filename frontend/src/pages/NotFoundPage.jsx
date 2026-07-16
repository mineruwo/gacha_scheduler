import React from 'react';
import { Link } from 'react-router-dom';

function NotFoundPage() {
  return (
    <div style={{ display: 'flex', flexDirection: 'column', gap: '12px', alignItems: 'flex-start' }}>
      <h1>페이지를 찾을 수 없습니다</h1>
      <p>주소가 잘못되었거나 삭제된 페이지입니다.</p>
      <Link to="/">홈으로 돌아가기</Link>
    </div>
  );
}

export default NotFoundPage;
