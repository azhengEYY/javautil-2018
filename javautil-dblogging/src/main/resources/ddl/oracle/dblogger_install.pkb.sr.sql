CREATE OR REPLACE PACKAGE BODY logger
is
   g_ut_process_log_dir    varchar2 (32) := 'UT_PROCESS_LOG_DIR';
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

   function open_log_file (p_file_name varchar) return varchar
   --% opens a log file with the specified file name in the directory g_ut_process_log_dir
   is
      my_file_name varchar(255);
      my_directory_path varchar2(4000);
   begin
      if (NOT UTL_FILE.is_open (g_file_handle))
      then
         if g_process_status_id is null then
            g_process_status_id := ut_process_status_id_seq.nextval;
         end if;
     if p_file_name is null then
            my_file_name := 'job_' || to_char(g_process_status_id);
         else
                my_file_name := p_file_name;
         end if;
     g_file_handle := UTL_FILE.fopen (g_ut_process_log_dir, my_file_name, 'A');
      end if;
         return my_file_name;
   end open_log_file;

   function get_g_process_status_id return number is
   begin
       if g_process_status_id  is  null then
           g_process_status_id := ut_process_status_id_seq.nextval;
       end if;
       return g_process_status_id;
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

  --::<
  procedure create_process_log (
      p_ut_process_status_id   in   pls_integer,
      p_log_msg_id              in   varchar2,
      p_log_msg                 in   varchar2,
      p_log_level               in   pls_integer,
      p_elapsed_time            in   INTERVAL DAY TO SECOND DEFAULT NULL,
      p_caller_name             in   varchar2,
      p_line_number             in   pls_integer,
      p_call_stack              in   varchar2 DEFAULT NULL
   )
   --::* g_last_log_seq_nbr
   --::* p_ut_process_status_id
   --::* p_log_msg_id
   --::* to_char (current_timestamp, 'YYYY-MM-DD HH24:MI:SSXFF')
   --::* my_msg
   --::* p_log_level
   --::* p_caller_name
   --::* p_line_number
   --::* p_call_stack;
   is
      my_message   varchar2 (32767);
      my_msg       varchar2 (32767);
      now          timestamp        := SYSDATE;
      pragma autonomous_transaction ;
     short_message varchar2(255);
     long_message  clob;
     my_log_file_name varchar2(4000);
   --
   begin
      --% messages shorter than 256 go into log_msg
      --% longer messages go into log_msg_clob
      dbms_output.put_line('in create_process_log');
      if g_log_file_name is NULL
      then
         g_log_file_name :=
               g_process_name || '_'
            || to_char (current_timestamp, 'YYYY-MM-DD_HH24MisSXFF');
      end if;
      dbms_output.put_line('open_log_file' || g_log_file_name);
      my_log_file_name := open_log_file (g_log_file_name);  -- TODO why pass a global
      dbms_output.put_line('log_file: ' || my_log_file_name);
      g_last_log_seq_nbr := g_last_log_seq_nbr + 1;

      if p_log_level <= g_filter_level
      then

          if (length(p_log_msg) < 255) then
             short_message := my_msg;
             long_message  := null;
          else
             short_message := 'see clob';
             long_message  := my_msg;
          end if;

          my_message := logger_message_formatter  (
              log_seq_nbr            =>   g_last_log_seq_nbr,
              ut_process_status_id   =>   p_ut_process_status_id,
              log_msg_id             =>   p_log_msg_id,
              log_msg                =>   p_log_msg,
              log_level              =>   p_log_level,
              caller_name            =>   p_caller_name,
              line_number            =>   p_line_number,
              call_stack             =>   p_call_stack
          );
     	  dbms_output.put_line('about to write ' || my_message); 
          UTL_FILE.put_line (g_file_handle, my_message);
      end if;

      if p_log_level = g_snap OR p_log_level <= g_record_level
      then
          insert into ut_process_log (
               ut_process_log_id,
               ut_process_status_id,   log_seq_nbr,    log_msg_id,    log_msg,
               log_level,              log_msg_ts,     caller_name,
               line_nbr,               log_msg_clob
          )
          values (
	       ut_process_log_id_seq.nextval,
               p_ut_process_status_id, g_last_log_seq_nbr, p_log_msg_id,   short_message,
               p_log_level,            current_timestamp,   p_caller_name,
               p_line_number,          long_message
         );
      end if;
