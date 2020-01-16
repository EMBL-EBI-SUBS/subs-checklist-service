#!/bin/bash

./gradlew clean build -x test

mv ./build/libs/*.jar ./docker/subs-checklist-service.jar

cp ./data-definitions/tools/ena-checklists-to-usi/convert_to_usi.pl ./docker

docker-compose down

docker-compose up --build -d
