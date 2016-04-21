/*
 * Copyright (C) Schweizerische Bundesbahnen SBB, 2016.
 */

package de.qfotografie;

import de.qfotografie.akm.DataPoint;
import de.qfotografie.rest.TidepoolController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

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
                .sorted((DataPoint o1, DataPoint o2) -> o2.getTime().compareTo(o1.getTime()))
                .collect(Collectors.toList());
    }

    /**
     * Get average glucose level for last X days
     *
     * @param days Amount of days to look back
     * @param data List of {@Datapoint}
     * @return {@double} avergae glucose level
     */
    public static double getAvgLevel(int days, List<DataPoint> data) {
        if (data.isEmpty()) {
            LOGGER.error("No CGM Data available");
            throw new IllegalArgumentException("No CGM Data available");
        }

        LocalDate today = LocalDate.now();
        LocalDate yesterday = today.minusDays(days);

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

    /**
     * Get glucose level for a specific hour range in the last X days
     */
    public static double getAverageBetweenTimeslotsIn(int hour, int amountOfDays, List<DataPoint> data) {
        if (data.isEmpty()) {
            LOGGER.error("No CGM Data available");
            throw new IllegalArgumentException("No CGM Data available");
        }

        LocalDate today = LocalDate.now();
        LocalDate startDate = today.minusDays(amountOfDays);

        List<DataPoint> filteredByDay;
        if (amountOfDays == 0) {
            filteredByDay = data.stream()
                    .filter(u -> u.getTime().after(convert(today)))
                    .collect(Collectors.toList());
        } else {
            filteredByDay = data.stream()
                    .filter(u -> u.getTime().before(convert(today)))
                    .filter(u -> u.getTime().after(convert(startDate)))
                    .collect(Collectors.toList());
        }

        if (filteredByDay.isEmpty()) {
            return 0;
        }

        List<DataPoint> resultList = new ArrayList<>();

        for (DataPoint dataPoint : filteredByDay) {
            Instant instant = Instant.ofEpochMilli(dataPoint.getTime().getTime());
            LocalTime localTime = LocalDateTime.ofInstant(instant, ZoneId.systemDefault()).toLocalTime();
            if (localTime.getHour() == hour) {
                resultList.add(dataPoint);
            }
        }
        if (resultList.isEmpty()) {
            return 0;
        }

        return resultList.stream().mapToDouble(DataPoint::getValue)
                .average()
                .getAsDouble();

    }

    private static Date convert(LocalDate localDate) {
        Instant instant = localDate.atStartOfDay().toInstant(ZoneOffset.UTC);
        return Date.from(instant);
    }
}
