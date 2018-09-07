set echo on
spool logger
CREATE OR REPLACE PACKAGE logger AS
--
    G_SEVERE       CONSTANT PLS_INTEGER := 1 ;
    G_WARNING      CONSTANT PLS_INTEGER := 2 ;
    G_INFO         CONSTANT PLS_INTEGER := 4 ;
    G_SNAP         CONSTANT PLS_INTEGER := 5 ;
    G_ENTERING     CONSTANT PLS_INTEGER := 6 ;
    G_EXITING      CONSTANT PLS_INTEGER := 6 ;
    G_FINE         CONSTANT PLS_INTEGER := 7 ;
    G_FINER        CONSTANT PLS_INTEGER := 8 ;
    G_FINEST       CONSTANT PLS_INTEGER := 9 ;
    G_NONE         CONSTANT PLS_INTEGER := 10 ;

--
    PROCEDURE begin_job (
        parm_process_name IN VARCHAR2 DEFAULT NULL );
    
    function begin_java_job (
	p_process_name IN VARCHAR2,
        p_class_name   in varchar2,
        p_module_name  in varchar2,
        p_status_msg   in varchar2,
        p_thread_name  in varchar2) return number;
    
    --
    --
    -- various log convenience methods
    PROCEDURE severe (
        p_unit          IN VARCHAR2,
        p_line          IN PLS_INTEGER,
        p_log_msg       IN VARCHAR2 DEFAULT '',
        p_record_stack  IN BOOLEAN DEFAULT FALSE ) ;
    --
    --
    PROCEDURE warning (
        p_unit          IN        VARCHAR2,
        p_line          IN        PLS_INTEGER,
        p_log_msg       IN        VARCHAR2 DEFAULT '',
        p_record_stack  IN        BOOLEAN DEFAULT FALSE ) ;
    --
    --
    PROCEDURE info (
        p_unit          IN        VARCHAR2,
        p_line          IN        PLS_INTEGER,
        p_log_msg       IN        VARCHAR2 DEFAULT '',
        p_record_stack  IN        BOOLEAN DEFAULT FALSE ) ;
    --
    --
    PROCEDURE entering (
        p_unit         IN VARCHAR2,
        p_line         IN PLS_INTEGER,
        p_log_msg      IN VARCHAR2 DEFAULT '',
        p_record_stack IN BOOLEAN DEFAULT FALSE,
        p_set_action   IN BOOLEAN DEFAULT TRUE ) ;
    --
    --
    PROCEDURE exiting (
        p_unit         IN        VARCHAR2,
        p_line         IN        PLS_INTEGER,
        p_log_msg      IN        VARCHAR2 DEFAULT '',
        p_record_stack IN        BOOLEAN DEFAULT FALSE ) ;
    --
    --
    PROCEDURE fine (
        p_unit         IN        VARCHAR2,
        p_line         IN        PLS_INTEGER,
        p_log_msg      IN        VARCHAR2 DEFAULT '',
        p_record_stack IN        BOOLEAN DEFAULT FALSE ) ;
    --
    --
    PROCEDURE finer (
        p_unit         IN        VARCHAR2,
        p_line         IN        PLS_INTEGER,
        p_log_msg      IN        VARCHAR2 DEFAULT '',
        p_record_stack IN        BOOLEAN DEFAULT FALSE ) ;
    --
    --
    PROCEDURE finest (
        p_unit         IN        VARCHAR2,
        p_line         IN        PLS_INTEGER,
        p_log_msg      IN        VARCHAR2 DEFAULT '',
        p_record_stack IN        BOOLEAN DEFAULT FALSE ) ;
    --
    --
    PROCEDURE end_job ;
    --
    --
    PROCEDURE abort_job ;
    --
    --
    PROCEDURE log_snap (
        p_unit      IN        VARCHAR2,
        p_line      IN        PLS_INTEGER,
        p_log_msg   IN        VARCHAR2 ) ;
    --
    --
    PROCEDURE snap_stats (
        p_snap_name    IN        VARCHAR2 ) ;
    --
    --
    PROCEDURE set_action (
        p_action IN        VARCHAR2) ;
    --
    --
    PROCEDURE set_module (
        p_module IN        VARCHAR2) ;
    --
    --
    PROCEDURE set_dbms_output_level(
        p_level  IN        PLS_INTEGER ) ;
    --
    --
    PROCEDURE set_filter_level (  
        p_level  IN PLS_INTEGER ) ;
    --
    --
    PROCEDURE set_record_level (
        p_level  IN        PLS_INTEGER ) ;
    --
    --
    function get_directory_path return varchar;

    function get_tracefile(p_file_name in varchar) return clob;

    function get_my_tracefile_name return varchar ;

    function get_my_tracefile return clob;


    function basename (p_full_path in varchar2,
                       p_suffix    in varchar2 default null,
                       p_separator in char default '/') return varchar2;

