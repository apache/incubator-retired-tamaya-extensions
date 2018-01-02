/*
 * Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.apache.tamaya.ext.examples.events;

import org.apache.tamaya.events.ConfigEvent;
import org.apache.tamaya.events.ConfigEventListener;
import org.apache.tamaya.events.ConfigEventManager;
import org.apache.tamaya.events.ConfigChange;
import org.joda.time.DateTime;
import org.joda.time.Duration;

import javax.config.Config;
import javax.config.ConfigProvider;
import java.beans.PropertyChangeEvent;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.*;
import java.util.*;
import java.util.logging.LogManager;
import java.util.logging.Logger;

import static java.lang.System.out;
import static java.util.Arrays.asList;

public class Main {
    private static final Duration EXAMPLE_RUNTIME = Duration.standardSeconds(300L);

    /*
     * Turns off all logging.
     */
    static {
        LogManager.getLogManager().reset();
        Logger globalLogger = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);
        globalLogger.setLevel(java.util.logging.Level.OFF);
    }

    private Main() {
    }

    public static void main(String[] args) throws Exception {
        DateTime end = DateTime.now().plus(EXAMPLE_RUNTIME);

        Timer timer = new Timer();
        timer.schedule(new PropertiesFileWritingTask(), 0L, 5_000L);

        installCleanupHook(getPropertiesFilePath());

        ConfigEventManager.addListener(new ConfigurationChangeListener());
        ConfigEventManager.setChangeMonitoringPeriod(1_000L);
        ConfigEventManager.enableChangeMonitoring(true);
        Config config = ConfigProvider.getConfig();

        for (String key : config.getPropertyNames()) {
            System.out.println(key + ": " + config.getValue(key, String.class));
        }



        out.println("****************************************************");
        out.println("File observer example");
        out.println("****************************************************");
        out.println();
        out.println("Configuration source is: " + getPropertiesFilePath());
        out.println();

        Thread.sleep(EXAMPLE_RUNTIME.getMillis());

        timer.cancel();
    }

    private static void installCleanupHook(final Path path) {
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                try {
                    Files.delete(path);
                    out.println("Removed " + path);
                } catch (IOException e) {
                    throw new RuntimeException("Failed to delete " + path, e);
                }
            }
        });
    }

    static Path getPropertiesFilePath() {
        String tempDir = System.getProperty("java.io.tmpdir");

        return Paths.get(tempDir, "fileobserver.properties");
    }

    private static class ConfigurationChangeListener implements ConfigEventListener {
        @Override
        public void onConfigEvent(ConfigEvent<?> event) {

            ConfigChange c = (ConfigChange) event;

            if (c.isKeyAffected("a")) {
                // Looking for the change event of property a. Not recomanded
                // for production.
                Object newValue = null;
                Object oldValue = null;

                for (PropertyChangeEvent change : c.getChanges()) {
                    if ("a".equals(change.getPropertyName())) {
                        oldValue = change.getOldValue();
                        newValue = change.getNewValue();
                        break;
                    }
                }

                if (oldValue != null) {
                    out.println("Value for key a changed (" + oldValue + " (old) => " + newValue + " (new))");
                } else {
                    out.println("Value for key a changed (" + newValue + " (new))");
                }

            }

        }
    }

    private static class ContentProvider implements Iterable<String> {
        private long value;

        public void setValue(long val) {
            this.value = val;
        }

        public long getValue() {
            return value;
        }

        @Override
        public Iterator<String> iterator() {
            List<String> list = asList("# Generated file", "a="+ getValue());

            return list.iterator();
        }
    }

    private static class PropertiesFileWritingTask extends TimerTask {
        private static ContentProvider contentProvider = new ContentProvider();

        @Override
        public void run() {

            try {
                contentProvider.setValue(System.currentTimeMillis());

                Files.write(getPropertiesFilePath(), contentProvider,
                            Charset.defaultCharset(), StandardOpenOption.CREATE);
            }
            catch (IOException e) {
                throw new RuntimeException("Failed to write properties file.", e);
            }

        }
    }
}
