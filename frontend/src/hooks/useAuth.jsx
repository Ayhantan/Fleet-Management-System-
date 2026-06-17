import { createContext, useContext, useEffect, useState } from "react";
import { loginRequest, registerRequest } from "../services/authService.js";
import { clearStoredAuth, getStoredAuth, storeAuth } from "../utils/authStorage.js";

const AuthContext = createContext(null);

export function AuthProvider({ children }) {
  const [auth, setAuth] = useState(() => getStoredAuth());

  useEffect(() => {
    const syncAuth = () => setAuth(getStoredAuth());

    window.addEventListener("auth-changed", syncAuth);
    return () => window.removeEventListener("auth-changed", syncAuth);
  }, []);

  const value = {
    user: auth?.user ?? null,
    token: auth?.token ?? null,
    isAuthenticated: Boolean(auth?.token),
    async login(credentials) {
      const response = await loginRequest(credentials);
      const nextAuth = buildAuthState(response);
      storeAuth(nextAuth);
      setAuth(nextAuth);
      return nextAuth;
    },
    async register(payload) {
      const response = await registerRequest(payload);
      const nextAuth = buildAuthState(response);
      storeAuth(nextAuth);
      setAuth(nextAuth);
      return nextAuth;
    },
    logout() {
      clearStoredAuth();
      setAuth(null);
    },
  };

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

function buildAuthState(response) {
  return {
    token: response.token,
    user: {
      username: response.username,
      email: response.email,
      role: response.role,
    },
  };
}

export function useAuth() {
  const context = useContext(AuthContext);

  if (!context) {
    throw new Error("useAuth must be used inside AuthProvider");
  }

  return context;
}
