# prereq: run nativebuild.sh to have a binary to package

docker build -f Dockerfile.run . --tag ttyper/main && echo "play with 'docker run -it ttyper/main'"
