/*
 * Copyright 2008 Arne Limburg
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
package net.sf.jpasecurity.security.rules;

import java.util.Map;
import java.util.Set;

import javax.persistence.PersistenceException;

import net.sf.jpasecurity.jpql.compiler.JpqlCompiler;
import net.sf.jpasecurity.jpql.parser.JpqlAccessRule;
import net.sf.jpasecurity.mapping.MappingInformation;
import net.sf.jpasecurity.security.AccessRule;

/**
 * This compiler compiles access rules
 * @author Arne Limburg
 */
public class AccessRulesCompiler extends JpqlCompiler {

    public AccessRulesCompiler(MappingInformation mappingInformation) {
        super(mappingInformation);
    }

    public AccessRule compile(JpqlAccessRule rule) {
        Map<String, Class<?>> aliasTypes = getAliasTypes(rule);
        if (aliasTypes.size() > 1) {
            throw new IllegalStateException("An access rule may have only on alias specified");
        }
        Map.Entry<String, Class<?>> aliasType = aliasTypes.entrySet().iterator().next();
        Set<String> namedParameters = getNamedParameters(rule);
        if (namedParameters.size() > 0) {
            throw new PersistenceException("Named parameters are not allowed for access rules");
        }
        if (getPositionalParameters(rule).size() > 0) {
            throw new PersistenceException("Positional parameters are not allowed for access rules");
        }
        return new AccessRule(rule, aliasType.getKey(), aliasType.getValue(), namedParameters);
    }
}