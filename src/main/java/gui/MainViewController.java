package gui;

import database.DatabaseConnection;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.ListView;
import model.asset.AssetSource;
import model.asset.AssetSourceCredentials;

import javax.security.auth.login.CredentialException;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.stream.Collectors;

public class MainViewController {

    public MainViewController(String encryptionKey) throws CredentialException, IOException {
        this.databaseConnection = new DatabaseConnection(encryptionKey);
    }

    private DatabaseConnection databaseConnection;
    private List<AssetSource> assetSourcesList;

    @FXML
    private ListView<String> assetSourceListView;

    @FXML
    public void initialize() {
        try {
            List<AssetSourceCredentials> credentials = databaseConnection.getAssetSourceCredentials();
            AssetSource offlineAccounts = databaseConnection.getOfflineAccountAssetSource();

            this.assetSourcesList = MoneyManager.retrieveAssets(credentials, offlineAccounts);
            System.out.println();

        } catch (SQLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        ObservableList<String> items = FXCollections.observableList(assetSourcesList.stream().map(assetSource -> assetSource.getName()).collect(Collectors.toList()));
        assetSourceListView.setItems(items);

    }

}
