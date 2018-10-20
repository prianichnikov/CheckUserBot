package info.prianichnikov.telegram.bot.checkuserbot;

import info.prianichnikov.telegram.bot.checkuserbot.exception.BotException;
import info.prianichnikov.telegram.bot.checkuserbot.service.PropertiesService;
import info.prianichnikov.telegram.bot.checkuserbot.task.DeleteMessageTask;
import info.prianichnikov.telegram.bot.checkuserbot.task.DeleteUserTask;
import info.prianichnikov.telegram.bot.checkuserbot.task.UnbanningUserTask;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.groupadministration.GetChatAdministrators;
import org.telegram.telegrambots.meta.api.methods.groupadministration.KickChatMember;
import org.telegram.telegrambots.meta.api.methods.groupadministration.LeaveChat;
import org.telegram.telegrambots.meta.api.methods.groupadministration.UnbanChatMember;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.objects.*;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.*;

public class CheckUserBot extends TelegramLongPollingBot {

    private static CheckUserBot bot;
    private final Logger LOG = LogManager.getLogger(CheckUserBot.class.getName());
    private static final long DELETE_TIMEOUT = 60 * 1000;
    private static final long UNBANNING_TIMEOUT = 10 * 1000;
    private static final String BUTTON_MESSAGE = "Привет";
    private static final String REPLY_MESSAGE = " добрый день!\n" +
            "Чтобы стать участником данного чата, пожалуйста, " +
            "нажмите на кнопку \"" + BUTTON_MESSAGE + "\".\n" +
            "У вас есть на это 60 секунд.";
    private static final Map<String, List<Timer>> TIMERS = new HashMap<>();
    private final PropertiesService propertiesService;

    private CheckUserBot() throws BotException {
        if (getBotToken() == null || getBotToken().isEmpty()) {
            throw new BotException("Bot token cannot be null or empty");
        }
        propertiesService = new PropertiesService("configuration.properties");
    }


    public static CheckUserBot getInstance() throws BotException {
        if (bot == null) {
            bot = new CheckUserBot();
        }
        return bot;
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

        // Only new messages and callbacks
        if (!update.hasCallbackQuery() && !update.hasMessage()) {
            return;
        }

        // Ignore bot chat left messages
        if (update.hasMessage() && update.getMessage().getLeftChatMember() != null &&
            update.getMessage().getLeftChatMember().getId().toString().equals(getBotToken().split(":")[0])) {
            return;
        }

        // Leaving from non allowed chats
        final String chatId = getChatId(update);
        if(!propertiesService.getAllowedChats().contains(chatId)) {
            final String chatName = getChatName(update);
            LOG.error("Message from not allowed chat name: {}, id: {}", chatName, chatId);
            leaveChat(chatId);
            return;
        }

        // Callbacks
        if (update.hasCallbackQuery()) {
            handleCallbackEvent(update.getCallbackQuery());
            return;
        }

        // Only events from last 30 minutes
        final long between = ChronoUnit.MINUTES.between(
                LocalDateTime.ofEpochSecond(update.getMessage().getDate(), 0, ZoneOffset.UTC),
                LocalDateTime.now(ZoneOffset.UTC));
        if (between > 30) {
            return;
        }

        // Handle new members event
        if (update.getMessage().getNewChatMembers() != null) {
            handleNewUserEvent(update.getMessage());
            return;
        }

        // Other messages from users
        if (update.hasMessage()) {
            handleMessageFromNewUsers(update.getMessage());
        }

    }

    private void leaveChat(final String chatId) {
        LOG.info("Leaving chat id: {}", chatId);
        final LeaveChat leaveChat = new LeaveChat();
        leaveChat.setChatId(chatId);
        try {
            execute(leaveChat);
        } catch (TelegramApiException ex) {
            LOG.error("Error leaving chat", ex);
        }
    }

    private String getChatName(final Update update) {
        String chatName;
        if (update.hasMessage()) {
            chatName = update.getMessage().getChat().getTitle();
        } else {
            chatName = update.getCallbackQuery().getMessage().getChat().getTitle();
        }
        return chatName;
    }

    private String getChatId(final Update update) {
        String chatId;
        if (update.hasMessage()) {
            chatId = update.getMessage().getChatId().toString();
        } else {
            chatId = update.getCallbackQuery().getMessage().getChatId().toString();
        }
        return chatId;
    }

