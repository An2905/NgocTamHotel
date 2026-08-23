IF DB_ID(N'NgocTamHotel') IS NULL
BEGIN
    CREATE DATABASE NgocTamHotel;
END;
GO

USE NgocTamHotel;
GO

IF OBJECT_ID(N'dbo.users', N'U') IS NULL
BEGIN
    CREATE TABLE dbo.users (
        id UNIQUEIDENTIFIER NOT NULL
            CONSTRAINT pk_users PRIMARY KEY
            CONSTRAINT df_users_id DEFAULT NEWSEQUENTIALID(),
        username NVARCHAR(50) NOT NULL,
        password_hash NVARCHAR(100) NOT NULL,
        email NVARCHAR(255) NOT NULL,
        created_at DATETIME2(0) NOT NULL
            CONSTRAINT df_users_created_at DEFAULT SYSUTCDATETIME(),
        CONSTRAINT uq_users_username UNIQUE (username),
        CONSTRAINT uq_users_email UNIQUE (email)
    );
END;
GO

