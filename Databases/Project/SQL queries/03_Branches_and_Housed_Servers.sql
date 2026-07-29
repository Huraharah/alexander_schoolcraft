SELECT b.BrNumber, b.ZipCode, h.Type as HwType, h.Model as ServerModel
FROM branch b, hardware h
WHERE h.BrLocation = b.BrNumber
AND h.Type = 'server';