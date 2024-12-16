export type Brand = {
  id: number;
  name: string;
  country: string;
  website: string;
  email: string;
};

export type Product = {
  id: number;
  name: string;
  description: string;
  rate: number;
  brand: Brand;
  guitarForm: string;
  typeOfProduct: string;
  lads: number;
  avgPrice: number;
  color: string;
  strings?: number;
  tipMaterial: string;
  bodyMaterial: string;
  pickupConfiguration: string;
  typeComboAmplifier: string;
  genre: [string];
};

export type Article = {
  id: number;
  header: string;
  text: string;
  author: string;
  createdAt: string;
  accepted: boolean;
};

export type Musician = {
  id: number;
  name: string;
  subscribers: number;
};

export type Feedback = {
  id: number;
  author: string;
  stars: number;
  text: string;
  createdAt: string;
};

export type ShopProduct = {
  id: number;
  name: string;
  address: string;
  website: string;
  price: number;
};

export type Shop = {
  id: number;
  name: string;
  address: string;
  website: string;
};
