/*
 * Copyright (C) 2015 Daniel Vimont
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.commonvox.le_browser;

import javafx.geometry.Point2D;
import javafx.scene.control.Control;
import javafx.scene.control.Tooltip;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;

/**
 *
 * @author Daniel
 */
public class Utilities {
    
    /** This method provided in http://stackoverflow.com/questions/17405688
     * @param owner
     * @param control
     * @param tooltipText
     * @param tooltipGraphic */
    public static void showTooltip(Stage owner, Control control, 
                                    String tooltipText, ImageView tooltipGraphic)
    {
        try {
            Point2D p = control.localToScene(0.0, 0.0);

            final Tooltip customTooltip = new Tooltip();
            customTooltip.setText(tooltipText);
            customTooltip.setStyle
            ("-fx-background-color: white; -fx-text-fill: red; -fx-font-weight: bold");

            control.setTooltip(customTooltip);
            customTooltip.setAutoHide(true);

            customTooltip.show(owner, p.getX()
                + control.getScene().getX() + control.getScene().getWindow().getX(), p.getY()
                + control.getScene().getY() + control.getScene().getWindow().getY());

        } 
        /** v1.3.3: catch null pointer exception -- can occur if Detail window 
         * closed before tooltip displayed. */
        catch (NullPointerException npe) { }
    }
}
