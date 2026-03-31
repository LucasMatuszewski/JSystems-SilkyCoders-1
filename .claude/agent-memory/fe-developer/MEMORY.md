# FE Developer Agent Memory

## Project: Sinsay AI PoC

### Stack
- React 19, TypeScript strict, Vite 8, Tailwind CSS v4, Shadcn/ui, assistant-ui, Vercel AI SDK v6
- Package manager: npm

### Key File Paths
- Frontend root: `frontend/`
- Design tokens: `assets/design-tokens.json`
- Assets: `assets/logo.svg`, `assets/sinsay-favicon.ico`
- ADR: `docs/ADR/002-frontend.md`, `frontend/AGENTS.md`

### Tailwind v4 Setup
- Use `@tailwindcss/vite` plugin (not PostCSS config)
- Import via `@import "tailwindcss"` in CSS (not @tailwind directives)
- Custom tokens via `@theme {}` block in `src/index.css`
- Prettier normalizes hex colors to lowercase in CSS

### Vitest Setup
- Separate `vitest.config.ts` (not embedded in vite.config.ts)
- Needs `setupFiles: ['./src/test-setup.ts']` with `@testing-library/jest-dom` import
- Environment: `jsdom`, globals: true

### Prettier
- Config: `.prettierrc` — semi:false, singleQuote:true
- `.prettierignore` must exclude `*.md` (AGENTS.md/CLAUDE.md have inconsistent formatting)
- Auto-fix scaffold files before first format:check run

### TypeScript
- `JSX.Element` needs `import React from 'react'` or use `React.JSX.Element`
- tsconfig.app.json `include: ["src"]` covers test files in src/ automatically

### Shadcn/ui Init
- Command: `npx shadcn@latest init -t vite` (supports Tailwind v4)
- Path alias `@` -> `./src` required in both vite.config.ts and vitest.config.ts

### Vite Config Pattern
- Proxy: `server.proxy: { '/api': 'http://localhost:8080' }`
- Build: `outDir: '../backend/src/main/resources/static', emptyOutDir: true`
