#include <stdio.h>
#include <stdlib.h>
#include <string.h>

typedef int bool;

typedef struct Link
{
    struct Link* prevLink;
    void* valuePtr;
    struct Link* nextLink;
} Link;

typedef struct Atom
{
    int bondCount;
    bool isSolitary;
    char name[2];
    double electroneg;
    Link* bondList;
    int totalBondCount;
    int listIndex;
} Atom;

typedef struct Pair
{
    char* name;
    int bondWeight;
} Pair;

Atom* createAtom(int bondCount, char name[2], double electroneg, int listIndex);
void loadAtom(int bondCount, char name[2], double electroneg, int listIndex, Atom* atom);

void iterator(Atom* atomList, int atomListSize, Link* currAtomVisit, Link* solutionList);

void printMol(Atom* atomList, int atomListSize);
void printMolMatrix(Atom* atomList, int atomListSize);
void printSolMatrix(int* solution, int atomListSize);

int* saveSolution(Atom* atomList, int atomListSize);

bool compareSolMatrix(Atom* atomList, int* a, int* b, int atomListSize);
bool compareString(char* a, char* b);
char* appendString(char* a, char* b, int sizeA, int sizeB);
bool isSmallerThan(Pair* a, Pair* b);
bool isSmallerString(char* a, char* b);
bool comparePairList(Pair* a, Pair* b, int listSize);

void sortPairs(Pair* pairList, int listSize);

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

void loadAtom(int bondCount, char name[2], double electroneg, int listIndex, Atom* atom)
{
    atom->bondCount = bondCount;
    atom->totalBondCount = bondCount;
    atom->isSolitary = 0;
    Link* startLink = malloc(sizeof(Link));
    startLink->prevLink = NULL;
    startLink->nextLink = NULL;
    startLink->valuePtr = NULL;
    atom->bondList = startLink;
    atom->electroneg = electroneg;
    atom->name[0] = name[0];
    atom->name[1] = name[1];
    atom->listIndex = listIndex;
}

Atom* createAtom(int bondCount, char name[2], double electroneg, int listIndex)
{
    Atom* returnAtom = malloc(sizeof(Atom));

    loadAtom(bondCount, name, electroneg, listIndex, returnAtom);

    return returnAtom;
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

bool compareSolMatrix(Atom* atomList, int* a, int* b, int atomListSize)
{
    /*//check if they are the same, without permutations
    for (int i = 0; i < atomListSize*atomListSize; i++)
        if (a[i] != b[i])
            return 0;
    */

    bool* isDisabled = calloc(atomListSize, sizeof(bool));

    Pair* aPairList = malloc(atomListSize * sizeof(Pair));
    Pair* bPairList = malloc(atomListSize * sizeof(Pair));

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
        return 0;
      }
    }

    free(isDisabled);
    free(aPairList);
    free(bPairList);

    return 1;
}

// Must be null terminated
bool compareString(char* a, char* b)
{
	int i;
	for (i = 0; a[i] != '\0' && b[i] != '\0'; i++)
		if (a[i] != b[i])
			return 0;

	//one must have caught and been equal to '\0'
	//thus if they both are equal to '\0', they are equal
	if (a[i] == b[i])
		return 1;

	return 0;
}

char* appendString(char* a, char* b, int sizeA, int sizeB)
{
    char* returnStr = calloc(sizeA + sizeB, sizeof(char));
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
