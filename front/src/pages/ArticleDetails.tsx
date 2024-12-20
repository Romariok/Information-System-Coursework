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
import StarRating from "../components/StarRating";
import CollapsibleReviewForm from "../components/CollapsibleReviewForm";

export default function ArticleDetails() {
  const { id } = useParams();
  const queryClient = useQueryClient();
  const [feedbackPage, setFeedbackPage] = useState(1);
  const pageSize = 10;

  const {
    data: article,
    isLoading,
    error,
  } = useQuery<Article>({
    queryKey: ["article", id],
    queryFn: () => getArticleDetails(id!),
  });

  const { data: feedbackData } = useQuery<{ items: Feedback[] }>({
    queryKey: ["articleFeedbacks", id, feedbackPage],
    queryFn: () =>
      getArticleFeedbacks(id!, (feedbackPage - 1) * pageSize, pageSize),
    enabled: !!article,
  });

  const feedbackMutation = useMutation({
    mutationFn: (data: { text: string; stars: number }) =>
      addArticleFeedback(id!, data.text, data.stars),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["articleFeedbacks", id] });
    },
  });

  if (error) {
    return <Navigate to="/error" />;
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
            <span className="font-medium">By {article.author.username}</span>
            <span className="mx-2">•</span>
            <time dateTime={article.createdAt}>
              {new Date(article.createdAt).toLocaleDateString()}
            </time>
          </div>

          <div className="prose prose-lg max-w-none">
            {article.htmlContent ? (
              <div dangerouslySetInnerHTML={{ __html: article.htmlContent }} />
            ) : (
              article.text.split("\n").map((paragraph, index) => (
                <p key={index} className="mb-4">
                  {paragraph}
                </p>
              ))
            )}
          </div>
        </article>

        {/* Feedbacks Section */}
        <section className="mt-12">
          <h2 className="text-2xl font-bold mb-6">Reviews</h2>

          {/* Add feedback form */}
          <CollapsibleReviewForm
            onSubmit={({ text, stars }) => {
              feedbackMutation.mutate({ text, stars });
            }}
            isSubmitting={feedbackMutation.isPending}
          />

          {/* Feedback list */}
          <div className="space-y-4">
            {feedbackData?.items?.map((feedback) => (
              <div
                key={feedback.id}
                className="bg-white p-6 rounded-lg shadow-md"
              >
                <div className="flex items-center justify-between mb-2">
                  <span className="font-semibold">
                    {feedback.author.username}
                  </span>
                  <StarRating
                    rating={feedback.stars}
                    onRatingChange={() => {}}
                    size="sm"
                  />
                </div>
                <p className="text-gray-600">{feedback.text}</p>
                <div className="text-sm text-gray-500 mt-2">
                  {new Date(feedback.createdAt).toLocaleDateString()}
                </div>
              </div>
            ))}
          </div>

          {feedbackData?.items?.length !== 0 && (
            <Pagination
              currentPage={feedbackPage}
              hasMore={feedbackData?.items?.length === pageSize}
              onPageChange={setFeedbackPage}
            />
          )}
        </section>
      </main>
    </div>
  );
}
