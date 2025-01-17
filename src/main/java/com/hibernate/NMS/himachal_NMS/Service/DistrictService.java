package com.hibernate.NMS.himachal_NMS.Service;

import com.hibernate.NMS.himachal_NMS.dto.DayData;
import com.hibernate.NMS.himachal_NMS.dto.PerformanceDeviceData;
import com.hibernate.NMS.himachal_NMS.exceptions.ResourceNotFoundException;
import com.hibernate.NMS.himachal_NMS.model.History;
import com.hibernate.NMS.himachal_NMS.repository.HistoryRepository;
import jakarta.persistence.Tuple;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.time.*;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class DistrictService {

    private static final Logger log = LoggerFactory.getLogger(DistrictService.class);
    @Autowired
    private HistoryRepository historyRepository;

    public List<History> getDeviceDataLogs(String district, String deviceName, Integer days) throws ResourceNotFoundException{
              if(historyRepository.existsByDistrictAndName(district,deviceName)){
                  return historyRepository.getDataByDistrictAndDeviceNameOfDays(district,deviceName,days);
              }else{
                  throw new ResourceNotFoundException("Device not found");
              }
    }

    public PerformanceDeviceData getDevicePerformance(String district, String deviceName, Integer days) {
        try {
            Tuple tuple = historyRepository.getPerformanceDataByDeviceOfDays(district, deviceName, days);
            Tuple t1 = historyRepository.getFirstLogBeforeDate(deviceName, district, days);
            Tuple t2 = historyRepository.getFirstLogAfterDate(deviceName, district, days);
            PerformanceDeviceData data = new PerformanceDeviceData((Long) tuple.get(0), (Long) tuple.get(1), new BigDecimal(tuple.get(2).toString()), new BigDecimal(tuple.get(3).toString()));
            days--;


            if (t1 != null) {
                LocalTime currentTime = LocalTime.now();
                long secondsSinceMidnight = currentTime.toSecondOfDay();
                Short carryForwardStatus = t1.get(0, Short.class); // Handle SMALLINT as Short
                Timestamp lastLogTimestamp = t1.get(1, Timestamp.class);
                log.info(carryForwardStatus + " " + lastLogTimestamp.toString());
                if (t2 == null && carryForwardStatus == 1) {
                    data.setUpTime(new BigDecimal(days * 24d * 3600 + secondsSinceMidnight));
                } else if (t2 == null && carryForwardStatus == 0) {
                    data.setDownTime(new BigDecimal(days * 24d * 3600 + secondsSinceMidnight));
                }
            }

            if (t2 != null) {
                Short carryForwardStatust2 = t2.get(0, Short.class); // Handle SMALLINT as Short
                Timestamp lastLogTimestampt2 = t2.get(1, Timestamp.class);
                OffsetDateTime istEndDate = lastLogTimestampt2.toInstant().atOffset(ZoneOffset.UTC).atZoneSameInstant(ZoneId.of("Asia/Kolkata")).toOffsetDateTime();
                LocalDate today = LocalDate.now();
                LocalDate DaysAgo = today.minusDays(days);
                LocalDateTime DaysAgoAtMidnight = DaysAgo.atStartOfDay();
                Duration duration = Duration.between(DaysAgoAtMidnight, istEndDate);
                if (carryForwardStatust2 == 1) {
                    Double downHours = (Double) (duration.getSeconds() * 1d);
                    BigDecimal down = data.getDownTime();
                    down = down.add(new BigDecimal(downHours));
                    data.setDownTime(down);
                } else {
                    Double upHours = (Double) (duration.getSeconds() * 1d);
                    BigDecimal up = data.getUpTime();
                    up = up.add(new BigDecimal(upHours));
                    data.setUpTime(up);
                }

            }
            return data;
        }catch (Exception e){
            return new PerformanceDeviceData();
        }
    }



    public List<Map<String, Object>> getDeviceMonthlyData(String district, String deviceName, int year, int month) {
        if(historyRepository.existsByDistrictAndName(district,deviceName)){
            String[] dates = getStartAndEndOfMonth(year, month);
            List<Object[]> monthlyData = historyRepository.getDeviceMonthlyData(district, deviceName, dates[0], dates[1]);
            List<Object[]> monthlyDataOrignal = historyRepository.getDeviceOrignalMonthlyData(district, deviceName, dates[0], dates[1]);
            // Fetch log data
            for (int i = 0; i < monthlyData.size(); i++) {
                Object[] a = monthlyData.get(i);
                Object[] b = monthlyDataOrignal.get(i);

                if (((BigDecimal) b[1]).compareTo(BigDecimal.ZERO) == 0
                        && ((BigDecimal) a[1]).compareTo(BigDecimal.valueOf(24)) == 0) {
                    b[1] = BigDecimal.valueOf(24);
                }
            }
            return formatMonthlyData(monthlyDataOrignal);
        }else{
            throw new ResourceNotFoundException("Device not found");
        }
    }

    private List<Map<String, Object>> formatMonthlyData(List<Object[]> monthlyData) {
        return monthlyData.stream()
                .map(row -> {
                    OffsetDateTime utcDateTime = ((Timestamp) row[0]).toInstant().atOffset(ZoneOffset.UTC);
                    OffsetDateTime istDateTime = utcDateTime.atZoneSameInstant(ZoneId.of("Asia/Kolkata")).toOffsetDateTime();

                    Map<String, Object> resultMap = new HashMap<>();
                    resultMap.put("activity_date", istDateTime);
                    resultMap.put("total_active_hours", row[1]);
                    return resultMap;
                })
                .collect(Collectors.toList());
    }

    private void adjustActiveHoursForCarryForward(List<Map<String, Object>> results, Date startDate, Timestamp endTimestamp) {
        OffsetDateTime istStartDate = startDate.toInstant().atOffset(ZoneOffset.UTC).atZoneSameInstant(ZoneId.of("Asia/Kolkata")).toOffsetDateTime();
        OffsetDateTime istEndDate = endTimestamp.toInstant().atOffset(ZoneOffset.UTC).atZoneSameInstant(ZoneId.of("Asia/Kolkata")).toOffsetDateTime();

        int i = 0;
        while (istStartDate.plusDays(1).isBefore(istEndDate)) {
            Map<String, Object> resultMap = results.get(i);
            resultMap.put("total_active_hours", 24);
            istStartDate = istStartDate.plusDays(1);
            i++;
        }

        int hours = istEndDate.getHour();
        int minutes = istEndDate.getMinute();
        double activeHours = hours + ((minutes*100) / 60.0);
           log.info("last day hours to be added condition 1"+activeHours+" datetime "+istEndDate.toString());
        Map<String, Object> resultMap = results.get(i);
        double existingHours = (Double) resultMap.get("total_active_hours");
        resultMap.put("total_active_hours", existingHours + activeHours);
    }

    private void markAllRecordsAsActive(List<Map<String, Object>> results, String formattedDateEnd, Date endDate) {
        results.forEach(result -> result.put("total_active_hours", 24));


        // Adjust for the current day's partial hours if applicable
        LocalDateTime today = LocalDateTime.now();
        if (formattedDateEnd.equals(new SimpleDateFormat("yyyy-MM-dd").format(today))) {
            Map<String, Object> lastResult = results.get(results.size() - 1);
            int hours = today.getHour();
            int minutes = today.getMinute();
            double todayTime = hours + ((minutes*100) / 60.0);
            log.info("last day hours to be added condition 2 "+todayTime);
            lastResult.put("total_active_hours", todayTime);
        }
    }




    public String[] getStartAndEndOfMonth(int year, int month) {
        // Get the start of the month
        LocalDate startOfMonth = LocalDate.of(year, month, 1);
        LocalDate endOfMonth = startOfMonth.withDayOfMonth(startOfMonth.lengthOfMonth());

        Date startDate = Date.from(startOfMonth.atStartOfDay(ZoneId.systemDefault()).toInstant());
        Date endDate = Date.from(endOfMonth.atStartOfDay(ZoneId.systemDefault()).toInstant());
        Date now1 = new Date(System.currentTimeMillis());
        Date adjustedNow = Date.from(LocalDateTime.now()
                .minusDays(1)
                .atZone(ZoneId.systemDefault())
                .toInstant());
        endDate = endDate.before(now1) ? endDate : adjustedNow;
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
        String formattedDateStart = formatter.format(startDate);
        String formattedDateEnd = formatter.format(endDate);
        // Fetch monthly data and format results

        return new String[]{formattedDateStart, formattedDateEnd};
    }
}
