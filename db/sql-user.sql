CREATE ROLE 'mmt_terminal_user';
GRANT SELECT, INSERT, UPDATE, DELETE ON mediamanagement.* TO 'mmt_terminal_user';
GRANT EXECUTE ON mediamanagement.* TO 'mmt_terminal_user';

CREATE ROLE 'mmt_web_user';
GRANT SELECT ON mediamanagement.* TO 'mmt_web_user';
GRANT INSERT, UPDATE on mediamanagement.lending TO 'mmt_web_user';
GRANT INSERT, UPDATE on mediamanagement.lendee TO 'mmt_web_user';
GRANT UPDATE (status) ON mediamanagement.media TO 'mmt_web_user';
GRANT EXECUTE ON mediamanagement.* TO 'mmt_web_user';

CREATE ROLE 'mmt_db_admin';
GRANT ALL PRIVILEGES ON mediamanagement.* TO 'mmt_db_admin';


CREATE USER 'mmt_db_admin_user' IDENTIFIED BY 'password' ; #dummy-password
GRANT 'mmt_db_admin' TO 'mmt_db_admin_user';
SET DEFAULT ROLE 'mmt_db_admin' TO 'mmt_db_admin_user';

CREATE USER 'mmt_admin' IDENTIFIED BY 'password'; #dummy-password
GRANT 'mmt_terminal_user' TO 'mmt_admin';
SET DEFAULT ROLE 'mmt_terminal_user' TO 'mmt_admin';

CREATE USER 'mmt_user' IDENTIFIED BY 'password'; #dummy-password
GRANT 'mmt_web_user' TO 'mmt_user';
SET DEFAULT ROLE 'mmt_web_user' TO 'mmt_user';
