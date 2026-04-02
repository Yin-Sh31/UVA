package com.cty.nopersonfinally.utils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * 日期时间工具类
 * 处理LocalDateTime和LocalDate的格式化与解析
 */
public class DateUtil {

    /**
     * 日期时间格式：yyyy-MM-dd HH:mm:ss
     */
    public static final String DATETIME_FORMAT = "yyyy-MM-dd HH:mm:ss";

    /**
     * 日期格式：yyyy-MM-dd
     */
    public static final String DATE_FORMAT = "yyyy-MM-dd";

    /**
     * 格式化LocalDateTime为字符串
     * @param dateTime 日期时间对象
     * @return 格式化后的字符串
     */
    public static String formatDateTime(LocalDateTime dateTime) {
        if (dateTime == null) {
            return null;
        }
        return dateTime.format(DateTimeFormatter.ofPattern(DATETIME_FORMAT));
    }

    /**
     * 格式化LocalDate为字符串
     * @param date 日期对象
     * @return 格式化后的字符串
     */
    public static String formatDate(LocalDate date) {
        if (date == null) {
            return null;
        }
        return date.format(DateTimeFormatter.ofPattern(DATE_FORMAT));
    }

    /**
     * 解析字符串为LocalDateTime
     * @param dateTimeStr 日期时间字符串
     * @return LocalDateTime对象
     */
    public static LocalDateTime parseDateTime(String dateTimeStr) {
        if (dateTimeStr == null || dateTimeStr.trim().isEmpty()) {
            return null;
        }
        return LocalDateTime.parse(dateTimeStr, DateTimeFormatter.ofPattern(DATETIME_FORMAT));
    }

    /**
     * 解析字符串为LocalDate
     * @param dateStr 日期字符串
     * @return LocalDate对象
     */
    public static LocalDate parseDate(String dateStr) {
        if (dateStr == null || dateStr.trim().isEmpty()) {
            return null;
        }
        return LocalDate.parse(dateStr, DateTimeFormatter.ofPattern(DATE_FORMAT));
    }
}
