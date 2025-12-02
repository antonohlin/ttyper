docker build -f Dockerfile.ssh . --tag ttyper/ssh

docker run --publish 2222:22 --env ROOT_PASSWORD=ttyper --volume /tmp:/root/.config --name ttyper --rm -it ttyper/ssh
ZZ
