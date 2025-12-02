#!/bin/sh

script_path="$0"
while [ -L "$script_path" ]; do
    link_target="$(readlink "$script_path")"
    case "$link_target" in
    /*) script_path="$link_target" ;;
    *) script_path="$(dirname "$script_path")/$link_target" ;;
    esac
done
script_path="$(cd "$(dirname "$script_path")" && pwd)/$(basename "$script_path")"
script_directory="$(dirname "$script_path")"

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
    ./gradlew uberJar --console=plain || exit 1

    INSTALL_DIR="$HOME/.local/bin"

    case ":$PATH:" in
    *":$INSTALL_DIR:"*) ;;
    *)
        echo "Warning: $INSTALL_DIR is not in your PATH. You won't be able to run 'ttyper' globally."
        ;;
    esac

    mkdir -p "$INSTALL_DIR"
    ln -sf "$script_directory/run.sh" "$INSTALL_DIR/ttyper"
    echo "Installed symlink: $INSTALL_DIR/ttyper"
fi

java --enable-native-access=ALL-UNNAMED -jar "$script_directory/build/libs/ttyper.jar" "$@"
