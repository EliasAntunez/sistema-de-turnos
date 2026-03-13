# =============================================================================
# STAGE 1 — BUILD
# Node 22 Alpine para compilar el proyecto Vue con Vite
# =============================================================================
FROM node:22-alpine AS builder

WORKDIR /app

# Copiar descriptores de dependencias primero (cache de Docker)
COPY package.json package-lock.json* ./
RUN npm ci --prefer-offline

# Copiar código fuente y construir para producción
COPY . .

# VITE_API_URL no es necesaria: Nginx actúa como proxy inverso hacia /api
# Si tu proyecto usa import.meta.env.VITE_*, pásalas aquí como ARG/ENV
RUN npm run build

# =============================================================================
# STAGE 2 — RUNTIME
# Nginx Alpine (~25 MB) para servir los assets estáticos
# =============================================================================
FROM nginx:1.27-alpine AS runtime

# Eliminar la configuración default de Nginx
RUN rm /etc/nginx/conf.d/default.conf

# Copiar nuestra configuración personalizada (History Mode + proxy)
COPY nginx.conf /etc/nginx/conf.d/app.conf

# Copiar los artefactos de producción generados por Vite
COPY --from=builder /app/dist /usr/share/nginx/html

# Nginx escucha en el puerto 80
EXPOSE 80

CMD ["nginx", "-g", "daemon off;"]
