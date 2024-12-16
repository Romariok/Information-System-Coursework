import { useQuery } from "@tanstack/react-query";
import { useParams, Navigate } from "react-router-dom";
import Navbar from "../components/Navbar";
import { getArticleDetails } from "../services/api";
import { Article } from "../services/types";

export default function ArticleDetails() {
  const { id } = useParams();

  const {
    data: article,
    isLoading,
    error,
  } = useQuery<Article>({
    queryKey: ["article", id],
    queryFn: () => getArticleDetails(id!),
  });

  if (error) {
    return <Navigate to="*" />;
  }

  if (isLoading) {
    return <div>Loading...</div>;
  }

  if (!article) {
    return <Navigate to="/error" />;
  }

  return (
    <div className="min-h-screen bg-gray-50">
      <Navbar />

      <main className="max-w-4xl mx-auto px-4 py-8">
        <article className="bg-white rounded-lg shadow-md p-8">
          <h1 className="text-3xl font-bold text-gray-900 mb-4">
            {article.header}
          </h1>

          <div className="flex items-center text-gray-600 mb-6">
            <span className="font-medium">By {article.author}</span>
            <span className="mx-2">•</span>
            <time dateTime={article.createdAt}>
              {new Date(article.createdAt).toLocaleDateString()}
            </time>
          </div>

          <div className="prose prose-lg max-w-none">
            {article.text.split("\n").map((paragraph, index) => (
              <p key={index} className="mb-4">
                {paragraph}
              </p>
            ))}
          </div>
        </article>
      </main>
    </div>
  );
}
