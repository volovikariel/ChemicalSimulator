#ifndef ATOMS
#define ATOMS

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
    char name[3];
    double electroneg;
    Link* bondList;
    int totalBondCount;
    int listIndex;
    int atomicNumber;
} Atom;

typedef struct Solution
{
    int* matrix;
    int overallCharge;
    int score;
    //int* ionRatios;
} Solution;

Atom* createAtom(int bondCount, char name[2], double electroneg, int listIndex, int atomicNumber);
void loadAtom(int bondCount, char name[2], double electroneg, int listIndex, int atomicNumber, Atom* atom);

#endif
