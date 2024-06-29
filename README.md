# cardioTelegramBot

This telegram bot intends to promote information about cardiology.

## Instructions

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
3. Start your application with ```restartDockerImage``` gradle custom task.

### To deploy the server proceed with the following steps:

1. Execute ```buildDeployDockerImage``` gradle task.
2. Push (through Docker Desktop) produced image to the DockerHub.
3. Get connected to the VPS (e.g. through Termius).
4. Execute the sequence of the following commands:
   1. ```sudo apt update && sudo apt upgrade```
   2. Proceed with this documentation: https://docs.docker.com/engine/install/ubuntu/
   3. ```docker login```
   4. ```mkdir app && cd app```
5. Copy ```compose.yaml``` and ```.env``` files to this folder (e.g. through Termius SFTP).
6. Add to the beginning of the image name ```almaxgood/``` in ```compose.yaml``` file.
7. Start docker container with ```docker compose up -d``` command.

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
