package org.archicontribs.specialization.types;

import java.io.File;
import java.lang.reflect.Field;

import org.apache.log4j.Level;
import org.archicontribs.specialization.SpecializationLogger;
import org.archicontribs.specialization.SpecializationPlugin;
import org.archicontribs.specialization.propertysections.OldImageManagerDialog;
import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.gef.GraphicalEditPart;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTException;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;

import com.archimatetool.canvas.model.ICanvasFactory;
import com.archimatetool.canvas.model.ICanvasModel;
import com.archimatetool.editor.ArchiPlugin;
import com.archimatetool.editor.diagram.editparts.ArchimateElementEditPart;
import com.archimatetool.editor.diagram.figures.IDiagramModelObjectFigure;
import com.archimatetool.editor.diagram.figures.RectangleFigureDelegate;
import com.archimatetool.editor.diagram.util.DiagramUtils;
import com.archimatetool.editor.model.IArchiveManager;
import com.archimatetool.editor.ui.ColorFactory;
import com.archimatetool.editor.ui.factory.IArchimateElementUIProvider;
import com.archimatetool.editor.ui.factory.IGraphicalObjectUIProvider;
import com.archimatetool.editor.ui.factory.ObjectUIFactory;
import com.archimatetool.model.FolderType;
import com.archimatetool.model.IArchimateElement;
import com.archimatetool.model.IArchimateFactory;
import com.archimatetool.model.IArchimateModel;
import com.archimatetool.model.IDiagramModel;
import com.archimatetool.model.IDiagramModelArchimateObject;
import com.archimatetool.model.IDiagramModelImage;
import com.archimatetool.model.IDiagramModelObject;
import com.archimatetool.model.util.UUIDFactory;

public class ElementFigure extends Composite {
	static final SpecializationLogger logger = new SpecializationLogger(ElementFigure.class);

	static final int figureMargin = 2;
	static final String canChangeIconString = "canChangeIcon";
	
	static final String ID_PREFIX = "SpecializationPlugin_";
	static final String CANVAS_NAME = "SpecializationPluginCanvas";
	
	Color defaultBackgroundColor = null;
	
	IArchimateModel model = null;

	Label lblIconSize = null;
	Label lblIconLocation = null;
	Button btnNewIcon = null;
	Button btnDeleteIcon = null;
	Text txtIconSize= null;
	Text txtIconLocation = null;
	
	String iconName = "";

	Composite outerCompo1 = null;
	Composite innerCompo1 = null;
	Composite outerCompo2 = null;
	Composite innerCompo2 = null;
	Label figure1 = null;
	Label figure2 = null;
	
	EClass eClass = null;

	Composite selectedFigure = null;
	