    private void handleMessageFromNewUsers(final Message message) {
        final Integer userId = message.getFrom().getId();
        final Long chatId = message.getChatId();
        final String chatUserId = getChatUserId(chatId, userId);
        if (TIMERS.containsKey(chatUserId)) {
            LOG.info(String.format("Delete message id: %s from not verified user id: %s", message.getMessageId(), userId));
            deleteMessage(chatId, message.getMessageId());
        }
    }

    private void handleCallbackEvent(final CallbackQuery callbackQuery) {
        final User user = callbackQuery.getFrom();
        LOG.info(String.format("Callback from user: %s (id: %s) in group %s (id: %s)",
                user.getUserName(), user.getId(),
                callbackQuery.getMessage().getChat().getTitle(), callbackQuery.getMessage().getChatId()));
        final Long chatId = callbackQuery.getMessage().getChatId();
        final Integer messageId = callbackQuery.getMessage().getMessageId();
        if (callbackQuery.getData().equalsIgnoreCase(getChatUserId(chatId, user.getId()))) {
            LOG.info("Callback is correct");
            removeScheduledTasks(chatId, user.getId());
            deleteMessage(chatId, messageId);
        }
    }

    private void handleNewUserEvent(final Message message) {
        final User user = message.getNewChatMembers().get(0);
        LOG.info("---===---");
        LOG.info(String.format("New user id: %s, login: %s, first name: %s, last name %s, chat id: %s, message id %s",
                user.getId(), user.getUserName(), user.getFirstName(), user.getLastName(),
                message.getChatId(), message.getMessageId()));

        // Allow bots added by administrators
        if (user.getBot()) {
            LOG.info("User is bot!");
            final boolean isBotAddedByAdministrator = getChatAdministrators(message.getChatId()).stream()
                    .anyMatch(chatMember -> chatMember.getUser().getId().equals(message.getFrom().getId()));
            if (!isBotAddedByAdministrator) {
                LOG.warn("User was added not by administrator, remove them from the chat");
                deleteUser(message.getChatId(), user.getId());
            }
            return;
        }
        final SendMessage replyMessage = prepareReplyMessage(message);
        try {
            final Message repliedMessage = execute(replyMessage);
            prepareDeleteUserTask(message);
            prepareDeleteMessageTask(repliedMessage, user.getId());
            prepareDeleteMessageTask(message, user.getId());
        } catch (TelegramApiException e) {
            LOG.error("Cannot sent reply message", e);
        }
    }

    private List<ChatMember> getChatAdministrators(final Long chatId) {
        final GetChatAdministrators chatAdministratorsRequest = new GetChatAdministrators();
        chatAdministratorsRequest.setChatId(chatId);
        List<ChatMember> chatAdministrators = new ArrayList<>();
        try {
            chatAdministrators = execute(chatAdministratorsRequest);
        } catch (TelegramApiException e) {
            LOG.error(String.format("Cannot fetch list of administrators chat id: %s", chatId));
        }
        return chatAdministrators;
    }

    public void deleteUser(final Long chatId, final Integer userId) {
        LOG.info(String.format("Delete user id: %s from chat id: %s", userId, chatId));
        final KickChatMember kickChatMember = new KickChatMember();
        kickChatMember.setChatId(chatId);
        kickChatMember.setUserId(userId);
        kickChatMember.setUntilDate(0);
        try {
            execute(kickChatMember);
        } catch (TelegramApiException e) {
            LOG.error("Cannot delete the user", e);
        }
        prepareUnbanningUserTask(chatId, userId);
    }

    public void deleteMessage(final Long chatId, final Integer messageId) {
        LOG.info(String.format("Delete message id: %s from chat id: %s", messageId, chatId));
        final DeleteMessage deleteMessage = new DeleteMessage();
        deleteMessage.setMessageId(messageId);
        deleteMessage.setChatId(chatId.toString());
        try {
            execute(deleteMessage);
        } catch (TelegramApiException e) {
            LOG.error("Cannot delete the message", e);
        }
    }

    public void unbanningUser(final Long chatId, final Integer userId) {
        LOG.info(String.format("Unbanning user id: %s in chat id: %s", userId, chatId));
        final UnbanChatMember unbanChatMember = new UnbanChatMember();
        unbanChatMember.setChatId(chatId);
        unbanChatMember.setUserId(userId);
        try {
            execute(unbanChatMember);
        } catch (TelegramApiException e) {
            LOG.error("Cannot unbanning user", e);
        }
    }

