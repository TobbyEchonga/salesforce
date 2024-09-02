package com.tobby.echonga.salesforce.services;

import org.springframework.stereotype.Service;

import com.tobby.echonga.salesforce.models.SalesRecordModel;

import java.text.NumberFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class SalesRecordService {

    private final List<SalesRecordModel> SalesRecordModelList = new ArrayList<>();

    // Return a copy of the list to avoid external modifications
    public List<SalesRecordModel> getAllData() {
        return new ArrayList<>(SalesRecordModelList);
    }

    // Returns earliest date
    public LocalDate getEarliestRecordDate() {
        List<SalesRecordModel> records = SalesRecordModelList;

        if (records.isEmpty()) {
            // Handle the case when there are no records
            return LocalDate.now(); // or any other default value
        }

        return records.stream()
                .map(SalesRecordModel::getOrderDate)
                .min(LocalDate::compareTo)
                .orElse(LocalDate.now()); // or any other default value
    }

    // Save data temporarily
    public void saveSalesRecordModel(List<SalesRecordModel> data) {
        SalesRecordModelList.clear(); // Clear existing data
        SalesRecordModelList.addAll(data);
    }

    public String calculateTotalProfit(LocalDate startDate, LocalDate endDate) {
        double sales = SalesRecordModelList.stream()
                .filter(data -> !data.getOrderDate().isBefore(startDate) && !data.getOrderDate().isAfter(endDate))
                .mapToDouble(SalesRecordModel::getTotalProfit)
                .sum();

        NumberFormat formatter = NumberFormat.getNumberInstance(Locale.US);
        return formatter.format(sales);
    }

    // Get top 5 most profitable item types
    public List<Map.Entry<String, String>> getTop5MostProfitableItemTypes() {
        NumberFormat formatter = NumberFormat.getNumberInstance(Locale.US);

        return SalesRecordModelList.stream()
                .collect(Collectors.groupingBy(SalesRecordModel::getItemType,
                        Collectors.summingDouble(SalesRecordModel::getTotalProfit)))
                .entrySet().stream()
                .sorted(Map.Entry.<String, Double>comparingByValue().reversed())
                .limit(5)
                .map(entry -> Map.entry(entry.getKey(), formatter.format(entry.getValue())))
                .collect(Collectors.toList());
    }
}
