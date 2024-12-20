import { useState } from "react";
import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import { Link } from "react-router-dom";
import Navbar from "../components/Navbar";
import Pagination from "../components/Pagination";
import { getForumTopics, createForumTopic } from "../services/api";

export default function Forum() {
  const [page, setPage] = useState(1);
  const [isCreateModalOpen, setIsCreateModalOpen] = useState(false);
  const [newTopic, setNewTopic] = useState({ title: "", description: "" });
  const pageSize = 10;
  const queryClient = useQueryClient();

  const {
    data: topicsData,
    isLoading,
    error,
  } = useQuery({
    queryKey: ["forumTopics", page],
    queryFn: () => getForumTopics((page - 1) * pageSize, pageSize),
  });

  const createTopicMutation = useMutation({
    mutationFn: (data: { title: string; description: string }) =>
      createForumTopic(data.title, data.description),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["forumTopics"] });
      setIsCreateModalOpen(false);
      setNewTopic({ title: "", description: "" });
    },
  });

  const handleCreateTopic = (e: React.FormEvent) => {
    e.preventDefault();
    createTopicMutation.mutate(newTopic);
  };

  const hasMore = topicsData?.items.length === pageSize;

  return (
    <div className="min-h-screen bg-gray-50">
      <Navbar />

      <main className="max-w-7xl mx-auto px-4 py-8">
        <div className="flex justify-between items-center mb-8">
          <h1 className="text-3xl font-bold text-gray-900">Forum</h1>
          <button
            onClick={() => setIsCreateModalOpen(true)}
            className="px-4 py-2 bg-indigo-600 text-white rounded-md hover:bg-indigo-700"
          >
            Create New Topic
          </button>
        </div>

        {isLoading ? (
          <div className="text-center py-8">Loading topics...</div>
        ) : error ? (
          <div className="text-red-600">Error loading topics</div>
        ) : (
          <div className="space-y-4">
            {topicsData?.items.map((topic) => (
              <div
                key={topic.id}
                className="bg-white p-6 rounded-lg shadow-md hover:shadow-lg transition-shadow"
              >
                <div className="flex justify-between items-start">
                  <div>
                    {topic.closed ? (
                      <div>
                        <span className="text-xl font-semibold text-gray-600">
                          {topic.title}
                        </span>
                        <span className="ml-2 px-3 py-1 bg-red-100 text-red-800 rounded-full text-sm">
                          Closed
                        </span>
                      </div>
                    ) : (
                      <Link
                        to={`/forum/topic/${topic.id}`}
                        className="text-xl font-semibold text-indigo-600 hover:text-indigo-800"
                      >
                        {topic.title}
                      </Link>
                    )}
                    <p className="text-gray-600 mt-2">{topic.description}</p>
                  </div>
                </div>
                <div className="mt-4 text-sm text-gray-500">
                  By {topic.author.username} •{" "}
                  {new Date(topic.createdAt).toLocaleDateString()}
                </div>
              </div>
            ))}
          </div>
        )}

        {(topicsData?.items.length ?? 0) > 0 && (
          <div className="mt-8">
            <Pagination
              currentPage={page}
              hasMore={hasMore}
              onPageChange={setPage}
            />
          </div>
        )}

        {/* Create Topic Modal */}
        {isCreateModalOpen && (
          <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center">
            <div className="bg-white p-6 rounded-lg w-full max-w-lg">
              <h2 className="text-2xl font-bold mb-4">Create New Topic</h2>
              <form onSubmit={handleCreateTopic}>
                <div className="mb-4">
                  <label className="block text-gray-700 mb-2">Title</label>
                  <input
                    type="text"
                    value={newTopic.title}
                    onChange={(e) =>
                      setNewTopic((prev) => ({
                        ...prev,
                        title: e.target.value,
                      }))
                    }
                    className="w-full p-2 border rounded-md"
                    required
                  />
                </div>
                <div className="mb-4">
                  <label className="block text-gray-700 mb-2">
                    Description
                  </label>
                  <textarea
                    value={newTopic.description}
                    onChange={(e) =>
                      setNewTopic((prev) => ({
                        ...prev,
                        description: e.target.value,
                      }))
                    }
                    className="w-full p-2 border rounded-md h-32"
                    required
                  />
                </div>
                <div className="flex justify-end gap-4">
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
                    disabled={createTopicMutation.isPending}
                  >
                    Create Topic
                  </button>
                </div>
              </form>
            </div>
          </div>
        )}
      </main>
    </div>
  );
}
