import { defineConfig } from 'vitest/config';
import react from '@vitejs/plugin-react';

export default defineConfig({
  plugins: [react()],
  test: {
    globals: true,
    environment: 'jsdom',
    setupFiles: ['./src/test/setup.ts'],
    // Exclude Playwright E2E tests — they are run via `pnpm e2e`
    exclude: ['**/e2e/**', '**/node_modules/**'],
  },
});
