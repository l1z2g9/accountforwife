DROP TABLE IF EXISTS Item;
CREATE TABLE Item(
id INTEGER PRIMARY KEY AUTOINCREMENT,
title VARCHAR(200) NULL,
time INTEGER NOT NULL,
money FLOAT NOT NULL,
categoryID INTEGER NOT NULL REFERENCES Category(id),
remark TEXT NULL,
user VARCHAR(20) NULL,
address VARCHAR(200) NULL
);

DROP TABLE IF EXISTS Category;
CREATE TABLE Category(
id INTEGER PRIMARY KEY,
parentID INTEGER NULL REFERENCES Category(id),
type VARCHAR(20) NOT NULL Check (type = 'Expenditure' OR Type = 'Income'),
name VARCHAR(50) NOT NULL,
displayOrder INTEGER NOT NULL DEFAULT 0
);

DROP TABLE IF EXISTS Budget;
CREATE TABLE Budget(
id INTEGER PRIMARY KEY AUTOINCREMENT,
categoryID INTEGER NOT NULL REFERENCES Category(ID),
year INTEGER NOT NULL,
month INTEGER NOT NULL,
money FLOAT NOT NULL
);

DROP TABLE IF EXISTS Overdraw;
CREATE TABLE Overdraw(
id INTEGER PRIMARY KEY AUTOINCREMENT,
time INTEGER NOT NULL,
money FLOAT NOT NULL,
remark TEXT NULL,
address VARCHAR(200) NULL,
returnTime INTEGER NULL,
returnMoney FLOAT NULL,
returnRemark TEXT NULL
--completed INTEGER NOT NULL Check (completed = 1 OR completed = 0) DEFAULT 0
--balance Float (returnMoney -money )
);

DROP INDEX IF EXISTS Item_idx;
CREATE INDEX Item_idx ON Item (time);

DROP INDEX IF EXISTS Budget_idx;
CREATE INDEX Budget_idx ON Budget (Year, Month);

-- 修改结构
ALTER TABLE Overdraw
ADD completed INTEGER NOT NULL Check (completed = 1 OR completed = 0) DEFAULT 0;
