import React, { useEffect } from 'react';
import { BrowserRouter as Router, Routes, Route, useNavigate } from 'react-router-dom';
import { useAuth } from './context/AuthContext';
import MainLayout from './layouts/MainLayout';
import HomePage from './pages/HomePage';
import SchedulerPage from './pages/SchedulerPage';
import SimulatorPage from './pages/SimulatorPage';
import StrategyBoardPage from './pages/StrategyBoardPage';
import HistoryPage from './pages/HistoryPage';
import LoginPage from './pages/LoginPage';
import GameManagementPage from './pages/GameManagementPage';
import GachaBannerManagementPage from './pages/GachaBannerManagementPage';
import AnnouncementManagementPage from './pages/AnnouncementManagementPage';
import NoticeCreationPage from './pages/NoticeCreationPage';
import ChannelManagementPage from './pages/ChannelManagementPage';
import UserManagementPage from './pages/UserManagementPage';
import UserProfilePage from './pages/UserProfilePage';
import NotFoundPage from './pages/NotFoundPage';
import useTheme from './hooks/useTheme';
import ProtectedRoute from './components/ProtectedRoute';

// 외부 브라우저(Custom Tabs)에 위임한 Google 로그인이 끝나면 gachascheduler://auth-callback
// 딥링크로 앱에 돌아온다 — 여기서 토큰/유저 정보를 받아 로그인 처리하고 브라우저 탭을 닫는다
function NativeAuthBridge() {
  const navigate = useNavigate();
  const { login } = useAuth();

  useEffect(() => {
    const appPlugin = window.Capacitor?.Plugins?.App;
    if (!appPlugin) return undefined;

    let cancelled = false;
    let handle;

    // 네이티브 플러그인 프록시가 항상 Promise를 반환한다고 보장되지 않아 Promise.resolve로 감싼다
    Promise.resolve(
      appPlugin.addListener('appUrlOpen', ({ url }) => {
        try {
          const parsed = new URL(url);
          if (parsed.protocol === 'gachascheduler:' && parsed.hostname === 'auth-callback') {
            const token = parsed.searchParams.get('token');
            const userJson = parsed.searchParams.get('user');
            if (token && userJson) {
              login(JSON.parse(userJson), token);
              navigate('/');
            }
          }
        } catch (error) {
          console.error('Failed to parse auth callback URL:', error);
        }
        Promise.resolve(window.Capacitor?.Plugins?.Browser?.close()).catch(() => {});
      })
    ).then((result) => {
      handle = result;
      if (cancelled) handle?.remove?.();
    });

    return () => {
      cancelled = true;
      handle?.remove?.();
    };
  }, [navigate, login]);

  return null;
}

function App() {
  const [theme, toggleTheme] = useTheme();

  return (
    <Router>
      <NativeAuthBridge />
      <Routes>
        <Route path="/" element={<MainLayout theme={theme} toggleTheme={toggleTheme} />}>
          <Route index element={<HomePage />} />
          {/* 스케줄러는 비로그인 열람 허용 (개인화 필터만 로그인 필요) — docs/plans/03-scheduler.md DoD */}
          <Route path="scheduler" element={<SchedulerPage />} />
          <Route path="simulator" element={<SimulatorPage />} />
          <Route path="board" element={<StrategyBoardPage />} />
          <Route path="history" element={<ProtectedRoute><HistoryPage /></ProtectedRoute>} />
          <Route path="login" element={<LoginPage />} />
          <Route path="admin/game" element={<ProtectedRoute><GameManagementPage /></ProtectedRoute>} />
          <Route path="admin/gacha-banner" element={<ProtectedRoute><GachaBannerManagementPage /></ProtectedRoute>} />
          <Route path="admin/announcements" element={<ProtectedRoute><AnnouncementManagementPage /></ProtectedRoute>} />
          <Route path="admin/notice" element={<ProtectedRoute><NoticeCreationPage /></ProtectedRoute>} />
          <Route path="admin/channel" element={<ProtectedRoute><ChannelManagementPage /></ProtectedRoute>} />
          <Route path="admin/users" element={<ProtectedRoute><UserManagementPage /></ProtectedRoute>} />
          <Route path="profile" element={<ProtectedRoute><UserProfilePage /></ProtectedRoute>} />
          {/* Add more routes here as needed */}
          <Route path="*" element={<NotFoundPage />} />
        </Route>
      </Routes>
    </Router>
  );
}

export default App;