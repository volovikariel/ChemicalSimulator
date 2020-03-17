#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include "../inc/atoms.h"
#include "../inc/csvParser.h"
#include "../inc/memory.h"

Element* loadTable(char* filepath)
{
    FILE *fp;
    char str[MAXCHAR];
    memset(str, '\0', MAXCHAR * sizeof(char));

    Element* table = calloc(TABLESIZE, sizeof(Element));
#ifdef MEMDEBUG
    printMalloc((void*) table, sizeof(Element) * TABLESIZE, 19);
#endif

    fp = fopen(filepath, "r");
    if (fp == NULL){
        printf("Could not open file %s!\n",filepath);
        return NULL;
    }

    int rowCount = 0;
    while (fgets(str, MAXCHAR, fp))
    {
        if (!rowCount)
        {
            rowCount++;
            continue;
        }

        char* field = strtok(str, ",");

        //getName(field, (char*) &(table[rowCount - 1].name));
        strcpy((char*) &(table[rowCount - 1].name), field);
        field = strtok(NULL, ",");

        table[rowCount - 1].bond = atoi(field);

        field = strtok(NULL, ",");

        table[rowCount - 1].elecNeg = atof(field);
        field = strtok(NULL, ",");

        table[rowCount - 1].isMetal = atoi(field);
        field = strtok(NULL, ",");

        if (!table[rowCount - 1].isMetal)
        {
          if (rowCount < 3)
              table[rowCount - 1].bond = 2 - table[rowCount - 1].bond;
          else
              table[rowCount - 1].bond = 8 - table[rowCount - 1].bond;
        }

        table[rowCount - 1].atomicNumber = rowCount;
        field = strtok(NULL, ",");

        if (field != NULL)
        {
            printf("parsing error at line %d, too many arguments", rowCount);
            return NULL;
        }

#ifdef CSVDEBUG
        printf("row %d: %s, %d, %f, %d\n", rowCount, table[rowCount - 1].name,
            table[rowCount - 1].bond, table[rowCount - 1].elecNeg, table[rowCount - 1].isMetal);
#endif

        rowCount++;
    }

    fclose(fp);
    return table;
}

void clearTable(Element* table)
{
#ifdef MEMDEBUG
    printFree((void*) table, 19);
#endif
    free(table);
}

void getName(char* str, char* save)
{
    bool reachedEnd = 0;
    for (int i = 0; i < 3 && !reachedEnd; i++)
    {
        if (str[i] == '\0')
            reachedEnd = 1;

        save[i] = str[i];
    }
}
