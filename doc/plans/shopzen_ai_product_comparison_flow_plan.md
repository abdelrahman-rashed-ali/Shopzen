# ShopZen AI Product Comparison Flow Plan

## Objective

Add an AI-powered comparison flow where the user can select exactly 2 products and ask the assistant to compare them using real product data.

The comparison should help the user understand:

- Which product is better for their needs
- Key differences
- Price/value difference
- Variant/availability differences
- Pros and cons
- Recommended choice based on user intent

The AI must not invent product details. It should only compare based on data returned from product details, variants, tags, descriptions, reviews, and available metadata.

---

# 1. User Flow

## Flow A: Compare From Product Listing

### Scenario

User browses products and selects two items to compare.

### Steps

```text
Product Listing Screen
  -> User taps "Compare" on Product A
  -> Product A is added to comparison tray
  -> User taps "Compare" on Product B
  -> Comparison tray shows 2 selected products
  -> User taps "Compare with AI"
  -> App opens AI Comparison Screen / Bottom Sheet
  -> AI generates comparison
```

### UI Behavior

- Each product card should have a compare icon/button.
- When one product is selected, show a floating comparison tray.
- Tray should show:
  - Product thumbnail 1
  - Product thumbnail 2 placeholder
  - Clear button
  - Compare button disabled until 2 products are selected
- When 2 products are selected:
  - Disable selecting more products
  - Enable "Compare with AI"

---

## Flow B: Compare From Product Details

### Scenario

User is viewing a product and wants to compare it with another product.

### Steps

```text
Product Details Screen
  -> User taps "Compare"
  -> Product is added to comparison tray
  -> User navigates/searches for another product
  -> User adds second product
  -> User taps "Compare with AI"
```

### UI Behavior

- Product details screen should include a "Compare" action.
- If product is already selected, show "Remove from comparison."
- Comparison state should persist across navigation.

---

## Flow C: Compare From AI Chat

### Scenario

User asks the assistant to compare products naturally.

### Example Prompts

```text
Compare this product with the black hoodie.
Which one is better between these two?
Compare the first and second products.
Which one should I buy?
```

### Steps

```text
User asks comparison question
  -> AI checks if 2 products are already selected
  -> If yes, calls compare_products
  -> If no, asks user to select another product
  -> AI returns comparison result
```

---

# 2. Recommended UX Design

## Comparison Tray

A persistent mini component shown at the bottom of product listing/details screens.

### States

#### Empty State

```text
No products selected for comparison.
```

#### One Product Selected

```text
Compare
[Product A] + [Select another product]
```

#### Two Products Selected

```text
Compare
[Product A] vs [Product B]
[Compare with AI]
[Clear]
```

---

## AI Comparison Result UI

The comparison result should not be only plain text.

Use structured UI sections:

```text
AI Product Comparison

Product A vs Product B

1. Quick Verdict
2. Best For
3. Key Differences
4. Price & Value
5. Availability / Variants
6. Pros and Cons
7. Final Recommendation
```

---

# 3. Required Screens / Components

## New Components

```text
presentation/comparison
  ComparisonTray.kt
  ProductCompareButton.kt
  AiComparisonScreen.kt
  AiComparisonBottomSheet.kt
  ProductComparisonResultCard.kt
  ComparisonSectionCard.kt
```

## Existing Screens to Update

```text
presentation/home
presentation/search
presentation/category
presentation/productdetails
presentation/ai
```

---

# 4. State Management

## Comparison Selection State

The comparison state should be shared across screens.

Recommended options:

1. Shared `ComparisonViewModel`
2. App-level state holder
3. Navigation-scoped ViewModel

Recommended approach:

```text
Navigation graph scoped ComparisonViewModel
```

This allows:

- Selecting product from listing
- Navigating to details
- Keeping selected products
- Opening comparison from different screens

---

## Comparison State Model

