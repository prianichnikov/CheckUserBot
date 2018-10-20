package info.prianichnikov.telegram.bot.checkuserbot.task;

import info.prianichnikov.telegram.bot.checkuserbot.CheckUserBot;
import info.prianichnikov.telegram.bot.checkuserbot.exception.BotException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.TimerTask;

public class DeleteMessageTask extends TimerTask {

    private Integer messageId;
    private Long chatId;
    private final Logger LOG = LogManager.getLogger(DeleteMessageTask.class);

    public DeleteMessageTask(Long chatId, Integer messageId) {
        this.messageId = messageId;
        this.chatId = chatId;
    }

    @Override
    public void run() {
        try {
            CheckUserBot.getInstance().deleteMessage(chatId, messageId);
        } catch (BotException e) {
            LOG.error("Task error: Cannot delete message id: {} from chat id: {}", messageId, chatId);
        }
    }
}
