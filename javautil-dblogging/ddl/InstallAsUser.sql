--connect &&sys_uid_pwd_service_name  as sysdba 
create directory job_msg as '&&job_msg_directory';
@01-create-user.sql
grant read, write on directory job_msg to &&username;
connect &&user/&&userpassword
@02-create_sequences.sql
@job_log.sql
@job_msg.sql
@job_stat.sql
@logger.pks
@logger.pkb
