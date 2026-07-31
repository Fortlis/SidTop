import { integer, sqliteTable, text } from "drizzle-orm/sqlite-core";

export const sessions = sqliteTable("sessions", {
    id: text("id").primaryKey(),
    label: text("label").notNull(),

    hour: integer("hour").notNull(),
    minute: integer("minute").notNull(),
    faceDownMinutes: integer("face_down_minutes").notNull(),
    triggerAtMillis: integer("trigger_at_millis").notNull(),

    state: text("state", { enum: ['CANCELLED', 'COMPLETED'] }).notNull(),
    cancelReason: text("cancel_reason"),

    createdAt: integer("created_at", { mode: 'timestamp' })
        .$defaultFn(() => new Date())
        .notNull()
})

export type Session = typeof sessions.$inferSelect;
export type NewSession = typeof sessions.$inferInsert;