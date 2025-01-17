package com.hibernate.NMS.himachal_NMS.repository;

import com.hibernate.NMS.himachal_NMS.model.HimachalDevice;
import com.hibernate.NMS.himachal_NMS.model.History;
import jakarta.persistence.Tuple;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.Optional;

public interface HistoryRepository extends JpaRepository<History,Long> {

    @Query(value = """
         SELECT DISTINCT ON (name) *
        FROM hpt
        ORDER BY name, timestamp DESC;
        """,nativeQuery = true)
          List<History> getLastStatus();


    @Query(value = """
            SELECT * FROM hpt
            WHERE LOWER(district) = LOWER(:district)
            AND LOWER(name) = LOWER(:deviceName)
            AND timestamp >= NOW() - (:days || ' days')::interval
            """, nativeQuery = true)
    List<History> getDataByDistrictAndDeviceNameOfDays(@Param("district") String district,
                                                              @Param("deviceName") String deviceName,
                                                              @Param("days") Integer days);



    boolean existsByDistrictAndName(String district,String name);
    @Query(value = """
    WITH time_intervals AS (
    SELECT
        status,
        EXTRACT(EPOCH FROM (COALESCE(LEAD(timestamp) OVER (ORDER BY timestamp), NOW()) - timestamp)) AS duration
    FROM hpt
    WHERE LOWER(name) = LOWER(:deviceName)
    AND district = :district
    AND timestamp >= DATE_TRUNC('day', NOW() - (:days || ' days')::interval)
)
SELECT
    COUNT(*) FILTER (WHERE status = 1) AS NoOfUp,
    COUNT(*) FILTER (WHERE status = 0) AS NoOfDown,
    COALESCE(SUM(duration) FILTER (WHERE status = 1), 0) AS TotalUpTime,
    COALESCE(SUM(duration) FILTER (WHERE status = 0), 0) AS TotalDownTime
FROM time_intervals;
            """, nativeQuery = true)
    Tuple getPerformanceDataByDeviceOfDays(@Param("district") String district,
                                           @Param("deviceName") String deviceName,
                                           @Param("days") Integer days);




    @Query(value = """
         SELECT DISTINCT ON (name) *
        FROM hpt
        ORDER BY name, timestamp DESC;
        """,nativeQuery = true)
    List<History> getLastDownStatusDevices();



//    @Query(value = """
//    WITH time_intervals AS (
//        SELECT
//            GENERATE_SERIES(
//                :startDate,
//                :endDate,
//                INTERVAL '1 day'
//            ) AS activity_date
//    ),
//    daily_status AS (
//        SELECT
//            t.activity_date,
//            h.status,
//            LEAST(
//                COALESCE(LEAD(h.timestamp) OVER (PARTITION BY h.name ORDER BY h.timestamp), NOW()),
//                t.activity_date + INTERVAL '1 day'
//            ) - GREATEST(h.timestamp, t.activity_date) AS duration
//        FROM time_intervals t
//        LEFT JOIN hpt h
//        ON h.timestamp >= t.activity_date
//           AND h.timestamp < t.activity_date + INTERVAL '1 day'
//           AND LOWER(h.name) = LOWER(:deviceName)
//           AND h.district = :district
//    ),
//    aggregated_hours AS (
//        SELECT
//            t.activity_date,
//            ROUND(SUM(EXTRACT(EPOCH FROM duration) / 3600) FILTER (WHERE status = 1), 2) AS total_active_hours
//        FROM time_intervals t
//        LEFT JOIN daily_status d
//        ON t.activity_date = d.activity_date
//        GROUP BY t.activity_date
//    )
//    SELECT
//        activity_date AT TIME ZONE 'Asia/Kolkata' AS activity_date_ist,
//        COALESCE(total_active_hours,
//                 CASE
//                    WHEN activity_date = DATE_TRUNC('day', NOW() AT TIME ZONE 'Asia/Kolkata') THEN ROUND(EXTRACT(EPOCH FROM (NOW() - GREATEST((SELECT MAX(timestamp) FROM hpt WHERE LOWER(name) = LOWER(:deviceName) AND district = :district AND timestamp <= NOW()), activity_date))) / 3600, 2)
//                    ELSE 0
//                 END
//        ) AS total_active_hours
//    FROM aggregated_hours
//    ORDER BY activity_date_ist
//    """, nativeQuery = true)
//    List<Tuple> getDeviceMonthlyData(
//            @Param("district") String district,
//            @Param("deviceName") String deviceName,
//            @Param("startDate") Date startDate,
//            @Param("endDate") Date endDate
//    );