	public ElementFigure(Composite parent, int type) {
		super(parent, type);
		this.defaultBackgroundColor = this.getBackground();
		setLayout(new FormLayout());

		// figure 1
		this.outerCompo1 = new Composite(this, SWT.NONE);
		this.outerCompo1.setBackground(this.getBackground());
		FormData fd = new FormData();
		fd.top = new FormAttachment(0, 0);
		fd.left = new FormAttachment(0, 0);
		fd.width = ArchiPlugin.INSTANCE.getPreferenceStore().getInt(com.archimatetool.editor.preferences.IPreferenceConstants.DEFAULT_ARCHIMATE_FIGURE_WIDTH) + 2*figureMargin;
		fd.height = ArchiPlugin.INSTANCE.getPreferenceStore().getInt(com.archimatetool.editor.preferences.IPreferenceConstants.DEFAULT_ARCHIMATE_FIGURE_HEIGHT) + 2*figureMargin;
		this.outerCompo1.setLayoutData(fd);
		this.outerCompo1.setLayout(new FormLayout());

		this.innerCompo1 = new Composite(this.outerCompo1, SWT.NONE);
		this.innerCompo1.setBackground(this.getBackground());
		fd = new FormData();
		fd.top = new FormAttachment(0, figureMargin);
		fd.left = new FormAttachment(0, figureMargin);
		fd.width = ArchiPlugin.INSTANCE.getPreferenceStore().getInt(com.archimatetool.editor.preferences.IPreferenceConstants.DEFAULT_ARCHIMATE_FIGURE_WIDTH);
		fd.height = ArchiPlugin.INSTANCE.getPreferenceStore().getInt(com.archimatetool.editor.preferences.IPreferenceConstants.DEFAULT_ARCHIMATE_FIGURE_HEIGHT);
		this.innerCompo1.setLayoutData(fd);
		this.innerCompo1.setLayout(new FormLayout());

		this.figure1 = new Label(this.innerCompo1, SWT.NULL);
		this.figure1.setLayoutData(new GridData(GridData.FILL_BOTH));
		this.figure1.setData("imageFigure", this);
		this.figure1.addMouseListener(this.selectListener);
		fd = new FormData();
		fd.top = new FormAttachment(0, 0);
		fd.left = new FormAttachment(0, 0);
		fd.width = ArchiPlugin.INSTANCE.getPreferenceStore().getInt(com.archimatetool.editor.preferences.IPreferenceConstants.DEFAULT_ARCHIMATE_FIGURE_WIDTH);
		fd.height = ArchiPlugin.INSTANCE.getPreferenceStore().getInt(com.archimatetool.editor.preferences.IPreferenceConstants.DEFAULT_ARCHIMATE_FIGURE_HEIGHT);
		this.figure1.setLayoutData(fd);
		this.figure1.addDisposeListener(new DisposeListener() {
			@Override public void widgetDisposed(DisposeEvent e) {
				Image oldImage = ((Label)e.widget).getBackgroundImage();
				if ( oldImage != null )
					oldImage.dispose();
			}
		});

		// figure 2
		this.outerCompo2 = new Composite(this, SWT.NONE);
		this.outerCompo2.setBackground(this.getBackground());
		fd = new FormData();
		fd.top = new FormAttachment(0, 0);
		fd.left = new FormAttachment(this.outerCompo1, 5);
		fd.width = ArchiPlugin.INSTANCE.getPreferenceStore().getInt(com.archimatetool.editor.preferences.IPreferenceConstants.DEFAULT_ARCHIMATE_FIGURE_WIDTH) + 2*figureMargin;
		fd.height = ArchiPlugin.INSTANCE.getPreferenceStore().getInt(com.archimatetool.editor.preferences.IPreferenceConstants.DEFAULT_ARCHIMATE_FIGURE_HEIGHT) + 2*figureMargin;
		this.outerCompo2.setLayoutData(fd);
		this.outerCompo2.setLayout(new FormLayout());

		this.innerCompo2 = new Composite(this.outerCompo2, SWT.NONE);
		this.innerCompo2.setBackground(this.getBackground());
		fd = new FormData();
		fd.top = new FormAttachment(0, figureMargin);
		fd.left = new FormAttachment(0, figureMargin);
		fd.width = ArchiPlugin.INSTANCE.getPreferenceStore().getInt(com.archimatetool.editor.preferences.IPreferenceConstants.DEFAULT_ARCHIMATE_FIGURE_WIDTH);
		fd.height = ArchiPlugin.INSTANCE.getPreferenceStore().getInt(com.archimatetool.editor.preferences.IPreferenceConstants.DEFAULT_ARCHIMATE_FIGURE_HEIGHT);
		this.innerCompo2.setLayoutData(fd);
		this.innerCompo2.setLayout(new FormLayout());

		this.figure2 = new Label(this.innerCompo2, SWT.NULL);
		this.figure2.setLayoutData(new GridData(GridData.FILL_BOTH));
		this.figure2.setData("imageFigure", this);
		this.figure2.addMouseListener(this.selectListener);
		fd = new FormData();
		fd.top = new FormAttachment(0, 0);
		fd.left = new FormAttachment(0, 0);
		fd.width = ArchiPlugin.INSTANCE.getPreferenceStore().getInt(com.archimatetool.editor.preferences.IPreferenceConstants.DEFAULT_ARCHIMATE_FIGURE_WIDTH);
		fd.height = ArchiPlugin.INSTANCE.getPreferenceStore().getInt(com.archimatetool.editor.preferences.IPreferenceConstants.DEFAULT_ARCHIMATE_FIGURE_HEIGHT);
		this.figure2.setLayoutData(fd);
		this.figure2.addDisposeListener(new DisposeListener() {
			@Override public void widgetDisposed(DisposeEvent e) {
				Image oldImage = ((Label)e.widget).getBackgroundImage();
				if ( oldImage != null )
					oldImage.dispose();
			}
		});


		// buttons icon
		this.btnNewIcon = new Button(this, SWT.PUSH);
		this.btnNewIcon.setImage(SpecializationPlugin.NEW_ICON);
		this.btnNewIcon.setToolTipText("Set icon");
		this.btnNewIcon.setVisible(false);
		fd = new FormData();
		fd.top = new FormAttachment(this.outerCompo2, 0, SWT.TOP);
		fd.left = new FormAttachment(this.outerCompo2, 5);
		fd.width = 20;
		fd.height = 20;
		this.btnNewIcon.setLayoutData(fd);
		this.btnNewIcon.addListener(SWT.MouseUp, new Listener() {
			@Override public void handleEvent(Event event) {
				OldImageManagerDialog dialog = new OldImageManagerDialog(parent.getShell(), ElementFigure.this.model, ElementFigure.this.iconName);

				if(dialog.open() == Window.OK) {
					Object selectedObject = dialog.getSelectedObject();
			        
			        try {
			            if ( selectedObject instanceof File )			// User selected a file
		                	ElementFigure.this.iconName = ((IArchiveManager)ElementFigure.this.model.getAdapter(IArchiveManager.class)).addImageFromFile((File)selectedObject);
			            else									// User selected an existing image from the model
			            	ElementFigure.this.iconName = (String)selectedObject;
		            } catch(Exception err) {
			        	SpecializationPlugin.popup(Level.ERROR, "Cannot use file "+selectedObject, err);
			        }
			        
			        // We check that the model contains a DiagramModelImage with the selected image
			        ICanvasModel canvas = null; 
			        for ( IDiagramModel d: ElementFigure.this.model.getDiagramModels() ) {
			        	if ( (d instanceof ICanvasModel ) && (d.getName().equals(CANVAS_NAME)) ) {
			        		canvas = (ICanvasModel)d;
			        		break;
			        	}
			        }
			        
			        if ( canvas == null ) {
			        	// we create a folder
			        	canvas = ICanvasFactory.eINSTANCE.createCanvasModel();
			        	canvas.setId(UUIDFactory.createID(canvas));
			        	canvas.setName(CANVAS_NAME);
			        	canvas.setDocumentation("This canvas is used by the Specialization Plugin to keep the images that are configured in the specializations.\n\nPlease do not modify nor delete it.");
			        	ElementFigure.this.model.getFolder(FolderType.DIAGRAMS).getElements().add(canvas);
			        }
			        
			        IDiagramModelImage diagramModelImage = null;
			        for ( IDiagramModelObject d: canvas.getChildren() ) {
			        	if ( d.getName().equals(ElementFigure.this.iconName) && (d instanceof IDiagramModelImage) ) {
			        		diagramModelImage = (IDiagramModelImage)d;
			        		break;
			        	}
			        }
			        
			        if ( diagramModelImage == null ) {
			        	diagramModelImage = IArchimateFactory.eINSTANCE.createDiagramModelImage();
			        	diagramModelImage.setId(UUIDFactory.createID(diagramModelImage));
			        	diagramModelImage.setName(ElementFigure.this.iconName);
			        	diagramModelImage.setDocumentation("This image stores the image required by the Specialization Plugin.");
			        	diagramModelImage.setImagePath(ElementFigure.this.iconName);
			        	try {
			                Image image = ((IArchiveManager)ElementFigure.this.model.getAdapter(IArchiveManager.class)).createImage(ElementFigure.this.iconName);
			                diagramModelImage.setBounds(image.getBounds().x, image.getBounds().y, image.getBounds().width, image.getBounds().height);
			                image.dispose();
			            }
			            catch(@SuppressWarnings("unused") Exception err) {
			            	diagramModelImage.setBounds(0, 0, 50, 50);
			            }
			        	canvas.getChildren().add(diagramModelImage);
			        }
			        
			        if ( ElementFigure.this.iconName != null ) {
			        	ElementFigure.this.notifyListeners(SWT.Selection, new Event());		// indicates that something changed in the figure
			        }
				}
			}
		});

		this.btnDeleteIcon = new Button(this, SWT.PUSH);
		this.btnDeleteIcon.setImage(SpecializationPlugin.DELETE_ICON);
		this.btnDeleteIcon.setToolTipText("Delete icon");
		this.btnDeleteIcon.setEnabled(false);
		this.btnDeleteIcon.setVisible(false);
		fd = new FormData();
		fd.top = new FormAttachment(this.btnNewIcon, 0, SWT.TOP);
		fd.left = new FormAttachment(this.btnNewIcon, 5);
		fd.width = 20;
		fd.height = 20;
		this.btnDeleteIcon.setLayoutData(fd);
		this.btnDeleteIcon.addListener(SWT.MouseUp, new Listener() {
			@Override public void handleEvent(Event event) {
				ElementFigure.this.iconName = null;
				ElementFigure.this.notifyListeners(SWT.Selection, new Event());		// indicates that something changed in the figure
			}
		});
		
		this.lblIconLocation = new Label(this, SWT.NONE);
		this.lblIconLocation.setForeground(this.getForeground());
		this.lblIconLocation.setBackground(this.getBackground());
		this.lblIconLocation.setText("Location:");
		this.lblIconLocation.setVisible(false);
		fd = new FormData();
		fd.top = new FormAttachment(this.btnNewIcon, 5);
		fd.left = new FormAttachment(this.outerCompo2, 5);
		this.lblIconLocation.setLayoutData(fd);

		this.txtIconLocation = new Text(this, SWT.BORDER);
		this.txtIconLocation.setToolTipText("Location of the icon under the form \"x , y\"\n\n   positive numbers mean from top or left border,\n   negative numbers mean from rigth/bottom border,\n   center keyword allows to center the object inside the figure.");
		this.txtIconLocation.setVisible(false);
		fd = new FormData();
		fd.top = new FormAttachment(this.lblIconLocation, 0, SWT.CENTER);
		fd.left = new FormAttachment(this.lblIconLocation, 5);
		fd.right = new FormAttachment(100);
		this.txtIconLocation.setLayoutData(fd);
		
		this.txtIconLocation.addListener(SWT.DefaultSelection, new Listener() {
			@Override public void handleEvent(Event e) {
				setIconLocation(((Text)e.widget).getText());
				ElementFigure.this.notifyListeners(SWT.Selection, new Event());		// indicates that something changed in the figure
			}
		});
		this.txtIconLocation.addFocusListener(new FocusListener() {
			String text;
			
			@Override public void focusGained(FocusEvent e) {
				this.text=((Text)e.widget).getText();
			}
			@Override public void focusLost(FocusEvent e) {
				if ( !((Text)e.widget).getText().equals(this.text) ) {
					setIconLocation(((Text)e.widget).getText());
					ElementFigure.this.notifyListeners(SWT.Selection, new Event());		// indicates that something changed in the figure
				}
			}
		});

		this.lblIconSize = new Label(this, SWT.NONE);
		this.lblIconSize.setForeground(this.getForeground());
		this.lblIconSize.setBackground(this.getBackground());
		this.lblIconSize.setText("Size:");
		this.lblIconSize.setVisible(false);
		fd = new FormData();
		fd.top = new FormAttachment(this.lblIconLocation, 5);
		fd.left = new FormAttachment(this.lblIconLocation, 0, SWT.LEFT);
		this.lblIconSize.setLayoutData(fd);

		this.txtIconSize = new Text(this, SWT.BORDER);
		this.txtIconSize.setToolTipText("Size of the icon under the form \"width x height\" or \"auto\"\n\n   if width == 0, then a ratio is done using the height\n   if height == 0, then a ratio is done using the width\n\n   auto allows to adapt the icon size to the figure size.");
		this.txtIconSize.setVisible(false);
		fd = new FormData();
		fd.top = new FormAttachment(this.lblIconSize, 0, SWT.CENTER);
		fd.left = new FormAttachment(this.txtIconLocation, 0, SWT.LEFT);
		fd.right = new FormAttachment(this.txtIconLocation, 0, SWT.RIGHT);
		this.txtIconSize.setLayoutData(fd);
		
		this.txtIconSize.addListener(SWT.DefaultSelection, new Listener() {
			@Override public void handleEvent(Event e) {
				setIconSize(((Text)e.widget).getText());
				ElementFigure.this.notifyListeners(SWT.Selection, new Event());		// indicates that something changed in the figure
			}
		});
		this.txtIconSize.addFocusListener(new FocusListener() {
			String text;
			
			@Override public void focusGained(FocusEvent e) {
				this.text=((Text)e.widget).getText();
			}
			@Override public void focusLost(FocusEvent e) {
				if ( !((Text)e.widget).getText().equals(this.text) ) {
					setIconSize(((Text)e.widget).getText());
					ElementFigure.this.notifyListeners(SWT.Selection, new Event());		// indicates that something changed in the figure
				}
			}
		});
	}

