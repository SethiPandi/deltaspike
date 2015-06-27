/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.deltaspike.data.impl.util.jpa;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceUnitUtil;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class OpenJpaPersistenceUnitUtilDelegate implements PersistenceUnitUtil
{
    private final PersistenceUnitUtil persistenceUnitUtil;

    public OpenJpaPersistenceUnitUtilDelegate(EntityManager entityManager)
    {
        this.persistenceUnitUtil = entityManager.getEntityManagerFactory().getPersistenceUnitUtil();
    }

    @Override
    public boolean isLoaded(Object entity, String attributeName)
    {
        return persistenceUnitUtil.isLoaded(entity, attributeName);
    }

    @Override
    public boolean isLoaded(Object entity)
    {
        return persistenceUnitUtil.isLoaded(entity);
    }

    public Object getIdentifier(Object entity)
    {
        final String methodName = "getIdObject";
        final Object identifier = persistenceUnitUtil.getIdentifier(entity);
        if (identifier != null)
        {
            final Method method;
            try
            {
                method = identifier.getClass().getMethod(methodName, null);
                return method.invoke(identifier, null);
            }
            catch (NoSuchMethodException e)
            {
                // do nothing
            }
            catch (InvocationTargetException e)
            {
                // do nothing
            }
            catch (IllegalAccessException e)
            {
                // do nothing
            }

        }
        return null;
    }
}