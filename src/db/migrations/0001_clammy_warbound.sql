PRAGMA foreign_keys=OFF;--> statement-breakpoint
CREATE TABLE `__new_sessions` (
	`id` text PRIMARY KEY NOT NULL,
	`label` text NOT NULL,
	`hour` integer NOT NULL,
	`minute` integer NOT NULL,
	`face_down_minutes` integer NOT NULL,
	`trigger_at_millis` integer NOT NULL,
	`state` text NOT NULL,
	`cancel_reason` text,
	`created_at` integer NOT NULL
);
--> statement-breakpoint
INSERT INTO `__new_sessions`("id", "label", "hour", "minute", "face_down_minutes", "trigger_at_millis", "state", "cancel_reason", "created_at") SELECT "id", "label", "hour", "minute", "face_down_minutes", "trigger_at_millis", "state", "cancel_reason", "created_at" FROM `sessions`;--> statement-breakpoint
DROP TABLE `sessions`;--> statement-breakpoint
ALTER TABLE `__new_sessions` RENAME TO `sessions`;--> statement-breakpoint
PRAGMA foreign_keys=ON;