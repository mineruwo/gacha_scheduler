import React, { useState, useEffect } from 'react';
import { Outlet } from 'react-router-dom';
import Header from './Header';
import Sidebar from './Sidebar';
import MobileBottomNav from './MobileBottomNav';
import PopupBannerModal from '../components/PopupBannerModal';
import useMediaQuery from '../hooks/useMediaQuery';

function MainLayout({ theme, toggleTheme }) {
  const isMobile = useMediaQuery('(max-width: 768px)');
  const [isSidebarOpen, setIsSidebarOpen] = useState(() => {
    const storedState = localStorage.getItem('isSidebarOpen');
    return storedState ? JSON.parse(storedState) : true; // Default to open if not found
  });

  const toggleSidebar = () => {
    if (!isMobile) {
      setIsSidebarOpen((prevState) => {
        const newState = !prevState;
        localStorage.setItem('isSidebarOpen', JSON.stringify(newState));
        return newState;
      });
    }
  };

  useEffect(() => {
    if (isMobile) {
      setIsSidebarOpen(true); // Always open on mobile
    } else {
      // On desktop, load from localStorage or default to open
      const storedState = localStorage.getItem('isSidebarOpen');
      setIsSidebarOpen(storedState ? JSON.parse(storedState) : true);
    }
  }, [isMobile]);

  return (
    <div style={{ display: 'flex', flexDirection: 'column', minHeight: '100vh', background: 'var(--background-color)' }}>
      <Header toggleSidebar={toggleSidebar} isMobile={isMobile} />
      <div style={{ display: 'flex', flex: 1, background: 'var(--background-color)', flexDirection: isMobile ? 'column' : 'row' }}>
        {/* 모바일 폭에서는 하단 고정 탭바(MobileBottomNav)가 내비게이션을 담당하므로 Sidebar는 데스크톱 전용 */}
        {!isMobile && <Sidebar isOpen={isSidebarOpen} isMobile={isMobile} />}
        <main
          style={{
            flex: '1 1 0%',
            width: isMobile ? '100%' : 'auto',
            minWidth: isMobile ? 'auto' : '300px',
            padding: 'var(--main-padding)',
            // 하단 고정 탭바에 콘텐츠가 가려지지 않도록 모바일에서만 여유 공간 확보
            paddingBottom: isMobile
              ? 'calc(64px + var(--main-padding) + env(safe-area-inset-bottom, 0px))'
              : 'calc(var(--main-padding) + env(safe-area-inset-bottom, 0px))',
            paddingLeft: 'calc(var(--main-padding) + env(safe-area-inset-left, 0px))',
            paddingRight: 'calc(var(--main-padding) + env(safe-area-inset-right, 0px))',
            background: 'var(--background-color)',
            color: 'var(--text-color)',
            overflowX: 'auto',
            boxSizing: 'border-box',
          }}
        >
          {/* 로그아웃/테마 토글은 헤더에서 빠지고 내정보(UserProfilePage) 안으로 이동했으므로, 거기서 쓸 수 있도록 전달 */}
          <Outlet context={{ theme, toggleTheme }} />
        </main>
      </div>
      {isMobile && <MobileBottomNav />}
      <PopupBannerModal />
    </div>
  );
}

export default MainLayout;
