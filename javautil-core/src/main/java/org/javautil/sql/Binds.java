package org.javautil.sql;

import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// Needs unit tests
public class Binds extends LinkedHashMap<String, Object> {
    /**
     *
     */
    private static final long serialVersionUID = 283357236262161762L;
    private final Logger      logger           = LoggerFactory.getLogger(this.getClass());
    private String            sqlName;
    private String            sqlText;

    public Binds() {
        super();
    }

    public Binds(Map<String, Object> bindMap) {
        super.putAll(bindMap);
    }

    public Binds putBind(final String bindName, final Object value) {
        super.put(bindName, value);
        return this;
    }

    public String getString(String bindName) {

        final Object o = getInsensitive(bindName);
        String retval = null;
        if (o != null) {
            retval = o.toString();
        }
        return retval;

    }

    // TODO create lowercase map
    private Object getInsensitive(String bindName) {
        Object o;
        String searchKey = bindName;
        while (true) {
            if (super.containsKey(searchKey)) {
                o = super.get(bindName);
                break;
            }
            searchKey = bindName.toLowerCase();
            if (super.containsKey(searchKey)) {
                o = super.get(searchKey);
                break;
            }
            searchKey = bindName.toUpperCase();
            if (super.containsKey(searchKey)) {
                o = super.get(searchKey);
                break;
            }

            throw new IllegalArgumentException("key '" + bindName + "' not found in " +
                    super.keySet());
        }
        if (!searchKey.equals(bindName)) {
            logger.debug("specified '" + bindName + "' was '" + searchKey + "'");
        }
        if (o == null) {
            logger.debug("returning null for " + bindName);
        } else {
            logger.debug("returning " + o + " for " + bindName);
        }
        return o;
    }

    public Object get(String bindName) {
        return getInsensitive(bindName);
    }

    public Long getLong(String bindName) {

        final Object o = getInsensitive(bindName);
        Long retval = null;
        if (o != null) {
            if (o instanceof Long) {
                retval = (Long) o;
            } else {
                if (o instanceof Number) {
                    final Number n = (Number) o;
                    // todo use BigNumber trick
                    retval = n.longValue();
                } else {
                    throw new UnsupportedOperationException("cannot convert " + o + " to Long");
                }
            }
        }
        return retval;

    }

    public Date getDate(String bindName) {
        final Object date = getInsensitive(bindName);
        Date retval;
        if ((date instanceof java.sql.Date) || (date instanceof java.sql.Timestamp)) {
            retval = new java.util.Date(((java.util.Date) date).getTime());
        } else if (date instanceof java.util.Date) {
            retval = (java.util.Date) date;
        } else {
            throw new UnsupportedOperationException(
                    "Don't know hot to convert " + date.getClass().getName() + " to java.util.Date");
        }
        return retval;
    }

    public Binds getLowerCaseKeys() {
        final Binds retval = new Binds();
        for (final java.util.Map.Entry<String, Object> e : this.entrySet()) {
            retval.put(e.getKey().toLowerCase(), e.getValue());
        }
        return retval;
    }

    public String getSqlName() {
        return sqlName;
    }

    public void setSqlName(String sqlName) {
        this.sqlName = sqlName;
    }

    public String getSqlText() {
        return sqlText;
    }

    public void setSqlText(String sqlText) {
        this.sqlText = sqlText;
    }
}
