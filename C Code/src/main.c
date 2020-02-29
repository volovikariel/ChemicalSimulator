#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include "../inc/atoms.h"
#include "../inc/prints.h"
#include "../inc/comparator.h"

void iterator(Atom* atomList, int atomListSize, Link* currAtomVisit, Link* solutionList);

int* saveSolution(Atom* atomList, int atomListSize);

int solCount = 0;

int main(int argc, char *argv[])
{
	//example input "---.exe H 2 O 1"

  char names[][2] = {"H\0", "C\0", "N\0", "O\0"};
  int bonds[] = {1, 4, 3, 2};
  int HCNO[] = {0, 0, 0, 0};
  int maxElement = 4;
	int currIndx = 0;
	int size = 0;
	for (int i = 1; i < argc; i+=2) {
		while (!compareString(names[currIndx], argv[i])) {
			currIndx++;

			if (currIndx == maxElement) {
				printf("bad input\n");
				return 7;
			}
		}

		HCNO[currIndx] += atoi(argv[i+1]);
		size += atoi(argv[i+1]);
	}

	/*for (int i = 0; i < 4; i++)
	  {
		  printf("%s: %d\n", names[i], HCNO[i]);
		  //scanf_s("%d", &(HONC[i]));
	  }*/

    Atom* atomList = malloc(size * sizeof(Atom));

    int currOffset = 0;
    for (int k = 0; k < 4; k++)
    {
        for (int i = currOffset; i < currOffset + HCNO[k]; i++)
            loadAtom(bonds[k], names[k], 0, i, &(atomList[i]));
        currOffset += HCNO[k];
    }

    //make linked list for currIdx
    Link* startLink = malloc(sizeof(Link));
    startLink->prevLink = NULL;
    startLink->valuePtr = (void*) atomList;
    startLink->nextLink = NULL;

    //make linked list for solutions
    Link* solLink = malloc(sizeof(Link));
    solLink->prevLink = NULL;
    solLink->valuePtr = NULL;
    solLink->nextLink = NULL;

    iterator(atomList, size, startLink, solLink);

    Link* currLink = startLink;
    while (currLink != NULL)
    {
        startLink = currLink; //used here as a temp
        currLink = currLink->nextLink;
        free(startLink);
    }

    currLink = solLink;
    while (currLink != NULL)
    {
        if (currLink->valuePtr != NULL)
            free(currLink->valuePtr);
        startLink = currLink; //used here as a temp
        currLink = currLink->nextLink;
        free(startLink);
    }

    printf("END\n");
    printf("%d solutions found\n", solCount);

    free(atomList);
	//scanf("%s", NULL);
    return 0;
}