	public void reset() {
		this.outerCompo1.setBackground(this.outerCompo1.getParent().getBackground());
		Image oldImage = this.figure1.getBackgroundImage();
		this.figure1.setBackgroundImage(null);
		if ( oldImage != null )
			oldImage.dispose();

		this.outerCompo2.setBackground(this.outerCompo2.getParent().getBackground());
		oldImage = this.figure2.getBackgroundImage();
		this.figure2.setBackgroundImage(null);
		if ( oldImage != null )
			oldImage.dispose();

		this.selectedFigure = null;

		this.btnNewIcon.setVisible(false);
		this.btnDeleteIcon.setVisible(false);
		this.lblIconSize.setVisible(false);
		this.txtIconSize.setVisible(false);
		this.lblIconLocation.setVisible(false);
		this.txtIconLocation.setVisible(false);
	}

	@Override public void setEnabled(boolean enabled) throws SWTException {
		super.setEnabled(enabled);
		this.setBackground(enabled ? SpecializationPlugin.WHITE_COLOR : this.defaultBackgroundColor);
	}
	
	public void resetPreviewImages() {
		Image oldImage = this.figure1.getBackgroundImage();
		this.figure1.setBackgroundImage(getPreviewImage(this.eClass, 0));
		if ( oldImage != null )
			oldImage.dispose();
		
		oldImage = this.figure2.getBackgroundImage();
		this.figure2.setBackgroundImage(getPreviewImage(this.eClass, 1));
		if ( oldImage != null )
			oldImage.dispose();
	}
	
