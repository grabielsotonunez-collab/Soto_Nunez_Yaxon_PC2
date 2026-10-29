@echo off
cd /d "%~dp0"
if not exist "config\database.properties" (
  copy "config\database.properties.example" "config\database.properties" >nul
  echo Completa tu clave MySQL en config\database.properties y vuelve a ejecutar.
  notepad "config\database.properties"
  exit /b 1
)
if exist "Soto_Nunez_Yaxon_PC2.jar" (
  java -jar "Soto_Nunez_Yaxon_PC2.jar"
) else (
  java -jar "target\Soto_Nunez_Yaxon_PC2.jar"
)
if errorlevel 1 pause
