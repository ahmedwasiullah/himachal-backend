package com.hibernate.NMS.himachal_NMS.repository;

import com.hibernate.NMS.himachal_NMS.model.HimachalDevice;
import jakarta.persistence.Tuple;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface HimachalDeviceRepository extends JpaRepository<HimachalDevice, Integer> {

//    @Query(value = """
//                  WITH latest_status AS (
//                      SELECT
//                          ip,
//                          status,
//                          district,
//                          ROW_NUMBER() OVER (PARTITION BY ip ORDER BY timestamp DESC) AS rn
//                      FROM himachal
//                  )
//                  SELECT
//                      district,
//                      COUNT(CASE WHEN status = 'UP' THEN 1 END) AS active_devices,
//                      COUNT(CASE WHEN status = 'DOWN' THEN 1 END) AS inactive_devices
//                  FROM latest_status
//                  WHERE rn = 1
//                  group by district
//            """, nativeQuery = true)
@Query(value = """
  SELECT
  'VM' AS group_name,
  COUNT(CASE WHEN status = '1' THEN 1 END) AS active_devices,
  COUNT(CASE WHEN status = '0' THEN 1 END) AS inactive_devices
FROM hp
WHERE name ILIKE '% VM'

UNION ALL

SELECT
  substring(name FROM '^[^ ]+') AS group_name,
  COUNT(CASE WHEN status = '1' THEN 1 END) AS active_devices,
  COUNT(CASE WHEN status = '0' THEN 1 END) AS inactive_devices
FROM
    hp
WHERE
    name NOT ILIKE '% VM'
GROUP BY
  group_name
ORDER BY
  group_name;
            """,nativeQuery = true)
    List<Tuple> getAllData();

//    @Query(value = """
//                WITH latest_status AS (
//                    SELECT
//                        ip,
//                        status,
//                        ROW_NUMBER() OVER (PARTITION BY ip ORDER BY timestamp DESC) AS rn
//                    FROM himachal
//                    WHERE LOWER(district) = LOWER(:district)
//                )
//                SELECT
//                    COUNT(CASE WHEN status = 'UP' THEN 1 END) AS active_devices,
//                    COUNT(CASE WHEN status = 'DOWN' THEN 1 END) AS inactive_devices
//                FROM latest_status
//                WHERE rn = 1
//            """, nativeQuery = true)

    @Query(value = """
            SELECT *
            FROM hp
            WHERE name ILIKE %:district%;
            """, nativeQuery = true)
    List<HimachalDevice> getDistrictDataAndLogs(@Param("district") String district);


    @Query(value = """
            SELECT *
            FROM hp
            WHERE name ILIKE '% VM'
            """, nativeQuery = true)
    List<HimachalDevice> getVMDataAndLogs();


    @Query(value = """
         SELECT DISTINCT ON (name) *
        FROM hp
        ORDER BY name, timestamp DESC;
        """,nativeQuery = true)
    List<HimachalDevice> getLastStatus();


//    @Query(value = """
//        SELECT DISTINCT ON (name) *
//        FROM himachal
//        WHERE district = :district
//        ORDER BY name, timestamp DESC;
//    """, nativeQuery = true)
//    List<HimachalDevice> getAllDistrictLogs(@Param("district") String district);


//    @Query(value = """
//            SELECT * FROM himachal
//            WHERE LOWER(district) = LOWER(:district)
//            AND LOWER(name) = LOWER(:deviceName)
//            AND timestamp <= CURRENT_DATE
//            AND timestamp >= CURRENT_DATE - (:days || ' days')::interval
//            """, nativeQuery = true)
//    List<HimachalDevice> getDataByDistrictAndDeviceNameOfDays(@Param("district") String district,
//                                                              @Param("deviceName") String deviceName,
//                                                              @Param("days") Integer days);
//
//
//
//    boolean existsByDistrictAndName(String district,String name);
//
//    @Query(value = """
//    WITH time_intervals AS (
//    SELECT
//        status,
//        EXTRACT(EPOCH FROM (COALESCE(LEAD(timestamp) OVER (ORDER BY timestamp), NOW()) - timestamp)) AS duration
//    FROM himachal
//    WHERE LOWER(name) = LOWER(:deviceName)
//    AND district=:district
//    AND timestamp <= CURRENT_DATE
//    AND timestamp >= CURRENT_DATE - (:days || ' days')::interval
//)
//SELECT
//    COUNT(*) FILTER (WHERE status = 'UP') AS NoOfUp,
//    COUNT(*) FILTER (WHERE status = 'DOWN') AS NoOfDown,
//    COALESCE(SUM(duration) FILTER (WHERE status = 'UP'), 0) AS TotalUpTime,
//    COALESCE(SUM(duration) FILTER (WHERE status = 'DOWN'), 0) AS TotalDownTime
//FROM time_intervals;
//            """,nativeQuery = true)
//    Tuple getPerformanceDataByDeviceOfDays(@Param("district") String district,
//                                           @Param("deviceName") String deviceName,
//                                           @Param("days") Integer days);
//

}