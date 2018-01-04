package gui;

import model.ApiException;
import model.asset.AssetSource;
import model.asset.AssetSourceCredentials;
import model.asset.account.BasicAccount;
import model.asset.account.OfflineAssetSource;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import static java.util.Arrays.asList;
import static util.NumberHelper.roundValue;
import static util.PersistenceHelper.*;

public class MoneyManager {

    public static void main(String[] args) throws Exception {
        String encryptionKey = getEncryptionKeyFromConsoleInput();
        List<AssetSource> assetSources = loadObjects(encryptionKey, AssetSource.class);

        MoneyManager moneyManager = new MoneyManager();

        // TODO: Improve handling and initialization of online and offline accounts
        if (assetSources == null || assetSources.isEmpty()) {
            List<AssetSourceCredentials> credentials = loadObjects(encryptionKey, AssetSourceCredentials.class);
            assetSources = moneyManager.retrieveAssets(credentials, null);

            if (!assetSources.isEmpty()) {
                AssetSource offlineAssetSource = new OfflineAssetSource(new BasicAccount("Tesla Model 3 Reservation", new BigDecimal(1000.0)));
                offlineAssetSource.updateAssets(null);
                assetSources.add(offlineAssetSource);
                printAssets(assetSources);
                assetSources = assetSources.stream().map(assetSource -> saveObject(encryptionKey, assetSource)).collect(Collectors.toList());
            }
        }

        System.out.println();

        printAssets(assetSources);

        System.out.println();
        System.out.println("New:");

        for (int i = 0; i < assetSources.size(); i++) {
            AssetSource assetSource = assetSources.get(i);
            boolean done = false;
            while (!done) {
                System.out.println("Update assets from: " + assetSource.getName());
                try {
                    assetSource.updateAssets(loadAssetSourceCredentials(encryptionKey, assetSource.getCredentialsId()));
                    done = true;
                } catch (ApiException e) {
                    e.printStackTrace();
                }
            }
        }
        System.out.println();

        assetSources = assetSources.stream().map(assetSource -> saveObject(encryptionKey, assetSource)).collect(Collectors.toList());

        printAssets(assetSources);
    }

    private static void printAssets(List<AssetSource> assetSources) {
        // TODO: Remove
        List<String> ignoredAccountNames = asList("BonusSparen 4900776955", "KlassikSparen 3901133380");
        for (AssetSource assetSource : assetSources) {
            System.out.println(assetSource.toString(ignoredAccountNames));
        }

        BigDecimal totalAmount = roundValue(assetSources.stream().map(assetSource -> assetSource.getCurrentEurBalance(ignoredAccountNames).getEuroBalanceValue()).reduce(BigDecimal.ZERO, BigDecimal::add));
        System.out.println("--------------------");
        //System.out.println("Old Total: " + oldTotalAmount + " €");
        System.out.println("Total: " + totalAmount + " €");
        //System.out.println("\t\t\t" + (delta.doubleValue() > 0.0 ? "+" : "") + delta + " €");
    }

    public static String getEncryptionKeyFromConsoleInput() throws IOException {
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

    public static List<AssetSource> retrieveAssets(List<AssetSourceCredentials> credentialsList, AssetSource offlineAccounts) {
        List<AssetSource> assetSourceList = new LinkedList<>();

        for (int i = 0; i < credentialsList.size(); i++) {
            AssetSourceCredentials credentials = credentialsList.get(i);
            boolean done = false;
            while (!done) {
                AssetSource assetSource = new AssetSource(credentials);
                System.out.println("Retrieving assets from: " + assetSource.getName());
                try {
                    assetSource.updateAssets(credentialsList.get(i));
                    assetSourceList.add(assetSource);
                    done = true;
                } catch (ApiException e) {
                    e.printStackTrace();
                }
            }
        }
        if (offlineAccounts != null) {
            try {
                offlineAccounts.updateAssets(null);
            } catch (ApiException e) {
                e.printStackTrace();
            }
            assetSourceList.add(offlineAccounts);
        }

        return assetSourceList;
    }

}
