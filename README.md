# cardioTelegramBot

This telegram bot intends to promote information about cardiology.

## Instructions

### Reminder

When referral program is active, in case the docker container gets deleted and run again (not rerun of the container), it is obligatory to execute ```/startReferral``` command in the admin channel.

### To build the server proceed with the following steps:

1. Clone this repo to your local machine with the command: ```git clone https://github.com/almax07082005/cardioTelegramBot.git```.
2. Add ```hidden.properties``` file to ```resources``` folder with the following content:
   1. ```telegram.bot.token```
   2. ```telegram.guide.file```
   3. ```telegram.logger-bot.token```
   4. ```spring.datasource.username```
   5. ```spring.datasource.password```
3. Add ```.env``` file to root folder with ```POSTGRES_USER``` and ```POSTGRES_PASSWORD``` variables for ```compose.yaml``` file.
4. Start your application with ```startDockerImage``` gradle custom task.

### To deploy the server proceed with the following steps:

1. Execute ```buildDeployDockerImage``` gradle task.
2. Push (through Docker Desktop) produced image to the DockerHub.
3. Get connected to the VPS (e.g. through Termius).
4. Execute the sequence of the following commands:
   1. ```sudo apt update && sudo apt upgrade```
   2. Proceed with this documentation: https://docs.docker.com/engine/install/ubuntu/
   3. ```docker login```
   4. ```docker pull almaxgood/cardio-bot```
   5. ```mkdir app && cd app```
5. Copy ```compose.yaml``` file to this folder (through Termius SFTP).
6. Make the following changes in the ```compose.yaml``` file:
   1. Add to the beginning of the image name ```almaxgood/```.
   2. Change DB credentials to real ones.
7. Start docker container with ```docker compose up -d``` command.

## Change the bot

1. Create new bot through BotFather.
2. Set commands there as well.
3. Change ```telegram.bot.token``` variable in ```hidden.properties``` file.
4. Change all necessary variables in ```application.yml``` file.
5. Change link to the bot in GitHub repository page.

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
