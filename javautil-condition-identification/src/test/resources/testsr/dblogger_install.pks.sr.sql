
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
    procedure begin_job (p_process_name in varchar2);
    function get_my_tracefile return clob;

  FUNCTION begin_java_job ( p_process_name in varchar2,
                             p_class_name   in varchar2,
                             p_module_name  in varchar2,
                             p_status_msg   in varchar2,
                             p_thread_name  in varchar2,
                             p_trace_level  in pls_integer default G_INFO) 
   return number;

    
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
        p_module_name IN        VARCHAR,
        p_action_name in   varchar) ;
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


    function basename (p_full_path in varchar2,
                       p_suffix    in varchar2 default null,
                       p_separator in char default '/') return varchar2;

    procedure prepare_connection;

    procedure trace_step(p_step_name in varchar);

    function open_log_file(p_file_name in varchar)  return varchar;

    function set_tracefile_identifier(p_job_nbr in number) return varchar;
END logger ;
/
--#<
show errors
--#>

