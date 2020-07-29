package info.prianichnikov.telegram.bot.checkuserbot.task;

import info.prianichnikov.telegram.bot.checkuserbot.CheckUserBot;
import lombok.AllArgsConstructor;

import java.util.TimerTask;

@AllArgsConstructor
public class DeleteUserTask extends TimerTask {

    private final Integer userId;
    private final Long chatId;
    private final CheckUserBot checkUserBot;

    @Override
    public void run() {
        checkUserBot.deleteUser(chatId, userId);
    }
}