END logger ;
/
show errors;
exit;
    
set echo on

CREATE OR REPLACE PACKAGE BODY logger
is
--
-- 
   g_job_msg_dir    varchar2 (32) := 'job_msg_DIR';
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
   g_serial_nbr            pls_integer;
   g_sid                   pls_integer;
   g_current_schema        varchar2 (32);
   g_current_user          varchar2 (32);
   g_session_user          varchar2 (32);
   g_proxy_user            varchar2 (32);
   g_who_called_me_level   BINARY_integer     := 6;

   procedure open_log_file (parm_file_name in varchar2)
   --% opens a log file with the specified file name in the directory g_job_msg_dir
   is
   begin
      if (NOT UTL_FILE.is_open (g_file_handle))
      then
         dbms_output.put_line ('g_job_msg_dir ' || g_job_msg_dir);
         dbms_output.put_line ('opening log file ' || parm_file_name);
         dbms_output.put_line ('directory_path: ' || get_directory_path);
	 --show_directories;
         g_file_handle := UTL_FILE.fopen ('job_msg_DIR', 'test2.log', 'A');
         dbms_output.put_line('file opened');
         --g_file_handle := UTL_FILE.fopen (g_job_msg_dir, parm_file_name, 'A');
      end if;
   end open_log_file;

   function get_trace_file_name return varchar2 is
      cursor trace_file_cur is 
      select VALUE trace_file_name 
      from V$DIAG_INFO 
      where NAME = 'Default Trace File';

      trace_file_rec trace_file_cur%rowtype;
   begin
      open trace_file_cur;
      fetch trace_file_cur into trace_file_rec;
      close trace_file_cur;
      return trace_file_rec.trace_file_name;
   end get_trace_file_name;

  procedure create_process_log (
      parm_job_log_id   in   pls_integer,
      parm_log_msg_id              in   varchar2,
      parm_log_msg                 in   varchar2,
      parm_log_level               in   pls_integer,
      parm_elapsed_time            in   INTERVAL DAY TO SECOND DEFAULT NULL,
      parm_caller_name             in   varchar2,
      parm_line_number             in   pls_integer,
      parm_serial_number           in   pls_integer,
      parm_call_stack              in   varchar2 DEFAULT NULL
   )
   --% procedure create_process_log 
   --% a log file is created in the job_msg oracle directory with the name 
   --% g_process_name || '_' || to_char (current_timestamp, 'YYYY-MM-DD_HH24MisSXFF');
   --% \section{log file format}
   --% \begin{itemize}
   --%    \item g_last_log_seq_nbr 
   --%	  \item	parm_job_log_id 
   --%    \item	parm_log_msg_id 
   --%	  \item	to_char (current_timestamp, 'YYYY-MM-DD HH24:MI:SSXFF') 
   --%    \item my_msg 
   --%    \item parm_log_level 
   --%	  \item	parm_caller_name 
   --%	  \item	parm_line_number 
   --%	  \item	parm_call_stack;
   --% \end{itemize}
   is
      my_message   varchar2 (32767);
      my_msg       varchar2 (32767);
      now          timestamp        := SYSDATE;
      pragma autonomous_transaction ;
     short_message varchar2(255);
     long_message  clob; 
   --
   begin
      --% messages shorter than 256 go into log_msg
      --% longer messages go into log_msg_clob

      if g_log_file_name is NULL
      then
         g_log_file_name :=
               g_process_name || '_'
            || to_char (current_timestamp, 'YYYY-MM-DD_HH24MisSXFF');
      end if;

      open_log_file (g_log_file_name);
      g_last_log_seq_nbr := g_last_log_seq_nbr + 1;

      if parm_log_level <= g_filter_level
      then
         if instr (parm_log_msg, '"') > 0
         then
            my_msg := REPLACE (parm_log_msg, '"', '""');
         else
            my_msg := parm_log_msg;
         end if;

      if (length(parm_log_msg) < 255) then
         short_message := my_msg;
         long_message  := null;
      else 
         short_message := 'see clob';
         long_message  := my_msg;
      end if;

         my_message :=
                g_last_log_seq_nbr || ',' || 
		parm_job_log_id || ',"' || 
		parm_log_msg_id || '",' || 
		to_char (current_timestamp, 'YYYY-MM-DD HH24:MI:SSXFF') || ',"' || 
                 my_msg || '",' ||
            	 parm_log_level || ',"' || 
		parm_caller_name || '",' || 
		parm_line_number || ',' || 
		parm_call_stack;
