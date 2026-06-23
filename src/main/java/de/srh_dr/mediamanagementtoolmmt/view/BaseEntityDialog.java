package de.srh_dr.mediamanagementtoolmmt.view;

import de.srh_dr.mediamanagementtoolmmt.util.LanguageManager;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.layout.GridPane;

public abstract class BaseEntityDialog<T> extends Dialog<T> {
    protected abstract void hydrateGridPane(GridPane gridPane);
    protected abstract T createEntity();
    protected abstract boolean checkDuplicate(T newEntity);

    public BaseEntityDialog(String titleKey, String headerKey) {
        this.setTitle(LanguageManager.getString(titleKey));
        this.setHeaderText(LanguageManager.getString(headerKey));

        ButtonType saveButtonType = new ButtonType(LanguageManager.getString("ui.submit"), ButtonBar.ButtonData.OK_DONE);
        this.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        GridPane gridPane = new GridPane();
        gridPane.setHgap(10);
        gridPane.setVgap(10);

        hydrateGridPane(gridPane);
        this.getDialogPane().setContent(gridPane);

        this.setResultConverter(dialogButton -> {
            if(dialogButton == saveButtonType){
                T newEntity = createEntity();

                if(newEntity != null){
                    if(checkDuplicate(newEntity)){
                        return null;
                    }
                    return newEntity;
                }
            }
            return null;
        });
    }
}
