Overview
========


Terminology
-----------

What is a transaction?
----------------------

We will differentiate database transactions from spring transactions.

@Transactional
**************
ref: https://dzone.com/articles/how-does-spring-transactional

~OracleTransaction
******************
*A transaction is a logical unit of work that contains one or more SQL statements. A transaction is an atomic unit. The effects of all the SQL statements in a transaction can be either all committed (applied to the database) or all rolled back (undone from the database).

A transaction begins with the first executable SQL statement. A transaction ends when it is committed or rolled back, either explicitly with a COMMIT or ROLLBACK statement or implicitly when a DDL statement is issued.*

ref:  https://docs.oracle.com/cd/B19306_01/server.102/b14220/transact.htm



Session
-------
@@SpringSession
Associated with a client user, starts after login. TODO explain

~OracleSession
**************
An oracle session begins when the DataSource ConnectionPool calls connection() 
on the underlying jdbc driver.

It is **not** created with each call to dataSource.getConnection(), these are 
shared in a ConnectionPool.


Api Calls
*********



Tracking Level
--------------

Client Identifier
*****************

TODO what is it

Redo
----
Store elsewhere

Bind Variables
--------------

Compression
-----------
Statement hashes and stats

String length
hashes 
Statements stored just once 

Redo
----
Why a separate databse


Connection Pools
================

After Getting a connection
--------------------------

Client Identifier
-----------------
The client identifier should be set with either 

* The logged in user name
* The daemon name 

Module
------

Action
------

TIFIER and CLEAR_IDENTIFIER procedures to allow the real user to be associated with a session, regardless of what database user was being used for the connection. 

Session



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

TODO
----


References
----------
https://oracle-base.com/articles/misc/dbms_session

https://oracle-base.com/articles/misc/sql-trace-10046-trcsess-and-tkprof

Separate Container and database for logging postgres

TODO state diagram with other servive

Performance Impact

Create a new file for to minimize trcsess overhead
replicate trace files
