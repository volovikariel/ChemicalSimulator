@echo off
cd ../MainApplication
:loop
set /p atomList="Enter Atoms: "
echo Trying %atomList%
echo vvvvvvvvvvv
b.exe %atomList%
set /p retry="Do you want to try another? (Y/N) "
if '%retry%'=='Y' goto loop
