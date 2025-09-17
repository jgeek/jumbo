# Build stage
FROM node:18-alpine AS build

WORKDIR /app

# Copy package files
COPY package*.json ./

# Install dependencies
RUN npm ci --only=production

# Copy source code
COPY . .

# Build the application
RUN npm run build

# Production stage
FROM nginx:alpine

# Copy custom nginx config
COPY nginx.conf /etc/nginx/conf.d/default.conf

# Copy built app
COPY --from=build /app/build /usr/share/nginx/html

# Copy env script
COPY env.sh /docker-entrypoint.d/

# Make script executable
RUN chmod +x /docker-entrypoint.d/env.sh

# Expose port
EXPOSE 80

CMD ["nginx", "-g", "daemon off;"]
