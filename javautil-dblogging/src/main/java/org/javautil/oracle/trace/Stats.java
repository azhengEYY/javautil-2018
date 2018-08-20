package org.javautil.oracle.trace;

import java.util.List;

import org.javautil.oracle.trace.record.Stat;

public class Stats {
    private List<Stat> statList;

    public Stats(List<Stat> statList) {
        this.statList = statList;
    }

    void setDepth() {
        statList.get(0).setDepth(0);
        int statNbr = 0;
        for (Stat stat : statList) {
            if (statNbr == 0) {
                stat.setDepth(0);
            }
            Stat parent = statList.get(stat.getParentId());
            stat.setDepth(parent.getDepth() + 1);
        }
    }
}
