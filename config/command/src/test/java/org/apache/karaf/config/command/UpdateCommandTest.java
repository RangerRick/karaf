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
package org.apache.karaf.config.command;


import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;

import java.util.Properties;

import junit.framework.TestCase;

import org.apache.felix.service.command.CommandSession;
import org.apache.karaf.config.core.impl.ConfigRepositoryImpl;
import org.easymock.EasyMock;
import org.osgi.framework.BundleContext;
import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;

/**
 * Test cases for {@link EditCommand}
 */
public class UpdateCommandTest extends TestCase {

    private static final String PID = "my.test.persistent.id-other";

    private UpdateCommand command;
    private BundleContext context;
    private ConfigurationAdmin admin;
    private CommandSession session;

    @Override
    protected void setUp() throws Exception {
        command = new UpdateCommand();

        context = EasyMock.createMock(BundleContext.class);
        command.setBundleContext(context);

        admin = createMock(ConfigurationAdmin.class);
        command.setConfigRepository(new ConfigRepositoryImpl(admin));
        expect(context.getBundle(0)).andReturn(null).anyTimes();

        replay(context);

        session = new MockCommandSession();
    }

    public void testupdateOnNewFactoryPid() throws Exception {
        Properties props = new Properties();

        session.put(ConfigCommandSupport.PROPERTY_CONFIG_PID, PID);
        session.put(ConfigCommandSupport.PROPERTY_CONFIG_PROPS, props);

        Configuration configNew = createMock(Configuration.class);
        expect(admin.getConfiguration(PID, null)).andReturn(configNew);
        expect(configNew.getProperties()).andReturn(null);


        Configuration configFac = createMock(Configuration.class);
        expect(admin.createFactoryConfiguration(PID.substring(0, PID.indexOf('-')), null)).andReturn(configFac);
        configFac.update(props);
        expect(configFac.getBundleLocation()).andReturn(null);
        replay(admin);
        replay(configNew);
        replay(configFac);

        command.execute(session);

    }

}
