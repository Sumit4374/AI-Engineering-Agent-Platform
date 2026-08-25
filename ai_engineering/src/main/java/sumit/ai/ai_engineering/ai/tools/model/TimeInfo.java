package com.ai_engineering.ai_service.tools.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;

public record TimeInfo(
    LocalDate date,
    LocalTime time,
    LocalDateTime dateTime,
    ZoneId zone
) {}
