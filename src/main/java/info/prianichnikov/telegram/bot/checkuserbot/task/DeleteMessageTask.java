package info.prianichnikov.telegram.bot.checkuserbot.task;

import info.prianichnikov.telegram.bot.checkuserbot.CheckUserBot;
import lombok.AllArgsConstructor;

import java.util.TimerTask;

@AllArgsConstructor
public class DeleteMessageTask extends TimerTask {

    private final Long userId;
    private final Long chatId;
    private final CheckUserBot checkUserBot;

    @Override
    public void run() {
        checkUserBot.deleteEntryMessage(chatId, userId);
        checkUserBot.deleteReplyMessage(chatId, userId);
    }
}
