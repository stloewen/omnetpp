/*--------------------------------------------------------------*
  Copyright (C) 2006-2015 OpenSim Ltd.

  This file is distributed WITHOUT ANY WARRANTY. See the file
  'License' for details on this and other legal matters.
*--------------------------------------------------------------*/

package org.omnetpp.scave.editors.forms;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.ColorDialog;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FontDialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Text;
import org.omnetpp.common.color.ColorFactory;
import org.omnetpp.common.ui.SWTFactory;
import org.omnetpp.common.ui.StyledTextUndoRedoManager;
import org.omnetpp.common.util.Converter;
import org.omnetpp.common.util.StringUtils;
import org.omnetpp.common.wizard.XSWTDataBinding;
import org.omnetpp.scave.ScavePlugin;
import org.omnetpp.scave.charting.properties.ChartDefaults;
import org.omnetpp.scave.charting.properties.ChartProperties;
import org.omnetpp.scave.charting.properties.ChartProperties.LegendAnchor;
import org.omnetpp.scave.charting.properties.ChartProperties.LegendPosition;
import org.omnetpp.scave.charting.properties.ChartProperties.ShowGrid;
import org.omnetpp.scave.editors.ui.ResultItemNamePatternField;
import org.omnetpp.scave.engine.ResultFileManager;
import org.omnetpp.scave.engine.Run;
import org.omnetpp.scave.model.Chart;
import org.omnetpp.scave.model.MatplotlibChart;
import org.omnetpp.scave.model.Property;
import org.omnetpp.scave.model.ScaveModelPackage;
import org.omnetpp.scave.model2.ChartLine;

import com.swtworkbench.community.xswt.XSWT;

/**
 * Edit form of charts.
 *
 * The properties of the chart are organized into groups
 * each group is displayed in a tab of the main tab folder.
 *
 * @author tomi
 */
// TODO use validator for font and number fields
// TODO: split into super class containing only "Main" (for Matplotlib), and "NativeWidgetChartEditForm" to add the rest
public class ChartEditForm extends BaseScaveObjectEditForm {
    public static final String TAB_MAIN = "Main";
    public static final String TAB_CHART = "Chart";
    public static final String TAB_AXES = "Axes";
    public static final String TAB_LEGEND = "Legend";
    public static final String TAB_PARAMETERS = "Parameters";

    public static final String PROP_DEFAULT_TAB = "default-page";

    protected static final ScaveModelPackage pkg = ScaveModelPackage.eINSTANCE;

    /**
     * Features edited on this form.
     */
    private static final EStructuralFeature[] features = new EStructuralFeature[] {
        pkg.getAnalysisItem_Name(),
        pkg.getChart_Script(),
        pkg.getChart_Properties(),
    };

    /**
     * The edited chart.
     */
    protected Chart chart;
    protected Map<String, Object> formParameters;
    protected ResultFileManager manager;
    protected ChartProperties properties;
    protected Map<String,Control> xswtWidgetMap;

    // controls
    private Text nameText;
    private StyledText scriptText;
    //private ScriptContentProposalProvider scriptContentProposalProvider;
    protected Group optionsGroup;
    private Button antialiasCheckbox;
    private Button cachingCheckbox;
    private ColorEdit backgroundColor;

    protected Group axisTitlesGroup;
    private Text graphTitleText;
    private Text graphTitleFontText;
    private Text xAxisTitleText;
    private Text yAxisTitleText;
    private Text axisTitleFontText;
    private Text labelFontText;
    private Combo xLabelsRotateByCombo;

    protected Group axisBoundsGroup;
    private Label maxBoundLabel;
    private Text yAxisMinText;
    private Text yAxisMaxText;
    private Group axisOptionsGroup;
    private Button yAxisLogCheckbox;
    private Combo showGridCombo;

    private Button displayLegendCheckbox;
    private Button displayBorderCheckbox;
    private Text legendFontText;
    private Combo legendPositionCombo;
    private Combo legendAnchorCombo;


    /**
     * Number of visible items in combo boxes.
     */
    protected static final int VISIBLE_ITEM_COUNT = 15;

    protected static final String NO_CHANGE = "(no change)";

    protected static final String USER_DATA_KEY = "ChartEditForm";

