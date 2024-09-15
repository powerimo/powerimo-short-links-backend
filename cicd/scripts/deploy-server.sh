#!/bin/bash

ENVIRONMENT=$1

if [ "$ENVIRONMENT" == "QA" ]; then
    echo "Deploying to QA environment"
    # QA deployment steps
elif [ "$ENVIRONMENT" == "PROD" ]; then
    echo "Deploying to PROD environment"
    # PROD deployment steps
    DC_NAME="powerimo-sl-server-prod"

    # shellcheck disable=SC1090
    source ~/config/powerimo-sl-vars-prod

    docker stop ${DC_NAME} > /dev/null
    docker container rm ${DC_NAME} > /dev/null
    # shellcheck disable=SC2153
    docker pull "${DI_NAME}"

    docker run -d --restart unless-stopped \
        --name=${DC_NAME} \
        -e SPRING_DATASOURCE_URL="${SPRING_DATASOURCE_URL}" \
        -e SPRING_DATASOURCE_USER="${SPRING_DATASOURCE_USER}" \
        -e SPRING_DATASOURCE_PASSWORD="${SPRING_DATASOURCE_PASSWORD}" \
        -e APP_ENV="prod" \
        -p 15500:8080 \
        "${DI_NAME}"
else
    echo "Unknown environment: $ENVIRONMENT"
    exit 1
fi