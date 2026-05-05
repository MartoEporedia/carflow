# CarFlow - Architettura e Deploy

## Panoramica

CarFlow è un'app Android per il tracciamento delle spese automobilistiche con parsing intelligente delle spese tramite NLP. Supporta due modalità di parsing:

1. **Rule-based (offline)** — Parser deterministico con regex e keyword dictionary
2. **LLM (online)** — Parsing tramite modelli linguistici con fallback automatico al rule-based

---

## Architettura

### Diagramma completo

```
┌──────────────────────────────────────────────────────────────────────────┐
│                         Android App (CarFlow)                            │
│                                                                          │
│  ┌────────────────────────────────────────────────────────────────────┐  │
│  │                     UI Layer (Compose)                             │  │
│  │  ExpenseInputScreen ──┬─── ParserTab (NLP)                        │  │
│  │                       └─── FuelFormTab (Manuale)                   │  │
│  │  LlmSettingsScreen ──────── Configurazione LLM                     │  │
│  └────────────────────────────────┬───────────────────────────────────┘  │
│                                   │                                      │
│  ┌────────────────────────────────v───────────────────────────────────┐  │
│  │                    ViewModel Layer                                 │  │
│  │  ExpenseInputViewModel                                             │  │
│  │    ├── parseExpense() ── seleziona parser attivo                   │  │
│  │    ├── isParsing StateFlow ── loading indicator                    │  │
│  │    └── saveExpense() ── salva su Room DB                           │  │
│  └────────────────────────────────┬───────────────────────────────────┘  │
│                                   │                                      │
│  ┌────────────────────────────────v───────────────────────────────────┐  │
│  │                     Parser Layer                                   │  │
│  │                                                                    │  │
│  │  ┌─────────────────────┐    ┌──────────────────────────────────┐  │  │
│  │  │ ExpenseParser       │    │ LlmExpenseParser                 │  │  │
│  │  │ (Rule-based)        │    │                                  │  │  │
│  │  │                     │    │  ┌─ LlmConfigResolver            │  │  │
│  │  │  Normalizer         │    │  │   ├── Mode DIRECT?             │  │  │
│  │  │  TokenExtractor     │    │  │   │   → DirectLlmClient       │  │  │
│  │  │  CategoryClassifier │    │  │   └── Mode PROXY?             │  │  │
│  │  │  DateParser         │    │  │       → ProxyLlmClient        │  │  │
│  │  │                     │    │  └─ Fallback chain               │  │  │
│  │  │  Output: ParsedExpense     │  │   (se errore → rule-based)   │  │  │
│  │  └─────────────────────┘    └──────────────────────────────────┘  │  │
│  └────────────────────────────────────────────────────────────────────┘  │
│                                   │                                      │
│  ┌────────────────────────────────v───────────────────────────────────┐  │
│  │                     Data Layer                                     │  │
│  │  ┌──────────────────┐  ┌──────────────────┐  ┌──────────────────┐ │  │
│  │  │ Room DB          │  │ LlmSettings      │  │ AuthRepository   │ │  │
│  │  │ (expenses,       │  │ (EncryptedShared │  │ (session token,  │ │  │
│  │  │  vehicles)       │  │  Preferences)    │  │  auth state)     │ │  │
│  │  └──────────────────┘  └──────────────────┘  └──────────────────┘ │  │
│  └────────────────────────────────────────────────────────────────────┘  │
└──────────────────────────────────────────────────────────────────────────┘
         │                                                │
         │ Mode DIRECT                                    │ Mode PROXY
         │                                                │
         ▼                                                ▼
┌────────────────────────────────┐          ┌────────────────────────────────┐
│     Provider LLM (esterno)     │          │   Cloudflare Workers           │
│                                │          │   (100K req/giorno free)       │
│  OpenAI    (gpt-4o-mini)      │          │                                │
│  Anthropic (claude-3.5-haiku) │          │  POST /api/parse  → LLM Proxy  │
│  Groq      (llama-3.3-70b)    │◄─────────┤  POST /api/auth/* → Better Auth│
│  OpenRouter (100+ modelli)    │          │  GET  /api/auth/session        │
│  Ollama Cloud (open source)   │          │                                │
└────────────────────────────────┘          └───────────────┬────────────────┘
                                                            │
                                                            ▼
                                                  ┌────────────────────────┐
                                                  │  Turso DB (D1 SQLite)  │
                                                  │  (50MB free, 9M righe) │
                                                  │                        │
                                                  │  users                 │
                                                  │  sessions              │
                                                  │  accounts (OAuth)      │
                                                  │  subscriptions         │
                                                  │  verifications         │
                                                  └────────────────────────┘
```

### Multi-module Gradle

