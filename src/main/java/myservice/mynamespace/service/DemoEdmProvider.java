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
package myservice.mynamespace.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.olingo.commons.api.edm.EdmPrimitiveTypeKind;
import org.apache.olingo.commons.api.edm.FullQualifiedName;
import org.apache.olingo.commons.api.edm.provider.CsdlAbstractEdmProvider;
import org.apache.olingo.commons.api.edm.provider.CsdlEntityContainer;
import org.apache.olingo.commons.api.edm.provider.CsdlEntityContainerInfo;
import org.apache.olingo.commons.api.edm.provider.CsdlEntitySet;
import org.apache.olingo.commons.api.edm.provider.CsdlEntityType;
import org.apache.olingo.commons.api.edm.provider.CsdlNavigationProperty;
import org.apache.olingo.commons.api.edm.provider.CsdlNavigationPropertyBinding;
import org.apache.olingo.commons.api.edm.provider.CsdlProperty;
import org.apache.olingo.commons.api.edm.provider.CsdlPropertyRef;
import org.apache.olingo.commons.api.edm.provider.CsdlSchema;

import myservice.mynamespace.data.DBUtillocal;

public class DemoEdmProvider extends CsdlAbstractEdmProvider {

	// Service Namespace
	public static final String NAMESPACE = "OData.Demo";

	// EDM Container
	public static final String CONTAINER_NAME = "Container";
	public static final FullQualifiedName CONTAINER = new FullQualifiedName(NAMESPACE, CONTAINER_NAME);

	// Entity Types Names
	public static final String ET_PRODUCT_NAME = "Product";
	public static final FullQualifiedName ET_PRODUCT_FQN = new FullQualifiedName(NAMESPACE, ET_PRODUCT_NAME);

	public static final String ET_APPCONFG_NAME = "Appconfg";
	public static final FullQualifiedName ET_APPCONFG_FQN = new FullQualifiedName(NAMESPACE, ET_APPCONFG_NAME);

	public static final String ET_DATACONF_NAME = "Dataconf";
	public static final FullQualifiedName ET_DATACONF_FQN = new FullQualifiedName(NAMESPACE, ET_DATACONF_NAME);

	public static final String ET_FLOORPLAN_NAME = "Floorplan";
	public static final FullQualifiedName ET_FLOORPLAN_FQN = new FullQualifiedName(NAMESPACE, ET_FLOORPLAN_NAME);

	public static final String ET_CATEGORY_NAME = "Category";
	public static final FullQualifiedName ET_CATEGORY_FQN = new FullQualifiedName(NAMESPACE, ET_CATEGORY_NAME);

	public static final String ET_ZEMP_NAME = "Zemptable";
	public static final FullQualifiedName ET_ZEMP_FQN = new FullQualifiedName(NAMESPACE, ET_ZEMP_NAME);

	public static final String ET_DP_NAME = "Dpconfig";
	public static final FullQualifiedName ET_DP_FQN = new FullQualifiedName(NAMESPACE, ET_DP_NAME);

	// Entity Set Names
	public static final String ES_PRODUCTS_NAME = "Products";
	public static final String ES_CATEGORIES_NAME = "Categories";
	public static final String ES_ZEMP_NAME = "Zemptable";
	public static final String ES_DP_NAME = "Dpconfig";
	public static final String ES_APPCONFG_NAME = DBUtillocal.readCollectionNames(1);
	public static final String ES_DATACONF_NAME = DBUtillocal.readCollectionNames(2);
	public static final String ES_FLOORPLAN_NAME = DBUtillocal.readCollectionNames(3);

