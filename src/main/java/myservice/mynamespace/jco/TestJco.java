package myservice.mynamespace.jco;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Properties;

import com.sap.conn.jco.AbapException;
import com.sap.conn.jco.JCoDestination;
import com.sap.conn.jco.JCoDestinationManager;
import com.sap.conn.jco.JCoException;
import com.sap.conn.jco.JCoField;
import com.sap.conn.jco.JCoFunction;
import com.sap.conn.jco.JCoParameterList;
import com.sap.conn.jco.ext.DestinationDataProvider;

public class TestJco {
	public static void main(String[] args) {
		String DESTINATION_NAME1 = "mySAPSystem";
		Properties connectProperties = new Properties();
		connectProperties.setProperty(DestinationDataProvider.JCO_ASHOST, "10.0.0.30");
		connectProperties.setProperty(DestinationDataProvider.JCO_SYSNR, "18");
		connectProperties.setProperty(DestinationDataProvider.JCO_CLIENT, "800");
		connectProperties.setProperty(DestinationDataProvider.JCO_USER, "race1");
		connectProperties.setProperty(DestinationDataProvider.JCO_PASSWD, "innovation1");
		connectProperties.setProperty(DestinationDataProvider.JCO_LANG, "en");
		createDestinationDataFile(DESTINATION_NAME1, connectProperties);

		try {	
			JCoDestination destination = JCoDestinationManager.getDestination("mySAPSystem");
			System.out.println("Attributes:");
			System.out.println(destination.getAttributes());
			System.out.println();
			destination.ping();
		} catch (JCoException e) {
			e.printStackTrace();
		}
		try {
			JCoDestination destination = JCoDestinationManager.getDestination(DESTINATION_NAME1);
			JCoFunction function = destination.getRepository().getFunction("ZBAPI_EMP_LIST_001");
			if (function == null)
				throw new RuntimeException("Not found in SAP.");
			function.getImportParameterList().setValue("IV_ZENO", "");
			
			// Read Single Record in Table
			//function.getImportParameterList().setValue("IV_ZENO", "0001");

			try {
				function.execute(destination);
			} catch (AbapException e) {
				System.out.println(e.toString());
				return;
			}
			System.out.println("STFC_CONNECTION finished:");
			System.out.println(" Echo: " + function.getExportParameterList().getString("ET_EMP_LIST"));
			//System.out.println(" Response: " + function.getExportParameterList().getString("ET_EMP_LIST"));
			
			// Read Enter Table DATA
			JCoParameterList exportParameterList = function.getExportParameterList();
			for (JCoField jCoField : exportParameterList) {	
				System.out.println(" Response: " +jCoField.getValue());
				
			}
			
			
			/*JCoParameterList exportParams = function.getExportParameterList();	
			String  returnCode  = exportParams.getString("ZENAME");
			//String returnCode= function.getExportParameterList().getString("Field_Name");
			System.out.println(returnCode);*/
			
			
/*			JCoTable resultTable = function.getTableParameterList().getTable("ZEMP_TABLE1_T");
			for (int i = 0; i < resultTable.getNumRows(); i++){
				 resultTable.setRow( i );
				}
			String result = resultTable.getString("Field_Name");
			System.out.println(result);*/

			 
	
			//String string = function.getExportParameterList().getListMetaData().getDefault("ET_EMP_LIST");
			System.out.println();
		} catch (JCoException e) {
			System.out.println(e.toString());
			return;
		}
	}

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
}