```
:app                    → Android app (Compose, Room, Hilt)
:shared:parser          → Kotlin JVM, parser rule-based (zero dipendenze esterne)
:shared:network         → Kotlin JVM, client LLM (Ktor + serialization)
```

Il parser module è isolato e platform-independent. Il network module dipende dal parser module e aggiunge Ktor Client per le chiamate HTTP.

### Dependency Injection (Hilt)

```
ParserModule
├── @Named("default") ExpenseParser          → Rule-based parser (singleton)
├── @Named("llm") ExpenseParserStrategy      → LLM parser con fallback (singleton)
├── LlmSettings                              → EncryptedSharedPreferences (singleton)
└── LlmConfigResolver                        → Risolve config in base a mode/settings

AuthModule
└── AuthRepository                           → Gestione token auth (singleton)
```

---

## Flusso di parsing

### 1. Utente digita testo e preme "Analizza"

```
ExpenseInputViewModel.parseExpense(input)
    │
    ├─ viewModelScope.launch { _isParsing = true }
    │
    ├─ getActiveParser()
    │   │
    │   ├─ LlmSettings.getMode() == DIRECT && hasDirectConfig()
    │   │   └─→ LlmExpenseParser
    │   │
    │   └─ Altrimenti
    │       └─→ ExpenseParser (rule-based, wrapper come ExpenseParserStrategy)
    │
    ├─ parser.parse(input)
    │   │
    │   ├─ Se LLM: LlmExpenseParser.parse()
    │   │   ├─ configResolver.resolve() → LlmConfig.Direct o LlmConfig.Proxy
    │   │   ├─ LlmClientFactory.create(config) → DirectLlmClient o ProxyLlmClient
    │   │   ├─ client.chat(SYSTEM_PROMPT, userPrompt) → JSON response
    │   │   ├─ Json.decodeFromString<ParsedExpense>(response)
    │   │   └─ Se errore → catch → fallbackParser.parse(input)
    │   │
    │   └─ Se Rule-based: ExpenseParser.parse()
    │       ├─ Normalizer → TokenExtractor → CategoryClassifier → DateParser
    │       └─→ ParsedExpense
    │
    ├─ _parsedExpense.value = result
    └─ _isParsing = false
```

### 2. Fallback chain

```
Tenta LLM parser
    │
    ├─ UnconfiguredException → "LLM not configured" → Rule-based parser
    ├─ UnauthenticatedException → "Auth required" → Rule-based parser
    ├─ NetworkException → "Network error" → Rule-based parser
    ├─ SerializationException → "Invalid JSON" → ParsedExpense(UNKNOWN, LOW)
    └─ Successo → ParsedExpense da LLM
```

---

## Provider LLM supportati

| Provider | Endpoint | Auth | Modelli consigliati | Note |
|---|---|---|---|---|
| **OpenRouter** | `openrouter.ai/api/v1/chat/completions` | Bearer token | `openai/gpt-4o-mini`, `anthropic/claude-3.5-haiku`, `meta-llama/llama-3.1-8b-instruct:free` | **Default consigliato**: 1 API key, 100+ modelli, tier free |
| **OpenAI** | `api.openai.com/v1/chat/completions` | Bearer token | `gpt-4o-mini`, `gpt-4o` | Standard, affidabile |
| **Anthropic** | `api.anthropic.com/v1/messages` | x-api-key header | `claude-3-5-haiku-20241022`, `claude-3-5-sonnet-20241022` | Ottimo per parsing strutturato |
| **Groq** | `api.groq.com/openai/v1/chat/completions` | Bearer token | `llama-3.3-70b-versatile`, `mixtral-8x7b-32768` | Ultra veloce (inferenza su LPU) |
| **Ollama Cloud** | `cloud.ollama.com/api/chat` | Bearer token | `llama3.1:8b`, `mistral:7b` | Open source, self-hostable |

### Perché OpenRouter è il default

- **1 API key per 100+ modelli** — non serve registrarsi su ogni provider
- **Modelli gratuiti** — `meta-llama/llama-3.1-8b-instruct:free` è gratis e illimitato
- **Fallback automatico** — se un modello è down, OpenRouter route su un altro provider
- **Perfetto per open source** — l'utente sceglie il modello in base a costo/qualità

---

## Infrastruttura Backend

### Stack

| Componente | Tecnologia | Free Tier | Costo oltre free |
|---|---|---|---|
| **Compute** | Cloudflare Workers | 100K req/giorno | $5/mese per 10M req |
| **Database** | Turso (D1 SQLite) | 50MB, 9M righe | $9/mese per 5GB |
| **Auth** | Better Auth + GitHub OAuth | Gratis | Gratis |
| **LLM Proxy** | OpenRouter API | Modelli free illimitati | Pay-per-use (~$0.001/req) |
| **Framework** | Hono (TypeScript) | Open source | Open source |

