package myservice.mynamespace.data;

import java.io.ByteArrayInputStream;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import org.apache.olingo.commons.api.data.Entity;
import org.apache.olingo.commons.api.data.EntityCollection;
import org.apache.olingo.commons.api.data.Property;
import org.apache.olingo.commons.api.data.ValueType;
import org.apache.olingo.commons.api.ex.ODataRuntimeException;
import org.apache.olingo.commons.api.format.ContentType;
import org.apache.olingo.commons.api.http.HttpStatusCode;
import org.apache.olingo.server.api.ODataApplicationException;
import org.apache.olingo.server.api.ODataResponse;
import org.apache.olingo.server.api.uri.UriInfo;
import org.apache.olingo.server.api.uri.queryoption.FilterOption;
import org.apache.olingo.server.api.uri.queryoption.expression.Expression;
import org.apache.olingo.server.api.uri.queryoption.expression.ExpressionVisitException;

import com.sap.conn.jco.JCoDestination;
import com.sap.conn.jco.JCoException;
import com.sap.conn.jco.JCoFunction;
import com.sap.conn.jco.JCoTable;

import myservice.mynamespace.jco.TestJco;
import myservice.mynamespace.service.FilterExpressionVisitor;

public class BAPIFunction {

	public static List<Entity> getFiltterImportExportBAPI(UriInfo uriInfo, List<Entity> entityList,
			EntityCollection returnEntityCollection, List<Entity> etdynamicData, ODataResponse response,
			ContentType responseFormat) {
		FilterOption filterOption = uriInfo.getFilterOption();
		if (filterOption != null) {
			// Apply $filter system query option
			try {
				// entityList = entityCollection.getEntities();
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
					Object visitorResult = null;
					try {
						visitorResult = filterExpression.accept(expressionVisitor);
					} catch (ODataApplicationException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}

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
						try {
							throw new ODataApplicationException("A filter expression must evaulate to type Edm.Boolean",
									HttpStatusCode.BAD_REQUEST.getStatusCode(), Locale.ENGLISH);
						} catch (ODataApplicationException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				}

			} catch (ExpressionVisitException e) {
				try {
					throw new ODataApplicationException("Exception in filter evaluation",
							HttpStatusCode.INTERNAL_SERVER_ERROR.getStatusCode(), Locale.ENGLISH);
				} catch (ODataApplicationException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
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
			return etdynamicData;
		}
		return etdynamicData;

	}

	private static URI createId(String entitySetName, Entity entity, Object id) {
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