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

import java.io.IOException;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.UnknownHostException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import org.apache.commons.codec.binary.Base64;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.olingo.client.api.ODataClient;
import org.apache.olingo.client.api.communication.request.retrieve.ODataServiceDocumentRequest;
import org.apache.olingo.client.core.ODataClientFactory;
import org.apache.olingo.client.core.http.BasicAuthHttpClientFactory;
import org.apache.olingo.commons.api.data.ContextURL;
import org.apache.olingo.commons.api.data.Entity;
import org.apache.olingo.commons.api.data.EntityCollection;
import org.apache.olingo.commons.api.edm.EdmEntitySet;
import org.apache.olingo.commons.api.edm.EdmEntityType;
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
import org.apache.olingo.server.api.uri.queryoption.SkipOption;
import org.apache.olingo.server.api.uri.queryoption.TopOption;

import myservice.mynamespace.data.DBUtillocal;
import myservice.mynamespace.data.Storage;
import myservice.mynamespace.util.TracingBean;

public class DemoEntityCollectionProcessor implements EntityCollectionProcessor {
	private OData odata;
	private ServiceMetadata srvMetadata;
	// our database-mock
	private Storage storage;
	InetAddress ip = null;
	String hostname;

	TracingBean bean = new TracingBean();

	public DemoEntityCollectionProcessor(Storage storage) {
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

	/*
	 * This method is invoked when a collection of entities has to be read. In
	 * our example, this can be either a "normal" read operation, or a
	 * navigation:
	 * 
	 * Example for "normal" read entity set operation:
	 * http://localhost:8080/DemoService/DemoService.svc/Categories
	 * 
	 * Example for navigation
	 * http://localhost:8080/DemoService/DemoService.svc/Categories(3)/Products
	 */
	/*
	 * public void readEntityCollection(ODataRequest request, ODataResponse
	 * response, UriInfo uriInfo, ContentType responseFormat) throws
	 * ODataApplicationException, SerializerException { Date relativeTime = new
	 * Date(); bean.setRequest_Method(request.getMethod() + "");
	 * bean.setContent_type_body("Content-Type:" + responseFormat); EdmEntitySet
	 * responseEdmEntitySet = null; // we'll need this to build // the
	 * ContextURL EntityCollection responseEntityCollection = null; // we'll
	 * need this to // set the response // body
	 * 
	 * // 1st retrieve the requested EntitySet from the uriInfo (representation
	 * // of the parsed URI) List<UriResource> resourceParts =
	 * uriInfo.getUriResourceParts(); int segmentCount = resourceParts.size();
	 * 
	 * UriResource uriResource = resourceParts.get(0); // in our example, the //
	 * first segment is the // EntitySet if (!(uriResource instanceof
	 * UriResourceEntitySet)) { throw new
	 * ODataApplicationException("Only EntitySet is supported",
	 * HttpStatusCode.NOT_IMPLEMENTED.getStatusCode(), Locale.ROOT); }
	 * 
	 * UriResourceEntitySet uriResourceEntitySet = (UriResourceEntitySet)
	 * uriResource; EdmEntitySet startEdmEntitySet =
	 * uriResourceEntitySet.getEntitySet();
	 * 
	 * if (segmentCount == 1) { // this is the case for: //
	 * DemoService/DemoService.svc/Categories responseEdmEntitySet =
	 * startEdmEntitySet; // the response body is // built from the first //
	 * (and only) entitySet
	 * 
	 * // 2nd: fetch the data from backend for this requested EntitySetName //
	 * and deliver as EntitySet responseEntityCollection =
	 * storage.readEntitySetData(startEdmEntitySet); } else if (segmentCount ==
	 * 2) { // in case of navigation: // DemoService.svc/Categories(3)/Products
	 * 
	 * UriResource lastSegment = resourceParts.get(1); // in our example we //
	 * don't support // more complex URIs if (lastSegment instanceof
	 * UriResourceNavigation) { UriResourceNavigation uriResourceNavigation =
	 * (UriResourceNavigation) lastSegment; EdmNavigationProperty
	 * edmNavigationProperty = uriResourceNavigation.getProperty();
	 * EdmEntityType targetEntityType = edmNavigationProperty.getType(); // from
	 * Categories(1) to Products responseEdmEntitySet =
	 * Util.getNavigationTargetEntitySet(startEdmEntitySet,
	 * edmNavigationProperty);
	 * 
	 * // 2nd: fetch the data from backend // first fetch the entity where the
	 * first segment of the URI // points to List<UriParameter> keyPredicates =
	 * uriResourceEntitySet.getKeyPredicates(); // e.g. for
	 * Categories(3)/Products we have to find the single // entity: Category
	 * with ID 3 Entity sourceEntity = storage.readEntityData(startEdmEntitySet,
	 * keyPredicates); // error handling for e.g. //
	 * DemoService.svc/Categories(99)/Products if (sourceEntity == null) { throw
	 * new ODataApplicationException("Entity not found.",
	 * HttpStatusCode.NOT_FOUND.getStatusCode(), Locale.ROOT); } // then fetch
	 * the entity collection where the entity navigates // to // note: we don't
	 * need to check // uriResourceNavigation.isCollection(), // because we are
	 * the EntityCollectionProcessor responseEntityCollection =
	 * storage.getRelatedEntityCollection(sourceEntity, targetEntityType); } }
	 * else { // this would be the case for e.g. //
	 * Products(1)/Category/Products throw new
	 * ODataApplicationException("Not supported",
	 * HttpStatusCode.NOT_IMPLEMENTED.getStatusCode(), Locale.ROOT); }
	 * 
	 * // 3rd: create and configure a serializer ContextURL contextUrl =
	 * ContextURL.with().entitySet(responseEdmEntitySet).build(); final String
	 * id = request.getRawBaseUri() + "/" + responseEdmEntitySet.getName();
	 * System.out.println("http.url : " + id);
	 * bean.setRequest_Uri(request.getRawRequestUri());
	 * 
	 * EntityCollectionSerializerOptions opts =
	 * EntityCollectionSerializerOptions.with().contextURL(contextUrl).id(id)
	 * .build(); EdmEntityType edmEntityType =
	 * responseEdmEntitySet.getEntityType();
	 * 
	 * ODataSerializer serializer = odata.createSerializer(responseFormat);
	 * SerializerResult serializerResult =
	 * serializer.entityCollection(this.srvMetadata, edmEntityType,
	 * responseEntityCollection, opts);
	 * 
	 * // 4th: configure the response object: set the body, headers and status
	 * // code Date nowTime = new Date(); long diffs = nowTime.getTime() -
	 * relativeTime.getTime(); ; long dateandTimes = diffs / 1000;
	 * bean.setTime_Diffirence(dateandTimes);
	 * bean.setResponse_Code(HttpStatusCode.OK.getStatusCode());
	 * 
	 * Connection con = null; try { con = DBUtillocal.getConnection(); int ID =
	 * 0; String querys = "select * from requesttracing"; Statement pstmt =
	 * con.createStatement(); java.sql.ResultSet rs =
	 * pstmt.executeQuery(querys); while (rs.next()) {
	 * 
	 * ID = rs.getInt("sr_no") + 1; } bean.setSr_no(ID); String query =
	 * "insert into requesttracing(ip,hostname,dateand_Time,time_Diffirence,request_Uri,response_Code,sr_no,request_Method,content_type_body) values(?,?,?,?,?,?,?,?,?)"
	 * ; PreparedStatement pStmt = con.prepareStatement(query);
	 * pStmt.setString(1, bean.getIp() + ""); pStmt.setString(2,
	 * bean.getHostname() + ""); pStmt.setString(3, bean.getDateand_Time() +
	 * ""); pStmt.setLong(4, bean.getTime_Diffirence()); pStmt.setString(5,
	 * bean.getRequest_Uri()); pStmt.setInt(6, bean.getResponse_Code());
	 * pStmt.setInt(7, bean.getSr_no()); pStmt.setString(8,
	 * bean.getRequest_Method() + ""); pStmt.setString(9,
	 * bean.getContent_type_body()); int n = pStmt.executeUpdate(); if (n > 0) {
	 * System.out.println("Data Inserted Successfully ID:" + n); }
	 * 
	 * } catch (SQLException e) { // TODO Auto-generated catch block
	 * e.printStackTrace(); } finally { if (con != null) { try { con.close();
	 * DBUtillocal.Close(); } catch (Exception e) {
	 * System.out.println("exception in closing connection");
	 * e.printStackTrace(); } } }
	 * 
	 * response.setContent(serializerResult.getContent());
	 * response.setStatusCode(HttpStatusCode.OK.getStatusCode());
	 * response.setHeader(HttpHeader.CONTENT_TYPE,
	 * responseFormat.toContentTypeString());
	 * 
	 * }
	 */

	public void readEntityCollection(ODataRequest request, ODataResponse response, UriInfo uriInfo,
			ContentType responseFormat) throws ODataApplicationException, SerializerException {
		

		Map<String, List<String>> map = request.getAllHeaders();
		for (Map.Entry<String, List<String>> entry : map.entrySet()) {
			String key = entry.getKey();
			for (String value : entry.getValue()) {
				System.out.println(key+"----"+value);
			}
		}
		
		String name = "admin";
		String password = "admin";
		String authString = name + ":" + password;
		System.out.println("auth string: " + authString);
		byte[] authEncBytes = Base64.encodeBase64(authString.getBytes());
		String authStringEnc = new String(authEncBytes);
		System.out.println("Base64 encoded auth string: " + authStringEnc);
		URL url = null;
		try {
			url = new URL(request.getRawRequestUri());
		} catch (MalformedURLException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		URLConnection urlConnection = null;
		try {
			urlConnection = url.openConnection();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		urlConnection.setRequestProperty("Authorization", "Basic " + authStringEnc);

		
		ODataClient client = ODataClientFactory.getClient();
		// add the configuration here
		client.getConfiguration().setHttpClientFactory(new BasicAuthHttpClientFactory("raju", "raju"));
		// String iCrmServiceRoot = "https://example.dev/Authenticated/Service";
		ODataServiceDocumentRequest odClientReq = client.getRetrieveRequestFactory()
				.getServiceDocumentRequest(request.getRawRequestUri());
		System.out.println(odClientReq.getURI() + "----------------");

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

		// after applying the query options, create EntityCollection based on
		// the reduced list
		for (Entity entity : entityList) {
			returnEntityCollection.getEntities().add(entity);
		}

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
		;
		long dateandTimes = diffs / 1000;
		bean.setTime_Diffirence(dateandTimes);
		bean.setResponse_Code(HttpStatusCode.OK.getStatusCode());

		Connection con = null;
		try {
			con = DBUtillocal.getConnection();
			int ID = 0;
			String querys = "select * from requesttracing";
			Statement pstmt = con.createStatement();
			java.sql.ResultSet rs = pstmt.executeQuery(querys);
			while (rs.next()) {

				ID = rs.getInt("sr_no") + 1;
			}
			bean.setSr_no(ID);
			String query = "insert into requesttracing(ip,hostname,dateand_Time,time_Diffirence,request_Uri,response_Code,sr_no,request_Method,content_type_body,operation_name,class_name) values(?,?,?,?,?,?,?,?,?,?,?)";
			PreparedStatement pStmt = con.prepareStatement(query);
			pStmt.setString(1, bean.getIp() + "");
			pStmt.setString(2, bean.getHostname() + "");
			pStmt.setString(3, bean.getDateand_Time() + "");
			pStmt.setLong(4, bean.getTime_Diffirence());
			pStmt.setString(5, bean.getRequest_Uri());
			pStmt.setInt(6, bean.getResponse_Code());
			pStmt.setInt(7, bean.getSr_no());
			pStmt.setString(8, bean.getRequest_Method() + "");
			pStmt.setString(9, bean.getContent_type_body());
			pStmt.setString(10, bean.getOperation_name());
			pStmt.setString(11, bean.getClass_name());

			int n = pStmt.executeUpdate();
			if (n > 0) {
				System.out.println("Data Inserted Successfully ID:" + n);
			}

		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			if (con != null) {
				try {
					con.close();
					DBUtillocal.Close();
				} catch (Exception e) {
					System.out.println("exception in closing connection");
					e.printStackTrace();
				}
			}
		}

		// 5th: configure the response object: set the body, headers and status
		// codez
		response.setContent(serializerResult.getContent());
		response.setStatusCode(HttpStatusCode.OK.getStatusCode());
		response.setHeader(HttpHeader.CONTENT_TYPE, responseFormat.toContentTypeString());
	}

}
