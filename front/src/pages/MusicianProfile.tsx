import { useParams } from "react-router-dom";
import { useQuery } from "@tanstack/react-query";
import { Link } from "react-router-dom";
import Navbar from "../components/Navbar";
import {
  getMusicianInfo,
  checkMusicianSubscribed,
  subscribeToMusician,
  unsubscribeFromMusician,
} from "../services/api";
import { useMutation, useQueryClient } from "@tanstack/react-query";
import { Genre, TypeOfMusician, ProductSimple } from "../services/types.ts";
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
      </main>
    </div>
  );
}
