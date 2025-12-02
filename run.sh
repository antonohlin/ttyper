#!/bin/sh

run_path="$0"
while [ -L "$run_path" ]; do
    link_target="$(readlink "$run_path")"
    case "$link_target" in
    /*) run_path="$link_target" ;;
    *) run_path="$(dirname "$run_path")/$link_target" ;;
    esac
done
run_path="$(cd "$(dirname "$run_path")" && pwd)/$(basename "$run_path")"
run_directory="$(dirname "$run_path")"

build=0
install=0

show_help() {
    echo "./run.sh to run"
    echo "./run.sh -b to build and run"
    echo "./run.sh -i to install"
    echo "./run.sh -v or --version to print version"
}

# Check for long version flag before getopts parses it
for arg in "$@"; do
    if [ "$arg" = "--version" ]; then
        long_version_flag=true
        break
    fi
done

while getopts ":h?biv" opt; do
    case "$opt" in
    h)
        show_help
        exit 0
        ;;
    b)
        build=1
        ;;
    i)
        install=1
        ;;
    v)
        # Handled in ArgumentManager
        ;;
    *)

        if [ "$long_version_flag" = true ]; then
            break
        fi
        echo "Error: Unknown option '-$OPTARG'"
        show_help
        exit 1
        ;;
    esac
done

if [ $build -eq 1 ]; then
    ./gradlew uberJar --console=plain || exit 1
fi

if [ $install -eq 1 ]; then
    "$run_directory/install.sh"
fi

java --enable-native-access=ALL-UNNAMED -jar "$run_directory/build/libs/ttyper.jar" "$@"
