SELECT h2.SerialNo AS ServerSerialNo, h2.Type AS ServerType, h2.Model AS ServerModel, h1.SerialNo AS hwSerialNo, h1.Type AS hwType, h1.Model AS hwModel
FROM hwconnectedtohw hct, hardware h1, hardware h2
WHERE hct.SerialNo_1 = h1.SerialNo AND hct.ConnectedToSerialNo_2 = h2.SerialNo
AND h2.Type = 'server'
ORDER BY h2.Model ASC, h1.Model ASC;
