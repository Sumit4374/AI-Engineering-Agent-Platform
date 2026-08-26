"use client";

import { createContext, useContext, useState, useEffect, useCallback } from "react";
import {
  getToken,
  setToken,
  removeToken,
  getStoredUser,
  setStoredUser,
  isAuthenticated as checkAuth,
  StoredUser,
} from "@/lib/auth";
import { login as apiLogin, signup as apiSignup } from "@/lib/api";

interface AuthContextValue {
  user: StoredUser | null;
  token: string | null;
  isAuthenticated: boolean;
  isLoading: boolean;
  login: (loginId: string, password: string) => Promise<void>;
  signup: (userName: string, email: string, password: string, phone?: string) => Promise<void>;
  logout: () => void;
}

const AuthContext = createContext<AuthContextValue | null>(null);

export function AuthProvider({ children }: { children: React.ReactNode }) {
  const [user, setUser] = useState<StoredUser | null>(null);
  const [token, setTokenState] = useState<string | null>(null);
  const [isLoading, setIsLoading] = useState(true);

  useEffect(() => {
    const stored = getStoredUser();
    const tok = getToken();
    if (checkAuth() && stored && tok) {
      setUser(stored);
      setTokenState(tok);
    }
    setIsLoading(false);
  }, []);

  const login = useCallback(async (loginId: string, password: string) => {
    const res = await apiLogin(loginId, password);
    setToken(res.token);
    const u: StoredUser = {
      userId: res.userId,
      userName: res.userName,
      email: res.email,
      role: res.role,
    };
    setStoredUser(u);
    setUser(u);
    setTokenState(res.token);
  }, []);

  const signup = useCallback(
    async (userName: string, email: string, password: string, phone?: string) => {
      const res = await apiSignup(userName, email, password, phone);
      setToken(res.token);
      const u: StoredUser = {
        userId: res.userId,
        userName: res.userName,
        email: res.email,
        role: res.role,
      };
      setStoredUser(u);
      setUser(u);
      setTokenState(res.token);
    },
    []
  );

  const logout = useCallback(() => {
    removeToken();
    setUser(null);
    setTokenState(null);
    window.location.href = "/login";
  }, []);

  return (
    <AuthContext.Provider
      value={{ user, token, isAuthenticated: !!user && !!token, isLoading, login, signup, logout }}
    >
      {children}
    </AuthContext.Provider>
  );
}

export function useAuth(): AuthContextValue {
  const ctx = useContext(AuthContext);
  if (!ctx) throw new Error("useAuth must be used within AuthProvider");
  return ctx;
}
