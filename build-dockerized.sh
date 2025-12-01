docker run --mount type=bind,src=./,dst=/project -w /project gradle:jdk25 gradle --no-daemon uberJar
