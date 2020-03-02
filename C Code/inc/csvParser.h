#ifndef CSVPARSER
#define CSVPARSER

#define TABLESIZE 118
#define MAXCHAR 100

typedef struct Element
{
  char name[3];
  int bond;
  bool isMetal;
  double elecNeg;
  int count;
} Element;

Element* loadTable(char* filepath);

void getName(char* str, char* save);

void clearTable(Element* table);

#endif
