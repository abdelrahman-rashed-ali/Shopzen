# ShopZen AI Assistant Requirements

## Scope

This document defines the requirements for the first two AI implementation phases in the Shopify Android app:

1. **Phase 1: Read-Only AI Shopping Assistant**
2. **Phase 2: Action-Based AI Assistant with Confirmation**
3. **Optional: `llm-client` Refactor**

The goal is to evolve the AI feature from a simple chat interface into a safe shopping assistant that can understand user intent, search products, explain products, compare options, answer store-policy questions, and later perform controlled actions such as adding items to cart.

---

# Phase 1: Read-Only AI Shopping Assistant

## Objective

Build an AI chat assistant that can answer shopping-related questions and use read-only app/domain use cases to retrieve product, category, cart, and store-policy information.

The assistant should not modify user data in this phase.

It can search, explain, compare, summarize, and recommend, but it must not add to cart, remove from cart, update wishlist, apply discounts, or open checkout.

---

## Core Features

### 1. Chat Interface

The app should include a dedicated AI chat screen where the user can interact with the assistant.

#### Requirements

- The user can send natural-language messages.
- The assistant can respond in conversational text.
- The UI supports loading states.
- The UI supports streamed responses if available.
- The UI displays tool-based results in a user-friendly way.
- The UI handles errors gracefully.
- The user can retry failed messages.
- The chat should preserve the current conversation during the screen lifecycle.
- The chat should support basic message types:
  - User text message
  - Assistant text message
  - Loading assistant message
  - Error message
  - Product suggestion cards
  - Product comparison result
  - Suggested follow-up actions

#### Example Prompts

```text
Show me black shoes under 2000 EGP.
What do you recommend for daily use?
Compare these two products.
Do you have skincare products?
What is your return policy?
Which product is cheaper?
Find something similar to this.
```

---

### 2. Read-Only Tool Calling

The AI assistant should be able to call safe read-only tools mapped to domain use cases.

The AI should not access repositories, APIs, or Shopify services directly from the UI layer.

#### Required Tool Flow

```text
AI Assistant
  -> Tool Registry
    -> Domain Use Case
      -> Repository
        -> Shopify / Backend / Local Data Source
```

#### Required Read-Only Tools

| Tool Name | Purpose | Example User Prompt |
|---|---|---|
| `search_products` | Search products by natural-language query | “Find running shoes under 2000 EGP” |
| `get_categories` | Retrieve available product categories | “What categories do you have?” |
| `get_product_details` | Get details for a specific product | “Tell me more about this product” |
| `filter_products` | Filter products by price, category, brand, rating, etc. | “Only show products under 1000 EGP” |
| `sort_products` | Sort products by price, rating, newest, etc. | “Sort them from cheapest to most expensive” |
| `compare_products` | Compare two or more products | “Compare these two items” |
| `get_cart_summary` | Read current cart state only | “What is in my cart?” |
| `get_store_policy` | Answer questions from store policy content | “Can I return this item?” |
| `get_recommendations` | Suggest products based on query/context | “Recommend something for summer” |

---

## Tool Requirements

### `search_products`

Searches products using user intent.

#### Input

```json
{
  "query": "string",
  "category": "string | null",
  "minPrice": "number | null",
  "maxPrice": "number | null",
  "brand": "string | null",
  "limit": "number"
}
```

#### Output

```json
{
  "products": [
    {
      "id": "string",
      "title": "string",
      "description": "string",
      "price": "number",
      "currency": "string",
      "imageUrl": "string | null",
      "rating": "number | null",
      "available": "boolean"
    }
  ]
}
```

#### Acceptance Criteria

- The assistant can search products using natural-language queries.
- The assistant can extract price constraints from user text.
- The assistant can return empty-state messages when no products match.
- The assistant should not invent products.
- The assistant should only mention products returned by the tool.

---

### `get_categories`

Returns available store categories.

#### Input

```json
{}
```

#### Output

