/**
 * This program and the accompanying materials
 * are made available under the terms of the License
 * which accompanies this distribution in the file LICENSE.txt
 */
package org.archicontribs.specialization.uiProvider.elements.figures;

import org.archicontribs.specialization.SpecializationPlugin;
import org.eclipse.draw2d.Label;
import org.eclipse.draw2d.text.TextFlow;
import com.archimatetool.model.IDiagramModelArchimateObject;

/**
 * Product Figure
 * 
 * @author Hervé Jouin
 */
public class ProductFigure extends com.archimatetool.editor.diagram.figures.elements.ProductFigure {
    @Override
    protected void setText() {
        String labelName = null;
        
        if ( SpecializationPlugin.mustShowLabel(getDiagramModelObject()) )
            labelName = SpecializationPlugin.getLabelName(((IDiagramModelArchimateObject)getDiagramModelObject()).getArchimateElement());
        
        if ( labelName==null )
            super.setText();
        else {
            if(getTextControl() instanceof TextFlow) {
                ((TextFlow)getTextControl()).setText(labelName);
            }
            else if(getTextControl() instanceof Label) {
                ((Label)getTextControl()).setText(labelName);
            }
        }
    }
}