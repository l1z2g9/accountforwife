@echo off
rem 打包
jar cvfm core.jar MANIFEST.MF -C ..\..\bin\ .

rem 用可执行文件包装java程序运行
nmake 

rem 依赖包
mkdir dependencies
xcopy ..\dependencies .\dependencies

rem 初始化数据库
sqlite3 Account.db < ..\src\Account.sql

python zip-all.py

rem 清除文件
del /f /q dependencies
rmdir dependencies
del Account.db
del 小艺有数.exe
del core.jar
nmake clean