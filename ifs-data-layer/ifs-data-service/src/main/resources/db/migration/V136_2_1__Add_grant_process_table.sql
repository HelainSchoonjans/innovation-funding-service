-- IFS-4494 - Create grant process table
-- Add a new table file_type

CREATE TABLE grant_process (
	id bigint(20) NOT NULL AUTO_INCREMENT,
	application_id bigint(20) NOT NULL,
	sent_requested datetime NOT NULL,
	sent_succeeded datetime DEFAULT NULL,
	last_processed datetime DEFAULT NULL,
	pending BOOLEAN DEFAULT TRUE NOT NULL,
	message VARCHAR(255),
	PRIMARY KEY (id),
	UNIQUE KEY application_id_uk (application_id)
);