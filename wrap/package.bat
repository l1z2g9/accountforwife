@echo off
rem 打包
jar cvfm core.jar MANIFEST.MF -C ..\..\bin\ .

rem 用可执行文件包装java程序运行
windres account.rc account-res.o
gcc wrap.c account-res.o -o 小艺有数.exe

rem 依赖包
mkdir dependencies
xcopy ..\..\dependencies .\dependencies

rem 初始化数据库
sqlite3 Account.db < ..\src\Account.sql

