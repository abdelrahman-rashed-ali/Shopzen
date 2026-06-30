# HomeScreen Implementation Plan — Shopzen

## Overview
Full implementation of the HomeScreen feature (`main/home` route) following Clean Architecture + MVI across all 4 Gradle modules.

## Color Palette (from reference UI)

| Token | Hex |
|---|---|
| primary | `#000000` |
| onPrimary | `#FFFFFF` |
| surface / background | `#F9F9F9` |
| onSurface | `#1A1C1C` |
| onSurfaceVariant | `#444748` |
| outline | `#747878` |
| outlineVariant | `#C4C7C7` |
| surfaceContainerLow | `#F3F3F3` |
| error | `#BA1A1A` |
| secondary | `#735C00` |

## Architecture

```
:presentation ──▶ :domain ◀── :data
                    ▲
              :app wires everything
```

## File Inventory

### Phase 1: Gradle Dependencies
| File | Action |
|---|---|
| `gradle/libs.versions.toml` | Add retrofit, okhttp, coil, compose-foundation, lifecycle-compose |
| `presentation/build.gradle.kts` | Add kotlin-compose plugin, Compose deps, Coil, fix minSdk |
| `data/build.gradle.kts` | Add Retrofit/OkHttp, fix minSdk, fix project dep syntax |
| `app/build.gradle.kts` | Add project deps (:domain, :data, :presentation) |

### Phase 2: Domain Layer
| File | Description |
|---|---|
| `domain/.../catalog/model/Product.kt` | Pure Kotlin data class |
| `domain/.../catalog/model/Brand.kt` | Pure Kotlin data class |
| `domain/.../catalog/model/Category.kt` | Pure Kotlin data class |
| `domain/.../catalog/repository/CatalogRepository.kt` | Interface with suspend funs |
| `domain/.../catalog/usecase/GetProductsUseCase.kt` | Single invoke operator |
| `domain/.../catalog/usecase/GetBrandsUseCase.kt` | Single invoke operator |
| `domain/.../catalog/usecase/GetCategoriesUseCase.kt` | Single invoke operator |

### Phase 3: Data Layer
| File | Description |
|---|---|
| `data/.../catalog/remote/dto/ProductDto.kt` | Gson-annotated DTOs |
| `data/.../catalog/remote/dto/CollectionDto.kt` | Gson-annotated DTOs |
| `data/.../catalog/remote/api/CatalogApiService.kt` | Retrofit interface |
| `data/.../catalog/mapper/CatalogMapper.kt` | DTO → Domain mapping |
| `data/.../catalog/repository/CatalogRepositoryImpl.kt` | Repository implementation |

### Phase 4: Theme & Shared Components
| File | Description |
|---|---|
| `presentation/.../common/theme/Color.kt` | Material 3 color tokens |
| `presentation/.../common/theme/Type.kt` | Typography scale |
| `presentation/.../common/theme/ShopzenTheme.kt` | Theme composable |
| `presentation/.../common/components/LoadingIndicator.kt` | Shared loading UI |
| `presentation/.../common/components/ErrorScreen.kt` | Shared error UI |
| `presentation/.../common/components/ProductCard.kt` | Shared product card |

### Phase 5: MVI
| File | Description |
|---|---|
| `presentation/.../catalog/state/HomeState.kt` | Data class with defaults |
| `presentation/.../catalog/intent/HomeIntent.kt` | Sealed class |
| `presentation/.../catalog/viewmodel/HomeViewModel.kt` | ViewModel + Factory |

### Phase 6: UI Components
| File | Description |
|---|---|
| `presentation/.../catalog/components/FeaturedBanner.kt` | HorizontalPager banner |
| `presentation/.../catalog/components/BrandGrid.kt` | Horizontal brand chips |
| `presentation/.../catalog/components/CategoryChips.kt` | Horizontal category cards |
| `presentation/.../catalog/components/NewArrivalsSection.kt` | 2-col product grid |
| `presentation/.../catalog/screen/HomeScreen.kt` | Main stateless composable |

### Phase 7: Navigation & Wiring
| File | Action |
|---|---|
| `app/.../navigation/NavScreen.kt` | Add HomeScreen route |
| `app/.../navigation/AppNavHost.kt` | Register HomeScreen, wire ViewModel |
| `app/.../MainActivity.kt` | Wrap in ShopzenTheme, change start dest |
| `app/.../di/CatalogModule.kt` | Manual DI provider |
| `app/src/main/res/values/strings.xml` | Add string resources |

### Phase 8: Unit Tests
| File | Description |
|---|---|
| `domain/src/test/.../GetProductsUseCaseTest.kt` | Use case delegates to repo |
| `domain/src/test/.../GetBrandsUseCaseTest.kt` | Use case delegates to repo |
| `domain/src/test/.../GetCategoriesUseCaseTest.kt` | Use case delegates to repo |
| `data/src/test/.../CatalogMapperTest.kt` | DTO → Domain mapping correctness |
| `presentation/src/test/.../HomeViewModelTest.kt` | MVI state transitions |

## Unit Test Strategy

### Domain Tests
- Verify each use case delegates to its repository method
- Verify Result.success and Result.failure propagation

### Data Tests (CatalogMapper)
- `ProductDto.toDomain()` maps all fields correctly
- Null/empty fields produce safe defaults (empty strings, not crashes)
- `toDistinctBrands()` extracts unique vendor names
- `CollectionDto.toDomain()` maps id, title, imageUrl

### Presentation Tests (HomeViewModel)
- Initial state is `HomeState()` defaults
- `LoadHomeData` sets `isLoading = true` then loads data
- Successful load populates all state fields
- All-failures sets error message
- Partial failures still populate available data
- Banner images derived from first 5 products

## Verification
- `./gradlew :domain:test` — domain unit tests
- `./gradlew :data:test` — mapper unit tests  
- `./gradlew :presentation:test` — ViewModel unit tests
- `./gradlew assembleDebug` — full build verification