    public ChartEditForm(Chart chart, Map<String,Object> formParameters, ResultFileManager manager) {
        super(chart);
        this.chart = chart;
        this.formParameters = formParameters;
        this.manager = manager;
        this.properties = ChartProperties.createPropertySource(chart, manager);
    }

    /**
     * Returns the features edited on this form.
     */
    public EStructuralFeature[] getFeatures() {
        return features;
    }

    /**
     * Creates the controls of the dialog.
     */
    public void populatePanel(Composite panel) {
        panel.setLayout(new GridLayout(1, false));
        final TabFolder tabfolder = createTabFolder(panel);

        populateTabFolder(tabfolder);
        for (int i=0; i < tabfolder.getItemCount(); ++i)
            populateTabItem(tabfolder.getItem(i));

        // switch to the requested page
        String defaultPage = formParameters==null ? null : (String) formParameters.get(PROP_DEFAULT_TAB);
        if (formParameters != null && formParameters.get(PARAM_SELECTED_OBJECT) instanceof ChartLine)
            defaultPage = BaseLineChartEditForm.TAB_LINES; // when editing a line, open with the "Lines" tab
        if (defaultPage == null)
            defaultPage = getDialogSettings().get(PROP_DEFAULT_TAB);
        if (defaultPage != null)
            for (TabItem tabItem : tabfolder.getItems())
                if (tabItem.getText().equals(defaultPage)) {
                    tabfolder.setSelection(tabItem);
                    break;
                }

        // save current tab as dialog setting (the code is here because there's no convenient function that is invoked on dialog close (???))
        tabfolder.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                TabItem[] selectedTabs = tabfolder.getSelection();
                if (selectedTabs.length > 0)
                    getDialogSettings().put(PROP_DEFAULT_TAB, selectedTabs[0].getText());
            }
        });
    }

    protected IDialogSettings getDialogSettings() {
        final String KEY = "ChartEditForm";
        IDialogSettings dialogSettings = ScavePlugin.getDefault().getDialogSettings();
        IDialogSettings section = dialogSettings.getSection(KEY);
        if (section == null)
            section = dialogSettings.addNewSection(KEY);
        return section;
    }

    /**
     * Creates the tabs of the dialog.
     */
    protected void populateTabFolder(TabFolder tabfolder) {
        createTab(TAB_MAIN, tabfolder, 1);

        if (!(chart instanceof MatplotlibChart) ) {
            createTab(TAB_CHART, tabfolder, 1);
            createTab(TAB_AXES, tabfolder, 2);
            createTab(TAB_LEGEND, tabfolder, 1);
        }

        createTab(TAB_PARAMETERS, tabfolder, 1);
    }

    List<String> getComboContents(String contentString) {
        List<String> result = new ArrayList<String>();

        for (String part : contentString.split(",")) {
            switch (part) {
            case "scalarnames":
                for (String name : manager.getUniqueNames(manager.getAllScalars(false, false)).keys().toArray())
                    result.add(name);
                break;
            case "vectornames":
                for (String name : manager.getUniqueNames(manager.getAllVectors()).keys().toArray())
                    result.add(name);
                break;
            case "histogramnames":
                for (String name : manager.getUniqueNames(manager.getAllHistograms()).keys().toArray())
                    result.add(name);
                break;
            case "statisticnames":
                for (String name : manager.getUniqueNames(manager.getAllStatistics()).keys().toArray())
                    result.add(name);
                break;
            case "itervarnames":
                Set<String> itervars = new HashSet<String>();

                for (Run run : manager.getRuns().toArray())
                    for (String itervar : run.getIterationVariables().keys().toArray())
                        itervars.add(itervar);

                result.addAll(itervars);
                break;
            case "runattrnames":
                Set<String> runattrs = new HashSet<String>();

                for (Run run : manager.getRuns().toArray())
                    for (String runattr : run.getAttributes().keys().toArray())
                        runattrs.add(runattr);

                result.addAll(runattrs);
                break;
            }
        }

        return result;
    }

    /**
     * Creates the controls of the given tab.
     */
    protected void populateTabItem(TabItem item) {
        Group group;
        String name = item.getText();
        Composite panel = (Composite)item.getControl();

        if (TAB_MAIN.equals(name)) {
            ((GridLayout)panel.getLayout()).numColumns = 2;
            nameText = createTextField("Chart name:", panel);
            nameText.setFocus();
            scriptText = createMultilineTextField("Chart script:", panel);
            ((GridLayout)panel.getLayout()).numColumns = 2;
            new StyledTextUndoRedoManager(scriptText);
            //scriptContentProposalProvider = new ScriptContentProposalProvider();
            //ContentAssistUtil.configureStyledText(scriptText, scriptContentProposalProvider);
            //scriptContentProposalProvider.setFilterHints(new FilterHints(manager, manager.getAllItems(false)));
            scriptText.addModifyListener((e) -> fireChangeNotification());
        }
        else if (TAB_CHART.equals(name)) {
            group = createGroup("Title", panel);
            graphTitleText = createTextField("Chart title:", group);
            new ResultItemNamePatternField(graphTitleText);
            graphTitleFontText = createFontField("Title font:", group);
            optionsGroup = createGroup("Options", panel, 2);
            showGridCombo = createComboField("Show grid:", optionsGroup, ShowGrid.class, false);
            backgroundColor = createColorField("Background color:", optionsGroup);
            antialiasCheckbox = createCheckboxField("Use antialias", optionsGroup);
            antialiasCheckbox.setSelection(ChartDefaults.DEFAULT_ANTIALIAS);
            setColumnSpan(antialiasCheckbox, 2);
            cachingCheckbox = createCheckboxField("Use caching", optionsGroup);
            cachingCheckbox.setSelection(ChartDefaults.DEFAULT_CANVAS_CACHING);
            setColumnSpan(cachingCheckbox, 2);
        }
        else if (TAB_AXES.equals(name)) {
            axisTitlesGroup = createGroup("Titles", panel, 2);
            xAxisTitleText = createTextField("X axis title:", axisTitlesGroup);
            yAxisTitleText = createTextField("Y axis title:", axisTitlesGroup);
            axisTitleFontText = createFontField("Axis title font:", axisTitlesGroup);
            labelFontText = createFontField("Label font:", axisTitlesGroup);
            xLabelsRotateByCombo = createComboField("Rotate X labels by:", axisTitlesGroup, new String[] {"0", "30", "45", "60", "90"});
            axisBoundsGroup = createGroup("Ranges", panel, 3);
            axisBoundsGroup.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
            createLabel("", axisBoundsGroup);
            createLabel("Min", axisBoundsGroup);
            maxBoundLabel = createLabel("Max", axisBoundsGroup);
            yAxisMinText = createTextField("Y axis", axisBoundsGroup);
            yAxisMaxText = createTextField(null, axisBoundsGroup);
            yAxisLogCheckbox = createCheckboxField("Logarithmic Y axis", axisBoundsGroup);
            setColumnSpan(yAxisLogCheckbox, 2);
        }
        else if (TAB_LEGEND.equals(name)) {
            displayLegendCheckbox = createCheckboxField("Display legend", panel);
            group = createGroup("Options", panel);
            displayBorderCheckbox = createCheckboxField("Border", group);
            displayBorderCheckbox.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
            legendFontText = createFontField("Legend font:", group);
            legendPositionCombo = createComboField("Position:", group, LegendPosition.class, false);
            legendAnchorCombo = createComboField("Anchoring:", group, LegendAnchor.class, false);
            displayLegendCheckbox.addSelectionListener(new SelectionAdapter() {
                @Override
                public void widgetSelected(SelectionEvent e) {
                    updateLegendPanelEnabled();
                }
            });
            updateLegendPanelEnabled();
        } else if (TAB_PARAMETERS.equals(name)) {
            try {
                Composite xswtHolder = SWTFactory.createComposite(panel, 1, 1, SWTFactory.GRAB_AND_FILL_HORIZONTAL);
                xswtWidgetMap = new HashMap<>(); // prevent NPE later
                if (chart.getForm() != null && !chart.getForm().isEmpty())
                    xswtWidgetMap = XSWT.create(xswtHolder, new ByteArrayInputStream(chart.getForm().getBytes()));
            } catch (Exception e) {
                IStatus status = new Status(IStatus.ERROR, ScavePlugin.PLUGIN_ID, "Error showing the XSWT form of chart '" + chart.getName() + "'", e);
                ScavePlugin.log(status);
                ErrorDialog.openError(Display.getCurrent().getActiveShell(), "Error", "Cannot add chart options to Edit dialog.", status);
            }

            for (String key : xswtWidgetMap.keySet()) {
                Control control = xswtWidgetMap.get(key);
                String content = (String)control.getData("content");

                if (control instanceof Combo && content != null) {
                    Combo combo = (Combo)control;
                    for (String comboItem : getComboContents(content))
                        combo.add(comboItem);
                }
            }

            for (Property prop : chart.getProperties())
                if (xswtWidgetMap.containsKey(prop.getName())) {
                    XSWTDataBinding.putValueIntoControl(xswtWidgetMap.get(prop.getName()), prop.getValue(), null);
                }
        }
    }

    protected Group getAxisBoundsGroup() {
        return axisBoundsGroup;
    }

    protected Label getMaxBoundLabel() {
        return maxBoundLabel;
    }

    protected Group getAxisOptionsGroup() {
        return axisOptionsGroup;
    }

    protected Button getYAxisLogCheckbox() {
        return yAxisLogCheckbox;
    }

    private TabFolder createTabFolder(Composite parent) {
        TabFolder tabfolder = new TabFolder(parent, SWT.NONE);
        tabfolder.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        return tabfolder;

    }

    protected Composite createTab(String tabText, TabFolder tabfolder, int numOfColumns) {
        TabItem tabitem = new TabItem(tabfolder, SWT.NONE);
        tabitem.setText(tabText);
        Composite panel = new Composite(tabfolder, SWT.NONE);
        panel.setLayout(new GridLayout(numOfColumns, false));
        tabitem.setControl(panel);
        return panel;
    }

    protected Group createGroup(String text, Composite parent) {
        return createGroup(text, parent, 2);
    }

    protected Group createGroup(String text, Composite parent, int numOfColumns) {
        return createGroup(text, parent, 1, numOfColumns);
    }

    protected Group createGroup(String text, Composite parent, int colSpan, int numOfColumns) {
        return createGroup(text, parent, colSpan, numOfColumns, false);
    }

    protected Group createGroup(String text, Composite parent,
            int colSpan, int numOfColumns, boolean grabExcessVerticalSpace) {
        Group group = new Group(parent, SWT.NONE);
        group.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, grabExcessVerticalSpace, colSpan, 1));
        group.setLayout(new GridLayout(numOfColumns, false));
        group.setText(text);
        return group;
    }

    protected void setColumnSpan(Control control, int hspan) {
        ((GridData)control.getLayoutData()).horizontalSpan = hspan;
    }

    /**
     * Moves {@code child} before {@code nextSibling} in the children
     * list of their common parent.
     */
    protected void moveChildBefore(Control child, Control nextSibling) {
        if (nextSibling != null)
            child.moveAbove(nextSibling); // above in Z order = before in child order
        else
            child.moveBelow(null); // no next = last child, smallest Z
    }

    /**
     * Moves {@code child} after {@code nextSibling} in the children
     * list of their common parent.
     */
    protected void moveChildAfter(Control child, Control prevSibling) {
        if (prevSibling != null)
            child.moveBelow(prevSibling);
        else
            child.moveAbove(null);
    }

    protected Label createLabel(String text, Composite parent) {
        Label label = new Label(parent, SWT.NONE);
        label.setText(text);
        return label;
    }

    protected Text createTextField(String labelText, Composite parent) {
        if (labelText != null)
            createLabel(labelText, parent);
        Text text = new Text(parent, SWT.BORDER);
        text.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        return text;
    }

    protected Text createTextField(String labelText, Composite parent, Control prevSibling) {
        Label label = null;
        if (labelText != null)
            label = createLabel(labelText, parent);
        Text text = new Text(parent, SWT.BORDER);
        text.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

        if (label != null) {
            moveChildAfter(label, prevSibling);
            moveChildAfter(text, label);
        }
        else {
            moveChildAfter(text, prevSibling);
        }
        return text;
    }

    protected StyledText createMultilineTextField(String labelText, Composite parent) {
        if (labelText != null) {
            Label label = createLabel(labelText, parent);
            label.setLayoutData(new GridData(SWT.BEGINNING, SWT.BEGINNING, false, false));
        }
        StyledText text = new StyledText(parent, SWT.MULTI | SWT.WRAP | SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL);
        text.setAlwaysShowScrollBars(false);
        text.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        ((GridData)text.getLayoutData()).heightHint = 100;
        return text;
    }

    protected Combo createComboField(String labelText, Composite parent, String[] items) {
        return createComboField(labelText, parent, items, false);
    }

    protected Combo createComboField(String labelText, Composite parent, String[] items, boolean optional) {
        Label label = new Label(parent, SWT.NONE);
        label.setText(labelText);
        int style = SWT.BORDER | SWT.READ_ONLY;
        Combo combo = new Combo(parent, style);
        combo.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        combo.setVisibleItemCount(VISIBLE_ITEM_COUNT);
        combo.setItems(items);
        if (optional) combo.add(NO_CHANGE, 0);
        return combo;
    }

    protected Combo createComboField(String labelText, Composite parent, Class<? extends Enum<?>> type, boolean optional) {
        Enum<?>[] values = type.getEnumConstants();
        String[] items = new String[values.length];
        for (int i = 0; i < values.length; ++i)
            items[i] = values[i].toString();
        return createComboField(labelText, parent, items, optional);
    }

    protected Button createCheckboxField(String labelText, Composite parent) {
        return createCheckboxField(labelText, parent, null);
    }

    protected Button createCheckboxField(String labelText, Composite parent, Control nextSibling) {
        return createCheckboxField(labelText, parent, nextSibling, null);
    }

    protected Button createCheckboxField(String labelText, Composite parent, Control nextSibling, Object value) {
        Button checkbox = new Button(parent, SWT.CHECK);
        checkbox.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        checkbox.setText(labelText);
        checkbox.setData(USER_DATA_KEY, value);
        moveChildBefore(checkbox, nextSibling);
        return checkbox;
    }

    protected Button createRadioField(String labelText, Composite parent, Object value) {
        Button radio = new Button(parent, SWT.RADIO);
        radio.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        radio.setText(labelText);
        radio.setData(USER_DATA_KEY, value);
        return radio;
    }

    protected Button[] createRadioGroup(String groupLabel, Composite parent, int numOfColumns, Class<? extends Enum<?>> type, boolean optional, String... radioLabels) {
        Group group = createGroup(groupLabel, parent, numOfColumns);
        Enum<?>[] values = type.getEnumConstants();
        int numOfRadios = optional ? values.length + 1 : values.length;
        Button[] radios = new Button[numOfRadios];
        int i = 0;
        if (optional) {
            radios[i++] = createRadioField(NO_CHANGE, group, null);
        }
        for (int j = 0; j < values.length; ++j) {
            Enum<?> value = values[j];
            String radioLabel = radioLabels != null && j < radioLabels.length ? radioLabels[j] :
                                value.name().toLowerCase();
            radios[i++] = createRadioField(radioLabel, group, value);
        }
        return radios;
    }

    protected void updateLegendPanelEnabled() {
        if (displayLegendCheckbox != null && !displayLegendCheckbox.isDisposed()) {
            setEnabledDescendants(
                displayLegendCheckbox.getParent(),
                displayLegendCheckbox.getSelection(),
                displayLegendCheckbox);
        }
    }

    protected static class ColorEdit {
        Text text;
        Label label;

        public ColorEdit(final Text text, final Label label) {
            this.text = text;
            this.label = label;
            updateImageLabel();
            text.addModifyListener(new ModifyListener() {
                public void modifyText(ModifyEvent e) {
                    updateImageLabel();
                }
            });
        }

        public String getText() {
            return StringUtils.trimToEmpty(text.getText());
        }

        public void setText(String color) {
            text.setText(color);
            updateImageLabel();
        }

        private void updateImageLabel() {
            Image image = ColorFactory.createColorImage(ColorFactory.asRGB(text.getText()));
            Image oldImage = label.getImage();
            label.setImage(image);
            if (oldImage != null)
                oldImage.dispose();
        }
    }

    protected ColorEdit createColorField(String labelText, Composite parent) {
        Label label = new Label(parent, SWT.NONE);
        label.setText(labelText);
        Composite panel = new Composite(parent, SWT.NONE);
        panel.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        GridLayout layout = new GridLayout(3, false);
        layout.marginWidth = 0;
        layout.marginHeight = 0;
        panel.setLayout(layout);
        Label imageLabel = new Label(panel, SWT.NONE);
        imageLabel.setLayoutData(new GridData(16,16));
        Text text = new Text(panel, SWT.BORDER);
        text.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
        /*new ContentAssistCommandAdapter(text, new TextContentAdapter(), new ColorContentProposalProvider(),
                ITextEditorActionDefinitionIds.CONTENT_ASSIST_PROPOSALS, null, true);*/
        Button button = new Button(panel, SWT.NONE);
        button.setText("...");
        final ColorEdit colorField = new ColorEdit(text, imageLabel);
        button.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                if (!colorField.text.isDisposed()) {
                    ColorDialog dialog = new ColorDialog(colorField.text.getShell());
                    dialog.setText(colorField.getText());
                    RGB rgb = dialog.open();
                    if (rgb != null) {
                        colorField.setText(ColorFactory.asString(rgb));
                    }
                }
            }
        });
        return colorField;
    }

    protected Text createFontField(String labelText, Composite parent) {
        Label label = new Label(parent, SWT.NONE);
        label.setText(labelText);
        Composite panel = new Composite(parent, SWT.NONE);
        panel.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        GridLayout layout = new GridLayout(2, false);
        layout.marginWidth = 0;
        layout.marginHeight = 0;
        panel.setLayout(layout);
        final Text text = new Text(panel, SWT.BORDER);
        text.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
        Button button = new Button(panel, SWT.NONE);
        button.setText("...");
        button.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent event) {
                if (!text.isDisposed()) {
                    FontDialog dialog = new FontDialog(text.getShell());

                    FontData font = Converter.stringToFontdata(text.getText());
                    if (font != null)
                        dialog.setFontList(new FontData[] {font});
                    font = dialog.open();
                    if (font != null)
                        text.setText(Converter.fontdataToString(font));
                }
            }
        });
        return text;
    }

    /**
     * Reads the value of the given feature from the corresponding control.
     */
    public Object getValue(EStructuralFeature feature) {
        switch (feature.getFeatureID()) {
        case ScaveModelPackage.CHART__NAME:
            return nameText.getText();
        case ScaveModelPackage.CHART__SCRIPT:
            return scriptText.getText();
        case ScaveModelPackage.CHART__PROPERTIES:
            ChartProperties newProps = ChartProperties.createPropertySource(chart, new ArrayList<Property>(), manager);
            collectProperties(newProps);
            return newProps.getProperties();
        }
        return null;
    }

    /**
     * Sets the value of the given feature in the corresponding control.
     */
    @SuppressWarnings("unchecked")
    public void setValue(EStructuralFeature feature, Object value) {
        switch (feature.getFeatureID()) {
        case ScaveModelPackage.CHART__NAME:
            nameText.setText(value == null ? "" : (String)value);
            break;
        case ScaveModelPackage.CHART__SCRIPT:
            scriptText.setText(value == null ? "" : (String)value);
            break;
        case ScaveModelPackage.CHART__PROPERTIES:
            if (value != null) {
                List<Property> properties = (List<Property>)value;
                ChartProperties props = ChartProperties.createPropertySource(chart, properties, manager);
                setProperties(props);
            }
            break;
        }
    }

    /**
     * Sets the properties in <code>newProps</code> from the values of the controls.
     */
    protected void collectProperties(ChartProperties newProps) {
        if (!(chart instanceof MatplotlibChart)) {
            // Main
            newProps.setAntialias(antialiasCheckbox.getSelection());
            newProps.setCaching(cachingCheckbox.getSelection());
            newProps.setBackgroundColor(backgroundColor.getText());
            // Titles
            newProps.setGraphTitle(graphTitleText.getText());
            newProps.setGraphTitleFont(Converter.stringToFontdata(graphTitleFontText.getText()));
            newProps.setXAxisTitle(xAxisTitleText.getText());
            newProps.setYAxisTitle(yAxisTitleText.getText());
            newProps.setAxisTitleFont(Converter.stringToFontdata(axisTitleFontText.getText()));
            newProps.setLabelsFont(Converter.stringToFontdata(labelFontText.getText()));
            newProps.setXLabelsRotate(Converter.stringToDouble(xLabelsRotateByCombo.getText()));
            // Axes
            newProps.setYAxisMin(Converter.stringToDouble(yAxisMinText.getText()));
            newProps.setYAxisMax(Converter.stringToDouble(yAxisMaxText.getText()));
            newProps.setYAxisLogarithmic(yAxisLogCheckbox.getSelection());
            newProps.setXYGrid(resolveEnum(showGridCombo.getText(), ShowGrid.class));
            // Legend
            newProps.setDisplayLegend(displayLegendCheckbox.getSelection());
            newProps.setLegendBorder(displayBorderCheckbox.getSelection());
            newProps.setLegendFont(Converter.stringToFontdata(legendFontText.getText()));
            newProps.setLegendPosition(resolveEnum(legendPositionCombo.getText(), LegendPosition.class));
            newProps.setLegendAnchoring(resolveEnum(legendAnchorCombo.getText(), LegendAnchor.class));
        }

        for (String k : xswtWidgetMap.keySet()) {
            Control control = xswtWidgetMap.get(k);
            Object value = XSWTDataBinding.getValueFromControl(control, null);
            newProps.setProperty(k, value.toString());
        }
    }

    /**
     * Returns the selected radio button as the enum value it represents.
     */
    @SuppressWarnings("unchecked")
    protected static <T extends Enum<T>> T getSelection(Button[] radios, Class<T> type) {
        for (int i = 0; i < radios.length; ++i)
            if (radios[i].getSelection())
                return (T)radios[i].getData(USER_DATA_KEY);
        return null;
    }

    protected static <T extends Enum<T>> T resolveEnum(String text, Class<T> type) {
        T[] values = type.getEnumConstants();
        for (int i = 0; i < values.length; ++i)
            if (values[i].toString().equals(text))
                return values[i];
        return null;
    }

    /**
     * Sets the values of the controls from the given <code>props</code>.
     * @param props
     */
    protected void setProperties(ChartProperties props) {
        if (!(chart instanceof MatplotlibChart)) {
            // Main
            antialiasCheckbox.setSelection(props.getAntialias());
            cachingCheckbox.setSelection(props.getCaching());
            backgroundColor.setText(props.getBackgroundColor());
            // Titles
            graphTitleText.setText(props.getGraphTitle());
            graphTitleFontText.setText(asString(props.getGraphTitleFont()));
            xAxisTitleText.setText(props.getXAxisTitle());
            yAxisTitleText.setText(props.getYAxisTitle());
            axisTitleFontText.setText(asString(props.getAxisTitleFont()));
            labelFontText.setText(asString(props.getLabelsFont()));
            xLabelsRotateByCombo.setText(StringUtils.defaultString(Converter.integerToString(props.getXLabelsRotate().intValue())));

            // Axes
            yAxisMinText.setText(StringUtils.defaultString(Converter.doubleToString(props.getYAxisMin())));
            yAxisMaxText.setText(StringUtils.defaultString(Converter.doubleToString(props.getYAxisMax())));
            yAxisLogCheckbox.setSelection(props.getYAxisLogarithmic());
            showGridCombo.setText(props.getXYGrid().toString());
            // Legend
            displayLegendCheckbox.setSelection(props.getDisplayLegend());
            displayBorderCheckbox.setSelection(props.getLegendBorder());
            legendFontText.setText(asString(props.getLegendFont()));
            legendPositionCombo.setText(props.getLegendPosition().toString());
            legendAnchorCombo.setText(props.getLegendAnchoring().toString());
            updateLegendPanelEnabled();
        }
    }

    private static String asString(FontData fontData) {
        String str = Converter.fontdataToString(fontData);
        return str != null ? str : "";
    }

    /**
     * Select the radio button representing the enum value.
     */
    protected static void setSelection(Button[] radios, Enum<?> value) {
        for (int i = 0; i < radios.length; ++i)
            radios[i].setSelection(radios[i].getData(USER_DATA_KEY) == value);
    }

    /**
     * Sets the enabled state of the controls under {@code composite}
     * except the given {@code control} to {@code enabled}.
     */
    protected void setEnabledDescendants(Composite composite, boolean enabled, Control except) {
        for (Control child : composite.getChildren()) {
            if (child != except)
                child.setEnabled(enabled);
            if (child instanceof Composite)
                setEnabledDescendants((Composite)child, enabled, except);
        }
    }
}
