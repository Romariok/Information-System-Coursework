export type Brand = {
  id: number;
  name: string;
  country: string;
  website: string;
  email: string;
};

export type Product = {
  product: {
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
  };
  genres: [string];
};

export type ProductSimple = {
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
};

export type Article = {
  id: number;
  header: string;
  text: string;
  author: Author;
  createdAt: string;
  accepted: boolean;
};

export type Author = {
  id: number;
  username: string;
}

export type Musician = {
  id: number;
  name: string;
  subscribers: number;
};

export type Feedback = {
  id: number;
  author: Author;
  product: ProductSimple;
  article: string;
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
