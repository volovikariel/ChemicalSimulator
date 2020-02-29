#ifndef ATOMS
#define ATOMS

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

#endif
