/*
 * Copyright (c) 2013 Mark Prichard, CloudBees
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.cloudbees.gasp.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

public class Config implements ServletContextListener {
    private static final Logger LOGGER = LoggerFactory.getLogger(Config.class.getName());
    private static String p12Pwd;
    private static String token;

    public static String getP12Pwd() {
        return p12Pwd;
    }

    public static String getToken() {
        return token;
    }

    /**
     * Initialize servlet with GCM API Key: for convenience, we will load this from system property,
     * environment variable or via the Jenkins build secret plugin (upload file gcm-api-key.env). In
     * each case the property/variable is GCM_API_KEY
     * Get the API key from http://code.google.com/apis/console
     *
     * @param event
     */
    public void contextInitialized(ServletContextEvent event) {
        try {
            //TODO: (temporary fix) get device token for testing from environment
            //TODO: Devices will register tokens from didRegisterForRemoteNotificationsWithDeviceToken
            if ((token = System.getProperty("APNS_TOKEN")) != null) {
                Datastore.register(token);
                LOGGER.debug("APNS device token: " + token);
            }
            else {
                LOGGER.error("APNS device token not set");
            }


            //TODO: tidy up and add Jenkins build secret support
            if ((p12Pwd = System.getProperty("P12_PWD")) != null) {
                LOGGER.debug("Loaded P12_PWD from system property");
            }
            else {
                LOGGER.error("P12_PWD not set");
            }
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }
    public void contextDestroyed(ServletContextEvent event) {
    }
}