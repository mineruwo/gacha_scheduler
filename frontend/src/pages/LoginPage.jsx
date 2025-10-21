import React from 'react';
import { GoogleOAuthProvider, GoogleLogin } from '@react-oauth/google';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';

function LoginPage() {
  const navigate = useNavigate();
  const { login } = useAuth();

  const handleSuccess = async (credentialResponse) => {
    console.log('Google login success:', credentialResponse);
    try {
      const response = await fetch(`${import.meta.env.VITE_API_BASE_URL}/api/auth/google`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({ idToken: credentialResponse.credential }),
      });

      if (response.ok) {
        const userData = await response.json();
        console.log('Backend response:', userData);
        login(userData, 'dummy-jwt-token'); // Replace 'dummy-jwt-token' with actual JWT from backend
        navigate('/');
      } else {
        console.error('Backend authentication failed:', response.statusText);
      }
    } catch (error) {
      console.error('Error sending ID token to backend:', error);
    }
  };

  const handleError = () => {
    console.error('Google login failed');
  };

  return (
    <div style={{ display: 'flex', flexDirection: 'column', alignItems: 'center', justifyContent: 'center', minHeight: 'calc(100vh - 60px)', background: 'var(--background-color)', color: 'var(--text-color)' }}>
      <div style={{
        background: 'var(--header-bg)',
        padding: '40px',
        borderRadius: '10px',
        boxShadow: '0 4px 8px rgba(0, 0, 0, 0.1)',
        display: 'flex',
        flexDirection: 'column',
        alignItems: 'center',
        gap: '20px',
        maxWidth: '400px',
        width: '90%',
        textAlign: 'center'
      }}>
        <h1>로그인</h1>
        <p>Google 계정으로 로그인하여 서비스를 이용하세요.</p>
        <GoogleOAuthProvider clientId={import.meta.env.VITE_GOOGLE_CLIENT_ID}>
          <GoogleLogin
            onSuccess={handleSuccess}
            onError={handleError}
          />
        </GoogleOAuthProvider>
      </div>
    </div>
  );
}

export default LoginPage;
