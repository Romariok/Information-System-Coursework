import { useState } from "react";
import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import { Link } from "react-router-dom";
import Navbar from "../components/Navbar";
import { useAuth } from "../contexts/AuthContext";
import api, { getUserMusicians } from "../services/api";
import type {
  ProductSimple,
  Musician,
  Genre,
  TypeOfMusician,
} from "../services/types";

export default function UserInfo() {
  const { user } = useAuth();
  const queryClient = useQueryClient();
  const [activeTab, setActiveTab] = useState<"products" | "musicians">(
    "products"
  );
  const [isEditingGenres, setIsEditingGenres] = useState(false);
  const [isEditingTypes, setIsEditingTypes] = useState(false);
  const [editedGenres, setEditedGenres] = useState<Genre[]>([]);
  const [editedTypes, setEditedTypes] = useState<TypeOfMusician[]>([]);

  // Fetch user products
  const { data: likedProducts } = useQuery<ProductSimple[]>({
    queryKey: ["userProducts"],
    queryFn: async () => {
      const response = await api.get<ProductSimple[]>("/user/products");
      return response.data;
    },
  });

  // Fetch subscribed musicians
  const { data: subscribedMusicians } = useQuery<Musician[]>({
    queryKey: ["userMusicians"],
    queryFn: async () => {
      const response = await getUserMusicians();
      return response;
    },
  });

  // Fetch user genres
  const { data: userGenres } = useQuery<Genre[]>({
    queryKey: ["userGenres"],
    queryFn: async () => {
      const response = await api.get<Genre[]>("/user/genres");
      return response.data;
    },
  });

  // Fetch user types of musicians
  const { data: userTypes } = useQuery<TypeOfMusician[]>({
    queryKey: ["userTypes"],
    queryFn: async () => {
      const response = await api.get<TypeOfMusician[]>("/user/types");
      return response.data;
    },
  });

  // Add mutations for updating preferences
  const updateGenresMutation = useMutation({
    mutationFn: async (genres: Genre[]) => {
      await api.post("/user/genres", { genres });
    },
    onSuccess: () => {
      void queryClient.invalidateQueries({ queryKey: ["userGenres"] });
      setIsEditingGenres(false);
    },
  });

  const updateTypesMutation = useMutation({
    mutationFn: async (types: TypeOfMusician[]) => {
      await api.post("/user/types", { typesOfMusicians: types });
    },
    onSuccess: () => {
      void queryClient.invalidateQueries({ queryKey: ["userTypes"] });
      setIsEditingTypes(false);
    },
  });

  // Start editing handlers
  const handleStartEditingGenres = () => {
    setEditedGenres(userGenres || []);
    setIsEditingGenres(true);
  };

  const handleStartEditingTypes = () => {
    setEditedTypes(userTypes || []);
    setIsEditingTypes(true);
  };

  // Cancel editing handlers
  const handleCancelEditingGenres = () => {
    setIsEditingGenres(false);
    setEditedGenres([]);
  };

  const handleCancelEditingTypes = () => {
    setIsEditingTypes(false);
    setEditedTypes([]);
  };

  // Toggle handlers for editing
  const handleGenreToggle = (genre: Genre) => {
    setEditedGenres((current) =>
      current.includes(genre)
        ? current.filter((g) => g !== genre)
        : [...current, genre]
    );
  };

  const handleTypeToggle = (type: TypeOfMusician) => {
    setEditedTypes((current) =>
      current.includes(type)
        ? current.filter((t) => t !== type)
        : [...current, type]
    );
  };

  // Save handlers
  const handleSaveGenres = () => {
    updateGenresMutation.mutate(editedGenres);
  };

  const handleSaveTypes = () => {
    updateTypesMutation.mutate(editedTypes);
  };

  // All available options
  const allGenres: Genre[] = [
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

  const allTypes: TypeOfMusician[] = [
    "MUSICAL_PRODUCER",
    "GUITARIST",
    "DRUMMER",
    "BASSIST",
    "SINGER",
    "RAPPER",
    "KEYBOARDIST",
  ];

  return (
    <div className="min-h-screen bg-gray-50">
      <Navbar />

      <main className="max-w-7xl mx-auto px-4 py-8">
        <div className="bg-white rounded-lg shadow-md p-6 mb-6">
          <div className="space-y-6">
            <div className="flex justify-between items-start">
              <div>
                <h1 className="text-2xl font-bold text-gray-900 mb-2">
                  User Profile
                </h1>
                <div className="space-y-1">
                  <p className="text-gray-600">
                    Username:{" "}
                    <span className="font-medium">{user?.username}</span>
                  </p>
                  <p className="text-gray-600">
                    Role:{" "}
                    <span className="font-medium capitalize">
                      {user?.role?.replace("ROLE_", "").toLowerCase()}
                    </span>
                  </p>
                </div>
              </div>
              {user?.role === "ROLE_ADMIN" && (
                <Link
                  to="/admin"
                  className="px-4 py-2 bg-indigo-600 text-white rounded-md hover:bg-indigo-700 text-sm font-medium"
                >
                  Admin Panel
                </Link>
              )}
            </div>

            <div className="mt-6">
              <div className="flex flex-wrap gap-4">
                <div className="flex-1 min-w-[300px]">
                  <div className="flex justify-between items-center mb-2">
                    <h3 className="text-sm font-medium text-gray-500">
                      Favorite Genres
                    </h3>
                    {!isEditingGenres ? (
                      <button
                        onClick={handleStartEditingGenres}
                        className="text-xs text-indigo-600 hover:text-indigo-800"
                      >
                        Edit
                      </button>
                    ) : (
                      <div className="flex gap-2">
                        <button
                          onClick={handleSaveGenres}
                          className="text-xs text-green-600 hover:text-green-800"
                          disabled={updateGenresMutation.isPending}
                        >
                          Save
                        </button>
                        <button
                          onClick={handleCancelEditingGenres}
                          className="text-xs text-red-600 hover:text-red-800"
                        >
                          Cancel
                        </button>
                      </div>
                    )}
                  </div>
                  <div className="flex flex-wrap gap-2">
                    {isEditingGenres
                      ? allGenres.map((genre) => (
                          <button
                            key={genre}
                            onClick={() => handleGenreToggle(genre)}
                            className={`px-2 py-1 rounded-full text-xs ${
                              editedGenres.includes(genre)
                                ? "bg-indigo-600 text-white"
                                : "bg-gray-100 text-gray-800 hover:bg-gray-200"
                            }`}
                          >
                            {genre.toLowerCase().replace("_", " ")}
                          </button>
                        ))
                      : userGenres?.map((genre) => (
                          <span
                            key={genre}
                            className="px-2 py-1 bg-indigo-100 text-indigo-800 rounded-full text-xs"
                          >
                            {genre.toLowerCase().replace("_", " ")}
                          </span>
                        ))}
                  </div>
                </div>
                <div className="flex-1 min-w-[300px]">
                  <div className="flex justify-between items-center mb-2">
                    <h3 className="text-sm font-medium text-gray-500">
                      Musician Types
                    </h3>
                    {!isEditingTypes ? (
                      <button
                        onClick={handleStartEditingTypes}
                        className="text-xs text-indigo-600 hover:text-indigo-800"
                      >
                        Edit
                      </button>
                    ) : (
                      <div className="flex gap-2">
                        <button
                          onClick={handleSaveTypes}
                          className="text-xs text-green-600 hover:text-green-800"
                          disabled={updateTypesMutation.isPending}
                        >
                          Save
                        </button>
                        <button
                          onClick={handleCancelEditingTypes}
                          className="text-xs text-red-600 hover:text-red-800"
                        >
                          Cancel
                        </button>
                      </div>
                    )}
                  </div>
                  <div className="flex flex-wrap gap-2">
                    {isEditingTypes
                      ? allTypes.map((type) => (
                          <button
                            key={type}
                            onClick={() => handleTypeToggle(type)}
                            className={`px-2 py-1 rounded-full text-xs ${
                              editedTypes.includes(type)
                                ? "bg-green-600 text-white"
                                : "bg-gray-100 text-gray-800 hover:bg-gray-200"
                            }`}
                          >
                            {type.toLowerCase().replace("_", " ")}
                          </button>
                        ))
                      : userTypes?.map((type) => (
                          <span
                            key={type}
                            className="px-2 py-1 bg-green-100 text-green-800 rounded-full text-xs"
                          >
                            {type.toLowerCase().replace("_", " ")}
                          </span>
                        ))}
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>

        {/* Tabs */}
        <div className="bg-white rounded-lg shadow-md">
          <div className="border-b border-gray-200">
            <nav className="flex -mb-px">
              <button
                onClick={() => setActiveTab("products")}
                className={`py-4 px-6 ${
                  activeTab === "products"
                    ? "border-b-2 border-indigo-500 text-indigo-600"
                    : "text-gray-500 hover:text-gray-700"
                }`}
              >
                Liked Products
              </button>
              <button
                onClick={() => setActiveTab("musicians")}
                className={`py-4 px-6 ${
                  activeTab === "musicians"
                    ? "border-b-2 border-indigo-500 text-indigo-600"
                    : "text-gray-500 hover:text-gray-700"
                }`}
              >
                Subscribed Musicians
              </button>
            </nav>
          </div>

          <div className="p-6">
            {activeTab === "products" && (
              <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
                {likedProducts?.map((product) => (
                  <div
                    key={product.id}
                    className="bg-white p-6 rounded-lg shadow-md"
                  >
                    <Link to={`/product/${product.id}`}>
                      <div className="aspect-w-16 aspect-h-9 mb-4">
                        <img
                          src={
                            [
                              "https://images.equipboard.com/uploads/item/image/16008/gibson-les-paul-classic-electric-guitar-m.webp?v=1734091576",
                              "https://images.equipboard.com/uploads/item/image/17684/roland-g-707-m.webp?v=1734005219",
                              "https://images.equipboard.com/uploads/item/image/9259/yamaha-hs8-powered-studio-monitor-m.webp?v=1734264173",
                              "https://images.equipboard.com/uploads/item/image/17369/dave-smith-instruments-sequential-prophet-6-m.webp?v=1732782610",
                            ][product.id % 4]
                          }
                          alt={product.name}
                          className="w-full h-full object-cover rounded-md"
                        />
                      </div>
                      <h3 className="text-lg font-semibold mb-2">
                        {product.name}
                      </h3>
                    </Link>
                  </div>
                ))}
              </div>
            )}

            {activeTab === "musicians" && (
              <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
                {subscribedMusicians?.map((musician) => (
                  <div
                    key={musician.id}
                    className="bg-white p-6 rounded-lg shadow-md flex flex-col items-center"
                  >
                    <div className="w-32 h-32 mb-4">
                      <Link to={`/musician/${musician.id}`}>
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
          </div>
        </div>
      </main>
    </div>
  );
}
