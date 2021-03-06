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
package myservice.mynamespace.data;

import java.io.File;
import java.io.FileOutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Properties;

import org.apache.olingo.commons.api.data.Entity;
import org.apache.olingo.commons.api.data.EntityCollection;
import org.apache.olingo.commons.api.data.Property;
import org.apache.olingo.commons.api.data.ValueType;
import org.apache.olingo.commons.api.edm.EdmEntitySet;
import org.apache.olingo.commons.api.edm.EdmEntityType;
import org.apache.olingo.commons.api.edm.EdmKeyPropertyRef;
import org.apache.olingo.commons.api.edm.FullQualifiedName;
import org.apache.olingo.commons.api.ex.ODataRuntimeException;
import org.apache.olingo.commons.api.http.HttpMethod;
import org.apache.olingo.commons.api.http.HttpStatusCode;
import org.apache.olingo.server.api.ODataApplicationException;
import org.apache.olingo.server.api.uri.UriParameter;

import com.sap.conn.jco.AbapException;
import com.sap.conn.jco.JCoDestination;
import com.sap.conn.jco.JCoDestinationManager;
import com.sap.conn.jco.JCoException;
import com.sap.conn.jco.JCoFunction;
import com.sap.conn.jco.JCoTable;

import myservice.mynamespace.jco.TestJco;
import myservice.mynamespace.service.DemoEdmProvider;
import myservice.mynamespace.util.Util;

public class Storage {

	// represent our database
	private List<Entity> productList;
	private List<Entity> categoryList;
	private List<Entity> appconfgList;
	private List<Entity> dataconfgList;
	private List<Entity> floorplanList;
	private List<Entity> zempList;
	private List<Entity> dpconfigList;

	public Storage() {
		productList = new ArrayList<Entity>();
		categoryList = new ArrayList<Entity>();
		appconfgList = new ArrayList<Entity>();
		dataconfgList = new ArrayList<Entity>();
		floorplanList = new ArrayList<Entity>();
		dpconfigList = new ArrayList<Entity>();
		zempList = new ArrayList<Entity>();

		// creating some sample data
		initProductSampleData();
		initCategorySampleData();
		initAppConfg();
		initDataConf();
		initFloorPlan();
		initDataProvider();
		initZemp();
	}

	/* PUBLIC FACADE */

	public EntityCollection readEntitySetData(EdmEntitySet edmEntitySet) {
		EntityCollection entitySet = null;

		if (edmEntitySet.getName().equals(DemoEdmProvider.ES_PRODUCTS_NAME)) {
			entitySet = getProducts();
		} else if (edmEntitySet.getName().equals(DemoEdmProvider.ES_CATEGORIES_NAME)) {
			entitySet = getCategories();
		} else if (edmEntitySet.getName().equals(DemoEdmProvider.ES_APPCONFG_NAME)) {
			entitySet = getAppConfg();
		} else if (edmEntitySet.getName().equals(DemoEdmProvider.ES_DATACONF_NAME)) {
			entitySet = getDataConfg();
		} else if (edmEntitySet.getName().equals(DemoEdmProvider.ES_FLOORPLAN_NAME)) {
			entitySet = getFloorPlan();
		} else if (edmEntitySet.getName().equals(DemoEdmProvider.ES_ZEMP_NAME)) {
			entitySet = getZempPlan();
		} else if (edmEntitySet.getName().equals(DemoEdmProvider.ES_DP_NAME)) {
			entitySet = getDataProvider();
		}

		return entitySet;
	}

	public Entity readEntityData(EdmEntitySet edmEntitySet, List<UriParameter> keyParams) {
		Entity entity = null;

		EdmEntityType edmEntityType = edmEntitySet.getEntityType();

		if (edmEntityType.getName().equals(DemoEdmProvider.ET_PRODUCT_NAME)) {
			entity = getProduct(edmEntityType, keyParams);
		} else if (edmEntityType.getName().equals(DemoEdmProvider.ET_CATEGORY_NAME)) {
			entity = getCategory(edmEntityType, keyParams);
		} else if (edmEntityType.getName().equals(DemoEdmProvider.ET_APPCONFG_NAME)) {
			entity = getAppConfg(edmEntityType, keyParams);
		} else if (edmEntityType.getName().equals(DemoEdmProvider.ET_DATACONF_NAME)) {
			entity = getDataConfg(edmEntityType, keyParams);
		} else if (edmEntityType.getName().equals(DemoEdmProvider.ET_FLOORPLAN_NAME)) {
			entity = getFloorPlan(edmEntityType, keyParams);
		} else if (edmEntityType.getName().equals(DemoEdmProvider.ET_ZEMP_NAME)) {
			entity = getZempPlan(edmEntityType, keyParams);
		} else if (edmEntityType.getName().equals(DemoEdmProvider.ET_DP_NAME)) {
			entity = getDataProvider(edmEntityType, keyParams);
		}

		return entity;
	}

	// Navigation

	public Entity getRelatedEntity(Entity entity, EdmEntityType relatedEntityType) {
		EntityCollection collection = getRelatedEntityCollection(entity, relatedEntityType);
		if (collection.getEntities().isEmpty()) {
			return null;
		}
		return collection.getEntities().get(0);
	}

	public Entity getRelatedEntity(Entity entity, EdmEntityType relatedEntityType, List<UriParameter> keyPredicates) {

		EntityCollection relatedEntities = getRelatedEntityCollection(entity, relatedEntityType);
		return Util.findEntity(relatedEntityType, relatedEntities, keyPredicates);
	}

	public EntityCollection getRelatedEntityCollection(Entity sourceEntity, EdmEntityType targetEntityType) {
		EntityCollection navigationTargetEntityCollection = new EntityCollection();

		FullQualifiedName relatedEntityFqn = targetEntityType.getFullQualifiedName();
		String sourceEntityFqn = sourceEntity.getType();

		if (sourceEntityFqn.equals(DemoEdmProvider.ET_PRODUCT_FQN.getFullQualifiedNameAsString())
				&& relatedEntityFqn.equals(DemoEdmProvider.ET_CATEGORY_FQN)) {
			// relation Products->Category (result all categories)
			int productID = (Integer) sourceEntity.getProperty("ID").getValue();
			if (productID == 1 || productID == 2) {
				navigationTargetEntityCollection.getEntities().add(categoryList.get(0));
			} else if (productID == 3 || productID == 4) {
				navigationTargetEntityCollection.getEntities().add(categoryList.get(1));
			} else if (productID == 5 || productID == 6) {
				navigationTargetEntityCollection.getEntities().add(categoryList.get(2));
			}
		} else if (sourceEntityFqn.equals(DemoEdmProvider.ET_CATEGORY_FQN.getFullQualifiedNameAsString())
				&& relatedEntityFqn.equals(DemoEdmProvider.ET_PRODUCT_FQN)) {
			// relation Category->Products (result all products)
			int categoryID = (Integer) sourceEntity.getProperty("ID").getValue();
			if (categoryID == 1) {
				// the first 2 products are notebooks
				navigationTargetEntityCollection.getEntities().addAll(productList.subList(0, 2));
			} else if (categoryID == 2) {
				// the next 2 products are organizers
				navigationTargetEntityCollection.getEntities().addAll(productList.subList(2, 4));
			} else if (categoryID == 3) {
				// the first 2 products are monitors
				navigationTargetEntityCollection.getEntities().addAll(productList.subList(4, 6));
			}
		}

		if (navigationTargetEntityCollection.getEntities().isEmpty()) {
			return null;
		}

		return navigationTargetEntityCollection;
	}

	/* INTERNAL */
	private EntityCollection getDataProvider() {
		EntityCollection retEntitySet = new EntityCollection();

		for (Entity productEntity : this.dpconfigList) {
			retEntitySet.getEntities().add(productEntity);
		}

		return retEntitySet;
	}

	private Entity getDataProvider(EdmEntityType edmEntityType, List<UriParameter> keyParams) {

		// the list of entities at runtime
		EntityCollection entityCollection = getDataProvider();

		/* generic approach to find the requested entity */
		return Util.findEntity(edmEntityType, entityCollection, keyParams);
	}

	private EntityCollection getZempPlan() {
		EntityCollection retEntitySet = new EntityCollection();

		for (Entity productEntity : this.zempList) {
			retEntitySet.getEntities().add(productEntity);
		}

		return retEntitySet;
	}

