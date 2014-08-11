/*
 * Copyright 2014 Blazebit.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.blazebit.testsuite.base;

import java.util.Properties;

/**
 *
 * @author Christian Beikov
 * @since 1.0
 */
public abstract class AbstractPersistenceTest extends AbstractJpaPersistenceTest {

    @Override
    protected Properties applyProperties(Properties properties) {
        properties.put("openjpa.RuntimeUnenhancedClasses", "supported");
        properties.put("openjpa.jdbc.DBDictionary", "h2");
        properties.put("openjpa.jdbc.SynchronizeMappings", "buildSchema(ForeignKeys=true)");
        properties.put("openjpa.Log", "DefaultLevel=WARN, Tool=INFO, SQL=TRACE");
        properties.put("openjpa.jdbc.MappingDefaults", "ForeignKeyDeleteAction=restrict,JoinForeignKeyDeleteAction=restrict");
        return properties;
    }

}