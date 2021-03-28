/*
 * Lumeer: Modern Data Definition and Processing Platform
 *
 * Copyright (C) since 2017 Lumeer.io, s.r.o. and/or its affiliates.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package io.lumeer.api.util;

import io.lumeer.api.model.Attribute;
import io.lumeer.api.model.Collection;
import io.lumeer.api.model.Constraint;
import io.lumeer.api.model.Organization;
import io.lumeer.api.model.Permission;
import io.lumeer.api.model.Project;
import io.lumeer.api.model.Role;
import io.lumeer.api.model.common.Resource;

import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

public class ResourceUtils {

   private ResourceUtils() {
   }

   private static Set<String> getManagers(Resource resource) {
      return resource.getPermissions().getUserPermissions()
                     .stream()
                     .filter(permission -> permission.getRoles().contains(Role.MANAGE))
                     .map(Permission::getId)
                     .collect(Collectors.toSet());
   }

   public static Set<String> getOrganizationManagers(Organization organization) {
      return getManagers(organization);
   }

   public static Set<String> getProjectManagers(Organization organization, Project project) {
      var managers = getManagers(project);
      managers.retainAll(getReaders(organization));
      return managers;
   }

   public static Set<String> getResourceManagers(Organization organization, Project project, Resource resource) {
      var resourceManagers = getManagers(resource);
      resourceManagers.retainAll(getProjectReaders(organization, project));
      resourceManagers.addAll(getProjectManagers(organization, project));

      return resourceManagers;
   }

   public static Set<String> getCollectionManagers(Organization organization, Project project, Collection collection, Set<String> managersFromViews) {
      var collectionManagers = getManagers(collection);
      collectionManagers.addAll(managersFromViews);
      collectionManagers.retainAll(getProjectReaders(organization, project));
      collectionManagers.addAll(getProjectManagers(organization, project));

      return collectionManagers;
   }

   public static Set<String> getResourceReaders(Organization organization, Project project, Resource resource) {
      var resourceReaders = getReaders(resource);
      resourceReaders.retainAll(getProjectReaders(organization, project));
      resourceReaders.addAll(getProjectManagers(organization, project));

      return resourceReaders;
   }

   public static Set<String> getCollectionReaders(Organization organization, Project project, Collection collection, Set<String> readersFromViews) {
      var collectionReaders = getReaders(collection);
      collectionReaders.addAll(readersFromViews);
      collectionReaders.retainAll(getProjectReaders(organization, project));
      collectionReaders.addAll(getProjectManagers(organization, project));

      return collectionReaders;
   }

   public static Set<String> getReaders(Resource resource) {
      return resource.getPermissions().getUserPermissions().stream()
                     .filter(ResourceUtils::canReadByPermission)
                     .map(Permission::getId)
                     .collect(Collectors.toSet());
   }

   public static Set<String> getProjectReaders(Organization organization, Project project) {
      var managers = getReaders(project);
      managers.retainAll(getReaders(organization));
      return managers;
   }

   public static Set<String> getAddedPermissions(final Resource originalResource, final Resource updatedResource) {
      return getPermissionsDifference(updatedResource, originalResource);
   }

   public static Set<String> getRemovedPermissions(final Resource originalResource, final Resource updatedResource) {
      return getPermissionsDifference(originalResource, updatedResource);
   }

   private static Set<String> getPermissionsDifference(final Resource resource1, final Resource resource2) {
      if (resource1 == null || resource2 == null) {
         return Collections.emptySet();
      }
      Set<Permission> permissions1 = resource1.getPermissions().getUserPermissions();
      Set<Permission> permissions2 = resource2.getPermissions().getUserPermissions();
      // TODO groups

      Set<String> users = permissions1.stream().filter(ResourceUtils::canReadByPermission).map(Permission::getId).collect(Collectors.toSet());
      users.removeAll(permissions2.stream().filter(ResourceUtils::canReadByPermission).map(Permission::getId).collect(Collectors.toSet()));
      return users;
   }

   public static boolean canReadByPermission(Permission permission) {
      return permission.getRoles().contains(Role.READ) || canManageByPermission(permission);
   }

   public static boolean canManageByPermission(Permission permission) {
      return permission.getRoles().contains(Role.MANAGE);
   }

   public static java.util.Collection<Attribute> incOrDecAttributes(java.util.Collection<Attribute> attributes, Set<String> attributesIdsToInc, Set<String> attributesIdsToDec) {
      Map<String, Attribute> oldAttributes = attributes.stream()
                                                       .collect(Collectors.toMap(Attribute::getId, Function.identity()));
      oldAttributes.keySet().forEach(attributeId -> {
         if (attributesIdsToInc.contains(attributeId)) {
            Attribute attribute = oldAttributes.get(attributeId);
            attribute.setUsageCount(attribute.getUsageCount() + 1);
         } else if (attributesIdsToDec.contains(attributeId)) {
            Attribute attribute = oldAttributes.get(attributeId);
            attribute.setUsageCount(Math.max(attribute.getUsageCount() - 1, 0));
         }

      });

      return oldAttributes.values();
   }

   public static java.util.Collection<Attribute> incAttributes(java.util.Collection<Attribute> attributes, Map<String, Integer> attributesToInc) {
      Map<String, Attribute> oldAttributes = attributes.stream()
                                                       .collect(Collectors.toMap(Attribute::getId, Function.identity()));
      oldAttributes.keySet().forEach(attributeId -> {
            Attribute attribute = oldAttributes.get(attributeId);
            attribute.setUsageCount(attribute.getUsageCount() + attributesToInc.computeIfAbsent(attributeId, aId -> 0));
      });

      return oldAttributes.values();
   }


   public static Constraint findConstraint(java.util.Collection<Attribute> attributes, String attributeId) {
      Attribute attribute = findAttribute(attributes, attributeId);
      if (attribute != null) {
         return attribute.getConstraint();
      }
      return null;
   }

   public static Attribute findAttribute(java.util.Collection<Attribute> attributes, String attributeId) {
      if (attributes == null || attributeId == null) {
         return null;
      }
      for (Attribute attribute : attributes) {
         if (attribute.getId().equals(attributeId)) {
            return attribute;
         }
      }
      return null;
   }
}
