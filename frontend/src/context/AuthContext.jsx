import React, { createContext, useState, useContext, useEffect } from 'react';

const AuthContext = createContext(null);

export const AuthProvider = ({ children }) => {
    const [user, setUser] = useState(null);
    const [token, setToken] = useState(null);
    const [role, setRole] = useState(null);
  
    useEffect(() => {
      const storedUser = localStorage.getItem('user');
      const storedToken = localStorage.getItem('token');
      const storedRole = localStorage.getItem('role');
      if (storedUser && storedToken) {
        setUser(JSON.parse(storedUser));
        setToken(storedToken);
        setRole(storedRole);
      }
    }, []);
  
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
