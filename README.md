# cardioTelegramBot

This telegram bot intends to promote information about cardiology.

To build the server proceed with the following steps:

1. Clone this repo to your local machine with the command: ```git clone https://github.com/almax07082005/cardioTelegramBot.git```.
2. Add ```hidden.properties``` file to ```resources``` folder with necessary content and ```spring.datasource``` variables in order to set up database credentials.
3. Add ```.env``` file to root folder with ```POSTGRES_USER``` and ```POSTGRES_PASSWORD``` variables for ```compose.yaml``` file.
4. Start your application with ```startDockerImage``` gradle custom task.

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
