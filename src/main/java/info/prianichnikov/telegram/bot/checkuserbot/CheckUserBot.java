package info.prianichnikov.telegram.bot.checkuserbot;

import info.prianichnikov.telegram.bot.checkuserbot.number.RandomNumber;
import info.prianichnikov.telegram.bot.checkuserbot.service.PropertiesService;
import info.prianichnikov.telegram.bot.checkuserbot.service.RandomNumberService;
import info.prianichnikov.telegram.bot.checkuserbot.task.DeleteMessageTask;
import info.prianichnikov.telegram.bot.checkuserbot.task.DeleteUserTask;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.groupadministration.GetChatAdministrators;
import org.telegram.telegrambots.meta.api.methods.groupadministration.KickChatMember;
import org.telegram.telegrambots.meta.api.methods.groupadministration.LeaveChat;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.objects.*;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;

public class CheckUserBot extends TelegramLongPollingBot {

    private static final Logger LOG = LogManager.getLogger(CheckUserBot.class.getName());
    private static final Map<String, List<Timer>> TIMERS = new HashMap<>();
    private static final Map<String, Integer> ENTRY_MESSAGES = new HashMap<>();
    private static final Map<String, Integer> REPLY_MESSAGES = new HashMap<>();

    private final PropertiesService propertiesService = new PropertiesService();
    private final RandomNumberService randomNumberService = new RandomNumberService();

    @Override
    public String getBotUsername() {
        return propertiesService.getBotName();
    }

    @Override
    public String getBotToken() {
        return propertiesService.getBotToken();
    }

    @Override
    public void onUpdateReceived(Update update) {

        // Process only new messages and callbacks
        if (!update.hasCallbackQuery() && !update.hasMessage()) {
            return;
        }

        // Private messages
        if (update.hasMessage() && update.getMessage().getChat().isUserChat()) {
            handlePrivateMessage(update.getMessage());
            return;
        }

        // Leaving not allowed chats
        String chatId = getChatId(update);
        if (!propertiesService.getAllowedChats().contains(chatId)) {
            String chatName = getChatName(update);
            LOG.warn("Message from not allowed chat: [{}], id: [{}], leaving this chat", chatName, chatId);
            leaveChat(chatId);
            return;
        }

        // Only last events
        if (update.hasMessage() && ChronoUnit.MINUTES.between(
                ZonedDateTime.ofInstant(Instant.ofEpochSecond(update.getMessage().getDate()), ZoneOffset.UTC),
                ZonedDateTime.now(ZoneOffset.UTC)) > propertiesService.getMessageTimeoutMinutes()) {
            LOG.warn("The message id [{}] was send more than {} minutes ago, ignore it",
                    update.getMessage().getMessageId(), propertiesService.getMessageTimeoutMinutes());
            return;
        }

        // Ignore bot left group messages
        if (update.hasMessage() && update.getMessage().getLeftChatMember() != null &&
                update.getMessage().getLeftChatMember().getId().toString().equals(getBotToken().split(":")[0])) {
            return;
        }

        // Callbacks
        if (update.hasCallbackQuery()) {
            handleCallbackEvent(update.getCallbackQuery());
            return;
        }

        // Handle new members event
        if (!update.getMessage().getNewChatMembers().isEmpty()) {
            handleNewUserEvent(update.getMessage());
            return;
        }

        // Other messages from users
        deleteNonVerifiedUserMessages(update.getMessage());
    }

    private void leaveChat(String chatId) {
        LeaveChat leaveChat = new LeaveChat();
        leaveChat.setChatId(chatId);
        try {
            execute(leaveChat);
        } catch (TelegramApiException ex) {
            LOG.error("Error leaving chat", ex);
        }
    }

    private String getChatName(Update update) {
        String chatName;
        if (update.hasMessage()) {
            chatName = update.getMessage().getChat().getTitle();
        } else {
            chatName = update.getCallbackQuery().getMessage().getChat().getTitle();
        }
        return chatName;
    }

    private String getChatId(Update update) {
        if (update.hasMessage()) {
            return update.getMessage().getChatId().toString();
        } else {
            return update.getCallbackQuery().getMessage().getChatId().toString();
        }
    }

