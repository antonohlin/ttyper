#!/bin/sh

build=0

show_help() {
  echo "./run.sh to run"
  echo "./run.sh -b to build and run"
  echo "./run.sh -v or --version to print version"
}

while getopts ":h?b" opt; do
  case "$opt" in
    h)
      show_help
      exit 0
      ;;
    b)  build=1
      ;;
    *)
      # invalid options are ignored as they could be valid in-game options. Handle in ArgumentManager.
      ;;
  esac
done

if [ $build -eq 1 ]; then
  ./gradlew --no-daemon uberJar --console=plain
fi

java --enable-native-access=ALL-UNNAMED -jar build/libs/ttyper.jar $@
