-- src/main/resources/db/migration/V2__add_indexes.sql

-- Indexes for performance
CREATE INDEX idx_tasks_project_id ON tasks(project_id);
CREATE INDEX idx_timesheet_entries_task_id ON timesheet_entries(task_id);
CREATE INDEX idx_timesheet_entries_work_date ON timesheet_entries(work_date);