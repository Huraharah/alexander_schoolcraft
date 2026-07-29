DROP PROCEDURE IF EXISTS ExpiredSoftware

DELIMITER //

CREATE PROCEDURE ExpiredSoftware(IN TargetDate DATE, IN GraceDays INT)
BEGIN
    SELECT SwName, SwPrice, 
           TargetDate, 
           SwExpireDate, 
           GraceDays, 
           DATEDIFF(TargetDate, SwExpireDate) AS DaysPassedExpiration
    FROM software
    WHERE SwExpireDate <= DATE_SUB(TargetDate, INTERVAL GraceDays DAY);
END //

DELIMITER ;

CALL ExpiredSoftware("2020-12-01", 90);

CALL ExpiredSoftware("2023-09-05", 60);
