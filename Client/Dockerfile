# ── Stage 1: base ─────────────────────────────────────────────
# Shared foundation: installs deps once, all stages reuse it.
FROM node:20-alpine AS base
WORKDIR /app
COPY package.json package-lock.json* ./

# ── Stage 2: development ───────────────────────────────────────
# Hot-reload Vite dev server. Source is mounted via compose volume.
FROM base AS dev
RUN npm install
COPY . .
EXPOSE 5173
CMD ["npm", "run", "dev", "--", "--host", "0.0.0.0"]

# ── Stage 3: builder ───────────────────────────────────────────
# Produces /app/dist for nginx or the client_dist named volume.
FROM base AS builder
RUN npm ci
COPY . .
RUN npm run build
CMD ["true"]

# ── Stage 4: production ────────────────────────────────────────
# Minimal nginx:alpine (~5 MB) serving the built static files.
FROM nginx:alpine AS production
COPY --from=builder /app/dist /usr/share/nginx/html
EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]
