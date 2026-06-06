#!/usr/bin/env bash
# Seeds a cook + a dish + a customer for manual app testing.
set -u
BASE=http://localhost:5287
JH='Content-Type: application/json'

echo "=== Register COOK (Bagmane / zone 1) ==="
COOK_RESP=$(curl -s -X POST "$BASE/api/Auth/register" -H "$JH" \
  -d '{"fullName":"Anita Rao","phoneNumber":"9000000001","userRole":"Cook","zoneId":1,"addressText":"Near Bagmane gate"}')
echo "$COOK_RESP"

COOK_ID=$(echo "$COOK_RESP" | grep -oP '"userId":\s*\K[0-9]+')
if [ -z "$COOK_ID" ]; then
  # Cook already existed — look it up by phone number.
  COOK_ID=$(curl -s "$BASE/api/users" \
    | grep -oP '\{[^}]*"phoneNumber":"9000000001"[^}]*\}' \
    | grep -oP '"userId":\s*\K[0-9]+')
fi
echo "COOK_ID=$COOK_ID"

echo
echo "=== Register CUSTOMER (Bagmane / zone 1) — log in as 9111111111 ==="
curl -s -X POST "$BASE/api/Auth/register" -H "$JH" \
  -d '{"fullName":"Bhanu","phoneNumber":"9111111111","userRole":"Customer","zoneId":1,"addressText":"Bagmane WTC 4th floor"}'
echo

echo
echo "=== Add a dish for the cook (today) ==="
TODAY=$(date -u +%Y-%m-%dT00:00:00Z)
curl -s -X POST "$BASE/api/DailyMenus" -H "$JH" \
  -d "{\"cookId\":$COOK_ID,\"shiftId\":2,\"dishName\":\"Veg Thali\",\"description\":\"Rice, dal, 2 sabzi, roti\",\"isVegetarian\":true,\"availablePortions\":20,\"pricePerPortion\":120,\"menuDate\":\"$TODAY\"}"
echo

echo
echo "=== Verify: kitchens visible to customers in zone 1 ==="
curl -s "$BASE/api/users/kitchens/zone/1"; echo
echo
echo "=== Verify: menu for cook $COOK_ID ==="
curl -s "$BASE/api/DailyMenus/kitchen/$COOK_ID"; echo
