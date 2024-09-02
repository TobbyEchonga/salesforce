package com.tobby.echonga.salesforce.controllers;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;

import com.tobby.echonga.salesforce.models.SalesRecordModel;
import com.tobby.echonga.salesforce.services.SalesRecordService;

@Controller
@RequestMapping("/sales")
public class SalesDataController {

    @Autowired
    private SalesRecordService salesDataService;

    // Display upload page
    @GetMapping("/upload")
    public String showUploadPage() {
        return "upload";
    }

    // Handle file upload
    @PostMapping("/upload-data")
    public ModelAndView uploadCsv(@RequestParam("file") MultipartFile file) {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream()))) {
            List<SalesRecordModel> data = reader.lines()
                    .skip(1) // Skip header
                    .map(line -> {
                        String[] fields = line.split(",");
                        SalesRecordModel salesData = new SalesRecordModel();
                        salesData.setRegion(fields[0]);
                        salesData.setCountry(fields[1]);
                        salesData.setItemType(fields[2]);
                        salesData.setSalesChannel(fields[3]);
                        salesData.setOrderPriority(fields[4]);
                        salesData.setOrderDate(LocalDate.parse(fields[5], DateTimeFormatter.ofPattern("M/d/yyyy")));
                        salesData.setOrderId(fields[6]);
                        salesData.setShipDate(LocalDate.parse(fields[7], DateTimeFormatter.ofPattern("M/d/yyyy")));
                        salesData.setUnitsSold(Integer.parseInt(fields[8]));
                        salesData.setUnitPrice(Double.parseDouble(fields[9]));
                        salesData.setUnitCost(Double.parseDouble(fields[10]));
                        salesData.setTotalRevenue(Double.parseDouble(fields[11]));
                        salesData.setTotalCost(Double.parseDouble(fields[12]));
                        salesData.setTotalProfit(Double.parseDouble(fields[13]));
                        return salesData;
                    })
                    .collect(Collectors.toList());

            salesDataService.saveSalesRecordModel(data);

        } catch (Exception e) {
            e.printStackTrace();
        }

        // Redirect to the dashboard
        return new ModelAndView("redirect:/sales/dashboard");
    }

    @GetMapping("/dashboard")
    public ModelAndView showDashboard(@RequestParam(value = "startDate", required = false) LocalDate startDate,
            @RequestParam(value = "endDate", required = false) LocalDate endDate) {

        ModelAndView modelAndView = new ModelAndView("dashboard");

        LocalDate earliestDate = salesDataService.getEarliestRecordDate();

        if (startDate != null && endDate != null) {
            if (startDate.isBefore(earliestDate)) {
                modelAndView.addObject("error_msg", "Start date cannot be earlier than the earliest record date.");
            } else if (!endDate.isAfter(startDate)) {
                modelAndView.addObject("error_msg", "End date must be after the start date.");
            } else {
                // Process valid dates and add results to model
                String totalProfit = salesDataService.calculateTotalProfit(startDate, endDate);
                List<Map.Entry<String, String>> top5ItemTypes = salesDataService.getTop5MostProfitableItemTypes();
                modelAndView.addObject("range", "Total Profit for the range from " + startDate + " -> " + endDate);
                modelAndView.addObject("totalProfit", totalProfit);
                modelAndView.addObject("top5ItemTypes", top5ItemTypes);
                modelAndView.addObject("earliestRecordDate", earliestDate);
            }
        } else {
            // Handle case where dates are not provided
            modelAndView.addObject("earliestRecordDate", earliestDate);
            modelAndView.addObject("range", "Total Profit for the range");
        }

        return modelAndView;
    }

}