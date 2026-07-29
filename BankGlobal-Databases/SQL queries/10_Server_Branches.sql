SELECT h.SerialNo AS ServerSerialNo, h.Model AS ServerModel, h.Type AS ServerType, b.ZipCode AS zipcode, b.BrNumber
FROM hardware h, branch b
WHERE b.ServerConnectedTo = h.SerialNo
AND h.Type = 'Server'
ORDER BY ServerModel ASC, BrNumber ASC;