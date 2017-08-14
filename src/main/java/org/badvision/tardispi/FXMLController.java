package org.badvision.tardispi;

import javafx.fxml.FXML;
import javafx.scene.effect.Glow;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;

public class FXMLController {
    
    @FXML
    private AnchorPane pane;
    
    public void initialize() throws Exception {
         GalifreyanGlyph word1 = new GalifreyanGlyph("AEIOUS", 50, Color.BLUE, Color.WHITE);
         word1.setTranslateX(200);
         word1.setTranslateY(200);
         word1.setEffect(new Glow(0.4));
         pane.getChildren().add(word1);
         
         GalifreyanGlyph word2 = new GalifreyanGlyph("ZYGGY", 75, Color.RED, Color.YELLOW);
         word2.setTranslateX(350);
         word2.setTranslateY(350);
         word2.setEffect(new Glow(0.6));
         pane.getChildren().add(word2);
    }
}
