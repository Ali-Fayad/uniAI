FROM nginx:alpine
COPY default.conf /etc/nginx/conf.d/default.conf
# certs provided via compose volume (nginx/certs/)
# nginx:alpine is ~5 MB vs ~237 MB for the full image
