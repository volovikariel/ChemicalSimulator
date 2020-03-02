cd src
gcc main.c atoms.c comparator.c prints.c memory.c csvParser.c -o ../debug.exe -Wall -D MEMDEBUG -D CSVDEBUG
pause
