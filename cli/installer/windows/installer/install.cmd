@echo off
cd %~dp0
xcopy /s/i "%~dp0\fgit" "%programfiles%\fgit"
echo Please accept the upcoming Prompts to install successfully
setx path "%path%;%programfiles%\fgit"
PAUSE