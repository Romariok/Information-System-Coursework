import { useState } from "react";
import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import { Link } from "react-router-dom";
import Navbar from "../components/Navbar";
import Pagination from "../components/Pagination";
import {
  getMusicians,
  searchMusiciansByName,
  subscribeToMusician,
  unsubscribeFromMusician,
  checkMusicianSubscribed,
  createMusician,
} from "../services/api";
import { Genre, TypeOfMusician } from "../services/types";

type SortOption = {
  field: "NAME" | "SUBSCRIBERS";
  direction: boolean;
};

// Add these arrays for the enum values
const GENRE_VALUES: Genre[] = [
  "BLUES",
  "ROCK",
  "POP",
  "JAZZ",
  "RAP",
  "METAL",
  "CLASSICAL",
  "REGGAE",
  "ELECTRONIC",
  "HIP_HOP",
];

const TYPE_OF_MUSICIAN_VALUES: TypeOfMusician[] = [
  "MUSICAL_PRODUCER",
  "GUITARIST",
  "DRUMMER",
  "BASSIST",
  "SINGER",
  "RAPPER",
  "KEYBOARDIST",
];

export default function Musicians() {
  const [page, setPage] = useState(1);
  const [searchQuery, setSearchQuery] = useState("");
  const [sort, setSort] = useState<SortOption>({
    field: "SUBSCRIBERS",
    direction: false,
  });
  const pageSize = 12;

  // Add subscription state
  const [subscriptions, setSubscriptions] = useState<{
    [key: number]: boolean;
  }>({});

  const [isCreateModalOpen, setIsCreateModalOpen] = useState(false);
  const [newMusicianName, setNewMusicianName] = useState("");
  const [selectedGenres, setSelectedGenres] = useState<Genre[]>([]);
  const [selectedTypes, setSelectedTypes] = useState<TypeOfMusician[]>([]);
  const queryClient = useQueryClient();

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

  // Add subscription check for each musician
  useQuery({
    queryKey: ["subscriptions", musiciansData?.items],
    queryFn: async () => {
      if (!musiciansData?.items) return;
      const checks = await Promise.all(
        musiciansData.items.map((musician) =>
          checkMusicianSubscribed(musician.id)
        )
      );
      const newSubscriptions = musiciansData.items.reduce(
        (acc, musician, index) => {
          acc[musician.id] = checks[index];
          return acc;
        },
        {} as { [key: number]: boolean }
      );
      setSubscriptions(newSubscriptions);
    },
    enabled: !!musiciansData?.items,
  });

  // Add subscription mutation
  const subscriptionMutation = useMutation({
    mutationFn: async ({
      musicianId,
      subscribed,
    }: {
      musicianId: number;
      subscribed: boolean;
    }) => {
      if (subscribed) {
        return await unsubscribeFromMusician(musicianId);
      } else {
        return await subscribeToMusician(musicianId);
      }
    },
    onSuccess: (_, { musicianId }) => {
      setSubscriptions((prev) => ({
        ...prev,
        [musicianId]: !prev[musicianId],
      }));
    },
  });

  const handleSubscribe = (musicianId: number) => {
    subscriptionMutation.mutate({
      musicianId,
      subscribed: subscriptions[musicianId],
    });
  };

  const hasMore = musiciansData?.items.length === pageSize;

  const handleSearch = (e: React.FormEvent<HTMLFormElement>) => {
    e.preventDefault();
    setPage(1);
  };

  const handleSortChange = (field: SortOption["field"]) => {
    setSort((prev) => ({
      field,
      direction: prev.field === field ? !prev.direction : true,
    }));
  };

  const createMusicianMutation = useMutation({
    mutationFn: (data: {
      name: string;
      genres: Genre[];
      typesOfMusician: TypeOfMusician[];
    }) => createMusician(data.name, data.genres, data.typesOfMusician),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["musicians"] });
      setIsCreateModalOpen(false);
      setNewMusicianName("");
      setSelectedGenres([]);
      setSelectedTypes([]);
    },
  });

  const handleCreateMusician = (e: React.FormEvent) => {
    e.preventDefault();
    if (newMusicianName.trim()) {
      createMusicianMutation.mutate({
        name: newMusicianName,
        genres: selectedGenres,
        typesOfMusician: selectedTypes,
      });
    }
  };

  if (error) return <div>Error loading musicians</div>;

  return (
    <div className="min-h-screen bg-gray-50">
      <Navbar />

      <main className="max-w-7xl mx-auto px-4 py-8">
        <div className="mb-8">
          <div className="flex justify-between items-center mb-6">
            <h1 className="text-3xl font-bold text-gray-900">Musicians</h1>
            <button
              onClick={() => setIsCreateModalOpen(true)}
              className="px-4 py-2 bg-indigo-600 text-white rounded-md hover:bg-indigo-700"
            >
              Add Musician
            </button>
          </div>

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
              onClick={() => handleSortChange("NAME")}
              className={`px-4 py-2 rounded-md ${
                sort.field === "NAME"
                  ? "bg-indigo-600 text-white"
                  : "bg-gray-200 text-gray-700"
              }`}
            >
              Name {sort.field === "NAME" && (sort.direction ? "↑" : "↓")}
            </button>
            <button
              onClick={() => handleSortChange("SUBSCRIBERS")}
              className={`px-4 py-2 rounded-md ${
                sort.field === "SUBSCRIBERS"
                  ? "bg-indigo-600 text-white"
                  : "bg-gray-200 text-gray-700"
              }`}
            >
              Subscribers{" "}
              {sort.field === "SUBSCRIBERS" && (sort.direction ? "↑" : "↓")}
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
                <Link
                  to={`/musician/${musician.id}`}
                  className="w-32 h-32 mb-4 hover:opacity-75 transition-opacity"
                >
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
                </Link>
                <h3 className="text-lg font-semibold text-center">
                  {musician.name}
                </h3>
                <p className="text-gray-600 mt-2">
                  {musician.subscribers} subscribers
                </p>
                <div className="flex gap-4 mt-4">
                  <Link
                    to={`/musician/${musician.id}`}
                    className="text-indigo-600 hover:text-indigo-800"
                  >
                    View Profile
                  </Link>
                  <button
                    onClick={() => handleSubscribe(musician.id)}
                    className={`px-4 py-1 rounded-full text-sm font-medium ${
                      subscriptions[musician.id]
                        ? "bg-gray-200 text-gray-700 hover:bg-gray-300"
                        : "bg-indigo-600 text-white hover:bg-indigo-700"
                    }`}
                    disabled={subscriptionMutation.isPending}
                  >
                    {subscriptions[musician.id] ? "Unsubscribe" : "Subscribe"}
                  </button>
                </div>
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

      {isCreateModalOpen && (
        <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
          <div className="bg-white p-6 rounded-lg w-full max-w-md">
            <h2 className="text-2xl font-bold mb-4">Create New Musician</h2>
            <form onSubmit={handleCreateMusician}>
              <div className="mb-4">
                <label className="block text-gray-700 mb-2">Name</label>
                <input
                  type="text"
                  value={newMusicianName}
                  onChange={(e) => setNewMusicianName(e.target.value)}
                  className="w-full p-2 border rounded-md"
                  required
                />
              </div>

              <div className="mb-4">
                <label className="block text-gray-700 mb-2">Genres</label>
                <div className="grid grid-cols-2 gap-2">
                  {GENRE_VALUES.map((genre) => (
                    <label key={genre} className="flex items-center space-x-2">
                      <input
                        type="checkbox"
                        checked={selectedGenres.includes(genre)}
                        onChange={(e) => {
                          if (e.target.checked) {
                            setSelectedGenres([...selectedGenres, genre]);
                          } else {
                            setSelectedGenres(
                              selectedGenres.filter((g) => g !== genre)
                            );
                          }
                        }}
                        className="rounded"
                      />
                      <span>{genre.toLowerCase().replace("_", " ")}</span>
                    </label>
                  ))}
                </div>
              </div>

              <div className="mb-4">
                <label className="block text-gray-700 mb-2">
                  Types of Musician
                </label>
                <div className="grid grid-cols-2 gap-2">
                  {TYPE_OF_MUSICIAN_VALUES.map((type) => (
                    <label key={type} className="flex items-center space-x-2">
                      <input
                        type="checkbox"
                        checked={selectedTypes.includes(type)}
                        onChange={(e) => {
                          if (e.target.checked) {
                            setSelectedTypes([...selectedTypes, type]);
                          } else {
                            setSelectedTypes(
                              selectedTypes.filter((t) => t !== type)
                            );
                          }
                        }}
                        className="rounded"
                      />
                      <span>{type.toLowerCase().replace("_", " ")}</span>
                    </label>
                  ))}
                </div>
              </div>

              <div className="flex justify-end gap-4">
                <button
                  type="button"
                  onClick={() => {
                    setIsCreateModalOpen(false);
                    setNewMusicianName("");
                    setSelectedGenres([]);
                    setSelectedTypes([]);
                  }}
                  className="px-4 py-2 text-gray-600 hover:text-gray-800"
                >
                  Cancel
                </button>
                <button
                  type="submit"
                  className="px-4 py-2 bg-indigo-600 text-white rounded-md hover:bg-indigo-700"
                  disabled={createMusicianMutation.isPending}
                >
                  {createMusicianMutation.isPending ? "Creating..." : "Create"}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
}