```json
{
  "categories": [
    {
      "id": "string",
      "name": "string"
    }
  ]
}
```

#### Acceptance Criteria

- The assistant can list available categories.
- The assistant can use categories to guide product search.
- The assistant should not invent unavailable categories.

---

### `get_product_details`

Returns detailed product information.

#### Input

```json
{
  "productId": "string"
}
```

#### Output

```json
{
  "id": "string",
  "title": "string",
  "description": "string",
  "price": "number",
  "currency": "string",
  "images": ["string"],
  "variants": [
    {
      "id": "string",
      "title": "string",
      "price": "number",
      "available": "boolean"
    }
  ],
  "rating": "number | null",
  "reviewCount": "number | null",
  "tags": ["string"]
}
```

#### Acceptance Criteria

- The assistant can explain product details clearly.
- The assistant can answer questions about variants if variant data is available.
- The assistant must say when information is missing.
- The assistant must not claim unavailable specifications.

---

### `compare_products`

Compares multiple products.

#### Input

```json
{
  "productIds": ["string"]
}
```

#### Output

```json
{
  "products": [
    {
      "id": "string",
      "title": "string",
      "price": "number",
      "currency": "string",
      "rating": "number | null",
      "available": "boolean",
      "mainFeatures": ["string"],
      "weaknesses": ["string"]
    }
  ]
}
```

#### Acceptance Criteria

- The assistant can compare products by price, availability, rating, features, and suitability.
- The assistant should identify the best option only when enough information exists.
- The assistant should explain uncertainty when product data is incomplete.

---

### `get_cart_summary`

Reads the current cart without changing it.

#### Input

```json
{}
```

#### Output

```json
{
  "items": [
    {
      "cartLineId": "string",
      "productId": "string",
      "variantId": "string",
      "title": "string",
      "quantity": "number",
      "price": "number",
      "currency": "string"
    }
  ],
  "subtotal": "number",
  "currency": "string"
}
```

#### Acceptance Criteria

- The assistant can tell the user what is currently in the cart.
- The assistant can summarize total price.
- The assistant cannot modify the cart in Phase 1.

---

### `get_store_policy`

Answers store policy questions.

#### Input

```json
{
  "question": "string"
}
```

#### Output

```json
{
  "answer": "string",
  "source": "string | null",
  "confidence": "low | medium | high"
}
```

#### Acceptance Criteria

- The assistant can answer return, refund, shipping, payment, and delivery policy questions.
- The assistant should not invent policy rules.
- If the policy is unclear, the assistant should say that it cannot confirm.
- Policy answers should be grounded in stored policy text.

---

## Phase 1 Android Requirements

### Suggested Package Structure

```text
presentation/ai
  AiChatScreen.kt
  AiChatViewModel.kt
  AiChatState.kt
  AiChatIntent.kt
  AiMessageUiModel.kt
  AiToolResultUiMapper.kt

domain/ai
  AiAssistantUseCase.kt
  AiTool.kt
  AiToolRegistry.kt
  AiToolRequest.kt
  AiToolResult.kt
  AiConversationContext.kt

data/ai
  AiRepositoryImpl.kt
  AiGatewayDataSource.kt
  AiToolSchemaMapper.kt
```

---

## Phase 1 ViewModel Requirements

The `AiChatViewModel` should:

- Hold chat state.
- Send user messages.
- Receive assistant responses.
- Handle streaming chunks.
- Map tool results into UI models.
- Handle loading and error states.
- Avoid exposing low-level LLM classes directly to the UI.

Example state:

```kotlin
data class AiChatState(
    val messages: List<AiMessageUiModel> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val suggestions: List<String> = emptyList()
)
```

Example intent:

```kotlin
sealed interface AiChatIntent {
    data class SendMessage(val text: String) : AiChatIntent
    data object RetryLastMessage : AiChatIntent
    data object ClearChat : AiChatIntent
    data class OpenProduct(val productId: String) : AiChatIntent
}
```

---

