import { useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import { useMutation } from "@tanstack/react-query";
import type { AxiosError } from "axios";
import { register } from "../services/api";
import { useAuth } from "../contexts/AuthContext";

export default function Register() {
  const navigate = useNavigate();
  const { login } = useAuth();
  const [username, setUsername] = useState("");
  const [password, setPassword] = useState("");
  const [confirmPassword, setConfirmPassword] = useState("");
  const [error, setError] = useState("");

  const registerMutation = useMutation({
    mutationFn: () => register(username, password),
    onSuccess: (data) => {
      login(data);
      void navigate("/");
    },
    onError: (error: unknown) => {
      const axiosError = error as AxiosError<{ message?: string }>;
      setError(axiosError.response?.data?.message || "An error occurred");
    },
  });

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    setError("");

    if (password !== confirmPassword) {
      setError("Passwords do not match");
      return;
    }

    registerMutation.mutate();
  };

  return (
    <div className="min-h-screen flex flex-col items-center justify-center bg-gradient-to-br from-indigo-100 via-purple-50 to-pink-100">
      <div className="w-full max-w-md px-8 py-6 mt-4 text-left bg-white shadow-lg rounded-xl">
        <div className="mb-8">
          <h3 className="text-3xl font-bold text-center text-gray-800">
            Create Account
          </h3>
          <p className="mt-3 text-center text-gray-600">
            Join our community today
          </p>
        </div>

        <form onSubmit={handleSubmit}>
          {error && (
            <div className="mb-4 p-4 rounded-lg bg-red-50 border border-red-200">
              <p className="text-sm text-red-600">{error}</p>
            </div>
          )}

          <div className="space-y-5">
            <div>
              <label className="block text-sm font-medium text-gray-700">
                Username
              </label>
              <input
                type="text"
                required
                value={username}
                onChange={(e) => setUsername(e.target.value)}
                className="w-full px-4 py-3 mt-2 text-gray-700 bg-gray-50 border border-gray-200 rounded-lg focus:outline-none focus:border-indigo-400 focus:ring-1 focus:ring-indigo-400"
                placeholder="Choose a username"
              />
            </div>

            <div>
              <label className="block text-sm font-medium text-gray-700">
                Password
              </label>
              <input
                type="password"
                required
                value={password}
                onChange={(e) => setPassword(e.target.value)}
                className="w-full px-4 py-3 mt-2 text-gray-700 bg-gray-50 border border-gray-200 rounded-lg focus:outline-none focus:border-indigo-400 focus:ring-1 focus:ring-indigo-400"
                placeholder="Create a password"
              />
            </div>

            <div>
              <label className="block text-sm font-medium text-gray-700">
                Confirm Password
              </label>
              <input
                type="password"
                required
                value={confirmPassword}
                onChange={(e) => setConfirmPassword(e.target.value)}
                className="w-full px-4 py-3 mt-2 text-gray-700 bg-gray-50 border border-gray-200 rounded-lg focus:outline-none focus:border-indigo-400 focus:ring-1 focus:ring-indigo-400"
                placeholder="Confirm your password"
              />
            </div>

            <button
              type="submit"
              disabled={registerMutation.isPending}
              className="w-full px-4 py-3 text-white font-medium bg-indigo-600 hover:bg-indigo-700 rounded-lg transition-colors duration-200 ease-in-out disabled:opacity-70 disabled:cursor-not-allowed"
            >
              {registerMutation.isPending ? (
                <span className="flex items-center justify-center">
                  <svg
                    className="animate-spin -ml-1 mr-3 h-5 w-5 text-white"
                    xmlns="http://www.w3.org/2000/svg"
                    fill="none"
                    viewBox="0 0 24 24"
                  >
                    <circle
                      className="opacity-25"
                      cx="12"
                      cy="12"
                      r="10"
                      stroke="currentColor"
                      strokeWidth="4"
                    ></circle>
                    <path
                      className="opacity-75"
                      fill="currentColor"
                      d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"
                    ></path>
                  </svg>
                  Creating account...
                </span>
              ) : (
                "Create account"
              )}
            </button>
          </div>
        </form>

        <p className="mt-6 text-center text-gray-600">
          Already have an account?{" "}
          <Link
            to="/login"
            className="text-indigo-600 hover:text-indigo-700 font-medium"
          >
            Sign in
          </Link>
        </p>
      </div>
    </div>
  );
}
