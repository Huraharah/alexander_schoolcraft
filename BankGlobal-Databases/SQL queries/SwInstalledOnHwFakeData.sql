INSERT INTO SwInstalledOnHw (SwID, SerialNo)
SELECT s.SwID, h.SerialNo
FROM software s, hardware h;
