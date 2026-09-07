# syntax=docker/dockerfile:1
# ---------- 阶段 1:node 构建 ----------
FROM node:20-alpine AS builder
WORKDIR /build
COPY package*.json ./
# 走 npm ci,若没 package-lock.json 会回退到 install
RUN npm install --no-audit --no-fund
COPY . .
RUN npm run build

# ---------- 阶段 2:nginx 托管 ----------
FROM nginx:1.27-alpine
COPY --from=builder /build/dist /usr/share/nginx/html
# 直接覆盖主配置,避免默认 server 块与我们的冲突导致 80 端口重复绑定
RUN rm /etc/nginx/conf.d/default.conf
COPY nginx.conf /etc/nginx/nginx.conf
EXPOSE 80
