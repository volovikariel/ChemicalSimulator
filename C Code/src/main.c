#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include "../inc/atoms.h"
#include "../inc/prints.h"
#include "../inc/comparator.h"
#include "../inc/csvParser.h"

#include "../inc/memory.h"

void iterator(Atom* atomList, int atomListSize, Link* currAtomVisit, Link* solutionList);
void ionizeSolutions(Link* solutionList, Atom* atomList, Atom* metalList, int atomListSize, int metalListSize);

int* saveSolution(Atom* atomList, int atomListSize);

int solCount = 0;

int main(int argc, char *argv[])
{
	//example input "---.exe H 2 O 1"

  Element* table = loadTable("res/elements.csv");

	int currIndx = 0;
	int sizeCovalent = 0;
  int sizeIonic = 0;
	for (int i = 1; i < argc; i+=2) {
		while (!compareString(table[currIndx].name, argv[i])) {
			currIndx++;

			if (currIndx == TABLESIZE) {
				printf("bad input: %s\n", argv[i]);
				return 7;
			}
		}
		table[currIndx].count += atoi(argv[i+1]);
    if (!table[currIndx].isMetal)
		  sizeCovalent += table[currIndx].count;
    else
      sizeIonic += table[currIndx].count;
	}

	/*for (int i = 0; i < 4; i++)
	  {
		  printf("%s: %d\n", names[i], HCNO[i]);
		  //scanf_s("%d", &(HONC[i]));
	  }*/

    Atom* atomList = malloc(sizeCovalent * sizeof(Atom));
#ifdef MEMDEBUG
    printMalloc((void*) atomList, sizeCovalent * sizeof(Atom), 0);
#endif

    Atom* metalList = malloc(sizeIonic * sizeof(Atom));
#ifdef MEMDEBUG
    printMalloc((void*) atomList, sizeIonic * sizeof(Atom), 0);
#endif

    int currOffset = 0;
    int metalOffset = 0;
    for (int k = 0; k < TABLESIZE; k++)
    {
      if (!table[k].isMetal)
      {
        for (int i = currOffset; i < currOffset + table[k].count; i++)
            loadAtom(table[k].bond, table[k].name, table[k].elecNeg, i, table[k].atomicNumber, &(atomList[i]));
        currOffset += table[k].count;
      }
      else
      {
        for (int i = metalOffset; i < metalOffset + table[k].count; i++)
            loadAtom(table[k].bond, table[k].name, table[k].elecNeg, i, table[k].atomicNumber, &(metalList[i]));
        metalOffset += table[k].count;
      }
    }

    clearTable(table);

    //make linked list for currIdx
    Link* startLink = malloc(sizeof(Link));
    startLink->prevLink = NULL;
    startLink->valuePtr = (void*) atomList;
    startLink->nextLink = NULL;
#ifdef MEMDEBUG
    printMalloc((void*) startLink, sizeof(Link), 1);
#endif

    //make linked list for solutions
    Link* solLink = malloc(sizeof(Link));
    solLink->prevLink = NULL;
    solLink->valuePtr = NULL;
    solLink->nextLink = NULL;
#ifdef MEMDEBUG
    printMalloc((void*) solLink, sizeof(Link), 2);
#endif

    //get a list of solutions for the covalent part
    iterator(atomList, sizeCovalent, startLink, solLink);

    if (sizeIonic)
      //see if the ionic part can be added to it
      ionizeSolutions(solLink, atomList, metalList, sizeCovalent, sizeIonic);

    //print all the solutions
    //start with the list of atoms
    printf("+ ");
    for (int i = 0; i < sizeCovalent; i++)
      printf("%s ", atomList[i].name);
    for (int i = 0; i < sizeIonic; i++)
      printf("%s ", metalList[i].name);

    printf("\n");

    //print the solutions
    Link* currLink;

    //skip first empty entry
    currLink = solLink->nextLink;
    while (currLink != NULL)
    {
        printSolMatrix(((Solution*) currLink->valuePtr), sizeCovalent + sizeIonic);
        currLink = currLink->nextLink;
    }

    currLink = startLink;
    while (currLink != NULL)
    {
        startLink = currLink; //used here as a temp
        currLink = currLink->nextLink;
#ifdef MEMDEBUG
        printFree((void*) startLink, 1);
#endif
        free(startLink);
    }

    for (int i = 0; i < sizeCovalent; i++)
    {
        currLink = atomList[i].bondList;
        while (currLink != NULL)
        {
            if (currLink->valuePtr != NULL)
            {
#ifdef MEMDEBUG
                printFree(currLink->valuePtr, 2);
#endif
                free(currLink->valuePtr);
            }
            startLink = currLink; //used here as a temp
            currLink = currLink->nextLink;
#ifdef MEMDEBUG
            printFree((void*) startLink, 3);
#endif
            free(startLink);
        }
    }

    for (int i = 0; i < sizeIonic; i++)
    {
        currLink = metalList[i].bondList;
        while (currLink != NULL)
        {
            if (currLink->valuePtr != NULL)
            {
#ifdef MEMDEBUG
                printFree(((Solution*) currLink->valuePtr)->matrix, 2);
#endif
                free(((Solution*) currLink->valuePtr)->matrix);
#ifdef MEMDEBUG
                printFree(currLink->valuePtr, 2);
#endif
                free(currLink->valuePtr);
            }
            startLink = currLink; //used here as a temp
            currLink = currLink->nextLink;
#ifdef MEMDEBUG
            printFree((void*) startLink, 3);
#endif
            free(startLink);
        }
    }

    currLink = solLink;
    while (currLink != NULL)
    {
        if (currLink->valuePtr != NULL)
        {
#ifdef MEMDEBUG
            printFree(currLink->valuePtr, 4);
#endif
            free(currLink->valuePtr);
        }
        startLink = currLink; //used here as a temp
        currLink = currLink->nextLink;
#ifdef MEMDEBUG
        printFree((void*) startLink, 5);
#endif
        free(startLink);
    }

    printf("END\n");
    printf("%d solutions found\n", solCount);

#ifdef MEMDEBUG
    printFree((void*) atomList, 6);
#endif
    free(atomList);

#ifdef MEMDEBUG
    printFree((void*) metalList, 6);
#endif
    free(metalList);

#ifdef MEMDEBUG
    cleanup();
#endif

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
        reduceMatrix(atomList, temp, atomListSize);
        //compare with previous solutions
        //skip first empty link
        Link* tempStartLink = solutionList->nextLink;
        Link* saveBeforeLast = solutionList;
        bool isEqual = 0;
        while (tempStartLink != NULL && !isEqual)
        {
            isEqual = compareSolMatrix(atomList, temp, (int*) tempStartLink->valuePtr, atomListSize);
            //isEqual = isIsomorph(atomList, temp, (int*) tempStartLink->valuePtr, atomListSize);
            saveBeforeLast = tempStartLink;
            tempStartLink = tempStartLink->nextLink;
        }
        //if no similar solution was found
        if (!isEqual)
        {
            //printSolMatrix(temp, atomListSize);
            Link* newSolLink = malloc(sizeof(Link));
            newSolLink->prevLink = tempStartLink;
            Solution* newSol = malloc(sizeof(Solution));
            newSol->matrix = temp;
            newSol->overallCharge = 0;
            newSol->score = 0;
            newSolLink->valuePtr = (void*) newSol;
            newSolLink->nextLink = NULL;
            saveBeforeLast->nextLink = newSolLink;
#ifdef MEMDEBUG
            printMalloc((void*) newSolLink, sizeof(Link), 4);
#endif
#ifdef MEMDEBUG
            printMalloc((void*) newSol, sizeof(Solution), 4);
#endif

            solCount++;
        }
        else
        {
#ifdef MEMDEBUG
          printFree(temp, 18);
#endif
          free(temp);
        }

        return;
    }

    //prev solitary atoms tried
    Link* triedAtomsStart = malloc(sizeof(Link));
    triedAtomsStart->prevLink = NULL;
    triedAtomsStart->nextLink = NULL;
    triedAtomsStart->valuePtr = NULL;
