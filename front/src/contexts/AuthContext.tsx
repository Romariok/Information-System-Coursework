import { createContext, useContext, useState, ReactNode } from "react";
import { useNavigate } from "react-router-dom";

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
  login: (userData: any) => void;
  logout: () => void;
}

const AuthContext = createContext<AuthContextType | undefined>(undefined);

export function AuthProvider({ children }: { children: ReactNode }) {
  const navigate = useNavigate();
  const [user, setUser] = useState<User | null>(() => {
    const savedUser = localStorage.getItem("user");
    return savedUser ? JSON.parse(savedUser) : null;
  });

  const login = (userData: any) => {
    const userWithRole = {
      ...userData,
      role: userData.isAdmin ? "ROLE_ADMIN" : "ROLE_USER"
    };
    setUser(userWithRole);
    localStorage.setItem("user", JSON.stringify(userWithRole));
    localStorage.setItem("token", userData.token);
    navigate("/");
  };

  const logout = () => {
    setUser(null);
    localStorage.removeItem("user");
    localStorage.removeItem("token");
    navigate("/login");
  };

  return (
    <AuthContext.Provider
      value={{ isAuthenticated: !!user, user, login, logout }}
    >
      {children}
    </AuthContext.Provider>
  );
}

export function useAuth() {
  const context = useContext(AuthContext);
  if (context === undefined) {
    throw new Error("useAuth must be used within an AuthProvider");
  }
  return context;
}
