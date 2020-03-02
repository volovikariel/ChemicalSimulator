#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include "../inc/atoms.h"

#include "../inc/memory.h"

void loadAtom(int bondCount, char name[2], double electroneg, int listIndex, Atom* atom)
{
    atom->bondCount = bondCount;
    atom->totalBondCount = bondCount;
    atom->isSolitary = 0;

    Link* startLink = malloc(sizeof(Link));
    startLink->prevLink = NULL;
    startLink->nextLink = NULL;
    startLink->valuePtr = NULL;
#ifdef MEMDEBUG
    printMalloc((void*) startLink, sizeof(Link), 12);
#endif

    atom->bondList = startLink;
    atom->electroneg = electroneg;
    atom->name[0] = name[0];
    atom->name[1] = name[1];
    atom->listIndex = listIndex;
}

Atom* createAtom(int bondCount, char name[2], double electroneg, int listIndex)
{
    Atom* returnAtom = malloc(sizeof(Atom));
#ifdef MEMDEBUG
    printMalloc((void*) returnAtom, sizeof(Atom), 13);
#endif

    loadAtom(bondCount, name, electroneg, listIndex, returnAtom);

    return returnAtom;
}