void iterator(Atom* atomList, int atomListSize, Link* currAtomVisit, Link* solutionList)
{
    Link* currAtomLink = currAtomVisit;
    Atom* currAtom = NULL;
    bool isFound = 0;
    while (currAtomLink != NULL && !isFound)
    {
        currAtom = (Atom*) currAtomLink->valuePtr;

        if (currAtom->bondCount)
            isFound = 1;

        currAtomLink = currAtomLink->nextLink;
    }

    if (!isFound)
    {
        int freeLinks = 0;
        for (int i = 0; i < atomListSize; i++)
            freeLinks += atomList[i].bondCount;

        if (freeLinks)
        {
            //printf("Failed branch because of %d missing links\n", freeLinks);
            return;
        }

        //keep total solutions (before comparing)
        //solCount++;
        /*if (solCount % 1000000 == 0)
            printf("%d million\n", solCount / 1000000);*/

        //save the solution
        int* temp = saveSolution(atomList, atomListSize);
        //compare with previous solutions
        //skip first empty link
        Link* tempStartLink = solutionList->nextLink;
        Link* saveBeforeLast = solutionList;
        bool isEqual = 0;
        while (tempStartLink != NULL && !isEqual)
        {
            isEqual = compareSolMatrix(atomList, temp, (int*) tempStartLink->valuePtr, atomListSize);
            saveBeforeLast = tempStartLink;
            tempStartLink = tempStartLink->nextLink;
        }
        //if no similar solution was found
        if (!isEqual)
        {
            printSolMatrix(temp, atomListSize);
            Link* newSolLink = malloc(sizeof(Link));
            newSolLink->prevLink = tempStartLink;
            newSolLink->valuePtr = (void*) temp;
            newSolLink->nextLink = NULL;
            saveBeforeLast->nextLink = newSolLink;

            solCount++;
        }

        return;
    }

    //prev solitary atoms tried
    Link* triedAtomsStart = malloc(sizeof(Link));
    triedAtomsStart->prevLink = NULL;
    triedAtomsStart->nextLink = NULL;
    triedAtomsStart->valuePtr = NULL;

    //for every other atom, save current state, try, then reload current state
    //if not solitary, then try it,
    //if solitary only try one of each atom
    for (int i = 0; i < atomListSize; i++)
    {
        if (i == currAtom->listIndex)
            continue;

        if (atomList[i].bondCount == 0)
            continue;

        if (atomList[i].isSolitary == 1)
        {
            bool wasTried = 0;
            //skipping the last empty item
            Link* currItem = triedAtomsStart;
            while(currItem->nextLink != NULL && !wasTried)
            {
                if (atomList[i].name[0] == ((Atom*) currItem->valuePtr)->name[0])
                    wasTried = 1;

                currItem = currItem->nextLink;
            }

            if (!wasTried)
            {
                //add this attempt
                Link* newTry = malloc(sizeof(Link));
                newTry->prevLink = NULL;
                newTry->nextLink = triedAtomsStart;
                triedAtomsStart->prevLink = newTry;
                newTry->valuePtr = (void*) &(atomList[i]);
                triedAtomsStart = newTry;
            }
            else
                continue;
        }

        /*printf(currAtom->name);
        printf("\t");
        printf(atomList[i].name);
        printf("\n");*/

        currAtom->bondCount--;
        currAtom->isSolitary = 0;
        Link* newCurrStart = malloc(sizeof(Link));
        newCurrStart->prevLink = NULL;
        newCurrStart->nextLink = currAtom->bondList;
        newCurrStart->valuePtr = &(atomList[i]);
        currAtom->bondList->prevLink = newCurrStart;
        currAtom->bondList = newCurrStart;

        atomList[i].bondCount--;
        atomList[i].isSolitary = 0;
        newCurrStart = malloc(sizeof(Link));
        newCurrStart->prevLink = NULL;
        newCurrStart->nextLink = atomList[i].bondList;
        newCurrStart->valuePtr = currAtom;
        atomList[i].bondList->prevLink = newCurrStart;
        atomList[i].bondList = newCurrStart;

        //add this location to beggining of the currAtomVisit
        Link* newStart = malloc(sizeof(Link));
        newStart->prevLink = NULL;
        newStart->valuePtr = (void*) &(atomList[i]);
        newStart->nextLink = currAtomVisit;
        currAtomVisit->prevLink = newStart;


        iterator(atomList, atomListSize, newStart, solutionList);


        //remove this atom from the beginning of the currAtomVisit
        free(currAtomVisit->prevLink);
        currAtomVisit->prevLink = NULL;

        atomList[i].bondList = atomList[i].bondList->nextLink;
        free(atomList[i].bondList->prevLink);
        atomList[i].bondList->prevLink = NULL;
        atomList[i].bondCount++;
        if (atomList[i].bondCount == atomList[i].totalBondCount)
            atomList[i].isSolitary = 1;

        currAtom->bondList = currAtom->bondList->nextLink;
        free(currAtom->bondList->prevLink);
        currAtom->bondList->prevLink = NULL;
        currAtom->bondCount++;
        if (currAtom->bondCount == currAtom->totalBondCount)
            currAtom->isSolitary = 1;
    }

    Link* currItem = triedAtomsStart->nextLink;
    while (currItem != NULL)
    {
        free(currItem->prevLink);
        currItem = currItem->nextLink;
    }

}

int* saveSolution(Atom* atomList, int atomListSize)
{
    int* tempArray;
    tempArray = calloc(atomListSize*atomListSize, sizeof(int));

    for (int i = 0; i < atomListSize; i++)
    {
        Link* currLink = atomList[i].bondList;

        //currLink->nextLink is used since the last one is empty and thus should be ignored
        while(currLink->nextLink != NULL)
        {
            tempArray[atomList[i].listIndex * atomListSize + ((Atom*) currLink->valuePtr)->listIndex] += 1;
            currLink = currLink->nextLink;
        }
    }

    return tempArray;
}
