# Product Catalog & Offline Cart

Android app that browses products from the [DummyJSON Products API](https://dummyjson.com/docs/products)
and keeps a shopping cart in a local Room database so it works fully offline.

## Setup / Build
1. Open the project in Android Studio (Ladybug or newer) with JDK 17.
2. Let Gradle sync, then run the `app` configuration on an emulator/device (minSdk 24).
3. No API key needed. Internet is only required to load products.

## Architecture
MVVM with a thin repository layer and unidirectional data flow (UI observes `StateFlow`s from ViewModels).

```
ui (Compose screens + ViewModels)  ->  data/repository  ->  data/remote (Retrofit)
                                                        ->  data/local  (Room)
```
Packages: `data/remote`, `data/local`, `data/repository`, `domain`, `ui/{products,detail,cart,components,navigation}`, `util`.

## Libraries
Jetpack Compose + Material 3, Navigation Compose, Lifecycle/ViewModel, Kotlin Coroutines & Flow,
Retrofit + OkHttp (+ logging interceptor), Gson, Room (KSP), Coil.

## Local storage approach
Cart is a single Room table (`cart_items`) keyed by `productId`. It stores title, price and thumbnail
snapshot alongside quantity, so the cart renders with no network. The cart screen observes a `Flow`
from the DAO; item count and total price are derived from that flow. Add/increment/decrement run as
Room transactions.

## Important design decisions
- Search is server-side (`/products/search`) with a 400 ms debounce and `collectLatest`, so stale requests are cancelled.
- Errors are mapped to friendly messages (no internet, timeout, HTTP error) with a Retry button.
- Decreasing quantity below 1 removes the item.
- Manual DI (`AppContainer`) instead of Hilt to keep the project small and readable.
- Cart badge on list/detail screens comes from an activity-scoped `CartViewModel`.

## Known limitations
- Product list is not paginated (first 30 products only).
- Product list/details are not cached; they need a network connection.
- Cart images rely on Coil's default cache when offline.
- Category endpoints exist in the API/repository but have no UI yet.
- No unit/UI tests yet.