```kotlin
data class ComparisonState(
    val selectedProducts: List<ComparableProductUiModel> = emptyList(),
    val isCompareEnabled: Boolean = false,
    val error: String? = null
)
```

```kotlin
data class ComparableProductUiModel(
    val productId: String,
    val title: String,
    val imageUrl: String?,
    val price: Double,
    val currency: String,
    val selectedVariantId: String? = null
)
```

---

## Comparison Intent

```kotlin
sealed interface ComparisonIntent {
    data class AddProduct(val product: ComparableProductUiModel) : ComparisonIntent
    data class RemoveProduct(val productId: String) : ComparisonIntent
    data object ClearComparison : ComparisonIntent
    data object CompareWithAi : ComparisonIntent
}
```

---

# 5. Product Selection Rules

## Required Rules

- User can select maximum 2 products.
- User cannot select the same product twice.
- User can remove selected products.
- User can clear all selected products.
- Compare button is enabled only when exactly 2 products are selected.
- If selected product has multiple variants, use the currently selected variant if available.
- If no variant is selected, use default variant or ask user to choose.

---

# 6. AI Tool Design

## Tool Name

```text
compare_products
```

## Purpose

Compares two products using product details, variants, price, availability, tags, description, and optional reviews.

---

## Tool Input

```json
{
  "productAId": "string",
  "productBId": "string",
  "productAVariantId": "string | null",
  "productBVariantId": "string | null",
  "userIntent": "string | null"
}
```

### Field Meaning

| Field | Description |
|---|---|
| `productAId` | First selected product ID |
| `productBId` | Second selected product ID |
| `productAVariantId` | Selected variant for first product, if available |
| `productBVariantId` | Selected variant for second product, if available |
| `userIntent` | Optional user goal, such as “cheapest”, “best quality”, “daily use”, “gift”, etc. |

---

## Tool Output

```json
{
  "productA": {
    "id": "string",
    "title": "string",
    "price": "number",
    "currency": "string",
    "available": "boolean",
    "description": "string",
    "tags": ["string"],
    "variants": [
      {
        "id": "string",
        "title": "string",
        "price": "number",
        "available": "boolean"
      }
    ],
    "rating": "number | null",
    "reviewCount": "number | null"
  },
  "productB": {
    "id": "string",
    "title": "string",
    "price": "number",
    "currency": "string",
    "available": "boolean",
    "description": "string",
    "tags": ["string"],
    "variants": [
      {
        "id": "string",
        "title": "string",
        "price": "number",
        "available": "boolean"
      }
    ],
    "rating": "number | null",
    "reviewCount": "number | null"
  }
}
```

---

# 7. AI Comparison Response Format

The assistant should return a structured comparison, not a generic paragraph.

## Required Response Sections

```text
1. Quick Verdict
2. Side-by-Side Summary
3. Key Differences
4. Price & Value
5. Best For
6. Pros and Cons
7. Final Recommendation
```

---

## Example AI Output

```text
Quick Verdict:
Product A is better if you want a cheaper option, while Product B is better if you care more about premium material and stronger reviews.

Side-by-Side Summary:

Product A:
- Lower price
- Available in more variants
- Better for casual daily use

Product B:
- Higher price
- Better rating
- Better for long-term use

Key Differences:
The biggest difference is price and material quality. Product A is more budget-friendly, while Product B appears more premium based on the available product details.

Price & Value:
Product A gives better value if your main goal is saving money. Product B may be worth the higher price if quality is more important.

Best For:
- Choose Product A for budget and daily casual use.
- Choose Product B for better quality and long-term use.

Final Recommendation:
If your priority is price, choose Product A. If your priority is quality, choose Product B.
```

---

# 8. Domain Layer Plan

## New Use Case

```kotlin
class CompareProductsUseCase(
    private val productRepository: ProductRepository
) {
    suspend operator fun invoke(
        productAId: String,
        productBId: String,
        productAVariantId: String?,
        productBVariantId: String?
    ): ProductComparisonData
}
```

