import { BrowserRouter, Routes, Route } from "react-router-dom";
import { QueryClient, QueryClientProvider } from "@tanstack/react-query";
import { AuthProvider } from "./contexts/AuthContext";
import { ProtectedRoute } from "./components/ProtectedRoute";
import Login from "./pages/Login";
import Register from "./pages/Register";
import Home from "./pages/Home";
import ProductDetails from "./pages/ProductDetails";
import NotFound from "./pages/Error";
import ArticleDetails from "./pages/ArticleDetails";
import Catalog from "./pages/Catalog";
import Articles from "./pages/Articles";
import Musicians from "./pages/Musicians";
import Forum from "./pages/Forum";
import Brands from "./pages/Brands";
import BrandDetails from "./pages/BrandDetails";
import UserInfo from "./pages/UserInfo";
import MusicianProfile from "./pages/MusicianProfile";
import ForumTopic from "./pages/ForumTopic";
import AdminArticles from "./pages/AdminArticles";

const queryClient = new QueryClient();

function App() {
  return (
    <QueryClientProvider client={queryClient}>
      <BrowserRouter>
        <AuthProvider>
          <Routes>
            <Route path="/login" element={<Login />} />
            <Route path="/register" element={<Register />} />
            <Route
              path="/"
              element={
                <ProtectedRoute>
                  <Home />
                </ProtectedRoute>
              }
            />
            <Route
              path="/brand"
              element={
                <ProtectedRoute>
                  <Brands></Brands>
                </ProtectedRoute>
              }
            ></Route>
            <Route
              path="/product/:id"
              element={
                <ProtectedRoute>
                  <ProductDetails />
                </ProtectedRoute>
              }
            />
            <Route
              path="/musician/:id"
              element={
                <ProtectedRoute>
                  <MusicianProfile></MusicianProfile>
                </ProtectedRoute>
              }
            />
            <Route
              path="/forum/topic/:id"
              element={
                <ProtectedRoute>
                  <ForumTopic />
                </ProtectedRoute>
              }
            />
            <Route
              path="/brand/:id"
              element={
                <ProtectedRoute>
                  <BrandDetails />
                </ProtectedRoute>
              }
            />
            <Route
              path="/article/:id"
              element={
                <ProtectedRoute>
                  <ArticleDetails />
                </ProtectedRoute>
              }
            />
            <Route
              path="/catalog"
              element={
                <ProtectedRoute>
                  <Catalog />
                </ProtectedRoute>
              }
            />
            <Route
              path="/articles"
              element={
                <ProtectedRoute>
                  <Articles />
                </ProtectedRoute>
              }
            />
            <Route
              path="/musicians"
              element={
                <ProtectedRoute>
                  <Musicians></Musicians>
                </ProtectedRoute>
              }
            ></Route>
            <Route
              path="/forum"
              element={
                <ProtectedRoute>
                  <Forum />
                </ProtectedRoute>
              }
            />
            <Route path="/profile" element={<UserInfo />} />
            <Route path="/admin" element={<AdminArticles />} />
            <Route path="*" element={<NotFound />} />
          </Routes>
        </AuthProvider>
      </BrowserRouter>
    </QueryClientProvider>
  );
}

export default App;
