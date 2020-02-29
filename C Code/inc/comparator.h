#ifndef COMPARATOR
#define COMPARATOR

#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include "atoms.h"
#include "prints.h"

typedef struct Pair
{
    char* name;
    int bondWeight;
} Pair;

bool compareSolMatrix(Atom* atomList, int* a, int* b, int atomListSize);
bool compareString(char* a, char* b);
char* appendString(char* a, char* b, int sizeA, int sizeB);
bool isSmallerThan(Pair* a, Pair* b);
bool isSmallerString(char* a, char* b);
bool comparePairList(Pair* a, Pair* b, int listSize);

void sortPairs(Pair* pairList, int listSize);

#endif
