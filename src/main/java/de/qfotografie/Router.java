/*
 * Copyright (C) Schweizerische Bundesbahnen SBB, 2016.
 */

package de.qfotografie;

import de.qfotografie.akm.DataPoint;
import de.qfotografie.rest.TidepoolController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.List;

@SpringBootApplication
@Controller
@EnableCaching
public class Router {

    @Autowired
    private TidepoolController tidepoolController;

    @RequestMapping(value = "/", method = RequestMethod.GET)
    public String home(Model model) {

        List<DataPoint> dataPointList = tidepoolController.getDataPoints();
        dataPointList = DataAnalysis.getCGMData(dataPointList);

        if (!dataPointList.isEmpty()) {
            model.addAttribute("avgToday", DataAnalysis.getAvgLevel(1, dataPointList));
            model.addAttribute("avgYesterday", DataAnalysis.getYesterdaysAvgLevel(dataPointList));
            model.addAttribute("avgLastThree", DataAnalysis.getAvgLevel(3, dataPointList));
            setDetailHourStats(3, model, dataPointList);
        }

        return "home";
    }

    @RequestMapping(value = "/analysis", method = RequestMethod.GET)
    public String analysis(Model model) {

        List<DataPoint> dataPointList = tidepoolController.getDataPoints();
        dataPointList = DataAnalysis.getCGMData(dataPointList);

        if (!dataPointList.isEmpty()) {
            setDetailHourStats(3, model, dataPointList);
        }

        return "analysis";
    }

    @RequestMapping(value = "/analysis-today", method = RequestMethod.GET)
    public String analysisToday(Model model) {

        List<DataPoint> dataPointList = tidepoolController.getDataPoints();
        dataPointList = DataAnalysis.getCGMData(dataPointList);

        if (!dataPointList.isEmpty()) {
            setDetailHourStats(0, model, dataPointList);
        }

        return "analysis";
    }

    @RequestMapping(value = "/raw-data", method = RequestMethod.GET)
    public String rawData(Model model) {

        List<DataPoint> dataPointList = tidepoolController.getDataPoints();
        dataPointList = DataAnalysis.getCGMData(dataPointList);

        if (!dataPointList.isEmpty()) {
            model.addAttribute("gcmList", dataPointList);
        }

        return "raw-data";
    }

    private void setDetailHourStats(int days, Model model, List<DataPoint> dataPointList) {
        for (int i = 0; i < 24; i++) {
            model.addAttribute("avgHour" + i, DataAnalysis.getAverageBetweenTimeslotsIn(i, days, dataPointList));
        }
    }
}
