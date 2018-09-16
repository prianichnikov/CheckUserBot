package info.prianichnikov.telegram.bot.checkuserbot;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.telegram.telegrambots.ApiContextInitializer;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.exceptions.TelegramApiRequestException;

public class CheckUserBotApplication {

    private static final Logger LOG = LogManager.getLogger(CheckUserBotApplication.class.getName());

    public static void main(String[] args) {

        ApiContextInitializer.init();
        TelegramBotsApi api = new TelegramBotsApi();
        try {
            CheckUserBot bot = CheckUserBot.getInstance();
            bot.checkEnvironmentVariables();
            api.registerBot(new CheckUserBot());
            LOG.info(bot.getBotUsername() + " is online");
        } catch (TelegramApiRequestException e) {
            LOG.error("Error during registering bot", e);
        } catch (TelegramApiException e) {
            LOG.error("Error during starting bot", e);
        }
    }
}
