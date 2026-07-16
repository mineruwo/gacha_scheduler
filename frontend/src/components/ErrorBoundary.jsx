import React from 'react';

class ErrorBoundary extends React.Component {
  constructor(props) {
    super(props);
    this.state = { hasError: false };
  }

  static getDerivedStateFromError() {
    return { hasError: true };
  }

  componentDidCatch(error, info) {
    console.error('Unhandled UI error:', error, info);
  }

  render() {
    if (this.state.hasError) {
      return (
        <div
          style={{
            display: 'flex',
            flexDirection: 'column',
            alignItems: 'center',
            justifyContent: 'center',
            gap: '16px',
            minHeight: '100vh',
            padding: '20px',
            textAlign: 'center',
            background: 'var(--background-color)',
            color: 'var(--text-color)',
          }}
        >
          <h1 style={{ fontSize: '1.6em' }}>문제가 발생했습니다</h1>
          <p>화면을 표시하는 중 오류가 발생했습니다. 새로고침해서 다시 시도해주세요.</p>
          <button type="button" onClick={() => window.location.assign('/')}>
            홈으로 이동
          </button>
        </div>
      );
    }

    return this.props.children;
  }
}

export default ErrorBoundary;