### Totale costi

| Utenti attivi/giorno | Costo mensile |
|---|---|
| 0 - 3,000 | **$0** |
| 3,000 - 30,000 | ~$5-15 (Cloudflare Workers upgrade) |
| 30,000+ | ~$15-50 ( Workers + Turso upgrade + LLM costs) |

### Struttura backend

```
backend/
├── src/
│   ├── index.ts              # Hono app, CORS, error handling, routing
│   ├── lib/
│   │   ├── auth.ts           # Better Auth config (GitHub OAuth, sessions)
│   │   └── d1-dialect.ts     # Adapter D1 per Better Auth
│   └── routes/
│       └── parse.ts          # Endpoint LLM proxy con auth check
├── migrations/
│   └── 001_initial.sql       # Schema: users, sessions, accounts, subscriptions, verifications
├── wrangler.toml             # Cloudflare Workers config (D1 binding, vars)
├── package.json
├── tsconfig.json
└── README.md
```

### Endpoint API

#### POST /api/parse

Richiede autenticazione (sessione attiva).

**Request:**
```json
{
  "systemPrompt": "You are an expense parser...",
  "userPrompt": "Today's timestamp is 1712160000000. Parse this expense: \"benzina 50€ ieri\""
}
```

**Response (200):**
```json
{
  "json": "{\"category\":\"FUEL\",\"amount\":50.0,\"description\":\"benzina\",...}"
}
```

**Errori:**
- `400` — Request invalida
- `401` — Non autenticato
- `502` — Risposta vuota da LLM
- `503` — LLM non configurato sul server
- `500` — Errore interno

#### POST /api/auth/github

Inizia il flusso OAuth con GitHub. Redirecta a GitHub per l'autenticazione.

#### GET /api/auth/session

Restituisce la sessione corrente se l'utente è autenticato.

---

## Deploy

### Prerequisiti

