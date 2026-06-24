# tcg-search

## Local Services

Start the local database and object storage:

```bash
docker compose -f docker-compose.local.yml up -d postgres minio minio-create-bucket
```

Connect with `psql`:

```bash
psql "postgresql://tcg_search:tcg_search@localhost:5432/tcg_search"
```

If port `5432` is already in use, choose another host port:

```bash
POSTGRES_PORT=5433 docker compose -f docker-compose.local.yml up -d postgres
psql "postgresql://tcg_search:tcg_search@localhost:5433/tcg_search"
```

MinIO local defaults:

| Setting | Default |
| --- | --- |
| S3 endpoint | `http://localhost:9000` |
| Console | `http://localhost:9001` |
| Access key | `tcg_search` |
| Secret key | `tcg_search_minio` |
| Bucket | `tcg-search-local` |

Override MinIO ports or bucket name when needed:

```bash
MINIO_API_PORT=9010 MINIO_CONSOLE_PORT=9011 MINIO_BUCKET=tcg-search-dev \
  docker compose -f docker-compose.local.yml up -d minio minio-create-bucket
```

Stop local services:

```bash
docker compose -f docker-compose.local.yml down
```

Remove local database and bucket data:

```bash
docker compose -f docker-compose.local.yml down -v
```

PostgreSQL 18 stores data under a major-version-specific subdirectory, so the
Compose volume is mounted at `/var/lib/postgresql`.

Run backend tests:

```bash
./gradlew test
```
