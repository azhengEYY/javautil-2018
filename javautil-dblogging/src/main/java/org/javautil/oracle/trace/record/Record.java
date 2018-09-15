package org.javautil.oracle.trace.record;
/**
<pre>
                                                       <==== Cursor Operatiom ====>
                      Type       Parsing    ParseError Parse      Exec       Fetch      Stat  Lobread Lobpgsize Close
#          cursorNumbe                      x                     x          x          x          
ad         sgaAddress            X
bytes      bytes                                                                                  X      X
c          CpuMicroSec                                 X          X          X                    X      X         x
card       cardinality
cnt                                                                                     O
cost       cost (optim
cr         consistentR                                 x          x          x                    x      x
cu         currentMode                                 X          X          X                    x      x
dep        depth                 x          x          x          x          x                                     x
e          elapsedMicr                                 x          x          x                    X      X         x
err        oracleError                      X
hv         sqlHashValu           x
id                                                                                      X
len        sqlTextLeng           x          x
lid                              x          x
mis        libraryCach                                 X          X          X
obj        objectNumbe                                                                  X
oct        oracleComma           x          x
og         optimizerGo                                 x          x          x
op         operation                                                                    X
p          physicalBlo                                 x          x          x                   x       x
pid        processId                                                                    X
plh                                                    x          x          x
pos        position (o                                                                  x
pw         physicalWri                                                                  x
r          rowCount                                    x          x          x
size                                                                                    O
sqlid      sqlId                 x
str                                                                                     x
tim                                                    x          x          x                   x       x        x
time
type                                                                                                              x
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
