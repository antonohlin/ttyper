mkdir nativebuild

docker build -f Dockerfile.build . --tag ttyper/build

docker run --mount type=bind,src=./nativebuild,dst=/output --mount type=bind,src=./build/libs/,dst=/input  ttyper/build

