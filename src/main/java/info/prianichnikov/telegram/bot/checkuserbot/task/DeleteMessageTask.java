package info.prianichnikov.telegram.bot.checkuserbot.task;

import info.prianichnikov.telegram.bot.checkuserbot.CheckUserBot;

import java.util.TimerTask;

public class DeleteMessageTask extends TimerTask {

    private Integer messageId;
    private Long chatId;

    public DeleteMessageTask(Long chatId, Integer messageId) {
        this.messageId = messageId;
        this.chatId = chatId;
    }

    @Override
    public void run() {
        CheckUserBot.getInstance().deleteMessage(chatId, messageId);
    }
}
