import { useState } from "react";
import { useQuery } from "@tanstack/react-query";
import Navbar from "../components/Navbar";
import Pagination from "../components/Pagination";
import api from "../services/api";
import { Brand } from "../services/types";
import { Link } from "react-router-dom";

export default function Brands() {
  const [page, setPage] = useState(1);
  const pageSize = 12;
  const { data: brandsData, isLoading } = useQuery<Brand[]>({
    queryKey: ["brands", page],
    queryFn: async () => {
      const response = await api.get("/brand", {
        params: {
          from: (page - 1) * pageSize,
          size: pageSize,
        },
      });
      return response.data;
    },
  });
  const hasMore = brandsData?.length === pageSize;
  return (
    <div className="min-h-screen bg-gray-50">
      <Navbar />
      <main className="max-w-7xl mx-auto px-4 py-8">
        <h1 className="text-3xl font-bold text-gray-900 mb-8">Music Brands</h1>
        {isLoading ? (
          <div className="text-center py-8">Loading brands...</div>
        ) : (
          <>
            <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
              {brandsData?.map((brand) => (
                <div
                  key={brand.id}
                  className="bg-white rounded-lg shadow-md overflow-hidden hover:shadow-lg transition-shadow"
                >
                  <Link to={`/brand/${brand.id}`} className="block p-6">
                    <div className="flex items-center justify-between mb-4">
                      <h2 className="text-xl font-semibold text-gray-900">
                        {brand.name}
                      </h2>
                      <span className="px-3 py-1 text-sm font-medium text-gray-600 bg-gray-100 rounded-full">
                        {brand.country}
                      </span>
                    </div>

                    <div className="space-y-3">
                      <div className="flex items-center text-gray-600">
                        <svg
                          className="w-5 h-5 mr-2"
                          fill="none"
                          stroke="currentColor"
                          viewBox="0 0 24 24"
                        >
                          <path
                            strokeLinecap="round"
                            strokeLinejoin="round"
                            strokeWidth="2"
                            d="M21 12a9 9 0 01-9 9m9-9a9 9 0 00-9-9m9 9H3m9 9a9 9 0 01-9-9m9 9c1.657 0 3-4.03 3-9s-1.343-9-3-9m0 18c-1.657 0-3-4.03-3-9s1.343-9 3-9m-9 9a9 9 0 019-9"
                          />
                        </svg>
                        <a
                          href={brand.website}
                          target="_blank"
                          rel="noopener noreferrer"
                          className="text-indigo-600 hover:text-indigo-800"
                        >
                          Visit Website
                        </a>
                      </div>

                      <div className="flex items-center text-gray-600">
                        <svg
                          className="w-5 h-5 mr-2"
                          fill="none"
                          stroke="currentColor"
                          viewBox="0 0 24 24"
                        >
                          <path
                            strokeLinecap="round"
                            strokeLinejoin="round"
                            strokeWidth="2"
                            d="M3 8l7.89 5.26a2 2 0 002.22 0L21 8M5 19h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v10a2 2 0 002 2z"
                          />
                        </svg>
                        <a
                          href={`mailto:${brand.email}`}
                          className="text-indigo-600 hover:text-indigo-800"
                        >
                          {brand.email}
                        </a>
                      </div>
                    </div>
                  </Link>
                </div>
              ))}
            </div>
            {brandsData?.length !== 0 && (
              <div className="mt-8">
                <Pagination
                  currentPage={page}
                  hasMore={hasMore}
                  onPageChange={setPage}
                />
              </div>
            )}
          </>
        )}
      </main>
    </div>
  );
}
