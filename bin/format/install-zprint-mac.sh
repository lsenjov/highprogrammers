#!/bin/bash
# Install zprint and add it to a git hook

# Install zprint for formatting
curl https://github.com/kkinnear/zprint/releases/download/1.1.1/zprintm-1.1.1 -L --output bin/format/zprint && chmod +x bin/format/zprint

# Go add a pre-commit hook
FILE='.git/hooks/pre-commit'
# Make sure the file exists, and we can execute
touch $FILE
chmod +x $FILE
# Add this line to the file if it doesn't exist
# If it's commented out, it won't do anything
LINE='bin/format/git-pre-commit-mac.sh'
grep -qF -- "$LINE" "$FILE" || echo "$LINE" >> "$FILE"
