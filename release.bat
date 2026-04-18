@echo off
set /p RELEASE_VERSION="Enter release version (e.g. 1.0.0): "
set /p NEXT_VERSION="Enter next snapshot version (e.g. 1.0.1-SNAPSHOT): "

echo Setting release version %RELEASE_VERSION% ...
mvn versions:set -DnewVersion=%RELEASE_VERSION% -DgenerateBackupPoms=false

echo Committing release version ...
git commit -am "[release] set version %RELEASE_VERSION%"

echo Creating tag v%RELEASE_VERSION% ...
git tag v%RELEASE_VERSION%

echo Pushing tag to trigger deploy ...
git push origin v%RELEASE_VERSION%

echo Setting next snapshot version %NEXT_VERSION% ...
mvn versions:set -DnewVersion=%NEXT_VERSION% -DgenerateBackupPoms=false

echo Committing next snapshot version ...
git commit -am "[release] set version %NEXT_VERSION%"

echo Pushing changes ...
git push origin dev