# syntax=docker/dockerfile:1

# --- Build stage: bundle the React app ---
FROM node:22-alpine AS builder

# Set working directory inside builder
WORKDIR /app

# Copy package manifests and install dependencies via lockfile
COPY package*.json ./
RUN npm ci

# Copy the frontend source
COPY . .

# Build production assets
RUN npm run build

# --- Runtime stage: lightweight static server ---
FROM node:22-alpine

# Set working directory for runtime container
WORKDIR /app

# Install a small static server
RUN npm install -g serve

# Copy build output from builder stage
COPY --from=builder /app/dist ./dist

EXPOSE 4173

# Serve dist directory on port 4173
CMD ["serve", "-s", "dist", "-l", "4173"]
