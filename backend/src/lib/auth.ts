import { betterAuth } from 'better-auth'
import { drizzleAdapter } from 'better-auth/adapters/drizzle'
import { D1Dialect } from './d1-dialect'
import type { Env } from '../index'

export function createAuth(c: { env: Env }) {
  const dialect = new D1Dialect(c.env.DB)

  return betterAuth({
    database: drizzleAdapter({
      dialect,
      schema: {
        user: {
          tableName: 'users',
          fields: {
            id: 'id',
            email: 'email',
            emailVerified: 'email_verified',
            name: 'name',
            image: 'image',
            createdAt: 'created_at',
            updatedAt: 'updated_at',
          },
        },
        session: {
          tableName: 'sessions',
          fields: {
            id: 'id',
            userId: 'user_id',
            token: 'token',
            expiresAt: 'expires_at',
            ipAddress: 'ip_address',
            userAgent: 'user_agent',
          },
        },
        account: {
          tableName: 'accounts',
          fields: {
            id: 'id',
            userId: 'user_id',
            accountType: 'account_type',
            providerId: 'provider_id',
            accountId: 'account_id',
            accessToken: 'access_token',
            refreshToken: 'refresh_token',
            idToken: 'id_token',
            expiresAt: 'expires_at',
            password: 'password',
          },
        },
        verification: {
          tableName: 'verifications',
          fields: {
            id: 'id',
            identifier: 'identifier',
            value: 'value',
            expiresAt: 'expires_at',
          },
        },
      },
    }),
    secret: c.env.BETTER_AUTH_SECRET,
    emailAndPassword: {
      enabled: true,
    },
    socialProviders: {
      github: {
        clientId: c.env.GITHUB_CLIENT_ID || '',
        clientSecret: c.env.GITHUB_CLIENT_SECRET || '',
      },
    },
    advanced: {
      database: {
        useNumberId: false,
      },
    },
  })
}

export const auth = createAuth({ env: {} as Env })
