@echo off
cd %~dp0
xcopy /s/i "%~dp0\fgit" "C:\Users\Public\fgit"
echo Please accept the upcoming Prompts to install successfully
"%~dp0\fgit.reg" 
setx path "%path%;C:\Users\Public\fgit"
PAUSE