    private SendMessage prepareReplyMessage(final Message message) {
        final SendMessage reply = new SendMessage();
        final Long chatId = message.getChatId();
        final User user = message.getNewChatMembers().get(0);
        reply.setChatId(chatId);
        reply.setReplyToMessageId(message.getMessageId());
        String userName = user.getUserName();
        if (userName == null || userName.isEmpty()) {
            userName = String.format("[%s](tg://user?id=%s)",
                    user.getFirstName(),
                    user.getId());
            reply.enableMarkdown(true);
            reply.setText(userName + REPLY_MESSAGE);
        } else {
            reply.setText("@" + userName + REPLY_MESSAGE);
        }

        // Prepare buttons
        final InlineKeyboardButton button = new InlineKeyboardButton();
        button.setText(BUTTON_MESSAGE).setCallbackData(getChatUserId(chatId, user.getId()));

        // Prepare buttons row
        final List<InlineKeyboardButton> buttons = new ArrayList<>();
        buttons.add(button);

        // Prepare keyboard row
        final List<List<InlineKeyboardButton>> keyboardRows = new ArrayList<>();
        keyboardRows.add(buttons);

        // keyboard
        final InlineKeyboardMarkup keyboard = new InlineKeyboardMarkup();
        keyboard.setKeyboard(keyboardRows);
        reply.setReplyMarkup(keyboard);
        return reply;
    }

    private void removeScheduledTasks(final Long chatId, final Integer userId) {
        LOG.info(String.format("Removing scheduled tasks for user id: %s in chat id: %s", userId, chatId));
        final String timerKey = getChatUserId(chatId, userId);
        if(!TIMERS.containsKey(timerKey)) {
            LOG.error(String.format("Timers map doesn't contain key %s", timerKey));
            return;
        }
        TIMERS.remove(timerKey).forEach(Timer::cancel);
    }

    private void prepareDeleteUserTask(final Message message) {
        final Integer userId = message.getNewChatMembers().get(0).getId();
        final Long chatId = message.getChatId();
        LOG.info(String.format("Preparing task to delete new user id: %s from chat id: %s", userId, chatId));
        final DeleteUserTask deleteUserTask = new DeleteUserTask(chatId, userId);
        final Timer deleteUserTimer = new Timer();
        deleteUserTimer.schedule(deleteUserTask, DELETE_TIMEOUT);
        final String timerKey = getChatUserId(chatId, userId);
        if (TIMERS.containsKey(timerKey)) {
            final List<Timer> timers = TIMERS.remove(timerKey);
            timers.add(deleteUserTimer);
            TIMERS.put(timerKey, timers);
        } else {
            final List<Timer> timers = new ArrayList<>();
            timers.add(deleteUserTimer);
            TIMERS.put(timerKey, timers);
        }
    }

    private void prepareDeleteMessageTask(final Message message, final Integer userId) {
        LOG.info("Preparing task to delete message id: {}", message.getMessageId());
        final Long chatId = message.getChatId();
        final Integer messageId = message.getMessageId();
        final DeleteMessageTask deleteMessageTask = new DeleteMessageTask(chatId, messageId);
        final Timer deleteMessageTimer = new Timer();
        deleteMessageTimer.schedule(deleteMessageTask, DELETE_TIMEOUT);
        final String timerKey = getChatUserId(chatId, userId);
        if (TIMERS.containsKey(timerKey)) {
            final List<Timer> timers = TIMERS.remove(timerKey);
            timers.add(deleteMessageTimer);
            TIMERS.put(timerKey, timers);
        } else {
            final List<Timer> timers = new ArrayList<>();
            timers.add(deleteMessageTimer);
            TIMERS.put(timerKey, timers);
        }
    }

    private void prepareUnbanningUserTask(final Long chatId, final Integer userId) {
        LOG.info("Preparing task to unbanning user id: {}", userId);
        final UnbanningUserTask unbanningUserTask = new UnbanningUserTask(chatId, userId);
        final Timer unbanningUserTimer = new Timer();
        unbanningUserTimer.schedule(unbanningUserTask, UNBANNING_TIMEOUT);
    }

    private String getChatUserId(final Long chatId, final Integer userId) {
        return chatId + "_" + userId;
    }
}
