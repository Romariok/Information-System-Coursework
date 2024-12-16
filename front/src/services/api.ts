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
      size: 5,
      minPrice: 0,
      maxPrice: 100000,
    },
  });
  return response.data;
};

export const getTopArticles = async () => {
  const response = await api.get("/article", {
    params: {
      from: 0,
      size: 5,
    },
  });
  return response.data;
};

export const getTopMusicians = async () => {
  const response = await api.get("/musician", {
    params: {
      from: 0,
      size: 5,
    },
  });
  return response.data;
};

export const getProductDetails = async (id: string): Promise<Product> => {
  const response = await api.get(`/product/id`, {
    params: {
      id: id,
    },
  });
  return response.data;
};

export const getProductArticles = async (
  id: string,
  from: number,
  size: number
): Promise<{ items: Article[]; total: number }> => {
  const response = await api.get(`/product/${id}/articles`, {
    params: { from, size },
  });
  return {
    items: response.data.items,
    total: response.data.total,
  };
};

export const getProductMusicians = async (
  id: string,
  from: number,
  size: number
): Promise<{ items: Musician[]; total: number }> => {
  const response = await api.get(`/product/${id}/musicians`, {
    params: { from, size },
  });
  return {
    items: response.data.items,
    total: response.data.total,
  };
};

export const getProductFeedbacks = async (
  id: string, 
  from: number, 
  size: number
): Promise<{ items: Feedback[], total: number }> => {
  const response = await api.get(`/feedback/product/${id}`, {
    params: { from, size },
  });
  return {
    items: response.data.items,
    total: response.data.total,
  };
};

export const getProductShops = async (
  id: string,
  from: number,
  size: number
): Promise<{ items: ShopProduct[]; total: number }> => {
  const response = await api.get(`/product/${id}/shops`, {
    params: { from, size },
  });
  return {
    items: response.data.items,
    total: response.data.total,
  };
};

export default api;
