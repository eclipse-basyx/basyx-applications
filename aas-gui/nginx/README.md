# How to use the AAS Web UI with NGINX

## User Story

Let's say you want to host the AAS Web UI using NGINX.
You also want to have the application running in a subpath like this: `http://localhost/aasui/`.

## NGINX Configuration

This folder contains a sample configuration for NGINX.

> default.conf

> proxy.conf

## Docker (Compose) Configuration

Add this to your docker-compose.yml:

```yaml
aas-web-gui:
    image: eclipsebasyx/aas-gui
    container_name: aasui
    environment:
        VITE_BASE_PATH: "<base_path>"
    restart: always
```

Here you can replace `<base_path>` with your desired subpath (for example **aasui**).

This folder also includes a sample docker-compose.yml file.
There you can find a docker configuration for the AAS Web UI and NGINX.

When using the sample cinfiguration, you can start the application by executing `docker-compose up -d`.
The application will be available at `http://localhost/aasui/`.