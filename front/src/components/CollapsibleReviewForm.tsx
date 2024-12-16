import { useState } from "react";
import StarRating from "./StarRating";

interface CollapsibleReviewFormProps {
  onSubmit: (data: { text: string; stars: number }) => void;
  isSubmitting: boolean;
}

export default function CollapsibleReviewForm({
  onSubmit,
  isSubmitting,
}: CollapsibleReviewFormProps) {
  const [isOpen, setIsOpen] = useState(false);
  const [rating, setRating] = useState(5);
  const [reviewText, setReviewText] = useState("");

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    onSubmit({ text: reviewText, stars: rating });
    setIsOpen(false);
    setReviewText("");
    setRating(5);
  };

  return (
    <div className="bg-white rounded-lg shadow-md overflow-hidden">
      <button
        onClick={() => setIsOpen(!isOpen)}
        className="w-full px-6 py-4 flex items-center justify-between text-left bg-gradient-to-r from-indigo-500 to-purple-600 text-white hover:from-indigo-600 hover:to-purple-700 transition-all"
      >
        <span className="text-lg font-semibold">Write a Review</span>
        <svg
          className={`w-5 h-5 transform transition-transform ${
            isOpen ? "rotate-180" : ""
          }`}
          fill="none"
          viewBox="0 0 24 24"
          stroke="currentColor"
        >
          <path
            strokeLinecap="round"
            strokeLinejoin="round"
            strokeWidth={2}
            d="M19 9l-7 7-7-7"
          />
        </svg>
      </button>

      {isOpen && (
        <div className="p-6 animate-slideDown">
          <form onSubmit={handleSubmit}>
            <div className="mb-4">
              <label className="block text-gray-700 mb-2">Rating</label>
              <StarRating
                rating={rating}
                onRatingChange={setRating}
                size="lg"
              />
            </div>
            <div className="mb-4">
              <label className="block text-gray-700 mb-2">Your Review</label>
              <textarea
                className="w-full border rounded-md px-3 py-2 focus:ring-2 focus:ring-indigo-500 focus:border-transparent"
                rows={4}
                value={reviewText}
                onChange={(e) => setReviewText(e.target.value)}
                placeholder="Share your thoughts about this product..."
              />
            </div>
            <div className="flex justify-end">
              <button
                type="button"
                onClick={() => setIsOpen(false)}
                className="mr-3 px-4 py-2 text-gray-600 hover:text-gray-800"
              >
                Cancel
              </button>
              <button
                type="submit"
                className="bg-indigo-600 text-white px-6 py-2 rounded-md hover:bg-indigo-700 transition-colors disabled:opacity-50"
                disabled={isSubmitting || !reviewText.trim()}
              >
                Submit Review
              </button>
            </div>
          </form>
        </div>
      )}
    </div>
  );
}
