USE FileStorageDB;
GO

-- Create the table matching the XmlPayloadRecord JPA Entity
IF OBJECT_ID(N'dbo.xml_payload_records', N'U') IS NULL
BEGIN
CREATE TABLE dbo.xml_payload_records (
     id BIGINT IDENTITY(1,1) NOT NULL,
     file_name NVARCHAR(255) NULL,
     request_payload NVARCHAR(MAX) NOT NULL,
     response_payload NVARCHAR(MAX) NULL,
     status NVARCHAR(50) NULL,
     created_at DATETIME2(6) NOT NULL DEFAULT SYSDATETIME(),
     CONSTRAINT PK_xml_payload_records PRIMARY KEY CLUSTERED (id ASC)
);
END
GO