	@Override
	public CsdlEntityType getEntityType(FullQualifiedName entityTypeName) {

		// this method is called for each EntityType that are configured in the
		// Schema
		CsdlEntityType entityType = null;

		if (entityTypeName.equals(ET_PRODUCT_FQN)) {
			// create EntityType properties
			CsdlProperty id = new CsdlProperty().setName("ID")
					.setType(EdmPrimitiveTypeKind.Int32.getFullQualifiedName());
			CsdlProperty name = new CsdlProperty().setName("Name")
					.setType(EdmPrimitiveTypeKind.String.getFullQualifiedName());
			CsdlProperty description = new CsdlProperty().setName("Description")
					.setType(EdmPrimitiveTypeKind.String.getFullQualifiedName());

			// create PropertyRef for Key element
			CsdlPropertyRef propertyRef = new CsdlPropertyRef();
			propertyRef.setName("ID");

			// navigation property: many-to-one, null not allowed (product must
			// have a category)
			CsdlNavigationProperty navProp = new CsdlNavigationProperty().setName("Category").setType(ET_CATEGORY_FQN)
					.setNullable(false).setPartner("Products");
			List<CsdlNavigationProperty> navPropList = new ArrayList<CsdlNavigationProperty>();
			navPropList.add(navProp);

			// configure EntityType
			entityType = new CsdlEntityType();
			entityType.setName(ET_PRODUCT_NAME);
			entityType.setProperties(Arrays.asList(id, name, description));
			entityType.setKey(Arrays.asList(propertyRef));
			entityType.setNavigationProperties(navPropList);
			System.out.println(Arrays.asList(propertyRef.getName()));

		}

		if (entityTypeName.equals(ET_APPCONFG_FQN)) {
			// create EntityType properties
			CsdlProperty id = new CsdlProperty().setName("APPID")
					.setType(EdmPrimitiveTypeKind.String.getFullQualifiedName());
			CsdlProperty name = new CsdlProperty().setName("APPNAME")
					.setType(EdmPrimitiveTypeKind.String.getFullQualifiedName());
			CsdlProperty description = new CsdlProperty().setName("APPCONFIG")
					.setType(EdmPrimitiveTypeKind.String.getFullQualifiedName());

			// create PropertyRef for Key element
			CsdlPropertyRef propertyRef = new CsdlPropertyRef();
			propertyRef.setName("APPID");

			// navigation property: many-to-one, null not allowed (product must
			// have a category)
			/*
			 * CsdlNavigationProperty navProp = new
			 * CsdlNavigationProperty().setName("Category")
			 * .setType(ET_CATEGORY_FQN).setNullable(false).setPartner(
			 * "Products"); List<CsdlNavigationProperty> navPropList = new
			 * ArrayList<CsdlNavigationProperty>(); navPropList.add(navProp);
			 */

			// configure EntityType
			entityType = new CsdlEntityType();
			entityType.setName(ET_APPCONFG_NAME);
			entityType.setProperties(Arrays.asList(id, name, description));
			entityType.setKey(Arrays.asList(propertyRef));
			System.out.println(Arrays.asList(propertyRef.getName()));
			// entityType.setNavigationProperties(navPropList);

		}

		if (entityTypeName.equals(ET_DATACONF_FQN)) {
			// create EntityType properties
			CsdlProperty MODULEID = new CsdlProperty().setName("MODULEID")
					.setType(EdmPrimitiveTypeKind.String.getFullQualifiedName());

			CsdlProperty DPID = new CsdlProperty().setName("DPID")
					.setType(EdmPrimitiveTypeKind.String.getFullQualifiedName());

			CsdlProperty DATACATEGORY = new CsdlProperty().setName("DATACATEGORY")
					.setType(EdmPrimitiveTypeKind.String.getFullQualifiedName());

			CsdlProperty ZUSAGE = new CsdlProperty().setName("ZUSAGE")
					.setType(EdmPrimitiveTypeKind.String.getFullQualifiedName());

			CsdlProperty DATAFORMAT = new CsdlProperty().setName("DATAFORMAT")
					.setType(EdmPrimitiveTypeKind.String.getFullQualifiedName());

			CsdlProperty DELTATOKEN = new CsdlProperty().setName("DELTATOKEN")
					.setType(EdmPrimitiveTypeKind.String.getFullQualifiedName());

			CsdlProperty PAGESIZE = new CsdlProperty().setName("PAGESIZE")
					.setType(EdmPrimitiveTypeKind.Int32.getFullQualifiedName());

			CsdlProperty LOADMORE = new CsdlProperty().setName("LOADMORE")
					.setType(EdmPrimitiveTypeKind.String.getFullQualifiedName());

			// create PropertyRef for Key element
			CsdlPropertyRef propertyRef = new CsdlPropertyRef();
			propertyRef.setName("MODULEID");

			// configure EntityType
			entityType = new CsdlEntityType();
			entityType.setName(ET_DATACONF_NAME);
			entityType.setProperties(
					Arrays.asList(MODULEID, DPID, DATACATEGORY, ZUSAGE, DATAFORMAT, DELTATOKEN, PAGESIZE, LOADMORE));
			// entityType.setKey(Arrays.asList(propertyRef));
			entityType.setKey(
					Arrays.asList(new CsdlPropertyRef().setName("MODULEID"), new CsdlPropertyRef().setName("DPID")));
			System.out.println(Arrays.asList(propertyRef.getName()));

		}
		if (entityTypeName.equals(ET_FLOORPLAN_FQN)) {
			// create EntityType properties
			CsdlProperty id = new CsdlProperty().setName("APPID")
					.setType(EdmPrimitiveTypeKind.String.getFullQualifiedName());
			CsdlProperty moduleid = new CsdlProperty().setName("MODULEID")
					.setType(EdmPrimitiveTypeKind.String.getFullQualifiedName());
			CsdlProperty floorid = new CsdlProperty().setName("FPID")
					.setType(EdmPrimitiveTypeKind.String.getFullQualifiedName());
			CsdlProperty floorname = new CsdlProperty().setName("FPNAME")
					.setType(EdmPrimitiveTypeKind.String.getFullQualifiedName());
			CsdlProperty status = new CsdlProperty().setName("STATUS")
					.setType(EdmPrimitiveTypeKind.String.getFullQualifiedName());
			CsdlProperty floorconfg = new CsdlProperty().setName("FPCONFIG")
					.setType(EdmPrimitiveTypeKind.String.getFullQualifiedName());

			// create PropertyRef for Key element
			CsdlPropertyRef propertyRef = new CsdlPropertyRef();
			propertyRef.setName("APPID");

			// navigation property: many-to-one, null not allowed (product must
			// have a category)
			/*
			 * CsdlNavigationProperty navProp = new
			 * CsdlNavigationProperty().setName("Category")
			 * .setType(ET_CATEGORY_FQN).setNullable(false).setPartner(
			 * "Products"); List<CsdlNavigationProperty> navPropList = new
			 * ArrayList<CsdlNavigationProperty>(); navPropList.add(navProp);
			 */

			// configure EntityType
			entityType = new CsdlEntityType();
			entityType.setName(ET_FLOORPLAN_NAME);
			entityType.setProperties(Arrays.asList(id, moduleid, floorid, floorname, status, floorconfg));
			entityType.setKey(Arrays.asList(propertyRef));

			entityType.setKey(Arrays.asList(new CsdlPropertyRef().setName("APPID"),
					new CsdlPropertyRef().setName("MODULEID"), new CsdlPropertyRef().setName("FPID")));

			System.out.println(Arrays.asList(propertyRef.getName()));
			// entityType.setNavigationProperties(navPropList);

		}

		if (entityTypeName.equals(ET_DP_FQN)) {
			// create EntityType properties
			CsdlProperty moduleID = new CsdlProperty().setName("MODULEID")
					.setType(EdmPrimitiveTypeKind.String.getFullQualifiedName());
			CsdlProperty dataProvider = new CsdlProperty().setName("DATAPROVIDER")
					.setType(EdmPrimitiveTypeKind.String.getFullQualifiedName());
			CsdlProperty fieldName = new CsdlProperty().setName("FIELDNAME")
					.setType(EdmPrimitiveTypeKind.String.getFullQualifiedName());
			CsdlProperty apiName = new CsdlProperty().setName("APINAME")
					.setType(EdmPrimitiveTypeKind.String.getFullQualifiedName());
			CsdlProperty parameterName = new CsdlProperty().setName("PARAMETERNAME")
					.setType(EdmPrimitiveTypeKind.String.getFullQualifiedName());
			CsdlProperty status = new CsdlProperty().setName("STATUS")
					.setType(EdmPrimitiveTypeKind.String.getFullQualifiedName());
			CsdlProperty dpConfig = new CsdlProperty().setName("DPCONFIG")
					.setType(EdmPrimitiveTypeKind.String.getFullQualifiedName());
			CsdlProperty isKey = new CsdlProperty().setName("ISKEY")
					.setType(EdmPrimitiveTypeKind.String.getFullQualifiedName());

			CsdlPropertyRef propertyRef = new CsdlPropertyRef();
			propertyRef.setName("MODULEID");
			entityType = new CsdlEntityType();
			entityType.setName(ET_DP_NAME);
			entityType.setProperties(Arrays.asList(moduleID, dataProvider, fieldName, apiName, status, parameterName,
					status, dpConfig, isKey));
			entityType.setKey(Arrays.asList(propertyRef));
			entityType.setKey(Arrays.asList(new CsdlPropertyRef().setName("MODULEID"),
					new CsdlPropertyRef().setName("DATAPROVIDER"), new CsdlPropertyRef().setName("FIELDNAME")));
			System.out.println(Arrays.asList(propertyRef.getName()));

		}

		else if (entityTypeName.equals(ET_CATEGORY_FQN)) {
			// create EntityType properties
			CsdlProperty id = new CsdlProperty().setName("ID")
					.setType(EdmPrimitiveTypeKind.Int32.getFullQualifiedName());
			CsdlProperty name = new CsdlProperty().setName("Name")
					.setType(EdmPrimitiveTypeKind.String.getFullQualifiedName());

			// create PropertyRef for Key element
			CsdlPropertyRef propertyRef = new CsdlPropertyRef();
			propertyRef.setName("ID");

			// navigation property: one-to-many
			CsdlNavigationProperty navProp = new CsdlNavigationProperty().setName("Products").setType(ET_PRODUCT_FQN)
					.setCollection(true).setPartner("Category");
			List<CsdlNavigationProperty> navPropList = new ArrayList<CsdlNavigationProperty>();
			navPropList.add(navProp);

			// configure EntityType
			entityType = new CsdlEntityType();
			entityType.setName(ET_CATEGORY_NAME);
			entityType.setProperties(Arrays.asList(id, name));
			entityType.setKey(Arrays.asList(propertyRef));
			entityType.setNavigationProperties(navPropList);
		}

		else if (entityTypeName.equals(ET_ZEMP_FQN)) {
			CsdlProperty zeNo = new CsdlProperty().setName("ZENO")
					.setType(EdmPrimitiveTypeKind.String.getFullQualifiedName());
			CsdlProperty zeName = new CsdlProperty().setName("ZENAME")
					.setType(EdmPrimitiveTypeKind.String.getFullQualifiedName());
			CsdlProperty zeader = new CsdlProperty().setName("ZEADR")
					.setType(EdmPrimitiveTypeKind.String.getFullQualifiedName());
			CsdlProperty zemSalary = new CsdlProperty().setName("ZEMPSALA")
					.setType(EdmPrimitiveTypeKind.String.getFullQualifiedName());
			CsdlProperty zemDob = new CsdlProperty().setName("ZEMPDOB")
					.setType(EdmPrimitiveTypeKind.String.getFullQualifiedName());
			/*
			 * CsdlPropertyRef propertyRef = new CsdlPropertyRef();
			 * propertyRef.setName("ZENO"); entityType = new CsdlEntityType();
			 * entityType.setName(ET_ZEMP_NAME);
			 * entityType.setProperties(Arrays.asList(zeNo, zeName,
			 * zeader,zemSalary,zemDob));
			 * entityType.setKey(Arrays.asList(propertyRef));
			 * System.out.println(Arrays.asList(propertyRef.getName()));
			 */
			CsdlPropertyRef propertyRef = new CsdlPropertyRef();
			propertyRef.setName("ZENO");
			entityType = new CsdlEntityType();
			entityType.setName(ET_ZEMP_NAME);
			entityType.setProperties(Arrays.asList(zeNo, zeName, zeader, zemSalary, zemDob));
			entityType.setKey(Arrays.asList(propertyRef));
			System.out.println(Arrays.asList(propertyRef.getName()));
			// entityType.setNavigationProperties(navPropList);

		}

		return entityType;

	}

