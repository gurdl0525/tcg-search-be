# tcg-search

## Local PostgreSQL

Start the local database:

```bash
docker compose up -d postgres
```

Connect with `psql`:

```bash
psql "postgresql://tcg_search:tcg_search@localhost:5432/tcg_search"
```

If port `5432` is already in use, choose another host port:

```bash
POSTGRES_PORT=5433 docker compose up -d postgres
psql "postgresql://tcg_search:tcg_search@localhost:5433/tcg_search"
```

Stop the database:

```bash
docker compose down
```

Remove local database data:

```bash
docker compose down -v
```

PostgreSQL 18 stores data under a major-version-specific subdirectory, so the
Compose volume is mounted at `/var/lib/postgresql`.

Run backend tests:

```bash
./gradlew test
```
