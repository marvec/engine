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
package io.lumeer.core.task;

import io.lumeer.api.model.Document;
import io.lumeer.api.model.LinkInstance;
import io.lumeer.core.task.executor.ChangesTracker;

import java.util.List;

public class SearchTask implements Task {

   private String query;
   private int recursionDepth;

   public SearchTask(final String query, final int recursionDepth) {
      this.query = query;
      this.recursionDepth = recursionDepth;
   }

   @Override
   public void setParent(final Task task) {

   }

   @Override
   public Task getParent() {
      return null;
   }

   @Override
   public void process(final TaskExecutor executor, final ChangesTracker changesTracker) {
      // TBD
   }

   @Override
   public void propagateChanges(final List<Document> documents, final List<LinkInstance> links) {
   }

   @Override
   public void processChanges(final ChangesTracker changesTracker) {
      // TBD
   }

   @Override
   public int getRecursionDepth() {
      return recursionDepth;
   }
}