	private Entity getZempPlan(EdmEntityType edmEntityType, List<UriParameter> keyParams) {

		// the list of entities at runtime
		EntityCollection entityCollection = getZempPlan();

		/* generic approach to find the requested entity */
		return Util.findEntity(edmEntityType, entityCollection, keyParams);
	}

	private EntityCollection getFloorPlan() {
		EntityCollection retEntitySet = new EntityCollection();

		for (Entity productEntity : this.floorplanList) {
			retEntitySet.getEntities().add(productEntity);
		}

		return retEntitySet;
	}

	private Entity getFloorPlan(EdmEntityType edmEntityType, List<UriParameter> keyParams) {

		// the list of entities at runtime
		EntityCollection entityCollection = getFloorPlan();

		/* generic approach to find the requested entity */
		return Util.findEntity(edmEntityType, entityCollection, keyParams);
	}

	private EntityCollection getProducts() {
		EntityCollection retEntitySet = new EntityCollection();

		for (Entity productEntity : this.productList) {
			retEntitySet.getEntities().add(productEntity);
		}

		return retEntitySet;
	}

	private Entity getProduct(EdmEntityType edmEntityType, List<UriParameter> keyParams) {

		// the list of entities at runtime
		EntityCollection entityCollection = getProducts();

		/* generic approach to find the requested entity */
		return Util.findEntity(edmEntityType, entityCollection, keyParams);
	}

	private EntityCollection getCategories() {
		EntityCollection entitySet = new EntityCollection();

		for (Entity categoryEntity : this.categoryList) {
			entitySet.getEntities().add(categoryEntity);
		}

		return entitySet;
	}

	private Entity getCategory(EdmEntityType edmEntityType, List<UriParameter> keyParams) {

		// the list of entities at runtime
		EntityCollection entitySet = getCategories();

		/* generic approach to find the requested entity */
		return Util.findEntity(edmEntityType, entitySet, keyParams);
	}

	private Entity getAppConfg(EdmEntityType edmEntityType, List<UriParameter> keyParams) {

		// the list of entities at runtime
		EntityCollection entitySet = getAppConfg();

		/* generic approach to find the requested entity */
		return Util.findEntity(edmEntityType, entitySet, keyParams);
	}

	private EntityCollection getAppConfg() {
		EntityCollection entitySet = new EntityCollection();

		for (Entity categoryEntity : this.appconfgList) {
			entitySet.getEntities().add(categoryEntity);
		}

		return entitySet;
	}

	private Entity getDataConfg(EdmEntityType edmEntityType, List<UriParameter> keyParams) {

		// the list of entities at runtime
		EntityCollection entitySet = getDataConfg();

		/* generic approach to find the requested entity */
		return Util.findEntity(edmEntityType, entitySet, keyParams);
	}

	private EntityCollection getDataConfg() {
		EntityCollection entitySet = new EntityCollection();

		for (Entity categoryEntity : this.dataconfgList) {
			entitySet.getEntities().add(categoryEntity);
		}

		return entitySet;
	}

	/* HELPER */
	private static void createDestinationDataFile(String destinationName, Properties connectProperties) {
		// TODO
		File destCfg = new File(destinationName + ".jcoDestination");
		try {
			FileOutputStream fos = new FileOutputStream(destCfg, false);
			connectProperties.store(fos, "for tests only !");
			fos.close();
		} catch (Exception e) {
			throw new RuntimeException("Unable to create the destination files", e);
		}

	}

