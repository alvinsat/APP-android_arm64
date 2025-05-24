


extern char strHomePath[256];
extern char strConfigPath[256];
extern void* mainBlenderInitial(int argc, const char **argv);
extern void mainBlenderInitial_reinit(void*pContext);
extern int mainBlenderLoop(void*pContext);
extern void initialLib(void*pNativeWindo);
extern void oblSetValue(int values[],int num);
extern void oblSetValueOn(int values[],int num);
extern void oblSetValueOff(int values[],int num);
extern void inputKey(int p_physical_keycode,
                     int p_unicode, int p_key_label, int p_pressed,
                     int p_echo);