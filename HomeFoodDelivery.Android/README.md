# HomeFood — Android customer app

Native Android (Kotlin + Jetpack Compose) client for the existing
`HomeFoodDelivery.Api` backend. It implements the IT-employee ("customer") flow:

**Log in → pick your tech park → see nearby kitchens (sorted by distance) →
browse a cook's menu → add to cart → place order → track orders.**

Cooks continue to use the existing web dashboard (`wwwroot/cook/dashboard.html`)
for now — this app is customer-only by design (v1).

---

## 1. Architecture

```
app/src/main/java/com/homefood/delivery/
├── MainActivity.kt              # single-activity host
├── data/
│   ├── model/Models.kt          # data classes matching the API JSON
│   ├── remote/ApiService.kt     # Retrofit endpoints (1:1 with the controllers)
│   ├── remote/ApiClient.kt      # Retrofit/OkHttp setup + BASE_URL  ← edit this
│   ├── session/SessionManager.kt# remembers the logged-in userId (SharedPreferences)
│   └── cart/CartManager.kt      # in-memory cart shared across screens
└── ui/
    ├── theme/Theme.kt
    ├── navigation/AppNav.kt     # routes + NavHost
    ├── components/Common.kt     # loading / message helpers
    └── screens/                 # Login, Register, TechPark, Kitchens, Menu, Cart, Orders
```

Pattern: **MVVM**. Each screen has a small `ViewModel` that calls `ApiClient.service`
(Retrofit) and exposes `loading` / `error` / data via Compose state. No DI framework —
the Retrofit instance is a singleton, which keeps the project easy to read.

### Which API endpoint each screen uses
| Screen | Endpoint |
|--------|----------|
| Login | `POST /api/Auth/login` |
| Register | `GET /api/DeliveryZones`, `POST /api/Auth/register` |
| Tech parks | `GET /api/DeliveryZones` |
| Kitchens (nearby cooks) | `GET /api/users/kitchens/zone/{zoneId}` |
| Menu | `GET /api/DailyMenus/kitchen/{cookId}` |
| Cart / checkout | `POST /api/Orders/checkout` |
| Orders | `GET /api/Orders/customer/{customerId}` |

> **Note on "nearby cooks":** the backend sorts kitchens by distance from the
> *tech park's* coordinates (Haversine), not the phone's live GPS. So the flow is
> "pick your office → see cooks near it." Real device GPS can be added later.

> **Note on auth:** the backend currently has no token/password — login just looks
> up the phone number. The app stores `userId` locally. When the backend adds JWT,
> store the token in `SessionManager` and attach it via an OkHttp interceptor.

---

## 2. One-time setup (from zero)

1. **Install Android Studio** (latest stable, "Ladybug" or newer) from
   <https://developer.android.com/studio>. During first launch let it install the
   **Android SDK**, **SDK Platform 35**, and **Android Emulator**.
2. **JDK**: Android Studio bundles a JDK 17 — nothing extra to install.
3. **Create an emulator**: Android Studio → *Device Manager* → *Add a device* →
   pick e.g. **Pixel 7**, system image **API 34 or 35**, finish.

## 3. Open the project

- Android Studio → **Open** → select this folder
  (`HomeFoodDelivery/HomeFoodDelivery.Android`). **Do not** open the repo root — the
  `.NET` API and the Android app are separate projects.
- On first open, Android Studio reads `gradle/wrapper/gradle-wrapper.properties`,
  downloads **Gradle 8.11.1**, and syncs. If it asks to create the Gradle wrapper,
  accept. `local.properties` (your SDK path) is generated automatically.

> Tip: the repo lives under WSL. Opening a `\\wsl$\...` path in Android Studio for
> Windows works but file-watching can be slow. For the smoothest experience you can
> copy `HomeFoodDelivery.Android` to a Windows path (e.g. `C:\dev\`) while developing
> the app. The backend stays in WSL.

---

## 4. Run the backend so the app can reach it

The app talks HTTP to the API on **port 5287**. Run the **http** profile so there's
no HTTPS redirect to trip over:

```bash
# in WSL, from HomeFoodDelivery/HomeFoodDelivery.Api
dotnet run --launch-profile http
# API now serving http://localhost:5287  (Swagger at http://localhost:5287/swagger)
```

### Networking: how the emulator finds the API
| You are testing on | `BASE_URL` in `ApiClient.kt` |
|--------------------|------------------------------|
| **Android emulator** | `http://10.0.2.2:5287/`  ← default (10.0.2.2 = your PC from inside the emulator) |
| **Physical phone (USB/Wi-Fi)** | `http://<your-PC-LAN-IP>:5287/` e.g. `http://192.168.1.20:5287/` |

If `10.0.2.2:5287` can't connect (WSL2 sometimes doesn't forward localhost), run the
API bound to all interfaces and use your machine's IP instead:

```bash
dotnet run --urls "http://0.0.0.0:5287"          # bind all interfaces
ip addr | grep inet                               # find the WSL/host IP
```

Then set `BASE_URL` to that IP **and** add the IP to
`app/src/main/res/xml/network_security_config.xml` (cleartext is only allowed for
listed hosts). For a physical phone, the phone and PC must be on the same Wi-Fi, and
Windows Firewall must allow inbound 5287.

---

## 5. Seed some test data (so there's something to order)

The app is customer-only, so create a **cook + a menu** via Swagger
(`http://localhost:5287/swagger`) or curl. Pre-seeded tech parks:
`1 = Bagmane Tech Park`, `2 = Manyata`, `3 = RMZ Ecospace`, `4 = Electronic City P1`.
Meal shifts: `1 = Breakfast`, `2 = Lunch`, `3 = Dinner`.

```bash
# 1) Register a COOK in Bagmane Tech Park (zoneId 1)
curl -X POST http://localhost:5287/api/Auth/register -H "Content-Type: application/json" -d '{
  "fullName":"Anita Rao","phoneNumber":"9000000001","userRole":"Cook","zoneId":1,
  "addressText":"Near Bagmane gate"
}'
# -> note the returned userId; that is the cookId (say 1)

# 2) Add a dish for that cook (menuDate must be today or later)
curl -X POST http://localhost:5287/api/DailyMenus -H "Content-Type: application/json" -d '{
  "cookId":1,"shiftId":2,"dishName":"Veg Thali","description":"Rice, dal, 2 sabzi, roti",
  "isVegetarian":true,"availablePortions":20,"pricePerPortion":120,
  "menuDate":"2026-06-05T00:00:00Z"
}'

# 3) Register a CUSTOMER in the same tech park (this is who you log in as in the app)
curl -X POST http://localhost:5287/api/Auth/register -H "Content-Type: application/json" -d '{
  "fullName":"Bhanu","phoneNumber":"9111111111","userRole":"Customer","zoneId":1,
  "addressText":"Bagmane WTC, 4th floor"
}'
```

---

## 6. Run the app + manual test script

1. Start the emulator, then press **Run ▶** (app) in Android Studio.
2. **Log in** with the customer phone `9111111111`.
   - ✅ You land on **Kitchens near you** and see *Anita Rao's Kitchen*.
3. Tap the kitchen → **menu** shows *Veg Thali ₹120, 20 portions left*.
   - Tap **Add**, bump quantity to 2 → bottom bar shows *2 items · ₹240*.
4. Tap **View cart** → **Place order**.
   - ✅ Toast-free success → you're taken to **My orders** with one *Pending* order.
5. Verify on the backend: `GET http://localhost:5287/api/Orders` (Swagger) shows the
   order, and the dish's `availablePortions` dropped from 20 → 18.
6. Tap the 📍 icon to **change tech park**; tap **Log out** (top-right of My orders)
   to return to login.

### Edge cases worth checking
- Log in with an unregistered number → "Account not found. Please register."
- Register a brand-new customer in-app → auto-logged-in and routed to kitchens.
- Order more than `availablePortions` → backend rejects; app shows the sold-out error.
- Stop the API and open any screen → friendly "Cannot reach server" message.

---

## 7. Automated tests (optional next step)

- **Unit-test `CartManager`** (pure Kotlin: add/decrement, switching cooks clears cart)
  with JUnit in `app/src/test/`.
- **API-layer tests** with Retrofit + OkHttp `MockWebServer` to assert each
  `ApiService` call hits the right path and parses JSON.
- **UI tests** with Compose `createAndroidComposeRule` for the login → order happy path.

These aren't included in v1 to keep the scaffold small; the code is structured
(ViewModels + a testable `CartManager`) so they're easy to add.

---

## 8. Troubleshooting

| Symptom | Fix |
|---------|-----|
| Gradle sync fails on first open | Let it finish downloading Gradle 8.11.1; check internet/proxy. |
| `CLEARTEXT communication not permitted` | Host not in `network_security_config.xml` — add your IP. |
| `Failed to connect to /10.0.2.2:5287` | API not running, or wrong profile. Run `dotnet run --launch-profile http`. Try the `0.0.0.0` + LAN-IP fallback (section 4). |
| Kitchens list is empty | No cook registered in that `zoneId`, or cook has `UserRole != "Cook"`. |
| Menu is empty | Dish `menuDate` is in the past or `availablePortions = 0` (backend filters those out). |
| Distances all show `0.0 km` | Seeded tech parks/cooks have no lat/long yet; expected. Set coordinates to see real distances. |
