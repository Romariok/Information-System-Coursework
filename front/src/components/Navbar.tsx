import { Link } from "react-router-dom";
import { useAuth } from "../contexts/AuthContext";

export default function Navbar() {
  const { logout } = useAuth();

  return (
    <nav className="bg-indigo-600 text-white shadow-lg">
      <div className="max-w-7xl mx-auto px-4">
        <div className="flex justify-between h-16">
          <div className="flex items-center">
            <Link to="/" className="text-xl font-bold">
              GMatch
            </Link>
            <div className="ml-10 flex space-x-4">
              <Link
                to="/catalog"
                className="hover:bg-indigo-700 px-3 py-2 rounded-md"
              >
                Catalog
              </Link>
              <Link
                to="/articles"
                className="hover:bg-indigo-700 px-3 py-2 rounded-md"
              >
                Articles
              </Link>
              <Link
                to="/musicians"
                className="hover:bg-indigo-700 px-3 py-2 rounded-md"
              >
                Musicians
              </Link>
              <Link
                to="/brand"
                className="hover:bg-indigo-700 px-3 py-2 rounded-md"
              >
                Brands
              </Link>
              <Link
                to="/forum"
                className="hover:bg-indigo-700 px-3 py-2 rounded-md"
              >
                Forum
              </Link>
            </div>
          </div>
          <div className="flex items-center space-x-4">
            <Link
              to="/profile"
              className="hover:bg-indigo-700 px-3 py-2 rounded-md flex items-center"
            >
              <svg
                className="w-5 h-5 mr-2"
                fill="none"
                stroke="currentColor"
                viewBox="0 0 24 24"
                xmlns="http://www.w3.org/2000/svg"
              >
                <path
                  strokeLinecap="round"
                  strokeLinejoin="round"
                  strokeWidth={2}
                  d="M16 7a4 4 0 11-8 0 4 4 0 018 0zM12 14a7 7 0 00-7 7h14a7 7 0 00-7-7z"
                />
              </svg>
              Profile
            </Link>
            <button
              onClick={logout}
              className="hover:bg-indigo-700 px-3 py-2 rounded-md flex items-center"
            >
              <svg
                className="w-5 h-5 mr-2"
                fill="none"
                stroke="currentColor"
                viewBox="0 0 24 24"
                xmlns="http://www.w3.org/2000/svg"
              >
                <path
                  strokeLinecap="round"
                  strokeLinejoin="round"
                  strokeWidth={2}
                  d="M17 16l4-4m0 0l-4-4m4 4H7m6 4v1a3 3 0 01-3 3H6a3 3 0 01-3-3V7a3 3 0 013-3h4a3 3 0 013 3v1"
                />
              </svg>
              Logout
            </button>
          </div>
        </div>
      </div>
    </nav>
  );
}