	private void initDataProvider() {
		// FPNAME STATUS FPCONFIG
		dpconfigList.clear();
		String moduleID = null;
		String dataProvider = null;
		String fieldName = null;
		String apiName = null;
		String status = null;
		String parameterName = null;
		String dpConfig = null;
		String isKey = null;

		Connection connection = null;

		try {
			connection = DBUtillocal.getConnection();
			String query = "select * from dpconfig";
			Statement pstmt = connection.createStatement();
			java.sql.ResultSet rs = pstmt.executeQuery(query);
			try {
				while (rs.next()) {
					moduleID = rs.getString("MODULEID");
					dataProvider = rs.getString("DATAPROVIDER");
					fieldName = rs.getString("FIELDNAME");
					apiName = rs.getString("APINAME");
					parameterName = rs.getString("PARAMETERNAME");
					status = rs.getString("STATUS");
					dpConfig = rs.getString("DPCONFIG");
					isKey = rs.getString("ISKEY");
					final Entity e = new Entity()
							.addProperty(new Property(null, "MODULEID", ValueType.PRIMITIVE, moduleID))
							.addProperty(new Property(null, "DATAPROVIDER", ValueType.PRIMITIVE, dataProvider))
							.addProperty(new Property(null, "FIELDNAME", ValueType.PRIMITIVE, fieldName))
							.addProperty(new Property(null, "APINAME", ValueType.PRIMITIVE, apiName))
							.addProperty(new Property(null, "PARAMETERNAME", ValueType.PRIMITIVE, parameterName))
							.addProperty(new Property(null, "STATUS", ValueType.PRIMITIVE, status))
							.addProperty(new Property(null, "DPCONFIG", ValueType.PRIMITIVE, dpConfig))
							.addProperty(new Property(null, "ISKEY", ValueType.PRIMITIVE, isKey));
					e.setId(createId("dpconfig", e, moduleID));
					dpconfigList.add(e);
					System.out.println(dpconfigList.size());
				}
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (connection != null) {
				try {
					connection.close();
					DBUtillocal.Close();
				} catch (Exception e) {
					System.out.println("exception in closing connection");
					e.printStackTrace();
				}
			}
		}

	}

	private void initZemp() {
		zempList.clear(); // ZENO,ZENAME ZEADR ZEMPSALARY ZEMPDOB
		String ZENO = null;
		String ZENAME = null;
		String ZEADR = null;
		String ZEMPSALA = null;
		String ZEMPDOB = null;
		try {

			JCoDestination destination1 = JCoDestinationManager.getDestination("mySAPSystem");
			try {
				destination1.ping();
				JCoDestination destination = TestJco.callZempTable();
				JCoFunction function = destination.getRepository().getFunction("ZBAPI_EMP_LIST_001");
				if (function == null)
					throw new RuntimeException("Not found in SAP.");
				try {
					function.execute(destination);
				} catch (AbapException e) {
					System.out.println(e.toString());
					return;
				}

				JCoTable resultTable = function.getTableParameterList().getTable("ET_EMP_LIST1");
				for (int i = 0; i < resultTable.getNumRows(); i++, resultTable.nextRow()) {
					ZENO = resultTable.getString("ZENO"); // ZENO ZENAME ZEADR
															// ZEMPSALA ZEMPDOB
					ZENAME = resultTable.getString("ZENAME");
					ZEADR = resultTable.getString("ZEADR");
					ZEMPSALA = resultTable.getString("ZEMPSALARY");
					ZEMPDOB = resultTable.getString("ZEMPDOB");
					final Entity e = new Entity().addProperty(new Property(null, "ZENO", ValueType.PRIMITIVE, ZENO))
							.addProperty(new Property(null, "ZENAME", ValueType.PRIMITIVE, ZENAME))
							.addProperty(new Property(null, "ZEADR", ValueType.PRIMITIVE, ZEADR))
							.addProperty(new Property(null, "ZEMPSALA", ValueType.PRIMITIVE, ZEMPSALA))
							.addProperty(new Property(null, "ZEMPDOB", ValueType.PRIMITIVE, ZEMPDOB));
					e.setId(createId("zemptable", e, ZENO));
					zempList.add(e);
					System.out.println(zempList.size());
				}

			} catch (JCoException e) {
				e.printStackTrace();
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	private void initFloorPlan() {

		Entity entity = new Entity();
		// FPNAME STATUS FPCONFIG
		floorplanList.clear();
		String ID = null;
		String moduleid = null;
		String fid = null;
		String fName = null;
		String status = null;
		String fconfg = null;

		Connection connection = null;

		try {
			connection = DBUtillocal.getConnection();
			String query = "select * from floorplan";
			Statement pstmt = connection.createStatement();
			java.sql.ResultSet rs = pstmt.executeQuery(query);
			try {
				while (rs.next()) {
					ID = rs.getString("APPID");
					moduleid = rs.getString("MODULEID");
					fid = rs.getString("FPID");
					fName = rs.getString("FPNAME");
					status = rs.getString("STATUS");
					fconfg = rs.getString("FPCONFIG");
					final Entity e = new Entity().addProperty(new Property(null, "APPID", ValueType.PRIMITIVE, ID))
							.addProperty(new Property(null, "MODULEID", ValueType.PRIMITIVE, moduleid))
							.addProperty(new Property(null, "FPID", ValueType.PRIMITIVE, fid))
							.addProperty(new Property(null, "FPNAME", ValueType.PRIMITIVE, fName))
							.addProperty(new Property(null, "STATUS", ValueType.PRIMITIVE, status))
							.addProperty(new Property(null, "FPCONFIG", ValueType.PRIMITIVE, fconfg));
					e.setId(createId("floorplan", e, ID));
					floorplanList.add(e);
					System.out.println(floorplanList.size());
				}
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (connection != null) {
				try {
					connection.close();
					DBUtillocal.Close();
				} catch (Exception e) {
					System.out.println("exception in closing connection");
					e.printStackTrace();
				}
			}
		}

	}

	private void initProductSampleData() {

		Entity entity = new Entity();

		productList.clear();
		int ID = 0;
		String Name = null;
		String Description = null;

		Connection connection = null;

		try {
			connection = DBUtillocal.getConnection();
			String query = "select * from product";
			Statement pstmt = connection.createStatement();
			java.sql.ResultSet rs = pstmt.executeQuery(query);
			while (rs.next()) {

				ID = rs.getInt("ID");
				Name = rs.getString("Name");
				Description = rs.getString("Description");
				final Entity e = new Entity().addProperty(new Property(null, "ID", ValueType.PRIMITIVE, ID))
						.addProperty(new Property(null, "Name", ValueType.PRIMITIVE, Name))
						.addProperty(new Property(null, "Description", ValueType.PRIMITIVE, Description));
				e.setId(createId("product", e, ID));
				productList.add(e);
				System.out.println(productList.size());
			}
		} catch (java.sql.SQLException se) {
			se.printStackTrace();
		} catch (Exception exp) {
			System.out.println("inside catch " + exp.getMessage());
		} finally {
			if (connection != null) {
				try {
					connection.close();
					DBUtillocal.Close();
				} catch (Exception e) {
					System.out.println("exception in closing connection");
					e.printStackTrace();
				}
			}
		}

	}

	private void initCategorySampleData() {

		Entity entity = new Entity();

		entity.addProperty(new Property(null, "ID", ValueType.PRIMITIVE, 1));
		entity.addProperty(new Property(null, "Name", ValueType.PRIMITIVE, "Notebooks"));
		entity.setType(DemoEdmProvider.ET_CATEGORY_FQN.getFullQualifiedNameAsString());
		entity.setId(createId(entity, "ID"));
		categoryList.add(entity);

		entity = new Entity();
		entity.addProperty(new Property(null, "ID", ValueType.PRIMITIVE, 2));
		entity.addProperty(new Property(null, "Name", ValueType.PRIMITIVE, "Organizers"));
		entity.setType(DemoEdmProvider.ET_CATEGORY_FQN.getFullQualifiedNameAsString());
		entity.setId(createId(entity, "ID"));
		categoryList.add(entity);

		entity = new Entity();
		entity.addProperty(new Property(null, "ID", ValueType.PRIMITIVE, 3));
		entity.addProperty(new Property(null, "Name", ValueType.PRIMITIVE, "Monitors"));
		entity.setType(DemoEdmProvider.ET_CATEGORY_FQN.getFullQualifiedNameAsString());
		entity.setId(createId(entity, "ID"));
		categoryList.add(entity);
	}

	private void initDataConf() {

		Entity entity = new Entity();
		dataconfgList.clear();
		String moduleID = null;
		String dpID = null;
		String dataCategory = null;
		String zusage = null;
		String dataformat = null;
		String deltaToken = null;
		int pageSize = 0;
		String loadMore = null;

		Connection connection = null;

		try {
			connection = DBUtillocal.getConnection();
			String query = "select * from dataconf";
			Statement pstmt = connection.createStatement();
			java.sql.ResultSet rs = pstmt.executeQuery(query);
			while (rs.next()) {
				moduleID = rs.getString("MODULEID");
				dpID = rs.getString("DPID");
				dataCategory = rs.getString("DATACATEGORY");
				zusage = rs.getString("ZUSAGE");
				dataformat = rs.getString("DATAFORMAT");
				deltaToken = rs.getString("DELTATOKEN");
				pageSize = rs.getInt("PAGESIZE");
				loadMore = rs.getString("LOADMORE");

				final Entity e = new Entity().addProperty(new Property(null, "MODULEID", ValueType.PRIMITIVE, moduleID))
						.addProperty(new Property(null, "DPID", ValueType.PRIMITIVE, dpID))
						.addProperty(new Property(null, "DATACATEGORY", ValueType.PRIMITIVE, dataCategory))
						.addProperty(new Property(null, "ZUSAGE", ValueType.PRIMITIVE, zusage))
						.addProperty(new Property(null, "DATAFORMAT", ValueType.PRIMITIVE, dataformat))
						.addProperty(new Property(null, "DELTATOKEN", ValueType.PRIMITIVE, deltaToken))
						.addProperty(new Property(null, "PAGESIZE", ValueType.PRIMITIVE, pageSize))
						.addProperty(new Property(null, "LOADMORE", ValueType.PRIMITIVE, loadMore));
				e.setId(createId("dataconf", e, moduleID));
				dataconfgList.add(e);
				System.out.println(dataconfgList.size());
			}
		} catch (java.sql.SQLException se) {
			se.printStackTrace();
		} catch (Exception exp) {
			System.out.println("inside catch " + exp.getMessage());
		} finally {
			if (connection != null) {
				try {
					connection.close();
					DBUtillocal.Close();
				} catch (Exception e) {
					System.out.println("exception in closing connection");
					e.printStackTrace();
				}
			}
		}

	}

	private void initAppConfg() {

		Entity entity = new Entity();

		appconfgList.clear();
		String ID = null;
		String Name = null;
		String Description = null;

		Connection connection = null;

		try {
			connection = DBUtillocal.getConnection();
			String query = "select * from appconfg";
			Statement pstmt = connection.createStatement();
			java.sql.ResultSet rs = pstmt.executeQuery(query);
			while (rs.next()) {

				ID = rs.getString("APPID");
				Name = rs.getString("APPName");
				Description = rs.getString("APPCONFIG");
				final Entity e = new Entity().addProperty(new Property(null, "APPID", ValueType.PRIMITIVE, ID))
						.addProperty(new Property(null, "APPNAME", ValueType.PRIMITIVE, Name))
						.addProperty(new Property(null, "APPCONFIG", ValueType.PRIMITIVE, Description));
				e.setId(createId("appconfg", e, ID));
				appconfgList.add(e);
				System.out.println(appconfgList.size());
			}
		} catch (java.sql.SQLException se) {
			se.printStackTrace();
		} catch (Exception exp) {
			System.out.println("inside catch " + exp.getMessage());
		} finally {
			if (connection != null) {
				try {
					connection.close();
					DBUtillocal.Close();
				} catch (Exception e) {
					System.out.println("exception in closing connection");
					e.printStackTrace();
				}
			}
		}

	}

	private URI createId(Entity entity, String idPropertyName) {
		return createId(entity, idPropertyName, null);
	}

	private URI createId(Entity entity, String idPropertyName, String navigationName) {
		try {
			StringBuilder sb = new StringBuilder(getEntitySetName(entity)).append("(");
			final Property property = entity.getProperty(idPropertyName);
			sb.append(property.asPrimitive()).append(")");
			if (navigationName != null) {
				sb.append("/").append(navigationName);
			}
			return new URI(sb.toString());
		} catch (URISyntaxException e) {
			throw new ODataRuntimeException("Unable to create (Atom) id for entity: " + entity, e);
		}
	}

	private String getEntitySetName(Entity entity) {
		if (DemoEdmProvider.ET_CATEGORY_FQN.getFullQualifiedNameAsString().equals(entity.getType())) {
			return DemoEdmProvider.ES_CATEGORIES_NAME;
		} else if (DemoEdmProvider.ET_PRODUCT_FQN.getFullQualifiedNameAsString().equals(entity.getType())) {
			return DemoEdmProvider.ES_PRODUCTS_NAME;
		} else if (DemoEdmProvider.ET_APPCONFG_FQN.getFullQualifiedNameAsString().equals(entity.getType())) {
			return DemoEdmProvider.ES_APPCONFG_NAME;
		} else if (DemoEdmProvider.ET_DATACONF_FQN.getFullQualifiedNameAsString().equals(entity.getType())) {
			return DemoEdmProvider.ES_DATACONF_NAME;
		} else if (DemoEdmProvider.ET_FLOORPLAN_FQN.getFullQualifiedNameAsString().equals(entity.getType())) {
			return DemoEdmProvider.ES_FLOORPLAN_NAME;
		} else if (DemoEdmProvider.ET_DP_FQN.getFullQualifiedNameAsString().equals(entity.getType())) {
			return DemoEdmProvider.ES_DP_NAME;
		}
		return entity.getType();
	}

	private URI createId(String entitySetName, Entity entity, Object id) {
		try {
			return new URI(entitySetName + "(" + java.net.URLEncoder.encode(String.valueOf(id)) + ")");

		} catch (URISyntaxException e) {
			throw new ODataRuntimeException("Unable to create id for entity: " + entitySetName, e);
		} catch (Exception exp) {
			exp.printStackTrace();
		}
		return null;
	}

	public Entity createEntityData(EdmEntitySet edmEntitySet, Entity entityToCreate) {

		EdmEntityType edmEntityType = edmEntitySet.getEntityType();

		// actually, this is only required if we have more than one Entity Type
		if (edmEntityType.getName().equals(DemoEdmProvider.ET_PRODUCT_NAME)) {
			return createProduct(edmEntityType, entityToCreate);
		}
		if (edmEntityType.getName().equals(DemoEdmProvider.ET_APPCONFG_NAME)) {
			return createAppCnfg(edmEntityType, entityToCreate);
		}
		if (edmEntityType.getName().equals(DemoEdmProvider.ET_DATACONF_NAME)) {
			return createDataCnfg(edmEntityType, entityToCreate);
		}
		if (edmEntityType.getName().equals(DemoEdmProvider.ET_FLOORPLAN_NAME)) {
			return createFloorplan(edmEntityType, entityToCreate);
		}
		if (edmEntityType.getName().equals(DemoEdmProvider.ET_DP_NAME)) {
			return createDataProvider(edmEntityType, entityToCreate);
		}

		return null;
	}

	private Entity createDataProvider(EdmEntityType edmEntityType, Entity entity) {

		Connection con = null;
		try {

			con = DBUtillocal.getConnection();
			String query = "insert into dpconfig(MODULEID,DATAPROVIDER,FIELDNAME,APINAME,PARAMETERNAME,STATUS,DPCONFIG,ISKEY) values(?,?,?,?,?,?,?,?)";
			PreparedStatement pStmt = con.prepareStatement(query);
			pStmt.setString(1, entity.getProperty("MODULEID").getValue().toString());
			pStmt.setString(2, entity.getProperty("DATAPROVIDER").getValue().toString());
			pStmt.setString(3, entity.getProperty("FIELDNAME").getValue().toString());
			pStmt.setString(4, entity.getProperty("APINAME").getValue().toString());
			pStmt.setString(5, entity.getProperty("PARAMETERNAME").getValue().toString());
			pStmt.setString(6, entity.getProperty("STATUS").getValue().toString());
			pStmt.setString(7, entity.getProperty("DPCONFIG").getValue().toString());
			pStmt.setString(8, entity.getProperty("ISKEY").getValue().toString());
			int n = pStmt.executeUpdate();
			if (n > 0) {
				this.dpconfigList.add(entity);
				System.out.println("Data Inserted Successfully ID:");
			}

		} catch (Exception exp) {
			System.out.println(exp.getMessage());
			exp.printStackTrace();
			throw new ODataRuntimeException("Duplicate entry for key 'PRIMARY'" + entity);
		} finally {
			if (con != null) {
				try {
					con.close();
				} catch (SQLException se) {
					System.out.println(se.getMessage());
					se.printStackTrace();
				}
			}
		}
		return entity;

	}

	/*
	 * private Entity createProduct(EdmEntityType edmEntityType, Entity entity)
	 * {
	 * 
	 * // the ID of the newly created product entity is generated automatically
	 * int newId = 1; while (productIdExists(newId)) { newId++; }
	 * 
	 * Property idProperty = entity.getProperty("ID"); if (idProperty != null) {
	 * idProperty.setValue(ValueType.PRIMITIVE, Integer.valueOf(newId)); } else
	 * { // as of OData v4 spec, the key property can be omitted from the //
	 * POST request body entity.getProperties().add(new Property(null, "ID",
	 * ValueType.PRIMITIVE, newId)); } entity.setId(createId("Products",
	 * newId)); this.productList.add(entity);
	 * 
	 * return entity;
	 * 
	 * }
	 */
	private Entity createFloorplan(EdmEntityType edmEntityType, Entity entity) {

		Connection con = null;
		try {
			con = DBUtillocal.getConnection();
			String query = "insert into floorplan(APPID,MODULEID,FPID,FPNAME,STATUS,FPCONFIG) values(?,?,?,?,?,?)";
			PreparedStatement pStmt = con.prepareStatement(query);
			pStmt.setString(1, entity.getProperty("APPID").getValue().toString());
			pStmt.setString(2, entity.getProperty("MODULEID").getValue().toString());
			pStmt.setString(3, entity.getProperty("FPID").getValue().toString());
			pStmt.setString(4, entity.getProperty("FPNAME").getValue().toString());
			pStmt.setString(5, entity.getProperty("STATUS").getValue().toString());
			pStmt.setString(6, entity.getProperty("FPCONFIG").getValue().toString());
			int n = pStmt.executeUpdate();

			if (n > 0) {
				this.floorplanList.add(entity);
				System.out.println("Data Inserted Successfully ID:");
			}

		} catch (Exception exp) {
			System.out.println(exp.getMessage());
			exp.printStackTrace();
			throw new ODataRuntimeException("Duplicate entry for key 'PRIMARY'" + entity);
		} finally {
			if (con != null) {
				try {
					con.close();
				} catch (SQLException se) {
					System.out.println(se.getMessage());
					se.printStackTrace();
				}
			}
		}
		return entity;

	}

	private Entity createDataCnfg(EdmEntityType edmEntityType, Entity entity) {

		// the ID of the newly created product entity is generated automatically

		/*
		 * int newId = 1; while (appCnfgIdExists(newId)) { newId++; }
		 */

		// MODULEID DPID DATACATEGORY ZUSAGE DATAFORMAT DELTATOKEN PAGESIZE
		// LOADMORE

		Connection con = null;
		try {
			con = DBUtillocal.getConnection();
			String query = "insert into dataconf(MODULEID,DPID,DATACATEGORY,ZUSAGE,DATAFORMAT,DELTATOKEN,PAGESIZE,LOADMORE) values(?,?,?,?,?,?,?,?)";
			PreparedStatement pStmt = con.prepareStatement(query);
			pStmt.setString(1, entity.getProperty("MODULEID").getValue().toString());
			pStmt.setString(2, entity.getProperty("DPID").getValue().toString());
			pStmt.setString(3, entity.getProperty("DATACATEGORY").getValue().toString());
			pStmt.setString(4, entity.getProperty("ZUSAGE").getValue().toString());
			pStmt.setString(5, entity.getProperty("DATAFORMAT").getValue().toString());
			pStmt.setString(6, entity.getProperty("DELTATOKEN").getValue().toString());
			pStmt.setString(7, entity.getProperty("PAGESIZE").getValue().toString());
			pStmt.setString(8, entity.getProperty("LOADMORE").getValue().toString());
			int n = pStmt.executeUpdate();

			if (n > 0) {
				/*
				 * int newid = getNewSno("appconfg"); Property idProperty =
				 * entity.getProperty("APPID)"); if (idProperty != null) {
				 * idProperty.setValue(ValueType.PRIMITIVE, new Integer(newid));
				 * } else { // as of OData v4 spec, the key property can be
				 * omitted // from // the // POST request body
				 * entity.getProperties().add(new Property(null, "APPID",
				 * ValueType.PRIMITIVE, newid)); }
				 */
				this.dataconfgList.add(entity);
				System.out.println("Data Inserted Successfully ID:");
			}

		} catch (Exception exp) {
			System.out.println(exp.getMessage());
			exp.printStackTrace();
			throw new ODataRuntimeException("Duplicate entry for key 'PRIMARY'" + entity);
		} finally {
			if (con != null) {
				try {
					con.close();
				} catch (SQLException se) {
					System.out.println(se.getMessage());
					se.printStackTrace();
				}
			}
		}
		return entity;

	}

	private Entity createAppCnfg(EdmEntityType edmEntityType, Entity entity) {

		// the ID of the newly created product entity is generated automatically

		/*
		 * int newId = 1; while (appCnfgIdExists(newId)) { newId++; }
		 */

		Connection con = null;
		try {
			con = DBUtillocal.getConnection();
			String query = "insert into appconfg(APPID,APPNAME,APPCONFIG) values(?,?,?)";
			PreparedStatement pStmt = con.prepareStatement(query);
			pStmt.setString(1, entity.getProperty("APPID").getValue().toString());
			pStmt.setString(2, entity.getProperty("APPNAME").getValue().toString());
			pStmt.setString(3, entity.getProperty("APPCONFIG").getValue().toString());
			int n = pStmt.executeUpdate();

			if (n > 0) {
				/*
				 * int newid = getNewSno("appconfg"); Property idProperty =
				 * entity.getProperty("APPID)"); if (idProperty != null) {
				 * idProperty.setValue(ValueType.PRIMITIVE, new Integer(newid));
				 * } else { // as of OData v4 spec, the key property can be
				 * omitted // from // the // POST request body
				 * entity.getProperties().add(new Property(null, "APPID",
				 * ValueType.PRIMITIVE, newid)); }
				 */
				this.appconfgList.add(entity);
				System.out.println("Data Inserted Successfully ID:");
			}

		} catch (Exception exp) {
			System.out.println(exp.getMessage());
			exp.printStackTrace();
			throw new ODataRuntimeException("Duplicate entry for key 'PRIMARY'" + entity);
		} finally {
			if (con != null) {
				try {
					con.close();
				} catch (SQLException se) {
					System.out.println(se.getMessage());
					se.printStackTrace();
				}
			}
		}
		return entity;

	}

	private Entity createProduct(EdmEntityType edmEntityType, Entity entity) {

		// the ID of the newly created product entity is generated automatically

		int newId = 1;
		while (productIdExists(newId)) {
			newId++;
		}

		Connection con = null;
		try {
			con = DBUtillocal.getConnection();
			String query = "insert into product(Name,Description) values(?,?)";
			PreparedStatement pStmt = con.prepareStatement(query);
			pStmt.setString(1, entity.getProperty("ID").getValue().toString());
			pStmt.setString(1, entity.getProperty("Name").getValue().toString());
			pStmt.setString(2, entity.getProperty("Description").getValue().toString());
			int n = pStmt.executeUpdate();

			if (n > 0) {
				int newid = getNewSno("product");
				Property idProperty = entity.getProperty("ID)");
				if (idProperty != null) {
					idProperty.setValue(ValueType.PRIMITIVE, new Integer(newid));
				} else { // as of OData v4 spec, the key property can be omitted
							// from // the // POST request body
					entity.getProperties().add(new Property(null, "ID", ValueType.PRIMITIVE, newid));
				}
				this.productList.add(entity);
				System.out.println("Data Inserted Successfully ID:" + newid);
			}

		} catch (Exception exp) {
			System.out.println(exp.getMessage());
			exp.printStackTrace();
			throw new ODataRuntimeException("Duplicate entry for key 'PRIMARY'" + entity);
		} finally {
			if (con != null) {
				try {
					con.close();
				} catch (SQLException se) {
					System.out.println(se.getMessage());
					se.printStackTrace();
				}
			}
		}
		return entity;

	}

	private URI createId(String entitySetName, Object id) {
		try {
			return new URI(entitySetName + "(" + String.valueOf(id) + ")");
		} catch (URISyntaxException e) {
			throw new ODataRuntimeException("Unable to create id for entity: " + entitySetName, e);
		}
	}

	private boolean productIdExists(int id) {

		for (Entity entity : this.productList) {
			Integer existingID = (Integer) entity.getProperty("ID").getValue();
			if (existingID.intValue() == id) {
				return true;
			}
		}

		return false;
	}

	private boolean appCnfgIdExists(int id) {

		for (Entity entity : this.appconfgList) {
			Integer existingID = (Integer) entity.getProperty("APPID").getValue();
			if (existingID.intValue() == id) {
				return true;
			}
		}

		return false;
	}

	public int getNewSno(String tableName) {
		Connection connection = null;
		try {
			connection = DBUtillocal.getConnection();
			String query = "select ID from " + tableName + " order by ID desc LIMIT 1";
			Statement stmt = connection.createStatement();
			ResultSet rs = stmt.executeQuery(query);
			if (rs.next()) {
				return rs.getInt("ID");
			}
		} catch (SQLException sq) {
			sq.printStackTrace();
			throw new RuntimeException(sq.getMessage());
		} finally {
			try {
				connection.close();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				throw new RuntimeException(e.getMessage());
			}
			DBUtillocal.Close();
		}
		return 1;
	}

	public void updateEntityData(EdmEntitySet edmEntitySet, List<UriParameter> keyParams, Entity updateEntity,
			HttpMethod httpMethod) throws ODataApplicationException {

		EdmEntityType edmEntityType = edmEntitySet.getEntityType();

		// actually, this is only required if we have more than one Entity Type
		if (edmEntityType.getName().equals(DemoEdmProvider.ET_PRODUCT_NAME)) {
			updateProduct(edmEntityType, keyParams, updateEntity, httpMethod);
		}
		if (edmEntityType.getName().equals(DemoEdmProvider.ET_APPCONFG_NAME)) {
			updateAppConfg(edmEntityType, keyParams, updateEntity, httpMethod);
		}
		if (edmEntityType.getName().equals(DemoEdmProvider.ET_DATACONF_NAME)) {
			updateDataConfg(edmEntityType, keyParams, updateEntity, httpMethod);
		}
		if (edmEntityType.getName().equals(DemoEdmProvider.ET_FLOORPLAN_NAME)) {
			updateFloorplan(edmEntityType, keyParams, updateEntity, httpMethod);
		}
		if (edmEntityType.getName().equals(DemoEdmProvider.ET_DP_NAME)) {
			updateDataProvider(edmEntityType, keyParams, updateEntity, httpMethod);
		}
	}

	private void updateDataProvider(EdmEntityType edmEntityType, List<UriParameter> keyParams, Entity entity,
			HttpMethod httpMethod) throws ODataApplicationException {
		Entity extensionEntity = getDataProvider(edmEntityType, keyParams);
		if (extensionEntity == null) {
			throw new ODataApplicationException("Entity not found", HttpStatusCode.NOT_FOUND.getStatusCode(),
					Locale.ENGLISH);
		}
		// loop over all properties and replace the values with the values of
		// the given payload
		// Note: ignoring ComplexType, as we don't have it in our odata model
		StringBuilder updateQuery = new StringBuilder("update dpconfig set ");
		@SuppressWarnings("unused")
		String keyProperty = null;
		List<Property> existingProperties = extensionEntity.getProperties();
		String keyPropValue = null;
		for (Property existingProp : existingProperties) {
			String propName = existingProp.getName();
			// ignore the key properties, they aren't updateable
			if (isKey(edmEntityType, propName)) {
				keyProperty = propName;
				keyPropValue = existingProp.getValue().toString();
				continue;
			}
			String existingPropValue = null;
			try {
				existingPropValue = entity.getProperty(propName).getValue() + "";
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			if ("null".equals(existingPropValue) || null == existingPropValue) {
				updateQuery.append(propName + " = ");
				updateQuery.append(existingPropValue);
				updateQuery.append(",");
			} else {
				updateQuery.append(propName + " = \"");
				updateQuery.append(existingPropValue);
				updateQuery.append("\",");
			}
		}
		updateQuery.deleteCharAt(updateQuery.lastIndexOf(",")).append(" where MODULEID = " + keyPropValue);
		Connection connection = DBUtillocal.getConnection();
		try {
			// System.out.println("udate query"+updateQuery);
			int n = connection.createStatement().executeUpdate(updateQuery.toString());
			System.out.println("n=" + n);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			for (Property existingProp : existingProperties) {
				// get the connection
				String propName = existingProp.getName();
				// ignore the key properties, they aren't updateable
				if (isKey(edmEntityType, propName)) {
					continue;
				}
				Property updateProperty = entity.getProperty(propName);
				// the request payload might not consider ALL properties, so it
				// can be null
				if (updateProperty == null) {
					// if a property has NOT been added to the request payload
					// depending on the HttpMethod, our behavior is different
					if (httpMethod.equals(HttpMethod.PATCH)) {
						// as of the OData spec, in case of PATCH, the existing
						// property is not touched
						continue; // do nothing
					} else if (httpMethod.equals(HttpMethod.PUT)) {
						if (keyPropValue != null && !keyPropValue.isEmpty()) {
							existingProp.setValue(existingProp.getValueType(), null);
						}
						continue;
					}
				}
				if (keyPropValue != null && !keyPropValue.isEmpty()) {
					existingProp.setValue(existingProp.getValueType(), updateProperty.getValue());
				}
			}
		} catch (Exception exp) {
			System.out.println(exp.getMessage());
			exp.printStackTrace();
		}
	}

	private void updateFloorplan(EdmEntityType edmEntityType, List<UriParameter> keyParams, Entity entity,
			HttpMethod httpMethod) throws ODataApplicationException {
		Entity extensionEntity = getFloorPlan(edmEntityType, keyParams);
		if (extensionEntity == null) {
			throw new ODataApplicationException("Entity not found", HttpStatusCode.NOT_FOUND.getStatusCode(),
					Locale.ENGLISH);
		}
		// loop over all properties and replace the values with the values of
		// the given payload
		// Note: ignoring ComplexType, as we don't have it in our odata model
		StringBuilder updateQuery = new StringBuilder("update floorplan set ");
		@SuppressWarnings("unused")
		String keyProperty = null;
		List<Property> existingProperties = extensionEntity.getProperties();
		String keyPropValue = null;
		for (Property existingProp : existingProperties) {
			String propName = existingProp.getName();
			// ignore the key properties, they aren't updateable
			if (isKey(edmEntityType, propName)) {
				keyProperty = propName;
				keyPropValue = existingProp.getValue().toString();
				continue;
			}
			String existingPropValue = null;
			try {
				existingPropValue = entity.getProperty(propName).getValue() + "";
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			if ("null".equals(existingPropValue) || null == existingPropValue) {
				updateQuery.append(propName + " = ");
				updateQuery.append(existingPropValue);
				updateQuery.append(",");
			} else {
				updateQuery.append(propName + " = \"");
				updateQuery.append(existingPropValue);
				updateQuery.append("\",");
			}
		}
		updateQuery.deleteCharAt(updateQuery.lastIndexOf(",")).append(" where APPID = " + keyPropValue);
		Connection connection = DBUtillocal.getConnection();
		try {
			// System.out.println("udate query"+updateQuery);
			int n = connection.createStatement().executeUpdate(updateQuery.toString());
			System.out.println("n=" + n);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			for (Property existingProp : existingProperties) {
				// get the connection
				String propName = existingProp.getName();
				// ignore the key properties, they aren't updateable
				if (isKey(edmEntityType, propName)) {
					continue;
				}
				Property updateProperty = entity.getProperty(propName);
				// the request payload might not consider ALL properties, so it
				// can be null
				if (updateProperty == null) {
					// if a property has NOT been added to the request payload
					// depending on the HttpMethod, our behavior is different
					if (httpMethod.equals(HttpMethod.PATCH)) {
						// as of the OData spec, in case of PATCH, the existing
						// property is not touched
						continue; // do nothing
					} else if (httpMethod.equals(HttpMethod.PUT)) {
						if (keyPropValue != null && !keyPropValue.isEmpty()) {
							existingProp.setValue(existingProp.getValueType(), null);
						}
						continue;
					}
				}
				if (keyPropValue != null && !keyPropValue.isEmpty()) {
					existingProp.setValue(existingProp.getValueType(), updateProperty.getValue());
				}
			}
		} catch (Exception exp) {
			System.out.println(exp.getMessage());
			exp.printStackTrace();
		}
	}

	private void updateDataConfg(EdmEntityType edmEntityType, List<UriParameter> keyParams, Entity entity,
			HttpMethod httpMethod) throws ODataApplicationException {

		Entity productEntity = getDataConfg(edmEntityType, keyParams);
		if (productEntity == null) {
			throw new ODataApplicationException("Entity not found", HttpStatusCode.NOT_FOUND.getStatusCode(),
					Locale.ENGLISH);
		}

		// loop over all properties and replace the values with the values of
		// the given payload
		// Note: ignoring ComplexType, as we don't have it in our odata model
		List<Property> existingProperties = productEntity.getProperties();
		for (Property existingProp : existingProperties) {
			String propName = existingProp.getName();

			// ignore the key properties, they aren't updateable
			if (isKey(edmEntityType, propName)) {
				continue;
			}

			Property updateProperty = entity.getProperty(propName);
			// the request payload might not consider ALL properties, so it can
			// be null
			if (updateProperty == null) {
				// if a property has NOT been added to the request payload
				// depending on the HttpMethod, our behavior is different
				if (httpMethod.equals(HttpMethod.PATCH)) {
					// as of the OData spec, in case of PATCH, the existing
					// property is not touched
					continue; // do nothing
				} else if (httpMethod.equals(HttpMethod.PUT)) {
					// as of the OData spec, in case of PUT, the existing
					// property is set to null (or to default value)
					existingProp.setValue(existingProp.getValueType(), null);
					continue;
				}
			}

			// change the value of the properties
			existingProp.setValue(existingProp.getValueType(), updateProperty.getValue());
		}
	}

	private void updateAppConfg(EdmEntityType edmEntityType, List<UriParameter> keyParams, Entity entity,
			HttpMethod httpMethod) throws ODataApplicationException {
		Entity extensionEntity = getAppConfg(edmEntityType, keyParams);
		if (extensionEntity == null) {
			throw new ODataApplicationException("Entity not found", HttpStatusCode.NOT_FOUND.getStatusCode(),
					Locale.ENGLISH);
		}
		// loop over all properties and replace the values with the values of
		// the given payload
		// Note: ignoring ComplexType, as we don't have it in our odata model
		StringBuilder updateQuery = new StringBuilder("update appconfg set ");
		@SuppressWarnings("unused")
		String keyProperty = null;
		List<Property> existingProperties = extensionEntity.getProperties();
		String keyPropValue = null;
		for (Property existingProp : existingProperties) {
			String propName = existingProp.getName();
			// ignore the key properties, they aren't updateable
			if (isKey(edmEntityType, propName)) {
				keyProperty = propName;
				keyPropValue = existingProp.getValue().toString();
				continue;
			}
			String existingPropValue = null;
			try {
				existingPropValue = entity.getProperty(propName).getValue() + "";
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			if ("null".equals(existingPropValue) || null == existingPropValue) {
				updateQuery.append(propName + " = ");
				updateQuery.append(existingPropValue);
				updateQuery.append(",");
			} else {
				updateQuery.append(propName + " = \"");
				updateQuery.append(existingPropValue);
				updateQuery.append("\",");
			}
		}
		updateQuery.deleteCharAt(updateQuery.lastIndexOf(",")).append(" where APPID = " + keyPropValue);
		Connection connection = DBUtillocal.getConnection();
		try {
			// System.out.println("udate query"+updateQuery);
			int n = connection.createStatement().executeUpdate(updateQuery.toString());
			System.out.println("n=" + n);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			for (Property existingProp : existingProperties) {
				// get the connection
				String propName = existingProp.getName();
				// ignore the key properties, they aren't updateable
				if (isKey(edmEntityType, propName)) {
					continue;
				}
				Property updateProperty = entity.getProperty(propName);
				// the request payload might not consider ALL properties, so it
				// can be null
				if (updateProperty == null) {
					// if a property has NOT been added to the request payload
					// depending on the HttpMethod, our behavior is different
					if (httpMethod.equals(HttpMethod.PATCH)) {
						// as of the OData spec, in case of PATCH, the existing
						// property is not touched
						continue; // do nothing
					} else if (httpMethod.equals(HttpMethod.PUT)) {
						if (keyPropValue != null && !keyPropValue.isEmpty()) {
							existingProp.setValue(existingProp.getValueType(), null);
						}
						continue;
					}
				}
				if (keyPropValue != null && !keyPropValue.isEmpty()) {
					existingProp.setValue(existingProp.getValueType(), updateProperty.getValue());
				}
			}
		} catch (Exception exp) {
			System.out.println(exp.getMessage());
			exp.printStackTrace();
		}
	}

	/*
	 * private void updateAppConfg(EdmEntityType edmEntityType,
	 * List<UriParameter> keyParams, Entity entity, HttpMethod httpMethod)
	 * throws ODataApplicationException {
	 * 
	 * Entity productEntity = getAppConfg(edmEntityType, keyParams); if
	 * (productEntity == null) { throw new
	 * ODataApplicationException("Entity not found",
	 * HttpStatusCode.NOT_FOUND.getStatusCode(), Locale.ENGLISH); }
	 * 
	 * // loop over all properties and replace the values with the values of //
	 * the given payload // Note: ignoring ComplexType, as we don't have it in
	 * our odata model List<Property> existingProperties =
	 * productEntity.getProperties(); for (Property existingProp :
	 * existingProperties) { String propName = existingProp.getName();
	 * 
	 * // ignore the key properties, they aren't updateable if
	 * (isKey(edmEntityType, propName)) { continue; }
	 * 
	 * Property updateProperty = entity.getProperty(propName); // the request
	 * payload might not consider ALL properties, so it can // be null if
	 * (updateProperty == null) { // if a property has NOT been added to the
	 * request payload // depending on the HttpMethod, our behavior is different
	 * if (httpMethod.equals(HttpMethod.PATCH)) { // as of the OData spec, in
	 * case of PATCH, the existing // property is not touched continue; // do
	 * nothing } else if (httpMethod.equals(HttpMethod.PUT)) { // as of the
	 * OData spec, in case of PUT, the existing // property is set to null (or
	 * to default value) existingProp.setValue(existingProp.getValueType(),
	 * null); continue; } }
	 * 
	 * // change the value of the properties
	 * existingProp.setValue(existingProp.getValueType(),
	 * updateProperty.getValue()); } }
	 */

	private void updateProduct(EdmEntityType edmEntityType, List<UriParameter> keyParams, Entity entity,
			HttpMethod httpMethod) throws ODataApplicationException {
		Entity extensionEntity = getProduct(edmEntityType, keyParams);
		if (extensionEntity == null) {
			throw new ODataApplicationException("Entity not found", HttpStatusCode.NOT_FOUND.getStatusCode(),
					Locale.ENGLISH);
		}
		// loop over all properties and replace the values with the values of
		// the given payload
		// Note: ignoring ComplexType, as we don't have it in our odata model
		StringBuilder updateQuery = new StringBuilder("update product set ");
		@SuppressWarnings("unused")
		String keyProperty = null;
		List<Property> existingProperties = extensionEntity.getProperties();
		String keyPropValue = null;
		for (Property existingProp : existingProperties) {
			String propName = existingProp.getName();
			// ignore the key properties, they aren't updateable
			if (isKey(edmEntityType, propName)) {
				keyProperty = propName;
				keyPropValue = existingProp.getValue().toString();
				continue;
			}
			String existingPropValue = null;
			try {
				existingPropValue = entity.getProperty(propName).getValue() + "";
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			if ("null".equals(existingPropValue) || null == existingPropValue) {
				updateQuery.append(propName + " = ");
				updateQuery.append(existingPropValue);
				updateQuery.append(",");
			} else {
				updateQuery.append(propName + " = \"");
				updateQuery.append(existingPropValue);
				updateQuery.append("\",");
			}
		}
		updateQuery.deleteCharAt(updateQuery.lastIndexOf(",")).append(" where ID = " + keyPropValue);
		Connection connection = DBUtillocal.getConnection();
		try {
			// System.out.println("udate query"+updateQuery);
			int n = connection.createStatement().executeUpdate(updateQuery.toString());
			System.out.println("n=" + n);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			for (Property existingProp : existingProperties) {
				// get the connection
				String propName = existingProp.getName();
				// ignore the key properties, they aren't updateable
				if (isKey(edmEntityType, propName)) {
					continue;
				}
				Property updateProperty = entity.getProperty(propName);
				// the request payload might not consider ALL properties, so it
				// can be null
				if (updateProperty == null) {
					// if a property has NOT been added to the request payload
					// depending on the HttpMethod, our behavior is different
					if (httpMethod.equals(HttpMethod.PATCH)) {
						// as of the OData spec, in case of PATCH, the existing
						// property is not touched
						continue; // do nothing
					} else if (httpMethod.equals(HttpMethod.PUT)) {
						if (keyPropValue != null && !keyPropValue.isEmpty()) {
							existingProp.setValue(existingProp.getValueType(), null);
						}
						continue;
					}
				}
				if (keyPropValue != null && !keyPropValue.isEmpty()) {
					existingProp.setValue(existingProp.getValueType(), updateProperty.getValue());
				}
			}
		} catch (Exception exp) {
			System.out.println(exp.getMessage());
			exp.printStackTrace();
		}
	}

	/*
	 * private void updateProduct(EdmEntityType edmEntityType,
	 * List<UriParameter> keyParams, Entity entity, HttpMethod httpMethod)
	 * throws ODataApplicationException {
	 * 
	 * Entity productEntity = getProduct(edmEntityType, keyParams); if
	 * (productEntity == null) { throw new
	 * ODataApplicationException("Entity not found",
	 * HttpStatusCode.NOT_FOUND.getStatusCode(), Locale.ENGLISH); }
	 * 
	 * // loop over all properties and replace the values with the values of //
	 * the given payload // Note: ignoring ComplexType, as we don't have it in
	 * our odata model List<Property> existingProperties =
	 * productEntity.getProperties(); for (Property existingProp :
	 * existingProperties) { String propName = existingProp.getName();
	 * 
	 * // ignore the key properties, they aren't updateable if
	 * (isKey(edmEntityType, propName)) { continue; }
	 * 
	 * Property updateProperty = entity.getProperty(propName); // the request
	 * payload might not consider ALL properties, so it can // be null if
	 * (updateProperty == null) { // if a property has NOT been added to the
	 * request payload // depending on the HttpMethod, our behavior is different
	 * if (httpMethod.equals(HttpMethod.PATCH)) { // as of the OData spec, in
	 * case of PATCH, the existing // property is not touched continue; // do
	 * nothing } else if (httpMethod.equals(HttpMethod.PUT)) { // as of the
	 * OData spec, in case of PUT, the existing // property is set to null (or
	 * to default value) existingProp.setValue(existingProp.getValueType(),
	 * null); continue; } }
	 * 
	 * // change the value of the properties
	 * existingProp.setValue(existingProp.getValueType(),
	 * updateProperty.getValue()); } }
	 */
	/* HELPER */

	private boolean isKey(EdmEntityType edmEntityType, String propertyName) {
		List<EdmKeyPropertyRef> keyPropertyRefs = edmEntityType.getKeyPropertyRefs();
		for (EdmKeyPropertyRef propRef : keyPropertyRefs) {
			String keyPropertyName = propRef.getName();
			if (keyPropertyName.equals(propertyName)) {
				return true;
			}
		}
		return false;
	}

	public void deleteEntityData(EdmEntitySet edmEntitySet, List<UriParameter> keyParams)
			throws ODataApplicationException {

		EdmEntityType edmEntityType = edmEntitySet.getEntityType();

		// actually, this is only required if we have more than one Entity Type
		if (edmEntityType.getName().equals(DemoEdmProvider.ET_PRODUCT_NAME)) {
			deleteProduct(edmEntityType, keyParams);
		}
		if (edmEntityType.getName().equals(DemoEdmProvider.ET_APPCONFG_NAME)) {
			deleteAppConfg(edmEntityType, keyParams);
		}
		if (edmEntityType.getName().equals(DemoEdmProvider.ET_DATACONF_NAME)) {
			deleteDataConfg(edmEntityType, keyParams);
		}
		if (edmEntityType.getName().equals(DemoEdmProvider.ET_FLOORPLAN_NAME)) {
			deleteFloorplan(edmEntityType, keyParams);
		}
		if (edmEntityType.getName().equals(DemoEdmProvider.ET_DP_NAME)) {
			deleteDataProvider(edmEntityType, keyParams);
		}
	}

	/*
	 * private void deleteProduct(EdmEntityType edmEntityType,
	 * List<UriParameter> keyParams) throws ODataApplicationException {
	 * 
	 * Entity productEntity = getProduct(edmEntityType, keyParams); if
	 * (productEntity == null) { throw new
	 * ODataApplicationException("Entity not found",
	 * HttpStatusCode.NOT_FOUND.getStatusCode(), Locale.ENGLISH); }
	 * 
	 * this.productList.remove(productEntity); }
	 */
	private void deleteDataProvider(EdmEntityType edmEntityType, List<UriParameter> keyParams)
			throws ODataApplicationException {
		Entity extensionEntity = getDataProvider(edmEntityType, keyParams);
		if (extensionEntity == null) {
			throw new ODataApplicationException("Entity not found", HttpStatusCode.NOT_FOUND.getStatusCode(),
					Locale.ENGLISH);
		}
		// delete from db also
		String keyPropertyName = extensionEntity.getProperty("MODULEID").getName();
		String keyPropertyValue = String.valueOf(extensionEntity.getProperty("MODULEID").getValue());
		keyPropertyValue = Storage.quote(keyPropertyValue);
		java.sql.Connection connection = null;
		try {
			connection = DBUtillocal.getConnection();
			// java.sql.Statement statement = connection.createStatement();
			String query = "delete from  dpconfig where " + keyPropertyName + "=" + keyPropertyValue;
			Statement pstmt = connection.createStatement();
			int no = pstmt.executeUpdate(query);
			if (no > 0) {
				this.dpconfigList.remove(extensionEntity);
			}
		} catch (java.sql.SQLException se) {
			se.printStackTrace();
		} catch (Exception exp) {
			System.out.println("inside catch " + exp.getMessage());
		} finally {
			if (connection != null) {
				try {
					connection.close();
					DBUtillocal.Close();
				} catch (Exception e) {
					System.out.println("exception in closing connection");
					e.printStackTrace();
				}
			}
		}

	}

	private void deleteFloorplan(EdmEntityType edmEntityType, List<UriParameter> keyParams)
			throws ODataApplicationException {
		Entity extensionEntity = getFloorPlan(edmEntityType, keyParams);
		if (extensionEntity == null) {
			throw new ODataApplicationException("Entity not found", HttpStatusCode.NOT_FOUND.getStatusCode(),
					Locale.ENGLISH);
		}
		// delete from db also
		String keyPropertyName = extensionEntity.getProperty("APPID").getName();
		String keyPropertyValue = String.valueOf(extensionEntity.getProperty("APPID").getValue());
		keyPropertyValue = Storage.quote(keyPropertyValue);
		java.sql.Connection connection = null;
		try {
			connection = DBUtillocal.getConnection();
			// java.sql.Statement statement = connection.createStatement();
			String query = "delete from  floorplan where " + keyPropertyName + "=" + keyPropertyValue;
			Statement pstmt = connection.createStatement();
			int no = pstmt.executeUpdate(query);
			if (no > 0) {
				this.floorplanList.remove(extensionEntity);
			}
		} catch (java.sql.SQLException se) {
			se.printStackTrace();
		} catch (Exception exp) {
			System.out.println("inside catch " + exp.getMessage());
		} finally {
			if (connection != null) {
				try {
					connection.close();
					DBUtillocal.Close();
				} catch (Exception e) {
					System.out.println("exception in closing connection");
					e.printStackTrace();
				}
			}
		}

	}

	private void deleteDataConfg(EdmEntityType edmEntityType, List<UriParameter> keyParams)
			throws ODataApplicationException {
		Entity extensionEntity = getDataConfg(edmEntityType, keyParams);
		if (extensionEntity == null) {
			throw new ODataApplicationException("Entity not found", HttpStatusCode.NOT_FOUND.getStatusCode(),
					Locale.ENGLISH);
		}
		// delete from db also
		String keyPropertyName = extensionEntity.getProperty("MODULEID").getName();
		String keyPropertyValue = String.valueOf(extensionEntity.getProperty("MODULEID").getValue());
		keyPropertyValue = Storage.quote(keyPropertyValue);
		java.sql.Connection connection = null;
		try {
			connection = DBUtillocal.getConnection();
			// java.sql.Statement statement = connection.createStatement();
			String query = "delete from  dataconf where " + keyPropertyName + "=" + keyPropertyValue;
			Statement pstmt = connection.createStatement();
			int no = pstmt.executeUpdate(query);
			if (no > 0) {
				this.dataconfgList.remove(extensionEntity);
			}
		} catch (java.sql.SQLException se) {
			se.printStackTrace();
		} catch (Exception exp) {
			System.out.println("inside catch " + exp.getMessage());
		} finally {
			if (connection != null) {
				try {
					connection.close();
					DBUtillocal.Close();
				} catch (Exception e) {
					System.out.println("exception in closing connection");
					e.printStackTrace();
				}
			}
		}

	}

	private void deleteAppConfg(EdmEntityType edmEntityType, List<UriParameter> keyParams)
			throws ODataApplicationException {
		Entity extensionEntity = getAppConfg(edmEntityType, keyParams);
		if (extensionEntity == null) {
			throw new ODataApplicationException("Entity not found", HttpStatusCode.NOT_FOUND.getStatusCode(),
					Locale.ENGLISH);
		}
		// delete from db also
		String keyPropertyName = extensionEntity.getProperty("APPID").getName();
		String keyPropertyValue = String.valueOf(extensionEntity.getProperty("APPID").getValue());
		keyPropertyValue = Storage.quote(keyPropertyValue);
		java.sql.Connection connection = null;
		try {
			connection = DBUtillocal.getConnection();
			// java.sql.Statement statement = connection.createStatement();
			String query = "delete from  appconfg where " + keyPropertyName + "=" + keyPropertyValue;
			Statement pstmt = connection.createStatement();
			int no = pstmt.executeUpdate(query);
			if (no > 0) {
				this.appconfgList.remove(extensionEntity);
			}
		} catch (java.sql.SQLException se) {
			se.printStackTrace();
		} catch (Exception exp) {
			System.out.println("inside catch " + exp.getMessage());
		} finally {
			if (connection != null) {
				try {
					connection.close();
					DBUtillocal.Close();
				} catch (Exception e) {
					System.out.println("exception in closing connection");
					e.printStackTrace();
				}
			}
		}

	}

	public static String quote(String s) {
		return new StringBuilder().append('\'').append(s).append('\'').toString();
	}

	private void deleteProduct(EdmEntityType edmEntityType, List<UriParameter> keyParams)
			throws ODataApplicationException {
		Entity extensionEntity = getProduct(edmEntityType, keyParams);
		if (extensionEntity == null) {
			throw new ODataApplicationException("Entity not found", HttpStatusCode.NOT_FOUND.getStatusCode(),
					Locale.ENGLISH);
		}
		// delete from db also
		String keyPropertyName = extensionEntity.getProperty("ID").getName();
		String keyPropertyValue = String.valueOf(extensionEntity.getProperty("ID").getValue());
		java.sql.Connection connection = null;
		try {
			connection = DBUtillocal.getConnection();
			// java.sql.Statement statement = connection.createStatement();
			String query = "delete from  product where " + keyPropertyName + "=" + keyPropertyValue;
			Statement pstmt = connection.createStatement();
			int no = pstmt.executeUpdate(query);
			if (no > 0) {
				this.productList.remove(extensionEntity);
			}
		} catch (java.sql.SQLException se) {
			se.printStackTrace();
		} catch (Exception exp) {
			System.out.println("inside catch " + exp.getMessage());
		} finally {
			if (connection != null) {
				try {
					connection.close();
					DBUtillocal.Close();
				} catch (Exception e) {
					System.out.println("exception in closing connection");
					e.printStackTrace();
				}
			}
		}
	}
}
