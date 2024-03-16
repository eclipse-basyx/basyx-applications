#!/bin/sh

# Check if environment variables are set and update config.json accordingly
if [ -n "$LOGO_PATH" ]; then
    jq '.logoPath = env.LOGO_PATH' /usr/src/app/dist/config.json > /tmp/config.json && mv /tmp/config.json /usr/src/app/dist/config.json
fi

if [ -n "$AAS_DISCOVERY_PATH" ]; then
    jq '.aasDiscoveryPath = env.AAS_DISCOVERY_PATH' /usr/src/app/dist/config.json > /tmp/config.json && mv /tmp/config.json /usr/src/app/dist/config.json
fi

if [ -n "$AAS_REGISTRY_PATH" ]; then
    jq '.aasRegistryPath = env.AAS_REGISTRY_PATH' /usr/src/app/dist/config.json > /tmp/config.json && mv /tmp/config.json /usr/src/app/dist/config.json
fi

if [ -n "$SUBMODEL_REGISTRY_PATH" ]; then
    jq '.submodelRegistryPath = env.SUBMODEL_REGISTRY_PATH' /usr/src/app/dist/config.json > /tmp/config.json && mv /tmp/config.json /usr/src/app/dist/config.json
fi

if [ -n "$AAS_REPO_PATH" ]; then
    jq '.aasRepoPath = env.AAS_REPO_PATH' /usr/src/app/dist/config.json > /tmp/config.json && mv /tmp/config.json /usr/src/app/dist/config.json
fi

if [ -n "$SUBMODEL_REPO_PATH" ]; then
    jq '.submodelRepoPath = env.SUBMODEL_REPO_PATH' /usr/src/app/dist/config.json > /tmp/config.json && mv /tmp/config.json /usr/src/app/dist/config.json
fi

if [ -n "$CD_REPO_PATH" ]; then
    jq '.cdRepoPath = env.CD_REPO_PATH' /usr/src/app/dist/config.json > /tmp/config.json && mv /tmp/config.json /usr/src/app/dist/config.json
fi

if [ -n "$PRIMARY_COLOR" ]; then
    jq '.primaryColor = env.PRIMARY_COLOR' /usr/src/app/dist/config.json > /tmp/config.json && mv /tmp/config.json /usr/src/app/dist/config.json
fi

if [ -n "$BASE_PATH" ]; then
    jq '.basePath = env.BASE_PATH' /usr/src/app/dist/config.json > /tmp/config.json && mv /tmp/config.json /usr/src/app/dist/config.json
fi

# Start Nginx
exec nginx -g 'daemon off;'