import { useState } from "react";
import { useParams, Navigate } from "react-router-dom";
import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import Navbar from "../components/Navbar";
import Pagination from "../components/Pagination";
import {
  getTopicPosts,
  createForumPost,
  closeForumTopic,
  isTopicOwner,
} from "../services/api";

export default function ForumTopic() {
  const { id } = useParams();
  const [page, setPage] = useState(1);
  const [newPost, setNewPost] = useState("");
  const pageSize = 10;
  const queryClient = useQueryClient();

  const {
    data: postsData,
    isLoading,
    error,
  } = useQuery({
    queryKey: ["forumPosts", id, page],
    queryFn: () => getTopicPosts(Number(id), (page - 1) * pageSize, pageSize),
  });

  const createPostMutation = useMutation({
    mutationFn: (content: string) => createForumPost(Number(id), content),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["forumPosts", id] });
      setNewPost("");
    },
  });

  const closeTopicMutation = useMutation({
    mutationFn: () => closeForumTopic(Number(id)),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["forumPosts", id] });
      queryClient.invalidateQueries({ queryKey: ["forumTopics"] });
    },
  });

  const handleCreatePost = (e: React.FormEvent) => {
    e.preventDefault();
    if (newPost.trim()) {
      createPostMutation.mutate(newPost);
    }
  };

  const handleCloseTopic = () => {
    if (
      window.confirm(
        "Are you sure you want to close this topic? No new messages will be allowed."
      )
    ) {
      closeTopicMutation.mutate();
    }
  };

  const { data: isOwner } = useQuery({
    queryKey: ["topicOwner", id],
    queryFn: () => isTopicOwner(Number(id)),
  });

  if (error) return <Navigate to="/error" />;

  const hasMore = postsData?.items.length === pageSize;
  const isClosed = postsData?.topic?.isClosed;

  return (
    <div className="min-h-screen bg-gray-50">
      <Navbar />

      <main className="max-w-4xl mx-auto px-4 py-8">
        <div className="flex justify-between items-center mb-8">
          <h1 className="text-3xl font-bold text-gray-900">Forum Topic</h1>
          {isOwner && (
            <button
              onClick={handleCloseTopic}
              className="px-4 py-2 bg-red-600 text-white rounded-md hover:bg-red-700"
              disabled={isClosed}
            >
              {isClosed ? "Topic Closed" : "Close Topic"}
            </button>
          )}
        </div>

        {/* Posts List */}
        {isLoading ? (
          <div className="text-center py-8">Loading posts...</div>
        ) : (
          <div className="space-y-6">
            {postsData?.items.map((post) => (
              <div key={post.id} className="bg-white p-6 rounded-lg shadow-md">
                <div className="flex justify-between items-start mb-4">
                  <span className="font-semibold text-gray-900">
                    {post.author.username}
                  </span>
                  <span className="text-sm text-gray-500">
                    {new Date(post.createdAt).toLocaleString()}
                  </span>
                </div>
                <p className="text-gray-700 whitespace-pre-wrap">
                  {post.content}
                </p>
              </div>
            ))}
          </div>
        )}

        {/* Pagination */}
        {(postsData?.items.length ?? 0) > 0 && (
          <div className="mt-8">
            <Pagination
              currentPage={page}
              hasMore={hasMore}
              onPageChange={setPage}
            />
          </div>
        )}

        {/* New Post Form - Only show if topic is not closed */}
        {!isClosed && (
          <form onSubmit={handleCreatePost} className="mt-8">
            <div className="mb-4">
              <label className="block text-gray-700 mb-2">Write a reply</label>
              <textarea
                value={newPost}
                onChange={(e) => setNewPost(e.target.value)}
                className="w-full p-4 border rounded-md h-32 resize-none"
                placeholder="Write your reply here..."
                required
              />
            </div>
            <div className="flex justify-end">
              <button
                type="submit"
                className="px-6 py-2 bg-indigo-600 text-white rounded-md hover:bg-indigo-700"
                disabled={createPostMutation.isPending}
              >
                Post Reply
              </button>
            </div>
          </form>
        )}

        {/* Show message if topic is closed */}
        {isClosed && (
          <div className="mt-8 p-4 bg-gray-100 text-gray-700 rounded-md text-center">
            This topic is closed. No new replies can be added.
          </div>
        )}
      </main>
    </div>
  );
}
