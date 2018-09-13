package org.javautil.oracle.trace.record;
/**
<pre>
                                                       <==== Cursor Operatiom ====>
                      Type       Parsing    ParseError Parse      Exec       Fetch      Stat
#          cursorNumbe                      x                     x          x          x          
ad         sgaAddress            X
c          CpuMicroSec                                 X          X          X
card       cardinality
cnt                                                                                     X
cost       cost (optim
cr         consistentR                                 x          x          x
cu         currentMode                                 X          X          X
dep        depth                 x          x          x          x          x
e          elapsedMicr                                 x          x          x
err        oracleError                      X
hv         sqlHashValu           x
id                                                                                      X
len        sqlTextLeng           x          x
lid                              x          x
mis        libraryCach                                 X          X          X
obj        objectNumbe                                                                  X
oct        oracleComma           x          x
og         optimizerGo                                 x          x          x
op         operation
p          physicalBlo                                 x          x          x
pid        processId                                                                    X
plh                                                    x          x          x
pos        position (o                                                                  x
pw         physicalWri                                                                  x
r          rowCount                                    x          x          x
size
sqlid      sqlId                 x
str                                                                                     x
tim                                                    x          x          x
time
uid                              x          x

</pre>

 * og Optimizer goal:
 * 1=All_Rows,
 * 2=First_Rows,
 * 3=Rule,
 * 4=Choose
*/


public interface Record {

    public int getLineNumber();

    public String getText();

    public RecordType getRecordType();

    public String getLineAndText();

}
