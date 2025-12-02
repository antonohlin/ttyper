#!/bin/sh

run_path="$(cd "$(dirname "$0")" && pwd)/run.sh"

INSTALL_DIR="$HOME/.local/bin"

case ":$PATH:" in
*":$INSTALL_DIR:"*) ;;
*) echo "Warning: $INSTALL_DIR is not in your PATH." ;;
esac

mkdir -p "$INSTALL_DIR"
ln -sf "$run_path" "$INSTALL_DIR/ttyper"

echo "Installed symlink: $INSTALL_DIR/ttyper"