	@Override
	public CsdlEntitySet getEntitySet(FullQualifiedName entityContainer, String entitySetName) {

		CsdlEntitySet entitySet = null;

		if (entityContainer.equals(CONTAINER)) {

			if (entitySetName.equals(ES_PRODUCTS_NAME)) {

				entitySet = new CsdlEntitySet();
				entitySet.setName(ES_PRODUCTS_NAME);
				entitySet.setType(ET_PRODUCT_FQN);

				// navigation
				CsdlNavigationPropertyBinding navPropBinding = new CsdlNavigationPropertyBinding();
				navPropBinding.setTarget("Categories"); // the target entity
														// set, where the
														// navigation property
														// points to
				navPropBinding.setPath("Category"); // the path from entity type
													// to navigation property
				List<CsdlNavigationPropertyBinding> navPropBindingList = new ArrayList<CsdlNavigationPropertyBinding>();
				navPropBindingList.add(navPropBinding);
				entitySet.setNavigationPropertyBindings(navPropBindingList);

			}

			if (entitySetName.equals(ES_APPCONFG_NAME)) {

				entitySet = new CsdlEntitySet();
				entitySet.setName(ES_APPCONFG_NAME);
				entitySet.setType(ET_APPCONFG_FQN);

				/*
				 * // navigation CsdlNavigationPropertyBinding navPropBinding =
				 * new CsdlNavigationPropertyBinding();
				 * navPropBinding.setTarget("Categories"); // the target entity
				 * set, where the navigation property points to
				 * navPropBinding.setPath("Category"); // the path from entity
				 * type to navigation property
				 * List<CsdlNavigationPropertyBinding> navPropBindingList = new
				 * ArrayList<CsdlNavigationPropertyBinding>();
				 * navPropBindingList.add(navPropBinding);
				 * entitySet.setNavigationPropertyBindings(navPropBindingList);
				 */

			}
			if (entitySetName.equals(ES_DATACONF_NAME)) {

				entitySet = new CsdlEntitySet();
				entitySet.setName(ES_DATACONF_NAME);
				entitySet.setType(ET_DATACONF_FQN);
			}
			if (entitySetName.equals(ES_FLOORPLAN_NAME)) {

				entitySet = new CsdlEntitySet();
				entitySet.setName(ES_FLOORPLAN_NAME);
				entitySet.setType(ET_FLOORPLAN_FQN);
			}

			else if (entitySetName.equals(ES_CATEGORIES_NAME)) {

				entitySet = new CsdlEntitySet();
				entitySet.setName(ES_CATEGORIES_NAME);
				entitySet.setType(ET_CATEGORY_FQN);

				// navigation
				CsdlNavigationPropertyBinding navPropBinding = new CsdlNavigationPropertyBinding();
				navPropBinding.setTarget("Products"); // the target entity set,
														// where the navigation
														// property points to
				navPropBinding.setPath("Products"); // the path from entity type
													// to navigation property
				List<CsdlNavigationPropertyBinding> navPropBindingList = new ArrayList<CsdlNavigationPropertyBinding>();
				navPropBindingList.add(navPropBinding);
				entitySet.setNavigationPropertyBindings(navPropBindingList);
			}

			else if (entitySetName.equals(ES_ZEMP_NAME)) {

				entitySet = new CsdlEntitySet();
				entitySet.setName(ES_ZEMP_NAME);
				entitySet.setType(ET_ZEMP_FQN);

			}

			else if (entitySetName.equals(ES_DP_NAME)) {

				entitySet = new CsdlEntitySet();
				entitySet.setName(ES_DP_NAME);
				entitySet.setType(ET_DP_FQN);

			}
		}

		return entitySet;
	}

