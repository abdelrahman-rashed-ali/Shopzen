# Plan: Reusable Ktor LLM Client Package (`:llm-client`)

## What changed from the original plan

The original plan is a reasonable skeleton but has three structural gaps that
will surface as soon as this client touches a real API:

1. **No error model.** `Result<ChatCompletionResponse>` was mentioned but
   never defined — what's inside the failure case? Auth errors, rate limits,
   and malformed JSON all need different handling by the caller.
2. **Tool calling was treated as "just data classes."** The hard part of tool
   calling isn't the model shapes, it's the *orchestration loop* — and in
   streaming mode, tool-call arguments arrive as fragmented deltas keyed by
   index that must be reassembled before they're valid JSON. This was
   missing entirely.
3. **The Gemini/OpenAI "same code" assumption is stated as fact.** It's close
   to true, but Google's own docs describe real gaps (silently ignored
   parameters, tool-surface differences, compat layer covering only Chat
   Completions). This needs a validation step, not an assumption baked into
   the architecture.

The revised plan below adds a design/validation phase up front, splits error
handling into its own phase (everything downstream depends on it), and gives
tool-calling orchestration a dedicated phase separate from the data models.

---

## Phase 0 — Design Decisions & Provider Compatibility Validation

*Goal: resolve the open architectural questions before any code is committed,
so later phases aren't built on shaky assumptions.*

- **KMP vs JVM-only.** Decide whether `:llm-client` is a plain JVM module or
  a Kotlin Multiplatform module with `commonMain`/`androidMain` source sets.
  Given the rest of the portfolio leans KMP, it's worth deciding this once,
  deliberately, rather than defaulting to Android-only and having to
  re-platform later.
- **Provider compatibility check.** Before committing to "one code path for
  both providers," run a small manual smoke test against both OpenAI and the
  Gemini OpenAI-compat endpoint using the exact request shapes this client
  will send (including a tool-calling request and a streaming request).
  Confirm: which fields are silently dropped, whether streaming tool-call
  deltas are chunked the same way, and whether the same auth header works
  for both. Capture the answer as a short compatibility note in the module's
  README rather than as an assumption in the code.
- **Provider configuration shape.** Decide whether provider differences
  (base URL, auth header style, model name quirks) are handled via a single
  config object passed to the client, or via a small `Provider` enum/sealed
  type with per-provider defaults. This decision drives Phase 4's
  constructor shape.
- **Error model shape.** Decide the sealed hierarchy for failures now (see
  Phase 3) so every later phase can reference it instead of inventing ad hoc
  exceptions.

**Deliverable:** a short decision record (can live at the top of the module
README) covering these four points, so future contributors don't have to
reverse-engineer the reasoning.

---

## Phase 1 — Module & Build Setup

*Goal: establish the module boundary and dependencies.*

- Add the SSE client dependency alongside the existing Ktor dependencies in
  the version catalog.
- Create the `:llm-client` module per the Phase 0 platform decision (JVM-only
  or KMP). Keep its dependency surface minimal: Ktor core, SSE, content
  negotiation, kotlinx-serialization, kotlinx-coroutines. No Android-only or
  app-specific dependencies — this module should be droppable into any other
  project unchanged.
- Register the module in the settings file.
- Confirm the module builds standalone (no dependency on `:app` or other
  ShopZen modules) — this is the actual test of "reusable."

**Deliverable:** an empty but buildable module, wired into the multi-module
graph, with zero circular dependencies on app-level modules.

---

## Phase 2 — Domain & Wire Models

*Goal: define the data shapes, with an explicit split between "what the wire
format looks like" and "what's convenient to use."*

- **Wire models** mirror the OpenAI JSON schema exactly: messages (role,
  content, tool calls), tool/function definitions, request envelope
  (model, messages, tools, temperature, stream flag), response envelope
  (choices, usage), and the streaming delta/chunk shape.
- **Serialization configuration** is a first-class concern here, not an
  afterthought: unknown keys must be ignored (providers add fields over
  time), missing optional fields must not blow up parsing, and fields that
  are legitimately nullable (e.g., message content is null when a tool call
  is present instead) need to be modeled as nullable, not defaulted away.
- Decide whether callers interact with the wire models directly or whether
  there's a thin domain-model layer on top (e.g., a `ChatMessage` sealed
  type distinguishing text/tool-call/tool-result messages) that's easier to
  pattern-match on than the wire shape. For a reusable library, the domain
  layer is worth the extra code — it keeps OpenAI's JSON quirks from leaking
  into every call site.

**Deliverable:** wire models + serializer configuration, with a short test
fixture (a few hand-written JSON examples from real API responses) used to
validate parsing in Phase 8.

---

## Phase 3 — Error & Result Modeling

*Goal: give every downstream phase one shared vocabulary for failure, before
any HTTP code is written.*

- Define a sealed error hierarchy distinguishing at least: network/transport
  failure, authentication failure, rate limiting (with retry-after info if
  the provider supplies it), a generic API error (status code + provider
  error message), and a deserialization/parsing failure (raw payload
  attached for debugging).
