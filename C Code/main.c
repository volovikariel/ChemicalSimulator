#include <stdio.h>
#include <stdlib.h>

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

Atom* createAtom(int bondCount, char name[2], double electroneg, int listIndex);
void loadAtom(int bondCount, char name[2], double electroneg, int listIndex, Atom* atom);

void iterator(Atom* atomList, int atomListSize, Link* currAtomVisit, Link* solutionList);

void printMol(Atom* atomList, int atomListSize);

bool compareString(char* a, char* b);

int solCount = 0;

int main(int argc, char *argv[])
{
	//example input "---.exe H 2 O 1"

  char names[][2] = {"H\0", "C\0", "N\0", "O\0"};
  int HCNO[4];
  int maxElement = 4;
  /*//read input
  printf("Enter the atom composition\n");
  for (int i = 0; i < 4; i++)
  {
      printf("%s: ", names[i]);
      scanf_s("%d", &(HONC[i]));
  }*/


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

    Atom* atomList = malloc(size * sizeof(Atom));

    int currOffset = 0;
    for (int k = 0; k < 4; k++)
    {
        for (int i = currOffset; i < currOffset + HCNO[k]; i++)
            loadAtom(k+1, names[k], 0, i, &(atomList[i]));
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
        startLink = currLink; //used here as a temp
        currLink = currLink->nextLink;
        free(startLink);
    }

    printf("%d solutions found", solCount);

    free(atomList);
	scanf("%s", NULL);
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

        //compare with previous solutions

        //add to solution list
        //printf("Found a solution\n");
        //printMol(atomList, atomListSize);
        solCount++;
        if (solCount % 1000000 == 0)
            printf("%d million\n", solCount / 1000000);

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
    printf("----Start of Solution----\n");
    for (int i = 0; i < atomListSize; i++)
    {
        printf("%c%d => ", atomList[i].name[0], atomList[i].listIndex);
        Link* currLink = atomList[i].bondList;
        Atom* currAtom;

        //currLink->nextLink is used since the last one is empty and thus should be ignored
        while(currLink->nextLink != NULL)
        {
            currAtom = (Atom*) currLink->valuePtr;
            printf(" %c%d", currAtom->name[0], currAtom->listIndex);

            currLink = currLink->nextLink;
        }
        printf("\n");
    }
    printf("-----End of Solution-----\n");
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
