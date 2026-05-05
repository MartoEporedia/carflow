# CarFlow Backend - LLM Proxy

Serverless backend per il parsing LLM delle spese. Costruito con **Cloudflare Workers**, **Hono**, **Better Auth** e **Turso (D1)**.

## Architettura

```
┌─────────────────────────────────────────────────┐
│              Cloudflare Workers                  │
│              (100K req/giorno free)              │
│                                                  │
│  POST /api/parse    → LLM Proxy                 │
│  POST /api/auth/*   → Better Auth               │
│  GET  /api/auth/*   → Session check             │
└──────────────────────┬──────────────────────────┘
                       │
┌──────────────────────v──────────────────────────┐
│              Turso DB (D1 SQLite)                │
│              (50MB free, 9M righe)              │
│                                                  │
│  users          → Utenti                        │
│  sessions       → Sessioni auth                 │
│  accounts       → OAuth accounts (GitHub)       │
│  subscriptions  → Piano (free/premium)          │
└─────────────────────────────────────────────────┘
```

## Costi

| Servizio | Free Tier | Sufficiente per |
|---|---|---|
| Cloudflare Workers | 100K req/giorno | ~3000 utenti attivi/giorno |
| Turso DB | 50MB, 9M righe | ~50K utenti |
| OpenRouter | Modelli free illimitati | Parsing illimitato (modelli base) |
| GitHub OAuth | Gratis | Autenticazione |

**Totale: $0/mese** fino a ~3000 utenti attivi/giorno.

## Setup

### 1. Installazione

```bash
cd backend
npm install
```

### 2. Configurazione

Copia `.dev.vars.example` in `.dev.vars`:

```env
# Better Auth
BETTER_AUTH_SECRET=your-secret-key-generate-with-openssl-rand-base64-32

# GitHub OAuth
GITHUB_CLIENT_ID=your-github-oapp-client-id
GITHUB_CLIENT_SECRET=your-github-oapp-client-secret

# LLM Provider (OpenRouter consigliato)
OPENROUTER_API_KEY=your-openrouter-api-key
LLM_MODEL=openai/gpt-4o-mini
```

### 3. Database

```bash
# Crea il database D1
wrangler d1 create carflow-db

# Aggiorna wrangler.toml con il database_id

# Applica le migrazioni
npm run db:migrate
```

### 4. Sviluppo

```bash
npm run dev
```

Il server parte su `http://localhost:8787`.

### 5. Deploy

```bash
npm run deploy
```

## API

### POST /api/parse

Richiede autenticazione (Bearer token nella sessione).

**Request:**
```json
{
  "systemPrompt": "...",
  "userPrompt": "Parse this expense: benzina 50€ ieri"
}
```

**Response:**
```json
{
  "json": "{\"category\":\"FUEL\",\"amount\":50.0,...}"
}
```

### POST /api/auth/github

Inizia il flusso OAuth con GitHub.

### GET /api/auth/session

Verifica la sessione dell'utente.

## Struttura

```
backend/
├── src/
│   ├── index.ts           # Entry point Hono
│   ├── lib/
│   │   ├── auth.ts        # Better Auth config
│   │   └── d1-dialect.ts  # D1 adapter per Better Auth
│   └── routes/
│       └── parse.ts       # LLM proxy endpoint
├── migrations/
│   └── 001_initial.sql    # Schema DB
├── wrangler.toml          # Cloudflare Workers config
├── package.json
└── tsconfig.json
```

## Estendere

### Aggiungere un provider LLM

Modifica `src/routes/parse.ts` per supportare provider multipli:

```typescript
const providers = {
  openrouter: {
    endpoint: 'https://openrouter.ai/api/v1/chat/completions',
    headers: (key: string) => ({ 'Authorization': `Bearer ${key}` }),
  },
  openai: {
    endpoint: 'https://api.openai.com/v1/chat/completions',
    headers: (key: string) => ({ 'Authorization': `Bearer ${key}` }),
  },
}
```

### Rate limiting

Cloudflare Workers supporta rate limiting nativo:

```typescript
import { RateLimiter } from '@cloudflare/workers-types'

const limiter = new RateLimiter({
  type: 'fixedWindow',
  limit: 100,
  window: 3600, // 1 ora
})
```
