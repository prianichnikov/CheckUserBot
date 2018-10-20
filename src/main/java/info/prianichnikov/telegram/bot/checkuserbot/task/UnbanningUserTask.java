package info.prianichnikov.telegram.bot.checkuserbot.task;

import info.prianichnikov.telegram.bot.checkuserbot.CheckUserBot;
import info.prianichnikov.telegram.bot.checkuserbot.exception.BotException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.TimerTask;

public class UnbanningUserTask extends TimerTask {

    private final Long chatId;
    private final Integer userId;
    private final Logger LOG = LogManager.getLogger(UnbanningUserTask.class);

    public UnbanningUserTask(Long chatId, Integer userId) {
        this.chatId = chatId;
        this.userId = userId;
    }

    @Override
    public void run() {
        try {
            CheckUserBot.getInstance().unbanningUser(chatId, userId);
        } catch (BotException e) {
            LOG.error("Task error: Cannot unbanning user id: {} from chat id: {}", userId, chatId);
        }
    }
}