----
         UTL_FILE.put_line (g_file_handle, my_message);
      end if;

      if parm_log_level = g_snap OR parm_log_level <= g_record_level
      then
         INSERT into job_msg (	
		job_log_id,  	log_seq_nbr, 
		log_msg_id, 			log_msg, 		
		log_level,   			log_msg_ts, 
		elapsed_time, 			caller_name, 
		line_nbr,                       log_msg_clob
         )
         VALUES (
		parm_job_log_id, 	g_last_log_seq_nbr,
                parm_log_msg_id, 		short_message, 
		parm_log_level, 		current_timestamp, 
		parm_elapsed_time, 		parm_caller_name, 
		parm_line_number,               long_message
         );
      end if;
--
      commit;
   end create_process_log;
--

   procedure TRACE (p_string in varchar2)
   --% procedure TRACE (p_string in varchar2)
   --% Write the messsage to dbms_output
   is
   begin
      dbms_output.put_line (p_string);
   end TRACE;

   function get_my_tracefile_name return varchar is 
         trace_file_name varchar2(4096);
   begin
  	select value into trace_file_name
	from v$diag_info 
	where name = 'Default Trace File';

	return trace_file_name;
   end get_my_tracefile_name;
--
--
-- public
   procedure set_dbms_output_level (p_level in pls_integer)
   --% procedure set_dbms_output_level (p_level in pls_integer)
   --% set the dbms_output level 
   --% higher number trace levels will not be written to dbms_output
   is
   begin
      g_dbms_output_level := p_level;
   end set_dbms_output_level;

--
--
-- public
   procedure set_trace (p_trace_level in pls_integer)
   --% procedure set_trace (p_trace_level in pls_integer)
   --% set the trace level for dbms_trace an oracle provided package  
   --% DBMS_TRACE.set_plsql_trace (p_trace_level);
   is
   begin
      DBMS_TRACE.set_plsql_trace (p_trace_level);
   end set_trace;

--
--


--
--
   procedure close_log_file
   --% procedure close_log_file
   --% close the log file
   is
   begin
      if utl_file.is_open (g_file_handle) then 
	utl_file.fclose (g_file_handle); 
      end if;
   end close_log_file;

--
--
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
   --% \begin{verbatim}
   --% procedure log_level (
   --%   p_log_msg       in   varchar2,
   --%   p_log_level     in   pls_integer,
   --%   p_caller_name   in   varchar2 DEFAULT NULL,
   --%   p_line_number   in   pls_integer DEFAULT NULL,
   --%   p_call_stack    in   varchar2 DEFAULT NULL
   --% \end{verbatim}
   is
      log_time       timestamp  := current_timestamp;
      elapsed        INTERVAL DAY TO SECOND;
      my_log_level   pls_integer;
   begin
      my_log_level := p_log_level;

--
      if my_log_level < 1 then my_log_level := 1; end if;
      if my_log_level > 9 then my_log_level := 9; end if;

--
      create_process_log (parm_job_log_id      => g_process_status_id,
                          parm_log_msg_id                 => NULL,
                          parm_log_msg                    => p_log_msg,
                          parm_log_level                  => my_log_level,
                          parm_elapsed_time               => elapsed,
                          parm_caller_name                => p_caller_name,
                          parm_line_number                => p_line_number,
                          parm_serial_number              => g_serial_nbr,
                          parm_call_stack                 => p_call_stack
                         );
--

   --
   end log_level;

--
--
   procedure begin_job (parm_process_name in varchar2)
   --% procedure begin_job (parm_process_name in varchar2)
   --% *create a record in job_log
   is
      PRAGMA AUTONOMOUS_TRANSACTION;
   begin
--
      if parm_process_name is NULL
      then
         g_process_name := g_caller_name;
      else
         g_process_name := parm_process_name;
      end if;

--
      g_process_start_tm := current_timestamp;

--
--   todo why are we no longer using the returning 
      select job_log_id_seq.NEXTVAL into g_process_status_id from DUAL; 
--
      INSERT into job_log
                  (process_name, schema_name, thread_name,
                   process_run_nbr, status_msg, status_id, status_ts, SID,
                   serial#, job_log_id
                  )
           VALUES (g_process_name, g_current_schema, 'main',
                                                -- no threading in pl/sql jobs
                   g_process_status_id,       -- no run number was being used
                                        'init',      -- @todo what's the point
                                               'A',
                                                -- active @todo document table
                                                   SYSDATE, g_sid,
                   g_serial_nbr, g_process_status_id
                  );

        --ut_surrogate_seq.NEXTVAL )
    --RETURNING job_log_id into g_process_status_id ;
