# ---- Stage 1: Build Angular app ----
FROM node:20-alpine AS builder

WORKDIR /app

# Cache npm dependencies
COPY package*.json ./
RUN npm install --silent

# Build Angular production bundle
COPY . .
RUN npm run build

# ---- Stage 2: Serve with Nginx ----
FROM nginx:1.25-alpine

# Remove default Nginx config
RUN rm /etc/nginx/conf.d/default.conf

# Copy custom Nginx config
COPY nginx.conf /etc/nginx/nginx.conf

# Copy Angular build output (Angular 17 new builder outputs to dist/<name>/browser/)
COPY --from=builder /app/dist/bitcoin-testnet/browser /usr/share/nginx/html

EXPOSE 80

CMD ["nginx", "-g", "daemon off;"]
