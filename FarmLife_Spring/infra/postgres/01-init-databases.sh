#!/bin/bash
set -e

DATABASES=("identity_db" "world_db" "economy_db" "social_quest_db" "analytics_db" "media_db" "ai_db")

for db in "${DATABASES[@]}"; do
  echo ">>> Initializing database: $db"
  psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" <<-EOSQL
    CREATE DATABASE $db;
    GRANT ALL PRIVILEGES ON DATABASE $db TO $POSTGRES_USER;
EOSQL

  psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" --dbname "$db" <<-EOSQL
    CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
    CREATE EXTENSION IF NOT EXISTS "pgcrypto";
    CREATE EXTENSION IF NOT EXISTS "citext";
    CREATE EXTENSION IF NOT EXISTS "btree_gist";
EOSQL
done

echo ">>> Enabling pgvector extension on ai_db"
psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" --dbname "ai_db" <<-EOSQL
  CREATE EXTENSION IF NOT EXISTS "vector";
EOSQL

echo ">>> All 7 databases initialized successfully!"