## Phase 1 Backend / Gateway Requirements

For production, the Android app should not call the LLM provider directly.

The recommended structure is:

```text
Android App
  -> AI Gateway Backend
    -> LLM Provider
    -> Shopify Storefront API
    -> Product Search
    -> Policy Search
```

### Gateway Responsibilities

- Store LLM API keys securely.
- Validate user requests.
- Execute tools safely.
- Apply rate limits.
- Track token usage.
- Log tool calls.
- Prevent unauthorized access.
- Return assistant response to the app.
- Hide internal provider details from Android.

---

## Phase 1 Non-Functional Requirements

### Security

- No LLM provider API key should be stored in the Android app.
- Tool calls must be validated before execution.
- The assistant must not expose internal IDs unless needed by the app.
- The assistant must not expose raw tool JSON to normal users.
- User-specific data must only be accessed for authenticated users.

### Reliability

- Failed tool calls should return structured errors.
- The assistant should not crash the chat when a tool fails.
- The UI should allow retrying failed messages.
- Network timeout should be handled.

### Performance

- Streaming should be used when available.
- Product results should be limited.
- Large product descriptions should be summarized before sending to the model.
- Conversation history should be trimmed or summarized when it grows too large.

### UX

- Product results should be shown as product cards.
- Assistant answers should be concise.
- Suggested follow-up actions should be shown after useful responses.
- Empty states should be clear.

---

# Phase 2: Action-Based AI Assistant With Confirmation

## Objective

Extend the assistant so it can perform safe user actions through app use cases.

Unlike Phase 1, Phase 2 allows the AI to request state-changing operations such as adding items to cart, updating cart quantity, removing items, saving products, and opening checkout.

Every sensitive or state-changing action must require user confirmation before execution.

---

## Core Features

### 1. Confirmed Cart Actions

The assistant should be able to prepare cart actions, explain them to the user, and ask for confirmation before execution.

#### Supported Actions

| Tool Name | Purpose | Requires Confirmation |
|---|---|---|
| `add_to_cart` | Add product variant to cart | Yes |
| `remove_from_cart` | Remove item from cart | Yes |
| `update_cart_quantity` | Change cart item quantity | Yes |
| `clear_cart` | Remove all cart items | Yes |
| `apply_discount_code` | Apply discount code | Yes |
| `open_checkout` | Open checkout flow | Yes |
| `toggle_wishlist` | Save or unsave product | Optional, recommended |
| `save_user_preference` | Save preference like size/color | Yes |

---

## Confirmation Flow

The AI must not directly execute action tools.

Instead, the flow should be:

```text
User asks for action
  -> AI identifies intended action
  -> AI prepares pending action
  -> App shows confirmation UI
  -> User confirms
  -> App executes domain use case
  -> Assistant summarizes result
```

Example:

```text
User:
Add the cheapest black hoodie in size M to my cart.

Assistant:
I found "Black Cotton Hoodie" in size M for 850 EGP.
Do you want me to add it to your cart?

User:
Yes.

System:
Executes add_to_cart.

Assistant:
Added "Black Cotton Hoodie" to your cart.
```

---

## Pending Action Model

```kotlin
data class PendingAiAction(
    val id: String,
    val type: AiActionType,
    val title: String,
    val description: String,
    val payload: JsonObject,
    val requiresConfirmation: Boolean = true
)
```

```kotlin
enum class AiActionType {
    ADD_TO_CART,
    REMOVE_FROM_CART,
    UPDATE_CART_QUANTITY,
    CLEAR_CART,
    APPLY_DISCOUNT,
    OPEN_CHECKOUT,
    TOGGLE_WISHLIST,
    SAVE_USER_PREFERENCE
}
```

---

## Required Action Tools

### `add_to_cart`

Adds a product variant to cart after confirmation.

#### Input

```json
{
  "productId": "string",
  "variantId": "string",
  "quantity": "number"
}
```

#### Output

