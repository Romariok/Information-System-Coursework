import { useState, useEffect } from "react";
import { useQuery, useMutation } from "@tanstack/react-query";
import { Link } from "react-router-dom";
import Navbar from "../components/Navbar";
import StarRating from "../components/StarRating";
import { Product } from "../services/types";
import api from "../services/api";
import { likeProduct, unlikeProduct } from "../services/api";

// Reusing the existing format function from ProductDetails
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

// Add these enums at the top of the file
enum GuitarForm {
  STRATOCASTER = "STRATOCASTER",
  TELECASTER = "TELECASTER",
  LES_PAUL = "LES_PAUL",
  SG = "SG",
  FLYING_V = "FLYING_V",
  EXPLORER = "EXPLORER",
}

enum Color {
  BLACK = "BLACK",
  WHITE = "WHITE",
  RED = "RED",
  BLUE = "BLUE",
  GREEN = "GREEN",
  YELLOW = "YELLOW",
  BROWN = "BROWN",
  NATURAL = "NATURAL",
}

enum BodyMaterial {
  MAHOGANY = "MAHOGANY",
  MAPLE = "MAPLE",
  ASH = "ASH",
  ALDER = "ALDER",
  BASSWOOD = "BASSWOOD",
}

enum PickupConfiguration {
  SSS = "SSS",
  HSS = "HSS",
  HH = "HH",
  SS = "SS",
  P90 = "P90",
}

type SortOption = {
  field: keyof Product | "brand.name";
  direction: "asc" | "desc";
};

