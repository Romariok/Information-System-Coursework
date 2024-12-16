import { useState } from "react";
import { useQuery } from "@tanstack/react-query";
import { Link } from "react-router-dom";
import Navbar from "../components/Navbar";
import Pagination from "../components/Pagination";
import { getMusicians, searchMusiciansByName } from "../services/api";

type SortOption = {
  field: "name" | "subscribers";
  direction: "asc" | "desc";
};

export default function Musicians() {
  const [page, setPage] = useState(1);
  const [searchQuery, setSearchQuery] = useState("");
  const [sort, setSort] = useState<SortOption>({
    field: "subscribers",
    direction: "desc",
  });
  const pageSize = 12;

  const {
    data: musiciansData,
    isLoading,
    error,
  } = useQuery({
    queryKey: ["musicians", page, searchQuery, sort],
    queryFn: async () => {
      const from = (page - 1) * pageSize;
      if (searchQuery.trim()) {
        return await searchMusiciansByName(searchQuery, from, pageSize);
      }
      return await getMusicians(from, pageSize, sort);
    },
  });

  const hasMore = musiciansData?.items.length === pageSize;

  const handleSearch = (e: React.FormEvent<HTMLFormElement>) => {
    e.preventDefault();
    setPage(1);
  };

  const handleSortChange = (field: SortOption["field"]) => {
    setSort((prev) => ({
      field,
      direction:
        prev.field === field && prev.direction === "asc" ? "desc" : "asc",
    }));
  };

  if (error) return <div>Error loading musicians</div>;

  return (
    <div className="min-h-screen bg-gray-50">
      <Navbar />

      <main className="max-w-7xl mx-auto px-4 py-8">
        <div className="mb-8">
          <h1 className="text-3xl font-bold text-gray-900 mb-4">Musicians</h1>

          {/* Search Form */}
          <form onSubmit={handleSearch} className="mb-6">
            <div className="flex gap-4">
              <input
                type="text"
                value={searchQuery}
                onChange={(e) => setSearchQuery(e.target.value)}
                placeholder="Search musicians by name..."
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
              onClick={() => handleSortChange("name")}
              className={`px-4 py-2 rounded-md ${
                sort.field === "name"
                  ? "bg-indigo-600 text-white"
                  : "bg-gray-200 text-gray-700"
              }`}
            >
              Name{" "}
              {sort.field === "name" && (sort.direction === "asc" ? "↑" : "↓")}
            </button>
            <button
              onClick={() => handleSortChange("subscribers")}
              className={`px-4 py-2 rounded-md ${
                sort.field === "subscribers"
                  ? "bg-indigo-600 text-white"
                  : "bg-gray-200 text-gray-700"
              }`}
            >
              Subscribers{" "}
              {sort.field === "subscribers" &&
                (sort.direction === "asc" ? "↑" : "↓")}
            </button>
          </div>
        </div>

        {/* Musicians Grid */}
        {isLoading ? (
          <div className="text-center py-8">Loading musicians...</div>
        ) : (
          <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
            {musiciansData?.items.map((musician) => (
              <div
                key={musician.id}
                className="bg-white p-6 rounded-lg shadow-md flex flex-col items-center"
              >
                <div className="w-32 h-32 mb-4">
                  <img
                    src={
                      [
                        "https://tntmusic.ru/media/content/article@2x/2020-12-25_08-09-59__950100bc-4688-11eb-be12-87ef0634b7d4.jpg",
                        "https://i1.sndcdn.com/artworks-QSYcavKwyzW8LwyR-jAEK0g-t500x500.jpg",
                        "https://the-flow.ru/uploads/images/origin/04/15/95/60/74/8161911.jpg",
                        "https://avatars.mds.yandex.net/get-mpic/5304425/img_id6170984171594674671.jpeg/orig",
                      ][musician.id % 4]
                    }
                    alt={musician.name}
                    className="w-full h-full object-cover rounded-full"
                  />
                </div>
                <h3 className="text-lg font-semibold text-center">
                  {musician.name}
                </h3>
                <p className="text-gray-600 mt-2">
                  {musician.subscribers} subscribers
                </p>
                <Link
                  to={`/musician/${musician.id}`}
                  className="text-indigo-600 hover:text-indigo-800 mt-4"
                >
                  View Profile
                </Link>
              </div>
            ))}
          </div>
        )}

        {/* Pagination */}
        {(musiciansData?.items.length ?? 0) > 0 && (
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
