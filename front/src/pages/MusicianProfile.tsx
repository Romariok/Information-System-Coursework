import { useParams } from "react-router-dom";
import { useQuery } from "@tanstack/react-query";
import { Link } from "react-router-dom";
import Navbar from "../components/Navbar";
import {
  getMusicianInfo,
  checkMusicianSubscribed,
  subscribeToMusician,
  unsubscribeFromMusician,
  addProductToMusician,
  searchProducts,
} from "../services/api";
import { useMutation, useQueryClient } from "@tanstack/react-query";
import { Genre, TypeOfMusician, ProductSimple } from "../services/types.ts";
import { useState } from "react";
export default function MusicianProfile() {
  const { id } = useParams<{ id: string }>();
  const queryClient = useQueryClient();
  const { data: musician, isLoading } = useQuery({
    queryKey: ["musician", id],
    queryFn: () => getMusicianInfo(id!),
    enabled: !!id,
  });
  const { data: isSubscribed } = useQuery({
    queryKey: ["musicianSubscription", id],
    queryFn: () => checkMusicianSubscribed(Number(id)),
    enabled: !!id,
  });
  const subscriptionMutation = useMutation({
    mutationFn: async () => {
      if (isSubscribed) {
        await unsubscribeFromMusician(Number(id));
      } else {
        await subscribeToMusician(Number(id));
      }
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["musicianSubscription", id] });
      queryClient.invalidateQueries({ queryKey: ["musician", id] });
    },
  });
  const [isAddProductModalOpen, setIsAddProductModalOpen] = useState(false);
  const [newProductId, setNewProductId] = useState("");
  const [searchQuery, setSearchQuery] = useState("");
  const [searchResults, setSearchResults] = useState<ProductSimple[]>([]);
  const [selectedProduct, setSelectedProduct] = useState<ProductSimple | null>(
    null
  );
  const [isSearching, setIsSearching] = useState(false);
  const addProductMutation = useMutation({
    mutationFn: () =>
      addProductToMusician(musician?.name || "", Number(newProductId)),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["musician", id] });
      setIsAddProductModalOpen(false);
      setNewProductId("");
    },
  });
  const handleSearch = async (query: string) => {
    setSearchQuery(query);
    if (query.trim().length >= 2) {
      setIsSearching(true);
      try {
        const results = await searchProducts(query);
        setSearchResults(results);
      } catch (error) {
        console.error("Error searching products:", error);
      }
      setIsSearching(false);
    } else {
      setSearchResults([]);
    }
  };
  const handleAddProduct = (e: React.FormEvent) => {
    e.preventDefault();
    if (selectedProduct) {
      addProductMutation.mutate();
      setIsAddProductModalOpen(false);
      setSearchQuery("");
      setSearchResults([]);
      setSelectedProduct(null);
    }
  };
  if (isLoading) {
    return (
      <div className="min-h-screen bg-gray-50">
        <Navbar />
        <main className="max-w-7xl mx-auto px-4 py-8">
          <div className="text-center">Loading musician profile...</div>
        </main>
      </div>
    );
  }
  return (
    <div className="min-h-screen bg-gray-50">
      <Navbar />
      <main className="max-w-7xl mx-auto px-4 py-8">
        <div className="bg-white rounded-lg shadow-md p-6 mb-6">
          <div className="flex items-start justify-between space-x-8">
            <div className="flex-1">
              <h1 className="text-3xl font-bold text-gray-900 mb-2">
                {musician?.name}
              </h1>
              <p className="text-gray-600 mb-4">
                {musician?.subscribers} subscribers
              </p>
              <div className="flex gap-4">
                <button
                  onClick={() => subscriptionMutation.mutate()}
                  className={`px-4 py-2 rounded-full text-sm font-medium ${
                    isSubscribed
                      ? "bg-gray-200 text-gray-700 hover:bg-gray-300"
                      : "bg-indigo-600 text-white hover:bg-indigo-700"
                  }`}
                  disabled={subscriptionMutation.isPending}
                >
                  {isSubscribed ? "Unsubscribe" : "Subscribe"}
                </button>
                <button
                  onClick={() => setIsAddProductModalOpen(true)}
                  className="px-4 py-2 bg-green-600 text-white rounded-full text-sm font-medium hover:bg-green-700"
                >
                  Add Product
                </button>
              </div>
            </div>
            <div className="w-64 h-64">
              <img
                src={
                  [
                    "https://tntmusic.ru/media/content/article@2x/2020-12-25_08-09-59__950100bc-4688-11eb-be12-87ef0634b7d4.jpg",
                    "https://i1.sndcdn.com/artworks-QSYcavKwyzW8LwyR-jAEK0g-t500x500.jpg",
                    "https://the-flow.ru/uploads/images/origin/04/15/95/60/74/8161911.jpg",
                    "https://avatars.mds.yandex.net/get-mpic/5304425/img_id6170984171594674671.jpeg/orig",
                  ][Number(id) % 4]
                }
                alt={musician?.name}
                className="w-full h-full object-cover rounded-full shadow-lg"
              />
            </div>
          </div>
          <div className="mt-2 space-y-4">
            <div>
              <h3 className="text-sm font-medium text-gray-500 mb-2">Genres</h3>
              <div className="flex flex-wrap gap-2">
                {musician?.genres?.map((genre: Genre) => (
                  <span
                    key={genre}
                    className="px-2 py-1 bg-indigo-100 text-indigo-800 rounded-full text-xs"
                  >
                    {genre.toLowerCase().replace("_", " ")}
                  </span>
                ))}
              </div>
            </div>
            <div>
              <h3 className="text-sm font-medium text-gray-500 mb-2">
                Musician Types
              </h3>
              <div className="flex flex-wrap gap-2">
                {musician?.typesOfMusicians?.map((type: TypeOfMusician) => (
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
        <div className="bg-white rounded-lg shadow-md p-6">
          <h2 className="text-xl font-bold mb-4">Products</h2>
          <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
            {musician?.products?.map((product: ProductSimple) => (
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
                  <h3 className="text-lg font-semibold mb-2">{product.name}</h3>
                </Link>
              </div>
            ))}
          </div>
        </div>
        {isAddProductModalOpen && (
          <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
            <div className="bg-white p-6 rounded-lg w-full max-w-md">
              <h2 className="text-2xl font-bold mb-4">
                Add Product to {musician?.name}
              </h2>
              <form onSubmit={handleAddProduct}>
                <div className="mb-4">
                  <label className="block text-gray-700 mb-2">
                    Search Product
                  </label>
                  <input
                    type="text"
                    value={searchQuery}
                    onChange={(e) => handleSearch(e.target.value)}
                    className="w-full p-2 border rounded-md"
                    placeholder="Type to search products..."
                  />

                  {/* Search Results */}
                  {searchQuery.trim().length >= 2 && !selectedProduct && (
                    <div className="mt-2 max-h-60 overflow-y-auto border rounded-md">
                      {isSearching ? (
                        <div className="p-2 text-gray-500">Searching...</div>
                      ) : searchResults.length > 0 ? (
                        searchResults.map((product) => (
                          <div
                            key={product.id}
                            onClick={() => {
                              setSelectedProduct(product);
                              setNewProductId(product.id.toString());
                              setSearchQuery(product.name);
                              setSearchResults([]);
                            }}
                            className={`p-2 flex items-center gap-3 hover:bg-gray-100 cursor-pointer ${
                              selectedProduct?.id === product.id
                                ? "bg-indigo-50"
                                : ""
                            }`}
                          >
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
                              className="w-12 h-12 object-cover rounded"
                            />
                            <div>
                              <div className="font-medium">{product.name}</div>
                              <div className="text-sm text-gray-500">
                                {product.typeOfProduct}
                              </div>
                            </div>
                          </div>
                        ))
                      ) : (
                        <div className="p-2 text-gray-500">
                          No products found
                        </div>
                      )}
                    </div>
                  )}
                </div>

                <div className="flex justify-end gap-4">
                  <button
                    type="button"
                    onClick={() => {
                      setIsAddProductModalOpen(false);
                      setSearchQuery("");
                      setSearchResults([]);
                      setSelectedProduct(null);
                    }}
                    className="px-4 py-2 text-gray-600 hover:text-gray-800"
                  >
                    Cancel
                  </button>
                  <button
                    type="submit"
                    className="px-4 py-2 bg-green-600 text-white rounded-md hover:bg-green-700"
                    disabled={addProductMutation.isPending || !selectedProduct}
                  >
                    {addProductMutation.isPending ? "Adding..." : "Add Product"}
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
