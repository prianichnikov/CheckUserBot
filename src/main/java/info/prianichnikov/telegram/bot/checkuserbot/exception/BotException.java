package info.prianichnikov.telegram.bot.checkuserbot.exception;

public class BotException extends Exception {

    public BotException(String message) {
        super(message);
    }

    public BotException(String message, Throwable cause) {
        super(message, cause);
    }
}
