import React from 'react';
import { Link } from 'react-router-dom';
import ThemeToggleButton from '../components/ThemeToggleButton';
import { useAuth } from '../context/AuthContext';

function Header({ toggleTheme, theme, toggleSidebar, isMobile }) {
  const { isAuthenticated, user, logout } = useAuth();

  return (
    <header style={{ padding: 'calc(10px + env(safe-area-inset-top, 0px)) calc(20px + env(safe-area-inset-right, 0px)) 10px calc(20px + env(safe-area-inset-left, 0px))', background: 'var(--header-bg)', borderBottom: '1px solid var(--border-color)', display: 'flex', justifyContent: 'space-between', alignItems: 'center', color: 'var(--text-color)' }}>
      <div style={{ display: 'flex', alignItems: 'center' }}>
        {!isMobile && (
          <button onClick={toggleSidebar} style={{ background: 'none', border: 'none', cursor: 'pointer', marginRight: '15px', padding: '5px' }}>
            <svg xmlns="http://www.w3.org/2000/svg" height="24px" viewBox="0 0 24 24" width="24px" fill="var(--text-color)"><path d="M0 0h24v24H0V0z" fill="none"/><path d="M3 18h18v-2H3v2zm0-5h18v-2H3v2zm0-7v2h18V6H3z"/></svg>
          </button>
        )}
        <div style={{ fontSize: '24px', fontWeight: 'bold' }}>
          <Link to="/" style={{ textDecoration: 'none', color: 'inherit', display: 'flex', alignItems: 'center' }}>
            <div style={{ background: 'var(--logo-box-bg)', borderRadius: '8px', padding: '5px 10px', display: 'flex', alignItems: 'center', justifyContent: 'center' }}>
              <img src="/logo.png" alt="Gacha Scheduler Logo" style={{ height: '40px' }} />
            </div>
          </Link>
        </div>
      </div>
      <nav style={{ display: 'flex', alignItems: 'center', gap: '20px' }}>
        <ul style={{ listStyle: 'none', margin: 0, padding: 0, display: 'flex', alignItems: 'center' }}>
          {isAuthenticated ? (
            <>
              <li style={{ marginRight: '10px' }}><Link to="/profile" style={{ textDecoration: 'none', color: 'var(--text-color)', fontWeight: 'bold' }}>{user?.name}</Link></li>
              <li><button onClick={logout} style={{ background: 'none', border: 'none', cursor: 'pointer', color: 'var(--text-color)', textDecoration: 'none' }}>로그아웃</button></li>
            </>
          ) : (
            <li><Link to="/login" style={{ textDecoration: 'none', color: 'var(--text-color)' }}>로그인</Link></li>
          )}
        </ul>
        <ThemeToggleButton toggleTheme={toggleTheme} theme={theme} />
      </nav>
    </header>
  );
}

export default Header;
