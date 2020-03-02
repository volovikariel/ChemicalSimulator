#include <stdio.h>
#include <stdlib.h>
#include "../inc/memory.h"

int mallocCount = 0;
Entry start = {0, 0};

void printMalloc(void* ref, int size, int debugCode)
{
  Entry* curr = &start;
  while (curr->nextEntry != NULL)
    curr = curr->nextEntry;

  curr->nextEntry = malloc(sizeof(Entry));
  curr->nextEntry->ptr = ref;
  curr->nextEntry->debugCode = debugCode;
  curr->nextEntry->nextEntry = NULL;

  mallocCount++;
  printf("%p -> Allocated %d bytes (%d allocated) [%d]\n", ref, size, mallocCount, debugCode);
}

void printFree(void* ref, int debugCode)
{
  Entry* curr = &start;
  Entry* prev = NULL;
  while (curr != NULL && curr->ptr != ref)
  {
    prev = curr;
    curr = curr->nextEntry;
  }

  if (curr == NULL)
    printf("````````````Trying to free unallocated memory address -> %p!! [%d]\n", ref, debugCode);
  else
  {
    prev->nextEntry = curr->nextEntry;
    free(curr);
  }

  mallocCount--;
  printf("%p -> Freed (%d allocated) [%d]\n", ref, mallocCount, debugCode);
  if (!mallocCount)
    printf("~~~~~~~~~ Evertyhing has been freed!\n");
}

void cleanup()
{
  Entry* curr = start.nextEntry;
  Entry* temp = curr;
  while (curr != NULL)
  {
    temp = curr;
    curr = curr->nextEntry;
    printf("........ Forgot to free %p [%d]\n", temp->ptr, temp->debugCode);
    free(temp);
  }
}
