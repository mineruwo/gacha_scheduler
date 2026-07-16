import React, { useState } from 'react';
import { GoogleOAuthProvider, GoogleLogin } from '@react-oauth/google';
import { useNavigate, useSearchParams } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import './LoginPage.css';

// Google이 임베디드 WebView(Capacitor 앱 셸)의 User-Agent를 서버 단에서 차단해
// gsi/client 스크립트 자체를 403으로 거부한다(2021년부터 시행 중인 Google 정책) — 앱 셸
// 안에서는 로그인 버튼을 렌더링하는 대신, 외부 시스템 브라우저(Custom Tabs)로 로그인을 위임하고
// 완료되면 커스텀 URL 스킴(gachascheduler://auth-callback)으로 결과를 앱에 돌려받는다
const isNativeShell = !!window.Capacitor?.isNativePlatform?.();

function LoginPage() {
  const navigate = useNavigate();
  const { login } = useAuth();
  const [searchParams] = useSearchParams();
  const isBridgeTab = searchParams.get('bridge') === '1';

  const [mode, setMode] = useState('login'); // 'login' | 'signup'
  const [form, setForm] = useState({ email: '', password: '', name: '' });
  const [error, setError] = useState(null);
  const [submitting, setSubmitting] = useState(false);

  // 로그인 성공 후 처리: 브릿지 탭(외부 브라우저)이면 딥링크로 앱에 결과를 돌려주고,
  // 아니면 그냥 이 화면에서 로그인 상태로 전환한다
  const completeLogin = (userData) => {
    if (isBridgeTab) {
      const params = new URLSearchParams({ token: userData.token, user: JSON.stringify(userData) });
      window.location.href = `gachascheduler://auth-callback?${params.toString()}`;
      return;
    }
    login(userData, userData.token);
    navigate('/');
  };

  const handleGoogleSuccess = async (credentialResponse) => {
    try {
      const response = await fetch(`${import.meta.env.VITE_API_BASE_URL}/api/auth/google`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ idToken: credentialResponse.credential }),
      });
      if (response.ok) {
        completeLogin(await response.json());
      } else {
        console.error('Backend authentication failed:', response.statusText);
      }
    } catch (error) {
      console.error('Error sending ID token to backend:', error);
    }
  };

  const handleGoogleError = () => {
    console.error('Google login failed');
  };

  const openLoginInSystemBrowser = () => {
    const bridgeBaseUrl = import.meta.env.VITE_OAUTH_BRIDGE_URL || 'http://localhost:5173';
    window.Capacitor?.Plugins?.Browser?.open({ url: `${bridgeBaseUrl}/login?bridge=1` });
  };

  const submitPasswordForm = async (event) => {
    event.preventDefault();
    setError(null);
    setSubmitting(true);
    try {
      const endpoint = mode === 'login' ? '/api/auth/login' : '/api/auth/signup';
      const body = mode === 'login'
        ? { email: form.email, password: form.password }
        : { email: form.email, password: form.password, name: form.name };

      const response = await fetch(`${import.meta.env.VITE_API_BASE_URL}${endpoint}`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(body),
      });

      if (response.ok) {
        completeLogin(await response.json());
        return;
      }

      if (mode === 'login') {
        setError('이메일 또는 비밀번호가 올바르지 않습니다.');
      } else if (response.status === 409) {
        setError('이미 가입된 이메일입니다.');
      } else {
        setError('회원가입에 실패했습니다. 입력값을 확인해주세요(비밀번호 8자 이상).');
      }
    } catch {
      setError('서버와 통신 중 오류가 발생했습니다.');
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <div className="login-page">
      <div className="login-card">
        <h1>로그인</h1>

        <div className="login-tabs">
          <button
            type="button"
            className={mode === 'login' ? 'active' : ''}
            onClick={() => { setMode('login'); setError(null); }}
          >
            로그인
          </button>
          <button
            type="button"
            className={mode === 'signup' ? 'active' : ''}
            onClick={() => { setMode('signup'); setError(null); }}
          >
            회원가입
          </button>
        </div>

        <form className="login-form" onSubmit={submitPasswordForm}>
          <label>
            이메일
            <input
              type="email"
              required
              value={form.email}
              onChange={(e) => setForm({ ...form, email: e.target.value })}
            />
          </label>
          {mode === 'signup' && (
            <label>
              이름
              <input
                type="text"
                required
                value={form.name}
                onChange={(e) => setForm({ ...form, name: e.target.value })}
              />
            </label>
          )}
          <label>
            비밀번호
            <input
              type="password"
              required
              minLength={8}
              value={form.password}
              onChange={(e) => setForm({ ...form, password: e.target.value })}
            />
          </label>
          {error && <p className="login-error">{error}</p>}
          <button type="submit" className="submit" disabled={submitting}>
            {submitting ? '처리 중...' : mode === 'login' ? '로그인' : '회원가입'}
          </button>
        </form>

        <div className="login-divider">또는</div>

        {isNativeShell ? (
          <button type="button" onClick={openLoginInSystemBrowser}>Google 계정으로 로그인</button>
        ) : (
          <GoogleOAuthProvider clientId={import.meta.env.VITE_GOOGLE_CLIENT_ID}>
            <GoogleLogin
              onSuccess={handleGoogleSuccess}
              onError={handleGoogleError}
            />
          </GoogleOAuthProvider>
        )}
      </div>
    </div>
  );
}

export default LoginPage;
