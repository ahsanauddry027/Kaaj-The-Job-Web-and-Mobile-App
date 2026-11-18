#!/bin/bash
# Recreates the scratch test database and applies the migrations in Flyway version order.
set -e
DB=${1:-kaaj_test}
sudo -u postgres psql -q -c "DROP DATABASE IF EXISTS $DB" -c "CREATE DATABASE $DB" 2>/dev/null
for f in $(ls /home/claude/kaaj/src/main/resources/db/migration/V*.sql | sort -t V -k2 -n); do
  echo "== $(basename $f)"; sudo -u postgres psql -q -v ON_ERROR_STOP=1 -d $DB -f "$f"
done
