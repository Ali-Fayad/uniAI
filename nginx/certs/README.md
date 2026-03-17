# nginx / certs

Self-signed TLS certificates for local development.

## Generating certificates

Run **once** from the repo root before your first `docker compose up`:

```bash
sh nginx/certs/generate.sh
```

This creates:

| File | Description |
|------|-------------|
| `fullchain.pem` | Self-signed certificate (nginx `ssl_certificate`) |
| `privkey.pem`   | Private key (nginx `ssl_certificate_key`) |

The compose file mounts this directory into the nginx container as read-only:
```yaml
volumes:
  - ./nginx/certs:/etc/nginx/certs:ro
```

## Renewing / resetting

Simply delete the two `.pem` files and run `generate.sh` again.

## Production

Replace the generated files with real certificates from Let's Encrypt or your CA
(same filenames – no compose changes needed).
