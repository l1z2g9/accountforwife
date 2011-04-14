compile:
	cl /ID:\VC2008\include /ID:\VC2008\PlatformSDK\Include /I"e:\Program Files\Java\jdk1.6.0_21\include" /I"e:\Program Files\Java\jdk1.6.0_21\include\win32" /ID:\VC2008\include /EHsc /c wrapper.c

link:
	rc /n /v /fo account.res account.rc
	link /RELEASE /LIBPATH:"e:\Program Files\Java\jdk1.6.0_21\lib" /LIBPATH:D:\VC2008\lib user32.lib uuid.lib gdi32.lib jvm.lib /LIBPATH:D:\VC2008\PlatformSDK\Lib /SUBSYSTEM:WINDOWS /OUT:小艺有数.exe wrapper.obj account.res

deploy: compile link
	mv 小艺有数.exe ..\

clean:
	del wrapper.obj
	del account.res
