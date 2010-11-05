@echo off
setlocal

set BGEXE=..\..\Binaries\Boogie.exe
rem set BGEXE=mono ..\..\Binaries\Boogie.exe

echo ----- Running regression test t1.bpl
%BGEXE% %* /noinfer /stratifiedInline:1 /extractLoops /removeEmptyBlocks:0 /coalesceBlocks:0 t1.bpl
echo -----
echo ----- Running regression test t2.bpl
%BGEXE% %* /noinfer /stratifiedInline:1 /extractLoops /removeEmptyBlocks:0 /coalesceBlocks:0 t2.bpl
echo -----

