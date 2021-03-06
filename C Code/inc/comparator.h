#ifndef COMPARATOR
#define COMPARATOR

typedef struct Pair
{
    int atomicNumber;
    int bondWeight;
} Pair;

bool isIsomorph(Atom* atomList, int* a, int* b, int atomListSize);
void reduceMatrix(Atom* atomList, int* matrix, int atomListSize);
void swapRowCol(int* matrix, int row1, int row2, int atomListSize);

bool compareSolMatrix(Atom* atomList, int* a, int* b, int atomListSize);
bool compareString(char* a, char* b);
char* appendString(char* a, char* b, int sizeA, int sizeB);
bool isSmallerThan(Pair* a, Pair* b);
bool isSmallerString(char* a, char* b);
bool comparePairList(Pair* a, Pair* b, int listSize);

void sortPairs(Pair* pairList, int listSize);

int getCharge(Atom* atomList, int atomListSize);
int getScore(Atom* atomList, int* temp, int atomListSize, int loopSize);
bool getLoop(int* temp, int atomListSize, int* prevRows, int currRow, int currIndx, int* startIndx, int* endIndx);

#endif
