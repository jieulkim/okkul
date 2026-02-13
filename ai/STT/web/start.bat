@echo off
echo Starting web server...
cd /d "%~dp0"
start http://localhost:8080
python -m http.server 8080
pause
