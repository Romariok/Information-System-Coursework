#!/bin/sh
set -e

if [ -f "/opt/app/parser" ]; then
  chmod +x /opt/app/parser || true
fi

exec java -jar /opt/app/app.jar
