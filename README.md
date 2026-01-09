A CLI typing test inspired by [MonkeyType](https://monkeytype.com/), currently supports Linux and Mac.

Release:  
Download Release 0.0.2 and chmod it to run. If on mac run: 'xattr -r -d com.apple.quarantine {filename}' to trust it.  

Clone:  
Checkout main, and use ./run.sh -i to perform an initial build and set up a symlink that enables 'ttyper' as a global shorthand.

    ./run.sh to run
    ./run.sh -b to build and run
    ./run.sh -i to install
    ./run.sh -v to print version
    ./run.sh -s <seed> to provide game seed