export default function Catalog() {
  const [page, setPage] = useState(1);
  const pageSize = 18;
  const [likedProducts, setLikedProducts] = useState<{
    [key: number]: boolean;
  }>({});
  const [sort, setSort] = useState<SortOption>({
    field: "name",
    direction: "asc",
  });

  // Filter states
  const [filters, setFilters] = useState({
    name: "",
    minRate: 0,
    maxRate: 5,
    brandId: "",
    guitarForm: "",
    typeOfProduct: "",
    lads: "",
    minPrice: 0,
    maxPrice: 100000,
    color: "",
    strings: "",
    tipMaterial: "",
    bodyMaterial: "",
    pickupConfiguration: "",
    typeComboAmplifier: "",
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

  const handleLikeClick = (productId: number) => {
    likeMutation.mutate(productId);
  };

  const handlePriceChange = (key: "minPrice" | "maxPrice", value: string) => {
    const numValue = Number(value);
    if (numValue < 0) return;

    if (key === "minPrice" && numValue > filters.maxPrice) return;
    if (key === "maxPrice" && numValue < filters.minPrice) return;

    handleFilterChange(key, numValue);
  };

  const { data: products, isLoading } = useQuery<Product[]>({
    queryKey: ["products", filters, page, sort],
    queryFn: async () => {
      const response = await api.get("/product/filter", {
        params: {
          ...filters,
          from: (page - 1) * pageSize,
          size: pageSize,
        },
      });
      const sortedProducts = [...response.data].sort((a, b) => {
        const field = sort.field;
        if (field === "brand.name") {
          const compareResult = a.brand.name.localeCompare(b.brand.name);
          return sort.direction === "asc" ? compareResult : -compareResult;
        }
        if (typeof a[field] === "string") {
          const compareResult = (a[field] as string).localeCompare(
            b[field] as string
          );
          return sort.direction === "asc" ? compareResult : -compareResult;
        }
        const compareResult = (a[field] as number) - (b[field] as number);
        return sort.direction === "asc" ? compareResult : -compareResult;
      });
      return sortedProducts;
    },
  });

  const handleFilterChange = (key: string, value: string | number) => {
    setFilters((prev) => ({
      ...prev,
      [key]: value,
    }));
    setPage(1);
  };

  return (
    <div className="min-h-screen bg-gray-50">
      <Navbar />

      <main className="max-w-7xl mx-auto px-4 py-8">
        <div className="grid grid-cols-4 gap-6">
          {/* Filters Sidebar */}
          <div className="col-span-1 bg-white p-4 rounded-lg shadow-md">
            <h2 className="text-xl font-bold mb-4">Filters</h2>

            {/* Sort Options */}
            <div className="mb-4">
              <label className="block text-sm font-medium text-gray-700">
                Sort By
              </label>
              <select
                value={`${sort.field}-${sort.direction}`}
                onChange={(e) => {
                  const [field, direction] = e.target.value.split("-");
                  setSort({
                    field: field as keyof Product | "brand.name",
                    direction: direction as "asc" | "desc",
                  });
                }}
                className="mt-1 block w-full rounded-md border-gray-300 shadow-sm focus:border-indigo-500 focus:ring-indigo-500"
              >
                <option value="name-asc">Name (A-Z)</option>
                <option value="name-desc">Name (Z-A)</option>
                <option value="rate-desc">Rating (High to Low)</option>
                <option value="rate-asc">Rating (Low to High)</option>
                <option value="avgPrice-asc">Price (Low to High)</option>
                <option value="avgPrice-desc">Price (High to Low)</option>
                <option value="brand.name-asc">Brand (A-Z)</option>
                <option value="brand.name-desc">Brand (Z-A)</option>
              </select>
            </div>

            {/* Original filters remain the same */}
            {/* Reference to original filters section */}
            <div className="space-y-4">
              {/* Search by name */}
              <div>
                <label className="block text-sm font-medium text-gray-700">
                  Search
                </label>
                <input
                  type="text"
                  value={filters.name}
                  onChange={(e) => handleFilterChange("name", e.target.value)}
                  className="mt-1 block w-full rounded-md border-gray-300 shadow-sm focus:border-indigo-500 focus:ring-indigo-500"
                />
              </div>

              {/* Price Range */}
              <div>
                <label className="block text-sm font-medium text-gray-700">
                  Price Range
                </label>
                <div className="grid grid-cols-2 gap-2">
                  <input
                    type="number"
                    value={filters.minPrice}
                    onChange={(e) =>
                      handlePriceChange("minPrice", e.target.value)
                    }
                    placeholder="Min"
                    min="0"
                    className="mt-1 block w-full rounded-md border-gray-300 shadow-sm focus:border-indigo-500 focus:ring-indigo-500"
                  />
                  <input
                    type="number"
                    value={filters.maxPrice}
                    onChange={(e) =>
                      handlePriceChange("maxPrice", e.target.value)
                    }
                    placeholder="Max"
                    min="0"
                    className="mt-1 block w-full rounded-md border-gray-300 shadow-sm focus:border-indigo-500 focus:ring-indigo-500"
                  />
                </div>
              </div>

              {/* Rating Range */}
              <div>
                <label className="block text-sm font-medium text-gray-700">
                  Rating
                </label>
                <div className="grid grid-cols-2 gap-2">
                  <input
                    type="number"
                    value={filters.minRate}
                    onChange={(e) =>
                      handleFilterChange("minRate", e.target.value)
                    }
                    min="0"
                    max="5"
                    step="0.5"
                    className="mt-1 block w-full rounded-md border-gray-300 shadow-sm focus:border-indigo-500 focus:ring-indigo-500"
                  />
                  <input
                    type="number"
                    value={filters.maxRate}
                    onChange={(e) =>
                      handleFilterChange("maxRate", e.target.value)
                    }
                    min="0"
                    max="5"
                    step="0.5"
                    className="mt-1 block w-full rounded-md border-gray-300 shadow-sm focus:border-indigo-500 focus:ring-indigo-500"
                  />
                </div>
              </div>

              {/* Guitar Form */}
              <div>
                <label className="block text-sm font-medium text-gray-700">
                  Guitar Form
                </label>
                <select
                  value={filters.guitarForm}
                  onChange={(e) =>
                    handleFilterChange("guitarForm", e.target.value)
                  }
                  className="mt-1 block w-full rounded-md border-gray-300 shadow-sm focus:border-indigo-500 focus:ring-indigo-500"
                >
                  <option value="">All Forms</option>
                  {Object.entries(GuitarForm).map(([key, value]) => (
                    <option key={key} value={value}>
                      {key.replace(/_/g, " ")}
                    </option>
                  ))}
                </select>
              </div>

              {/* Color */}
              <div>
                <label className="block text-sm font-medium text-gray-700">
                  Color
                </label>
                <select
                  value={filters.color}
                  onChange={(e) => handleFilterChange("color", e.target.value)}
                  className="mt-1 block w-full rounded-md border-gray-300 shadow-sm focus:border-indigo-500 focus:ring-indigo-500"
                >
                  <option value="">All Colors</option>
                  {Object.entries(Color).map(([key, value]) => (
                    <option key={key} value={value}>
                      {key}
                    </option>
                  ))}
                </select>
              </div>

              {/* Number of Strings */}
              <div>
                <label className="block text-sm font-medium text-gray-700">
                  Number of Strings
                </label>
                <select
                  value={filters.strings}
                  onChange={(e) =>
                    handleFilterChange("strings", e.target.value)
                  }
                  className="mt-1 block w-full rounded-md border-gray-300 shadow-sm focus:border-indigo-500 focus:ring-indigo-500"
                >
                  <option value="">Any</option>
                  {[4, 5, 6, 7, 8, 12].map((num) => (
                    <option key={num} value={num}>
                      {num} strings
                    </option>
                  ))}
                </select>
              </div>

              {/* Body Material */}
              <div>
                <label className="block text-sm font-medium text-gray-700">
                  Body Material
                </label>
                <select
                  value={filters.bodyMaterial}
                  onChange={(e) =>
                    handleFilterChange("bodyMaterial", e.target.value)
                  }
                  className="mt-1 block w-full rounded-md border-gray-300 shadow-sm focus:border-indigo-500 focus:ring-indigo-500"
                >
                  <option value="">All Materials</option>
                  {Object.entries(BodyMaterial).map(([key, value]) => (
                    <option key={key} value={value}>
                      {key}
                    </option>
                  ))}
                </select>
              </div>

              {/* Pickup Configuration */}
              <div>
                <label className="block text-sm font-medium text-gray-700">
                  Pickup Configuration
                </label>
                <select
                  value={filters.pickupConfiguration}
                  onChange={(e) =>
                    handleFilterChange("pickupConfiguration", e.target.value)
                  }
                  className="mt-1 block w-full rounded-md border-gray-300 shadow-sm focus:border-indigo-500 focus:ring-indigo-500"
                >
                  <option value="">All Configurations</option>
                  {Object.entries(PickupConfiguration).map(([key, value]) => (
                    <option key={key} value={value}>
                      {key}
                    </option>
                  ))}
                </select>
              </div>

              {/* Reset Filters */}
              <button
                onClick={() => {
                  setFilters({
                    name: "",
                    minRate: 0,
                    maxRate: 5,
                    brandId: "",
                    guitarForm: "",
                    typeOfProduct: "",
                    lads: "",
                    minPrice: 0,
                    maxPrice: 100000,
                    color: "",
                    strings: "",
                    tipMaterial: "",
                    bodyMaterial: "",
                    pickupConfiguration: "",
                    typeComboAmplifier: "",
                  });
                  setPage(1);
                }}
                className="w-full bg-gray-200 text-gray-700 py-2 px-4 rounded-md hover:bg-gray-300 transition-colors"
              >
                Reset Filters
              </button>
            </div>
          </div>

          {/* Products Grid */}
          <div className="col-span-3">
            {isLoading ? (
              <div className="text-center py-8">Loading products...</div>
            ) : (
              <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
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
                          fill={
                            likedProducts[product.id] ? "currentColor" : "none"
                          }
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

                    <h3 className="text-lg font-semibold mb-2">
                      {product.name}
                    </h3>

                    <div className="flex items-center mb-2">
                      <StarRating
                        rating={product.rate}
                        onRatingChange={() => {}}
                        size="sm"
                      />
                      <span className="ml-2 text-gray-600">
                        ({product.rate}/5)
                      </span>
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
                      <p>
                        <span className="font-medium">Price:</span> $
                        {product.avgPrice.toFixed(2)}
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
            )}
          </div>
        </div>
      </main>
    </div>
  );
}
