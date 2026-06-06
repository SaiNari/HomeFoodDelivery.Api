#!/usr/bin/env bash
set -u
export PATH="$PATH:$HOME/.dotnet/tools"
cd ~/codefood/HomeFoodDelivery/HomeFoodDelivery.Api

echo "=== Stopping any running API (frees the build output) ==="
pkill -f "dotnet run" 2>/dev/null
pkill -f "HomeFoodDelivery.Api" 2>/dev/null
sleep 2
echo "done"

echo "=== Ensuring dotnet-ef tool is installed ==="
if ! dotnet ef --version >/dev/null 2>&1; then
  dotnet tool install --global dotnet-ef
  export PATH="$PATH:$HOME/.dotnet/tools"
fi
dotnet ef --version

echo "=== Creating migration AddCookFssaiFields ==="
dotnet ef migrations add AddCookFssaiFields

echo "=== Applying migration to the database ==="
dotnet ef database update

echo "=== DONE ==="
