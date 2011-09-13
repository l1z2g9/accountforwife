VC_PATH=E:\Application\C++\VC2008
JAVA_PATH=c:\Program Files\Java\jdk1.6.0_05

compile:
	cl /I$(VC_PATH)\include /I$(VC_PATH)\PlatformSDK\Include /I"$(JAVA_PATH)\include" /I"$(JAVA_PATH)\include\win32" /I$(VC_PATH)\include /EHsc /c wrapper.cpp

link:
	rc /n /v /fo account.res account.rc
	link /RELEASE /LIBPATH:"$(JAVA_PATH)\lib" /LIBPATH:$(VC_PATH)\lib user32.lib uuid.lib Advapi32.lib jvm.lib /LIBPATH:$(VC_PATH)\PlatformSDK\Lib /SUBSYSTEM:WINDOWS /OUT:小艺有数.exe wrapper.obj account.res

deploy: compile link
	mv 小艺有数.exe ..\

clean:
	del wrapper.obj
	del account.res

test:
	cl /I$(VC_PATH)\include /I$(VC_PATH)\PlatformSDK\Include /I"$(JAVA_PATH)\include" /I"$(JAVA_PATH)\include\win32" /I$(VC_PATH)\include /EHsc /c cvm.cpp
	link /RELEASE /LIBPATH:"$(JAVA_PATH)\lib" /LIBPATH:$(VC_PATH)\lib user32.lib uuid.lib gdi32.lib jvm.lib Advapi32.lib /LIBPATH:$(VC_PATH)\PlatformSDK\Lib /SUBSYSTEM:WINDOWS /OUT:cvm.exe cvm.obj
	cvm.exe