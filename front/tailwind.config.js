/** @type {import('tailwindcss').Config} */
export default {
  content: ["./index.html", "./src/**/*.{js,ts,jsx,tsx}"],
  theme: {
    extend: {},
  },
  plugins: [],
  extend: {
    keyframes: {
      slideDown: {
        "0%": { maxHeight: "0", opacity: "0" },
        "100%": { maxHeight: "500px", opacity: "1" },
      },
    },
    animation: {
      slideDown: "slideDown 0.3s ease-out",
    },
  },
};
