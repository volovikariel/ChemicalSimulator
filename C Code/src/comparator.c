#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include "../inc/atoms.h"
#include "../inc/comparator.h"

#include "../inc/memory.h"

bool isIsomorph(Atom* atomList, int* a, int* b, int atomListSize)
{
    //assumes int* b is already reduced, since it it old solutions,
    // must reduce A, then must compare reduced forms

    //reduceMatrix(atomList, a, atomListSize);

    for (int i = 0; i < atomListSize * atomListSize; i++)
      if (a[i] != b[i])
        return 0;

    return 1;
}

void reduceMatrix(Atom* atomList, int* matrix, int atomListSize)
{
    bool hasEdited = 1;
    int atomCount = 1;

    int whileCount = 0;

    while (hasEdited && whileCount < atomListSize * atomListSize)
    {
        whileCount++;

        hasEdited = 0;
        atomCount = 1;
        // compare each row with the following
        for (int i = 0; i < atomListSize - 1; i++)
        {
            if (atomList[i + 1].atomicNumber == atomList[i].atomicNumber)
              atomCount++;
            else
            {
              if (atomCount == 1)
              {
                //reduce this row as much as possible
                bool hasChanged = 1;

                while (hasChanged)
                {
                  hasChanged = 0;

                  for (int j = 0; j < atomListSize - 1; j++)
                  {
                    if (atomList[j + 1].atomicNumber == atomList[j].atomicNumber)
                    {
                      if (matrix[i * atomListSize + j] < matrix[i * atomListSize + j + 1])
                      {
                        hasChanged = 1;
                        hasEdited = 1;

                        swapRowCol(matrix, j, j + 1, atomListSize);

                        break;
                      }
                    }
                  }
                }
              }

              atomCount = 1;
            }
            // if the next row is "smaller", then swap them and restart the loop
            //make sure they are the same element
            if (atomList[i + 1].atomicNumber == atomList[i].atomicNumber)
            {
                //go element by element and compare for first difference
                for (int k = 0; k < atomListSize; k++)
                {
                    if (matrix[i * atomListSize + k] < matrix[(i + 1) * atomListSize + k])
                    {
                        hasEdited = 1;

                        swapRowCol(matrix, i, i + 1, atomListSize);

                        break;
                    }
                    else if (matrix[i * atomListSize + k] > matrix[(i + 1) * atomListSize + k])
                      break;
                }
            }
        }

        if (atomCount == 1)
        {
          //reduce this row as much as possible
          bool hasChanged = 1;

          while (hasChanged)
          {
            hasChanged = 0;

            for (int j = 0; j < atomListSize - 1; j++)
            {
              if (atomList[j + 1].atomicNumber == atomList[j].atomicNumber)
              {
                if (matrix[(atomListSize - 1) * atomListSize + j] < matrix[(atomListSize - 1) * atomListSize + j + 1])
                {
                  hasChanged = 1;
                  hasEdited = 1;

                  swapRowCol(matrix, j, j + 1, atomListSize);

                  break;
                }
              }
            }
          }
        }
    }
}

void swapRowCol(int* matrix, int row1, int row2, int atomListSize)
{
    int temp;
    for (int i = 0; i < atomListSize; i++)
    {
      temp = matrix[row1 * atomListSize + i];
      matrix[row1 * atomListSize + i] = matrix[row2 * atomListSize + i];
      matrix[row2 * atomListSize + i] = temp;
    }

    for (int j = 0; j < atomListSize; j++)
    {
        temp = matrix[j * atomListSize + row1];
        matrix[j * atomListSize + row1] = matrix[j * atomListSize + row2];
        matrix[j * atomListSize + row2] = temp;
    }
}

bool compareSolMatrix(Atom* atomList, int* a, int* b, int atomListSize)
{
    /*//check if they are the same, without permutations
    for (int i = 0; i < atomListSize*atomListSize; i++)
        if (a[i] != b[i])
            return 0;
    */

    //save array for which rows have already been used
    bool* isDisabled = calloc(atomListSize, sizeof(bool));
#ifdef MEMDEBUG
    printMalloc((void*) isDisabled, atomListSize * sizeof(bool), 14);
#endif

    //save 2 pairLists to be used later for comparison
    Pair* aPairList = malloc(atomListSize * sizeof(Pair));
    Pair* bPairList = malloc(atomListSize * sizeof(Pair));
#ifdef MEMDEBUG
    printMalloc((void*) aPairList, atomListSize * sizeof(Pair), 15);
    printMalloc((void*) bPairList, atomListSize * sizeof(Pair), 16);
#endif

    bool foundSimRow = 0;

    int j = 0;
    int k = 0;

    //for every row
    for (int i  = 0; i < atomListSize; i++)
    {
      //if this row was disabled, dont even try it
      if (isDisabled[i])
        continue;

      foundSimRow = 0;

      //save the row as a pairList
      for (j = 0; j < atomListSize; j++)
      {
        aPairList[j].atomicNumber = atomList[j].atomicNumber;
        aPairList[j].bondWeight = a[i * atomListSize + j];
      }

      //sort this pair
      sortPairs(aPairList, atomListSize);

      //find a similar row in b that isn't disabled
      for (j = 0; j < atomListSize && !foundSimRow; j++)
      {
        //if this row wasn't already used
        if (!isDisabled[j])
        {
          //save the row as a pairList
          for (k = 0; k < atomListSize; k++)
          {
            bPairList[k].atomicNumber = atomList[k].atomicNumber;
            bPairList[k].bondWeight = b[j * atomListSize + k];
          }

          //sort this pair
          sortPairs(bPairList, atomListSize);

          if (comparePairList(aPairList, bPairList, atomListSize))
          {
            foundSimRow = 1;
            isDisabled[j] = 1;
            isDisabled[i] = 1;
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
    /*if (isSmallerString(a->name, b->name))
      return 1;

    if (isSmallerString(b->name, a->name))
      return 0;
    */

    if (a->atomicNumber == b->atomicNumber)
      //compare numbers
      return a->bondWeight <= b->bondWeight;

    return a->atomicNumber < b->atomicNumber;
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
        tempPair.atomicNumber = pairList[i].atomicNumber;
        tempPair.bondWeight = pairList[i].bondWeight;
        pairList[i].atomicNumber = pairList[i+1].atomicNumber;
        pairList[i].bondWeight = pairList[i+1].bondWeight;
        pairList[i+1].atomicNumber = tempPair.atomicNumber;
        pairList[i+1].bondWeight = tempPair.bondWeight;
      }
    }
  }
}

bool comparePairList(Pair* a, Pair* b, int listSize)
{
  for (int i = 0; i < listSize; i++)
  {
    if (a[i].atomicNumber != b[i].atomicNumber || a[i].bondWeight != b[i].bondWeight)
      return 0;
  }

  return 1;
}
