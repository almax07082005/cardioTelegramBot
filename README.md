# cardioTelegramBot

This telegram bot intends to promote information about cardiology.

## Instructions

#### !!CHANGE DOCKER_TAG GITHUB VARIABLE FOR NEW PRODUCTION VERSION!!

### To debug docker container

To debug application in docker container just proceed with the following steps:

1. Open port ```5005``` in ```compose.yaml``` file.
2. Change JVM Remote Debug host to current ip address.

### To build the server proceed with the following steps:

1. Clone this repo to your local machine with the command: ```git clone https://github.com/almax07082005/cardioTelegramBot.git```.
2. Add ```.env``` with the following variables:
   1. ```POSTGRES_USER```
   2. ```POSTGRES_PASSWORD```
   3. ```MAIN_TOKEN```
   4. ```LOGGER_TOKEN```
   5. ```GUIDE```
   6. ```DOCKER_TAG```
3. Start your application with ```restartDockerImage``` gradle custom task.

### To deploy the server proceed with the following steps:

1. Setup VM
2. Install Docker (https://docs.docker.com/engine/install/ubuntu/)
3. Login to Docker Hub
4. Create directory with the name given in CD.yml
5. Copy there compose.yaml
6. Change image name in compose.yaml file
7. Update GitHub secrets and variables

### Transfer docker volume between VMs

1. ```docker run --rm -v <your_volume_name>:/volume -v $(pwd):/backup busybox tar czvf /backup/backup.tar.gz -C /volume .```
2. ```scp backup.tar.gz user@<destination_vm>:</path/to/destination>```
3. ```docker volume create <new_volume_name>```
4. ```docker run --rm -v <new_volume_name>:/volume -v </path/to/destination>:/backup busybox tar xzvf /backup/backup.tar.gz -C /volume```

## Add button

1. Add name of the button to ```Buttons``` enum.
2. Create private method with ```Runnable``` structure in ```Button``` class.
3. Put new element in HashMap for ```BotService``` to be able to call this method.
4. Do not forget to create this button on the front side, if you need.

## Add Command

1. Add name of the button to ```Commands``` enum.
2. Create private method with ```Runnable``` structure in ```Command``` class.
3. Put new element in HashMap for ```BotService``` to be able to call this method.
4. Do not forget to add this command through BotFather, if you need.
