#ifndef PRINTS
#define PRINTS

#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include "atoms.h"
#include "comparator.h"

void printMol(Atom* atomList, int atomListSize);
void printMolMatrix(Atom* atomList, int atomListSize);
void printSolMatrix(int* solution, int atomListSize);

#endif