---

## Domain Model

```kotlin
data class ProductComparisonData(
    val productA: ProductComparisonItem,
    val productB: ProductComparisonItem
)
```

```kotlin
data class ProductComparisonItem(
    val id: String,
    val title: String,
    val description: String,
    val price: Double,
    val currency: String,
    val imageUrl: String?,
    val available: Boolean,
    val tags: List<String>,
    val variants: List<ProductVariantComparisonItem>,
    val rating: Double?,
    val reviewCount: Int?
)
```

```kotlin
data class ProductVariantComparisonItem(
    val id: String,
    val title: String,
    val price: Double,
    val available: Boolean
)
```

---

# 9. AI Integration Plan

## Option A: AI Receives Raw Comparison Data

Flow:

```text
User selects 2 products
  -> App calls CompareProductsUseCase
  -> App sends structured product data to AI
  -> AI generates comparison text
  -> UI displays result
```

### Pros

- Simple
- Easy to implement
- AI only receives already-approved product data

### Cons

- Less flexible for follow-up questions
- App handles data gathering before AI

---

## Option B: AI Calls `compare_products` Tool

Flow:

```text
User selects 2 products
  -> App sends product IDs to AI
  -> AI calls compare_products tool
  -> Tool fetches product details
  -> AI generates structured comparison
  -> UI displays result
```

### Pros

- Fits current AI tool-calling architecture
- Better for follow-up questions
- More scalable

### Cons

- Requires tool registry support
- Requires stronger validation

---

## Recommended Approach

Use **Option B** if your function-calling feature is already completed.

Reason:

- You already built AI tools.
- Comparison becomes just another tool.
- Follow-up questions become easier.

Example follow-up:

```text
User:
Which one is better for daily use?

AI:
Uses previous comparison context and answers without forcing the user to reselect products.
```

---

# 10. AI Prompt Requirements

## System Instruction Addition

Add this rule to the AI assistant system prompt:

```text
When comparing products, only use product information returned by the comparison tool or product detail tools. Do not invent specs, ratings, materials, discounts, stock status, or review information. If a detail is missing, clearly say it is not available.
```

---

## Comparison Prompt Template

```text
Compare the following two products for a Shopify store user.

User intent:
{{userIntent}}

Product A:
{{productAData}}

Product B:
{{productBData}}

Return the comparison using these sections:
1. Quick Verdict
2. Side-by-Side Summary
3. Key Differences
4. Price & Value
5. Best For
6. Pros and Cons
7. Final Recommendation

Rules:
- Do not invent missing details.
- Mention uncertainty when data is incomplete.
- Prefer concise, shopping-focused language.
- Give a final recommendation only when enough data exists.
```

---

# 11. UI Result Model

```kotlin
data class AiComparisonResultUiModel(
    val productA: ComparableProductUiModel,
    val productB: ComparableProductUiModel,
    val quickVerdict: String,
    val sideBySideSummary: List<ComparisonRowUiModel>,
    val keyDifferences: List<String>,
    val priceAndValue: String,
    val bestFor: List<BestForUiModel>,
    val prosAndCons: ProductProsConsUiModel,
    val finalRecommendation: String
)
```

```kotlin
data class ComparisonRowUiModel(
    val label: String,
    val productAValue: String,
    val productBValue: String
)
```

```kotlin
data class BestForUiModel(
    val productTitle: String,
    val reason: String
)
```

```kotlin
data class ProductProsConsUiModel(
    val productAPros: List<String>,
    val productACons: List<String>,
    val productBPros: List<String>,
    val productBCons: List<String>
)
```

---

# 12. UI Layout Plan

## AI Comparison Screen

