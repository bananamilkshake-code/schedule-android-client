DROP DATABASE IF EXISTS schedule;
CREATE DATABASE schedule;

CREATE TABLE schedule.users (
	global_id INT(10) PRIMARY KEY,
	name VARCHAR(50) NOT NULL,
	UNIQUE(global_id) 
); 

CREATE TABLE schedule.tables (
	_id INT PRIMARY KEY,
	global_id INT(10),
	last_update INT(10) NOT NULL DEFAULT 0,

	UNIQUE(_id)
);

CREATE TABLE schedule.tasks (
	_id INT PRIMARY KEY,
	global_id INT(10),
	table_id INT(10) NOT NULL,
	last_update INT(10) NOT NULL DEFAULT 0,

	FOREIGN KEY (table_id) REFERENCES tables(_id),

	UNIQUE(_id, table_id)
);

CREATE TABLE schedule.table_changes (
	table_id INT PRIMARY KEY,
	time INT(10) NOT NULL,
	user_id INT(10) NOT NULL,

	name VARCHAR(100),
	description TEXT,

	FOREIGN KEY (table_id) REFERENCES tables(_id),
	FOREIGN KEY (user_id) REFERENCES users(global_id)
);

CREATE TABLE schedule.task_changes (
	table_id INT(10),
	task_id INT(10),

	time INT(10) NOT NULL,
	user_id INT(10) NOT NULL,

	name VARCHAR(100),
	description TEXT,
	start_date DATE,
	end_date DATE,
	start_time TIME,
	end_time TIME,

	FOREIGN KEY (table_id) REFERENCES tables(_id),
	FOREIGN KEY (task_id) REFERENCES tasks(_id),
	FOREIGN KEY (user_id) REFERENCES users(global_id)
);

CREATE TABLE schedule.comments (
	table_id INT(10) NOT NULL,
	task_id INT(10) NOT NULL,
	time INT(10) NOT NULL,
	user_id INT(10) NOT NULL,
	commentary TEXT NOT NULL,

	FOREIGN KEY (user_id) REFERENCES users(global_id),
	FOREIGN KEY (table_id) REFERENCES tables(_id),
	FOREIGN KEY (task_id) REFERENCES tasks(_id)
);

CREATE TABLE schedule.readers (
	user_id INT(10) NOT NULL,
	table_id INT(10) NOT NULL,
	permission TINYINT(1) NOT NULL,

	FOREIGN KEY (user_id) REFERENCES users(global_id),
	FOREIGN KEY (table_id) REFERENCES tables(_id),

	UNIQUE(user_id, table_id)
);