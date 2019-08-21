package info.prianichnikov.telegram.bot.checkuserbot.task;

import info.prianichnikov.telegram.bot.checkuserbot.CheckUserBot;

import java.util.TimerTask;

public class DeleteMessageTask extends TimerTask {

    private Integer userId;
    private Long chatId;
    private CheckUserBot checkUserBot;

    public DeleteMessageTask(Long chatId, Integer userId, CheckUserBot checkUserBot) {
        this.userId = userId;
        this.chatId = chatId;
        this.checkUserBot = checkUserBot;
    }

    @Override
    public void run() {
        checkUserBot.deleteEntryMessage(chatId, userId);
        checkUserBot.deleteReplyMessage(chatId, userId);
    }
}
