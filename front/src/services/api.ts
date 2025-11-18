import axios from "axios";
import type {
  Product,
  Article,
  Musician,
  Feedback,
  ShopProduct,
  Genre,
  TypeOfMusician,
  ForumTopic,
  ForumPost,
  ProductSimple,
} from "./types";

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

export type AuthUser = {
  id: number;
  username: string;
  token: string;
  isAdmin: boolean;
};

export const login = async (
  username: string,
  password: string
): Promise<AuthUser> => {
  const response = await api.post<AuthUser>("/auth/login", {
    username,
    password,
  });
  return response.data;
};

export const register = async (
  username: string,
  password: string
): Promise<AuthUser> => {
  const response = await api.post<AuthUser>("/auth/register", {
    username,
    password,
  });
  return response.data;
};

export const getTopProducts = async (): Promise<ProductSimple[]> => {
  const response = await api.get<ProductSimple[]>("/product/filter", {
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

export const getTopArticles = async (): Promise<Article[]> => {
  const response = await api.get<Article[]>("/article", {
    params: {
      from: 0,
      size: 6,
      sortBy: "CREATED_AT",
      ascending: false,
    },
  });
  return response.data;
};

export const getTopMusicians = async (): Promise<Musician[]> => {
  const response = await api.get<Musician[]>("/musician", {
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
  const response = await api.get<Product>(`/product/id/${id}`);
  return response.data;
};

export const getProductArticles = async (
  id: string,
  from: number,
  size: number
): Promise<{ items: Article[] }> => {
  const response = await api.get<{ articles: Article[] }>(`/product/${id}/articles`, {
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
  const response = await api.get<Musician[]>(`/product/${id}/musicians`, {
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
  const response = await api.get<Feedback[]>(`/feedback/product/${id}`, {
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
  const response = await api.get<{ shops: ShopProduct[] }>(`/product/${id}/shops`, {
    params: { from, size },
  });

  return {
    items: response.data.shops,
  };
};

export const checkProductLiked = async (
  productId: string
): Promise<boolean> => {
  const response = await api.get<ProductSimple[]>(`/user/products`);
  return response.data.some((product) => product.id === Number(productId));
};

export const likeProduct = async (productId: string): Promise<boolean> => {
  const response = await api.post<boolean>(`/user/product`, {
    productId: Number(productId),
  });
  return response.data;
};

export const unlikeProduct = async (productId: string): Promise<boolean> => {
  const response = await api.delete<boolean>(`/user/product`, {
    data: {
      productId: Number(productId),
    },
  });
  return response.data;
};

export const getArticleDetails = async (id: string): Promise<Article> => {
  const response = await api.get<Article>(`/article/id/${id}`);
  return response.data;
};

export const getArticleFeedbacks = async (
  id: string,
  from: number,
  size: number
): Promise<{ items: Feedback[] }> => {
  const response = await api.get<Feedback[]>(`/feedback/article/${id}`, {
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
  const response = await api.post<boolean>(`/feedback/article`, {
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
  const response = await api.post<boolean>(`/feedback/product`, {
    productId: Number(productId),
    text,
    stars,
  });
  return response.data;
};

export const subscribeToMusician = async (
  musicianId: number
): Promise<boolean> => {
  const response = await api.post<boolean>(`/musician/subscription`, {
    musicianId,
  });
  return response.data;
};

export const unsubscribeFromMusician = async (
  musicianId: number
): Promise<boolean> => {
  const response = await api.delete<boolean>(`/musician/subscription`, {
    data: { musicianId: musicianId },
  });
  return response.data;
};

export const checkMusicianSubscribed = async (
  musicianId: number
): Promise<boolean> => {
  const response = await api.get<boolean>(`/musician/${musicianId}/subscription`);
  return response.data;
};

export const getArticles = async (
  from: number,
  size: number,
  asc: boolean
): Promise<{ items: Article[] }> => {
  const response = await api.get<Article[]>("/article", {
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
  const response = await api.get<Article[]>(`/article/header/${header}`, {
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
  const response = await api.get<Musician[]>("/musician", {
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
  const response = await api.get<Musician[]>(`/musician/name/${name}`, {
    params: { from, size },
  });
  return {
    items: response.data,
  };
};

export const getForumTopics = async (
  from: number,
  size: number
): Promise<{ items: ForumTopic[] }> => {
  const response = await api.get<ForumTopic[]>("/forum/topic", {
    params: { from, size },
  });
  return {
    items: response.data,
  };
};

export const createForumTopic = async (
  title: string,
  description: string
): Promise<ForumTopic> => {
  const response = await api.post<ForumTopic>("/forum/topic", {
    title,
    description,
  });
  return response.data;
};

export const getTopicPosts = async (
  topicId: number,
  from: number,
  size: number
): Promise<{ items: ForumPost[]; topic: ForumTopic }> => {
  const response = await api.get<ForumPost[]>(`/forum/topic/${topicId}/posts`, {
    params: { from, size },
  });
  const top = await getForumTopicById(topicId);
  return {
    items: response.data,
    topic: top,
  };
};

export const createForumPost = async (
  topicId: number,
  content: string
): Promise<ForumPost> => {
  const response = await api.post<ForumPost>("/forum/post", {
    forumTopicId: topicId,
    content,
  });
  return response.data;
};

export const closeForumTopic = async (topicId: number): Promise<boolean> => {
  const response = await api.put<boolean>(`/forum/topic/${topicId}/close`);
  return response.data;
};

export const getProductsByBrand = async (
  brandId: number,
  from: number,
  size: number
): Promise<ProductSimple[]> => {
  const response = await api.get<ProductSimple[]>("/product/filter", {
    params: {
      brandId: brandId,
      from: from,
      size: size,
      ascending: false,
      sortBy: "NAME",
    },
  });
  return response.data;
};

export const getUserMusicians = async (): Promise<Musician[]> => {
  const response = await api.get<Musician[]>("/user/subscribed");
  return response.data;
};

export const getUserGenres = async (): Promise<Genre[]> => {
  const response = await api.get<Genre[]>("/user/genres");
  return response.data;
};
export const getUserTypes = async (): Promise<TypeOfMusician[]> => {
  const response = await api.get<TypeOfMusician[]>("/user/types");
  return response.data;
};

export type MusicianInfo = {
  id: number;
  name: string;
  subscribers: number;
  genres?: Genre[];
  typesOfMusicians?: TypeOfMusician[];
  products?: ProductSimple[];
};

export const getMusicianInfo = async (id: string): Promise<MusicianInfo> => {
  const response = await api.get<MusicianInfo>(`/musician/id/${id}`);
  return response.data;
};

export const isTopicOwner = async (topicId: number): Promise<boolean> => {
  const response = await api.get<boolean>(`/forum/topic/${topicId}/is-owner`);
  return response.data;
};

export const getForumTopicById = async (
  topicId: number
): Promise<ForumTopic> => {
  const response = await api.get<ForumTopic>(`/forum/topic/${topicId}`);
  return response.data;
};

export const createMusician = async (
  name: string,
  genres: Genre[],
  typesOfMusician: TypeOfMusician[]
): Promise<string> => {
  const response = await api.post<string>("/musician", {
    name,
    genres,
    typesOfMusician,
  });
  return response.data;
};

export const addProductToMusician = async (
  musicianName: string,
  productId: number
): Promise<boolean> => {
  const response = await api.post<boolean>("/musician/product", {
    musicianName,
    productId,
  });
  return response.data;
};

export const searchProducts = async (
  name: string,
  from: number = 0,
  size: number = 5
): Promise<ProductSimple[]> => {
  const response = await api.get<Product[]>(`/product/${name}`, {
    params: { from, size },
  });
  return response.data.map((product: Product) => product.product);
};

export const getUnacceptedArticles = async (
  from: number,
  size: number
): Promise<{ items: { article: Article; product: ProductSimple }[] }> => {
  const response = await api.get<
    { article: Article; product: ProductSimple }[]
  >("/article/unaccepted", {
    params: { from, size },
  });
  return {
    items: response.data,
  };
};

export const moderateArticle = async (
  articleId: number,
  accepted: boolean
): Promise<boolean> => {
  const response = await api.post<boolean>("/article/moderate", {
    articleId,
    accepted,
  });
  return response.data;
};

export const createArticle = async (
  productName: string,
  header: string,
  text: string
): Promise<Article> => {
  const response = await api.post<Article>("/article", {
    productName,
    header,
    text,
  });
  return response.data;
};

export default api;
