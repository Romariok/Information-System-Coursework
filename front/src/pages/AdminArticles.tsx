import { useState } from "react";
import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import Navbar from "../components/Navbar";
import Pagination from "../components/Pagination";
import { getUnacceptedArticles, moderateArticle } from "../services/api";

export default function AdminArticles() {
  const [page, setPage] = useState(1);
  const pageSize = 10;
  const queryClient = useQueryClient();

  const {
    data: articlesData,
    isLoading,
    error,
  } = useQuery({
    queryKey: ["unacceptedArticles", page],
    queryFn: () => getUnacceptedArticles((page - 1) * pageSize, pageSize),
  });

  const moderateMutation = useMutation({
    mutationFn: ({
      articleId,
      accepted,
    }: {
      articleId: number;
      accepted: boolean;
    }) => moderateArticle(articleId, accepted),
    onSuccess: (_, variables) => {
      if (!variables.accepted && articlesData?.items.length === 1 && page > 1) {
        setPage((prev) => prev - 1);
      }
      queryClient.invalidateQueries({ queryKey: ["unacceptedArticles"] });
    },
  });

  if (error) return <div>Error loading articles</div>;

  return (
    <div className="min-h-screen bg-gray-50">
      <Navbar />
      <main className="max-w-7xl mx-auto px-4 py-8">
        <h1 className="text-3xl font-bold text-gray-900 mb-8">
          Pending Articles
        </h1>
        {isLoading ? (
          <div className="text-center py-8">Loading articles...</div>
        ) : (
          <div className="space-y-6">
            {articlesData?.items.map((item) => (
              <div
                key={item.article.id}
                className="bg-white p-6 rounded-lg shadow-md"
              >
                <div className="flex justify-between items-start">
                  <div className="flex-1">
                    <h2 className="text-xl font-semibold mb-2">
                      {item.article.header}
                    </h2>
                    <p className="text-gray-600 mb-4">{item.article.text}</p>
                    <div className="text-sm text-gray-500">
                      By {item.article.author.username} •{" "}
                      {new Date(item.article.createdAt).toLocaleDateString()}
                    </div>
                    {item.product && (
                      <div className="mt-4 p-4 bg-gray-50 rounded-md">
                        <h3 className="font-medium mb-2">Related Product:</h3>
                        <p>{item.product.name}</p>
                        <p className="text-sm text-gray-500">
                          {item.product.description}
                        </p>
                      </div>
                    )}
                  </div>
                  <div className="flex gap-2 ml-4">
                    <button
                      onClick={() =>
                        moderateMutation.mutate({
                          articleId: item.article.id,
                          accepted: true,
                        })
                      }
                      className="px-4 py-2 bg-green-600 text-white rounded-md hover:bg-green-700"
                      disabled={moderateMutation.isPending}
                    >
                      Approve
                    </button>
                    <button
                      onClick={() =>
                        moderateMutation.mutate({
                          articleId: item.article.id,
                          accepted: false,
                        })
                      }
                      className="px-4 py-2 bg-red-600 text-white rounded-md hover:bg-red-700"
                      disabled={moderateMutation.isPending}
                    >
                      Reject & Delete
                    </button>
                  </div>
                </div>
              </div>
            ))}
          </div>
        )}
        {(articlesData?.items.length ?? 0) > 0 && (
          <div className="mt-8">
            <Pagination
              currentPage={page}
              hasMore={(articlesData?.items.length ?? 0) === pageSize}
              onPageChange={setPage}
            />
          </div>
        )}
      </main>
    </div>
  );
}
