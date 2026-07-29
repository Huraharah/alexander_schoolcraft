SELECT (SELECT COUNT(SerialNo) FROM hardware) + (SELECT COUNT(SwID) FROM software) as AssetCount, 
CONCAT('$', FORMAT((SELECT SUM(Price) FROM hardware) + (SELECT SUM(SwPrice) FROM software), 2)) as AssetsTotalPrice;
