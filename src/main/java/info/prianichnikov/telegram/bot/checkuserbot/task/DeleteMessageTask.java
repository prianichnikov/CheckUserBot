package info.prianichnikov.telegram.bot.checkuserbot.task;

import info.prianichnikov.telegram.bot.checkuserbot.CheckUserBot;

import java.util.TimerTask;

public class DeleteMessageTask extends TimerTask {

    private Integer messageId;
    private Long chatId;
    private CheckUserBot checkUserBot;

    public DeleteMessageTask(Long chatId, Integer messageId, CheckUserBot checkUserBot) {
        this.messageId = messageId;
        this.chatId = chatId;
        this.checkUserBot = checkUserBot;
    }

    @Override
    public void run() {
        checkUserBot.deleteMessage(chatId, messageId);
    }
}
