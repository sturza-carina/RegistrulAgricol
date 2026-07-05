# Build stage
FROM node:20-alpine AS build
WORKDIR /app
COPY package.json package-lock.json ./
RUN npm install --legacy-peer-deps
COPY . .
# GOOGLE_MAPS_API_KEY is a client-side key embedded in the public JS bundle by design
# (Google Maps requires it in-browser); its only real protection is the HTTP referrer
# restriction configured in Google Cloud Console, not secrecy of this build layer.
ARG GOOGLE_MAPS_API_KEY
ENV GOOGLE_MAPS_API_KEY=$GOOGLE_MAPS_API_KEY
RUN npm run build --configuration=production

# Serve stage
FROM nginx:alpine
COPY --from=build /app/dist/frontend/browser /usr/share/nginx/html
# Custom nginx config if routing is needed, but default is fine for SPA routing
COPY nginx.conf /etc/nginx/conf.d/default.conf
EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]
