set echo on
select * from all_directories;

declare
   my_clob         clob;
   my_bfile bfile := bfilename('UDUMP_DIR', 'dev18b_ora_10790.trc');
begin
   dbms_lob.CreateTemporary(my_clob, FALSE, dbms_lob.CALL);
   dbms_lob.FileOpen(my_bfile);
end;
/
