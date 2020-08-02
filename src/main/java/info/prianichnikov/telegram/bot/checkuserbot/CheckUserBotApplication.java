package info.prianichnikov.telegram.bot.checkuserbot;

import lombok.extern.slf4j.Slf4j;
import org.telegram.telegrambots.ApiContextInitializer;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiRequestException;

@Slf4j
public class CheckUserBotApplication {

    public static void main(String[] args) {

        ApiContextInitializer.init();
        TelegramBotsApi api = new TelegramBotsApi();
        try {
            api.registerBot(new CheckUserBot());
            log.info("Bot is online");
        } catch (TelegramApiRequestException e) {
            log.error("Error during registering bot", e);
            throw new RuntimeException(e);
        }
    }
}
