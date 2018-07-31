package info.prianichnikov.telegram.bot.checkuserbot;

import info.prianichnikov.telegram.bot.checkuserbot.service.StopWordService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.telegram.telegrambots.api.methods.BotApiMethod;
import org.telegram.telegrambots.api.methods.groupadministration.KickChatMember;
import org.telegram.telegrambots.api.methods.groupadministration.RestrictChatMember;
import org.telegram.telegrambots.api.methods.send.SendMessage;
import org.telegram.telegrambots.api.objects.Message;
import org.telegram.telegrambots.api.objects.Update;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.exceptions.TelegramApiException;

import java.util.HashMap;
import java.util.Map;

public class CheckUserBot extends TelegramLongPollingBot {

    private final Logger LOG = LogManager.getLogger(CheckUserBot.class.getName());
    private final StopWordService stopWordService = new StopWordService();
    private Map<String, Integer> userCounts = new HashMap<>();

    public void checkEnvironmentVariables() throws TelegramApiException {
        if (getBotToken() == null || getBotToken().isEmpty()) {
            throw new TelegramApiException("Bot token cannot be null or empty");
        }
    }

    @Override
    public String getBotUsername() {
        return System.getenv("BOT_NAME");
    }

    @Override
    public String getBotToken() {
        return System.getenv("BOT_TOKEN");
    }

    public void onUpdateReceived(Update update) {
        if (update.getMessage() != null) {
            checkObsceneWords(update.getMessage());
        } else if (update.getEditedMessage() != null) {
            checkObsceneWords(update.getEditedMessage());
        }
    }

    private void checkObsceneWords(final Message message) {
        final String userKey = message.getFrom().getId().toString().concat(message.getChatId().toString());
        if (stopWordService.isContainsObsceneWords(message.getText())) {
//            if (isUserHasAttempt(userKey)) {
//                increaseCounter(userKey);
                try {
                    execute(prepareWarningMessage(message, "No obscene words in this group!"));
                } catch (TelegramApiException e) {
                    LOG.error("Cannot send message", e);
                }
//            } else {
//                try {
//                    execute(prepareRestrictUserAction(message));
//                    execute(prepareWarningMessage(message, "Your rights was limited to read-only!"));
//                } catch (TelegramApiException e) {
//                    LOG.error("Cannot send message", e);
//                }
//            }
        }
    }

    private void increaseCounter(final String userKey) {
        if (!userCounts.containsKey(userKey)) {
            userCounts.put(userKey, 1);
        }
        userCounts.replace(userKey, userCounts.get(userKey) + 1);
    }

    private boolean isUserHasAttempt(final String userKey) {
//        if (!userCounts.containsKey(userKey)) {
//            return true;
//        }
//        final Integer count = userCounts.get(userKey);
//        return count < 1;
        return true;
    }


    private void sendNormalMessage(final Update update) throws TelegramApiException {
        SendMessage startMessage = new SendMessage();
        startMessage.setChatId(update.getMessage().getChatId());
        startMessage.setText("It's ok");
        execute(startMessage);
    }


    private void sendExitMessage(final Update update) throws TelegramApiException {
        SendMessage startMessage = new SendMessage();
        startMessage.setChatId(update.getMessage().getChatId());
        startMessage.setText(update.getMessage().getFrom().getUserName() + " you rights was limited to read-only!");
        execute(startMessage);
    }

    private BotApiMethod<Boolean> prepareRestrictUserAction(final Message message) {
        final RestrictChatMember restrictChatMember = new RestrictChatMember();
        restrictChatMember.setChatId(message.getChatId());
        restrictChatMember.setUserId(message.getFrom().getId());
        restrictChatMember.setUntilDate(0);
        restrictChatMember.setCanSendMessages(false);
        restrictChatMember.setCanSendMediaMessages(false);
        restrictChatMember.setCanSendOtherMessages(false);
        restrictChatMember.setCanAddWebPagePreviews(false);
        String userName = message.getFrom().getUserName();
        if (userName == null || userName.isEmpty()) {
            userName = message.getFrom().getFirstName() + " " + message.getFrom().getLastName();
        }

        LOG.info(String.format("Restrict access userId: %d, userName %s",
                message.getFrom().getId(), userName));
        return restrictChatMember;
    }

    private BotApiMethod<Boolean> prepareKickUserAction(final Update update) {
        final KickChatMember kickChatMember = new KickChatMember();
        kickChatMember.setChatId(update.getMessage().getChatId());
        kickChatMember.setUserId(update.getMessage().getFrom().getId());
        kickChatMember.setUntilDate(0);
        return kickChatMember;
    }

    private SendMessage prepareWarningMessage(final Message message, final String warningText) {
        SendMessage warningMessage = new SendMessage();
        warningMessage.setChatId(message.getChatId());
        warningMessage.setReplyToMessageId(message.getMessageId());
        warningMessage.setText("Warning: " + warningText);
        String userName = message.getFrom().getUserName();
        if (userName == null || userName.isEmpty()) {
            userName = message.getFrom().getFirstName() + " " + message.getFrom().getLastName();
        }
        LOG.info(String.format("Warning message to userId: %d, userName: %s",
                message.getFrom().getId(), userName));
        return warningMessage;
    }

    private void writeToLog(final String messageText) {
        LOG.info(String.format("Detected message: %s", messageText));
    }
}
