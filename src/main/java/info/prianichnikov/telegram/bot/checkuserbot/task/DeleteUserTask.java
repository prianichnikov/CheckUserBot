package info.prianichnikov.telegram.bot.checkuserbot.task;

import info.prianichnikov.telegram.bot.checkuserbot.CheckUserBot;
import info.prianichnikov.telegram.bot.checkuserbot.exception.BotException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.TimerTask;

public class DeleteUserTask extends TimerTask {

    private Integer userId;
    private Long chatId;
    private final Logger LOG = LogManager.getLogger(DeleteUserTask.class);

    public DeleteUserTask(Long chatId, Integer userId) {
        this.userId = userId;
        this.chatId = chatId;
    }

    @Override
    public void run() {
        try {
            CheckUserBot.getInstance().deleteUser(chatId, userId);
        } catch (BotException e) {
            LOG.error("Task error: Cannot delete user id: {} from chat id: {}", userId, chatId);
        }
    }
}
