package info.prianichnikov.telegram.bot.checkuserbot.utils;

import org.telegram.telegrambots.meta.api.objects.Update;

public class ChatUtils {

    private ChatUtils() { }

    public static String getChatName(Update update) {
        String chatName;
        if (update.hasMessage()) {
            chatName = update.getMessage().getChat().getTitle();
        } else {
            chatName = update.getCallbackQuery().getMessage().getChat().getTitle();
        }
        return chatName;
    }

    public static String getChatId(Update update) {
        if (update.hasMessage()) {
            return update.getMessage().getChatId().toString();
        } else {
            return update.getCallbackQuery().getMessage().getChatId().toString();
        }
    }

    public static String getChatUserId(Long chatId, Long userId) {
        return chatId + "_" + userId;
    }
}
