@echo off
echo Opening in browser...
cd /d "%~dp0"
start chrome.exe "file:///%CD%\index.html" 2>nul
start msedge.exe "file:///%CD%\index.html" 2>nul
start "" "file:///%CD%\index.html"
echo.
echo If browser didn't open, manually open: index.html
echo.
pause
