#!/usr/bin/env bash
set -euo pipefail

BASE_URL=${1:-http://localhost:5252}
REDIS_CLI="redis-cli -h localhost -p 6379"
N=10

S1_NO_CACHE="" S1_WITH_CACHE="" S1_SPEEDUP=""
S2_NO_CACHE="" S2_WITH_CACHE="" S2_SPEEDUP=""
S3_NO_CACHE="" S3_WITH_CACHE="" S3_SPEEDUP=""

run_scenario() {
  local label="$1"
  local url="$2"
  local var_prefix="$3"

  echo "==> $label"

  # Warm up JVM and DB connection pool, then clear cache
  $REDIS_CLI FLUSHDB > /dev/null
  curl -o /dev/null -s "$url"
  $REDIS_CLI FLUSHDB > /dev/null

  # Measure avg latency without cache: flush before each request to force DB hit
  local total=0
  for ((i = 0; i < N; i++)); do
    $REDIS_CLI FLUSHDB > /dev/null
    local t
    t=$(curl -o /dev/null -s -w "%{time_total}" "$url")
    local ms
    ms=$(echo "$t * 1000" | bc)
    total=$(echo "$total + $ms" | bc)
  done
  local avg_no_cache
  avg_no_cache=$(echo "scale=2; $total / $N" | bc)

  # Warm up the cache with one priming request
  curl -o /dev/null -s "$url"

  # Measure avg latency with cache: all requests served from Redis
  total=0
  for ((i = 0; i < N; i++)); do
    local t
    t=$(curl -o /dev/null -s -w "%{time_total}" "$url")
    local ms
    ms=$(echo "$t * 1000" | bc)
    total=$(echo "$total + $ms" | bc)
  done
  local avg_with_cache
  avg_with_cache=$(echo "scale=2; $total / $N" | bc)

  local speedup
  speedup=$(echo "scale=4; x=($avg_no_cache - $avg_with_cache) / $avg_no_cache * 100; scale=1; x/1" | bc -l)

  eval "${var_prefix}_NO_CACHE=\"$avg_no_cache\""
  eval "${var_prefix}_WITH_CACHE=\"$avg_with_cache\""
  eval "${var_prefix}_SPEEDUP=\"$speedup\""

  echo "   no-cache avg: ${avg_no_cache} ms | with-cache avg: ${avg_with_cache} ms | speedup: ${speedup}%"
}

run_scenario \
  "Фильтрация продуктов" \
  "$BASE_URL/api/product/filter?typeOfProduct=ELECTRIC_GUITAR&minPrice=0&maxPrice=999999&from=0&size=20&sortBy=RATE&ascending=false" \
  "S1"

run_scenario \
  "Список музыкантов" \
  "$BASE_URL/api/musician?from=0&size=20&sortBy=SUBSCRIBERS&ascending=false" \
  "S2"

run_scenario \
  "Список статей" \
  "$BASE_URL/api/article?from=0&size=20&sortBy=CREATED_AT&ascending=false" \
  "S3"

echo ""
echo "| Сценарий | Без кэша (avg, мс) | С кэшем (avg, мс) | Ускорение |"
echo "|---|---|---|---|"
echo "| Фильтрация продуктов | ${S1_NO_CACHE} | ${S1_WITH_CACHE} | ${S1_SPEEDUP}% |"
echo "| Список музыкантов    | ${S2_NO_CACHE} | ${S2_WITH_CACHE} | ${S2_SPEEDUP}% |"
echo "| Список статей        | ${S3_NO_CACHE} | ${S3_WITH_CACHE} | ${S3_SPEEDUP}% |"
