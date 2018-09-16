package info.prianichnikov.telegram.bot.checkuserbot.task;

import info.prianichnikov.telegram.bot.checkuserbot.CheckUserBot;

import java.util.TimerTask;

public class DeleteUserTask extends TimerTask {

    private Integer userId;
    private Long chatId;

    public DeleteUserTask(Long chatId, Integer userId) {
        this.userId = userId;
        this.chatId = chatId;
    }

    @Override
    public void run() {
        CheckUserBot.getInstance().deleteUser(chatId, userId);
    }
}