#ifdef MEMDEBUG
    printMalloc((void*) triedAtomsStart, sizeof(Link), 5);
#endif

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
#ifdef MEMDEBUG
                printMalloc((void*) newTry, sizeof(Link), 6);
#endif
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
#ifdef MEMDEBUG
        printMalloc((void*) newCurrStart, sizeof(Link), 7);
#endif

        atomList[i].bondCount--;
        atomList[i].isSolitary = 0;
        newCurrStart = malloc(sizeof(Link));
        newCurrStart->prevLink = NULL;
        newCurrStart->nextLink = atomList[i].bondList;
        newCurrStart->valuePtr = currAtom;
        atomList[i].bondList->prevLink = newCurrStart;
        atomList[i].bondList = newCurrStart;
#ifdef MEMDEBUG
        printMalloc((void*) newCurrStart, sizeof(Link), 8);
#endif

        //add this location to beggining of the currAtomVisit
        Link* newStart = malloc(sizeof(Link));
        newStart->prevLink = NULL;
        newStart->valuePtr = (void*) &(atomList[i]);
        newStart->nextLink = currAtomVisit;
        currAtomVisit->prevLink = newStart;
#ifdef MEMDEBUG
        printMalloc((void*) newStart, sizeof(Link), 9);
#endif


        iterator(atomList, atomListSize, newStart, solutionList);


        //remove this atom from the beginning of the currAtomVisit
