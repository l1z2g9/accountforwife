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
);

DROP INDEX IF EXISTS Item_idx;
CREATE INDEX Item_idx ON Item (time);

DROP INDEX IF EXISTS Budget_idx;
CREATE INDEX Budget_idx ON Budget (Year, Month);

-- 插入数据
INSERT INTO Category(id, type, name)
VALUES(100, 'Income', '职业收入');
INSERT INTO Category
VALUES(101, 100, 'Income', '工资', 0);
INSERT INTO Category
VALUES(102, 100, 'Income', '奖金', 0);

INSERT INTO Category(id, type, name)
VALUES(200, 'Expenditure', '休闲');
INSERT INTO Category
VALUES(201, 200, 'Expenditure', '电影', 0);

INSERT INTO Category(id, type, name)
VALUES(300, 'Expenditure', '食物类');
INSERT INTO Category
VALUES(301, 300, 'Expenditure', '蔬菜', 0);
INSERT INTO Category
VALUES(302, 300, 'Expenditure', '荤菜', 0);
INSERT INTO Category
VALUES(303, 300, 'Expenditure', '每日三餐', 0);

INSERT INTO Category(id, type, name)
VALUES(400, 'Expenditure', '住房类');
INSERT INTO Category
VALUES(401, 400, 'Expenditure', '物业管理费', 0);
INSERT INTO Category
VALUES(402, 400, 'Expenditure', '房租水电', 0);

INSERT INTO Category(id, type, name)
VALUES(500, 'Expenditure', '服饰类');
INSERT INTO Category
VALUES(501, 500, 'Expenditure', '鞋子', 0);
INSERT INTO Category
VALUES(502, 500, 'Expenditure', '裤子', 0);
INSERT INTO Category
VALUES(503, 500, 'Expenditure', '衣服', 0);

INSERT INTO Category(id, type, name)
VALUES(600, 'Expenditure', '交通类');
INSERT INTO Category
VALUES(601, 600, 'Expenditure', '坐火车', 0);
INSERT INTO Category
VALUES(602, 600, 'Expenditure', '坐公交', 0);
