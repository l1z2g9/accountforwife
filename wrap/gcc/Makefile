compile:
	windres account.rc account-res.o
	gcc wrapper.c account-res.o -o 小艺有数.exe
	
deploy: compile
	mv 小艺有数.exe ../

clean:
	del account-res.o
