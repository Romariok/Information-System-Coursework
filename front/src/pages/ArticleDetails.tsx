import { useState } from "react";
import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import { useParams, Navigate } from "react-router-dom";
import Navbar from "../components/Navbar";
import Pagination from "../components/Pagination";
import {
  getArticleDetails,
  getArticleFeedbacks,
  addArticleFeedback,
} from "../services/api";
import { Article, Feedback } from "../services/types";

export default function ArticleDetails() {
  const { id } = useParams();
  const queryClient = useQueryClient();
  const [feedbackPage, setFeedbackPage] = useState(1);
  const [rating, setRating] = useState(5);
  const [reviewText, setReviewText] = useState("");
  const pageSize = 10;

  const {
    data: article,
    isLoading,
    error,
  } = useQuery<Article>({
    queryKey: ["article", id],
    queryFn: () => getArticleDetails(id!),
  });

  const { data: feedbackData } = useQuery<{ items: Feedback[]; total: number }>(
    {
      queryKey: ["articleFeedbacks", id, feedbackPage],
      queryFn: () =>
        getArticleFeedbacks(id!, (feedbackPage - 1) * pageSize, pageSize),
      enabled: !!article,
    }
  );

  const totalFeedbackPages = Math.ceil((feedbackData?.total || 0) / pageSize);

  const feedbackMutation = useMutation({
    mutationFn: (data: { text: string; stars: number }) =>
      addArticleFeedback(id!, data.text, data.stars),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["articleFeedbacks", id] });
      setReviewText(""); // Reset form
      setRating(5);
    },
  });

  const handleSubmitFeedback = (e: React.FormEvent) => {
    e.preventDefault();
    feedbackMutation.mutate({
      text: reviewText,
      stars: rating,
    });
  };

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

        {/* Feedbacks Section */}
        <section className="mt-12">
          <h2 className="text-2xl font-bold mb-6">Reviews</h2>

          {/* Add feedback form */}
          <div className="bg-white rounded-lg shadow-md p-6 mb-8">
            <h3 className="text-lg font-semibold mb-4">Add Your Review</h3>
            <form onSubmit={handleSubmitFeedback}>
              <div className="mb-4">
                <label className="block text-gray-700 mb-2">Rating</label>
                <select
                  className="w-full border rounded-md px-3 py-2"
                  value={rating}
                  onChange={(e) => setRating(Number(e.target.value))}
                >
                  {[1, 2, 3, 4, 5].map((num) => (
                    <option key={num} value={num}>
                      {num} stars
                    </option>
                  ))}
                </select>
              </div>
              <div className="mb-4">
                <label className="block text-gray-700 mb-2">Your Review</label>
                <textarea
                  className="w-full border rounded-md px-3 py-2"
                  rows={4}
                  value={reviewText}
                  onChange={(e) => setReviewText(e.target.value)}
                />
              </div>
              <button
                type="submit"
                className="bg-indigo-600 text-white px-4 py-2 rounded-md hover:bg-indigo-700"
                disabled={feedbackMutation.isPending}
              >
                Submit Review
              </button>
            </form>
          </div>

          {/* Feedback list */}
          <div className="space-y-4">
            {feedbackData?.items.map((feedback) => (
              <div
                key={feedback.id}
                className="bg-white p-6 rounded-lg shadow-md"
              >
                <div className="flex items-center justify-between mb-2">
                  <span className="font-semibold">{feedback.author}</span>
                  <span className="text-yellow-500">
                    {feedback.stars}/5 stars
                  </span>
                </div>
                <p className="text-gray-600">{feedback.text}</p>
                <div className="text-sm text-gray-500 mt-2">
                  {new Date(feedback.createdAt).toLocaleDateString()}
                </div>
              </div>
            ))}
          </div>

          {totalFeedbackPages > 1 && (
            <Pagination
              currentPage={feedbackPage}
              totalPages={totalFeedbackPages}
              onPageChange={setFeedbackPage}
            />
          )}
        </section>
      </main>
    </div>
  );
}