    private void handleCallbackEvent(CallbackQuery callbackQuery) {
        User user = callbackQuery.getFrom();
        Long chatId = callbackQuery.getMessage().getChatId();
        String callbackData = callbackQuery.getData();

        if (callbackData.contains(user.getId().toString())) {
            if (callbackData.equals(getChatUserId(chatId, user.getId()))) {
                LOG.info("Answer is correct");
                removeScheduledTasks(chatId, user.getId());
                deleteReplyMessage(chatId, user.getId());
                ENTRY_MESSAGES.remove(getChatUserId(chatId, user.getId()));
            } else {
                LOG.info("Answer is wrong, kicking user");
                removeScheduledTasks(chatId, user.getId());
                deleteUser(chatId, user.getId());
                deleteEntryMessage(chatId, user.getId());
                deleteReplyMessage(chatId, user.getId());
            }
        } else {
            LOG.info("Callback from wrong user id: [{}], name: [{}]", user.getId(), user.getUserName());
        }
    }

    private void handleNewUserEvent(Message newUserMessage) {
        for (User newChatMember : newUserMessage.getNewChatMembers()) {
            LOG.info("---===---");
            LOG.info("New user id: [{}], login: [{}], first name: [{}], last name [{}], chat id: [{}], message id [{}]",
                    newChatMember.getId(), newChatMember.getUserName(), newChatMember.getFirstName(), newChatMember.getLastName(),
                    newUserMessage.getChatId(), newUserMessage.getMessageId());

            // Allow bots was added by admins
            if (newChatMember.getBot()) {
                LOG.warn("User is bot!");
                boolean isBotAddedByAdmin = getChatAdministrators(newUserMessage.getChatId()).stream()
                        .anyMatch(admin -> admin.getUser().getId().equals(newUserMessage.getFrom().getId()));
                if (!isBotAddedByAdmin) {
                    LOG.warn("Bot was added not by administrators, remove them from the chat");
                    deleteUser(newUserMessage.getChatId(), newChatMember.getId());
                }
                return;
            }
            SendMessage replyMessage = prepareReplyMessage(newUserMessage, newChatMember);
            try {
                Message repliedMessage = execute(replyMessage);
                prepareDeleteTasks(newChatMember, newUserMessage, repliedMessage);
            } catch (TelegramApiException e) {
                LOG.error("Cannot sent reply message", e);
            }
        }
    }

    private void prepareDeleteTasks(User user, Message entryMessage, Message repliedMessage) {
        String chatUserId = getChatUserId(entryMessage.getChatId(), user.getId());
        ENTRY_MESSAGES.put(chatUserId, entryMessage.getMessageId());
        REPLY_MESSAGES.put(chatUserId, repliedMessage.getMessageId());

        DeleteMessageTask deleteMessageTask = new DeleteMessageTask(user.getId(), entryMessage.getChatId(), this);
        Timer deleteMessageTimer = new Timer();
        deleteMessageTimer.schedule(deleteMessageTask, propertiesService.getDeleteTimeout() * 1000L);

        DeleteUserTask deleteUserTask = new DeleteUserTask(user.getId(), entryMessage.getChatId(), this);
        Timer deleteUserTimer = new Timer();
        deleteUserTimer.schedule(deleteUserTask, propertiesService.getDeleteTimeout() * 1000L);
        if (TIMERS.containsKey(chatUserId)) {
            List<Timer> timers = TIMERS.remove(chatUserId);
            timers.add(deleteMessageTimer);
            timers.add(deleteUserTimer);
            TIMERS.put(chatUserId, timers);
        } else {
            List<Timer> timers = new ArrayList<>();
            timers.add(deleteMessageTimer);
            timers.add(deleteUserTimer);
            TIMERS.put(chatUserId, timers);
        }
    }

    private List<ChatMember> getChatAdministrators(Long chatId) {
        GetChatAdministrators chatAdministratorsRequest = new GetChatAdministrators();
        chatAdministratorsRequest.setChatId(chatId);
        List<ChatMember> chatAdministrators = new ArrayList<>();
        try {
            chatAdministrators = execute(chatAdministratorsRequest);
        } catch (TelegramApiException e) {
            LOG.error("Cannot fetch administrators list from chat id: [{}]", chatId);
        }
        return chatAdministrators;
    }

    public void deleteUser(Long chatId, Integer userId) {
        LOG.info("Delete user id: [{}] from chat id: [{}]", userId, chatId);
        KickChatMember kickChatMember = new KickChatMember();
        kickChatMember.setChatId(chatId);
        kickChatMember.setUserId(userId);
        kickChatMember.setUntilDate(ZonedDateTime.now().plusSeconds(propertiesService.getUnBanTimeoutSecond()).toInstant());
        try {
            execute(kickChatMember);
        } catch (TelegramApiException e) {
            LOG.error("Cannot delete the user", e);
        }
    }

