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
package io.lumeer.core.task.executor;

import io.lumeer.api.model.Collection;
import io.lumeer.api.model.rule.BlocklyRule;
import io.lumeer.core.task.RuleTask;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author <a href="mailto:marvenec@gmail.com">Martin Večeřa</a>
 */
public class BlocklyRuleTaskExecutor {

   private static Logger log = Logger.getLogger(BlocklyRuleTaskExecutor.class.getName());

   private String ruleName;
   private BlocklyRule rule;
   private RuleTask ruleTask;

   public BlocklyRuleTaskExecutor(final String ruleName, final RuleTask ruleTask) {
      this.ruleName = ruleName;
      this.rule = new BlocklyRule(ruleTask.getRule());
      this.ruleTask = ruleTask;
   }

   public void execute() {
      final JsExecutor.DocumentBridge oldDocument = new JsExecutor.DocumentBridge(ruleTask.getOldDocument());
      final JsExecutor.DocumentBridge newDocument = new JsExecutor.DocumentBridge(ruleTask.getNewDocument());
      final Map<String, Object> bindings = Map.of("oldDocument", oldDocument, "newDocument", newDocument);

      final JsExecutor jsExecutor = new JsExecutor();
      jsExecutor.setDryRun(rule.isDryRun());

      try {
         jsExecutor.execute(bindings, ruleTask, ruleTask.getCollection(), rule.getJs());

         if (!rule.isDryRun()) {
            jsExecutor.commitChanges();
         } else {
            writeDryRunResults(jsExecutor.getChanges());
         }

         checkErrorErasure();
      } catch (Exception e) {
         log.log(Level.WARNING, "Unable to execute Blockly Rule on document change: ", e);
         writeTaskError(e);
      }
   }

   private void checkErrorErasure() {
      if (rule.getError() != null && rule.getError().length() > 0 && System.currentTimeMillis() - rule.getResultTimestamp() > 3600_000) {
         rule.setError("");
         updateRule();
      }
   }

   private void writeTaskError(final Exception e) {
      try (StringWriter sw = new StringWriter(); PrintWriter pw = new PrintWriter(sw)) {
         e.printStackTrace(pw);

         rule.setError(sw.toString());
         updateRule();

         ruleTask.sendPushNotifications(ruleTask.getCollection());
      } catch (IOException ioe) {
         // we tried, cannot do more
      }
   }

   private void writeDryRunResults(final String results) {
      rule.setDryRunResult(results);
      updateRule();

      ruleTask.sendPushNotifications(ruleTask.getCollection());
   }

   private void updateRule() {
      final Collection originalCollection = ruleTask.getCollection().copy();

      rule.setResultTimestamp(System.currentTimeMillis());
      ruleTask.getCollection().getRules().put(ruleName, rule.getRule());
      ruleTask.getDaoContextSnapshot().getCollectionDao().updateCollection(ruleTask.getCollection().getId(), ruleTask.getCollection(), originalCollection);
   }
}
