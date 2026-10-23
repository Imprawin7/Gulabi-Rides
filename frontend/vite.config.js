import { defineConfig } from 'vite';
import react from '@vitejs/plugin-react';
// In dev, proxy the API and the STOMP socket to the Spring Boot server.
export default defineConfig({
  plugins: [react()],
  server: { proxy: { '/api': 'http://localhost:8080', '/ws': { target: 'http://localhost:8080', ws: true } } },
});
