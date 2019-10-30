#!/bin/bash

./gradlew clean assemble

mv ./build/distributions/*.tar ./docker/subs-checklist-service.tar

cp ./data-definitions/tools/ena-checklists-to-usi/convert_to_usi.pl ./docker

docker-compose down

docker-compose up --build -d
