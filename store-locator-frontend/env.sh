#!/bin/sh

# Generate env-config.js file with environment variables
cat <<EOF > /usr/share/nginx/html/env-config.js
window._env_ = {
  REACT_APP_API_BASE_URL: "${API_BASE_URL:-http://localhost:8080/api/v1}"
};
EOF
