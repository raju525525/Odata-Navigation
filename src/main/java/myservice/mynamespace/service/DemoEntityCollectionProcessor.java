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

import java.io.ByteArrayInputStream;
import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.UnknownHostException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import org.apache.olingo.commons.api.data.ContextURL;
import org.apache.olingo.commons.api.data.Entity;
import org.apache.olingo.commons.api.data.EntityCollection;
import org.apache.olingo.commons.api.data.Property;
import org.apache.olingo.commons.api.data.ValueType;
import org.apache.olingo.commons.api.edm.EdmEntitySet;
import org.apache.olingo.commons.api.edm.EdmEntityType;
import org.apache.olingo.commons.api.ex.ODataRuntimeException;
import org.apache.olingo.commons.api.format.ContentType;
import org.apache.olingo.commons.api.http.HttpHeader;
import org.apache.olingo.commons.api.http.HttpStatusCode;
import org.apache.olingo.server.api.OData;
import org.apache.olingo.server.api.ODataApplicationException;
import org.apache.olingo.server.api.ODataRequest;
import org.apache.olingo.server.api.ODataResponse;
import org.apache.olingo.server.api.ServiceMetadata;
import org.apache.olingo.server.api.processor.EntityCollectionProcessor;
import org.apache.olingo.server.api.serializer.EntityCollectionSerializerOptions;
import org.apache.olingo.server.api.serializer.ODataSerializer;
import org.apache.olingo.server.api.serializer.SerializerException;
import org.apache.olingo.server.api.serializer.SerializerResult;
import org.apache.olingo.server.api.uri.UriInfo;
import org.apache.olingo.server.api.uri.UriResource;
import org.apache.olingo.server.api.uri.UriResourceEntitySet;
import org.apache.olingo.server.api.uri.queryoption.CountOption;
import org.apache.olingo.server.api.uri.queryoption.FilterOption;
import org.apache.olingo.server.api.uri.queryoption.SkipOption;
import org.apache.olingo.server.api.uri.queryoption.TopOption;
import org.apache.olingo.server.api.uri.queryoption.expression.Expression;
import org.apache.olingo.server.api.uri.queryoption.expression.ExpressionVisitException;

import com.sap.conn.jco.JCoDestination;
import com.sap.conn.jco.JCoException;
import com.sap.conn.jco.JCoFunction;
import com.sap.conn.jco.JCoTable;

import myservice.mynamespace.data.DBUtillocal;
import myservice.mynamespace.data.ODataServiceTrace;
import myservice.mynamespace.data.Storage;
import myservice.mynamespace.jco.TestJco;
import myservice.mynamespace.util.TracingBean;

public class DemoEntityCollectionProcessor implements EntityCollectionProcessor {
	List<Entity> etdynamicData;

	private OData odata;
	private ServiceMetadata srvMetadata;
	// our database-mock
	private Storage storage;
	InetAddress ip = null;
	String hostname;

	TracingBean bean = new TracingBean();

