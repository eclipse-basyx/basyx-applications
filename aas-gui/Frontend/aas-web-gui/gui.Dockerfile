FROM node:lts-alpine

# Install serve
RUN npm install -g serve

# make the 'app' folder the current working directory
WORKDIR /app

# Copy the package.json and yarn.lock files
COPY package.json yarn.lock ./

# Install dependencies
RUN yarn install

# copy project files and folders to the current working directory (i.e. 'app' folder)
COPY . .

# Build the app
RUN yarn build

# Run the app
CMD ["serve", "-s", "dist"]
