import { fileURLToPath, URL } from "node:url";

import { defineConfig } from "vite";
import vue from "@vitejs/plugin-vue";
import vueDevTools from "vite-plugin-vue-devtools";

// https://vite.dev/config/
export default defineConfig({
  plugins: [vue(), vueDevTools()],
  resolve: {
    alias: {
      "@": fileURLToPath(new URL("./src", import.meta.url)),
    },
  },
  define: {
    global: "window",
  },

  server: {
    proxy: {
      "/api-address": {
        target: "https://provinces.open-api.vn",
        changeOrigin: true,
        secure: false,
        rewrite: (path) => path.replace(/^\/api-address/, ""),
      },
    },
  },
});
// import { fileURLToPath, URL } from "node:url";

// import { defineConfig } from "vite";
// import vue from "@vitejs/plugin-vue";

// // https://vite.dev/config/
// export default defineConfig({
//   plugins: [vue()],
//   resolve: {
//     alias: {
//       "@": fileURLToPath(new URL("./src", import.meta.url)),
//     },
//   },
//   define: {
//     global: "window",
//   },

//   server: {
//     proxy: {
//       "/api-address": {
//         target: "https://provinces.open-api.vn",
//         changeOrigin: true,
//         secure: false,
//         rewrite: (path) => path.replace(/^\/api-address/, ""),
//       },
//     },
//   },
// });
