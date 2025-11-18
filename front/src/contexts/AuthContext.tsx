import type { ReactNode } from "react";
import { createContext, useContext, useState } from "react";
import { useNavigate } from "react-router-dom";
import type { AuthUser } from "../services/api";

interface User {
  id: number;
  username: string;
  token: string;
  role: "ROLE_ADMIN" | "ROLE_USER";
  isAdmin: boolean;
}

interface AuthContextType {
  isAuthenticated: boolean;
  user: User | null;
  login: (userData: AuthUser) => void;
  logout: () => void;
}

const AuthContext = createContext<AuthContextType | undefined>(undefined);

export function AuthProvider({ children }: { children: ReactNode }) {
  const navigate = useNavigate();
  const [user, setUser] = useState<User | null>(() => {
    const savedUser = localStorage.getItem("user");
    if (!savedUser) return null;
    try {
      const parsed: unknown = JSON.parse(savedUser);
      if (
        parsed &&
        typeof parsed === "object" &&
        "id" in parsed &&
        "username" in parsed &&
        "token" in parsed &&
        "isAdmin" in parsed &&
        "role" in parsed
      ) {
        const p = parsed as User;
        return p;
      }
      return null;
    } catch {
      return null;
    }
  });

  const login = (userData: AuthUser) => {
    const userWithRole: User = {
      ...userData,
      role: userData.isAdmin ? "ROLE_ADMIN" : "ROLE_USER",
    };
    setUser(userWithRole);
    localStorage.setItem("user", JSON.stringify(userWithRole));
    localStorage.setItem("token", userData.token);
    void navigate("/");
  };

  const logout = () => {
    setUser(null);
    localStorage.removeItem("user");
    localStorage.removeItem("token");
    void navigate("/login");
  };

  return (
    <AuthContext.Provider
      value={{ isAuthenticated: !!user, user, login, logout }}
    >
      {children}
    </AuthContext.Provider>
  );
}

// eslint-disable-next-line react-refresh/only-export-components
export function useAuth() {
  const context = useContext(AuthContext);
  if (context === undefined) {
    throw new Error("useAuth must be used within an AuthProvider");
  }
  return context;
}
