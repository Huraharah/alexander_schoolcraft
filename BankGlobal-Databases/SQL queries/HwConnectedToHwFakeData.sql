SET @rownum1 = 0;
CREATE TEMPORARY TABLE IF NOT EXISTS TempHardware1 AS
SELECT SerialNo, @rownum1 := @rownum1 + 1 AS rownum
FROM hardware;

SET @rownum2 = 0;
CREATE TEMPORARY TABLE IF NOT EXISTS TempHardware2 AS
SELECT SerialNo, @rownum2 := @rownum2 + 1 AS rownum
FROM hardware
WHERE type = 'server';

INSERT INTO hwconnectedtohw (SerialNo_1, ConnectedToSerialNo_2)
SELECT h1.SerialNo, h2.SerialNo
FROM TempHardware1 h1
JOIN TempHardware2 h2 ON h1.rownum = h2.rownum;

DROP TEMPORARY TABLE IF EXISTS TempHardware1;
DROP TEMPORARY TABLE IF EXISTS TempHardware2;