- **Node.js 20+** (per il backend)
- **Android Studio** (per l'app)
- **Account Cloudflare** (gratuito)
- **Account GitHub** (per OAuth)
- **Account OpenRouter** (gratuito, per API key)

### 1. Deploy Backend

#### 1.1 Setup Cloudflare

```bash
# Installa Wrangler CLI
npm install -g wrangler

# Login a Cloudflare
wrangler login
```

#### 1.2 Crea il database D1

```bash
cd backend

# Crea il database
wrangler d1 create carflow-db

# Output:
# ✅ Successfully created DB 'carflow-db' with ID 'xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx'

# Copia il database_id e incollalo in wrangler.toml:
# database_id = "xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx"
```

#### 1.3 Configura le variabili d'ambiente

```bash
# Genera un secret per Better Auth
openssl rand -base64 32
# Output: abc123... (copia questo valore)

# Crea i secrets su Cloudflare
wrangler secret put BETTER_AUTH_SECRET
# Incolla il valore generato sopra

wrangler secret put OPENROUTER_API_KEY
# Incolla la tua API key da https://openrouter.ai/keys

wrangler secret put GITHUB_CLIENT_ID
# Dalla tua GitHub OAuth App: https://github.com/settings/developers

wrangler secret put GITHUB_CLIENT_SECRET
# Dalla tua GitHub OAuth App
```

#### 1.4 Configura GitHub OAuth

1. Vai su https://github.com/settings/developers
2. Clicca "New OAuth App"
3. Compila:
   - **Application name**: `CarFlow`
   - **Homepage URL**: `https://carflow.app` (o `http://localhost:8787` per dev)
   - **Authorization callback URL**: `https://<your-worker>.workers.dev/api/auth/callback/github`
4. Copia Client ID e Client Secret
5. Salvali come secrets (vedi step 1.3)

#### 1.5 Applica le migrazioni

```bash
# Localmente (per test)
wrangler d1 migrations apply carflow-db --local

# In produzione
wrangler d1 migrations apply carflow-db --remote
```

#### 1.6 Deploy

```bash
# Test locale
npm run dev
# Il server parte su http://localhost:8787

# Deploy in produzione
npm run deploy
# Output: https://carflow-api.<your-subdomain>.workers.dev
```

#### 1.7 Verifica

```bash
curl https://carflow-api.<your-subdomain>.workers.dev/api/auth/session
# Dovrebbe restituire: {"error":"Unauthorized"} (normale, non sei autenticato)
```

### 2. Setup App Android

#### 2.1 Configura il proxy URL

Aggiungi in `app/build.gradle.kts` dentro `defaultConfig`:

```kotlin
buildConfigField("String", "PROXY_BASE_URL", "\"https://carflow-api.<your-subdomain>.workers.dev\"")
```

#### 2.2 Build

```bash
# Debug
./gradlew assembleDebug

# Release
./gradlew assembleRelease
```

#### 2.3 Installa sul dispositivo

```bash
./gradlew installDebug
```

---

## Configurazione LLM nell'app

### Modalità Proxy (default)

L'utente si autentica tramite GitHub OAuth → il backend verifica la sessione → chiama l'LLM → ritorna il risultato.

**Vantaggi:**
- Zero configurazione per l'utente
- Tu controlli quale modello viene usato
- Puoi implementare rate limiting e billing

**Svantaggi:**
- Richiede il backend deployato
- Costi a tuo carico (minimi nel free tier)

### Modalità Diretta

L'utente inserisce la propria API key nelle impostazioni → le chiamate partono direttamente dal dispositivo al provider LLM.

**Vantaggi:**
- Zero costi per te
- Privacy: le richieste non passano dal tuo server
- Funziona anche senza backend

**Svantaggi:**
- L'utente deve avere una propria API key
- Frizione nell'onboarding

### Come l'utente configura la modalità diretta

1. Apre Impostazioni → LLM
2. Seleziona "Diretta"
3. Inserisce API key (es. da OpenRouter)
4. Seleziona provider (OpenRouter, OpenAI, Anthropic, Groq, Ollama Cloud)
5. Inserisce il modello (es. `openai/gpt-4o-mini`)
6. Salva → le chiamate parsate usano ora il LLM diretto

---

## Estendere il sistema

### Aggiungere un nuovo provider LLM

1. Aggiungi il provider in `LlmProvider.kt`:

```kotlin
data object Google : LlmProvider {
    override val id = "google"
    override val name = "Google AI"
    override val endpoint = "https://generativelanguage.googleapis.com/v1beta/models/gemini-pro:generateContent"
    override val authHeaderName = "x-goog-api-key"
    override val requiresVersionHeader = false
}
```

2. Aggiungi la logica di chiamata in `DirectLlmClient.kt`:

```kotlin
is LlmProvider.Google -> chatGoogle(url, systemPrompt, userPrompt)
```

### Aggiungere un nuovo campo a ParsedExpense

1. Modifica `ParsedExpense.kt` nel modulo parser
2. Aggiorna il system prompt in `LlmPrompt.kt`
3. Aggiorna il parser rule-based se necessario

### Aggiungere rate limiting al backend

Cloudflare Workers supporta rate limiting nativo:

```typescript
// wrangler.toml
[[unsafe.bindings]]
name = "RATE_LIMITER"
type = "ratelimit"
namespace_id = "1001"
simple = { limit = 100, period = 3600 }

// src/routes/parse.ts
const success = await c.env.RATE_LIMITER.limit({ key: session.user.id })
if (!success) {
  return c.json({ error: 'Rate limit exceeded' }, 429)
}
```

---

## Troubleshooting

### LLM restituisce JSON invalido

Il `LlmExpenseParser` ha già un fallback: se il JSON non è parsabile, ritorna un `ParsedExpense` con categoria UNKNOWN e confidence LOW. Il system prompt è ottimizzato per ridurre questo problema.

### Errore "LLM not configured"

Significa che:
- Modalità DIRECT: nessuna API key salvata nelle impostazioni
- Modalità PROXY: l'utente non è autenticato o il backend non ha `OPENROUTER_API_KEY` configurato

### Errore di build Gradle

Assicurati che `settings.gradle.kts` includa tutti i moduli:

```kotlin
include(":app")
include(":shared:parser")
include(":shared:network")
```

### Cloudflare Workers: errore 1101

Significa che il worker ha usato troppa CPU. Il parsing LLM è una singola chiamata HTTP, non dovrebbe succedere. Se succede, aumenta il timeout in `wrangler.toml`:

```toml
[limits]
cpu_ms = 50
```

---

## Checklist deploy

- [ ] Account Cloudflare creato
- [ ] Wrangler CLI installato e login effettuato
- [ ] Database D1 creato (`wrangler d1 create`)
- [ ] `database_id` aggiornato in `wrangler.toml`
- [ ] Secrets configurati (`BETTER_AUTH_SECRET`, `OPENROUTER_API_KEY`, `GITHUB_CLIENT_ID`, `GITHUB_CLIENT_SECRET`)
- [ ] GitHub OAuth App creata con callback URL corretto
- [ ] Migrazioni applicate (`wrangler d1 migrations apply --remote`)
- [ ] Backend deployato (`npm run deploy`)
- [ ] `PROXY_BASE_URL` aggiornato in `app/build.gradle.kts`
- [ ] App buildata e installata (`./gradlew installDebug`)
- [ ] Test modalità DIRECT con API key OpenRouter
- [ ] Test modalità PROXY con autenticazione GitHub
