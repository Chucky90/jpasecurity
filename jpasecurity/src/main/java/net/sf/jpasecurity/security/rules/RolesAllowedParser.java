/*
 * Copyright 2008, 2009 Arne Limburg
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

import java.util.HashMap;
import java.util.Map;

import net.sf.jpasecurity.AccessType;
import net.sf.jpasecurity.security.RoleAllowed;
import net.sf.jpasecurity.security.RolesAllowed;
import net.sf.jpasecurity.util.AbstractAnnotationParser;

/**
 * <strong>This class is not thread-safe.</strong>
 * @author Arne Limburg
 */
public class RolesAllowedParser extends AbstractAnnotationParser<RolesAllowed> {

    private Map<String, AccessType[]> rolesAllowed = new HashMap<String, AccessType[]>();

    public Map<String, AccessType[]> parseAllowedRoles(Class<?>... classes) {
        rolesAllowed.clear();
        EjbRolesAllowedParser rolesAllowedParser = new EjbRolesAllowedParser();
        for (String role: rolesAllowedParser.parseAllowedRoles(classes)) {
            rolesAllowed.put(role, AccessType.ALL);
        }
        RoleAllowedParser roleAllowedParser = new RoleAllowedParser();
        rolesAllowed.putAll(roleAllowedParser.parseAllowedRoles(classes));
        parse(classes);
        return rolesAllowed;
    }

    protected void process(RolesAllowed annotation) {
        for (RoleAllowed roleAllowed: annotation.value()) {
            rolesAllowed.put(roleAllowed.role(), roleAllowed.access());
        }
        for (String role: annotation.roles()) {
            rolesAllowed.put(role, annotation.access());
        }
    }
}
