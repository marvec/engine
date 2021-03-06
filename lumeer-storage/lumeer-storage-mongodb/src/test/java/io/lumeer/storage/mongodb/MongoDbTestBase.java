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
package io.lumeer.storage.mongodb;

import static io.lumeer.storage.mongodb.EmbeddedMongoDb.*;

import io.lumeer.engine.api.data.StorageConnection;

import com.mongodb.client.MongoDatabase;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;

public abstract class MongoDbTestBase {

   private static EmbeddedMongoDb embeddedMongoDb;

   protected MongoDbStorage mongoDbStorage;

   protected MongoDatabase database;

   @BeforeClass
   public static void startEmbeddedMongoDb() {
      if (!SKIP) {
         embeddedMongoDb = new EmbeddedMongoDb();
         embeddedMongoDb.start();
      }
   }

   @AfterClass
   public static void stopEmbeddedMongoDb() {
      if (embeddedMongoDb != null) {
         embeddedMongoDb.stop();
      }
   }

   @Before
   public void connectMongoDbStorage() {
      mongoDbStorage = new MongoDbStorage();
      mongoDbStorage.connect(new StorageConnection(HOST, PORT, USER, PASSWORD), NAME, SSL);
      database = mongoDbStorage.getDatabase();
      database.drop();
   }

   @After
   public void disconnectMongoDbStorage() {
      if (mongoDbStorage != null) {
         mongoDbStorage.disconnect();
      }
   }
}