- Decide the public shape callers see: `Result<T, LlmError>`-style return
  for the non-streaming call, and a sealed emission type for the streaming
  `Flow` (since a stream can succeed for a while and then fail mid-way —
  that's a distinct case from failing before any tokens arrive).
- This phase produces no HTTP logic yet — it's purely the contract that
  Phases 4–6 build against.

**Deliverable:** the error/result types, documented with what each case means
and when a caller should retry vs. surface the error vs. give up.

---

## Phase 4 — Non-Streaming Completion Client

*Goal: prove basic connectivity, auth, and JSON round-tripping before adding
streaming complexity on top.*

- Define the client interface's non-streaming method against the Phase 3
  result type.
- Implement the HTTP call: POST to the completions endpoint, provider-aware
  auth header attachment (per the Phase 0 decision), and mapping of non-2xx
  responses into the Phase 3 error hierarchy rather than throwing raw HTTP
  exceptions.
- Configure sensible defaults on the underlying `HttpClient`: request
  timeout, and a content negotiation setup that matches the Phase 2
  serializer configuration exactly (a mismatch here is a classic source of
  "works in tests, fails against the real API" bugs).
- Decide ownership: does the module construct and own its `HttpClient`, or
  does it accept one via constructor injection so the host app can share a
  single engine/connection pool across features? For a module meant to slot
  into an existing Hilt-based app, accepting an injected client is the more
  reusable choice.

**Deliverable:** a working non-streaming call against a real provider,
manually verified once, with errors correctly classified.

---

## Phase 5 — Streaming (SSE) Implementation

*Goal: implement token-by-token streaming, including the edge cases that
don't show up until you stream against a real, long response.*

- Install the SSE plugin and implement the streaming method as a `Flow` of
  the Phase 3 streaming emission type.
- Handle the provider-specific stream termination convention (a sentinel
  "done" event distinct from a normal JSON chunk) so the flow completes
  cleanly instead of trying to parse a non-JSON terminator.
- Handle partial/interleaved SSE comment or keep-alive lines that aren't
  data payloads at all — these should be skipped, not treated as parse
  failures.
- Make sure Flow cancellation (e.g., the caller navigates away mid-stream)
  actually cancels the underlying HTTP connection rather than leaking it.
- Use a longer timeout configuration specifically for the streaming call —
  the default request timeout that's fine for Phase 4 will be too short for
  a long streamed generation.

**Deliverable:** a streaming call that reliably starts, emits incremental
chunks, terminates cleanly on completion, and cancels cleanly on client-side
cancellation.

---

## Phase 6 — Tool-Calling Orchestration

*Goal: this is the phase the original plan collapsed into "data models." It
deserves its own build-out because the mechanics differ meaningfully between
non-streaming and streaming.*

- **Non-streaming tool calls** are relatively simple: the response either
  contains a final message or a list of tool calls to execute; the caller
  executes them and sends the results back as new messages in a follow-up
  request. Define the shape of that follow-up turn explicitly (how tool
  results get attached to the conversation so the model can see them).
- **Streaming tool calls** are the genuinely tricky part: tool-call
  arguments arrive as string fragments across multiple chunks, identified by
  an index rather than delivered whole. This phase needs an explicit
  accumulator responsible for collecting fragments by index and only
  yielding a complete, valid tool call once the stream signals that call is
  finished. Get this wrong and the failure mode is silent — you get
  truncated or invalid JSON arguments that only show up as bugs under
  real-world latency, not in a quick manual test.
- Decide whether the orchestration loop (call model → detect tool calls →
  execute tools → call model again with results → repeat until a final
  answer) lives inside this module as a helper, or is left entirely to the
  host app with this module only exposing the primitives. For a *reusable*
  library, exposing both — low-level primitives and an optional
  higher-level loop helper — is usually the right balance.

**Deliverable:** verified round-trip tool-calling in both non-streaming and
streaming modes, including a case with multiple parallel tool calls in a
single turn.

---

## Phase 7 — Resilience: Retries, Timeouts, Logging

*Goal: the difference between a demo client and one that survives real
network conditions.*

- Add retry-with-backoff for transient failures (network blips, 5xx) and
  respect provider-supplied rate-limit signals rather than retrying blindly
  into a 429.
- Confirm timeout values are distinct and intentional for non-streaming vs.
  streaming calls (Phase 4 vs Phase 5), not one shared default.
- Add request/response logging for debugging, with the API key explicitly
  redacted before anything is logged — this is easy to forget and easy to
  regret.

**Deliverable:** documented retry/timeout policy, with logging that's safe
to leave enabled in debug builds.

---

## Phase 8 — Testing Strategy

*Goal: verify the module in isolation, without depending on a live API key
in CI.*

- **Serialization tests** using the real-world JSON fixtures captured in
  Phase 2 (request shape with tools included, response shape, streaming
  chunk shape) — verifying round-trip correctness, not just "it compiles."
- **Mock-engine tests** covering: a successful non-streaming response, a
  successful streaming response, each Phase 3 error case (auth failure,
  rate limit, malformed JSON, mid-stream failure), and Flow cancellation
  behavior.
- **Tool-call accumulator tests** specifically targeting the streaming
  fragment-reassembly logic from Phase 6 — this is the single highest-risk
  piece of logic in the whole module and deserves tests that feed it
  multi-chunk, multi-tool-call sequences deliberately, not just the happy
  path.
- Optional: a manually-triggered (not CI-run) integration smoke test against
  a real provider, gated behind an environment flag, for occasional sanity
  checks after provider-side changes.

**Deliverable:** a test suite that gives confidence in the module without
requiring live credentials in CI.

---

## Phase 9 — Integration & Documentation

*Goal: make the module actually easy for the rest of the app (or a future
project) to adopt.*

- Add a Hilt module in the app layer providing the configured client as a
  singleton, wiring in whichever `HttpClient`/engine the app already uses
  elsewhere, per the Phase 4 ownership decision.
- Write a short module README covering: the Phase 0 compatibility notes,
  how to configure a provider, a minimal non-streaming example description,
  a minimal streaming example description, and how tool-calling
  orchestration is meant to be used. This is what makes the module
  reusable across projects rather than just working once inside ShopZen.

**Deliverable:** the module wired into ShopZen's DI graph and documented well
enough that dropping it into a different app (or the KMP side of the
portfolio) later is a config change, not a re-read of the source.
