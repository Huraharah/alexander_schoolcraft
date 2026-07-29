DROP SCHEMA bankglobal_project;
CREATE SCHEMA bankglobal_project;
USE bankglobal_project;

CREATE TABLE Software
(
  SwName VARCHAR(25) NOT NULL,
  SwVerNo INT NOT NULL,
  SwSellerName VARCHAR(25) NOT NULL,
  SwDatePurchaced DATE NOT NULL,
  PurOrLic CHAR(1) NOT NULL,
  SwExpireDate DATE NOT NULL,
  SwPrice NUMERIC(10,2) NOT NULL,
  SwID VARCHAR(50) NOT NULL,
  PRIMARY KEY (SwID)
);

CREATE TABLE Manager
(
  MgrName VARCHAR(25) NOT NULL,
  MgrID INT NOT NULL,
  PRIMARY KEY (MgrID)
);

CREATE TABLE Hardware
(
  SerialNo VARCHAR(50) NOT NULL,
  Type VARCHAR(25) NOT NULL,
  Model INT NOT NULL,
  PurchaseDate DATE NOT NULL,
  WarrDurration INT NOT NULL,
  Price NUMERIC(10,2) NOT NULL,
  BrLocation INT NOT NULL,
  PRIMARY KEY (SerialNo)#,
  #FOREIGN KEY (BrLocation) REFERENCES BRANCH(BrNumber)
);

CREATE TABLE BRANCH
(
  BrNumber INT NOT NULL,
  BrName VARCHAR(50) NOT NULL,
  TelNo VARCHAR(25) NOT NULL,
  Street VARCHAR(25) NOT NULL,
  City VARCHAR(25) NOT NULL,
  State VARCHAR(02) NOT NULL,
  ZipCode VARCHAR(10) NOT NULL,
  ServerConnectedTo VARCHAR(50) NOT NULL,
  MgrID INT NOT NULL,
  PRIMARY KEY (BrNumber),
  FOREIGN KEY (ServerConnectedTo) REFERENCES Hardware(SerialNo),
  FOREIGN KEY (MgrID) REFERENCES Manager(MgrID)
);

CREATE TABLE SwInstalledOnHw
(
  SwID VARCHAR(50) NOT NULL,
  SerialNo VARCHAR(50) NOT NULL,
  PRIMARY KEY (SwID, SerialNo),
  FOREIGN KEY (SwID) REFERENCES Software(SwID),
  FOREIGN KEY (SerialNo) REFERENCES Hardware(SerialNo)
);

CREATE TABLE HwConnectedToHw
(
  SerialNo_1 VARCHAR(50) NOT NULL,
  ConnectedToSerialNo_2 VARCHAR(50) NOT NULL,
  PRIMARY KEY (SerialNo_1, ConnectedToSerialNo_2),
  FOREIGN KEY (SerialNo_1) REFERENCES Hardware(SerialNo),
  FOREIGN KEY (ConnectedToSerialNo_2) REFERENCES Hardware(SerialNo)
);

ALTER TABLE hardware 
ADD CONSTRAINT fk
FOREIGN KEY (BrLocation)
REFERENCES BRANCH(BrNumber);