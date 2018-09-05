package org.javautil.oracle.trace.record;

import org.javautil.oracle.trace.CursorInfo;
import org.junit.Test;

public class CursorInfoTest {
    
    String[] lines = {
    "PARSING IN CURSOR #140603589769800 len=49 dep=2 uid=0 oct=3 lid=0 tim=2359115366 hv=1758550401 ad='60f33380' sqlid='0a7q9v9nd2qc1'",
    "select count(*) from association$ where obj# = :1",
    "END OF STMT",
    "PARSE #140603589769800:c=0,e=14,p=0,cr=0,cu=0,mis=0,r=0,dep=2,og=4,plh=3747789477,tim=2359115366",
    "EXEC #140603589769800:c=0,e=16,p=0,cr=0,cu=0,mis=0,r=0,dep=2,og=4,plh=3747789477,tim=2359115424",
    "FETCH #140603589769800:c=0,e=9,p=0,cr=1,cu=0,mis=0,r=1,dep=2,og=4,plh=3747789477,tim=2359115448",
    "STAT #140603589769800 id=1 cnt=1 pid=0 pos=1 obj=0 op='SORT AGGREGATE (cr=1 pr=0 pw=0 str=1 time=12 us)'",
    "STAT #140603589769800 id=2 cnt=0 pid=1 pos=1 obj=636 op='INDEX RANGE SCAN ASSOC1 (cr=1 pr=0 pw=0 str=1 time=8 us cost=0 size=13 card=1)'"
    };
    
    // TODO create mock reader 
    @Test
    public void test() {
        CursorInfo ci = new CursorInfo();
        Parsing parsing = new Parsing(lines[0], 1);
        Parse parse = new Parse(lines[3],4);
        Exec exec = new Exec(lines[4],5);
        Stat stat1 = new Stat(lines[6],7);
        Stat stat2 = new Stat(lines[7],8);
        ci.addStat(stat1);
        ci.addStat(stat2);
        String explainPlan = ci.formatExplainPlan();
        System.out.println("explainPlan\n" + explainPlan);
    }

}
