import { Link } from "react-router-dom";

export default function NotFound() {
  return (
    <div className="min-h-screen bg-gray-100 flex flex-col justify-center items-center px-4">
      <div className="max-w-md w-full text-center">
        <h1 className="text-9xl font-bold text-indigo-600">404</h1>

        <div className="mb-8 mt-4">
          <div className="text-6xl font-medium text-gray-400">Oops!</div>
          <div className="text-2xl font-medium text-gray-500 mt-4">
            Page not found
          </div>
          <div className="text-gray-400 mt-2">
            The page you're looking for doesn't exist or has been moved.
          </div>
        </div>

        <Link
          to="/"
          className="inline-flex items-center px-6 py-3 border border-transparent text-base font-medium rounded-md text-white bg-indigo-600 hover:bg-indigo-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-indigo-500 transition-colors duration-200"
        >
          Go back home
        </Link>
      </div>

      {/* Decorative elements */}
      <div className="absolute inset-0 -z-10 overflow-hidden">
        <div className="absolute left-[40%] top-[20%] h-72 w-72 -translate-x-1/2 rounded-full bg-indigo-100 opacity-20"></div>
        <div className="absolute right-[45%] bottom-[30%] h-96 w-96 translate-x-1/2 rounded-full bg-indigo-100 opacity-20"></div>
      </div>
    </div>
  );
}
