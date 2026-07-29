DELIMITER //

CREATE PROCEDURE ImpactedBranches(IN TargetDate DATE)
BEGIN
    SELECT DISTINCT b.BrNumber, h.SerialNo AS HwSerialNo, sw.SwName, TargetDate, sw.SwExpireDate
    FROM branch b, hardware h, software sw, swinstalledonhw sih, hwconnectedtohw hch
    WHERE (b.BrNumber = h.BrLocation OR b.ServerConnectedTo = hch.ConnectedToSerialNo_2)
      AND h.SerialNo = sih.SerialNo
      AND sw.SwID = sih.SwID
      AND sw.SwExpireDate < TargetDate
    ORDER BY b.BrNumber ASC;
END //

DELIMITER ;

CALL ImpactedBranches("2023-01-01");
