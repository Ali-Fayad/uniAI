#!/usr/bin/env sh
# ─────────────────────────────────────────────────────────────
#  generate.sh – self-signed TLS cert generator for local dev
#
#  Usage:
#    cd <repo-root>
#    sh nginx/certs/generate.sh
#
#  Idempotent: skips generation if both files already exist.
# ─────────────────────────────────────────────────────────────
set -e

CERT_DIR="$(cd "$(dirname "$0")" && pwd)"

if [ -f "$CERT_DIR/fullchain.pem" ] && [ -f "$CERT_DIR/privkey.pem" ]; then
  echo "[certs] Certificates already exist in $CERT_DIR – skipping."
  exit 0
fi

echo "[certs] Generating self-signed certificate in $CERT_DIR …"
openssl req -x509 -nodes -days 365 -newkey rsa:2048 \
  -keyout "$CERT_DIR/privkey.pem" \
  -out    "$CERT_DIR/fullchain.pem" \
  -subj   "/C=LB/ST=Beirut/L=Beirut/O=uniAI/CN=localhost"

echo "[certs] Done.  privkey.pem + fullchain.pem created."
