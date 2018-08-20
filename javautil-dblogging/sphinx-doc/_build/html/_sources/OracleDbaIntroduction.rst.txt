.. Springboot Sample Projects documentation master file, created by
   sphinx-quickstart on Tue Jul 10 16:07:36 2018.


DBMS_APPLICATION_INFO
&&&&&&&&&&&&&&&&&&&&&
In memory fields associated with your current session can be performed extremely quickly.
This is stored in a session level data structure in the Oracle SGA, *sys.v_$session*, 

Benefits:
        The state of your connection can be quickly and easily viewed outside your app
        by a DBA.

        This information is written to the oracle trace log.

        In order to aggregate this information all of the oracle trace logs must be
        scanned to look for this module.

Module
&&&&&&

What is a module?  For this purpose consider it any transaction.
Once spring starts a transaction as Annoted with @Transactional an AOP
aspect can start the trace process.

Action
&&&&&&

Turning on statistic and trace
&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&
.. code-block:: sql

        alter session set timed_statistics = true;
        alter session set max_dump_file_size = unlimited
        alter session set sql_trace = true
        alter session set events '10046 trace name context forever, level 12'

Setting the trace file identifier
&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&
.. code-block:: sql
        alter session set tracefile_identifier = 'text'

Getting the name of the trace file
&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&
        "SELECT VALUE FROM V$DIAG_INFO WHERE NAME = 'Default Trace File'"

Setting identifier
&&&&&&&&&&&&&&&&&&
        dbms_session.set_identifier(:txt)

Setting client info
&&&&&&&&&&&&&&&&&&&
        dbms_application_info.set_client_info(:txt)

Setting module
&&&&&&&&&&&&&&
        dbms_application_info.set_module(:module_name,:action_name)

Setting Action
&&&&&&&&&&&&&&
        dbms_application_info.set_action(:action_name)