    @Query(value = """
            WITH time_intervals AS (
                SELECT
                    GENERATE_SERIES(
                       CAST(:startDate AS TIMESTAMP) AT TIME ZONE 'Asia/Kolkata',
                   CAST(:endDate AS TIMESTAMP) AT TIME ZONE 'Asia/Kolkata',
                        INTERVAL '1 day'
                    ) AS activity_date
            ),
            daily_status AS (
                SELECT
                    t.activity_date,
                    h.status,
                    LEAST(
                        COALESCE(LEAD(h.timestamp) OVER (PARTITION BY h.name ORDER BY h.timestamp), t.activity_date + INTERVAL '1 day'),
                        t.activity_date + INTERVAL '1 day'
                    ) - GREATEST(h.timestamp, t.activity_date) AS duration
                FROM time_intervals t
                LEFT JOIN hpt h
                ON h.timestamp >= t.activity_date
                   AND h.timestamp < t.activity_date + INTERVAL '1 day'
                   AND LOWER(h.name) = LOWER(:deviceName)
                   AND h.district = :district
            ),
            first_log_of_day AS (
                SELECT
                    t.activity_date,
                    h.status AS first_status,
                    h.timestamp AS first_timestamp
                FROM time_intervals t
                LEFT JOIN LATERAL (
                    SELECT *
                    FROM hpt h
                    WHERE h.timestamp >= t.activity_date
                      AND h.timestamp < t.activity_date + INTERVAL '1 day'
                      AND LOWER(h.name) = LOWER(:deviceName)
                      AND h.district = :district
                    ORDER BY h.timestamp ASC
                    LIMIT 1
                ) h ON TRUE
            ),
            carry_forward_status AS (
                SELECT
                    t.activity_date,
                    COALESCE(
                        (SELECT h.status
                         FROM hpt h
                         WHERE h.timestamp <= t.activity_date + INTERVAL '1 day'
                           AND LOWER(h.name) = LOWER(:deviceName)
                           AND h.district = :district
                         ORDER BY h.timestamp DESC
                         LIMIT 1),
                        0
                    ) AS carry_forward_status
                FROM time_intervals t
            ),
            aggregated_hours AS (
                SELECT
                    t.activity_date,
                    ROUND(SUM(EXTRACT(EPOCH FROM duration) / 3600) FILTER (WHERE d.status = 1), 2) AS total_active_hours
                FROM time_intervals t
                LEFT JOIN daily_status d
                ON t.activity_date = d.activity_date
                GROUP BY t.activity_date
            )
            SELECT
                activity_date AT TIME ZONE 'Asia/Kolkata' AS activity_date_ist,
                COALESCE(
                    total_active_hours,
                    CASE
                        WHEN (SELECT carry_forward_status FROM carry_forward_status c WHERE c.activity_date = aggregated_hours.activity_date) = 1 THEN 24
                        ELSE ROUND(
                            CASE
                                WHEN (SELECT first_status FROM first_log_of_day f WHERE f.activity_date = aggregated_hours.activity_date) = 0 THEN
                                    EXTRACT(EPOCH FROM ((SELECT first_timestamp FROM first_log_of_day f WHERE f.activity_date = aggregated_hours.activity_date) - aggregated_hours.activity_date)) / 3600
                                ELSE 0
                            END, 2)
                    END
                ) AS total_active_hours
            FROM aggregated_hours
            ORDER BY activity_date_ist;
      
            """,nativeQuery = true)
    List<Object[]> getDeviceMonthlyData(
            @Param("district") String district,
            @Param("deviceName") String deviceName,
            @Param("startDate") String startDate,
            @Param("endDate") String endDate);

//

