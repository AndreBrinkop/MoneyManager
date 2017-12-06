import asset_checker.*;
import model.ApiException;
import model.AssetChecker;
import model.asset.Account;
import model.asset.AssetSource;
import util.EncryptedProperties;

import java.io.*;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

import static util.NumberHelper.roundValue;

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
        Double oldTotalAmount = Double.valueOf(properties.getProperty("totalAmount", "0.0d"));
        Double totalAmount = moneyManager.retrieveAssets(properties);
        Double delta = roundValue(totalAmount - oldTotalAmount);

        System.out.println("--------------------");
        System.out.println("Old Total: " + oldTotalAmount + " €");
        System.out.println("New Total: " + totalAmount + " €");
        System.out.println("\t\t\t" + (delta > 0.0 ? "+" : "") + delta + " €");

        properties.put("totalAmount", totalAmount.toString());
        properties.store(new FileOutputStream("properties.encrypted"), null);
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

    private Double retrieveAssets(Properties properties) throws IOException {
        List<AssetChecker> assetChecker = new LinkedList<>();

        assetChecker.add(new SparkasseHannoverAssetChecker(properties.getProperty("sparkasseHannover.username"), properties.getProperty("sparkasseHannover.password")));
        assetChecker.add(new INGDiBaAssetChecker(properties.getProperty("ingDiBa.username"), properties.getProperty("ingDiBa.password")));
        assetChecker.add(new AuxmoneyAssetChecker(properties.getProperty("auxmoney.username"), properties.getProperty("auxmoney.password")));
        assetChecker.add(new EquatePlusAssetChecker(properties.getProperty("equate.username"), properties.getProperty("equate.password")));
        assetChecker.add(new BitcoinDeAssetChecker(properties.getProperty("bitcoinDe.username"), properties.getProperty("bitcoinDe.password")));
        assetChecker.add(new KrakenAssetChecker(properties.getProperty("kraken.apikey"), properties.getProperty("kraken.apisecret")));
        assetChecker.add(new PayPalAssetChecker(properties.getProperty("payPal.apiUser"), properties.getProperty("payPal.apiKey"), properties.getProperty("payPal.apiSignature")));
        assetChecker.add(new AmazonVisaAssetChecker(properties.getProperty("amazonVisa.username"), properties.getProperty("amazonVisa.password")));
        assetChecker.add(new CoinbaseAssetChecker(properties.getProperty("coinbase.apikey"), properties.getProperty("coinbase.apisecret")));
        assetChecker.add(new OfflineAssetChecker("Tesla Model 3 Reservation", 1000.0));

        List<AssetSource> assetSourceList = new LinkedList<>();

        for (AssetChecker checker : assetChecker) {
            System.out.println("Retrieving assets from: " + checker.getName());
            try {
                assetSourceList.add(checker.retrieveAssets());
            } catch (ApiException e) {
                e.printStackTrace();
            }
        }

        System.out.println();

        filterOutAssetSources(assetSourceList, "KlassikSparen");
        filterOutAssetSources(assetSourceList, "BonusSparen");

        for (AssetSource assetSource : assetSourceList) {
            System.out.println(assetSource);
        }

        Double totalAmount = roundValue(assetSourceList.stream().mapToDouble(AssetSource::getTotalEurValue).sum());
        return totalAmount;
    }

    private void filterOutAssetSources(List<AssetSource> assetSourceList, String accountNamePart) {
        for (AssetSource assetSource : assetSourceList) {
            if (assetSource == null || assetSource.getAccounts() == null) {
                continue;
            }
            for (Account account : assetSource.getAccounts()) {
                if (account.getName().contains(accountNamePart)) {
                    assetSource.removeAccount(account);
                }
            }
        }
    }

}
