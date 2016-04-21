/*
 * Copyright (C) Schweizerische Bundesbahnen SBB, 2016.
 */

package de.qfotografie;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import de.qfotografie.akm.DataPoint;
import de.qfotografie.REST.TidepoolController;

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
            model.addAttribute("avgToday", DataAnalysis.getTodayAvgLevel(dataPointList));
            model.addAttribute("avgYesterday", DataAnalysis.getYesterdaysAvgLevel(dataPointList));
        }

        return "home";
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
}
