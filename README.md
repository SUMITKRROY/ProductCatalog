# Product Catalog & Offline Cart

A modern Android product catalog and offline shopping cart application built with Kotlin, Jetpack Compose, Material 3, and Room. Developed as a practical assessment demonstrating clean architecture, reactive unidirectional data flow, and reliable offline-first capabilities.

---

## Table of Contents
- [Overview](#overview)
- [Features](#features)
- [Screens](#screens)
- [Tech Stack](#tech-stack)
- [API](#api)
- [Architecture](#architecture)
- [Project Structure](#project-structure)
- [Local Storage & Offline Support](#local-storage--offline-support)
- [Error Handling](#error-handling)
- [Setup & Installation](#setup--installation)
- [Build & Run](#build--run)
- [Testing Checklist](#testing-checklist)
- [Design Decisions](#design-decisions)
- [Known Limitations](#known-limitations)

---

## Overview

The **Product Catalog & Offline Cart** application allows users to explore a rich catalog of products fetched from the [DummyJSON API](https://dummyjson.com/), search items dynamically with debounce, view detailed product information, and manage an interactive shopping cart. 

The cart is engineered as an **offline-first local database** powered by Room SQLite, ensuring that adding, adjusting quantities, and managing cart items works completely without an internet connection.

---

## Features

### Product Catalog & Browsing
- **Product Listing**: Displays products in an adaptive 2-column grid with high-resolution thumbnails, title, price, category badge, rating pill, and stock status.
- **Product Search**: Server-side keyword search with a 400ms debounce and automatic cancellation of stale requests using Kotlin Coroutines `collectLatest`.
- **Product Details**: Comprehensive detail page showing an aspect-ratio-scaled hero image, category/brand pills, price, star rating, stock availability indicator, and full description.

### Interactive Cart Experience
- **Add to Cart**: Adds items to the Room database with an initial quantity of 1.
- **Interactive Quantity Spinner**: On the Product Details screen, tapping "Add to Cart" animates smoothly into a counter `[ − ] [ quantity ] [ + ]`.
- **Increase / Decrease Quantity**: Instant UI feedback and database persistence when tapping `+` or `−`.
- **Automatic Item Removal**: Decreasing quantity to 0 automatically deletes the item from Room and restores the `[ Add to Cart ]` button.
- **Manual Item Deletion**: In the Cart screen, items can be directly deleted via the delete action button.
- **Cart Badge Counter**: Dynamic badge on the top app bar in both the Product List and Product Details screens showing the total quantity of items currently in the cart.
- **Order Summary & Totals**: Real-time computation of item count and grand total price directly derived from the database Flow.

### Offline & Persistence
- **Local Cart Persistence**: Cart items survive app restarts and process death using an SQLite database via Room.
- **100% Offline Cart Operations**: All cart actions (viewing, adding, updating quantities, removing, and computing totals) work seamlessly without network connectivity.

### UX & States
- **Loading State**: `LoadingView` with animated `CircularProgressIndicator` while data is being fetched.
- **Empty State**: `EmptyView` with informative messages when search queries yield no results, and an illustrated `EmptyCartView` with a "Start Shopping" call-to-action when the cart is empty.
- **Error State**: `ErrorView` displaying user-friendly error messages tailored to network failure causes.
- **Retry Functionality**: "Try Again" retry button on error screens allowing users to re-attempt failed requests without restarting the app.

---

## Screens

| Screen | Description |
|---|---|
| **Product List (`ProductsScreen`)** | Displays top products in an adaptive two-column grid. Features an integrated search bar with a clear button, real-time debounced query processing, product cards with rating/stock badges, and a top bar cart icon with an item badge. |
| **Product Details (`ProductDetailScreen`)** | Shows a high-definition product image card, brand and category tags, price, star rating, stock status pill, and full description. The bottom bar features a reactive action button: starts as `[ Add to Cart ]` and morphs into `[ − ] [ quantity ] [ + ]` with smooth micro-animations. |
| **Shopping Cart (`CartScreen`)** | Lists all selected cart items with thumbnails, unit prices, line totals, quantity controls, and delete buttons. The bottom sheet displays item count, free delivery tag, total price, and a checkout button. Displays a welcoming illustration when empty. |

---

## Tech Stack

The project strictly utilizes modern, standard Android libraries without unnecessary third-party overhead:

- **Language**: [Kotlin](https://kotlinlang.org/) (v2.0.21) — Coroutines, StateFlow, Flow
- **Platform**: Android SDK (minSdk 24, targetSdk 35, compileSdk 35)
- **UI Toolkit**: [Jetpack Compose](https://developer.android.com/jetpack/compose) with [Material 3](https://m3.material.io/) (Compose BOM `2024.12.01`)
- **Navigation**: [Jetpack Navigation Compose](https://developer.android.com/jetpack/compose/navigation) (v2.8.5)
- **Architecture**: MVVM (Model-View-ViewModel) + Unidirectional Data Flow (UDF)
- **Concurrency**: [Kotlin Coroutines Android](https://github.com/Kotlin/kotlinx.coroutines) (v1.9.0)
- **Networking**: [Retrofit](https://square.github.io/retrofit/) (v2.11.0) + [OkHttp](https://square.github.io/okhttp/) (v4.12.0) with `HttpLoggingInterceptor`
- **JSON Serialization**: [Gson](https://github.com/google/gson) (v2.11.0) with Retrofit Gson Converter
- **Local Persistence**: [Room Database](https://developer.android.com/training/data-storage/room) (v2.6.1) with KSP compiler & Coroutines extensions (`room-ktx`)
- **Image Loading**: [Coil](https://coil-kt.github.io/coil/) (v2.7.0) (`coil-compose`)
- **Dependency Injection**: Manual DI via `AppContainer` — lightweight, transparent, and avoids Hilt annotation-processing overhead.

*(Note: Third-party frameworks not present in the project, such as Hilt, are intentionally omitted.)*

---

## API

Product data is consumed from the public [DummyJSON API](https://dummyjson.com/docs/products).

- **Base URL**: `https://dummyjson.com/`

### Endpoints Used
| Endpoint | Method | Purpose |
|---|---|---|
| `/products?limit={limit}&skip={skip}` | `GET` | Fetches the initial list of catalog products. |
| `/products/search?q={query}` | `GET` | Searches products by keyword (debounced). |
| `/products/{id}` | `GET` | Fetches full product details by product ID. |
| `/products/category-list` | `GET` | Declared in API service for category exploration. |
| `/products/category/{category}` | `GET` | Declared in API service for filtered browsing. |

---

## Architecture

The project adheres to the recommended Android Architecture guidelines, following **MVVM** with **Unidirectional Data Flow (UDF)**:

```
┌─────────────────────────────────────────────────────────┐
│                        UI Layer                         │
│  (Jetpack Compose: ProductsScreen, DetailScreen, Cart)  │
└────────────────────────────▲────────────────────────────┘
                             │  Observes StateFlow
                             │  Sends User Events
┌────────────────────────────┴────────────────────────────┐
│                     ViewModel Layer                     │
│    (ProductsViewModel, ProductDetailViewModel, CartVM)  │
└────────────────────────────▲────────────────────────────┘
                             │  Invokes Suspend / Flow
┌────────────────────────────┴────────────────────────────┐
│                    Repository Layer                     │
│             (ProductRepository, CartRepository)         │
└──────────────┬────────────────────────────┬─────────────┘
               │                            │
   Remote Data Source               Local Data Source
 (Retrofit / DummyJsonApi)          (Room / CartDao / SQLite)
```

### Layer Responsibilities
1. **UI Layer (`ui/`)**: Declarative Compose components responsible solely for rendering UI based on immutable state. Observes ViewModel `StateFlow` via `collectAsStateWithLifecycle()` to remain lifecycle-aware.
2. **ViewModel Layer (`ui/*/`)**: Holds and manages UI state. Processes user actions, applies debouncing, launches coroutines within `viewModelScope`, and derives UI state from Room Flows.
3. **Repository Layer (`data/repository/`)**: Serves as the single source of truth. Handles network calls safely with error mapping and coordinates local database operations.
4. **Data Layer (`data/`)**:
   - `local/`: Room Database (`AppDatabase`), Data Access Object (`CartDao`), and SQLite entity (`CartItemEntity`).
   - `remote/`: Retrofit API definition (`DummyJsonApi`) and data transfer objects (`ProductDto`).
5. **Domain Layer (`domain/`)**: Pure Kotlin data classes (`Product`, `CartItem`) free from Android or serialization dependencies.

---

## Project Structure

```
app/src/main/java/com/example/productcatalog/
├── CatalogApp.kt                  # Application class & AppContainer (Manual DI)
├── MainActivity.kt                # Single activity host setting Compose content
├── data/
│   ├── local/
│   │   ├── AppDatabase.kt         # Room database definition (catalog.db)
│   │   ├── CartDao.kt             # DAO with Flow queries and @Transaction logic
│   │   └── CartItemEntity.kt      # SQLite Entity for cart_items table
│   ├── remote/
│   │   ├── DummyJsonApi.kt        # Retrofit interface for DummyJSON endpoints
│   │   └── ProductDto.kt          # DTOs and mapper functions (toDomain())
│   └── repository/
│       ├── CartRepository.kt      # Cart operations repository
│       └── ProductRepository.kt   # Catalog and search repository
├── domain/
│   └── Models.kt                  # Clean domain entities (Product, CartItem)
├── ui/
│   ├── cart/
│   │   ├── CartScreen.kt          # Shopping Cart screen & item card components
│   │   └── CartViewModel.kt       # Cart state & Room Flow reactive calculations
│   ├── components/
│   │   └── StateViews.kt          # Reusable LoadingView, ErrorView, EmptyView
│   ├── detail/
│   │   ├── ProductDetailScreen.kt # Details screen with animated quantity spinner
│   │   └── ProductDetailViewModel.kt # Product details fetcher ViewModel
│   ├── navigation/
│   │   └── AppNavHost.kt          # Navigation host, route declarations & scoping
│   ├── products/
│   │   ├── ProductsScreen.kt      # Product catalog grid & search interface
│   │   └── ProductsViewModel.kt   # Catalog state & debounced search ViewModel
│   └── theme/
│       ├── Color.kt               # Curated color tokens (Indigo, Slate, Amber)
│       ├── Theme.kt               # Material 3 ColorScheme & typography binding
│       └── Type.kt                # Typography styles
└── util/
    └── ApiResult.kt               # safeApiCall runner, error mapper & price formatter
```

---

## Local Storage & Offline Support

Cart persistence is implemented with **Room** targeting a local SQLite database named `catalog.db`:

```kotlin
@Entity(tableName = "cart_items")
data class CartItemEntity(
    @PrimaryKey val productId: Int,
    val title: String,
    val price: Double,
    val thumbnail: String,
    val quantity: Int,
    val addedAt: Long = System.currentTimeMillis()
)
```

### Key Offline Characteristics
- **Snapshot Caching**: The cart stores product metadata (`title`, `price`, `thumbnail`) locally at the time of addition. This allows the cart screen to render completely without reaching out to the API.
- **Atomic Operations**: Quantity increments, decrements, and deletions execute inside Room `@Transaction` blocks, guaranteeing consistency.
- **Reactive Stream**: `CartDao.observeAll()` emits a `Flow<List<CartItemEntity>>`. When any cart item is modified, all observers (top bar badges, Product Details screen counter, and Cart Screen summary) update immediately.
- **Full Offline Capabilities**: Users can:
  - View all saved cart items offline
  - Increase item quantities offline
  - Decrease item quantities offline
  - Remove items offline (via delete icon or decrementing to 0)
  - View accurate total item count and grand total price offline

---

## Error Handling

Network operations are wrapped using `safeApiCall`, which catches exceptions without breaking Coroutine cancellation:

```kotlin
fun Throwable.toUserMessage(): String = when (this) {
    is UnknownHostException -> "No internet connection. Please check your network and try again."
    is SocketTimeoutException -> "The request timed out. Please try again."
    is HttpException -> "Server error (${code()}). Please try again later."
    is IOException -> "Network problem. Please check your connection."
    else -> "Something went wrong. Please try again."
}
```

### Scenarios Handled
- **No Internet / Network Loss**: Identified via `UnknownHostException` / `IOException` → Shows `ErrorView` with message and "Try Again" button.
- **Request Timeout**: Identified via `SocketTimeoutException` → Prompts user to retry.
- **Server Errors (5xx / 4xx)**: Identified via `HttpException` with HTTP status code.
- **Empty Search Query Results**: When query matches 0 products → Displays `EmptyView` with guidance to try another keyword.
- **Empty Cart**: When cart contains 0 items → Displays `EmptyCartView` with "Start Shopping" button.
- **Stale Search Prevention**: `distinctUntilChanged()` and `collectLatest` cancel existing in-flight network searches when new text is typed.

---

## Setup & Installation

### Prerequisites
- **JDK 17** installed and configured in your environment.
- **Android Studio** (Ladybug, Koala, Iguana, or newer).
- Android SDK Platform **35** and Build-Tools installed.

### Clone the Repository
```bash
git clone <repository-url>
cd ProductCatalog
```

### Open in Android Studio
1. Open Android Studio.
2. Select **File > Open...** and choose the `ProductCatalog` root directory.
3. Wait for Gradle to finish syncing dependencies.

---

## Build & Run

### Build from Command Line

**Windows:**
```cmd
gradlew.bat assembleDebug
```

**macOS / Linux:**
```bash
./gradlew assembleDebug
```

The debug APK will be generated at:
`app/build/outputs/apk/debug/app-debug.apk`

### Run on Device or Emulator
1. Connect a physical Android device with USB Debugging enabled, or launch an Android Virtual Device (AVD) running **Android 7.0 (API 24) or higher**.
2. Select the `app` run configuration in Android Studio.
3. Click **Run** (or press `Shift + F10`).

---

## Testing Checklist

Use this practical verification checklist to validate the application:

- [ ] **Catalog Loading**: Products load into the 2-column grid upon launch.
- [ ] **Search**: Type "phone", "perfume", or "watch"; search results update after 400ms debounce.
- [ ] **Search Clear**: Tap the `✕` icon in the search bar; the full product list restores immediately.
- [ ] **Product Details**: Tap any product card; details screen displays title, price, image, tags, and stock.
- [ ] **Add to Cart**: Tap `[ Add to Cart ]` on Details screen; button smoothly animates into `[ − ] [ 1 ] [ + ]`.
- [ ] **Quantity Increment**: Tap `+`; quantity increments to 2, 3, etc., with subtle vertical number slide animation.
- [ ] **Quantity Decrement**: Tap `−`; quantity decrements accordingly.
- [ ] **Zero Quantity Removal**: Tap `−` when quantity is 1; product is removed from cart and button reverts to `[ Add to Cart ]`.
- [ ] **State Persistence on Details**: Add an item to cart, exit to catalog, and re-open details; spinner shows existing quantity.
- [ ] **Cart Badge**: Top bar cart badge reflects accurate total quantity on both Catalog and Details screens.
- [ ] **Cart Screen**: Open Cart; view item list, line prices, quantity stepper, and grand total.
- [ ] **Cart Item Deletion**: Tap trash icon on a cart item; item is removed and total recalculates immediately.
- [ ] **App Restart Persistence**: Add items to cart, force close the app, and reopen; all cart items and quantities remain intact.
- [ ] **Airplane Mode (Offline)**: Turn on Airplane mode; cart viewing, adding, adjusting quantities, and removals function 100% offline.
- [ ] **Error & Retry**: Turn off Wi-Fi/data, trigger a search or refresh; error screen appears with "Try Again" button that recovers once connection is restored.

---

## Design Decisions

- **Why MVVM & Unidirectional Data Flow?**  
  Ensures clear separation of concerns. The UI passively renders state emitted by ViewModels, making state changes predictable, easy to debug, and resilient against configuration changes (screen rotations).
- **Why the Repository Pattern?**  
  Decouples the business logic and ViewModels from underlying data source implementations (Retrofit vs. Room). If caching strategies change in the future, the UI layer remains unaffected.
- **Why Room for Cart Persistence?**  
  Room provides compile-time verification of SQL queries, robust SQLite performance, and native Kotlin Coroutines `Flow` support. Observing changes via Flow guarantees the UI stays synchronized with zero manual polling.
- **Why Decouple Cart from the API?**  
  DummyJSON does not provide persistent per-user cart endpoints. By saving item snapshots locally in Room, cart operations remain completely responsive and 100% functional offline.
- **Why Jetpack Compose?**  
  Modern declarative UI reduces boilerplate code, eliminates XML view hierarchies, enables reusable custom design components, and simplifies complex micro-animations (e.g., `AnimatedContent` for the quantity spinner).
- **Why Manual DI (`AppContainer`) Instead of Hilt?**  
  For an assessment application of this scope, manual dependency injection via `AppContainer` keeps the project transparent, easy to follow, fast to build, and free from excessive annotation-processing overhead.

---

## Known Limitations

- **Pagination**: The product catalog currently loads the first 30 products (`limit=30`). Infinite scroll / pagination is not yet implemented.
- **Catalog Caching**: Only the shopping cart is cached locally in Room; the product list and individual product detail views require an active internet connection.
- **Image Offline Availability**: Product images rely on Coil's in-memory and disk cache; images not previously loaded will not display when offline.
- **Category Filter UI**: Category endpoints exist in `DummyJsonApi` and `ProductRepository`, but a dedicated category chips filter UI has not yet been built.
- **Automated Tests**: Unit and UI automated test suites are not yet included in the current build.

---

## Assessment Requirements

This project fulfills the practical assessment specifications:
- Clean and readable Kotlin codebase.
- Offline-first Room cart with full increment, decrement, and removal flows.
- Reactive UI state updates driven by Room `Flow` and ViewModel `StateFlow`.
- Polished, responsive Jetpack Compose Material 3 UI with micro-animations.
- Server-side search with debounce and network cancellation.
- Graceful error handling for offline and timeout scenarios.
