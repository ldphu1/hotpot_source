package com.fourspicehotpot.backend.common.util;

import org.springframework.stereotype.Component;

import java.time.*;

@Component
public class DateTimeUtils {

    private static final ZoneId ZONE = ZoneId.of("Asia/Ho_Chi_Minh");

    // Lấy hiện tại
    public static LocalDateTime now() {
        return ZonedDateTime.now(ZONE).toLocalDateTime();
    }

    // Hôm nay
    public static LocalDate today() {
        return LocalDate.now(ZONE);
    }

    // Start ngày
    public static LocalDateTime startOfDay() {
        return today().atStartOfDay();
    }

    // End ngày (exclusive)
    public static LocalDateTime endOfDay() {
        return today().plusDays(1).atStartOfDay();
    }

    // Start tuần
    public static LocalDateTime startOfWeek() {
        LocalDate now = today();
        return now.with(DayOfWeek.MONDAY).atStartOfDay();
    }

    // End tuần
    public static LocalDateTime endOfWeek() {
        return startOfWeek().plusWeeks(1);
    }

    // Start tháng
    public static LocalDateTime startOfMonth() {
        return today().withDayOfMonth(1).atStartOfDay();
    }

    public static LocalDateTime endOfMonth() {
        return startOfMonth().plusMonths(1);
    }

    // Start năm
    public static LocalDateTime startOfYear() {
        return today().withDayOfYear(1).atStartOfDay();
    }

    public static LocalDateTime endOfYear() {
        return startOfYear().plusYears(1);
    }
}