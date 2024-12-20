import { useState, useEffect } from "react";
import { useQuery, useMutation } from "@tanstack/react-query";
import { useParams, Link, Navigate } from "react-router-dom";
import Navbar from "../components/Navbar";
import StarRating from "../components/StarRating";
import {
  getProductsByBrand,
  likeProduct,
  unlikeProduct,
} from "../services/api";
import { Brand, formatProductType, ProductSimple } from "../services/types";
import api from "../services/api";
import Pagination from "../components/Pagination";
export default function BrandDetails() {
  const { id } = useParams();
  const [page, setPage] = useState(1);
  const pageSize = 12;
  const [likedProducts, setLikedProducts] = useState<{
    [key: number]: boolean;
  }>({});
  const {
    data: brand,
    isLoading: isLoadingBrand,
    error: brandError,
  } = useQuery<Brand>({
    queryKey: ["brand", id],
    queryFn: async () => {
      const response = await api.get(`/brand/id/${id}`);
      return response.data;
    },
  });
  const { data: productsResponse, isLoading: isLoadingProducts } = useQuery({
    queryKey: ["brandProducts", id, page],
    queryFn: () =>
      getProductsByBrand(Number(id), (page - 1) * pageSize, pageSize),
    enabled: !!id,
  });
  const products = productsResponse?.data as ProductSimple[];
  const { data: userProducts } = useQuery<ProductSimple[]>({
    queryKey: ["userProducts"],
    queryFn: async () => {
      const response = await api.get("/user/products");
      return response.data;
    },
  });
  useEffect(() => {
    if (userProducts) {
      const likedMap = userProducts.reduce(
        (acc: { [key: number]: boolean }, product: ProductSimple) => {
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
  const handleLikeClick = (productId: number) => {
    likeMutation.mutate(productId);
  };
  if (brandError) {
    return <Navigate to="/error" />;
  }
  if (isLoadingBrand || isLoadingProducts) {
    return (
      <div className="min-h-screen bg-gray-50">
        <Navbar />
        <div className="max-w-7xl mx-auto px-4 py-8">
          <div className="text-center">Loading...</div>
        </div>
      </div>
    );
  }
  if (!brand) {
    return <Navigate to="/error" />;
  }
  return (
    <div className="min-h-screen bg-gray-50">
      <Navbar />
      <main className="max-w-7xl mx-auto px-4 py-8">
        {/* Brand Header Section */}
        <div className="bg-white rounded-lg shadow-md p-6 mb-8">
          <h1 className="text-3xl font-bold text-gray-900 mb-4">
            {brand.name}
          </h1>
          <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
            <div className="space-y-3">
              <p className="text-gray-600">
                <span className="font-medium">Country:</span> {brand.country}
              </p>
              <p className="text-gray-600">
                <a
                  href={brand.website}
                  target="_blank"
                  rel="noopener noreferrer"
                  className="text-indigo-600 hover:text-indigo-800"
                >
                  <span className="font-medium">Website:</span> {brand.website}
                </a>
              </p>
              <p className="text-gray-600">
                <a
                  href={`mailto:${brand.email}`}
                  className="text-indigo-600 hover:text-indigo-800"
                >
                  <span className="font-medium">Email:</span> {brand.email}
                </a>
              </p>
            </div>
          </div>
        </div>
        {/* Products Section */}
        <section>
          <h2 className="text-2xl font-bold text-gray-900 mb-6">
            Products by {brand.name}
          </h2>
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
            {products?.map((product) => (
              <div
                key={product.id}
                className="bg-white p-4 rounded-lg shadow-md"
              >
                <div className="flex justify-between items-start mb-2">
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
                      className="w-full h-48 object-cover rounded-md mb-4"
                    />
                  </Link>
                  <button
                    onClick={() => handleLikeClick(product.id)}
                    className={`p-2 rounded-full transition-colors ${
                      likedProducts[product.id]
                        ? "text-red-600 hover:text-red-700"
                        : "text-gray-400 hover:text-gray-500"
                    }`}
                  >
                    <svg
                      className="w-6 h-6"
                      fill={likedProducts[product.id] ? "currentColor" : "none"}
                      stroke="currentColor"
                      viewBox="0 0 24 24"
                    >
                      <path
                        strokeLinecap="round"
                        strokeLinejoin="round"
                        strokeWidth="2"
                        d="M4.318 6.318a4.5 4.5 0 000 6.364L12 20.364l7.682-7.682a4.5 4.5 0 00-6.364-6.364L12 7.636l-1.318-1.318a4.5 4.5 0 00-6.364 0z"
                      />
                    </svg>
                  </button>
                </div>

                <h3 className="text-lg font-semibold mb-2">{product.name}</h3>

                <div className="flex items-center mb-2">
                  <StarRating
                    rating={product.rate}
                    onRatingChange={() => {}}
                    size="sm"
                  />
                  <span className="ml-2 text-gray-600">({product.rate}/5)</span>
                </div>

                <div className="text-sm text-gray-600 space-y-1">
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
                </div>

                <Link
                  to={`/product/${product.id}`}
                  className="mt-4 inline-block text-indigo-600 hover:text-indigo-800"
                >
                  View Details
                </Link>
              </div>
            ))}
          </div>
          {products?.length !== 0 && (
            <div className="mt-8">
              <Pagination
                currentPage={page}
                hasMore={products?.length === pageSize}
                onPageChange={setPage}
              />
            </div>
          )}
        </section>
      </main>
    </div>
  );
}
