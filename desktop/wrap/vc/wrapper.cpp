// 参考 http://www.codeproject.com/KB/applications/cvm.aspx?display=Mobile
#include <stdio.h>
#include <jni.h>
#include <string>
#include <windows.h>

#define MAX_JVM_OPTIONS 15
typedef jint (JNICALL CreateJavaVM_t)(JavaVM **pvm, void **env, void *args);

//Global variables
JavaVM     *g_psJvm;                            //Pointer to the virtual machine
char       *g_acJvmOptions [MAX_JVM_OPTIONS];   //Global array which contains the JVM Options
int        g_iJvmOptionCount = 0;               //A counter to the JVM options
int        g_iMain_argc = 0;
char       *g_pcClasspath;                      //Classpath
char       **g_pcMain_argv = NULL;              //Command line arguments


HINSTANCE  g_sHandle;                           //Handle to the jvm.dll
jthrowable g_sException;                        //JNI Exception handling.

JNIEnv* CreateVm(char* dllpath)
{
    JNIEnv           *psJNIEnv;
    JavaVMInitArgs   sJavaVMInitArgs;
    JavaVMOption     psJavaVMOption[25];
    int iRetval      = 0;
    CreateJavaVM_t   *pfnCreateJavaVM;

    /* Load the Java VM DLL
    It is expected that , u have to place this wrapper under
    the bin folder of ur jre */
    if ( (g_sHandle = LoadLibrary(dllpath) ) == NULL){
        printf("Unable to load..\\client\\jvm.dll\n");
        return NULL;
    }

    /* Now get the function addresses */
    pfnCreateJavaVM = (CreateJavaVM_t *)GetProcAddress(g_sHandle, "JNI_CreateJavaVM");
    if (pfnCreateJavaVM == NULL)
    {
        printf("Unable to find JNI_CreateJavaVM\n");
        return NULL;
    }

    sJavaVMInitArgs.version         = JNI_VERSION_1_2;      //JVM Version
    sJavaVMInitArgs.nOptions        = g_iJvmOptionCount+1;  //Number of JVM Versions
    psJavaVMOption[0].optionString  = g_pcClasspath;        //Class Path

    //Copy all your JVM Option
    for (int i = 0; i < g_iJvmOptionCount; i++)
    {
        psJavaVMOption[i+1].optionString = g_acJvmOptions[i];
    }

    sJavaVMInitArgs.options             = psJavaVMOption;
    sJavaVMInitArgs.ignoreUnrecognized  = JNI_FALSE;        //ignore options VM does not understand;

    //Now Create the JVM
    iRetval = pfnCreateJavaVM(&g_psJvm, (void **)&psJNIEnv, &sJavaVMInitArgs);
    if(iRetval != 0)
    {
        printf("Cannot Create JVM");
        return NULL;
    }

    return psJNIEnv;
}

void InvokeClass(JNIEnv* psJNIEnv)
{
    jclass jcJclass = psJNIEnv->FindClass("cat/AccountPanel");

    jmethodID jmMainMethod = psJNIEnv->GetStaticMethodID(jcJclass, "main", "([Ljava/lang/String;)V");

    psJNIEnv->CallStaticVoidMethod(jcJclass, jmMainMethod, NULL);

    g_psJvm->DestroyJavaVM();
}

int __stdcall WinMain(HINSTANCE hInstance, HINSTANCE hPrevInstance, LPSTR lpCmdLine, int nCmdShow){
	HKEY hKey, hVersionKey;
	#define JRE_REG_PATH         TEXT("Software\\JavaSoft\\Java Runtime Environment\\1.6")
	#define JRE_LIB_KEY          TEXT("RuntimeLib")

	LONG retVal = RegOpenKeyEx(HKEY_LOCAL_MACHINE,JRE_REG_PATH,0,KEY_ALL_ACCESS,&hKey); //-- 打开子键

	if (retVal != ERROR_SUCCESS)
	{
		puts("read Reg fail");
		return 1;
	}

	TCHAR filename[MAX_PATH];
	DWORD length = MAX_PATH;

	if(RegQueryValueEx(hKey, JRE_LIB_KEY, NULL, NULL, (LPBYTE)&filename, &length) != ERROR_SUCCESS){
		puts("read Reg fail");
		return 1;
	}

	RegCloseKey(hKey);

    g_pcClasspath = (char *)malloc(100);
    sprintf(g_pcClasspath, "-Djava.class.path=%s", "core.jar");

    JNIEnv* psJNIEnv = CreateVm(filename);
	InvokeClass( psJNIEnv );
    FreeLibrary(g_sHandle);
    return 0;
}
