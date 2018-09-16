package info.prianichnikov.telegram.bot.checkuserbot.task;

import info.prianichnikov.telegram.bot.checkuserbot.CheckUserBot;

import java.util.TimerTask;

public class UnbanUserTask extends TimerTask {

    private final Long chatId;
    private final Integer userId;

    public UnbanUserTask(Long chatId, Integer userId) {
        this.chatId = chatId;
        this.userId = userId;
    }

    @Override
    public void run() {
        CheckUserBot.getInstance().unbanUser(chatId, userId);
    }
}