```text
Top Bar
  - Title: AI Comparison
  - Back button
  - Clear comparison button

Selected Products Header
  - Product A card
  - VS badge
  - Product B card

Intent Input
  - Optional text field:
    "What matters most to you?"
  - Example chips:
    - Cheapest
    - Best quality
    - Daily use
    - Gift
    - Most popular

Generate Button
  - "Compare with AI"

Result Area
  - Loading state
  - Comparison result
  - Error state
```

---

## Result Sections

```text
Quick Verdict Card

Side-by-Side Table

Key Differences Card

Price & Value Card

Best For Card

Pros and Cons Card

Final Recommendation Card

Actions
  - Add recommended product to cart
  - View Product A
  - View Product B
  - Ask follow-up question
```

---

# 13. Follow-Up Questions

After comparison, show suggested prompts:

```text
Which one is better for daily use?
Which one has better value?
Which one is cheaper?
Which one should I buy?
Show me alternatives.
Add the recommended one to cart.
```

These prompts can be sent into the existing AI chat flow with comparison context.

---

# 14. Action Integration

If Phase 2 action tools are available, comparison results should support:

```text
Add Product A to Cart
Add Product B to Cart
Add Recommended Product to Cart
Save Product A to Wishlist
Save Product B to Wishlist
View Product Details
```

State-changing actions still require confirmation.

---

# 15. Error Handling

## Error Cases

| Case | Expected Behavior |
|---|---|
| Only one product selected | Ask user to select another product |
| More than two selected | Prevent this in UI |
| Same product selected twice | Show “Product already selected” |
| Product unavailable | Show availability warning |
| Product details failed | Show retry option |
| AI comparison failed | Show fallback basic comparison |
| Missing product fields | AI should state that data is unavailable |
| Different currencies | Show warning and avoid direct price judgment |
| Variant unavailable | Ask user to select another variant |

---

## Fallback Non-AI Comparison

If AI fails, show deterministic comparison:

```text
- Product title
- Price
- Availability
- Rating
- Review count
- Variant count
```

This prevents the comparison feature from feeling broken.

---

# 16. Analytics Events

Track these events:

```text
comparison_product_added
comparison_product_removed
comparison_started
comparison_ai_generated
comparison_ai_failed
comparison_recommendation_clicked
comparison_add_to_cart_clicked
comparison_follow_up_clicked
comparison_cleared
```

Useful properties:

```text
product_a_id
product_b_id
source_screen
user_intent
recommended_product_id
comparison_duration_ms
error_type
```

---

# 17. Testing Plan

## Unit Tests

- Add product to comparison.
- Prevent duplicate product selection.
- Prevent selecting more than 2 products.
- Remove product from comparison.
- Clear comparison.
- Enable compare only with 2 products.
- CompareProductsUseCase returns both product details.
- AI tool validates both product IDs.
- AI tool fails clearly when product is missing.

## ViewModel Tests

- Selecting first product updates tray state.
- Selecting second product enables compare.
- Compare intent starts loading.
- Successful AI comparison updates result state.
- Failed AI comparison shows error.
- Retry works.
- Clear comparison resets state.

## UI Tests

- Compare button appears on product cards.
- Comparison tray appears after selecting product.
- Compare button disabled with one product.
- Compare button enabled with two products.
- Result screen displays AI comparison sections.
- Error state displays retry.

---

# 18. Acceptance Criteria

The comparison feature is complete when:

- User can select Product A from listing or details.
- User can select Product B from listing or details.
- User cannot select more than 2 products.
- User cannot compare duplicate products.
- Selected products persist across navigation.
- User can clear selected products.
- User can open AI comparison screen.
- User can optionally provide comparison intent.
- AI compares the two products using real product data.
- AI does not invent unavailable details.
- Result includes quick verdict, differences, price/value, pros/cons, and recommendation.
- User can ask follow-up questions.
- User can view either product from comparison.
- User can add one product to cart through the existing confirmed action flow.
- If AI fails, the app shows a fallback deterministic comparison.
