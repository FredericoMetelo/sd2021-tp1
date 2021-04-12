# SD Classes

## Table Of Contents
* [Maven](#maven)
* [Docker](#docker)
* [REST](/lab-2/)
    - [Problems](/lab-3/)
## Some Notes
POM.XML -> We Need to Change the \<authors\> Section with our Student Numbers !

## <a name='maven'/>Maven
### Relevant Commands

``` bash
mvn clean
mvn compile
mvn assembly:single
mvn docker:build
```
We can run all at once, by doing `mvn clean compile assembly:single docker:build`<br>
It is also possible to build the container image using the docker build command `docker build -t <name> <dir_of_dockerfile>`<br>
See more details on [docker](#docker)

|   **Command**    |    **Meaning**     |
| ---- | ---- |
| **Clean** | Remove generated files |
| **Compile** | Compiles the project |
| **assembly:single** | Creates a single file with all compiled classes and dependencies |
| **docker:build** | Builds a docker image using the Dockerfile in the current directory |

## <a name="docker"/> Docker

### Docker Run
`docker run <options> <image> <cmd>`

**To Start an Image and run the default Command**
``` bash
docker run <image_name>
```
**To start an Image, but run an alternative command - e.g the bash**
``` bash
docker run -it <image_name> /bin/bash
```

**To start a container, specifying the hostname, name and network:**
```bash
    docker run -h <hostname> --name <name> --network <network> <image>
```

#### Docker Run Options

|   **Options**    |    **Meaning**     |
| --- | --- |
| **\-it** | Run docker in **Interactive Mode** |
| **\-\-name \<name\>** | Change Container Name |
| **\-h \<hostname\>** | Change Container HostName <br> See [Networking](#docker_n) |
| **\-\-network \<network\>** | Select Network to connect <br> See [Networking](#docker_n) |
| **\-p *portIN*:*portOut*** | Open Port to communicate with the container from outside |

---

### <a name="docker_n"/> Docker Networking
Each container is assigned an IP and a hostName. The hostname is only known locally and it can be changed using the *\-h* option:
``` bash
docker run -h <hostname> <image>
```
<br>

It is possible to create a bridge network that connect containers in a machine with **hostname resolution**.<br>
To create a bridged network, run : `docker network create -d bridge <networkName>`

---

### Some Useful Commands

| **Commands** | **Meaning** |
| --- | --- |
| `ps -a` | List all Containers (running and stopped) |
| `exec -it <container> <cmd>` | Executes a command in a running image |
| `logs -f <container>` | Fetch the logs of a running container (\-f option keeps connected) |
| `kill <container>` | Kills one or more containers |
| `rm <container>` | Cleans up one or more exited containers |
| `system prune` | Cleans up **all unused data** |
| `images` | List Docker Images |

---

### DockerFile

| **Command** | **Meaning** | 
| --- | --- |
| **FROM \<image\>** | Defines the image that will be used |
| **WORKDIR \<dir\>**| Defines the directory do be used in the following instructions |
| **COPY \<from\> \<to\>** | Copies the file to the docker image on *dir/to* |
| **CMD \<\[\"arg1\",\"arg2\", ... \]\>** | Defines the program that will run by default
