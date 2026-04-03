import { z } from 'zod'
import { zValidator } from '@hono/zod-validator'
import { createAuth } from '../lib/auth'

const parseRequestSchema = z.object({
  systemPrompt: z.string().min(1),
  userPrompt: z.string().min(1),
})

export const parseExpense = async (c: any) => {
  const validated = parseRequestSchema.safeParse(await c.req.json())
  if (!validated.success) {
    return c.json({ error: 'Invalid request', details: validated.error }, 400)
  }

  const { systemPrompt, userPrompt } = validated.data

  const authHandler = createAuth(c.env)
  const session = await authHandler.api.getSession(c.req.raw)
  
  if (!session) {
    return c.json({ error: 'Unauthorized' }, 401)
  }

  const apiKey = c.env.OPENROUTER_API_KEY
  if (!apiKey) {
    return c.json({ error: 'LLM not configured on server' }, 503)
  }

  try {
    const response = await fetch('https://openrouter.ai/api/v1/chat/completions', {
      method: 'POST',
      headers: {
        'Authorization': `Bearer ${apiKey}`,
        'Content-Type': 'application/json',
        'HTTP-Referer': 'https://carflow.app',
        'X-Title': 'CarFlow',
      },
      body: JSON.stringify({
        model: c.env.LLM_MODEL || 'openai/gpt-4o-mini',
        messages: [
          { role: 'system', content: systemPrompt },
          { role: 'user', content: userPrompt },
        ],
        temperature: 0.0,
        max_tokens: 500,
      }),
    })

    if (!response.ok) {
      const errorBody = await response.text()
      console.error('LLM API error:', errorBody)
      return c.json({ error: 'LLM provider error' }, response.status)
    }

    const data = await response.json()
    const content = data.choices?.[0]?.message?.content

    if (!content) {
      return c.json({ error: 'Empty response from LLM' }, 502)
    }

    return c.json({ json: content })
  } catch (error) {
    console.error('Parse error:', error)
    return c.json({ error: 'Failed to parse expense' }, 500)
  }
}
