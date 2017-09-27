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

import java.io.InputStream;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.apache.olingo.commons.api.data.ContextURL;
import org.apache.olingo.commons.api.data.ContextURL.Suffix;
import org.apache.olingo.commons.api.data.Entity;
import org.apache.olingo.commons.api.edm.EdmEntitySet;
import org.apache.olingo.commons.api.edm.EdmEntityType;
import org.apache.olingo.commons.api.edm.EdmNavigationProperty;
import org.apache.olingo.commons.api.format.ContentType;
import org.apache.olingo.commons.api.http.HttpHeader;
import org.apache.olingo.commons.api.http.HttpMethod;
import org.apache.olingo.commons.api.http.HttpStatusCode;
import org.apache.olingo.server.api.OData;
import org.apache.olingo.server.api.ODataApplicationException;
import org.apache.olingo.server.api.ODataRequest;
import org.apache.olingo.server.api.ODataResponse;
import org.apache.olingo.server.api.ServiceMetadata;
import org.apache.olingo.server.api.deserializer.DeserializerException;
import org.apache.olingo.server.api.deserializer.DeserializerResult;
import org.apache.olingo.server.api.deserializer.ODataDeserializer;
import org.apache.olingo.server.api.processor.EntityProcessor;
import org.apache.olingo.server.api.serializer.EntitySerializerOptions;
import org.apache.olingo.server.api.serializer.ODataSerializer;
import org.apache.olingo.server.api.serializer.SerializerException;
import org.apache.olingo.server.api.serializer.SerializerResult;
import org.apache.olingo.server.api.uri.UriInfo;
import org.apache.olingo.server.api.uri.UriParameter;
import org.apache.olingo.server.api.uri.UriResource;
import org.apache.olingo.server.api.uri.UriResourceEntitySet;
import org.apache.olingo.server.api.uri.UriResourceNavigation;

import myservice.mynamespace.data.DBUtillocal;
import myservice.mynamespace.data.ODataServiceTrace;
import myservice.mynamespace.data.Storage;
import myservice.mynamespace.util.TracingBean;
import myservice.mynamespace.util.Util;

public class DemoEntityProcessor implements EntityProcessor {

	private OData odata;
	private ServiceMetadata srvMetadata;
	private Storage storage;
	InetAddress ip = null;
	String hostname;
	TracingBean bean = new TracingBean();

