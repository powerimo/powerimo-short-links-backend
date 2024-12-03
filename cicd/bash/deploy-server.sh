#!/usr/bin/env bash

CONTAINER_NAME=$1
IMAGE_NAME=$2
APP_ENV=$3

# log
echo "Starting container ${CONTAINER_NAME} based on the image ${IMAGE_NAME}"
echo "env: ${APP_ENV}"

# read local vars
echo "Sourcing vars..."
# shellcheck disable=SC1090
source ~/config/"${CONTAINER_NAME}"/vars

echo "Stop previous launched container"
docker stop "${CONTAINER_NAME}" > /dev/null
docker rm "${CONTAINER_NAME}" > /dev/null
docker pull "${IMAGE_NAME}"

echo "Starting the container..."
docker run -d --restart unless-stopped \
    --name="${CONTAINER_NAME}" \
    -e SPRING_DATASOURCE_URL="${SPRING_DATASOURCE_URL}" \
    -e SPRING_DATASOURCE_USERNAME="${SPRING_DATASOURCE_USERNAME}" \
    -e SPRING_DATASOURCE_PASSWORD="${SPRING_DATASOURCE_PASSWORD}" \
    -e APP_ENV="${APP_ENV}" \
    -e APP_BASE_URL="${APP_BASE_URL}" \
    -p 15520:8080 \
    "${IMAGE_NAME}"

echo "Container has been started"