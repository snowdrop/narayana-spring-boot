current_version=$(mvn help:evaluate -Dexpression=project.version -q -DforceStdout)
echo "Current version: $current_version"
base_version=$(echo $current_version | sed 's/-.*//')
IFS='.' read -r major minor patch <<< "$base_version"
next_patch=$((patch + 1))
next_version="${major}.${minor}.${next_patch}-SNAPSHOT"
echo "Next version: $next_version"

mvn versions:set -DnewVersion=$next_version
mvn versions:commit

git config user.name "github-actions"
git config user.email "github-actions@github.com"
git commit -am "chore: bump version to $next_version"
git push origin HEAD:main