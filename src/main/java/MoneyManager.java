import asset_checker.*;
import model.ApiException;
import model.Asset;
import model.AssetChecker;
import model.AssetList;
import util.EncryptedProperties;

import java.io.*;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

public class MoneyManager {

    public static void main(String[] args) throws Exception {
        // Load existing properties
        Properties properties = new EncryptedProperties(getEncryptionKey());
        File file = new File("properties.encrypted");
        InputStream inputStream = new FileInputStream(file);
        properties.load(inputStream);


        // Encrypt new properties
        /*
        Properties properties = new util.EncryptedProperties(getEncryptionKey());
        Properties plainProperties = new Properties();
        InputStream inputStream = new FileInputStream("config.properties");
        plainProperties.load(inputStream);

        plainProperties.stringPropertyNames().forEach(propertyName -> {
            properties.put(propertyName, plainProperties.getProperty(propertyName));
        });
        */

        // Add additional properties
        // properties.put("key", "value");

        // Write back properties
        // properties.store(new FileOutputStream("properties.encrypted"), null);

        MoneyManager moneyManager = new MoneyManager();
        moneyManager.retrieveAssets(properties);
    }

    public static String getEncryptionKey() throws IOException {
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(System.in));
        String encryptionKey = null;
        while (encryptionKey == null || encryptionKey.isEmpty()) {
            System.out.print("Enter Encryption Key: ");
            encryptionKey = bufferedReader.readLine();
        }
        System.out.println("Encryption Key Entered.");
        System.out.println();
        return encryptionKey;
    }

    private void retrieveAssets(Properties properties) throws IOException {
        List<AssetChecker> assetChecker = new LinkedList<>();

        assetChecker.add(new SparkasseHannoverAssetChecker(properties.getProperty("sparkasseHannover.username"), properties.getProperty("sparkasseHannover.password")));
        assetChecker.add(new INGDiBaAssetChecker(properties.getProperty("ingDiBa.username"), properties.getProperty("ingDiBa.password")));
        assetChecker.add(new AuxmoneyAssetChecker(properties.getProperty("auxmoney.username"), properties.getProperty("auxmoney.password")));
        assetChecker.add(new EquatePlusAssetChecker(properties.getProperty("equate.username"), properties.getProperty("equate.password")));
        assetChecker.add(new KrakenAssetChecker(properties.getProperty("kraken.apikey"), properties.getProperty("kraken.apisecret")));
        assetChecker.add(new PayPalAssetChecker(properties.getProperty("payPal.apiUser"), properties.getProperty("payPal.apiKey"), properties.getProperty("payPal.apiSignature")));
        assetChecker.add(new AmazonVisaAssetChecker(properties.getProperty("amazonVisa.username"), properties.getProperty("amazonVisa.password")));
        assetChecker.add(new OfflineAssetChecker("Tesla", new Asset(1000.0, "Model 3 Reservation")));

        List<AssetList> assetLists = new LinkedList<>();

        for (AssetChecker checker : assetChecker) {
            System.out.println("Retrieving assets from: " + checker.getName());
            AssetList assetList = null;
            try {
                assetList = checker.retrieveAssets();
                assetLists.add(assetList);
            } catch (ApiException e) {
                e.printStackTrace();
            }
        }

        System.out.println();

        for (AssetList assetList : assetLists) {
            System.out.println(assetList);
        }

        Double totalAmount = assetLists.stream().mapToDouble(AssetList::getTotalEurValue).sum();
        System.out.println("--------------------");
        System.out.println("Total: " + totalAmount + " €");
    }

}
