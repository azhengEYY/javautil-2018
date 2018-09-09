Trace Records
=============

Fields
------

---- --------------------  ------------------------------------------------------------------------------- 
Code Name                  Description
---- --------------------  ------------------------------------------------------------------------------- 
 c   currentModeBlocks     CPU time (100th's of a second in Oracle7 ,8 and 9).
 e   elapsedMicroseconds   Elapsed time (100th's of a second Oracle7, 8 Microseconds in Oracle 9 onwards).
 p   physicalBlocksRead    Number of physical reads.
cr   consistentReadBlocks  Number of buffers retrieved for CR reads.
cu   currentMode           Number of buffers retrieved in current mode.
mis  libraryCacheMissCount Cursor missed in the cache.
r    rowCount              Number of rows processed.
dep  recursiveDepth        Recursive call depth (0 = user SQL, >0 = recursive).
og   optimizerGoal         Optimizer goal:  1=All_Rows  2=First_Rows,  3=Rule,  4=Choose
pw   physicalWrites        Physical Write
tim  time                  Timestamp (large number in 100ths of a second). Use this to determine the time between any 2 operations.


Stat Records
id   id                    Stat Record identifier, starts with ??? per sqlid
obj  objectNumber
op   operation
pid  parentId              References id (also used in formatting operation depth)
pos  position              Position in explain plan STAT 
