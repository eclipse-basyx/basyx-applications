# Base image
FROM node:lts-alpine

# Get the environment variables from the build arguments
ARG VITE_AAS_DISCOVERY_PATH
ARG VITE_AAS_ REGISTRY_PATH
ARG VITE_SUBMODEL_REGISTRY_PATH
ARG VITE_AAS_SERVER_PATH
ARG VITE_PRIMARY_COLOR
ARG VITE_BASE_PATH=""
ENV BASE_PATH=$VITE_BASE_PATH

# Set working directory
WORKDIR /app

# Copy the package.json and yarn.lock files
COPY package.json yarn.lock ./

# Install dependencies
RUN yarn install

# Copy project files
COPY . .

# Write the environemt variables into the .env file
RUN printf "VITE_AAS_DISCOVERY_PATH=${VITE_AAS_DISCOVERY_PATH}\nVITE_AAS_REGISTRY_PATH=${VITE_AAS_REGISTRY_PATH}\nVITE_SUBMODEL_REGISTRY_PATH=${VITE_SUBMODEL_REGISTRY_PATH}\nVITE_AAS_SERVER_PATH=${VITE_AAS_SERVER_PATH}\nVITE_PRIMARY_COLOR=${VITE_PRIMARY_COLOR}" > /app/.env

# Start dev server
CMD ["yarn", "dev", "--host", "0.0.0.0"]
