import React from 'react';
import { Link } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';

function Sidebar({ isOpen, isMobile }) {
  const { isAuthenticated, role } = useAuth();

  const sidebarStyle = {
    background: 'var(--sidebar-bg)',
    color: 'var(--text-color)',
    transition: 'all 0.3s ease-in-out',
    overflow: 'hidden',
    display: 'flex',
    flexDirection: isMobile ? 'row' : 'column',
    justifyContent: isMobile ? 'space-around' : 'flex-start',
    alignItems: isMobile ? 'center' : 'flex-start',
  };

  if (isMobile) {
    sidebarStyle.width = '100%';
    sidebarStyle.height = 'auto';
    sidebarStyle.padding = '10px 0'; // Smaller padding for mobile top bar
    sidebarStyle.borderBottom = '1px solid var(--border-color)';
    sidebarStyle.borderRight = 'none';
  } else {
    sidebarStyle.width = isOpen ? '200px' : '0';
    sidebarStyle.padding = isOpen ? '20px' : '0';
    sidebarStyle.borderRight = isOpen ? '1px solid var(--border-color)' : 'none';
    sidebarStyle.flexShrink = 0;
  }

  const isSubAdmin = isAuthenticated && (role === 'SUB_ADMIN' || role === 'MAIN_ADMIN');
  const isMainAdmin = isAuthenticated && role === 'MAIN_ADMIN';

  // 모바일 상단 탭바는 항목 수가 늘어나면(관리자 메뉴 추가 등) 폭에 맞춰
  // 텍스트가 글자 단위로 줄바꿈되는 문제가 있어, 줄바꿈 대신 가로 스크롤로 처리한다.
  const ulStyle = {
    listStyle: 'none',
    padding: 0,
    margin: 0,
    display: 'flex',
    flexDirection: isMobile ? 'row' : 'column',
    width: '100%',
    alignItems: isMobile ? 'center' : 'stretch',
    justifyContent: isMobile ? 'flex-start' : 'flex-start',
    overflowX: isMobile ? 'auto' : 'visible',
    flexWrap: 'nowrap',
    gap: isMobile ? '20px' : '0',
    paddingLeft: isMobile ? '16px' : 0,
    paddingRight: isMobile ? '16px' : 0,
  };

  const linkStyle = {
    textDecoration: 'none',
    color: 'var(--text-color)',
    fontWeight: 'bold',
    fontSize: '1.2em',
    whiteSpace: isMobile ? 'nowrap' : 'normal',
  };

  const liStyle = { marginBottom: isMobile ? '0' : '15px', flexShrink: 0 };

  const dividerStyle = isMobile
    ? { borderLeft: '1px solid var(--border-color)', alignSelf: 'stretch', flexShrink: 0 }
    : { borderTop: '1px solid var(--border-color)', margin: '15px 0', width: '80%', alignSelf: 'center', listStyle: 'none' };

  return (
    <aside style={sidebarStyle}>
      {isOpen && (
        <ul style={ulStyle}>
          <li style={liStyle}><Link to="/scheduler" style={linkStyle}>스케줄러</Link></li>
          <li style={liStyle}><Link to="/history" style={linkStyle}>기록</Link></li>
          <li style={liStyle}><Link to="/simulator" style={linkStyle}>시뮬레이터</Link></li>
          <li style={liStyle}><Link to="/board" style={linkStyle}>채널</Link></li>

          {isAuthenticated && (isSubAdmin || isMainAdmin) && (
            <li style={dividerStyle}></li>
          )}

          {isSubAdmin && (
            <>
              <li style={liStyle}><Link to="/admin/game" style={linkStyle}>게임 관리</Link></li>
              <li style={liStyle}><Link to="/admin/gacha-banner" style={linkStyle}>가챠 배너 관리</Link></li>
              <li style={liStyle}><Link to="/admin/announcements" style={linkStyle}>공지/팝업 관리</Link></li>
              <li style={liStyle}><Link to="/admin/notice" style={linkStyle}>공지사항 작성</Link></li>
              <li style={liStyle}><Link to="/admin/channel" style={linkStyle}>채널 관리</Link></li>
            </>
          )}
          {isMainAdmin && (
            <li style={liStyle}><Link to="/admin/users" style={linkStyle}>유저 정보 관리</Link></li>
          )}
        </ul>
      )}
    </aside>
  );
}

export default Sidebar;

