

Spring Developers
-----------------
Oracle tracing is a powerful tool that logs detailed information about all calls
to the Oracle database.

In order to use this 
* one must turn on tracing for the current connection
* set the log file
* stop tracing
* call a service to store the trace


* store the raw trace file
* analyze the trace file
* store the analyzed trace file
* examine

Oracle Tracing
**************
The first call in a transaction should be 

.. code-block:: java

   dbStats.traceOn(final String module);

Intermediate calls can be made to record various steps to see activity within
that particular step. This call is extremely lightweight and only results in 
a change to a structure in oracle memory *SGA v_$session.Action*.

.. code-block:: java

    dbStats.setAction(final String action);

If the connection is not an Oracle connection this is logged in the slf4j logger
which may be ignored by configuring log4j.xml or your appropriate logger setup
for the class org.javautil.oracle.OraclHelper and the call does nothing.

Now the log file must be retrieved and analyzed or simply stored for later analysis.

.. code-block:: java

    dbStats.saveTrace(final String action);




Tracing should do the following

* Begin with any transaction as annotated by @Transactional

Tracing
-------
Oracle tracing is enabled 

Concepts
********

