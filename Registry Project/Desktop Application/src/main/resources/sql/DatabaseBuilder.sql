/**@application: UniversalGiftRegistry
 * @author: Alexander Schoolcraft, Benjamin King, Brandon King, Gabe Woolums
 * @date: 3/21/2024
 * @version: 3.0
 */
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[User](
	[Email] [nvarchar](255) NOT NULL,
	[Street_Address] [nvarchar](255) NOT NULL,
	[City] [nvarchar](25) NOT NULL,
	[State] [nchar](2) NOT NULL,
	[Zip] [numeric](5, 0) NOT NULL,
	[First_Name] [nvarchar](50) NOT NULL,
	[Last_Name] [nvarchar](50) NOT NULL,
	[Password] [nvarchar](128) NOT NULL,
	[Salt] [nvarchar](32) NOT NULL,
	[SecurityQuestion1Code] [nvarchar](5) NOT NULL,
	[SecurityAnswer1] [nvarchar](128) NOT NULL,
	[SecurityQuestion2Code] [nvarchar](5) NOT NULL,
	[SecurityAnswer2] [nvarchar](128) NOT NULL
) ON [PRIMARY]
GO
SET ANSI_PADDING ON
GO
ALTER TABLE [dbo].[User] ADD PRIMARY KEY CLUSTERED 
(
	[Email] ASC
)WITH (STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ONLINE = OFF, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
GO
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[List](
	[Occasion] [nchar](4) NOT NULL,
	[ListName] [nvarchar](25) NOT NULL,
	[ListID] [int] IDENTITY(1,1) NOT NULL,
	[Email] [nvarchar](255) NOT NULL
) ON [PRIMARY]
GO
ALTER TABLE [dbo].[List] ADD PRIMARY KEY CLUSTERED 
(
	[ListID] ASC
)WITH (STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ONLINE = OFF, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
GO
ALTER TABLE [dbo].[List]  WITH CHECK ADD FOREIGN KEY([Email])
REFERENCES [dbo].[User] ([Email])
GO
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[Item](
	[ItemURL] [nvarchar](2048) NULL,
	[ItemImage] [nvarchar](2048) NULL,
	[ItemName] [nvarchar](255) NOT NULL,
	[ItemSize] [nvarchar](25) NULL,
	[ItemColor] [nvarchar](25) NULL,
	[ItemPrice] [numeric](7, 2) NOT NULL,
	[ItemID] [int] IDENTITY(1,1) NOT NULL,
	[Purchased] [bit] NOT NULL,
	[Email] [nvarchar](255) NOT NULL,
	[ListID] [int] NOT NULL
) ON [PRIMARY]
GO
ALTER TABLE [dbo].[Item] ADD PRIMARY KEY CLUSTERED 
(
	[ItemID] ASC
)WITH (STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ONLINE = OFF, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
GO
ALTER TABLE [dbo].[Item]  WITH CHECK ADD FOREIGN KEY([Email])
REFERENCES [dbo].[User] ([Email])
GO
ALTER TABLE [dbo].[Item]  WITH CHECK ADD FOREIGN KEY([ListID])
REFERENCES [dbo].[List] ([ListID])
GO
