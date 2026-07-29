SELECT SerialNo AS HwSerialNo, Type AS HwType, '2010-01-01' AS TargetDate, PurchaseDate AS HwsPurchaseDate, WarrDurration, DATE_ADD(PurchaseDate, INTERVAL WarrDurration YEAR) AS WarEndDate
FROM hardware
WHERE DATE_ADD(PurchaseDate, INTERVAL WarrDurration YEAR) <= '2010-01-01';

SELECT SerialNo AS HwSerialNo, Type AS HwType, '2015-01-01' AS TargetDate, PurchaseDate AS HwsPurchaseDate, WarrDurration, DATE_ADD(PurchaseDate, INTERVAL WarrDurration YEAR) AS WarEndDate
FROM hardware
WHERE DATE_ADD(PurchaseDate, INTERVAL WarrDurration YEAR) <= '2015-01-01';
