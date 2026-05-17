import type { Config } from "tailwindcss";

const config: Config = {
  darkMode: ["class"],
  content: ["./src/**/*.{ts,tsx}"],
  theme: {
    extend: {
      fontFamily: {
        heading: ["'Space Grotesk'", "sans-serif"],
        body: ["'IBM Plex Sans'", "sans-serif"]
      },
      colors: {
        panel: "#101722",
        accent: "#1ecbe1",
        danger: "#ff5d5d",
        warn: "#f1c74a",
        ok: "#4ecb71"
      }
    }
  },
  plugins: []
};

export default config;
