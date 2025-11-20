#!/bin/sh
set -e

if [ -f "/opt/app/parser.exe" ]; then
  chmod +x /opt/app/parser.exe || true
fi

exec java -jar /opt/app/app.jar


