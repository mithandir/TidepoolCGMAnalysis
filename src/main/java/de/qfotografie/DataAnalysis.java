/*
 * Copyright (C) Schweizerische Bundesbahnen SBB, 2016.
 */

package de.qfotografie;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.qfotografie.REST.TidepoolController;
import de.qfotografie.akm.DataPoint;

public class DataAnalysis {

    private static final Logger LOGGER = LoggerFactory.getLogger(TidepoolController.class);

    /**
     * Filter GCM Data from from List of {@Datapoint}
     *
     * @param data List of {@Datapoint}
     * @return {@List<DataPoint>}
     */
    public static List<DataPoint> getCGMData(List<DataPoint> data) {
        if (data.isEmpty()) {
            LOGGER.error("No CGM Data available");
            throw new IllegalArgumentException("No CGM Data available");
        }
        return data.stream()
                .filter(u -> u.getType().equals("cbg"))
                .sorted((DataPoint o1, DataPoint o2)->o2.getTime().compareTo(o1.getTime()))
                .collect(Collectors.toList());
    }

    /**
     * Get average glucose level for today
     *
     * @param data List of {@Datapoint}
     * @return {@double} avergae glucose level
     */
    public static double getTodayAvgLevel(List<DataPoint> data) {
        if (data.isEmpty()) {
            LOGGER.error("No CGM Data available");
            throw new IllegalArgumentException("No CGM Data available");
        }

        LocalDate today = LocalDate.now();
        LocalDate yesterday = today.minusDays(1);

        return data.stream()
                .filter(u -> u.getTime().before(convert(today)))
                .filter(u -> u.getTime().after(convert(yesterday)))
                .mapToDouble(DataPoint::getValue)
                .average()
                .getAsDouble();
    }

    /**
     * Get average glucose level for yesterday
     *
     * @param data List of {@Datapoint}
     * @return {@double} avergae glucose level
     */
    public static double getYesterdaysAvgLevel(List<DataPoint> data) {
        if (data.isEmpty()) {
            LOGGER.error("No CGM Data available");
            throw new IllegalArgumentException("No CGM Data available");
        }

        LocalDate yesterdayEnd = LocalDate.now().minusDays(1);
        LocalDate yesterdayStart = yesterdayEnd.minusDays(1);

        return data.stream()
                .filter(u -> u.getTime().before(convert(yesterdayEnd)))
                .filter(u -> u.getTime().after(convert(yesterdayStart)))
                .mapToDouble(DataPoint::getValue)
                .average()
                .getAsDouble();
    }

    private static Date convert(LocalDate localDate) {
        Instant instant = localDate.atStartOfDay().toInstant(ZoneOffset.UTC);
        return Date.from(instant);
    }
}
