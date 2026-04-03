import type { D1Database } from '@cloudflare/workers-types'
import type { ColumnType, DriverOptions, ResultSet } from 'drizzle-orm'

export class D1Dialect {
  private db: D1Database

  constructor(db: D1Database) {
    this.db = db
  }

  async query(sql: string, params: unknown[]): Promise<ResultSet> {
    const stmt = this.db.prepare(sql).bind(...(params as any[]))
    const result = await stmt.run()
    return {
      rows: result.results || [],
      affectedRows: result.meta?.changes || 0,
      insertId: result.meta?.last_row_id?.toString() || '0',
    } as unknown as ResultSet
  }
}