--
      g_last_log_seq_nbr := 1;
--
      COMMIT;
   end begin_job;

   FUNCTION begin_java_job (p_process_name in varchar2,
			     p_class_name   in varchar2,
                             p_module_name  in varchar2,
			     p_status_msg   in varchar2,
			     p_thread_name  in varchar2)
            RETURN number
   --% procedure begin_job (parm_process_name in varchar2)
   --% *create a record in job_log
   --% 
   is
      PRAGMA AUTONOMOUS_TRANSACTION;
      my_trace_file_name varchar2(256);
   begin
--
      if p_process_name is NULL
      then
         g_process_name := g_caller_name;
      else
         g_process_name := p_process_name;
      end if;

--
      g_process_start_tm := current_timestamp;
      my_trace_file_name := get_trace_file_name();
--
--   todo why are we no longer using the returning 
      select job_log_id_seq.NEXTVAL 
      into g_process_status_id 
      from DUAL; 
--
      INSERT into job_log (
	process_name, 
	schema_name, 
	thread_name,
        process_run_nbr, 
	status_msg, 
	status_id, 
	status_ts, 
	SID,
        serial#, 
	job_log_id,
        class_name,
        module_name,
        trace_file_name
      )
      VALUES (
	g_process_name,          -- process_name
	g_current_schema,        -- schema_name 
	p_thread_name,           -- thread name
        g_process_status_id,    -- no run number was being used
        p_status_msg,            -- status msg
        'A',                     -- status id - 'A' active
        SYStimestamp, 		 --
	g_sid,                   -- sid
        g_serial_nbr,            -- serial#
        g_process_status_id,    -- status number TOOD
        p_class_name,
	p_module_name,
        my_trace_file_name
      );
        --ut_surrogate_seq.NEXTVAL )
    --RETURNING job_log_id into g_process_status_id ;
--
      g_last_log_seq_nbr := 1;
--
      COMMIT;
      return g_process_status_id;
   end begin_java_job;
--
--
   procedure end_job
   --% procedure end_job
   --% update job_log.status_id to 'C' and status_msg to 'DONE'
   is
      PRAGMA AUTONOMOUS_TRANSACTION;
      elapsed_tm   INTERVAL DAY TO SECOND;
   begin
      g_process_end_tm := current_timestamp;
      elapsed_tm := g_process_end_tm - g_process_start_tm;

--
      UPDATE job_log
         SET total_elapsed = elapsed_tm,
             SID = NULL,
             serial# = NULL,
             status_msg = 'DONE',
             status_id = 'C',
             status_ts = SYSDATE
       where job_log_id = g_process_status_id;

--
      COMMIT;
--
      close_log_file;
   end end_job;

--
-- private
   procedure abort_job
   --% procedure abort_job
   --% * update job_log 
   --% # elapsed_time 
   --% # status_id = 'I'
   --% # status_msg = 'ABORT'
   is
      PRAGMA AUTONOMOUS_TRANSACTION;
      elapsed_tm   INTERVAL DAY TO SECOND;
   begin
      g_process_end_tm := current_timestamp;
      elapsed_tm := g_process_end_tm - g_process_start_tm;

--
      UPDATE job_log
         SET total_elapsed = elapsed_tm,
             SID = NULL,
             serial# = NULL,
             status_msg = 'ABORT',
             status_id = 'I',
             status_ts = SYSDATE
       where job_log_id = g_process_status_id;

--
      close_log_file;
--
      COMMIT;
   end abort_job;

--
--
-- @todo set status messages in info
-- @tod support run as job
--

   --
   procedure LOG (p_level in pls_integer, p_log_msg IN varchar2)
   is
   begin
      log_level (p_log_level        => p_level,
                 p_log_msg          => p_log_msg,
                 p_caller_name      => g_caller_name,
                 p_line_number      => g_line_number
                );
   end LOG;

--
--
-- @todo print full stack trace
-- public
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

--
--
-- public
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

--
--
-- public
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

      log_level (p_log_level        => g_info,
                 p_log_msg          => p_log_msg,
                 p_caller_name      => p_unit,
                 p_line_number      => p_line,
                 p_call_stack       => stack
                );
   end info;

--
--
-- private
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
--
      log_level (p_log_level        => g_snap,
                 p_log_msg          => p_log_msg,
                 p_caller_name      => g_caller_name,
                 p_line_number      => g_line_number
                );
   end log_snap;

--
--
-- public
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
   --
      if p_set_action then set_action($$PLSQL_UNIT) ; end if;
   end entering;

