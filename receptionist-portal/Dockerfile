# Build stage
FROM node:18-alpine AS build

WORKDIR /app

# Set environment variables to prevent browser opening and make build non-interactive
ENV CI=true
ENV BROWSER=none

# Copy package files
COPY package*.json ./

# Clear npm cache and install dependencies fresh (fixes rollup issue)
RUN npm cache clean --force
RUN rm -f package-lock.json
RUN npm install

# Copy source code
COPY . .

# Build the application with non-interactive mode
RUN npm run build

# Production stage
FROM nginx:alpine

# Copy built application from build stage
COPY --from=build /app/dist /usr/share/nginx/html

# Create nginx configuration for Vite/React Router
RUN echo 'server { \
    listen 3001; \
    server_name localhost; \
    root /usr/share/nginx/html; \
    index index.html; \
    location / { \
        try_files $uri $uri/ /index.html; \
    } \
}' > /etc/nginx/conf.d/default.conf

# Remove default nginx config
RUN rm /etc/nginx/conf.d/default.conf.bak 2>/dev/null || true

# Expose port 3001 (receptionist portal)
EXPOSE 3001

# Start nginx
CMD ["nginx", "-g", "daemon off;"] 