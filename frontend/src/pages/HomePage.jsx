import React from 'react';
import useUserAgent from '../hooks/useUserAgent';

function HomePage() {
  const userAgent = useUserAgent();

  return (
    <div style={{ background: 'blue' }}>
      <h1>환영합니다!</h1>
      <p>가챠 스케줄러 플랫폼에 오신 것을 환영합니다. 왼쪽 사이드바를 통해 다양한 기능을 이용해 보세요.</p>
      <h2>접속 환경 정보:</h2>
      <ul>
        <li>모바일: {userAgent.isMobile ? '예' : '아니오'}</li>
        <li>데스크톱: {userAgent.isDesktop ? '예' : '아니오'}</li>
        <li>OS: {userAgent.os}</li>
        <li>브라우저: {userAgent.browser}</li>
      </ul>
      <p>이 정보를 활용하여 접속 환경에 따라 기능을 제한하거나 다른 UI를 제공할 수 있습니다.</p>
    </div>
  );
}

export default HomePage;