	static Image getPreviewImage(EClass eClass, int type) {
        if(type < 0 || type > 1) {
            return null;
        }
        
        IGraphicalObjectUIProvider provider = (IGraphicalObjectUIProvider)ObjectUIFactory.INSTANCE.getProviderForClass(eClass);

        if(!(provider instanceof IArchimateElementUIProvider)) {
            return null;
        }
        
        // No alternate figure
        if(type > 0 && !((IArchimateElementUIProvider)provider).hasAlternateFigure()) {
            return null;
        }
        
        IDiagramModelArchimateObject dmo = IArchimateFactory.eINSTANCE.createDiagramModelArchimateObject();
        IArchimateElement elm = (IArchimateElement)IArchimateFactory.eINSTANCE.create(eClass);
        elm.setId(ID_PREFIX+type);
        dmo.setId(ID_PREFIX+type+""+type);
        dmo.setArchimateElement(elm);
        dmo.setName(provider.getDefaultName());
        dmo.setTextPosition(provider.getDefaultTextPosition());
        dmo.setTextAlignment(provider.getDefaultTextAlignment());
        ColorFactory.setDefaultColors(dmo);
        dmo.setType(type);

        GraphicalEditPart editPart = (GraphicalEditPart)provider.createEditPart();
        editPart.setModel(dmo);
        
        IDiagramModelObjectFigure figure = (IDiagramModelObjectFigure)editPart.getFigure();
        figure.setSize(new Dimension(ArchiPlugin.INSTANCE.getPreferenceStore().getInt(com.archimatetool.editor.preferences.IPreferenceConstants.DEFAULT_ARCHIMATE_FIGURE_WIDTH), ArchiPlugin.INSTANCE.getPreferenceStore().getInt(com.archimatetool.editor.preferences.IPreferenceConstants.DEFAULT_ARCHIMATE_FIGURE_HEIGHT)));
        figure.refreshVisuals();
        figure.validate();

        Image image = DiagramUtils.createImage(figure, 1, 0);
        
        return image;
    }
	
