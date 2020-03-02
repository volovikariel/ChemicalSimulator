#ifndef MEMORY
#define MEMORY

typedef struct Entry
{
  void* ptr;
  struct Entry* nextEntry;
  int debugCode;
} Entry;

void printMalloc(void* ref, int size, int debugCode);
void printFree(void* ref, int debugCode);

void cleanup();

#endif
