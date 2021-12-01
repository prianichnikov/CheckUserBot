package info.prianichnikov.telegram.bot.checkuserbot;

import lombok.extern.slf4j.Slf4j;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

@Slf4j
public class CheckUserBotApplication {

    public static void main(String[] args) {

        try {
            TelegramBotsApi api = new TelegramBotsApi(DefaultBotSession.class);
            api.registerBot(new CheckUserBot());
            log.info("Bot is online");
        } catch (TelegramApiException ex) {
            log.error("Telegram exception", ex);
            throw new IllegalStateException(ex);
        }
    }
}
