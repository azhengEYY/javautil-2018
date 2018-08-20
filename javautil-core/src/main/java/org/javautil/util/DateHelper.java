package org.javautil.util;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;

public class DateHelper {
	public static java.util.Date asUtilDate(Object date, ZoneId zone) {
		if (date == null) {
			return null;
		}

		if ((date instanceof java.sql.Date) || (date instanceof java.sql.Timestamp)) {
			return new java.util.Date(((java.util.Date) date).getTime());
		}
		if (date instanceof java.util.Date) {
			return (java.util.Date) date;
		}
		if (date instanceof LocalDate) {
			return java.util.Date.from(((LocalDate) date).atStartOfDay(zone).toInstant());
		}
		if (date instanceof LocalDateTime) {
			return java.util.Date.from(((LocalDateTime) date).atZone(zone).toInstant());
		}
		if (date instanceof ZonedDateTime) {
			return java.util.Date.from(((ZonedDateTime) date).toInstant());
		}
		if (date instanceof Instant) {
			return java.util.Date.from((Instant) date);
		}

		throw new UnsupportedOperationException(
				"Don't know hot to convert " + date.getClass().getName() + " to java.util.Date");
	}
}