    public void deleteEntryMessage(Long chatId, Integer userId) {
        LOG.info("Delete entry message from chat id: [{}] from user id: [{}]", chatId, userId);
        deleteMessage(chatId, ENTRY_MESSAGES.remove(getChatUserId(chatId, userId)));
    }

    public void deleteReplyMessage(Long chatId, Integer userId) {
        LOG.info("Delete reply message from chat id: [{}] to user id: [{}]", chatId, userId);
        deleteMessage(chatId, REPLY_MESSAGES.remove(getChatUserId(chatId, userId)));
    }

    private void deleteMessage(Long chatId, Integer messageId) {
        DeleteMessage deleteMessage = new DeleteMessage();
        deleteMessage.setChatId(chatId);
        deleteMessage.setMessageId(messageId);
        try {
            execute(deleteMessage);
        } catch (TelegramApiException e) {
            LOG.error("Cannot delete the message", e);
        }
    }

    private SendMessage prepareReplyMessage(Message message, User user) {
        SendMessage reply = new SendMessage();
        Long chatId = message.getChatId();
        reply.setChatId(chatId);
        reply.setReplyToMessageId(message.getMessageId());
        reply.enableMarkdown(true);
        String userName = user.getUserName();

        List<InlineKeyboardButton> buttons = new ArrayList<>();
        List<RandomNumber> randomNumbers = randomNumberService.getRandomNumbers();
        RandomNumber controlNumber = randomNumberService.getControlNumber(randomNumbers);

        for (RandomNumber randomNumber : randomNumbers) {
            InlineKeyboardButton button = new InlineKeyboardButton();
            if (randomNumber.equals(controlNumber)) {
                button.setText(randomNumber.getUnicode()).setCallbackData(getChatUserId(chatId, user.getId()));
            } else {
                button.setText(randomNumber.getUnicode()).setCallbackData(randomNumber.getValue() + user.getId().toString());
            }
            buttons.add(button);
        }

        if (userName == null || userName.isEmpty()) {
            userName = String.format("[%s](tg://user?id=%s)",
                    user.getFirstName(),
                    user.getId());
            reply.setText(String.format(propertiesService.getHelloMessage(), userName, controlNumber.getName(),
                    propertiesService.getDeleteTimeout()));
        } else {
            userName = "@" + userName;
            reply.setText(String.format(propertiesService.getHelloMessage(), userName, controlNumber.getName(),
                    propertiesService.getDeleteTimeout()));
        }

        List<List<InlineKeyboardButton>> keyboardRows = new ArrayList<>();
        keyboardRows.add(buttons);

        InlineKeyboardMarkup keyboard = new InlineKeyboardMarkup();
        keyboard.setKeyboard(keyboardRows);
        reply.setReplyMarkup(keyboard);
        return reply;
    }

    private void removeScheduledTasks(Long chatId, Integer userId) {
        LOG.info("Removing scheduled tasks for user id: [{}] in chat id: [{}]", userId, chatId);
        String chatUserId = getChatUserId(chatId, userId);
        if (!TIMERS.containsKey(chatUserId)) {
            LOG.error("Timers map doesn't contain key [{}]", chatUserId);
            return;
        }
        TIMERS.remove(chatUserId).forEach(Timer::cancel);
    }

    private void handlePrivateMessage(Message message) {
        User user = message.getFrom();
        Long chatId = message.getChatId();
        LOG.info("Private message: [{}] from id: [{}], userName [{}], firstName [{}], lastName: [{}]",
                message.getText(), user.getId(), user.getUserName(), user.getFirstName(), user.getLastName());

        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(chatId);
        sendMessage.setText(propertiesService.getPrivateMessage());
        try {
            execute(sendMessage);
        } catch (TelegramApiException e) {
            LOG.error("Cannot send private message, chat id: [{}]", chatId);
        }
    }

    private void deleteNonVerifiedUserMessages(Message message) {
        Integer userId = message.getFrom().getId();
        Long chatId = message.getChatId();
        String chatUserId = getChatUserId(chatId, userId);
        if (TIMERS.containsKey(chatUserId)) {
            LOG.info("Message from non verified user: [{}]", message.getText());
            deleteMessage(chatId, message.getMessageId());
        }
    }

    private String getChatUserId(Long chatId, Integer userId) {
        return chatId + "_" + userId;
    }
}
