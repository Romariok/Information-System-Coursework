import { useState } from "react";
import { useQuery } from "@tanstack/react-query";
import { Link } from "react-router-dom";
import Navbar from "../components/Navbar";
import Pagination from "../components/Pagination";
import { getArticles, searchArticlesByHeader } from "../services/api";

type SortOption = {
  field: "date";
  direction: boolean;
};

export default function Articles() {
  const [page, setPage] = useState(1);
  const [searchQuery, setSearchQuery] = useState("");
  const [sort, setSort] = useState<SortOption>({
    field: "date",
    direction: false,
  });
  const pageSize = 12;

  const {
    data: articlesData,
    isLoading,
    error,
  } = useQuery({
    queryKey: ["articles", page, searchQuery, sort],
    queryFn: async () => {
      const from = (page - 1) * pageSize;
      if (searchQuery.trim()) {
        return await searchArticlesByHeader(searchQuery, from, pageSize);
      }
      return await getArticles(from, pageSize, sort.direction);
    },
  });

  const hasMore = articlesData?.items.length === pageSize;

  const handleSearch = (e: React.FormEvent<HTMLFormElement>) => {
    e.preventDefault();
    setPage(1); // Reset to first page when searching
  };

  const handleSortChange = () => {
    setSort((prev) => ({
      field: "date",
      direction: !prev.direction,
    }));
  };

  if (error) return <div>Error loading articles</div>;

  return (
    <div className="min-h-screen bg-gray-50">
      <Navbar />

      <main className="max-w-7xl mx-auto px-4 py-8">
        <div className="mb-8">
          <h1 className="text-3xl font-bold text-gray-900 mb-4">Articles</h1>

          {/* Search Form */}
          <form onSubmit={handleSearch} className="mb-6">
            <div className="flex gap-4">
              <input
                type="text"
                value={searchQuery}
                onChange={(e) => setSearchQuery(e.target.value)}
                placeholder="Search articles by header..."
                className="flex-1 p-2 border border-gray-300 rounded-md"
              />
              <button
                type="submit"
                className="px-4 py-2 bg-indigo-600 text-white rounded-md hover:bg-indigo-700"
              >
                Search
              </button>
            </div>
          </form>

          {/* Updated Sort Controls */}
          <div className="flex gap-4 mb-6">
            <button
              onClick={handleSortChange}
              className="px-4 py-2 rounded-md bg-indigo-600 text-white"
            >
              Date {sort.direction ? "↑" : "↓"}
            </button>
          </div>
        </div>

        {/* Articles Grid */}
        {isLoading ? (
          <div className="text-center py-8">Loading articles...</div>
        ) : (
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
            {articlesData?.items.map((article) => (
              <div
                key={article.id}
                className="bg-white p-6 rounded-lg shadow-md hover:shadow-lg transition-shadow"
              >
                <h2 className="text-xl font-semibold mb-2">{article.header}</h2>
                <p className="text-gray-600 mb-4 line-clamp-3">
                  {article.text}
                </p>
                <div className="text-sm text-gray-500 mb-4">
                  By{" "}
                  <span className="font-medium">{article.author.username}</span>
                  <br />
                  {new Date(article.createdAt).toLocaleDateString()}
                </div>
                <Link
                  to={`/article/${article.id}`}
                  className="text-indigo-600 hover:text-indigo-800"
                >
                  Read More →
                </Link>
              </div>
            ))}
          </div>
        )}

        {/* Pagination */}
        {(articlesData?.items.length ?? 0) > 0 && (
          <div className="mt-8">
            <Pagination
              currentPage={page}
              hasMore={hasMore}
              onPageChange={setPage}
            />
          </div>
        )}
      </main>
    </div>
  );
}
