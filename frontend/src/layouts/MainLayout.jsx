import React, { useState, useEffect } from 'react';
import { Outlet } from 'react-router-dom';
import Header from './Header';
import Sidebar from './Sidebar';
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
      <Header toggleTheme={toggleTheme} theme={theme} toggleSidebar={toggleSidebar} isMobile={isMobile} />
      <div style={{ display: 'flex', flex: 1, background: 'var(--background-color)', flexDirection: isMobile ? 'column' : 'row' }}>
        <Sidebar isOpen={isSidebarOpen} isMobile={isMobile} />
        <main style={{ flex: '1 1 0%', width: isMobile ? '100%' : 'auto', minWidth: isMobile ? 'auto' : '300px', padding: 'var(--main-padding)', background: 'var(--background-color)', color: 'var(--text-color)', overflowX: 'auto', boxSizing: 'border-box' }}>
          <Outlet />
        </main>
      </div>
    </div>
  );
}

export default MainLayout;