	public DemoEntityCollectionProcessor(Storage storage) {
		etdynamicData = new ArrayList<Entity>();
		this.storage = storage;
		try {
			ip = InetAddress.getLocalHost();
			bean.setIp(ip);
			hostname = ip.getHostName();
			bean.setHostname(hostname);
			String dateandTime = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(Calendar.getInstance().getTime());
			bean.setDateand_Time(dateandTime);
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public void init(OData odata, ServiceMetadata serviceMetadata) {
		this.odata = odata;
		this.srvMetadata = serviceMetadata;
	}

	public void readEntityCollection(ODataRequest request, ODataResponse response, UriInfo uriInfo,
			ContentType responseFormat) throws ODataApplicationException, SerializerException {

		Date relativeTime = new Date();
		bean.setRequest_Method(request.getMethod() + "");
		bean.setContent_type_body("Content-Type:" + responseFormat);
		bean.setOperation_name("READ_ENTIRY");
		bean.setClass_name("DemoEntityCollectionProcessor");

		// 1st retrieve the requested EntitySet from the uriInfo
		List<UriResource> resourcePaths = uriInfo.getUriResourceParts();
		UriResourceEntitySet uriResourceEntitySet = (UriResourceEntitySet) resourcePaths.get(0);
		EdmEntitySet edmEntitySet = uriResourceEntitySet.getEntitySet();

		// 2nd: fetch the data from backend for this requested EntitySetName
		EntityCollection entityCollection = storage.readEntitySetData(edmEntitySet);

		// 3rd: apply System Query Options
		// modify the result set according to the query options, specified by
		// the end user
		List<Entity> entityList = entityCollection.getEntities();
		EntityCollection returnEntityCollection = new EntityCollection();

		// handle $count: return the original number of entities, ignore $top
		// and $skip
		CountOption countOption = uriInfo.getCountOption();
		if (countOption != null) {
			boolean isCount = countOption.getValue();
			if (isCount) {
				returnEntityCollection.setCount(entityList.size());
			}
		}

		// handle $skip
		SkipOption skipOption = uriInfo.getSkipOption();
		if (skipOption != null) {
			int skipNumber = skipOption.getValue();
			if (skipNumber >= 0) {
				if (skipNumber <= entityList.size()) {
					entityList = entityList.subList(skipNumber, entityList.size());
				} else {
					// The client skipped all entities
					entityList.clear();
				}
			} else {
				throw new ODataApplicationException("Invalid value for $skip",
						HttpStatusCode.BAD_REQUEST.getStatusCode(), Locale.ROOT);
			}
		}

		// handle $top
		TopOption topOption = uriInfo.getTopOption();
		if (topOption != null) {
			int topNumber = topOption.getValue();
			if (topNumber >= 0) {
				if (topNumber <= entityList.size()) {
					entityList = entityList.subList(0, topNumber);
				} // else the client has requested more entities than available
					// => return what we have
			} else {
				throw new ODataApplicationException("Invalid value for $top",
						HttpStatusCode.BAD_REQUEST.getStatusCode(), Locale.ROOT);
			}
		}

		// 3rd: Check if filter system query option is provided and apply the
		// expression if necessary
		FilterOption filterOption = uriInfo.getFilterOption();
		if (filterOption != null) {
			// Apply $filter system query option
			try {
				entityList = entityCollection.getEntities();
				Iterator<Entity> entityIterator = entityList.iterator();

				// Evaluate the expression for each entity
				// If the expression is evaluated to "true", keep the entity
				// otherwise remove it from the entityList
				while (entityIterator.hasNext()) {
					// To evaluate the the expression, create an instance of the
					// Filter Expression Visitor and pass
					// the current entity to the constructor
					Entity currentEntity = entityIterator.next();
					Expression filterExpression = filterOption.getExpression();
					FilterExpressionVisitor expressionVisitor = new FilterExpressionVisitor(currentEntity);

					// Start evaluating the expression
					Object visitorResult = filterExpression.accept(expressionVisitor);

					// The result of the filter expression must be of type
					// Edm.Boolean
					if (visitorResult instanceof Boolean) {
						if (!Boolean.TRUE.equals(visitorResult)) {
							// The expression evaluated to false (or null), so
							// we have to remove the currentEntity from
							// entityList
							entityIterator.remove();
						}
					} else {
						throw new ODataApplicationException("A filter expression must evaulate to type Edm.Boolean",
								HttpStatusCode.BAD_REQUEST.getStatusCode(), Locale.ENGLISH);
					}
				}

			} catch (ExpressionVisitException e) {
				throw new ODataApplicationException("Exception in filter evaluation",
						HttpStatusCode.INTERNAL_SERVER_ERROR.getStatusCode(), Locale.ENGLISH);
			}
		}

		// after applying the query options, create EntityCollection based on
		// the reduced list

		// JCO START
		JCoDestination destination = TestJco.callZempTable();
		JCoFunction function = null;
		try {
			function = destination.getRepository().getFunction("/INVAPI/BAPI_DYNAMIC_GET_CALL");
		} catch (JCoException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		}
		// JCO END

		// Entity display Process
		for (Entity entity : entityList) {
			returnEntityCollection.getEntities().add(entity);

			// JCO START with only call Filter not null
			if (filterOption != null) {
				if (function == null)
					throw new RuntimeException("Not found in SAP.");
				JCoTable tableParameterList = function.getImportParameterList().getTable("IT_DPDATA");
				tableParameterList.appendRow();
				tableParameterList.setValue("MODULEID", entity.getProperty("MODULEID").getValue().toString());
				tableParameterList.setValue("DATAPROVIDER", entity.getProperty("DATAPROVIDER").getValue().toString());
				tableParameterList.setValue("FIELDNAME", entity.getProperty("FIELDNAME").getValue().toString());
				tableParameterList.setValue("APINAME", entity.getProperty("APINAME").getValue().toString());
				tableParameterList.setValue("PARAMETERNAME", entity.getProperty("PARAMETERNAME").getValue().toString());
				tableParameterList.setValue("DPCONFIG", entity.getProperty("DPCONFIG").getValue().toString());
			}
			// JCO END

		}

		// JCO START with only call Filter not null
		if (filterOption != null) {
			try {
				function.execute(destination);
			} catch (JCoException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}

			JCoTable resultTable = function.getExportParameterList().getTable("ET_DYNAMIC_DATA");
			try {

			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			for (int i = 0; i < resultTable.getNumRows(); i++, resultTable.nextRow()) {
				etdynamicData.clear();
				String SNO = resultTable.getString("SNO");
				String DATAPROVIDER = resultTable.getString("DATAPROVIDER");
				String KEYVALUES = resultTable.getString("KEYVALUES");
				String JSON = resultTable.getString("JSON");
				Entity entity = new Entity().addProperty(new Property(null, "SNO", ValueType.PRIMITIVE, SNO))
						.addProperty(new Property(null, "DATAPROVIDER", ValueType.PRIMITIVE, DATAPROVIDER))
						.addProperty(new Property(null, "KEYVALUES", ValueType.PRIMITIVE, KEYVALUES))
						.addProperty(new Property(null, "JSON", ValueType.PRIMITIVE, JSON));
				entity.setId(createId("ET_DYNAMIC_DATA", entity, SNO));
				etdynamicData.add(entity);
			}

		}
		// JCO END

		// 4th: create a serializer based on the requested format (json)
		ODataSerializer serializer = odata.createSerializer(responseFormat);

		// and serialize the content: transform from the EntitySet object to
		// InputStream
		EdmEntityType edmEntityType = edmEntitySet.getEntityType();
		ContextURL contextUrl = ContextURL.with().entitySet(edmEntitySet).build();
		bean.setRequest_Uri(request.getRawRequestUri());

		final String id = request.getRawBaseUri() + "/" + edmEntitySet.getName();
		EntityCollectionSerializerOptions opts = EntityCollectionSerializerOptions.with().contextURL(contextUrl).id(id)
				.count(countOption).build();
		SerializerResult serializerResult = serializer.entityCollection(srvMetadata, edmEntityType,
				returnEntityCollection, opts);
		Date nowTime = new Date();
		long diffs = nowTime.getTime() - relativeTime.getTime();
		long dateandTimes = diffs / 1000;
		bean.setTime_Diffirence(dateandTimes);
		bean.setResponse_Code(HttpStatusCode.OK.getStatusCode());

		// Tracing inserertion Code
		ODataServiceTrace.tracingMethod(bean);

		// 5th: configure the response object: set the body, headers and status
		// codez

		// JCO START with only call Filter not null
		if (filterOption != null) {
			ByteArrayInputStream stream = null;
			StringBuilder sb = new StringBuilder();
			for (Entity s : etdynamicData) {
				sb.append(s);
			}
			try {
				stream = new ByteArrayInputStream(sb.toString().getBytes("UTF-8"));
			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			response.setContent(stream);
			response.setStatusCode(HttpStatusCode.OK.getStatusCode());
			response.setHeader(HttpHeader.CONTENT_TYPE, responseFormat.toContentTypeString());
		}
		// JCO END
		else {
			response.setContent(serializerResult.getContent());
			response.setStatusCode(HttpStatusCode.OK.getStatusCode());
			response.setHeader(HttpHeader.CONTENT_TYPE, responseFormat.toContentTypeString());
		}
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
}
