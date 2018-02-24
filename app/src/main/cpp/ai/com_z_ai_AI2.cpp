#include <jni.h>
#include <android/log.h>
#include "2048.h"


#define DLL_PUBLIC

#ifdef __cplusplus
extern "C" {
#endif
int init = 0;

JNIEXPORT jint JNICALL Java_com_z_ai_AI2_getAIResult
  (JNIEnv *env, jclass obj, jstring jboard)
{
	if(!init)
	{
		init = 1;
		init_tables();
		printf("--init_table");
	}
	const char *board = env->GetStringUTFChars(jboard, 0);
	printf("cpp board:%s\n",board);
	char * pEnd;
  unsigned long long int llboard;
  llboard = strtoull (board, &pEnd, 10);
	int ret = find_best_move(llboard);
  env->ReleaseStringUTFChars(jboard, board);
	return ret;
}

#ifdef __cplusplus
}
#endif