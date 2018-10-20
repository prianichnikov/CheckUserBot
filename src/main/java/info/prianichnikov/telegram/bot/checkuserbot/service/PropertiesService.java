package info.prianichnikov.telegram.bot.checkuserbot.service;

import info.prianichnikov.telegram.bot.checkuserbot.exception.BotException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

public class PropertiesService {

    private final static Logger LOG = LogManager.getLogger(PropertiesService.class.getName());
    private final Properties properties = new Properties();

    public PropertiesService(String fileName) throws BotException {
        File propertiesFile = new File(ClassLoader.getSystemResource(fileName).getFile());
        LOG.info("Loading properties from file {}", propertiesFile.toString());
        try (FileInputStream fis = new FileInputStream(propertiesFile)) {
            properties.load(fis);
        } catch (IOException ex) {
            LOG.error("Cannot load properties file");
            throw new BotException("Cannot load properties file", ex);
        }
    }

    public List<String> getAllowedChats() {
        return Arrays.asList(properties.getProperty("bot.allowedchats").split(","));
    }
}