--
      commit;
   end create_process_log;
--
   --<
   procedure TRACE (p_string in varchar2)
   --% procedure TRACE (p_string in varchar2)
   --% Write the messsage to dbms_output
   -->
   is
   begin
      dbms_output.put_line (p_string);
   end TRACE;

-- public
   procedure set_dbms_output_level (p_level in pls_integer)
   is
   begin
      g_dbms_output_level := p_level;
   end set_dbms_output_level;

   procedure close_log_file
   is
   begin
      if utl_file.is_open (g_file_handle) then
          utl_file.fclose (g_file_handle);
      end if;
   end close_log_file;

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


----
--
   procedure log_level (
      p_log_msg       in   varchar2,
      p_log_level     in   pls_integer,
      p_caller_name   in   varchar2 DEFAULT NULL,
      p_line_number   in   pls_integer DEFAULT NULL,
      p_call_stack    in   varchar2 DEFAULT NULL
   )
   is
      log_time       timestamp  := current_timestamp;
      elapsed        INTERVAL DAY TO SECOND;
      my_log_level   pls_integer;
   begin
      my_log_level := p_log_level;

      if my_log_level < 1 then my_log_level := 1; end if;
      if my_log_level > 9 then my_log_level := 9; end if;

      dbms_output.put_line('about to create process log');

      create_process_log (p_ut_process_status_id  => g_process_status_id,
                          p_log_msg_id            => NULL,
                          p_log_msg               => p_log_msg,
                          p_log_level             => my_log_level,
                          p_elapsed_time          => elapsed,
                          p_caller_name           => p_caller_name,
                          p_line_number           => p_line_number,
                          p_call_stack            => p_call_stack
                         );
   end log_level;

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

      g_process_status_id := ut_process_status_id_seq.nextval;



      INSERT into ut_process_status (
	  process_name,         schema_name, thread_name, process_run_nbr,      
	  status_msg,           status_id,   status_ts,   SID,
          ut_process_status_id, tracefile_name
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

      g_process_status_id := ut_process_status_id_seq.NEXTVAL;
      dbms_output.put_line('begin java job ' || to_char(g_process_status_id));

      g_process_start_tm := current_timestamp;
      dbms_output.put_line('get_my_tracefile_name about');

      my_tracefile_name := set_tracefile_identifier;

      set_action('begin_java_job ' || to_char(g_process_status_id));

      insert into ut_process_status (
        process_name,         schema_name, thread_name, process_run_nbr,
        status_msg,           status_id,   status_ts,   SID,
        ut_process_status_id, classname,  module_name, tracefile_name
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
   --::* update ut_process_status.status_id to 'C' and status_msg to 'DONE'
   --::>
   is
      PRAGMA AUTONOMOUS_TRANSACTION;
      elapsed_tm   INTERVAL DAY TO SECOND;
   begin
      set_action('end_job');
      g_process_end_tm := current_timestamp;
      elapsed_tm := g_process_end_tm - g_process_start_tm;

      UPDATE ut_process_status
         SET 
             SID = NULL,
             status_msg = 'DONE',
             status_id = 'C',
             status_ts = SYSDATE
       where ut_process_status_id = g_process_status_id;

      COMMIT;
      close_log_file;
      set_action('end_job complete');
   end end_job;
   
   --::<
   procedure abort_job(p_stacktrace in varchar)
   --::* procedure abort_job
   --::* update ut_process_status
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

      UPDATE ut_process_status
         SET 
             SID = NULL,
             status_msg = 'ABORT',
             status_id = 'I',
             status_ts = SYSDATE,
             abort_stacktrace = p_stacktrace
       where ut_process_status_id = g_process_status_id;

      close_log_file;

      COMMIT;
      set_action('abort_job complete');
   end abort_job;

   procedure LOG (p_level in pls_integer, p_log_msg IN varchar2)
   is
   begin
      log_level (p_log_level        => p_level,
                 p_log_msg          => p_log_msg,
                 p_caller_name      => g_caller_name,
                 p_line_number      => g_line_number
                );
   end LOG;

   procedure severe (
      p_unit           in   varchar2,
      p_line           in   pls_integer,
      p_log_msg        in   varchar2 DEFAULT '',
      p_record_stack   in   BOOLEAN default false
   )
   is
      stack   varchar2 (32767);
   begin
      if p_record_stack
      then
         stack := DBMS_UTILITY.format_call_stack ();
      end if;

      log_level (p_log_level        => g_severe,
                 p_log_msg          => p_log_msg,
                 p_caller_name      => p_unit,
                 p_line_number      => p_line,
                 p_call_stack       => stack
                );
   end severe;

   procedure warning (
      p_unit           in   varchar2,
      p_line           in   pls_integer,
      p_log_msg        in   varchar2,
      p_record_stack   in   BOOLEAN
   )
   is
      stack   varchar2 (32767);
   begin
      if p_record_stack
      then
         stack := DBMS_UTILITY.format_call_stack ();
      end if;

      log_level (p_log_level        => g_warning,
                 p_log_msg          => p_log_msg,
                 p_caller_name      => p_unit,
                 p_line_number      => p_line,
                 p_call_stack       => stack
                );
   end warning;

   procedure info (
      p_unit           in   varchar2,       -- should be set with $$PLSQL_UNIT
      p_line           in   pls_integer,    -- should be set with $$PLSQL_LINE
      p_log_msg        in   varchar2,       -- the message to be logged
      p_record_stack   in   BOOLEAN         -- record the call stack
   )
   is
      stack   varchar2 (32767);
   begin
      if p_record_stack then
        stack := dbms_utility.format_call_stack ();
      end if;
      dbms_output.put_line('about to log_level in info');
      log_level (p_log_level        => g_info,
                 p_log_msg          => p_log_msg,
                 p_caller_name      => p_unit,
                 p_line_number      => p_line,
                 p_call_stack       => stack
                );
   end info;

   procedure log_snap (
      p_unit      in   varchar2,
      p_line      in   pls_integer,
      p_log_msg   in   varchar2
   )
   is
   begin
      OWA_UTIL.who_called_me (g_owner_name,
                              g_caller_name,
                              g_line_number,
                              g_caller_type
                             );

      log_level (p_log_level        => g_snap,
                 p_log_msg          => p_log_msg,
                 p_caller_name      => g_caller_name,
                 p_line_number      => g_line_number
                );
   end log_snap;

   procedure entering (
      p_unit           in   varchar2,
      p_line           in   pls_integer,
      p_log_msg        in   varchar2 DEFAULT '',
      p_record_stack   in   BOOLEAN DEFAULT FALSE,
      p_set_action     in   BOOLEAN DEFAULT TRUE
   )
   is
      stack   varchar2 (32767) := NULL;
   begin
      if p_record_stack
      then
         stack := DBMS_UTILITY.format_call_stack ();
      end if;

      log_level (p_log_level        => g_entering,
                 p_log_msg          => p_log_msg,
                 p_caller_name      => p_unit,
                 p_line_number      => p_line,
                 p_call_stack       => stack
                );
      if p_set_action then set_action($$PLSQL_UNIT) ; end if;
   end entering;

   procedure exiting (
      p_unit           in   varchar2,
      p_line           in   pls_integer,
      p_log_msg        in   varchar2,
      p_record_stack   in   BOOLEAN
   )
   is
      stack   varchar2 (32767);
   begin
      if p_record_stack
      then
         stack := DBMS_UTILITY.format_call_stack ();
      end if;

      log_level (p_log_level        => g_exiting,
                 p_log_msg          => p_log_msg,
                 p_caller_name      => p_unit,
                 p_line_number      => p_line,
                 p_call_stack       => stack
                );
   end exiting;

   procedure fine (
      p_unit           in   varchar2,
      p_line           in   pls_integer,
      p_log_msg        in   varchar2,
      p_record_stack   in   BOOLEAN
   )
   is
      stack   varchar2 (32767);
   begin
      if p_record_stack
      then
         stack := DBMS_UTILITY.format_call_stack ();
      end if;

      log_level (p_log_level        => g_fine,
                 p_log_msg          => p_log_msg,
                 p_caller_name      => p_unit,
                 p_line_number      => p_line,
                 p_call_stack       => stack
                );
   end fine;

   procedure finer (
      p_unit           in   varchar2,
      p_line           in   pls_integer,
      p_log_msg        in   varchar2,
      p_record_stack   in   BOOLEAN DEFAULT FALSE
   )
   is
      stack   varchar2 (32767);
   begin
      if p_record_stack then stack := DBMS_UTILITY.format_call_stack (); end if;

      log_level (p_log_level        => g_finer,
                 p_log_msg          => p_log_msg,
                 p_caller_name      => p_unit,
                 p_line_number      => p_line,
                 p_call_stack       => stack
                );
   end finer;

   procedure finest (
      p_unit           in   varchar2,
      p_line           in   pls_integer,
      p_log_msg        in   varchar2,
      p_record_stack   in   BOOLEAN DEFAULT FALSE
   )
   is
      stack   varchar2 (32767);
   begin
      if p_record_stack then stack := DBMS_UTILITY.format_call_stack (); end if;

      log_level (p_log_level        => g_finest,
                 p_log_msg          => p_log_msg,
                 p_caller_name      => p_unit,
                 p_line_number      => p_line,
                 p_call_stack       => stack
                );
   end finest;

   procedure LOG (
      p_msg_id         in   varchar2,
      p_log_msg        in   varchar2,
      p_long_msg       in   varchar2,
      p_elapsed_time   in   INTERVAL DAY TO SECOND DEFAULT NULL,
      p_log_level      in   pls_integer,
      p_call_stack     in   varchar2 DEFAULT NULL
   )
   is
      my_log_time    timestamp ( 6 )        := current_timestamp;
      my_elapsed     INTERVAL DAY TO SECOND;
      my_log_level   pls_integer;
   begin
      my_log_level := p_log_level;

      if my_log_level < 1 then my_log_level := 1; end if;
      if my_log_level > 9 then my_log_level := 9; end if;

      create_process_log (p_ut_process_status_id   => g_process_status_id,
                          p_log_msg_id              => p_msg_id,
                          p_log_msg                 => p_log_msg,
                          p_log_level               => my_log_level,
                          p_elapsed_time            => my_elapsed,
                          p_caller_name             => g_caller_name,
                          p_line_number             => g_line_number,
                          p_call_stack              => p_call_stack
                         );
   end LOG;


   procedure set_filter_level (p_level in pls_integer)
   is
   begin
      if    p_level < 1 then g_filter_level := 1;
      elsif p_level > 9 then g_filter_level := 9;
      else  g_filter_level := p_level; 
      end if;
   end set_filter_level;

   /**
    procedure set_record_level (p_level in pls_integer)
   */
   procedure set_record_level (p_level in pls_integer)
   is
   begin
      if    p_level < 1 then g_record_level := 1;
      elsif p_level > 9 then g_record_level := 9;
      else  g_record_level := p_level;
      end if;
   end set_record_level;


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
      where   directory_name = g_ut_process_log_dir;

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

   procedure test_dir is
      my_file_handle           UTL_FILE.file_type;
   begin
      my_file_handle := UTL_FILE.fopen ('UT_PROCESS_LOG_DIR', 'wtf.log', 'A');
      UTL_FILE.put_line (my_file_handle, 'wtf');
     utl_file.fclose (my_file_handle);
   end;

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
