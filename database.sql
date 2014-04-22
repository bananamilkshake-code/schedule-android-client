DROP DATABASE IF EXISTS schedule;
CREATE DATABASE schedule;

CREATE TABLE schedule.users (
	id INT(10) AUTO_INCREMENT PRIMARY KEY,
	name VARCHAR(50) NOT NULL,
	UNIQUE(id) 
); 

CREATE TABLE schedule.tables (
	local_id INT PRIMARY KEY,
	id INT(10),
	last_update INT(10) NOT NULL DEFAULT 0,

	UNIQUE(local_id, id)
);

CREATE TABLE schedule.tasks (
	local_id INT PRIMARY KEY,
	id INT(10),
	table_id INT(10) NOT NULL,
	last_update INT(10) NOT NULL DEFAULT 0,

	FOREIGN KEY (table_id) REFERENCES tables(id),

	UNIQUE(local_id, id)
);

CREATE TABLE schedule.table_changes (
	local_table_id INT PRIMARY KEY,
	table_id INT(10),
	time INT(10) NOT NULL,
	user_id INT(10) NOT NULL,

	name VARCHAR(100),
	description TEXT,

	FOREIGN KEY (table_id) REFERENCES tables(id),
	FOREIGN KEY (user_id) REFERENCES users(id),

	UNIQUE(local_table_id, table_id)
);

CREATE TABLE schedule.task_changes (
	local_table_id INT(10) NOT NULL,
	local_task_id INT(10) NOT NULL,

	table_id INT(10),
	task_id INT(10),

	time INT(10) NOT NULL,
	user_id INT(10) NOT NULL,

	name VARCHAR(100),
	description TEXT,
	start_date DATE,
	completion_date DATE, 
	end_time TIME,

	FOREIGN KEY (table_id) REFERENCES tables(id),
	FOREIGN KEY (task_id) REFERENCES tasks(id),

	UNIQUE(local_table_id, local_task_id),
	UNIQUE(table_id, task_id)
);

CREATE TABLE schedule.comments (
	commentator_id INT(10) NOT NULL,
	table_id INT(10) NOT NULL,
	task_id INT(10) NOT NULL,
	commentary TEXT NOT NULL,
	time INT(10) NOT NULL,

	FOREIGN KEY (commentator_id) REFERENCES users(id),
	FOREIGN KEY (table_id) REFERENCES tables(id),
	FOREIGN KEY (task_id) REFERENCES tasks(id)
);

CREATE TABLE schedule.readers (
	reader_id INT(10) NOT NULL,
	table_id INT(10) NOT NULL,
	permission TINYINT(1) NOT NULL,

	FOREIGN KEY (reader_id) REFERENCES users(id),
	FOREIGN KEY (table_id) REFERENCES tables(id),

	UNIQUE(reader_id, table_id)
);