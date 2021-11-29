package org.tucke;

import org.tucke.config.AppConfig;
import org.tucke.config.LoggerConfig;
import org.tucke.gnsscenter.GnssCenterService;
import org.tucke.jtt809.Jtt809Server;

/**
 * @author tucke
 */
public class MainApplication {

    private static void loadConfiguration(String[] args) {
        AppConfig.load(args);
        LoggerConfig.load();
    }

    private static void addHooks() {
        Runtime.getRuntime().addShutdownHook(new Thread(MainApplication::exit));
    }

    private static void startService() throws Exception {
        GnssCenterService.getInstance().start();
        Jtt809Server.getInstance().start();
    }

    private static void exit() {
        Jtt809Server.getInstance().stop();
        GnssCenterService.getInstance().stop();
    }

    public static void main(String[] args) throws Exception {
        loadConfiguration(args);
        addHooks();
        startService();
    }

}
