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
    echo "./run.sh -v to print version"
    echo "./run.sh -s <seed> to provide game seed"
}


filtered_arguments=()

for argument in "$@"; do
    case "$argument" in
        --*)
            # ignore long flags
            ;;
        *)
            filtered_arguments+=("$argument")
            ;;
    esac
done

set -- "${filtered_arguments[@]}"
while getopts ":hbi" opt; do
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
    *)
        # invalid options are ignored as they could be valid in-game options. Handle in ArgumentManager.
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
