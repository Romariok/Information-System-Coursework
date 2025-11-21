import { useState } from "react";
import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import { Link } from "react-router-dom";
import Navbar from "../components/Navbar";
import Pagination from "../components/Pagination";
import {
  getArticles,
  searchArticlesByHeader,
  createArticle,
} from "../services/api";

type SortOption = {
  field: "date";
  direction: boolean;
};

export default function Articles() {
  const [page, setPage] = useState(1);
  const [searchQuery, setSearchQuery] = useState("");
  const [isCreateModalOpen, setIsCreateModalOpen] = useState(false);
  const [newArticle, setNewArticle] = useState({
    productName: "",
    header: "",
    text: "",
  });
  const [sort, setSort] = useState<SortOption>({
    field: "date",
    direction: false,
  });
  const pageSize = 12;
  const queryClient = useQueryClient();

  const {
    data: articlesData,
    isLoading,
    error: queryError,
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

  const createArticleMutation = useMutation({
    mutationFn: (data: { productName: string; header: string; text: string }) =>
      createArticle(data.productName, data.header, data.text),
    onSuccess: () => {
      void queryClient.invalidateQueries({ queryKey: ["articles"] });
      setIsCreateModalOpen(false);
      setNewArticle({ productName: "", header: "", text: "" });
    },
    onError: (mutationError) => {
      console.error("Error creating article:", mutationError);
    },
  });

  const handleCreateArticle = (e: React.FormEvent) => {
    e.preventDefault();
    createArticleMutation.mutate(newArticle);
  };

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

  return (
    <div className="min-h-screen bg-gray-50">
      <Navbar />

      <main className="max-w-7xl mx-auto px-4 py-8">
        <div className="mb-8">
          <div className="flex justify-between items-center mb-4">
            <h1 className="text-3xl font-bold text-gray-900">Articles</h1>
            <button
              onClick={() => setIsCreateModalOpen(true)}
              className="px-4 py-2 bg-green-600 text-white rounded-md hover:bg-green-700"
            >
              Create Article
            </button>
          </div>

          {/* Error Display */}
          {queryError && (
            <div className="mb-6 p-4 text-red-700 bg-red-100 border border-red-400 rounded-md">
              Failed to load articles: {(queryError).message}
            </div>
          )}

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

          {/* Sort Controls */}
          <div className="flex gap-4 mb-6">
            <button
              onClick={handleSortChange}
              className="px-4 py-2 rounded-md bg-indigo-600 text-white"
            >
              Date {sort.direction ? "↑" : "↓"}
            </button>
          </div>
        </div>

        {/* Create Article Modal */}
        {isCreateModalOpen && (
          <div className="fixed inset-0 z-50  backdrop-blur-sm flex items-center justify-center p-4">
            <div className="bg-white rounded-lg p-6 max-w-2xl w-full">
              <h2 className="text-2xl font-bold mb-4">Create New Article</h2>
              <form onSubmit={handleCreateArticle} className="space-y-4">
                <div>
                  {/* Error Display */}
                  {queryError && (
                    <div className="mb-6 p-4 text-red-700 bg-red-100 border border-red-400 rounded-md">
                      Failed to load articles: product name not found
                    </div>
                  )}
                  {createArticleMutation.isError && (
                    <div className="mb-6 p-4 text-red-700 bg-red-100 border border-red-400 rounded-md">
                      Failed to create article: product name not found
                    </div>
                  )}
                  <label className="block text-sm font-medium text-gray-700 mb-1">
                    Product Name
                  </label>
                  <input
                    type="text"
                    value={newArticle.productName}
                    onChange={(e) =>
                      setNewArticle((prev) => ({
                        ...prev,
                        productName: e.target.value,
                      }))
                    }
                    className="w-full p-2 border border-gray-300 rounded-md"
                    required
                  />
                </div>
                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-1">
                    Header
                  </label>
                  <input
                    type="text"
                    value={newArticle.header}
                    onChange={(e) =>
                      setNewArticle((prev) => ({
                        ...prev,
                        header: e.target.value,
                      }))
                    }
                    className="w-full p-2 border border-gray-300 rounded-md"
                    required
                  />
                </div>
                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-1">
                    Content
                  </label>
                  <textarea
                    value={newArticle.text}
                    onChange={(e) =>
                      setNewArticle((prev) => ({
                        ...prev,
                        text: e.target.value,
                      }))
                    }
                    rows={10}
                    className="w-full p-2 border border-gray-300 rounded-md"
                    required
                  />
                </div>
                <div className="flex justify-end gap-2">
                  <button
                    type="button"
                    onClick={() => setIsCreateModalOpen(false)}
                    className="px-4 py-2 text-gray-600 hover:text-gray-800"
                  >
                    Cancel
                  </button>
                  <button
                    type="submit"
                    className="px-4 py-2 bg-indigo-600 text-white rounded-md hover:bg-indigo-700"
                    disabled={createArticleMutation.isPending}
                  >
                    {createArticleMutation.isPending
                      ? "Creating..."
                      : "Create"}
                  </button>
                </div>
              </form>
            </div>
          </div>
        )}

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
