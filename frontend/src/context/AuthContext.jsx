import React, { createContext, useState, useContext } from 'react';

const AuthContext = createContext(null);

// localStorage를 lazy initializer로 동기 로딩한다 — useEffect로 마운트 후에 읽으면
// 보호된 라우트를 하드 리로드했을 때 첫 렌더에서 isAuthenticated=false로 판정되어
// ProtectedRoute가 /login으로 잘못 리다이렉트한다.
function loadStoredAuth() {
  try {
    const storedUser = localStorage.getItem('user');
    const storedToken = localStorage.getItem('token');
    const storedRole = localStorage.getItem('role');
    if (storedUser && storedToken) {
      return { user: JSON.parse(storedUser), token: storedToken, role: storedRole };
    }
  } catch {
    // 손상된 저장값은 무시하고 비로그인으로 시작
  }
  return { user: null, token: null, role: null };
}

export const AuthProvider = ({ children }) => {
    const [initial] = useState(loadStoredAuth);
    const [user, setUser] = useState(initial.user);
    const [token, setToken] = useState(initial.token);
    const [role, setRole] = useState(initial.role);

    const login = (userData, jwtToken) => {
      setUser(userData);
      setToken(jwtToken);
      setRole(userData.role); // Assuming userData contains the role
      localStorage.setItem('user', JSON.stringify(userData));
      localStorage.setItem('token', jwtToken);
      localStorage.setItem('role', userData.role);
    };
  
    const logout = () => {
      setUser(null);
      setToken(null);
      setRole(null);
      localStorage.removeItem('user');
      localStorage.removeItem('token');
      localStorage.removeItem('role');
    };
  
    const isAuthenticated = !!user && !!token;
  
    return (
      <AuthContext.Provider value={{ user, token, role, isAuthenticated, login, logout }}>
      {children}
    </AuthContext.Provider>
  );
};

export const useAuth = () => {
  return useContext(AuthContext);
};
