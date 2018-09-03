package org.javautil.oracle.trace.record;

import java.util.regex.Pattern;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

// TODO eliminate Autoboxing
public abstract class AbstractCursorEvent extends AbstractRecord implements CursorRecord {
    protected static final Pattern sqlTextLengthPattern     = Pattern.compile(" len=(\\d*) ");
    protected static final Pattern recursionDepthPattern    = Pattern.compile(" dep=(\\d*) ");
    protected static final Pattern userIdPattern            = Pattern.compile(" uid=(\\d*) ");
    protected static final Pattern oracleCommandTypePattern = Pattern.compile(" oct=(\\d*) ");
    protected static final Pattern lidPattern               = Pattern.compile(" lid=(\\d*) ");
    protected static final Pattern timPattern               = Pattern.compile(" tim=(\\d*) ");
    protected static final Pattern sqltextHashValuePattern  = Pattern.compile(" hv=(\\d*) ");
    protected static final Pattern sgaAddressPattern        = Pattern.compile(" ad='([^']*)");
    protected static final Pattern sqlidPattern             = Pattern.compile(" sqlid='([^']*)");
    // stat fields

    protected static final Pattern idPattern                = Pattern.compile("id=(\\d*)");

    protected static final Pattern cntPattern               = Pattern.compile("cnt=(\\d*)");
    protected static final Pattern parentIdPattern          = Pattern.compile("pid=(\\d*)");
    protected static final Pattern posPattern               = Pattern.compile("pos=(\\d*).*");
    protected static final Pattern objPattern               = Pattern.compile("obj=(\\d*)");
    protected static final Pattern operationPattern         = Pattern.compile("op='(.*) \\(");
    protected static final Pattern currentReadPattern       = Pattern.compile("\\(cr=(\\d*)");
    protected static final Pattern physicalReadPattern      = Pattern.compile("pr=(\\d*)");
    protected static final Pattern physicalWritePattern     = Pattern.compile("pw=(\\d*)");
    protected static final Pattern timePattern              = Pattern.compile("time='(\\d*)");

    // private static Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private Long                   cursorNumber;

    private String                 sqlid;

    private String                 cursorId;

    public AbstractCursorEvent(int lineNumber, String stmt) {
        super(lineNumber, stmt);
    }

    @Override
    public final Long getCursorNumber() {
        return cursorNumber;
    }

    @Override
    public final void setCursorNumber(final Long l) {
        this.cursorNumber = l;
    }

    @Override
    public String getSqlid() {
        return sqlid;
    }

    @Override
    public void setSqlid(String sqlid) {
        this.sqlid = sqlid;
    }

    @Override
    public String getCursorId() {
        return cursorId;
    }

    public void setCursorId(String cursorId) {
        this.cursorId = cursorId;
    }

    @Override
    public String toString() {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        return gson.toJson(this);
    }

}
