#!/bin/bash
# Go format all the files

OPTIONS="$(cat bin/format/options.edn)"
REGEX='\.clj[cs]?$'

# shellcheck disable=SC2046
bin/format/zprint "$OPTIONS" -w $(find src | grep -E "${REGEX}") $(find env | grep -E "${REGEX}") $(find test | grep -E "${REGEX}")