--
-- public
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

--
--
-- public
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

--
--
-- public
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

--
--
-- public
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

--
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

--
      if my_log_level < 1 then my_log_level := 1; end if;

--
      if my_log_level > 9 then my_log_level := 9; end if;

--
--    if p_elapsed_time != NULL then
--        my_elapsed := p_elapsed_time;
--    else
--        my_elapsed := my_log_time - g_last_log_time;
--    end if ;
--
--    g_last_log_time := my_log_time;
--
      create_process_log (parm_job_log_id   => g_process_status_id,
                          parm_log_msg_id              => p_msg_id,
                          parm_log_msg                 => p_log_msg,
                          parm_log_level               => my_log_level,
                          parm_elapsed_time            => my_elapsed,
                          parm_caller_name             => g_caller_name,
                          parm_line_number             => g_line_number,
                          parm_serial_number           => g_serial_nbr,
                          parm_call_stack              => p_call_stack
                         );
   end LOG;

--
--
   procedure snap_stats (p_snap_name in varchar2)
   is
      my_log_time   timestamp ( 6 )        := current_timestamp;
      my_elapsed    INTERVAL DAY TO SECOND;
   begin
--
      create_process_log (parm_job_log_id      => g_process_status_id,
                          parm_log_msg_id                 => p_snap_name,
                          parm_log_msg                    => p_snap_name,
                          parm_log_level                  => g_snap,
                          parm_elapsed_time               => my_elapsed,
                          parm_caller_name                => g_caller_name,
                          parm_line_number                => g_line_number,
                          parm_serial_number              => g_serial_nbr
                         );

--
      INSERT into ut_process_stat (	
	     job_log_id, log_seq_nbr,       statistic#,      VALUE
      )
      select g_process_status_id, g_last_log_seq_nbr, stat.statistic#, stat.VALUE
        from SYS.v$mystat stat
       where SID = g_sid;
--
   end snap_stats;

--
----
--    every log entry at or below the filter level are recorded.. only to the file
--
   procedure set_filter_level (p_level in pls_integer)
   --% procedure set_filter_level (p_level in pls_integer)
   --% log levels less than or equal to the specified value are written to the log file
   is
   begin
      if p_level < 1 
      then
	g_filter_level := 1;
      elsif p_level > 9
      then 
	g_filter_level := 9;	
      else 
      	g_filter_level := p_level;
      end if;
   end set_filter_level;

   /**
   	procedure set_record_level (p_level in pls_integer)
   */
   procedure set_record_level (p_level in pls_integer)
   --% procedure set_record_level (p_level in pls_integer)
   --% everything less than or equal to the record level gets written to the trace file
   is
   begin
      if p_level < 1 
      then
	g_record_level := 1;
      elsif p_level > 9
      then 
	g_record_level := 9;	
      else 
      	g_record_level := p_level;
      end if;
   end set_record_level;

--
--
--      Wrapper for DBMS_APPLICATION_INFO.SET_ACTION
--
    procedure set_action ( p_action in varchar2 ) is
    begin
            dbms_application_info.set_action(substr(p_action, 1, 32)) ;
    end set_action ;
--
--
--
--      Wrapper for DBMS_APPLICATION_INFO.SET_MODULE
--
    procedure set_module ( p_module in varchar2 )
    is
    begin
            dbms_application_info.set_module(p_module, 'Uncategorized') ;
    end set_module ;
--
--


   function get_directory_path return varchar is
      -- todo see if grants are wrong, permission must be granted to the user
      cursor directory_cur is
      select owner, directory_name, directory_path
      from all_directories
      where directory_name = g_job_msg_dir;

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
         my_file_handle := UTL_FILE.fopen ('job_msg_DIR', 'wtf.log', 'A');
         UTL_FILE.put_line (my_file_handle, 'wtf');
	 utl_file.fclose (my_file_handle); 
   end;

--`
  function basename (p_full_path in varchar2,
                     p_suffix    in varchar2 default null,
                     p_separator in char default '/')
   return varchar2
   --* like bash basename or gnu basename, returns the filename of a path optionally 
   --* stripping the specified file extension
   --'
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
    
        dbms_lob.LoadClobFromFile
        (
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
     
        --dbms_output.put_line(my_clob);
       
        return my_clob;
    end get_tracefile;

begin
--    dbms_output.ENABLE(1000000) ;
--
   select v.SID, v.serial#
     into g_sid, g_serial_nbr
     from SYS.v_$session v
    where v.SID = (select SID
                     from SYS.v_$mystat
                    where ROWNUM = 1);
   
--
   set_context;
end logger;
/ 
show errors
exit;
