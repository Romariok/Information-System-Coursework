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
          <button
            onClick={logout}
            className="ml-4 hover:bg-indigo-700 px-3 py-2 rounded-md"
          >
            Logout
          </button>
        </div>
      </div>
    </nav>
  );
}
