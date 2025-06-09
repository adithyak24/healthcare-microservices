# Build stage
FROM node:18-alpine AS build

WORKDIR /app

# Set environment variables to prevent browser opening and make build non-interactive
ENV CI=true
ENV BROWSER=none

# Copy package files
COPY package*.json ./

# Clear npm cache and install dependencies fresh (prevents build issues)
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
COPY --from=build /app/build /usr/share/nginx/html

# Create nginx configuration for React Router on port 3000
RUN echo 'server { \
    listen 3000; \
    server_name localhost; \
    root /usr/share/nginx/html; \
    index index.html; \
    location / { \
        try_files $uri $uri/ /index.html; \
    } \
}' > /etc/nginx/conf.d/default.conf

# Copy any existing nginx configuration
COPY nginx.conf /etc/nginx/nginx.conf

# Expose port 3000 (patient portal)
EXPOSE 3000

# Start nginx
CMD ["nginx", "-g", "daemon off;"] 