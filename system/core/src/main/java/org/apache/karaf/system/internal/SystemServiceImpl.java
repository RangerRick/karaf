/*
* Licensed to the Apache Software Foundation (ASF) under one or more
* contributor license agreements.  See the NOTICE file distributed with
* this work for additional information regarding copyright ownership.
* The ASF licenses this file to You under the Apache License, Version 2.0
* (the "License"); you may not use this file except in compliance with
* the License.  You may obtain a copy of the License at
*
*      http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/
package org.apache.karaf.system.internal;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Calendar;
import java.util.GregorianCalendar;

import org.apache.felix.utils.properties.Properties;
import org.apache.karaf.system.SystemService;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.startlevel.FrameworkStartLevel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implementation of the system service.
 */
public class SystemServiceImpl implements SystemService {

    private static final Logger LOGGER = LoggerFactory.getLogger(SystemServiceImpl.class);

    private BundleContext bundleContext;

    public void setBundleContext(BundleContext bundleContext) {
        this.bundleContext = bundleContext;
    }

    public BundleContext getBundleContext() {
        return this.bundleContext;
    }

    public void halt() throws Exception {
        halt(null);
    }

    public void halt(String time) throws Exception {
        shutdown(timeToSleep(time));
    }

    public void reboot() throws Exception {
        reboot(null, false);
    }

    public void reboot(String time, boolean cleanup) throws Exception {
        reboot(timeToSleep(time), true);
    }

    private void shutdown(final long sleep) {
        new Thread() {
            public void run() {
                try {
                    if (sleep > 0) {
                        LOGGER.info("Shutdown in " + sleep / 1000 / 60 + " minute(s)");
                        System.err.println("Shutdown in " + sleep / 1000 / 60 + " minutes(s)");
                    }
                    Thread.sleep(sleep);
                    Bundle bundle = getBundleContext().getBundle(0);
                    bundle.stop();
                } catch (Exception e) {
                    LOGGER.error("Halt error", e);
                }
            }
        }.start();
    }

    private void reboot(final long sleep, final boolean clean) {
        new Thread() {
            public void run() {
                try {
                    if (sleep > 0) {
                        LOGGER.info("Reboot in " + sleep / 1000 / 60 + " minute(s)");
                        System.err.println("Reboot in " + sleep / 1000 / 60 + " minute(s)");
                    }
                    Thread.sleep(sleep);
                    System.setProperty("karaf.restart", "true");
                    System.setProperty("karaf.restart.clean", Boolean.toString(clean));
                    bundleContext.getBundle(0).stop();
                } catch (Exception e) {
                    LOGGER.error("Reboot error", e);
                }
            }
        }.start();
    }

    public void setStartLevel(int startLevel) throws Exception {
        getBundleContext().getBundle(0).adapt(FrameworkStartLevel.class).setStartLevel(startLevel);
    }

    public int getStartLevel() throws Exception {
        return getBundleContext().getBundle(0).adapt(FrameworkStartLevel.class).getStartLevel();
    }

    /**
     * Convert a time string to sleep period (in millisecond).
     *
     * @param time the time string.
     * @return the corresponding sleep period in millisecond.
     */
    private long timeToSleep(String time) throws Exception {
        long sleep = 0;
        if (time != null) {
            if (!time.equals("now")) {
                if (time.startsWith("+")) {
                    // delay in number of minutes provided
                    time = time.substring(1);
                    try {
                        sleep = Long.parseLong(time) * 60 * 1000;
                    } catch (Exception e) {
                        throw new IllegalArgumentException("Time " + time + " is not valid");
                    }
                } else {
                    // try to parse the date in hh:mm
                    String[] strings = time.split(":");
                    if (strings.length != 2) {
                        throw new IllegalArgumentException("Time " + time + " is not valid");
                    }
                    GregorianCalendar currentDate = new GregorianCalendar();
                    GregorianCalendar shutdownDate = new GregorianCalendar(currentDate.get(Calendar.YEAR), currentDate.get(Calendar.MONTH), currentDate.get(Calendar.DATE), Integer.parseInt(strings[0]), Integer.parseInt(strings[1]));
                    if (shutdownDate.before(currentDate)) {
                        shutdownDate.set(Calendar.DATE, shutdownDate.get(Calendar.DATE) + 1);
                    }
                    sleep = shutdownDate.getTimeInMillis() - currentDate.getTimeInMillis();
                }
            }
        }
        return sleep;
    }

    @Override
    public String getName() {
        return bundleContext.getProperty("karaf.name");
    }

    @Override
    public void setName(String name) {
        try {
            String karafBase = bundleContext.getProperty("karaf.base");
            File etcDir = new File(karafBase, "etc");
            File syspropsFile = new File(etcDir, "system.properties");
            FileInputStream fis = new FileInputStream(syspropsFile);
            Properties props = new Properties();
            props.load(fis);
            fis.close();
            props.setProperty("karaf.name", name);
            FileOutputStream fos = new FileOutputStream(syspropsFile);
            props.store(fos, "");
            fos.close();
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

}
