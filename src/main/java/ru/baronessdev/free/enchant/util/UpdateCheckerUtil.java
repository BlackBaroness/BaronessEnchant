package ru.baronessdev.free.enchant.util;

import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;

public class UpdateCheckerUtil {

    public static int check(JavaPlugin plugin) throws UpdateCheckException {
        int latest = getLatest();
        if (latest == -1) {
            throw new UpdateCheckException("Checker returned -1");
        }

        return (Integer.parseInt(plugin.getDescription().getVersion()) < latest) ? latest : -1;
    }

    private static int getLatest() throws UpdateCheckException {
        int version = -1;
        try {
            String result = EntityUtils.toString(HttpClients.createDefault().execute(
                    new HttpGet("https://market.baronessdev.ru/shop/baronessenchant.5/")
            ).getEntity());

            for (String s : result.split("\n")) {
                if (s.contains("model")) {
                    version = Integer.parseInt(s.replaceAll("[^0-9]", ""));
                    break;
                }
            }
        } catch (IOException e) {
            throw new UpdateCheckException(e.getCause().getMessage());
        }

        return version;
    }

    public static class UpdateCheckException extends Exception {

        private final String rootCause;

        public UpdateCheckException(String cause) {
            rootCause = cause;
        }

        public String getRootCause() {
            return rootCause;
        }
    }
}
