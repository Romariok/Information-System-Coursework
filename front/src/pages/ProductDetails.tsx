import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import { useParams, Link, Navigate } from "react-router-dom";
import Navbar from "../components/Navbar";
import {
  getProductDetails,
  getProductArticles,
  getProductMusicians,
  getProductFeedbacks,
  getProductShops,
  checkProductLiked,
  likeProduct,
  unlikeProduct,
  addProductFeedback,
  checkMusicianSubscribed,
  subscribeToMusician,
  unsubscribeFromMusician,
} from "../services/api";
import {
  Product,
  Article,
  Musician,
  Feedback,
  ShopProduct,
} from "../services/types";
import Pagination from "../components/Pagination";
import { useState, useEffect } from "react";
import StarRating from "../components/StarRating";
import CollapsibleReviewForm from "../components/CollapsibleReviewForm";

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

export default function ProductDetails() {
  const { id } = useParams();
  const [articlesPage, setArticlesPage] = useState(1);
  const [musiciansPage, setMusiciansPage] = useState(1);
  const [feedbackPage, setFeedbackPage] = useState(1);
  const [shopsPage, setShopsPage] = useState(1);
  const [isLiked, setIsLiked] = useState(false);
  const pageSize = 5;
  const [subscriptions, setSubscriptions] = useState<{ [key: number]: boolean }>({});

  const {
    data: product,
    isLoading: isLoadingProduct,
    error: productError,
  } = useQuery<Product>({
    queryKey: ["product", id],
    queryFn: () => getProductDetails(id!),
  });

  const { data: articlesData } = useQuery<{ items: Article[] }>({
    queryKey: ["productArticles", product?.product.name, articlesPage],
    queryFn: () =>
      product?.product.name
        ? getProductArticles(id!, (articlesPage - 1) * pageSize, pageSize)
        : Promise.resolve({ items: [] }),
    enabled: !!product,
  });

  const { data: musiciansData } = useQuery<{
    items: Musician[];
  }>({
    queryKey: ["productMusicians", id, musiciansPage],
    queryFn: () =>
      getProductMusicians(id!, (musiciansPage - 1) * pageSize, pageSize),
    enabled: !!product,
  });

  const { data: feedbackData } = useQuery<{ items: Feedback[] }>({
    queryKey: ["productFeedbacks", id, feedbackPage],
    queryFn: () =>
      getProductFeedbacks(id!, (feedbackPage - 1) * pageSize, pageSize),
    enabled: !!product,
  });

  const { data: shopsData } = useQuery<{ items: ShopProduct[] }>({
    queryKey: ["productShops", product?.product.name, shopsPage],
    queryFn: () => getProductShops(id!, (shopsPage - 1) * pageSize, pageSize),
    enabled: !!product,
  });

  const { data: liked } = useQuery<boolean>({
    queryKey: ["productLiked", id],
    queryFn: () => checkProductLiked(id!),
    enabled: !!id,
  });

  useEffect(() => {
    if (liked !== undefined) {
      setIsLiked(liked);
    }
  }, [liked]);

  const likeMutation = useMutation<boolean, Error, string>({
    mutationFn: (productId: string) =>
      isLiked ? unlikeProduct(productId) : likeProduct(productId),
    onSuccess: () => setIsLiked(!isLiked),
  });

  const handleLikeClick = () => {
    if (id) {
      likeMutation.mutate(id);
    }
  };

  const feedbackMutation = useMutation({
    mutationFn: (data: { text: string; stars: number }) =>
      addProductFeedback(id!, data.text, data.stars),
    onSuccess: () => {
      useQueryClient().invalidateQueries({
        queryKey: ["productFeedbacks", id],
      });
    },
  });

  useQuery({
    queryKey: ["subscriptions", musiciansData?.items],
    queryFn: async () => {
      if (!musiciansData?.items) return;
      const checks = await Promise.all(
        musiciansData.items.map(musician => 
          checkMusicianSubscribed(musician.id)
        )
      );
      const newSubscriptions = musiciansData.items.reduce((acc, musician, index) => {
        acc[musician.id] = checks[index];
        return acc;
      }, {} as { [key: number]: boolean });
      setSubscriptions(newSubscriptions);
    },
    enabled: !!musiciansData?.items,
  });

  const subscriptionMutation = useMutation({
    mutationFn: async ({ musicianId, subscribed }: { musicianId: number; subscribed: boolean }) => {
      if (subscribed) {
        return await unsubscribeFromMusician(musicianId);
      } else {
        return await subscribeToMusician(musicianId);
      }
    },
    onSuccess: (_, { musicianId }) => {
      setSubscriptions(prev => ({
        ...prev,
        [musicianId]: !prev[musicianId]
      }));
    },
  });

  const handleSubscribe = (musicianId: number) => {
    subscriptionMutation.mutate({ 
      musicianId, 
      subscribed: subscriptions[musicianId] 
    });
  };

  if (productError) {
    return <Navigate to="*" />;
  }
  if (isLoadingProduct) {
    return <div>Loading...</div>;
  }
  if (!product) {
    return <Navigate to="/error" />;
  }
  return (
    <div className="min-h-screen bg-gray-50">
      <Navbar />

      <main className="max-w-7xl mx-auto px-4 py-8">
        {/* Product Details Section */}
        <div className="bg-white rounded-lg shadow-md p-6 mb-8">
          <div className="grid grid-cols-1 md:grid-cols-2 gap-8">
            <div>
              <img
                src={
                  [
                    "https://images.equipboard.com/uploads/item/image/16008/gibson-les-paul-classic-electric-guitar-m.webp?v=1734091576",
                    "https://images.equipboard.com/uploads/item/image/17684/roland-g-707-m.webp?v=1734005219",
                    "https://images.equipboard.com/uploads/item/image/9259/yamaha-hs8-powered-studio-monitor-m.webp?v=1734264173",
                    "https://images.equipboard.com/uploads/item/image/17369/dave-smith-instruments-sequential-prophet-6-m.webp?v=1732782610",
                  ][product.product.id % 4]
                }
                alt={product.product.name}
                className="w-full rounded-lg"
              />
            </div>
            <div>
              <h1 className="text-3xl font-bold mb-4">
                {product.product.name}
              </h1>
              <div className="space-y-2">
                <p>
                  <span className="font-medium">Rating:</span>{" "}
                  {product.product.rate}/5
                </p>
                <p>
                  <span className="font-medium">Brand:</span>{" "}
                  <Link
                    to={`/brand/${product.product.brand.id}`}
                    className="text-indigo-600 hover:text-indigo-800"
                  >
                    {product.product.brand.name}
                  </Link>
                </p>
                <p>
                  <span className="font-medium">Type:</span>{" "}
                  {formatProductType(product.product.typeOfProduct)}
                </p>
                <p>
                  <span className="font-medium">Price:</span> $
                  {product.product.avgPrice.toFixed(2)}
                </p>
                <p>
                  <span className="font-medium">Body Material:</span>{" "}
                  {product.product.bodyMaterial.toLowerCase()}
                </p>
                {product.product.strings && (
                  <p>
                    <span className="font-medium">Strings:</span>{" "}
                    {product.product.strings}
                  </p>
                )}
                <p>
                  <span className="font-medium">Genres:</span>{" "}
                  {product.genres.join(", ")}
                </p>
                <p>
                  <span className="font-medium">Description:</span>{" "}
                  {product.product.description}
                </p>
              </div>
              <div className="mt-4">
                <button
                  onClick={handleLikeClick}
                  className={`flex items-center gap-2 px-4 py-2 rounded-md transition-colors ${
                    isLiked
                      ? "bg-red-500 text-white hover:bg-red-600"
                      : "bg-gray-100 text-gray-700 hover:bg-gray-200"
                  }`}
                  disabled={likeMutation.isPending}
                >
                  <svg
                    xmlns="http://www.w3.org/2000/svg"
                    className="h-5 w-5"
                    fill={isLiked ? "currentColor" : "none"}
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
                  {isLiked ? "Liked" : "Like"}
                </button>
              </div>
            </div>
          </div>
        </div>

        {/* Musicians Section */}
        <section className="mb-8">
          <h2 className="text-2xl font-bold mb-4">
            Musicians Using This Product
          </h2>
          <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
            {musiciansData?.items?.map((musician) => (
              <div
                key={musician.id}
                className="bg-white p-6 rounded-lg shadow-md flex flex-col items-center"
              >
                <div className="w-32 h-32 mb-4">
                  <img
                    src={[
                      "https://tntmusic.ru/media/content/article@2x/2020-12-25_08-09-59__950100bc-4688-11eb-be12-87ef0634b7d4.jpg",
                      "https://i1.sndcdn.com/artworks-QSYcavKwyzW8LwyR-jAEK0g-t500x500.jpg",
                      "https://the-flow.ru/uploads/images/origin/04/15/95/60/74/8161911.jpg",
                      "https://avatars.mds.yandex.net/get-mpic/5304425/img_id6170984171594674671.jpeg/orig",
                    ][musician.id % 4]}
                    alt={musician.name}
                    className="w-full h-full object-cover rounded-full"
                  />
                </div>
                <h3 className="text-lg font-semibold text-center">
                  {musician.name}
                </h3>
                <div className="flex flex-col items-center gap-2 mt-2">
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
          {musiciansData?.items?.length !== 0 && (
            <Pagination
              currentPage={musiciansPage}
              hasMore={musiciansData?.items?.length === pageSize}
              onPageChange={setMusiciansPage}
            />
          )}
        </section>

        {/* Articles Section */}
        <section className="mb-8">
          <h2 className="text-2xl font-bold mb-4">Related Articles</h2>
          <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
            {articlesData?.items.map((article) => (
              <div
                key={article.id}
                className="bg-white p-6 rounded-lg shadow-md"
              >
                <h3 className="text-lg font-semibold">{article.header}</h3>
                <p className="text-gray-600 mt-2 line-clamp-2">
                  {article.text}
                </p>
                <div className="mt-2 text-sm text-gray-500">
                  By{" "}
                  <span className="font-medium">{article.author.username}</span>{" "}
                  • {new Date(article.createdAt).toLocaleDateString()}
                </div>
                <Link
                  to={`/article/${article.id}`}
                  className="text-indigo-600 hover:text-indigo-800 mt-2 inline-block"
                >
                  Read More
                </Link>
              </div>
            ))}
          </div>
          {articlesData?.items?.length !== 0 && (
            <Pagination
              currentPage={articlesPage}
              hasMore={articlesData?.items?.length === pageSize}
              onPageChange={setArticlesPage}
            />
          )}
        </section>

        {/* Feedbacks Section */}
        <section className="mb-8">
          <h2 className="text-2xl font-bold mb-4">User Reviews</h2>
          <CollapsibleReviewForm
            onSubmit={({ text, stars }) => {
              feedbackMutation.mutate({ text, stars });
            }}
            isSubmitting={feedbackMutation.isPending}
          />

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

        {/* Shops Section */}
        <section>
          <h2 className="text-2xl font-bold mb-4">Where to Buy</h2>
          <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
            {shopsData?.items?.map((shop) => (
              <div key={shop.id} className="bg-white p-6 rounded-lg shadow-md">
                <h3 className="text-lg font-semibold">{shop.name}</h3>
                <p className="text-gray-600">{shop.address}</p>
                <div className="mt-2">
                  <span className="font-medium">Price:</span> $
                  {shop.price.toFixed(2)}
                </div>
                <div className="mt-2">
                  <span
                    className={`inline-flex items-center px-3 py-1 rounded-full text-sm font-medium ${
                      shop.available
                        ? "bg-green-100 text-green-800"
                        : "bg-red-100 text-red-800"
                    }`}
                  >
                    {shop.available ? "In Stock" : "Out of Stock"}
                  </span>
                </div>
              </div>
            ))}
          </div>
          {shopsData?.items?.length !== 0 && (
            <Pagination
              currentPage={shopsPage}
              hasMore={shopsData?.items?.length === pageSize}
              onPageChange={setShopsPage}
            />
          )}
        </section>
      </main>
    </div>
  );
}
