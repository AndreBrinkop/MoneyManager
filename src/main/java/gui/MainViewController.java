package gui;

import database.DatabaseConnection;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import model.asset.AssetObject;
import model.asset.AssetSource;
import model.asset.AssetSourceCredentials;
import model.asset.account.Account;

import javax.security.auth.login.CredentialException;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

import static util.NumberHelper.roundValue;

public class MainViewController {

    public MainViewController(String encryptionKey) throws CredentialException, IOException {
        this.databaseConnection = new DatabaseConnection(encryptionKey);
    }

    private DatabaseConnection databaseConnection;
    private ObservableList<AssetSource> assetSourcesList;

    @FXML
    private TreeView<AssetObject> assetSourceTreeView;

    @FXML
    public void initialize() {
        try {
            List<AssetSourceCredentials> credentials = databaseConnection.getAssetSourceCredentials();
            AssetSource offlineAccounts = databaseConnection.getOfflineAccountAssetSource();
            this.assetSourcesList = FXCollections.observableList(MoneyManager.retrieveAssets(credentials, offlineAccounts));
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        TreeItem<AssetObject> rootNode = new TreeItem<>(null);
        for (AssetSource assetSource : assetSourcesList) {
            TreeItem<AssetObject> groupNode = new TreeItem<>(assetSource);
            for (Account account : assetSource.getAccounts()) {
                groupNode.getChildren().add(new TreeItem<>(account));
            }
            rootNode.getChildren().add(groupNode);
        }

        assetSourceTreeView.setRoot(rootNode);
        assetSourceTreeView.setShowRoot(false);
        assetSourceTreeView.setCellFactory(p -> new BalanceCell());

    }

    static class BalanceCell extends TreeCell<AssetObject> {
        @Override
        public void updateItem(AssetObject item, boolean empty) {
            super.updateItem(item, empty);

            if (item != null) {
                BorderPane pane = new BorderPane();
                Text assetObjectNameText = new Text(item.getName());
                Text assetObjectEuroBalanceText = new Text(roundValue(item.getCurrentEurBalance().getEuroBalanceValue()).stripTrailingZeros().toPlainString() + " €");

                if (item instanceof AssetSource) {
                    assetObjectNameText.setFill(Color.BLACK);
                    assetObjectEuroBalanceText.setFill(Color.BLACK);
                } else {
                    assetObjectNameText.setFill(Color.GRAY);
                    assetObjectEuroBalanceText.setFill(Color.GRAY);
                }

                pane.setLeft(assetObjectNameText);
                pane.setRight(assetObjectEuroBalanceText);
                setGraphic(pane);
            } else {
                setGraphic(null);
            }
        }
    }

}
