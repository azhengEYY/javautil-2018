CREATE OR REPLACE PACKAGE BODY logger
is
   g_job_msg_dir    varchar2 (32) := 'UT_PROCESS_LOG_DIR';
   g_filter_level          pls_integer := G_INFO ;
   g_record_level          pls_integer := G_INFO ;
   g_file_handle           UTL_FILE.file_type;
   g_log_file_name         varchar2 (255);
   g_last_log_seq_nbr      pls_integer;
   g_dbms_output_level     pls_integer        := 5;
   g_process_start_tm      timestamp;
   g_process_end_tm        timestamp;
   g_process_name          varchar2 (128);
   g_process_status_id     pls_integer;
-- set by get_caller
   g_owner_name            varchar2 (100);
   g_caller_name           varchar2 (100);
   g_line_number           pls_integer;
   g_caller_type           varchar2 (100);
--
   g_sid                   pls_integer;
   g_current_schema        varchar2 (32);
   g_current_user          varchar2 (32);
   g_session_user          varchar2 (32);
   g_proxy_user            varchar2 (32);
   g_who_called_me_level   BINARY_integer     := 6;

   procedure set_trace (p_trace_level in pls_integer)
   is
   begin
      DBMS_TRACE.set_plsql_trace (p_trace_level);
   end set_trace;


   function get_g_process_status_id return number is
   begin
       if g_process_status_id  is  null then
           g_process_status_id := job_log_id_seq.nextval;
       end if;
       return g_process_status_id;
   end;

  procedure update_tracefile_name(p_tracefile_name in varchar) is
     pragma autonomous_transaction ;
  begin
	  update job_log set tracefile_name = p_tracefile_name 
	  where job_log_id = g_process_status_id;
	  commit;
  end;

  function get_my_tracefile_name return varchar is
         tracefile_name varchar2(4096);
   begin
    select value into tracefile_name
    from v$diag_info
    where name = 'Default Trace File';

    return tracefile_name;
   end get_my_tracefile_name;

   function set_tracefile_identifier(p_job_nbr in number) return varchar is
       identifier varchar2(32) := 'job_' || to_char(p_job_nbr);
   begin
        execute immediate 'alter session set tracefile_identifier = ''' || identifier || '''';
        return get_my_tracefile_name;
   end set_tracefile_identifier;

   function set_tracefile_identifier return varchar is
       identifier varchar2(32) := 'job_' || to_char(get_g_process_status_id);
   begin
        execute immediate 'alter session set tracefile_identifier = ''' || identifier || '''';
        return get_my_tracefile_name;
   end set_tracefile_identifier;


   procedure set_context
   --% set sys_context userenv variables
   is
   begin
      select SYS_CONTEXT ('USERENV', 'current_SCHEMA'),
             SYS_CONTEXT ('USERENV', 'current_USER'),
             SYS_CONTEXT ('USERENV', 'SESSION_USER'),
             SYS_CONTEXT ('USERENV', 'PROXY_USER')
        into g_current_schema,
             g_current_user,
             g_session_user,
             g_proxy_user
        from DUAL;
   end set_context;



   procedure begin_job (p_process_name in varchar2)
   is
      PRAGMA AUTONOMOUS_TRANSACTION;
      my_tracefile_name varchar2(4000) := set_tracefile_identifier;
   begin
      if p_process_name is NULL
      then
         g_process_name := g_caller_name;
      else
         g_process_name := p_process_name;
      end if;

      g_process_start_tm := current_timestamp;

      g_process_status_id := job_log_id_seq.nextval;



      INSERT into job_log (
	  process_name,         schema_name, thread_name, process_run_nbr,      
	  status_msg,           status_id,   status_ts,   SID,
          job_log_id, tracefile_name
      ) VALUES (
	  g_process_name,       g_current_schema,    'main',  g_process_status_id,  
          'init',               'A',                 SYSDATE,
          g_sid,                g_process_status_id, my_tracefile_name
     );

      g_last_log_seq_nbr := 1;

      COMMIT;
   end begin_job;

  FUNCTION begin_java_job ( 
    p_process_name in varchar2,
    p_classname   in varchar2,
    p_module_name  in varchar2,
    p_status_msg   in varchar2,
    p_thread_name  in varchar2,
    p_trace_level  in pls_integer default G_INFO
  ) RETURN number
   is
      PRAGMA AUTONOMOUS_TRANSACTION;
      my_tracefile_name varchar2(256);
   begin

      set_trace(p_trace_level);
      if p_process_name is NULL
      then
         g_process_name := g_caller_name;
      else
         g_process_name := p_process_name;
      end if;

      g_process_status_id := job_log_id_seq.NEXTVAL;
      dbms_output.put_line('begin java job ' || to_char(g_process_status_id));

      g_process_start_tm := current_timestamp;
      dbms_output.put_line('get_my_tracefile_name about');

      my_tracefile_name := set_tracefile_identifier;

      set_action('begin_java_job ' || to_char(g_process_status_id));

      insert into job_log (
        process_name,         schema_name, thread_name, process_run_nbr,
        status_msg,           status_id,   status_ts,   SID,
        job_log_id, classname,  module_name, tracefile_name
      ) values (
        g_process_name,        g_current_schema, p_thread_name, g_process_status_id,
        p_status_msg,          'A',              systimestamp,  g_sid,
        g_process_status_id,   p_classname,     p_module_name, my_tracefile_name
      );

      g_last_log_seq_nbr := 1;

      commit;
      return g_process_status_id;
   end begin_java_job;

   --::<
   procedure end_job
   --::* update job_log.status_id to 'C' and status_msg to 'DONE'
   --::>
   is
      PRAGMA AUTONOMOUS_TRANSACTION;
      elapsed_tm   INTERVAL DAY TO SECOND;
   begin
      set_action('end_job');
      g_process_end_tm := current_timestamp;
      elapsed_tm := g_process_end_tm - g_process_start_tm;

      UPDATE job_log
         SET 
             SID = NULL,
             status_msg = 'DONE',
             status_id = 'C',
             status_ts = SYSDATE
       where job_log_id = g_process_status_id;

      COMMIT;
      set_action('end_job complete');
   end end_job;
   
   --::<
   procedure abort_job(p_stacktrace in varchar)
   --::* procedure abort_job
   --::* update job_log
   --::* elapsed_time
   --::* status_id = 'I'
   --::* status_msg = 'ABORT'
   --::>
   is
      PRAGMA AUTONOMOUS_TRANSACTION;
      elapsed_tm   INTERVAL DAY TO SECOND;
   begin
      set_action('abort_job');
      g_process_end_tm := current_timestamp;
      elapsed_tm := g_process_end_tm - g_process_start_tm;

      UPDATE job_log
         SET 
             SID = NULL,
             status_msg = 'ABORT',
             status_id = 'A',
             status_ts = SYSDATE,
             abort_stacktrace = p_stacktrace
       where job_log_id = g_process_status_id;


      COMMIT;
      set_action('abort_job complete');
   end abort_job;



    procedure set_action ( p_action in varchar2 ) is
    begin
            dbms_application_info.set_action(substr(p_action, 1, 64)) ;
    end set_action ;

    procedure set_module ( p_module_name in varchar, p_action_name in varchar )
    is
    begin
            dbms_application_info.set_module(p_module_name, p_action_name) ;
    end set_module ;


   function get_directory_path return varchar is
      -- todo see if grants are wrong, permission must be granted to the user
      cursor directory_cur is
      select  owner, directory_name, directory_path
      from    all_directories
      where   directory_name = g_job_msg_dir;

      directory_rec directory_cur%rowtype;

    begin
      open directory_cur;
      fetch directory_cur into directory_rec;
      dbms_output.put_line('owner: '           || directory_rec.owner ||
                           ' directory_name: ' || directory_rec.directory_name ||
                           ' directory_path: ' || directory_rec.directory_path);
      close directory_cur;

      return directory_rec.directory_path;
   end get_directory_path;

  --::<
  function basename (p_full_path in varchar2,
                     p_suffix    in varchar2 default null,
                     p_separator in char default '/')
   return varchar2
   --:: like bash basename or gnu basename, returns the filename of a path optionally
   --:: stripping the specified file extension
   --::>
   is
       my_basename varchar2(256);
   begin
       dbms_output.put_line('basename ' || p_full_path);
       my_basename := substr(p_full_path, instr(p_full_path,p_separator,-1)+1);
       dbms_output.put_line('my_basename' || my_basename);
       if p_suffix is not null then
          my_basename := substr(my_basename, 1, instr(my_basename, p_suffix, -1)-1);
       end if;

       return my_basename;
   end basename;

    function get_my_tracefile return clob is
    begin
        return get_tracefile(basename(get_my_tracefile_name));
    end get_my_tracefile;

    function get_tracefile(p_file_name in varchar)
    return clob is
        my_clob         clob;
        my_bfile        bfile;
        my_dest_offset  integer := 1;
        my_src_offset   integer := 1;
        my_lang_context integer := dbms_lob.default_lang_ctx;
        my_warning      integer;
    begin
        my_bfile := bfilename('UDUMP_DIR', p_file_name);

        dbms_lob.CreateTemporary(my_clob, FALSE, dbms_lob.CALL);
        dbms_lob.FileOpen(my_bfile);
        dbms_output.put_line('get_tracefile: before LoadClobFromFile');

        dbms_lob.LoadClobFromFile (
            dest_lob     => my_clob,
            src_bfile    => my_bfile,
            amount       => dbms_lob.lobmaxsize,
            dest_offset  => my_dest_offset,
            src_offset   => my_src_offset,
            bfile_csid   => dbms_lob.default_csid,
            lang_context => my_lang_context,
            warning      => my_warning
        );
        dbms_output.put_line('get_tracefile warning: ' || my_warning);
        dbms_lob.FileClose(my_bfile);

        return my_clob;
    end get_tracefile;

    procedure trace_step (p_step_name in varchar) is 
    -- record the step name and then restore the action
    -- the execute immediate makes sure the action is written to the trace file
    -- as if there is no sql activity it is not written
    begin
       set_action(p_step_name);
       execute immediate 'select ''' || p_step_name || ''' from dual';
       --set_action(my_my_session_info.action);
    end;

    PROCEDURE prepare_connection is
       context_info DBMS_SESSION.AppCtxTabTyp;
       info_count   PLS_INTEGER;
       indx         PLS_INTEGER;
    BEGIN
       DBMS_SESSION.LIST_CONTEXT ( context_info, info_count);
       indx := context_info.FIRST;
       LOOP
          EXIT WHEN indx IS NULL;
          DBMS_SESSION.CLEAR_CONTEXT(
             context_info(indx).namespace,
             context_info(indx).attribute,
             null
          );
          indx := context_info.NEXT (indx);
       END LOOP;
       DBMS_SESSION.RESET_PACKAGE;
    END prepare_connection;

begin
   dbms_output.ENABLE(1000000) ;
   set_context;
end logger;
/
--#<
show errors
--#>
