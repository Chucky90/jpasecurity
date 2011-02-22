/*
 * Copyright 2008 - 2011 Arne Limburg
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions
 * and limitations under the License.
 */
package net.sf.jpasecurity.persistence;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.spi.PersistenceUnitInfo;

import net.sf.jpasecurity.configuration.Configuration;
import net.sf.jpasecurity.mapping.MappingInformation;
import net.sf.jpasecurity.mapping.PersistenceInformationReceiver;
import net.sf.jpasecurity.persistence.mapping.JpaAnnotationParser;
import net.sf.jpasecurity.persistence.mapping.OrmXmlParser;

/**
 * This class is a factory that creates {@link net.sf.jpasecurity.SecureEntityManager}s.
 * @author Arne Limburg
 */
public class SecureEntityManagerFactory implements EntityManagerFactory {

    private EntityManagerFactory nativeEntityManagerFactory;
    private MappingInformation mappingInformation;
    private Configuration configuration;

    protected SecureEntityManagerFactory(EntityManagerFactory entityManagerFactory,
                                         PersistenceUnitInfo persistenceUnitInfo,
                                         Map<String, String> properties,
                                         Configuration configuration) {
        this.nativeEntityManagerFactory = entityManagerFactory;
        if (entityManagerFactory == null) {
            throw new IllegalArgumentException("entityManagerFactory may not be null");
        }
        if (persistenceUnitInfo == null) {
            throw new IllegalArgumentException("persistenceUnitInfo may not be null");
        }
        if (configuration == null) {
            throw new IllegalArgumentException("configuration may not be null");
        }
        this.configuration = configuration;
        JpaAnnotationParser annotationParser
            = new JpaAnnotationParser(configuration.getPropertyAccessStrategyFactory(),
                                      configuration.getExceptionFactory());
        OrmXmlParser xmlParser = new OrmXmlParser(configuration.getPropertyAccessStrategyFactory(),
                                                  configuration.getExceptionFactory());
        this.mappingInformation = annotationParser.parse(persistenceUnitInfo);
        this.mappingInformation = xmlParser.parse(persistenceUnitInfo, mappingInformation);
        Map<String, String> persistenceProperties
            = new HashMap<String, String>((Map)persistenceUnitInfo.getProperties());
        if (properties != null) {
            persistenceProperties.putAll(properties);
        }
        injectPersistenceInformation(persistenceProperties);
    }

    protected Configuration getConfiguration() {
        return configuration;
    }

    public EntityManager createEntityManager() {
        return createSecureEntityManager(nativeEntityManagerFactory.createEntityManager(), Collections.EMPTY_MAP);
    }

    public EntityManager createEntityManager(Map map) {
        return createSecureEntityManager(nativeEntityManagerFactory.createEntityManager(map), map);
    }

    public boolean isOpen() {
        return nativeEntityManagerFactory.isOpen();
    }

    public void close() {
        configuration = null;
        nativeEntityManagerFactory.close();
    }

    protected EntityManager createSecureEntityManager(EntityManager entityManager, Map<String, String> properties) {
        return new DefaultSecureEntityManager(entityManager, configuration, mappingInformation);
    }

    private void injectPersistenceInformation(Map<String, String> persistenceProperties) {
        persistenceProperties = Collections.unmodifiableMap(persistenceProperties);
        injectPersistenceInformation(configuration.getSecurityContext(), persistenceProperties);
        injectPersistenceInformation(configuration.getAccessRulesProvider(), persistenceProperties);
    }

    private void injectPersistenceInformation(Object injectionTarget, Map<String, String> persistenceProperties) {
        if (injectionTarget instanceof PersistenceInformationReceiver) {
            PersistenceInformationReceiver persistenceInformationReceiver
                = (PersistenceInformationReceiver)injectionTarget;
            persistenceInformationReceiver.setPersistenceProperties(persistenceProperties);
            persistenceInformationReceiver.setPersistenceMapping(mappingInformation);
        }
        if (injectionTarget instanceof SecurityContextReceiver) {
            SecurityContextReceiver securityContextReceiver = (SecurityContextReceiver)injectionTarget;
            securityContextReceiver.setSecurityContext(configuration.getSecurityContext());
        }
        if (injectionTarget instanceof ConfigurationReceiver) {
            ConfigurationReceiver configurationReceiver = (ConfigurationReceiver)injectionTarget;
            configurationReceiver.setConfiguration(configuration);
        }
    }

    protected MappingInformation getMappingInformation() {
        return mappingInformation;
    }
}