	public DemoEntityProcessor(Storage storage) {
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

	/**
	 * This method is invoked when a single entity has to be read. In our
	 * example, this can be either a "normal" read operation, or a navigation:
	 * 
	 * Example for "normal" read operation:
	 * http://localhost:8080/DemoService/DemoService.svc/Products(1)
	 * 
	 * Example for navigation
	 * http://localhost:8080/DemoService/DemoService.svc/Products(1)/Category
	 */
	public void readEntity(ODataRequest request, ODataResponse response, UriInfo uriInfo, ContentType responseFormat)
			throws ODataApplicationException, SerializerException {
		Date relativeTime = new Date();
		bean.setRequest_Method(request.getMethod() + "");
		bean.setContent_type_body("Content-Type:" + responseFormat);
		bean.setOperation_name("READ_ENTIRY");
		bean.setClass_name("DemoEntityProcessor");

		EdmEntityType responseEdmEntityType = null; // we'll need this to build
													// the ContextURL
		Entity responseEntity = null; // required for serialization of the
										// response body
		EdmEntitySet responseEdmEntitySet = null; // we need this for building
													// the contextUrl

		// 1st step: retrieve the requested Entity: can be "normal" read
		// operation, or navigation (to-one)
		List<UriResource> resourceParts = uriInfo.getUriResourceParts();
		int segmentCount = resourceParts.size();

		UriResource uriResource = resourceParts.get(0); // in our example, the
														// first segment is the
														// EntitySet
		if (!(uriResource instanceof UriResourceEntitySet)) {
			throw new ODataApplicationException("Only EntitySet is supported",
					HttpStatusCode.NOT_IMPLEMENTED.getStatusCode(), Locale.ENGLISH);
		}

		UriResourceEntitySet uriResourceEntitySet = (UriResourceEntitySet) uriResource;
		EdmEntitySet startEdmEntitySet = uriResourceEntitySet.getEntitySet();

		// Analyze the URI segments
		if (segmentCount == 1) { // no navigation
			responseEdmEntityType = startEdmEntitySet.getEntityType();
			responseEdmEntitySet = startEdmEntitySet; // since we have only one
														// segment

			// 2. step: retrieve the data from backend
			List<UriParameter> keyPredicates = uriResourceEntitySet.getKeyPredicates();
			responseEntity = storage.readEntityData(startEdmEntitySet, keyPredicates);
		} else if (segmentCount == 2) { // navigation
			UriResource navSegment = resourceParts.get(1); // in our example we
															// don't support
															// more complex URIs
			if (navSegment instanceof UriResourceNavigation) {
				UriResourceNavigation uriResourceNavigation = (UriResourceNavigation) navSegment;
				EdmNavigationProperty edmNavigationProperty = uriResourceNavigation.getProperty();
				responseEdmEntityType = edmNavigationProperty.getType();
				// contextURL displays the last segment
				responseEdmEntitySet = Util.getNavigationTargetEntitySet(startEdmEntitySet, edmNavigationProperty);

				// 2nd: fetch the data from backend.
				// e.g. for the URI: Products(1)/Category we have to find the
				// correct Category entity
				List<UriParameter> keyPredicates = uriResourceEntitySet.getKeyPredicates();
				// e.g. for Products(1)/Category we have to find first the
				// Products(1)
				Entity sourceEntity = storage.readEntityData(startEdmEntitySet, keyPredicates);

				// now we have to check if the navigation is
				// a) to-one: e.g. Products(1)/Category
				// b) to-many with key: e.g. Categories(3)/Products(5)
				// the key for nav is used in this case:
				// Categories(3)/Products(5)
				List<UriParameter> navKeyPredicates = uriResourceNavigation.getKeyPredicates();

				if (navKeyPredicates.isEmpty()) { // e.g.
													// DemoService.svc/Products(1)/Category
					responseEntity = storage.getRelatedEntity(sourceEntity, responseEdmEntityType);
				} else { // e.g. DemoService.svc/Categories(3)/Products(5)
					responseEntity = storage.getRelatedEntity(sourceEntity, responseEdmEntityType, navKeyPredicates);
				}
			}
		} else {
			// this would be the case for e.g.
			// Products(1)/Category/Products(1)/Category
			throw new ODataApplicationException("Not supported", HttpStatusCode.NOT_IMPLEMENTED.getStatusCode(),
					Locale.ROOT);
		}

		if (responseEntity == null) {
			// this is the case for e.g. DemoService.svc/Categories(4) or
			// DemoService.svc/Categories(3)/Products(999)
			throw new ODataApplicationException("Nothing found.", HttpStatusCode.NOT_FOUND.getStatusCode(),
					Locale.ROOT);
		}

		// 3. serialize
		ContextURL contextUrl = ContextURL.with().entitySet(responseEdmEntitySet).suffix(Suffix.ENTITY).build();
		final String id = request.getRawBaseUri() + "/" + responseEdmEntitySet.getName();
		System.out.println("http.url : " + id);
		bean.setRequest_Uri(request.getRawRequestUri());
		EntitySerializerOptions opts = EntitySerializerOptions.with().contextURL(contextUrl).build();

		ODataSerializer serializer = this.odata.createSerializer(responseFormat);
		SerializerResult serializerResult = serializer.entity(this.srvMetadata, responseEdmEntityType, responseEntity,
				opts);

		// 4. configure the response object
		Date nowTime = new Date();
		long diffs = nowTime.getTime() - relativeTime.getTime();
		long dateandTimes = diffs / 1000;
		bean.setTime_Diffirence(dateandTimes);
		bean.setResponse_Code(HttpStatusCode.OK.getStatusCode());

		// Tracing inserertion Code
		ODataServiceTrace.tracingMethod(bean);

		response.setContent(serializerResult.getContent());
		response.setStatusCode(HttpStatusCode.OK.getStatusCode());
		response.setHeader(HttpHeader.CONTENT_TYPE, responseFormat.toContentTypeString());
	}

	/*
	 * These processor methods are not handled in this tutorial
	 */

	public void createEntity(ODataRequest request, ODataResponse response, UriInfo uriInfo, ContentType requestFormat,
			ContentType responseFormat) throws ODataApplicationException, DeserializerException, SerializerException {
		Date relativeTime = new Date();
		bean.setRequest_Method(request.getMethod() + "");
		bean.setOperation_name("CREAT_ENTIRY");
		bean.setClass_name("DemoEntityProcessor");

		// 1. Retrieve the entity type from the URI
		EdmEntitySet edmEntitySet = Util.getEdmEntitySet(uriInfo);
		EdmEntityType edmEntityType = edmEntitySet.getEntityType();
		// 2. create the data in backend
		// 2.1. retrieve the payload from the POST request for the entity to
		// create and deserialize it
		InputStream requestInputStream = request.getBody();
		ODataDeserializer deserializer = this.odata.createDeserializer(requestFormat);
		DeserializerResult result = deserializer.entity(requestInputStream, edmEntityType);
		Entity requestEntity = result.getEntity();
		// 2.2 do the creation in backend, which returns the newly created
		// entity
		// System.out.println("Content-Type:" + requestFormat + "\n" +
		// requestEntity);
		bean.setContent_type_body("Content-Type:" + requestFormat + "\n" + requestEntity);

		Entity createdEntity = storage.createEntityData(edmEntitySet, requestEntity);

		// 3. serialize the response (we have to return the created entity)
		ContextURL contextUrl = ContextURL.with().entitySet(edmEntitySet).build();
		final String id = request.getRawBaseUri() + "/" + edmEntitySet.getName();
		System.out.println("http.url : " + id);
		bean.setRequest_Uri(request.getRawRequestUri());
		// expand and select currently not supported
		EntitySerializerOptions options = EntitySerializerOptions.with().contextURL(contextUrl).build();

		ODataSerializer serializer = this.odata.createSerializer(responseFormat);
		SerializerResult serializedResponse = serializer.entity(srvMetadata, edmEntityType, createdEntity, options);

		// 4. configure the response object
		Date nowTime = new Date();
		long diffs = nowTime.getTime() - relativeTime.getTime();
		long dateandTimes = diffs / 1000;
		bean.setTime_Diffirence(dateandTimes);
		bean.setResponse_Code(HttpStatusCode.CREATED.getStatusCode());

		// Tracing inserertion Code
		ODataServiceTrace.tracingMethod(bean);

		response.setContent(serializedResponse.getContent());
		response.setStatusCode(HttpStatusCode.CREATED.getStatusCode());
		response.setHeader(HttpHeader.CONTENT_TYPE, responseFormat.toContentTypeString());
	}

	public void updateEntity(ODataRequest request, ODataResponse response, UriInfo uriInfo, ContentType requestFormat,
			ContentType responseFormat) throws ODataApplicationException, DeserializerException, SerializerException {
		Date relativeTime = new Date();
		bean.setRequest_Method(request.getMethod() + "");
		bean.setOperation_name("UPDATE_ENTIRY");
		bean.setClass_name("DemoEntityProcessor");

		// 1. Retrieve the entity set which belongs to the requested entity
		List<UriResource> resourcePaths = uriInfo.getUriResourceParts();
		// Note: only in our example we can assume that the first segment is the
		// EntitySet
		UriResourceEntitySet uriResourceEntitySet = (UriResourceEntitySet) resourcePaths.get(0);
		EdmEntitySet edmEntitySet = uriResourceEntitySet.getEntitySet();
		EdmEntityType edmEntityType = edmEntitySet.getEntityType();
		final String id = request.getRawBaseUri() + "/" + edmEntitySet.getName();
		System.out.println("http.url : " + id);
		bean.setRequest_Uri(request.getRawRequestUri());

		// 2. update the data in backend
		// 2.1. retrieve the payload from the PUT request for the entity to be
		// updated
		InputStream requestInputStream = request.getBody();
		ODataDeserializer deserializer = this.odata.createDeserializer(requestFormat);
		DeserializerResult result = deserializer.entity(requestInputStream, edmEntityType);
		Entity requestEntity = result.getEntity();
		bean.setContent_type_body("Content-Type:" + requestFormat + "\n" + requestEntity);
		System.out.println("Content-Type:" + requestFormat + "\n" + requestEntity);
		// 2.2 do the modification in backend
		List<UriParameter> keyPredicates = uriResourceEntitySet.getKeyPredicates();
		// Note that this updateEntity()-method is invoked for both PUT or PATCH
		// operations
		HttpMethod httpMethod = request.getMethod();
		storage.updateEntityData(edmEntitySet, keyPredicates, requestEntity, httpMethod);

		// 3. configure the response object
		Date nowTime = new Date();
		long diffs = nowTime.getTime() - relativeTime.getTime();
		long dateandTimes = diffs / 1000;
		bean.setTime_Diffirence(dateandTimes);
		bean.setResponse_Code(HttpStatusCode.NO_CONTENT.getStatusCode());

		// Tracing inserertion Code
		ODataServiceTrace.tracingMethod(bean);
		response.setStatusCode(HttpStatusCode.NO_CONTENT.getStatusCode());
	}

	public void deleteEntity(ODataRequest request, ODataResponse response, UriInfo uriInfo)
			throws ODataApplicationException {
		Date relativeTime = new Date();
		bean.setRequest_Method(request.getMethod() + "");
		bean.setOperation_name("DELETE_ENTIRY");
		bean.setClass_name("DemoEntityProcessor");

		// 1. Retrieve the entity set which belongs to the requested entity
		List<UriResource> resourcePaths = uriInfo.getUriResourceParts();
		// Note: only in our example we can assume that the first segment is the
		// EntitySet
		UriResourceEntitySet uriResourceEntitySet = (UriResourceEntitySet) resourcePaths.get(0);
		EdmEntitySet edmEntitySet = uriResourceEntitySet.getEntitySet();
		bean.setRequest_Uri(request.getRawRequestUri());

		// 2. delete the data in backend
		List<UriParameter> keyPredicates = uriResourceEntitySet.getKeyPredicates();
		storage.deleteEntityData(edmEntitySet, keyPredicates);

		// 3. configure the response object
		Date nowTime = new Date();
		long diffs = nowTime.getTime() - relativeTime.getTime();
		long dateandTimes = diffs / 1000;
		bean.setTime_Diffirence(dateandTimes);
		bean.setResponse_Code(HttpStatusCode.NO_CONTENT.getStatusCode());
		// Tracing inserertion Code
		ODataServiceTrace.tracingMethod(bean);

		response.setStatusCode(HttpStatusCode.NO_CONTENT.getStatusCode());
	}
}
