#!/bin/bash
# Get all the files currently being staged, go format them according to our options

OPTIONS="$(cat bin/format/options.edn)"

# Format all the clj[cs]? files
npx git-format-staged --formatter "bin/format/zprint '${OPTIONS}' " '*.clj' '*.cljc' '*.cljs'
# Format all our css/scss
npx git-format-staged --formatter 'prettier --stdin --stdin-filepath "{}"' "styles/**.scss" "styles/**.css"