    @Query(value = """
            WITH time_intervals AS (
                SELECT
                    GENERATE_SERIES(
                        CAST(:startDate AS TIMESTAMP) AT TIME ZONE 'Asia/Kolkata',
                   CAST(:endDate AS TIMESTAMP) AT TIME ZONE 'Asia/Kolkata',
                        INTERVAL '1 day'
                    ) AS activity_date
            ),
            daily_status AS (
                SELECT
                    t.activity_date,
                    h.status,
                    LEAST(
                        COALESCE(LEAD(h.timestamp) OVER (PARTITION BY h.name ORDER BY h.timestamp), t.activity_date + INTERVAL '1 day'),
                        t.activity_date + INTERVAL '1 day'
                    ) - GREATEST(h.timestamp, t.activity_date) AS duration
                FROM time_intervals t
                LEFT JOIN hpt h
                ON h.timestamp >= t.activity_date
                   AND h.timestamp < t.activity_date + INTERVAL '1 day'
                   AND LOWER(h.name) = LOWER(:deviceName)
                   AND h.district = :district
            ),
            first_log_of_day AS (
                SELECT
                    t.activity_date,
                    h.status AS first_status,
                    h.timestamp AS first_timestamp
                FROM time_intervals t
                LEFT JOIN LATERAL (
                    SELECT *
                    FROM hpt h
                    WHERE h.timestamp >= t.activity_date
                      AND h.timestamp < t.activity_date + INTERVAL '1 day'
                      AND LOWER(h.name) = LOWER(:deviceName)
                      AND h.district = :district
                    ORDER BY h.timestamp ASC
                    LIMIT 1
                ) h ON TRUE
            ),
            carry_forward_status AS (
                SELECT
                    t.activity_date,
                    COALESCE(
                        (SELECT h.status
                         FROM hpt h
                         WHERE h.timestamp < t.activity_date
                           AND LOWER(h.name) = LOWER(:deviceName)
                           AND h.district = :district
                         ORDER BY h.timestamp DESC
                         LIMIT 1),
                        0
                    ) AS carry_forward_status
                FROM time_intervals t
            ),
            aggregated_hours AS (
                SELECT
                    t.activity_date,
                    ROUND(SUM(EXTRACT(EPOCH FROM duration) / 3600) FILTER (WHERE d.status = 1), 2) AS total_active_hours
                FROM time_intervals t
                LEFT JOIN daily_status d
                ON t.activity_date = d.activity_date
                GROUP BY t.activity_date
            ),
            adjusted_hours AS (
                SELECT
                    a.activity_date,
                    ROUND(
                        COALESCE(a.total_active_hours, 0) +
                        CASE
                            WHEN f.first_status = 0 THEN
                                EXTRACT(EPOCH FROM (f.first_timestamp - a.activity_date)) / 3600
                            ELSE 0
                        END, 2
                    ) AS adjusted_active_hours
                FROM aggregated_hours a
                LEFT JOIN first_log_of_day f
                ON a.activity_date = f.activity_date
            )
            SELECT
                t.activity_date AT TIME ZONE 'Asia/Kolkata' AS activity_date_ist,
                COALESCE(
                    (SELECT adjusted_active_hours FROM adjusted_hours a WHERE a.activity_date = t.activity_date),
                    CASE
                        WHEN (SELECT carry_forward_status FROM carry_forward_status c WHERE c.activity_date = t.activity_date) = 1 THEN 24
                        ELSE 0
                    END
                ) AS total_active_hours
            FROM time_intervals t
            ORDER BY activity_date_ist;
            """,nativeQuery = true)
    List<Object[]> getDeviceOrignalMonthlyData(
            @Param("district") String district,
            @Param("deviceName") String deviceName,
            @Param("startDate") String startDate,
            @Param("endDate") String endDate);

    @Query(value = "SELECT h.status AS carry_forward_status, h.timestamp AS last_log_timestamp " +
            "FROM hpt h " +
            "WHERE LOWER(h.name) = LOWER(:deviceName) " +
            "AND h.district = :district " +
            "AND h.timestamp >= NOW() - (:days * INTERVAL '1 day') " +
            "ORDER BY h.timestamp " +
            "LIMIT 1", nativeQuery = true)
    Tuple getFirstLogAfterDate(
            @Param("deviceName") String deviceName,
            @Param("district") String district,
            @Param("days") Integer days);


    @Query(value = "SELECT h.status AS carry_forward_status, h.timestamp AS last_log_timestamp " +
            "FROM hpt h " +
            "WHERE LOWER(h.name) = LOWER(:deviceName) " +
            "AND h.district = :district " +
            "AND h.timestamp < NOW() - (:days * INTERVAL '1 day') " +
            "ORDER BY h.timestamp DESC " +
            "LIMIT 1", nativeQuery = true)
    Tuple getFirstLogBeforeDate(
            @Param("deviceName") String deviceName,
            @Param("district") String district,
            @Param("days") Integer days);




}
