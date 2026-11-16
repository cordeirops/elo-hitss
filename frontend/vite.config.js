import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'

// Em desenvolvimento: `npm run dev` (porta 5173) com proxy de /api para o Quarkus (porta 8080).
// Em producao/demo: `npm run build` gera o front direto em META-INF/resources do backend,
// e o Quarkus passa a servir tudo em http://localhost:8080.
export default defineConfig({
  base: './',
  plugins: [vue()],
  server: {
    port: 5173,
    proxy: { '/api': 'http://localhost:8080' }
  },
  build: {
    // Na Vercel (variavel VERCEL=1) o build sai em dist/; localmente vai para dentro do backend.
    outDir: process.env.VERCEL ? 'dist' : '../backend/src/main/resources/META-INF/resources',
    emptyOutDir: true
  }
})