```json
{
  "success": "boolean",
  "message": "string",
  "cartSummary": {
    "subtotal": "number",
    "currency": "string",
    "itemCount": "number"
  }
}
```

#### Acceptance Criteria

- The assistant must confirm before adding to cart.
- The assistant must use a valid product variant ID.
- The assistant must not add unavailable variants.
- The assistant must show a success or failure message.
- The cart UI should update after success.

---

### `remove_from_cart`

Removes an item from cart after confirmation.

#### Input

```json
{
  "cartLineId": "string"
}
```

#### Output

```json
{
  "success": "boolean",
  "message": "string",
  "cartSummary": {
    "subtotal": "number",
    "currency": "string",
    "itemCount": "number"
  }
}
```

#### Acceptance Criteria

- The assistant must confirm before removing an item.
- The assistant must identify the cart item clearly.
- The cart UI should update after success.

---

### `update_cart_quantity`

Updates item quantity after confirmation.

#### Input

```json
{
  "cartLineId": "string",
  "quantity": "number"
}
```

#### Output

```json
{
  "success": "boolean",
  "message": "string",
  "cartSummary": {
    "subtotal": "number",
    "currency": "string",
    "itemCount": "number"
  }
}
```

#### Acceptance Criteria

- Quantity must be greater than or equal to zero.
- Quantity zero should be treated as remove item.
- The assistant must confirm before updating quantity.
- The assistant should explain price changes when possible.

---

### `clear_cart`

Clears all cart items after confirmation.

#### Input

```json
{}
```

#### Output

```json
{
  "success": "boolean",
  "message": "string"
}
```

#### Acceptance Criteria

- The assistant must ask for explicit confirmation.
- The confirmation UI should clearly state that all cart items will be removed.
- The action should be easy to cancel.

---

### `apply_discount_code`

Applies a discount code to the current cart.

#### Input

```json
{
  "discountCode": "string"
}
```

#### Output

```json
{
  "success": "boolean",
  "message": "string",
  "subtotalBeforeDiscount": "number | null",
  "subtotalAfterDiscount": "number | null",
  "currency": "string | null"
}
```

#### Acceptance Criteria

- The assistant must confirm before applying the discount.
- The assistant must show whether the discount was accepted or rejected.
- The assistant must not invent discount values.
- If discount validation fails, the assistant should explain the failure message returned by the backend/use case.

---

### `open_checkout`

Starts the checkout flow.

#### Input

```json
{}
```

#### Output

```json
{
  "success": "boolean",
  "checkoutUrl": "string | null",
  "message": "string"
}
```

#### Acceptance Criteria

- The assistant must confirm before opening checkout.
- Checkout must only open if the cart is not empty.
- Checkout should use the app’s existing checkout flow.
- If using Shopify Checkout Kit, the assistant should only trigger the app flow, not handle payment directly.

---

### `toggle_wishlist`

Adds or removes a product from wishlist.

#### Input

```json
{
  "productId": "string",
  "targetState": "saved | unsaved"
}
```

#### Output

```json
{
  "success": "boolean",
  "message": "string"
}
```

#### Acceptance Criteria

- The assistant should confirm before changing wishlist state unless the product page already has clear user intent.
- The wishlist UI should update after success.
- The assistant should summarize the result.

---

## Phase 2 Android Requirements

### UI Requirements

The chat UI should support confirmation cards.

A confirmation card should include:

- Action title
- Product name or affected item
- Price if relevant
- Quantity if relevant
- Confirm button
- Cancel button

Example:

```text
Add to cart?

Product: Black Cotton Hoodie
Size: M
Quantity: 1
Price: 850 EGP

[Confirm] [Cancel]
```

---

## Phase 2 ViewModel Requirements

The `AiChatViewModel` should support:

```kotlin
sealed interface AiChatIntent {
    data class SendMessage(val text: String) : AiChatIntent
    data class ConfirmAction(val actionId: String) : AiChatIntent
    data class CancelAction(val actionId: String) : AiChatIntent
    data object RetryLastMessage : AiChatIntent
    data object ClearChat : AiChatIntent
}
```