	public void setEClass(EClass eClass, ElementSpecialization elementSpecialization) {
		this.eClass = eClass;
		
		if ( eClass == null ) {
			reset();
		} else {
			resetPreviewImages();

			IDiagramModelObjectFigure figure = ((ArchimateElementEditPart)ObjectUIFactory.INSTANCE.getProviderForClass(eClass).createEditPart()).getFigure();

			try {
				// either the IDiagramModelObjectFigure delegates the drawing to a figure delegate,
				// and in that case, we need to check if the figure delegate allows to change the icon
				Field field = figure.getClass().getSuperclass().getDeclaredField("fFigureDelegate1");
				field.setAccessible(true);
				this.outerCompo1.setData(canChangeIconString, (field.get(figure) instanceof RectangleFigureDelegate));
				field.setAccessible(false);

				field = figure.getClass().getSuperclass().getDeclaredField("fFigureDelegate2");
				field.setAccessible(true);
				this.outerCompo2.setData(canChangeIconString, (field.get(figure) instanceof RectangleFigureDelegate));
				field.setAccessible(false);
			} catch (@SuppressWarnings("unused") Exception err) {
				// either there is no figure delegate, and then the icon can be changed.
				this.outerCompo1.setData(canChangeIconString, true);
				this.outerCompo2.setData(canChangeIconString, true);
			}

			select(elementSpecialization.getFigure());
			setIconName(elementSpecialization.getIconName());
			setIconSize(elementSpecialization.getIconSize());
			setIconLocation(elementSpecialization.getIconLocation());
		}
	}