	@Override
	public CsdlEntityContainerInfo getEntityContainerInfo(FullQualifiedName entityContainerName) {

		// This method is invoked when displaying the service document at
		// e.g. http://localhost:8080/DemoService/DemoService.svc
		if (entityContainerName == null || entityContainerName.equals(CONTAINER)) {
			CsdlEntityContainerInfo entityContainerInfo = new CsdlEntityContainerInfo();
			entityContainerInfo.setContainerName(CONTAINER);
			return entityContainerInfo;
		}

		return null;
	}

	@Override
	public List<CsdlSchema> getSchemas() {
		// create Schema
		CsdlSchema schema = new CsdlSchema();
		schema.setNamespace(NAMESPACE);

		// add EntityTypes
		List<CsdlEntityType> entityTypes = new ArrayList<CsdlEntityType>();
		entityTypes.add(getEntityType(ET_PRODUCT_FQN));
		entityTypes.add(getEntityType(ET_CATEGORY_FQN));
		entityTypes.add(getEntityType(ET_APPCONFG_FQN));
		entityTypes.add(getEntityType(ET_DATACONF_FQN));
		entityTypes.add(getEntityType(ET_FLOORPLAN_FQN));
		entityTypes.add(getEntityType(ET_ZEMP_FQN));
		entityTypes.add(getEntityType(ET_DP_FQN));
		schema.setEntityTypes(entityTypes);

		// add EntityContainer
		schema.setEntityContainer(getEntityContainer());

		// finally
		List<CsdlSchema> schemas = new ArrayList<CsdlSchema>();
		schemas.add(schema);

		return schemas;
	}

	@Override
	public CsdlEntityContainer getEntityContainer() {

		// create EntitySets
		List<CsdlEntitySet> entitySets = new ArrayList<CsdlEntitySet>();
		entitySets.add(getEntitySet(CONTAINER, ES_PRODUCTS_NAME));
		entitySets.add(getEntitySet(CONTAINER, ES_CATEGORIES_NAME));
		entitySets.add(getEntitySet(CONTAINER, ES_APPCONFG_NAME));
		entitySets.add(getEntitySet(CONTAINER, ES_DATACONF_NAME));
		entitySets.add(getEntitySet(CONTAINER, ES_FLOORPLAN_NAME));
		entitySets.add(getEntitySet(CONTAINER, ES_ZEMP_NAME));
		entitySets.add(getEntitySet(CONTAINER, ES_DP_NAME));

		// create EntityContainer
		CsdlEntityContainer entityContainer = new CsdlEntityContainer();
		entityContainer.setName(CONTAINER_NAME);
		entityContainer.setEntitySets(entitySets);

		return entityContainer;
	}
}