State should include pending actions:

```kotlin
data class AiChatState(
    val messages: List<AiMessageUiModel> = emptyList(),
    val pendingActions: List<PendingAiAction> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)
```

---

## Phase 2 Safety Requirements

The assistant must require confirmation for:

- Adding items to cart
- Removing items from cart
- Changing item quantity
- Clearing cart
- Applying discount codes
- Opening checkout
- Saving user preferences
- Any action that changes user account, cart, wishlist, or checkout state

The assistant must not:

- Complete payment
- Cancel orders without deterministic backend flow
- Issue refunds
- Modify customer account data without confirmation
- Make unsupported promises about delivery, refund, or availability
- Invent product data
- Invent discounts
- Bypass app permissions

---

## Phase 2 Acceptance Criteria

Phase 2 is complete when:

- The assistant can add a product to cart after confirmation.
- The assistant can update cart quantity after confirmation.
- The assistant can remove an item from cart after confirmation.
- The assistant can summarize the cart.
- The assistant can apply a discount code after confirmation.
- The assistant can start checkout after confirmation.
- The user can cancel any pending action.
- Failed actions return clear error messages.
- The app state updates after successful actions.
- No state-changing action happens without user confirmation.

---

# Optional: `llm-client` Refactor

## Objective

Improve the current `llm-client` module so it is safer, easier to test, and more suitable for production AI tool-calling flows.

This refactor is optional if the app will use a backend AI gateway and the Android app will not directly call LLM providers.

However, the refactor is recommended if the module will remain part of the Android codebase.

---

## Recommended Changes

### 1. Add Response Metadata

Current chat responses return only the assistant message.

Refactor to return a richer response:

```kotlin
data class LlmResponse(
    val message: ChatMessage,
    val model: String?,
    val finishReason: String?,
    val usage: UsageInfo?,
    val responseId: String?
)
```

```kotlin
data class UsageInfo(
    val promptTokens: Int?,
    val completionTokens: Int?,
    val totalTokens: Int?
)
```

#### Reason

This allows:

- Token tracking
- Cost monitoring
- Better debugging
- Better history trimming
- Better retry decisions

---

### 2. Fix Duplicate Streaming Terminal Events

The stream should emit only one terminal event:

```kotlin
sealed interface StreamChunk {
    data class Delta(val content: String) : StreamChunk
    data class ToolCallDelta(...) : StreamChunk
    data class Done(val finishReason: String?) : StreamChunk
    data class Error(val error: LlmError) : StreamChunk
}
```

#### Requirement

- Emit `Done` once.
- Do not emit both `Done(finishReason = "stop")` and `Done(null)` from `[DONE]`.
- If the stream ends unexpectedly, emit `StreamInterrupted`.

---

### 3. Stop Stream Correctly After Errors

When `StreamChunk.Error` is emitted, the stream should not continue to emit chunks or `Done`.

#### Requirement

- Stop the stream immediately after error.
- Add tests for stream error behavior.

---

### 4. Wrap Tool Execution Exceptions

Tool execution should not crash the orchestrator.

#### Required Behavior

```kotlin
val output = runCatching {
    toolExecutor(request)
}.getOrElse { throwable ->
    return Result.failure(throwable)
}
```

For streaming:

```kotlin
emit(StreamChunk.Error(LlmError.ToolExecutionFailed(...)))
```

---

### 5. Add Typed Tool Registry

Replace raw tool execution with a typed registry.

```kotlin
interface AiTool {
    val name: String
    val description: String
    val parametersSchema: JsonObject
    val requiresConfirmation: Boolean
    suspend fun execute(arguments: JsonObject): AiToolResult
}
```

```kotlin
data class AiToolResult(
    val success: Boolean,
    val content: String,
    val structuredData: JsonObject? = null
)
```

#### Benefits

