set define on
set echo on

alter session set container = Sales_reporting_pdb;
drop user Sr cascade;
drop role dblogging;
exit;