	void select(Composite figure) {
		if ( this.selectedFigure != figure ) {
			this.selectedFigure = figure;

			this.outerCompo1.setBackground(figure == this.outerCompo1 ? ColorConstants.blue : this.getBackground());
			this.outerCompo2.setBackground(figure == this.outerCompo2 ? ColorConstants.blue : this.getBackground());

			if ( (this.selectedFigure != null) && (this.selectedFigure.getData(canChangeIconString) != null) ) {
				boolean canChangeIcon = (boolean)this.selectedFigure.getData(canChangeIconString);
				this.btnNewIcon.setVisible(canChangeIcon);

				this.btnDeleteIcon.setEnabled(this.iconName != null);
				this.btnDeleteIcon.setVisible(canChangeIcon);

				this.lblIconSize.setVisible(canChangeIcon && (this.iconName != null));
				this.txtIconSize.setVisible(canChangeIcon && (this.iconName != null));
				this.lblIconLocation.setVisible(canChangeIcon && (this.iconName != null));
				this.txtIconLocation.setVisible(canChangeIcon && (this.iconName != null));

				logger.trace("canChangeIcon = " + canChangeIcon);
			}
		}
	}
	
	void select(int type) {
		if ( type == 0 )
			select(this.outerCompo1);
		else
			select(this.outerCompo2);
	}
	
	public void setModel(IArchimateModel model) {
		this.model = model;
	}

	public int getSelectedFigure() {
		return ( (this.selectedFigure == null) || (this.selectedFigure == this.outerCompo1) ) ? 0 : 1;
	}

	public boolean canChangeIcon() {
		return (this.selectedFigure != null) && (this.selectedFigure.getData(canChangeIconString) != null);
	}

	public String getIconName() {
		if ( canChangeIcon() && (this.iconName != null) && !this.iconName.isEmpty() )
			return this.iconName;
		return null;
	}
	
	public void setIconName(String name) {
		this.iconName = name;
	}

	public String getIconSize() {
		if ( canChangeIcon() && !this.txtIconSize.getText().isEmpty() )
			return this.txtIconSize.getText();
		return null;
	}

	public void setIconSize(String iconSize) {
		if ( iconSize == null )
			this.txtIconSize.setText("");
		else
			this.txtIconSize.setText(iconSize);
	}

	public String getIconLocation() {
		if ( canChangeIcon() && !this.txtIconLocation.getText().isEmpty() )
			return this.txtIconLocation.getText();
		return null;
	}

	public void setIconLocation(String iconLocation) {
		if ( iconLocation == null )
			this.txtIconLocation.setText("");
		else
			this.txtIconLocation.setText(iconLocation);
	}

	private MouseAdapter selectListener = new MouseAdapter() {
		@Override
		public void mouseDown(MouseEvent e) {
			Composite outerComposite = ((Label)e.widget).getParent().getParent();
			// if the outer composite is not yet selected, we select it
			if ( outerComposite != ElementFigure.this.selectedFigure ) {
				select(outerComposite);
				ElementFigure.this.notifyListeners(SWT.Selection, new Event());		// indicates that something changed in the figure
			}
		}
	};
}