#ifdef MEMDEBUG
        printFree((void*) currAtomVisit->prevLink, 7);
#endif
        free(currAtomVisit->prevLink);
        currAtomVisit->prevLink = NULL;

        atomList[i].bondList = atomList[i].bondList->nextLink;
#ifdef MEMDEBUG
        printFree((void*) atomList[i].bondList->prevLink, 8);
#endif
        free(atomList[i].bondList->prevLink);
        atomList[i].bondList->prevLink = NULL;
        atomList[i].bondCount++;
        if (atomList[i].bondCount == atomList[i].totalBondCount)
            atomList[i].isSolitary = 1;

        currAtom->bondList = currAtom->bondList->nextLink;
#ifdef MEMDEBUG
        printFree((void*) currAtom->bondList->prevLink, 9);
#endif
        free(currAtom->bondList->prevLink);
        currAtom->bondList->prevLink = NULL;
        currAtom->bondCount++;
        if (currAtom->bondCount == currAtom->totalBondCount)
            currAtom->isSolitary = 1;
    }

    Link* currItem = triedAtomsStart;
    Link* tempLink;
    while (currItem != NULL)
    {
        tempLink = currItem;
        currItem = currItem->nextLink;
#ifdef MEMDEBUG
        printFree((void*) tempLink, 10);
#endif
        free(tempLink);
    }
}

int* saveSolution(Atom* atomList, int atomListSize)
{
    int* tempArray;
    tempArray = calloc(atomListSize*atomListSize, sizeof(int));
#ifdef MEMDEBUG
    printMalloc((void*) tempArray, atomListSize*atomListSize * sizeof(int), 10);
#endif

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

void ionizeSolutions(Link* solutionList, Atom* atomList, Atom* metalList, int atomListSize, int metalListSize)
{
    //go through every solution
    Link* currLink;
    //skip the first empty entry
    currLink = solutionList->nextLink;

    Solution* currSol;
    int* newMatrix;

    int totalSize = atomListSize + metalListSize;

    while (currLink != NULL)
    {
        currSol = (Solution*) currLink->valuePtr;
        newMatrix = malloc(totalSize * totalSize * sizeof(int));
  #ifdef MEMDEBUG
        printMalloc((void*) newMatrix, totalSize*totalSize * sizeof(int), 25);
  #endif

        if (currSol->overallCharge > 0)
        {

        }
        else
        {
            //add a row where the extra ions keep their electron
            for (int i = 0; i < atomListSize; i++)
              for (int j = 0; j < atomListSize; j++)
                newMatrix[i * totalSize + j] = currSol->matrix[i * atomListSize + j];

            for (int i = atomListSize; i < totalSize; i++)
              for (int j = 0; j < totalSize; j++)
              {
                if (i == j)
                {
                  newMatrix[j * totalSize + i] = 8 - metalList[i - atomListSize].totalBondCount;
                }
                else
                {
                  newMatrix[j * totalSize + i] = 0;
                  newMatrix[i * totalSize + j] = 0;
                }
              }
        }

#ifdef MEMDEBUG
        printFree((void*) currSol->matrix, 25);
#endif
        free(currSol->matrix);
        currSol->matrix = newMatrix;

        currLink = currLink->nextLink;
    }
}
