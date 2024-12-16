import { useState } from "react";
import { useQuery } from "@tanstack/react-query";
import { Link } from "react-router-dom";
import Navbar from "../components/Navbar";
import Pagination from "../components/Pagination";
import StarRating from "../components/StarRating";
import { getFilteredProducts, ProductFilters } from "../services/api";
import { Product } from "../services/types";

const sortOptions = [
  { value: "name", label: "Name" },
  { value: "price", label: "Price" },
  { value: "rate", label: "Rating" },
];

const typeOptions = [
  "PEDALS_AND_EFFECTS",
  "ELECTRIC_GUITAR",
  "STUDIO_RECORDING_GEAR",
  "KEYS_AND_MIDI",
  "AMPLIFIER",
  "DRUMMS_AND_PERCUSSION",
  "BASS_GUITAR",
  "ACOUSTIC_GUITAR",
  "SOFTWARE_AND_ACCESSORIES",
];

export default function Catalog() {
  const [page, setPage] = useState(1);
  const [filters, setFilters] = useState<ProductFilters>({
    minPrice: 0,
    maxPrice: 100000,
    minRate: 0,
    maxRate: 5,
  });
  const [sortBy, setSortBy] = useState("name");
  const [sortOrder, setSortOrder] = useState<"asc" | "desc">("asc");
  const pageSize = 12;

  const { data, isLoading } = useQuery({
    queryKey: ["products", filters, page, sortBy, sortOrder],
    queryFn: () =>
      getFilteredProducts(filters, page, pageSize, sortBy, sortOrder),
  });

  const handleFilterChange = (name: keyof ProductFilters, value: any) => {
    setFilters((prev) => ({ ...prev, [name]: value }));
    setPage(1);
  };

  return (
    <div className="min-h-screen bg-gray-50">
      <Navbar />

      <main className="max-w-7xl mx-auto px-4 py-8">
        <div className="grid grid-cols-4 gap-6">
          {/* Filters Sidebar */}
          <div className="col-span-1 bg-white p-6 rounded-lg shadow-md space-y-6">
            <h2 className="text-xl font-bold mb-4">Filters</h2>

            {/* Price Range */}
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-2">
                Price Range
              </label>
              <div className="flex gap-2">
                <input
                  type="number"
                  value={filters.minPrice}
                  onChange={(e) =>
                    handleFilterChange("minPrice", Number(e.target.value))
                  }
                  className="w-full border rounded-md px-3 py-2"
                  placeholder="Min"
                />
                <input
                  type="number"
                  value={filters.maxPrice}
                  onChange={(e) =>
                    handleFilterChange("maxPrice", Number(e.target.value))
                  }
                  className="w-full border rounded-md px-3 py-2"
                  placeholder="Max"
                />
              </div>
            </div>

            {/* Product Type */}
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-2">
                Product Type
              </label>
              <select
                value={filters.typeOfProduct || ""}
                onChange={(e) =>
                  handleFilterChange("typeOfProduct", e.target.value)
                }
                className="w-full border rounded-md px-3 py-2"
              >
                <option value="">All Types</option>
                {typeOptions.map((type) => (
                  <option key={type} value={type}>
                    {type.replace(/_/g, " ")}
                  </option>
                ))}
              </select>
            </div>

            {/* Rating Filter */}
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-2">
                Minimum Rating
              </label>
              <StarRating
                rating={filters.minRate || 0}
                onRatingChange={(rating) =>
                  handleFilterChange("minRate", rating)
                }
                size="sm"
              />
            </div>

            {/* Add more filters based on your needs */}
          </div>

          {/* Products Grid */}
          <div className="col-span-3">
            {/* Sorting Controls */}
            <div className="flex justify-between items-center mb-6">
              <div className="flex gap-4 items-center">
                <select
                  value={sortBy}
                  onChange={(e) => setSortBy(e.target.value)}
                  className="border rounded-md px-3 py-2"
                >
                  {sortOptions.map((option) => (
                    <option key={option.value} value={option.value}>
                      {option.label}
                    </option>
                  ))}
                </select>
                <button
                  onClick={() =>
                    setSortOrder((prev) => (prev === "asc" ? "desc" : "asc"))
                  }
                  className="p-2 hover:bg-gray-100 rounded-md"
                >
                  {sortOrder === "asc" ? "↑" : "↓"}
                </button>
              </div>
              <div className="text-gray-600">
                {data?.total || 0} products found
              </div>
            </div>

            {/* Products Grid */}
            <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
              {isLoading ? (
                <div className="col-span-3 text-center py-8">Loading...</div>
              ) : (
                data?.items.map((product: Product) => (
                  <div
                    key={product.id}
                    className="bg-white p-6 rounded-lg shadow-md"
                  >
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
                    <h3 className="text-lg font-semibold mb-2">
                      {product.name}
                    </h3>
                    <StarRating
                      rating={product.rate}
                      onRatingChange={() => {}}
                      size="sm"
                    />
                    <p className="text-gray-600 mt-2">${product.avgPrice}</p>
                  </div>
                ))
              )}
            </div>

            {/* Pagination */}
            {data && data.total > pageSize && (
              <Pagination
                currentPage={page}
                totalPages={Math.ceil(data.total / pageSize)}
                onPageChange={setPage}
              />
            )}
          </div>
        </div>
      </main>
    </div>
  );
}
