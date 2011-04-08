DROP TABLE IF EXISTS Account;
CREATE TABLE Item(
ID INTEGER PRIMARY KEY AUTOINCREMENT,
Title VARCHAR(200) NOT NULL,
Date DATETIME NOT NULL,
Money FLOAT NOT NULL,
CategoryID INT NOT NULL REFERENCES Category(ID),
Remark TEXT NULL,
User VARCHAR(20) NULL,
Address VARCHAR(200) NULL
);

DROP TABLE IF EXISTS Category;
CREATE TABLE Category(
ID INT PRIMARY KEY,
ParentID INT NULL REFERENCES Category(ID),
Type VARCHAR(20) NOT NULL Check ( Type = 'Expenditure' OR Type = 'Income'),
Name VARCHAR(50) NOT NULL
);

DROP TABLE IF EXISTS Budget;
CREATE TABLE Budget(
ID INTEGER PRIMARY KEY AUTOINCREMENT,
CategoryID INT NOT NULL REFERENCES Category(ID),
Year INT NOT NULL,
Month INT NOT NULL,
Color75 CHAR(7) NOT NULL default '#FFFF00',
Color90 CHAR(7) NOT NULL default '#FF0033',
Money FLOAT NOT NULL
);

DROP INDEX IF EXISTS Item_idx;
CREATE INDEX Item_idx ON Item (Date);

DROP INDEX IF EXISTS Budget_idx;
CREATE INDEX Budget_idx ON Budget (Year, Month);

-- 插入数据
INSERT INTO Category(ID, Type, Name)
VALUES(100, 'Income', '职业收入');
INSERT INTO Category
VALUES(101, 100, 'Income', '工资');
INSERT INTO Category
VALUES(102, 100, 'Income', '奖金');

INSERT INTO Category(ID, Type, Name)
VALUES(200, 'Expenditure', '休闲');
INSERT INTO Category
VALUES(201, 200, 'Expenditure', '电影');

INSERT INTO Category(ID, Type, Name)
VALUES(300, '食物类');
INSERT INTO Category
VALUES(301, 300, 'Expenditure', '蔬菜');
INSERT INTO Category
VALUES(302, 300, 'Expenditure', '荤菜');
INSERT INTO Category
VALUES(302, 300, 'Expenditure', '每日三餐');

INSERT INTO Category(ID, Type, Name)
VALUES(400, 'Expenditure', '住房类');
INSERT INTO Category
VALUES(401, 400, 'Expenditure', '物业管理费');
INSERT INTO Category
VALUES(402, 400, 'Expenditure', '房租水电');

INSERT INTO Category(ID, Type, Name)
VALUES(500, 'Expenditure', '服饰类');
INSERT INTO Category
VALUES(501, 500, 'Expenditure', '鞋子');
INSERT INTO Category
VALUES(502, 500, 'Expenditure', '裤子');
INSERT INTO Category
VALUES(503, 500, 'Expenditure', '衣服');

INSERT INTO Category(ID, Type, Name)
VALUES(600, 'Expenditure', '交通类');
INSERT INTO Category
VALUES(601, 600, 'Expenditure', '坐火车');
INSERT INTO Category
VALUES(602, 600, 'Expenditure', '坐公交');
