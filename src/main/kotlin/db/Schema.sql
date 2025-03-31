package db

-- Users table for Project Managers
CREATE TABLE users (
id SERIAL PRIMARY KEY,
username VARCHAR(50) NOT NULL UNIQUE,
password VARCHAR(255) NOT NULL,
email VARCHAR(100) NOT NULL UNIQUE,
role VARCHAR(20) NOT NULL,
created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Projects table
CREATE TABLE projects (
id SERIAL PRIMARY KEY,
project_manager_id INTEGER REFERENCES users(id),
name VARCHAR(100) NOT NULL,
description TEXT,
start_date DATE,
end_date DATE,
created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Tasks table
CREATE TABLE tasks (
id SERIAL PRIMARY KEY,
project_id INTEGER NOT NULL REFERENCES projects(id) ON DELETE CASCADE,
name VARCHAR(100) NOT NULL,
description TEXT,
estimated_hours NUMERIC(6,2),
created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Timesheet entries table
CREATE TABLE timesheet_entries (
id SERIAL PRIMARY KEY,
task_id INTEGER NOT NULL REFERENCES tasks(id) ON DELETE CASCADE,
employee_name VARCHAR(100) NOT NULL,
hours_worked NUMERIC(5,2) NOT NULL CHECK (hours_worked > 0),
work_date DATE NOT NULL,
submitted_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
notes TEXT
);

-- Indexes for performance
CREATE INDEX idx_tasks_project_id ON tasks(project_id);
CREATE INDEX idx_timesheet_entries_task_id ON timesheet_entries(task_id);
CREATE INDEX idx_timesheet_entries_work_date ON timesheet_entries(work_date);

