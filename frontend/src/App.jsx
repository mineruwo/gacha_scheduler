import React from 'react';
import { BrowserRouter as Router, Routes, Route } from 'react-router-dom';
import MainLayout from './layouts/MainLayout';
import HomePage from './pages/HomePage';
import SchedulerPage from './pages/SchedulerPage';
import SimulatorPage from './pages/SimulatorPage';
import StrategyBoardPage from './pages/StrategyBoardPage';
import HistoryPage from './pages/HistoryPage';
import LoginPage from './pages/LoginPage';
import GameManagementPage from './pages/GameManagementPage';
import NoticeCreationPage from './pages/NoticeCreationPage';
import ChannelManagementPage from './pages/ChannelManagementPage';
import UserManagementPage from './pages/UserManagementPage';
import UserProfilePage from './pages/UserProfilePage';
import useTheme from './hooks/useTheme';
import ProtectedRoute from './components/ProtectedRoute';

function App() {
  const [theme, toggleTheme] = useTheme();

  return (
    <Router>
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
          <Route path="admin/notice" element={<ProtectedRoute><NoticeCreationPage /></ProtectedRoute>} />
          <Route path="admin/channel" element={<ProtectedRoute><ChannelManagementPage /></ProtectedRoute>} />
          <Route path="admin/users" element={<ProtectedRoute><UserManagementPage /></ProtectedRoute>} />
          <Route path="profile" element={<ProtectedRoute><UserProfilePage /></ProtectedRoute>} />
          {/* Add more routes here as needed */}
        </Route>
      </Routes>
    </Router>
  );
}

export default App;