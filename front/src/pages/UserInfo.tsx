import { useState } from "react";
import { useQuery } from "@tanstack/react-query";
import { Link } from "react-router-dom";
import Navbar from "../components/Navbar";
import { useAuth } from "../contexts/AuthContext";
import api from "../services/api";
import { ProductSimple, Musician } from "../services/types";
export default function UserInfo() {
 const { logout } = useAuth();
 const [activeTab, setActiveTab] = useState<'products' | 'musicians'>('products');
  const { data: likedProducts } = useQuery<ProductSimple[]>({
   queryKey: ["userProducts"],
   queryFn: async () => {
     const response = await api.get("/user/products");
     return response.data;
   },
 });
  const { data: subscribedMusicians } = useQuery<Musician[]>({
   queryKey: ["userMusicians"],
   queryFn: async () => {
     const response = await api.get("/user/musicians");
     return response.data;
   },
 });
  return (
   <div className="min-h-screen bg-gray-50">
     <Navbar />
     
     <main className="max-w-7xl mx-auto px-4 py-8">
       <div className="bg-white rounded-lg shadow-md p-6 mb-6">
         <div className="flex justify-between items-center mb-6">
           <h1 className="text-2xl font-bold text-gray-900">User Profile</h1>
           <button
             onClick={logout}
             className="px-4 py-2 bg-red-600 text-white rounded-md hover:bg-red-700"
           >
             Logout
           </button>
         </div>
       </div>
        {/* Tabs */}
       <div className="bg-white rounded-lg shadow-md">
         <div className="border-b border-gray-200">
           <nav className="flex -mb-px">
             <button
               onClick={() => setActiveTab('products')}
               className={`py-4 px-6 ${
                 activeTab === 'products'
                   ? 'border-b-2 border-indigo-500 text-indigo-600'
                   : 'text-gray-500 hover:text-gray-700'
               }`}
             >
               Liked Products
             </button>
             <button
               onClick={() => setActiveTab('musicians')}
               className={`py-4 px-6 ${
                 activeTab === 'musicians'
                   ? 'border-b-2 border-indigo-500 text-indigo-600'
                   : 'text-gray-500 hover:text-gray-700'
               }`}
             >
               Subscribed Musicians
             </button>
           </nav>
         </div>
          <div className="p-6">
           {activeTab === 'products' ? (
             <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
               {likedProducts?.map((product) => (
                 <div key={product.id} className="bg-white p-6 rounded-lg shadow-md">
                   <Link to={`/product/${product.id}`}>
                     <div className="aspect-w-16 aspect-h-9 mb-4">
                       <img
                         src={[
                           "https://images.equipboard.com/uploads/item/image/16008/gibson-les-paul-classic-electric-guitar-m.webp?v=1734091576",
                           "https://images.equipboard.com/uploads/item/image/17684/roland-g-707-m.webp?v=1734005219",
                           "https://images.equipboard.com/uploads/item/image/9259/yamaha-hs8-powered-studio-monitor-m.webp?v=1734264173",
                           "https://images.equipboard.com/uploads/item/image/17369/dave-smith-instruments-sequential-prophet-6-m.webp?v=1732782610",
                         ][product.id % 4]}
                         alt={product.name}
                         className="w-full h-full object-cover rounded-md"
                       />
                     </div>
                     <h3 className="text-lg font-semibold mb-2">{product.name}</h3>
                   </Link>
                 </div>
               ))}
             </div>
           ) : (
             <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
               {subscribedMusicians?.map((musician) => (
                 <div key={musician.id} className="bg-white p-6 rounded-lg shadow-md flex flex-col items-center">
                   <div className="w-32 h-32 mb-4">
                     <Link to={`/musician/${musician.id}`}>
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
                     </Link>
                   </div>
                   <h3 className="text-lg font-semibold text-center">{musician.name}</h3>
                   <p className="text-gray-600 mt-2">{musician.subscribers} subscribers</p>
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