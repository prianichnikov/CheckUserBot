package info.prianichnikov.telegram.bot.checkuserbot.task;

import info.prianichnikov.telegram.bot.checkuserbot.CheckUserBot;

import java.util.TimerTask;

public class DeleteUserTask extends TimerTask {

    private Integer userId;
    private Long chatId;
    private CheckUserBot checkUserBot;

    public DeleteUserTask(Long chatId, Integer userId, CheckUserBot checkUserBot) {
        this.userId = userId;
        this.chatId = chatId;
        this.checkUserBot = checkUserBot;
    }

    @Override
    public void run() {
        checkUserBot.deleteUser(chatId, userId);
    }
}
