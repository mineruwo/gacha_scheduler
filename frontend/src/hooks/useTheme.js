import { useState, useEffect } from 'react';

function useTheme() {
  const [theme, setTheme] = useState(() => {
    const savedTheme = localStorage.getItem('color-theme');
    if (savedTheme) {
      return savedTheme;
    } else if (window.matchMedia && window.matchMedia('(prefers-color-scheme: dark)').matches) {
      return 'dark';
    } else {
      return 'light';
    }
  });

  useEffect(() => {
    document.documentElement.setAttribute('color-theme', theme);
    localStorage.setItem('color-theme', theme);

    // Capacitor 앱 셸에서 상태바 아이콘 색을 테마에 맞춘다 (일반 브라우저에선 no-op).
    // Style.Dark = 어두운 배경용(밝은 아이콘), Style.Light = 밝은 배경용(어두운 아이콘)
    const statusBar = window.Capacitor?.Plugins?.StatusBar;
    if (statusBar) {
      statusBar.setStyle({ style: theme === 'dark' ? 'DARK' : 'LIGHT' }).catch(() => {});
    }
  }, [theme]);

  const toggleTheme = () => {
    setTheme((prevTheme) => (prevTheme === 'light' ? 'dark' : 'light'));
  };

  return [theme, toggleTheme];
}

export default useTheme;
