package org.javautil.util;

import java.math.BigDecimal;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map.Entry;
import java.util.TreeMap;

// TODO consolidate with Binds
public class NameValue extends LinkedHashMap<String, Object> {
    /**
     *
     */
    private static final long serialVersionUID = 1L;

    public NameValue() {
        super();
    }

    public TreeMap<String, Object> getAsTreeMap() {
        return getAsTreeMap(false);
    }
    /*
     * Postgres returns column names in lowercase oracle in upper
     */

    public TreeMap<String, Object> getAsTreeMap(boolean toLowerCase) {
        final TreeMap<String, Object> retval = new TreeMap<>();

        if (toLowerCase) {
            for (final Entry<String, Object> e : entrySet()) {
                retval.put(e.getKey().toLowerCase(), e.getValue());
            }
        } else {
            retval.putAll(this);
        }
        return retval;
    }

    public String getSortedMultilineString() {
        final StringBuilder sb = new StringBuilder();
        final TreeMap<String, Object> sorted = new TreeMap<>();
        sorted.putAll(this);
        for (final Entry<String, Object> e : sorted.entrySet()) {
            final String valueString = e.getValue() == null ? "null" : "'" + e.getValue() + "'";
            sb.append(String.format("'%s': %s\n", e.getKey(), valueString));
        }
        return sb.toString();
    }

    public Object getObject(String name) {
        return get(name);
    }

    public String getString(String name) {
        final Object o = get(name);
        String retval = null;
        if (o != null) {
            retval = o.toString();
        }
        return retval;
    }

    public Long getLong(String name) {
        final Object o = get(name);
        Long retval = null;
        retval = new BigDecimal(o.toString()).longValue();
        return retval;
    }

    public Date getDate(String name) {
        return (Date) get(name);
    }

    public Object get(String name) {
        final Object retval = super.get(name);
        if (retval == null) {
            if (!super.containsKey(name)) {
                throw new IllegalArgumentException(String.format("key: '%s' not found.", name));
            }
        }
        return retval;
    }
}
