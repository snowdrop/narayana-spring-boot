#!/bin/bash
set -e

#RELEASE_VERSION=$1
#
# if [[ "$RELEASE_VERSION" != *-SNAPSHOT ]]; then
#           echo "ERROR: RELEASE_VERSION must be a SNAPSHOT version. Current value: $RELEASE_VERSION"
#           exit 1
# fi
#
# echo "✅ Version $RELEASE_VERSION is valid."
echo "############################ Calculate next snapshot version"
current_version=$(mvn help:evaluate -Dexpression=project.version -q -DforceStdout)
echo "Current version: $current_version"
base_version=$(echo $current_version | sed 's/-.*//')
IFS='.' read -r major minor patch <<< "$base_version"
next_patch=$((patch + 1))
next_version="${major}.${minor}.${next_patch}-SNAPSHOT"
echo "Next version: $next_version"
echo "next_version=$next_version"

echo "############################ Create new branch"
git checkout -b bump-version-$next_version
echo "Base version: "main

echo "############################ Bump version and commit"
mvn versions:set -DnewVersion=$next_version -DgenerateBackupPoms=false
mvn versions:commit

echo "::group::Git diff"
git diff
echo "::endgroup::"

git config user.name "github-actions"
git config user.email "github-actions@github.com"

if [ -n "$(git status --porcelain)" ]; then
  echo "There are changes to commit."
  find . -name "pom.xml.versionsBackup" -delete
  git status
  find . -name pom.xml -exec grep -H "<version>" {} \;
  git add -A
  git commit -m "chore: bump version to $next_version"
else
  echo "⚠️ No changes detected after mvn versions:commit. Skipping commit."
fi

echo "############################ Debug show Git status"
git status

echo "############################ Push changes to new branch"
git pull --rebase origin main
git push origin bump-version-$next_version
