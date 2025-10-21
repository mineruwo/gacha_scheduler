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

  return (
    <aside style={sidebarStyle}>
      {isOpen && (
        <ul style={{ listStyle: 'none', padding: 0, display: 'flex', flexDirection: isMobile ? 'row' : 'column', width: '100%', justifyContent: isMobile ? 'space-around' : 'flex-start' }}>
          <li style={{ marginBottom: isMobile ? '0' : '15px' }}><Link to="/scheduler" style={{ textDecoration: 'none', color: 'var(--text-color)', fontWeight: 'bold', fontSize: '1.2em' }}>스케줄러</Link></li>
          <li style={{ marginBottom: isMobile ? '0' : '15px' }}><Link to="/history" style={{ textDecoration: 'none', color: 'var(--text-color)', fontWeight: 'bold', fontSize: '1.2em' }}>기록</Link></li>
          <li style={{ marginBottom: isMobile ? '0' : '15px' }}><Link to="/simulator" style={{ textDecoration: 'none', color: 'var(--text-color)', fontWeight: 'bold', fontSize: '1.2em' }}>시뮬레이터</Link></li>
          <li style={{ marginBottom: isMobile ? '0' : '15px' }}><Link to="/board" style={{ textDecoration: 'none', color: 'var(--text-color)', fontWeight: 'bold', fontSize: '1.2em' }}>채널</Link></li>

          {isAuthenticated && (isSubAdmin || isMainAdmin) && (
            <li style={{ borderTop: '1px solid var(--border-color)', margin: '15px 0', width: '80%', alignSelf: 'center', listStyle: 'none' }}></li>
          )}

          {isSubAdmin && (
            <>
              <li style={{ marginBottom: isMobile ? '0' : '15px' }}><Link to="/admin/game" style={{ textDecoration: 'none', color: 'var(--text-color)', fontWeight: 'bold', fontSize: '1.2em' }}>게임 관리</Link></li>
              <li style={{ marginBottom: isMobile ? '0' : '15px' }}><Link to="/admin/notice" style={{ textDecoration: 'none', color: 'var(--text-color)', fontWeight: 'bold', fontSize: '1.2em' }}>공지사항 작성</Link></li>
              <li style={{ marginBottom: isMobile ? '0' : '15px' }}><Link to="/admin/channel" style={{ textDecoration: 'none', color: 'var(--text-color)', fontWeight: 'bold', fontSize: '1.2em' }}>채널 관리</Link></li>
            </>
          )}
          {isMainAdmin && (
            <li style={{ marginBottom: isMobile ? '0' : '15px' }}><Link to="/admin/users" style={{ textDecoration: 'none', color: 'var(--text-color)', fontWeight: 'bold', fontSize: '1.2em' }}>유저 정보 관리</Link></li>
          )}
        </ul>
      )}
    </aside>
  );
}

export default Sidebar;

