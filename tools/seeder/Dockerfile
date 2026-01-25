FROM python:3.12-slim

WORKDIR /app

# Install uv (fast Python package manager / runner)
RUN pip install --no-cache-dir uv

# Copy lockfiles first for better layer caching
COPY tools/seeder/pyproject.toml /app/pyproject.toml
COPY tools/seeder/uv.lock /app/uv.lock

# Create the project venv and install deps
ENV UV_PROJECT_ENVIRONMENT=/app/.venv
RUN uv sync --frozen --no-dev

# Copy seeder sources
COPY tools/seeder /app

# Defaults (can be overridden via docker-compose env / command args)
ENV BASE_URL=http://app:8080
ENV ADMIN_USERNAME=admin
ENV ADMIN_PASSWORD=admin
ENV USER_PASSWORD=password

CMD ["uv", "run", "-m", "seeder"]

