import { useState, useEffect } from "react";
import { useQuery, useMutation } from "@tanstack/react-query";
import { Link } from "react-router-dom";
import Navbar from "../components/Navbar";
import {
  getTopProducts,
  getTopArticles,
  getTopMusicians,
  likeProduct,
  unlikeProduct,
  subscribeToMusician,
  unsubscribeFromMusician,
} from "../services/api";
import { Article, Musician, Product } from "../services/types";
import api from "../services/api";
import StarRating from "../components/StarRating";

const formatProductType = (type: string) => {
  const typeMap: { [key: string]: string } = {
    PEDALS_AND_EFFECTS: "Pedals & Effects",
    ELECTRIC_GUITAR: "Electric Guitar",
    STUDIO_RECORDING_GEAR: "Studio Recording Gear",
    KEYS_AND_MIDI: "Keys & MIDI",
    AMPLIFIER: "Amplifier",
    DRUMMS_AND_PERCUSSION: "Drums & Percussion",
    BASS_GUITAR: "Bass Guitar",
    ACOUSTIC_GUITAR: "Acoustic Guitar",
    SOFTWARE_AND_ACCESSORIES: "Software & Accessories",
  };
  return typeMap[type] || type;
};

export default function Home() {
  const [likedProducts, setLikedProducts] = useState<{
    [key: number]: boolean;
  }>({});

  const [subscribedMusicians, setSubscribedMusicians] = useState<{
    [key: number]: boolean;
  }>({});

  const { data: topProducts, isLoading: isLoadingProducts } = useQuery<
    Product[]
  >({
    queryKey: ["topProducts"],
    queryFn: getTopProducts,
  });

  const { data: topArticles, isLoading: isLoadingArticles } = useQuery<
    Article[]
  >({
    queryKey: ["topArticles"],
    queryFn: getTopArticles,
  });

  const { data: topMusicians, isLoading: isLoadingMusicians } = useQuery<
    Musician[]
  >({
    queryKey: ["topMusicians"],
    queryFn: getTopMusicians,
  });

  const { data: userProducts } = useQuery<Product[]>({
    queryKey: ["userProducts"],
    queryFn: async () => {
      const response = await api.get("/user/products");
      return response.data;
    },
  });

  useEffect(() => {
    if (userProducts) {
      const likedMap = userProducts.reduce(
        (acc: { [key: number]: boolean }, product: Product) => {
          acc[product.id] = true;
          return acc;
        },
        {}
      );
      setLikedProducts(likedMap);
    }
  }, [userProducts]);

  const likeMutation = useMutation<boolean, Error, number>({
    mutationFn: async (productId: number) => {
      if (likedProducts[productId]) {
        return await unlikeProduct(productId.toString());
      } else {
        return await likeProduct(productId.toString());
      }
    },
    onSuccess: (_, productId) => {
      setLikedProducts((prev) => ({
        ...prev,
        [productId]: !prev[productId],
      }));
    },
  });

  const subscribeMutation = useMutation({
    mutationFn: async (musicianId: number) => {
      if (subscribedMusicians[musicianId]) {
        return await unsubscribeFromMusician(musicianId);
      } else {
        return await subscribeToMusician(musicianId);
      }
    },
    onSuccess: (_, musicianId) => {
      setSubscribedMusicians((prev) => ({
        ...prev,
        [musicianId]: !prev[musicianId],
      }));
    },
  });

  const handleLikeClick = (productId: number) => {
    likeMutation.mutate(productId);
  };

  const handleSubscribeClick = (musicianId: number) => {
    subscribeMutation.mutate(musicianId);
  };

  return (
    <div className="min-h-screen bg-gray-50">
      <Navbar />

      <main className="max-w-7xl mx-auto px-4 py-8">
        <div className="grid grid-cols-1 gap-8">
          {/* Top Products Section */}
          <section>
            <h2 className="text-2xl font-bold text-gray-800 mb-4">
              Top Rated Products
            </h2>
            <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
              {isLoadingProducts ? (
                <div className="col-span-3 text-center py-8">
                  Loading products...
                </div>
              ) : (
                topProducts?.map((product: any) => (
                  <div
                    key={product.id}
                    className="bg-white p-6 rounded-lg shadow-md"
                  >
                    <div className="aspect-w-16 aspect-h-9 mb-4">
                      <Link to={`/product/${product.id}`}>
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
                          className="w-full h-full object-cover rounded-md hover:opacity-75 transition-opacity"
                        />
                      </Link>
                    </div>
                    <div className="flex justify-between items-start mb-4">
                      <h3 className="text-lg font-semibold">{product.name}</h3>
                      <button
                        onClick={() => handleLikeClick(product.id)}
                        className={`flex items-center justify-center w-10 h-10 rounded-full transition-colors ${
                          likedProducts[product.id]
                            ? "text-red-500 hover:text-red-600"
                            : "text-gray-400 hover:text-gray-500"
                        }`}
                        disabled={likeMutation.isPending}
                      >
                        <svg
                          xmlns="http://www.w3.org/2000/svg"
                          className="h-6 w-6"
                          fill={
                            likedProducts[product.id] ? "currentColor" : "none"
                          }
                          viewBox="0 0 24 24"
                          stroke="currentColor"
                          strokeWidth={2}
                        >
                          <path
                            strokeLinecap="round"
                            strokeLinejoin="round"
                            d="M4.318 6.318a4.5 4.5 0 000 6.364L12 20.364l7.682-7.682a4.5 4.5 0 00-6.364-6.364L12 7.636l-1.318-1.318a4.5 4.5 0 00-6.364 0z"
                          />
                        </svg>
                      </button>
                    </div>
                    <div className="space-y-1 text-sm text-gray-700 mb-3">
                      <div className="flex items-center mb-2">
                        <StarRating
                          rating={product.rate}
                          onRatingChange={() => {}}
                          size="sm"
                        />
                        <span className="ml-2 text-gray-600">({product.rate}/5)</span>
                      </div>
                      <p>
                        <span className="font-medium">Brand:</span>{" "}
                        <Link 
                          to={`/brand/${product.brand.id}`} 
                          className="text-indigo-600 hover:text-indigo-800"
                        >
                          {product.brand.name}
                        </Link>
                      </p>
                      <p>
                        <span className="font-medium">Type:</span>{" "}
                        {formatProductType(product.typeOfProduct)}
                      </p>
                      <p>
                        <span className="font-medium">Price:</span> $
                        {product.avgPrice.toFixed(2)}
                      </p>
                      <p>
                        <span className="font-medium">Body Material:</span>{" "}
                        {product.bodyMaterial.toLowerCase()}
                      </p>
                      {product.strings && (
                        <p>
                          <span className="font-medium">Strings:</span>{" "}
                          {product.strings}
                        </p>
                      )}
                    </div>
                    <Link
                      to={`/product/${product.id}`}
                      className="text-indigo-600 hover:text-indigo-800 mt-2 inline-block"
                    >
                      View Details
                    </Link>
                  </div>
                ))
              )}
            </div>
          </section>

          {/* Top Articles Section */}
          <section>
            <h2 className="text-2xl font-bold text-gray-800 mb-4">
              Latest Top Articles
            </h2>
            <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
              {isLoadingArticles ? (
                <div className="col-span-2 text-center py-8">
                  Loading articles...
                </div>
              ) : (
                topArticles?.map((article: any) => (
                  <div
                    key={article.id}
                    className="bg-white p-6 rounded-lg shadow-md"
                  >
                    <h3 className="text-lg font-semibold">{article.header}</h3>
                    <p className="text-gray-600 mt-2 line-clamp-2">
                      {article.content}
                    </p>
                    <div className="mt-2 text-sm text-gray-500">
                      By <span className="font-medium">{article.author}</span> •{" "}
                      {new Date(article.createdAt).toLocaleDateString()}
                    </div>
                    <Link
                      to={`/article/${article.id}`}
                      className="text-indigo-600 hover:text-indigo-800 mt-2 inline-block"
                    >
                      Read More
                    </Link>
                  </div>
                ))
              )}
            </div>
          </section>

          {/* Top Musicians Section */}
          <section>
            <h2 className="text-2xl font-bold text-gray-800 mb-4">
              Featured Musicians
            </h2>
            <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
              {isLoadingMusicians ? (
                <div className="col-span-3 text-center py-8">
                  Loading musicians...
                </div>
              ) : (
                topMusicians?.map((musician: any) => (
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
                    <div className="flex justify-between items-start w-full mb-4">
                      <h3 className="text-lg font-semibold">{musician.name}</h3>
                      <button
                        onClick={() => handleSubscribeClick(musician.id)}
                        className={`px-4 py-2 rounded-full text-sm font-medium transition-colors ${
                          subscribedMusicians[musician.id]
                            ? "bg-indigo-100 text-indigo-700 hover:bg-indigo-200"
                            : "bg-indigo-600 text-white hover:bg-indigo-700"
                        }`}
                        disabled={subscribeMutation.isPending}
                      >
                        {subscribedMusicians[musician.id] ? "Subscribed" : "Subscribe"}
                      </button>
                    </div>
                    <p className="text-gray-600 text-sm mb-2">
                      {musician.subscribers} subscribers
                    </p>
                    <Link
                      to={`/musician/${musician.id}`}
                      className="text-indigo-600 hover:text-indigo-800 mt-2 inline-block"
                    >
                      View Profile
                    </Link>
                  </div>
                ))
              )}
            </div>
          </section>
        </div>
      </main>
    </div>
  );
}
