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
package org.apache.karaf.log.command;

import org.apache.karaf.shell.commands.Argument;
import org.apache.karaf.shell.commands.Command;

/**
 * Get the log level for a given logger
 */
@Command(scope = "log", name = "get", description = "Shows the currently set log level.")
public class GetLogLevel extends LogCommandSupport {

    @Argument(index = 0, name = "logger", description = "The name of the logger, ALL or ROOT (default)", required = false, multiValued = false)
    String logger;

    protected Object doExecute() throws Exception {
        System.out.println(logService.getLevelSt(logger));
        return null;
    }

}
