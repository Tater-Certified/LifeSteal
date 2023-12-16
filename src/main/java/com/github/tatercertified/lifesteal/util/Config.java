package com.github.tatercertified.lifesteal.util;

import net.fabricmc.loader.api.FabricLoader;

import java.io.*;
import java.nio.file.Files;
import java.util.Objects;
import java.util.Properties;

public class Config {
    public static final Properties properties = new Properties();

    private static final String cfgver = "1.4";
    public static String revivalBlock;
    public static boolean generateOres;
    public static String REVIVAL_MESSAGE;
    public static String MAX_HEALTH_REACHED;
    public static String RECEIVER_TOO_MUCH_HEALTH;
    public static String GIVER_TOO_LITTLE_HEALTH;
    public static String HEALTH_INFO_MESSAGE;
    public static String HEART_TRADED;
    public static String HEART_GIFTING_DISABLED;
    public static String PLAYER_IS_STILL_ALIVE;
    public static String PLAYER_DOES_NOT_EXIST;
    public static String REVIVER;
    public static String YOU_REVIVED;


    public static void init() {
        var path = FabricLoader.getInstance().getConfigDir().resolve("lifesteal.properties");

        if (Files.notExists(path)) {
            try {
                mkfile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            try {
                loadcfg();
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (!(Objects.equals(properties.getProperty("config-version"), cfgver))) {
                try {
                    mkfile();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else {
                parse();
            }
        }
    }

    private static void mkfile() throws IOException {
        OutputStream output = new FileOutputStream(String.valueOf(FabricLoader.getInstance().getConfigDir().resolve("lifesteal.properties")));
        if (!properties.contains("config-version")) {properties.setProperty("config-version", cfgver);}
        if (!properties.contains("revival_block")) {properties.setProperty("revival_block", "minecraft:netherite_block");}
        if (!properties.contains("generate_ores")) {properties.setProperty("generate_ores", "true");}
        if (!properties.contains("revival_message")) {properties.setProperty("revival_message", "You lost your last life. You now must be revived");}
        if (!properties.contains("max_health_reached_message")) {properties.setProperty("max_health_reached_message", "You are already at the maximum amount of health");}
        if (!properties.contains("receiver_has_max_health_message")) {properties.setProperty("receiver_has_max_health_message", "This player cannot receive this much health");}
        if (!properties.contains("giver_too_little_health_message")) {properties.setProperty("giver_too_little_health_message", "You have too little health to do this action");}
        if (!properties.contains("health_info_message")) {properties.setProperty("health_info_message", "Your max health is now ");}
        if (!properties.contains("heart_traded_message")) {properties.setProperty("heart_traded_message", "Converted health to hearts");}
        if (!properties.contains("heart_gifting_disabled_message")) {properties.setProperty("heart_gifting_disabled_message", "Heart gifting is disabled");}
        if (!properties.contains("player_alive_message")) {properties.setProperty("player_alive_message", " is still alive");}
        if (!properties.contains("nonexistent_player_message")) {properties.setProperty("nonexistent_player_message", " does not exist");}
        if (!properties.contains("reviver_message")) {properties.setProperty("reviver_message", " has revived you");}
        if (!properties.contains("you_revived_message")) {properties.setProperty("you_revived_message", "You revived ");}
        properties.store(output, null);
        parse();
        output.close();
    }

    private static void loadcfg() throws IOException {
        InputStream input = new FileInputStream(String.valueOf(FabricLoader.getInstance().getConfigDir().resolve("lifesteal.properties")));
        properties.load(input);
        input.close();
    }

    private static void parse() {
        revivalBlock = properties.getProperty("revival_block");
        generateOres = Boolean.parseBoolean(properties.getProperty("generate_ores"));
        REVIVAL_MESSAGE = properties.getProperty("revival_message");
        MAX_HEALTH_REACHED = properties.getProperty("max_health_reached_message");
        RECEIVER_TOO_MUCH_HEALTH = properties.getProperty("receiver_has_max_health_message");
        GIVER_TOO_LITTLE_HEALTH = properties.getProperty("giver_too_little_health_message");
        HEALTH_INFO_MESSAGE = properties.getProperty("health_info_message");
        HEART_TRADED = properties.getProperty("heart_traded_message");
        HEART_GIFTING_DISABLED = properties.getProperty("heart_gifting_disabled_message");
        PLAYER_IS_STILL_ALIVE = properties.getProperty("player_alive_message");
        PLAYER_DOES_NOT_EXIST = properties.getProperty("nonexistent_player_message");
        REVIVER = properties.getProperty("reviver_message");
        YOU_REVIVED = properties.getProperty("you_revived_message");
    }
}
