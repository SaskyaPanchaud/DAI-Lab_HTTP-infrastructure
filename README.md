# DAI Lab - HTTP Infrastructure

## Step 1 - Static Web site

File *nginx.conf* explained :
```
server {
    # This tells Nginx on which port he should expect connections.
    listen 80;

    # This defines the root of our static web site on the machine.
    root /contentStaticWebSite;

    # This defines a new location (in this case, it will be the homepage, i.e. when the URL is <mywebsiteaddress>/).
    location / {
        # This tells Nginx which file to display.
        index index.html;
    }
}
```

Commands to use for demo :
```
# to build image
docker build -t <imageName> ./staticWebSite
# to run container
docker run -it -d -p 8080:80 --name <containerName> <imageName>
```

## Step 2 - Docker compose

File *docker-compose.yml* explained :
```
version: '3.8'

services:

static-web-site:
    build: ./staticWebSite
    image: http/site
    ports:
        - "8081:80"
```

Commands to use for demo :
```
# to build the infrastructure
docker compose build
# to run the infrastructure
docker compose up
# to stop the infrastructure
docker compose down
```


## Step 3 - HTTP API server

It's a basic API which manages quotes (content and author).
There isn't a database, but a list of quotes is stored in the QuoteController class.
This class has the following methods to handle the requests : getOne, getAll, create, delete and update (these all take a Javalin Context as a parameter).


## Step 4 - Reverse proxy with Traefik

Configuration of *docker-compose.yml* :
```
version: '3.8'

# add network
networks:
    traefik:
        name: traefik

services:
    static-web-site:
        build: ./staticWebSite
        image: http/site
        # add route for Traefik
        labels:
           - traefik.http.routers.static-web-site.rule=Host("localhost")

    http-api-server:
        build: ./httpApiServer
        image: http/api
        # add route for Traefik
        labels:
            - traefik.http.routers.http-api-server.rule=Host("localhost") && PathPrefix("/api")

    # add reverse-proxy service
    reverse-proxy:
        image: traefik:v2.10
        # accee to dashboard
        command: --api.insecure=true --providers.docker
        ports:
            # api and static site web
            - "80:80"
            # dashboard
            - 8080:8080
        volumes:
            # access to container's list
            - /var/run/docker.sock:/var/run/docker.sock
```

Reverse proxy :
A reverse proxy is useful to improve the security of the infrastructure because all querys go on it and after the reverse proxy redirects to the internal servers.

Traefik Dashboard :
The Traefik dashboard is accessed on port 8080. It works because we add *command: --api.insecure=true --providers.docker* in docker-compose file.

## Step 5 - Scalability and load balancing

To start the infrastructure with several instances of each server (static and dynamic), we add this lines (in this case, there will be 2 instances) :
```
    deploy:
      replicas: 2
```
To do it dynamic, we have used this command :
```
# one service
docker compose up --scale <service>=<nb instance>
# several services
docker service scale <service>=<nb instance> <service>=<nb instance>
```
Logs show that Traefik performs load balancing among the instances (but for api ?).

## Step 6 - Load balancing with round-robin and sticky sessions

We add this line for the api service :
```
 - traefik.http.services.api.loadbalancer.sticky.cookie=true
```

To validate this step, we add *System.out.println("-- NEW REQUEST --");* in QuoteController.getAll() to see in the logs which instance is called.

## Step 7 - Securing Traefik with HTTPS
