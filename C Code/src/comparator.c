#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include "../inc/atoms.h"
#include "../inc/comparator.h"

#include "../inc/memory.h"

bool compareSolMatrix(Atom* atomList, int* a, int* b, int atomListSize)
{
    /*//check if they are the same, without permutations
    for (int i = 0; i < atomListSize*atomListSize; i++)
        if (a[i] != b[i])
            return 0;
    */

    bool* isDisabled = calloc(atomListSize, sizeof(bool));
#ifdef MEMDEBUG
    printMalloc((void*) isDisabled, atomListSize * sizeof(bool), 14);
#endif

    Pair* aPairList = malloc(atomListSize * sizeof(Pair));
    Pair* bPairList = malloc(atomListSize * sizeof(Pair));
#ifdef MEMDEBUG
    printMalloc((void*) aPairList, atomListSize * sizeof(Pair), 15);
    printMalloc((void*) bPairList, atomListSize * sizeof(Pair), 16);
#endif

    bool foundSimRow = 0;

    int j = 0;
    int k = 0;

    for (int i  = 0; i < atomListSize; i ++)
    {
      foundSimRow = 0;

      for (int j = 0; j < atomListSize; j++)
      {
        aPairList[j].name = atomList[j].name;
        aPairList[j].bondWeight = a[i * atomListSize + j];
      }

      sortPairs(aPairList, atomListSize);

      //find a similar row in b that isn't disabled
      for (j = 0; j < atomListSize && !foundSimRow; j++)
      {
        //if this row wasn't already used
        if (!isDisabled[j])
        {

          for (k = 0; k < atomListSize; k++)
          {
            bPairList[k].name = atomList[k].name;
            bPairList[k].bondWeight = b[j * atomListSize + k];
          }

          sortPairs(bPairList, atomListSize);

          if (comparePairList(aPairList, bPairList, atomListSize))
          {
            foundSimRow = 1;
            isDisabled[j] = 1;
          }
        }
      }

      //if failed to find a similar row
      if (!foundSimRow)
      {
        free(isDisabled);
        free(aPairList);
        free(bPairList);
#ifdef MEMDEBUG
        printFree((void*) isDisabled, 12);
        printFree((void*) aPairList, 13);
        printFree((void*) bPairList, 14);
#endif
        return 0;
      }
    }

    free(isDisabled);
    free(aPairList);
    free(bPairList);
#ifdef MEMDEBUG
    printFree((void*) isDisabled, 15);
    printFree((void*) aPairList, 16);
    printFree((void*) bPairList, 17);
#endif

    return 1;
}

// Must be null terminated
bool compareString(char* a, char* b)
{
	int i;
	for (i = 0; a[i] != '\0' && b[i] != '\0'; i++)
  {
		if (a[i] != b[i])
			return 0;
  }

	//one must have caught and been equal to '\0'
	//thus if they both are equal to '\0', they are equal
	if (a[i] == b[i])
		return 1;

	return 0;
}

char* appendString(char* a, char* b, int sizeA, int sizeB)
{
    char* returnStr = calloc(sizeA + sizeB, sizeof(char));
#ifdef MEMDEBUG
    printMalloc((void*) returnStr, (sizeA + sizeB) * sizeof(bool), 17);
#endif
    int offset = 0;
    for (int i = 0; i < sizeA; i++)
    {
        if (a[i] != '\0')
          returnStr[i - offset] = a[i];
        else
          offset++;
    }
    for (int i = 0; i < sizeA; i++)
    {
        if (b[i] != '\0')
          returnStr[i - offset + sizeA] = b[i];
        else
          offset++;
    }

    return returnStr;
}

bool isSmallerThan(Pair* a, Pair* b)
{
    if (isSmallerString(a->name, b->name))
      return 1;

    if (isSmallerString(b->name, a->name))
      return 0;

    //compare numbers
    return a->bondWeight <= b->bondWeight;
}

bool isSmallerString(char* a, char* b)
{
    int index = 0;
    while (a[index] != '\0' && b[index] != '\0')
    {
        if (a[index] < b[index])
          return 1;

        if (b[index] < a[index])
          return 0;

        index++;
    }

    if (b[index] != '\0')
      return 1;

    return 0;
}

void sortPairs(Pair* pairList, int listSize)
{
  bool isSorted = 0;
  Pair tempPair;
  while(!isSorted)
  {
    isSorted = 1;
    for (int i = 0; i < listSize - 1; i++)
    {
      if (!isSmallerThan(&(pairList[i]), &(pairList[i+1])))
      {
        isSorted = 0;
        tempPair.name = pairList[i].name;
        tempPair.bondWeight = pairList[i].bondWeight;
        pairList[i].name = pairList[i+1].name;
        pairList[i].bondWeight = pairList[i+1].bondWeight;
        pairList[i+1].name = tempPair.name;
        pairList[i+1].bondWeight = tempPair.bondWeight;
      }
    }
  }
}

bool comparePairList(Pair* a, Pair* b, int listSize)
{
  for (int i = 0; i < listSize; i++)
  {
    if (!compareString(a[i].name, b[i].name) || a[i].bondWeight != b[i].bondWeight)
      return 0;
  }

  return 1;
}