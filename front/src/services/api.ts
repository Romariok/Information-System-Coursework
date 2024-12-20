import axios from "axios";
import { Product, Article, Musician, Feedback, ShopProduct } from "./types";

const api = axios.create({
  baseURL: "http://localhost:5252/api",
});

api.interceptors.request.use((config) => {
  const token = localStorage.getItem("token");
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

export const login = async (username: string, password: string) => {
  const response = await api.post("/auth/login", { username, password });
  return response.data;
};

export const register = async (username: string, password: string) => {
  const response = await api.post("/auth/register", { username, password });
  return response.data;
};

export const getTopProducts = async () => {
  const response = await api.get("/product/filter", {
    params: {
      minRate: 4,
      maxRate: 5,
      from: 0,
      size: 6,
      minPrice: 0,
      maxPrice: 100000,
      ascending: false,
      sortBy: "RATE",
    },
  });
  return response.data;
};

export const getTopArticles = async () => {
  const response = await api.get("/article", {
    params: {
      from: 0,
      size: 6,
      sortBy: "CREATED_AT",
      ascending: false,
    },
  });
  return response.data;
};

export const getTopMusicians = async () => {
  const response = await api.get("/musician", {
    params: {
      from: 0,
      size: 6,
      sortBy: "SUBSCRIBERS",
      ascending: false,
    },
  });
  return response.data;
};

export const getProductDetails = async (id: string): Promise<Product> => {
  const response = await api.get(`/product/id/${id}`);
  return response.data;
};

export const getProductArticles = async (
  id: string,
  from: number,
  size: number
): Promise<{ items: Article[] }> => {
  const response = await api.get(`/product/${id}/articles`, {
    params: { from, size },
  });
  return {
    items: response.data.articles,
  };
};

export const getProductMusicians = async (
  id: string,
  from: number,
  size: number
): Promise<{ items: Musician[] }> => {
  const response = await api.get(`/product/${id}/musicians`, {
    params: { from, size },
  });
  return {
    items: response.data,
  };
};

export const getProductFeedbacks = async (
  id: string,
  from: number,
  size: number
): Promise<{ items: Feedback[] }> => {
  const response = await api.get(`/feedback/product/${id}`, {
    params: { from, size },
  });
  return {
    items: response.data,
  };
};

export const getProductShops = async (
  id: string,
  from: number,
  size: number
): Promise<{ items: ShopProduct[] }> => {
  const response = await api.get(`/product/${id}/shops`, {
    params: { from, size },
  });

  return {
    items: response.data.shops,
  };
};

export const checkProductLiked = async (
  productId: string
): Promise<boolean> => {
  const response = await api.get(`/user/products`);
  return response.data.some(
    (product: Product) => product.product.id === Number(productId)
  );
};

export const likeProduct = async (productId: string): Promise<boolean> => {
  const response = await api.post(`/user/product`, {
    productId: Number(productId),
  });
  return response.data;
};

export const unlikeProduct = async (productId: string): Promise<boolean> => {
  const response = await api.delete(`/user/product`, {
    data: {
      productId: Number(productId),
    },
  });
  return response.data;
};

export const getArticleDetails = async (id: string): Promise<Article> => {
  const response = await api.get(`/article/id/${id}`);
  return response.data;
};

export const getArticleFeedbacks = async (
  id: string,
  from: number,
  size: number
): Promise<{ items: Feedback[] }> => {
  const response = await api.get(`/feedback/article/${id}`, {
    params: { from, size },
  });
  return {
    items: response.data,
  };
};

export const addArticleFeedback = async (
  articleId: string,
  text: string,
  stars: number
): Promise<boolean> => {
  const response = await api.post(`/feedback/article`, {
    articleId: Number(articleId),
    text,
    stars,
  });
  return response.data;
};

export const addProductFeedback = async (
  productId: string,
  text: string,
  stars: number
): Promise<boolean> => {
  const response = await api.post(`/feedback/product`, {
    productId: Number(productId),
    text,
    stars,
  });
  return response.data;
};

export const subscribeToMusician = async (
  musicianId: number
): Promise<boolean> => {
  const response = await api.post(`/musician/subscription`, {
    musicianId,
  });
  return response.data;
};

export const unsubscribeFromMusician = async (
  musicianId: number
): Promise<boolean> => {
  const response = await api.delete(`/musician/subscription`, {
    data: { musicianId: musicianId },
  });
  return response.data;
};

export const checkMusicianSubscribed = async (
  musicianId: number
): Promise<boolean> => {
  const response = await api.get(`/musician/${musicianId}/subscription`);
  return response.data;
};

export const getArticles = async (
  from: number,
  size: number,
  asc: boolean
): Promise<{ items: Article[] }> => {
  const response = await api.get("/article", {
    params: { from, size, sortBy: "CREATED_AT", ascending: asc },
  });
  return {
    items: response.data,
  };
};

export const searchArticlesByHeader = async (
  header: string,
  from: number,
  size: number
): Promise<{ items: Article[] }> => {
  const response = await api.get(`/article/header/${header}`, {
    params: { from, size },
  });
  return {
    items: response.data,
  };
};

export const getMusicians = async (
  from: number,
  size: number,
  sort: { field: string; direction: boolean }
): Promise<{ items: Musician[] }> => {
  const response = await api.get("/musician", {
    params: { from, size, sortBy: sort.field, ascending: sort.direction },
  });
  return {
    items: response.data,
  };
};

export const searchMusiciansByName = async (
  name: string,
  from: number,
  size: number
): Promise<{ items: Musician[] }> => {
  const response = await api.get(`/musician/name/${name}`, {
    params: { from, size },
  });
  return {
    items: response.data,
  };
};

export interface ForumTopic {
  id: number;
  title: string;
  content: string;
  author: string;
  createdAt: string;
  closed: boolean;
  postsCount: number;
}

export interface ForumPost {
  id: number;
  content: string;
  author: string;
  createdAt: string;
}

export const getForumTopics = async (
  from: number,
  size: number
): Promise<{ items: ForumTopic[] }> => {
  const response = await api.get("/forum/topic", {
    params: { from, size },
  });
  return {
    items: response.data,
  };
};

export const createForumTopic = async (
  title: string,
  content: string
): Promise<ForumTopic> => {
  const response = await api.post("/forum/topic", {
    title,
    content,
  });
  return response.data;
};

export const getTopicPosts = async (
  topicId: number,
  from: number,
  size: number
): Promise<{ items: ForumPost[] }> => {
  const response = await api.get(`/forum/topic/${topicId}/posts`, {
    params: { from, size },
  });
  return {
    items: response.data,
  };
};

export const createForumPost = async (
  topicId: number,
  content: string
): Promise<ForumPost> => {
  const response = await api.post("/forum/post", {
    topicId,
    content,
  });
  return response.data;
};

export const getProductsByBrand = async (
  brandId: number,
  from: number,
  size: number
) => {
  const response = await api.get("/product/filter", {
    params: {
      brandId: brandId,
      from: from,
      size: size,
      ascending: false,
      sortBy: "NAME",
    },
  });
  return response;
};

export default api;
