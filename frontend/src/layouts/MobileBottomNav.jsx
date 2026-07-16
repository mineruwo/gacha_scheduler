import React from 'react';
import { NavLink } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import './MobileBottomNav.css';

// 기존 Flutter 앱(frontend_mobile)의 BottomNavigationBar와 동일한 Material 아이콘 세트를 사용해
// 모바일 폭(앱 셸 포함)에서 하단 고정 탭바를 제공한다. PC 웹은 기존 Header/Sidebar 그대로 사용.
const ICONS = {
  scheduler:
    'M19 3h-1V1h-2v2H8V1H6v2H5c-1.11 0-1.99.9-1.99 2L3 19c0 1.1.89 2 2 2h14c1.1 0 2-.9 2-2V5c0-1.1-.9-2-2-2zm0 16H5V8h14v11zM7 10h5v5H7z',
  simulator:
    'M17 4h-2.18C14.4 2.84 13.3 2 12 2s-2.4.84-2.82 2H7c-1.1 0-2 .9-2 2v13c0 1.1.9 2 2 2h10c1.1 0 2-.9 2-2V6c0-1.1-.9-2-2-2zm-5 0c.55 0 1 .45 1 1s-.45 1-1 1-1-.45-1-1 .45-1 1-1zM7.5 18c-.83 0-1.5-.67-1.5-1.5S6.67 15 7.5 15s1.5.67 1.5 1.5S8.33 18 7.5 18zm0-5C6.67 13 6 12.33 6 11.5S6.67 10 7.5 10s1.5.67 1.5 1.5S8.33 13 7.5 13zM12 15.5c-.83 0-1.5-.67-1.5-1.5s.67-1.5 1.5-1.5 1.5.67 1.5 1.5-.67 1.5-1.5 1.5zm4.5 2.5c-.83 0-1.5-.67-1.5-1.5s.67-1.5 1.5-1.5 1.5.67 1.5 1.5-.67 1.5-1.5 1.5zm0-5c-.83 0-1.5-.67-1.5-1.5S15.67 10 16.5 10s1.5.67 1.5 1.5-.67 1.5-1.5 1.5z',
  board:
    'M19 3H5c-1.1 0-2 .9-2 2v14c0 1.1.9 2 2 2h14c1.1 0 2-.9 2-2V5c0-1.1-.9-2-2-2zM8 19H5v-2h3v2zm0-4H5v-2h3v2zm0-4H5V9h3v2zm8 8h-6v-2h6v2zm0-4h-6v-2h6v2zm0-4h-6V9h6v2z',
  home: 'M10 20v-6h4v6h5v-8h3L12 3 2 12h3v8z',
  person:
    'M12 12c2.21 0 4-1.79 4-4s-1.79-4-4-4-4 1.79-4 4 1.79 4 4 4zm0 2c-2.67 0-8 1.34-8 4v2h16v-2c0-2.66-5.33-4-8-4z',
};

function Icon({ path }) {
  return (
    <svg xmlns="http://www.w3.org/2000/svg" height="22px" viewBox="0 0 24 24" width="22px" fill="currentColor">
      <path d={path} />
    </svg>
  );
}

function MobileBottomNav() {
  const { isAuthenticated } = useAuth();

  // Flutter 원본(frontend_mobile/lib/screens/home_screen.dart)과 동일한 탭 순서: 일정/가챠/홈(중앙)/게시판/내정보
  const items = [
    { to: '/scheduler', label: '스케줄러', icon: ICONS.scheduler },
    { to: '/simulator', label: '시뮬레이터', icon: ICONS.simulator },
    { to: '/', label: '홈', icon: ICONS.home, end: true },
    { to: '/board', label: '채널', icon: ICONS.board },
    { to: isAuthenticated ? '/profile' : '/login', label: '내 정보', icon: ICONS.person },
  ];

  return (
    <nav className="mobile-bottom-nav">
      {items.map((item) => (
        <NavLink
          key={item.label}
          to={item.to}
          end={item.end}
          className={({ isActive }) => `mobile-bottom-nav-item${isActive ? ' active' : ''}`}
        >
          <Icon path={item.icon} />
          <span>{item.label}</span>
        </NavLink>
      ))}
    </nav>
  );
}

export default MobileBottomNav;
