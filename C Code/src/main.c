#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include "../inc/atoms.h"
#include "../inc/prints.h"
#include "../inc/comparator.h"
#include "../inc/csvParser.h"

#include "../inc/memory.h"

void iterator(Atom* atomList, int atomListSize, Link* currAtomVisit, Link* solutionList);
void attempt(Atom* atomList, int atomListSize, Link* currAtomVisit, Link* solutionList, Atom* currAtom);
void ionizeSolutions(Link* solutionList, Atom* metalList, int metalListSize);

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

    //ionizeSolutions(solLink, metalList, sizeIonic);

    //print all the solutions
    //start with the list of atoms
    printf("+ ");
    for (int i = 0; i < sizeCovalent; i++)
      printf("%s ", atomList[i].name);
    printf("\n- ");
    for (int i = 0; i < sizeIonic; i++)
      printf("%s ", metalList[i].name);

    printf("\n");

    //print the solutions
    Link* currLink;

    //skip first empty entry
    currLink = solLink->nextLink;
    while (currLink != NULL)
    {
        printSolMatrix(((Solution*) currLink->valuePtr), sizeCovalent);
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
            printFree(((Solution*) currLink->valuePtr)->matrix, 2);
#endif
            free(((Solution*) currLink->valuePtr)->matrix);
// #ifdef MEMDEBUG
//             printFree(((Solution*) currLink->valuePtr)->ionRatios, 2);
// #endif
//             free(((Solution*) currLink->valuePtr)->ionRatios);
#ifdef MEMDEBUG
            printFree(currLink->valuePtr, 2);
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
    //see if any are solitary
    bool noSolitaryExist = 1;
    for (int i = 0; i < atomListSize; i++)
        noSolitaryExist *= atomList[i].totalBondCount - atomList[i].bondCount;

    //if no solitary atom exists
    if (noSolitaryExist || atomListSize == 1)
    {
        //save the solution
        int* temp = saveSolution(atomList, atomListSize);
        //reduceMatrix(atomList, temp, atomListSize);
        //compare with previous solutions
        //skip first empty link
        Link* tempStartLink = solutionList->nextLink;
        Link* saveBeforeLast = solutionList;
        bool isEqual = 0;
        while (tempStartLink != NULL && !isEqual)
        {
            isEqual = compareSolMatrix(atomList, temp, (int*) ((Solution*) tempStartLink->valuePtr)->matrix, atomListSize);
            //isEqual = isIsomorph(atomList, temp, (int*) tempStartLink->valuePtr, atomListSize);
            saveBeforeLast = tempStartLink;
            tempStartLink = tempStartLink->nextLink;
        }
        //if no similar solution was found
        if (!isEqual)
        {
            //printSolMatrix(temp, atomListSize);
            Link* newSolLink = malloc(sizeof(Link));
            newSolLink->prevLink = saveBeforeLast;
            Solution* newSol = malloc(sizeof(Solution));
            newSol->matrix = temp;
            newSol->overallCharge = getCharge(atomList, atomListSize);
            newSol->score = newSol->overallCharge;
            //newSol->ionRatios = NULL;
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
        //if a similar soluion was found, then following tree will be the same, so return
        else
        {
#ifdef MEMDEBUG
          printFree(temp, 18);
#endif
          free(temp);

          return;
        }
    }

    Link* currAtomLink = currAtomVisit;
    Atom* currAtom = NULL;

    //for each occurence of an atom with bonds left, select it (bonds left now means within octet)
    while (currAtomLink != NULL)
    {
        currAtom = (Atom*) currAtomLink->valuePtr;

        //if currAtom has less than 4 bonds, can fit in octet
        int maxBonds = (currAtom->atomicNumber > 2) ? 4 : 1;
        if (currAtom->totalBondCount - currAtom->bondCount < maxBonds)
            attempt(atomList, atomListSize, currAtomVisit, solutionList, currAtom);

        currAtomLink = currAtomLink->nextLink;
    }
}

void attempt(Atom* atomList, int atomListSize, Link* currAtomVisit, Link* solutionList, Atom* currAtom)
{
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
      //if it is the same atom (no loops allowed)
      if (i == currAtom->listIndex)
          continue;

      //if other atom doesnt have bonds left
      int maxBonds = (atomList[i].atomicNumber > 2) ? 4 : 1;
      if (atomList[i].totalBondCount - atomList[i].bondCount == maxBonds)
          continue;

      //if other atom isnt connected to anything
      if (atomList[i].isSolitary == 1)
      {
          bool wasTried = 0;
          //skipping the last empty item
          Link* currItem = triedAtomsStart;
          //check to make sure the same atom type wasn't already tried
          while(currItem->nextLink != NULL && !wasTried)
          {
              if (atomList[i].name[0] == ((Atom*) currItem->valuePtr)->name[0])
                  wasTried = 1;

              currItem = currItem->nextLink;
          }

          //if wasn't already tried
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
          //if was already tried
          else
              continue;
      }

      /*printf(currAtom->name);
      printf("\t");
      printf(atomList[i].name);
      printf("\n");*/

      //update currentAtom's information
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

      //update this atom's information
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

      //recursively call this method
      iterator(atomList, atomListSize, newStart, solutionList);


      //remove this atom from the beginning of the currAtomVisit
#ifdef MEMDEBUG
      printFree((void*) currAtomVisit->prevLink, 7);
#endif
      free(currAtomVisit->prevLink);
      currAtomVisit->prevLink = NULL;

      //restore this atom's information
      atomList[i].bondList = atomList[i].bondList->nextLink;
#ifdef MEMDEBUG
      printFree((void*) atomList[i].bondList->prevLink, 8);
#endif
      free(atomList[i].bondList->prevLink);
      atomList[i].bondList->prevLink = NULL;
      atomList[i].bondCount++;
      if (atomList[i].bondCount == atomList[i].totalBondCount)
          atomList[i].isSolitary = 1;

      //restore currentAtom's information
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

  //empty triedAtomsStart and remove
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

// void ionizeSolutions(Link* solutionList, Atom* metalList, int metalListSize)
// {
//     //go through every solution
//     Link* currLink;
//     //skip the first empty entry
//     currLink = solutionList->nextLink;
//
//     Solution* currSol;
//     int* ionRatios;
//
//     while (currLink != NULL)
//     {
//         //create an array of integers
//         currSol = (Solution*) currLink->valuePtr;
//         ionRatios = calloc(metalListSize, sizeof(int));
// #ifdef MEMDEBUG
//         printMalloc((void*) ionRatios, metalListSize * sizeof(int), 25);
// #endif
//
//         for (int i = 0; i < metalListSize; i++)
//           ionRatios[i] = metalList[i].totalBondCount;
//
//         currSol->ionRatios = ionRatios;
//     }
// }
