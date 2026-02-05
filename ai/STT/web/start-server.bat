@echo off
echo ========================================
echo   STT Web Server Starting...
echo ========================================
echo.
cd /d "%~dp0"
echo Current directory: %CD%
echo.
echo Starting Python HTTP server on port 8080...
echo.
echo Open your browser and go to:
echo   http://localhost:8080
echo.
echo Press Ctrl+C to stop the server
echo ========================================
echo.
python -m http.server 8080
