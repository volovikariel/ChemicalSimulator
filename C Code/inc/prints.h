#ifndef PRINTS
#define PRINTS

void printMol(Atom* atomList, int atomListSize);
void printMolMatrix(Atom* atomList, int atomListSize);
void printSolMatrix(Solution* solution, int atomListSize);

void printSolution(Solution* solution, int atomListSize, int metalListSize);

#endif
