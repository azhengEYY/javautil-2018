Connection Pools
================

After Getting a connection
--------------------------

Contexts
********

If a session is being used as part of a connection pool and the state of its contexts are not reinitialized, this can lead to unexpected behavior.

Packages
********

TODO Sessions have the ability to alter package state by amending the values of package variables. If a session is being used as part of a connection pool and the state of its packages are not reinitialized, this can lead to unexpected behavior. To solve this, the RESET_PACKAGE

Connections must be reset immediately after being obtained from a connection pool

In javutil/oralit/prepare_connection.sql:

Convenience Procedure
*********************

.. code-block:: plsql


    CREATE OR REPLACE PROCEDURE prepare_connection
    AS
        context_info DBMS_SESSION.AppCtxTabTyp;
        info_count PLS_INTEGER;
        indx PLS_INTEGER;
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
    END;
    /

    create public synonym prepare_connection for prepare_connection;
    grant execute on prepare_connection to public;

Zaxxer
******
TODO how to call this procedure in the connection pool



DBMS_SESSION
------------

Identifier
----------
SET_IDENTIFIER and CLEAR_IDENTIFIER procedures to allow the real user to be associated with a session, regardless of what database user was being used for the connection. 



Metrics
-------
try {
  String e2eMetrics[] = new String[OracleConnection.END_TO_END_STATE_INDEX_MAX];
  e2eMetrics[OracleConnection.END_TO_END_ACTION_INDEX]   = null;
  e2eMetrics[OracleConnection.END_TO_END_MODULE_INDEX]   = null;
  e2eMetrics[OracleConnection.END_TO_END_CLIENTID_INDEX] = null;
  ((OracleConnection) conn).setEndToEndMetrics(e2eMetrics, Short.MIN_VALUE);
} catch (SQLException sqle) {
  // Do something...
}

0 - No trace. Like switching sql_trace off.
2 - The equivalent of regular sql_trace.
4 - The same as 2, but with the addition of bind variable values.
8 - The same as 2, but with the addition of wait events.
12 - The same as 2, but with both bind variable values and wait events.

Monitoring long running
https://oracle-base.com/articles/11g/real-time-sql-monitoring-11gr1


References
----------
https://oracle-base.com/articles/misc/dbms_session

https://oracle-base.com/articles/misc/sql-trace-10046-trcsess-and-tkprof
