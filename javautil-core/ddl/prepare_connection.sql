-- https://docstore.mik.ua/orelly/oracle/guide8i/ch08_04.htm
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
