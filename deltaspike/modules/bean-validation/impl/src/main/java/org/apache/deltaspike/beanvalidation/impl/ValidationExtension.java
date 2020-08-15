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
package org.apache.deltaspike.beanvalidation.impl;

import org.apache.deltaspike.core.api.config.Configuration;
import org.apache.deltaspike.core.api.provider.BeanProvider;

import javax.enterprise.event.Observes;
import javax.enterprise.inject.spi.AfterDeploymentValidation;
import javax.enterprise.inject.spi.AnnotatedType;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.Extension;
import javax.enterprise.inject.spi.ProcessAnnotatedType;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import javax.validation.metadata.BeanDescriptor;
import javax.validation.metadata.PropertyDescriptor;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * This is a CDI extension that will validate {@link javax.validation.Constraint}
 * annotations found on certain beans that are added during initialization.
 *
 * @since 1.9.5
 */
public class ValidationExtension implements Extension
{
    /** The validator instance to validate object properties. */
    private final Validator validator;

    /**
     * During initialization, all types which are to be included in validation
     * at startup will appear here on {@link ProcessAnnotatedType}.
     */
    private final List<Class<?>> typesToValidate;

    /**
     * Initialize the extension.
     */
    public ValidationExtension()
    {
        typesToValidate = new ArrayList<>();

        final ValidatorFactory factory = Validation.byDefaultProvider()
            .configure()
            .constraintValidatorFactory(new CDIAwareConstraintValidatorFactory())
            .buildValidatorFactory();

        this.validator = factory.getValidator();
    }

    /**
     * Add all supported types to {@link #typesToValidate}.
     * All types will always have at least one {@link javax.validation.Constraint} to validate.
     *
     * @param processAnnotatedType
     */
    public void processAnnotatedType(@Observes final ProcessAnnotatedType<?> processAnnotatedType)
    {
        final AnnotatedType<?> annotatedType = processAnnotatedType.getAnnotatedType();

        if (!annotatedType.isAnnotationPresent(Configuration.class))
        {
            return;
        }

        final Class<?> javaClazz = annotatedType.getJavaClass();

        if (!javaClazz.isInterface())
        {
            return;
        }

        final BeanDescriptor beanDescriptor = validator.getConstraintsForClass(javaClazz);

        if (beanDescriptor.isBeanConstrained())
        {
            typesToValidate.add(javaClazz);
        }
    }

    /**
     * After all types have been processed, actually perform the validation here.
     * This will {@link AfterDeploymentValidation#addDeploymentProblem(Throwable) add deployment problems}
     * if any of the validation {@link javax.validation.Constraint}s are invalidated.
     *
     * @param afterDeploymentValidation
     * @param beanManager
     */
    public void afterDeploymentValidation(@Observes AfterDeploymentValidation afterDeploymentValidation,
                                          final BeanManager beanManager)
    {
        for (Class<?> typeToValidate : typesToValidate)
        {
            final BeanDescriptor descriptor = validator.getConstraintsForClass(typeToValidate);
            final Set<PropertyDescriptor> propertyDescriptors = descriptor.getConstrainedProperties();

            for (PropertyDescriptor propertyDescriptor : propertyDescriptors)
            {
                final Object typeImpl = BeanProvider.getContextualReference(beanManager,
                    typeToValidate, false);
                final String propertyName = propertyDescriptor.getPropertyName();

                Set<ConstraintViolation<?>> violations =
                    (Set<ConstraintViolation<?>>)(Object)validator.validateProperty(typeImpl, propertyName);

                if (!violations.isEmpty())
                {
                    ConstraintViolationException ex = new ConstraintViolationException(
                        "Validation of initial configuration values failed.", violations);

                    afterDeploymentValidation.addDeploymentProblem(ex);
                }
            }
        }
    }
}
