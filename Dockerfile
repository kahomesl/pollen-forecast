FROM oven/bun:1.4.0 as builder
WORKDIR /app

# Build frontend
COPY frontend/package.json frontend/bun.lock* ./frontend/
RUN cd frontend && bun install --frozen-lockfile
COPY frontend/ ./frontend/
RUN cd frontend && bun run build

# Build backend
COPY package.json bun.lock* ./
RUN bun install --frozen-lockfile
COPY backend/ ./backend/

FROM oven/bun:1.4.0-alpine
WORKDIR /app
COPY --from=builder /app/package.json /app/bun.lock* ./
RUN bun install --production --frozen-lockfile
COPY --from=builder /app/backend/ ./backend/
COPY --from=builder /app/frontend/dist/ ./frontend/dist/

USER bun
EXPOSE 8080
HEALTHCHECK --interval=30s --timeout=5s --start-period=20s --retries=3 CMD bun -e "fetch('http://127.0.0.1:8080/health').then((response) => process.exit(response.ok ? 0 : 1)).catch(() => process.exit(1))"
CMD ["bun", "run", "backend/src/index.ts"]
