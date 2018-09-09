Example
=======

.. code-block:: java

    package org.javautil.dblogging;
    
    import java.sql.Connection;
    import java.sql.SQLException;
    
    import org.javautil.sql.Binds;
    import org.javautil.sql.ConnectionUtil;
    import org.javautil.sql.SqlStatement;
    import org.javautil.util.NameValue;
    
    public class DbloggerForOracleExample {
    
    
        private Dblogger   dblogger;
        private Connection connection;
        private String     processName;
        private boolean testAbort = false;
    
        public DbloggerForOracleExample(Connection connection, Dblogger dblogger, String processName, boolean testAbort) {
            this.connection = connection;
            this.dblogger = dblogger;
            this.processName = processName;
            this.testAbort = testAbort;
        }
    
        public long process() throws SQLException {
            dblogger.prepareConnection();
            long id = 0;
    
            try {
             id = dblogger.beginJob(processName, getClass().getCanonicalName(), "ExampleLogging", null,
                    Thread.currentThread().getName(), null);
            actionNoStep();
            stepNoAction();
            stepTwo();
            if (testAbort) {
                int x = 1 / 0;
            }
            dblogger.endJob();
            } catch (Exception e) {
                dblogger.abortJob(e);
            }
         
            return id;
    
        }
    
       
    
        private void actionNoStep() throws SQLException {
            dblogger.setAction("Some work");
            ConnectionUtil.exhaustQuery(connection, "select * from user_tab_columns, user_tables");
        }
    
        private void stepNoAction() throws SQLException {
            dblogger.insertStep("Useless join", "full join", getClass().getName());
            ConnectionUtil.exhaustQuery(connection, "select * from user_tab_columns, user_tables");
            dblogger.finishStep();
        }
        
        private void stepTwo() throws SQLException {
            dblogger.insertStep("count full", "full join", getClass().getName());
            ConnectionUtil.exhaustQuery(connection, "select count(*) dracula from user_tab_columns, user_tables");
            dblogger.finishStep();
        }
    
    }


Calls
-----

Begin Job
*********

.. code-block:: java

             long id = dblogger.beginJob(processName, getClass().getCanonicalName(), "ExampleLogging", null,
                    Thread.currentThread().getName(), null);

After a connection is retrieved from the connection pool or the beginning of a Spring transactional event start a logging job

Abort Job
*********

.. code-block:: java

    catch (Exception e) {
            dblogger.abortJob(e);
        }

End Job
*******

.. code-block:: java

    dblogger.endJob();

Insert Step
***********

.. code-block:: java

    void insertStep() throws SQLException;

Finish Step
***********

.. code-block:: java

    void finishStep() throws SQLException;


Effect
------

Begin Job
*********

* sets the trace_identifier to create a new oracle trace file
* turns on tracing
* insert a record into job log

.. code-block:: java

             long id = dblogger.beginJob(processName, getClass().getCanonicalName(), "ExampleLogging", null,
                    Thread.currentThread().getName(), null);

After a connection is retrieved from the connection pool or the beginning of a Spring transactional event start a logging job

Abort Job
*********

.. code-block:: java

    catch (Exception e) {
            dblogger.abortJob(e);
        }

End Job
*******

.. code-block:: java

    dblogger.endJob();

Insert Step
***********

.. code-block:: java

    void insertStep() throws SQLException;

Finish Step
***********

.. code-block:: java

    void finishStep() throws SQLException;