- Easier validation
- Safer execution
- Better testability
- Cleaner mapping to domain use cases

---

### 6. Add Tool Argument Validation

Before executing a tool:

- Validate required fields.
- Validate field types.
- Validate enum values.
- Reject unknown dangerous operations.
- Return structured errors instead of crashing.

Example:

```kotlin
sealed interface ToolValidationResult {
    data object Valid : ToolValidationResult
    data class Invalid(val reason: String) : ToolValidationResult
}
```

---

### 7. Add Timeout Configuration

Add HTTP timeout support.

```kotlin
HttpTimeout {
    requestTimeoutMillis = config.requestTimeoutMillis
    connectTimeoutMillis = config.connectTimeoutMillis
    socketTimeoutMillis = config.socketTimeoutMillis
}
```

Config:

```kotlin
data class LlmTimeoutConfig(
    val requestTimeoutMillis: Long = 60_000,
    val connectTimeoutMillis: Long = 15_000,
    val socketTimeoutMillis: Long = 60_000
)
```

---

### 8. Improve Retry Handling

Current retry logic should be extended to:

- Respect `Retry-After` headers for `429`.
- Avoid retrying invalid request errors.
- Retry transient network failures.
- Retry `5xx` provider errors.
- Use capped exponential backoff.

---

### 9. Extract SSE Parsing Into a Pure Component

Create a pure parser for stream chunks.

```kotlin
interface LlmStreamParser {
    fun parseLine(line: String): StreamParseResult
}
```

This makes streaming easier to unit test without real network calls.

---

### 10. Add More Tests

Required tests:

- Non-streaming success response
- Non-streaming provider error
- Retry on transient error
- No retry on invalid request
- Streaming text chunks
- Streaming tool-call chunks
- Streaming emits only one terminal event
- Streaming interrupted before terminal event
- Tool call accumulation
- Tool execution failure
- Tool argument validation failure
- Multiple tool calls in one assistant turn
- Max tool-round limit

---

## Suggested Final Architecture

```text
Android App
  presentation/ai
    AiChatScreen
    AiChatViewModel
    AiChatState
    AiChatIntent

  domain/ai
    AiAssistantUseCase
    AiToolRegistry
    AiTool
    PendingAiAction

  data/ai
    AiRepositoryImpl
    AiGatewayDataSource

Backend
  AiGatewayController
  LlmProviderClient
  ToolExecutionService
  ShopifyToolService
  PolicySearchService
  UsageTrackingService
```

---

# Implementation Priority

## Required First

1. Build AI chat screen.
2. Add `AiChatViewModel`.
3. Add `AiAssistantUseCase`.
4. Add backend/gateway communication.
5. Add read-only tools:
   - `search_products`
   - `get_categories`
   - `get_product_details`
   - `compare_products`
   - `get_store_policy`
6. Display product suggestions in chat.
7. Add error and retry handling.

## Required Second

1. Add pending action model.
2. Add confirmation UI.
3. Add cart action tools:
   - `add_to_cart`
   - `remove_from_cart`
   - `update_cart_quantity`
   - `clear_cart`
4. Add wishlist action.
5. Add discount action.
6. Add checkout action.
7. Block all state-changing tools unless confirmed.

## Optional

1. Refactor `llm-client`.
2. Add response metadata.
3. Fix streaming terminal behavior.
4. Add typed tool registry.
5. Add validation.
6. Add stronger tests.
7. Move provider calls fully behind backend if not already done.

---

# Definition of Done

The first two phases are complete when:

- The user can chat with the assistant.
- The assistant can search and explain products.
- The assistant can compare products.
- The assistant can answer policy questions.
- The assistant can summarize cart contents.
- The assistant can prepare cart and wishlist actions.
- The user must confirm before any state-changing action.
- The app executes confirmed actions using domain use cases.
- The assistant summarizes the result after each action.
- No LLM provider API key is exposed in the Android app.
- Errors are handled without crashing the chat.
- Product and cart data are never invented by the assistant.
