package myservice.mynamespace.jco;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Properties;

import com.sap.conn.jco.AbapException;
import com.sap.conn.jco.JCoDestination;
import com.sap.conn.jco.JCoDestinationManager;
import com.sap.conn.jco.JCoException;
import com.sap.conn.jco.JCoFunction;
import com.sap.conn.jco.ext.DestinationDataProvider;

public class TestJco {
	public static JCoDestination callZempTable() {
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
			destination.ping();
		} catch (JCoException e) {
			e.printStackTrace();
		}
		JCoDestination destination = null;
		try {
			destination = JCoDestinationManager.getDestination(DESTINATION_NAME1);
			JCoFunction function = destination.getRepository().getFunction("ZBAPI_EMP_LIST_001");
			if (function == null)
				throw new RuntimeException("Not found in SAP.");
			function.getImportParameterList().setValue("IV_ZENO", "");
			try {
				function.execute(destination);
			} catch (AbapException e) {
				System.out.println(e.toString());
				return destination;
			}

		} catch (JCoException e) {
			System.out.println(e.toString());
			return destination;
		}
		return destination;
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
