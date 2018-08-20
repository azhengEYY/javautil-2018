set echo on
alter pluggable database sales_reporting_pdb close immediate;

drop pluggable database sales_reporting_pdb including datafiles;
