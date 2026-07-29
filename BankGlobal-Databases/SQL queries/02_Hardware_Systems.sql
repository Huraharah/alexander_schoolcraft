SELECT b.BrNumber as BranchNumber, b.ZipCode as BranchZipCode, h.Type as HousedHardwareType
FROM branch b, hardware h
WHERE h.BrLocation = b.BrNumber;