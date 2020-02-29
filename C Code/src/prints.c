#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include "../inc/atoms.h"
#include "../inc/prints.h"

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
}

void printSolMatrix(int* solution, int atomListSize)
{
    printf(">>>>\n");
    for (int i = 0; i < atomListSize; i++)
    {
        for (int j = 0; j < atomListSize; j++)
            printf("%d ", solution[i * atomListSize + j]);

        printf("\n");
    }
    printf("<<<<\n");
}
