import { useQuery } from "@tanstack/react-query";
import { Link } from "react-router-dom";
import Navbar from "../components/Navbar";
import {
  getTopProducts,
  getTopArticles,
  getTopMusicians,
} from "../services/api";

export default function Home() {
  const { data: topProducts, isLoading: isLoadingProducts } = useQuery({
    queryKey: ["topProducts"],
    queryFn: getTopProducts,
  });

  const { data: topArticles, isLoading: isLoadingArticles } = useQuery({
    queryKey: ["topArticles"],
    queryFn: getTopArticles,
  });

  const { data: topMusicians, isLoading: isLoadingMusicians } = useQuery({
    queryKey: ["topMusicians"],
    queryFn: getTopMusicians,
  });

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
                    <h3 className="text-lg font-semibold">{product.name}</h3>
                    <p className="text-gray-600 mb-2">
                      Rating: {product.rate}/5
                    </p>
                    <div className="space-y-1 text-sm text-gray-700 mb-3">
                      <p>
                        <span className="font-medium">Brand:</span>{" "}
                        {product.brand.name}
                      </p>
                      <p>
                        <span className="font-medium">Type:</span>{" "}
                        {product.typeOfProduct.replace("_", " ")}
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
                    className="bg-white p-6 rounded-lg shadow-md"
                  >
                    <h3 className="text-lg font-semibold">{musician.name}</h3>
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
