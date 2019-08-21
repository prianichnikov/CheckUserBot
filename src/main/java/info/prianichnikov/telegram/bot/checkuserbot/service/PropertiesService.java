package info.prianichnikov.telegram.bot.checkuserbot.service;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

public class PropertiesService {

    private final static String FILE_NAME = "/configuration.properties";
    private final static Logger LOG = LogManager.getLogger(PropertiesService.class.getName());
    private final Properties properties = new Properties();

    public PropertiesService() {
        loadPropertiesFile();
    }

    private void loadPropertiesFile() {
        try (InputStreamReader reader = new InputStreamReader(
                PropertiesService.class.getResourceAsStream(FILE_NAME), StandardCharsets.UTF_8)) {
            properties.load(reader);
        } catch (IOException ex) {
            LOG.error("Cannot load properties file");
            throw new RuntimeException("Cannot load properties file", ex);
        }
    }

    public List<String> getAllowedChats() {
        return Arrays.asList(properties.getProperty("bot.chats").split(","));
    }

    public String getBotName() {
        return properties.getProperty("bot.name");
    }

    public String getBotToken() {
        String botToken = System.getenv("TOKEN");
        if (botToken == null || botToken.isEmpty()) {
            LOG.fatal("Bot token cannot be null or empty");
            throw new IllegalArgumentException("Bot token cannot be null or empty");
        }
        return botToken;
    }

    public long getDeleteTimeout() {
        return Long.parseLong(properties.getProperty("bot.deleteTimeoutSeconds"));
    }

    public long getUnBanTimeoutSecond() {
        return Long.parseLong(properties.getProperty("bot.unBanTimeoutSeconds"));
    }

    public long getMessageTimeoutMinutes() {
        return Long.parseLong(properties.getProperty("bot.messageTimeoutMinutes"));
    }

    public String getHelloMessage() {
        return properties.getProperty("bot.helloMessage");
    }

    public String getPrivateMessage() {
        return properties.getProperty("bot.privateMessage");
    }
}
