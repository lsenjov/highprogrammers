#!/bin/bash

unameOut="$(uname -s)"
case "${unameOut}" in
    Linux*)     bin/format/install-zprint-linux.sh;;
    Darwin*)    bin/format/install-zprint-mac.sh;;
    CYGWIN*)    echo "Not implemented for Cygwin";;
    MINGW*)     echo "Not implemented for MinGw";;
    *)          echo "Not implemented for Unknown: ${unameOut}"
esac
