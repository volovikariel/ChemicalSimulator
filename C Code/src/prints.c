#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include "../inc/atoms.h"
#include "../inc/prints.h"

#include "../inc/memory.h"

void printMol(Atom* atomList, int atomListSize)
{
    printf(">----Start of Solution----<\n");
    for (int i = 0; i < atomListSize; i++)
    {
        printf("%s%d =>", atomList[i].name, atomList[i].listIndex);
        Link* currLink = atomList[i].bondList;
        Atom* currAtom;

        //currLink->nextLink is used since the last one is empty and thus should be ignored
        while(currLink->nextLink != NULL)
        {
            currAtom = (Atom*) currLink->valuePtr;
            printf(" %s%d", currAtom->name, currAtom->listIndex);

            currLink = currLink->nextLink;
        }
        printf("\n");
    }
    printf("<-----End of Solution----->\n");
}

void printMolMatrix(Atom* atomList, int atomListSize)
{
    int* tempArray;
    tempArray = malloc(sizeof(int)*atomListSize);
#ifdef MEMDEBUG
    printMalloc((void*) tempArray, sizeof(int)*atomListSize, 11);
#endif

    printf(">>>>\n");

    for (int i = 0; i < atomListSize; i++)
    {
        printf("%s%d :", atomList[i].name, atomList[i].listIndex);

        memset(tempArray, 0, sizeof(int)*atomListSize);

        Link* currLink = atomList[i].bondList;
        Atom* currAtom;

        //currLink->nextLink is used since the last one is empty and thus should be ignored
        while(currLink->nextLink != NULL)
        {
            currAtom = (Atom*) currLink->valuePtr;
            //printf(" %s%d", currAtom->name, currAtom->listIndex);
            tempArray[currAtom->listIndex] += 1;

            currLink = currLink->nextLink;
        }

        for (int j = 0; j < atomListSize; j++)
        {
            printf(" %d", tempArray[j]);
        }
        printf("\n");
    }

    printf("<<<<\n");

    free(tempArray);
#ifdef MEMDEBUG
    printFree((void*) tempArray, 11);
#endif
}

void printSolMatrix(Solution* solution, int atomListSize)
{
    printf(">>>>\n");
    printf("%d\n", solution->score);
    for (int i = 0; i < atomListSize; i++)
    {
        for (int j = 0; j < atomListSize; j++)
            printf("%d ", solution->matrix[i * atomListSize + j]);

        printf("\n");
    }
    printf("<<<<\n");
}

void printSolution(Solution* solution, int atomListSize, int metalListSize)
{
  printf(">>>>\n");
  printf("%d\n", solution->score);
  for (int i = 0; i < atomListSize; i++)
  {
      for (int j = 0; j < atomListSize; j++)
          printf("%d ", solution->matrix[i * atomListSize + j]);

      printf("\n");
  }

  //print the ratio list, including the covalent parts ratio
  for (int i = 0; i < metalListSize + 1; i++)
  {
      printf("%d ", solution->ionRatios[i]);
  }

  printf("\n");

  printf("<<<<\n");
}
