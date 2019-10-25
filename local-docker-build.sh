#!/bin/bash

IMAGE_NAME='subs-checklist-service'
CONTAINER_NAME=$IMAGE_NAME

./gradlew clean assemble

mv ./build/distributions/*.tar ./docker/subs-checklist-service.tar

docker stop $CONTAINER_NAME

docker rm -vf $CONTAINER_NAME

docker build -t $IMAGE_NAME ./docker

docker create --name $CONTAINER_NAME $IMAGE_NAME

docker start $CONTAINER_NAME
