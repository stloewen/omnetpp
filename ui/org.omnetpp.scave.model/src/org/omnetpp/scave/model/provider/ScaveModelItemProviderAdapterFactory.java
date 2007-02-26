/**
 * <copyright>
 * </copyright>
 *
 * $Id$
 */
package org.omnetpp.scave.model.provider;

import java.util.ArrayList;
import java.util.Collection;

import org.eclipse.emf.common.notify.Adapter;
import org.eclipse.emf.common.notify.Notification;
import org.eclipse.emf.common.notify.Notifier;

import org.eclipse.emf.edit.provider.ChangeNotifier;
import org.eclipse.emf.edit.provider.ComposeableAdapterFactory;
import org.eclipse.emf.edit.provider.ComposedAdapterFactory;
import org.eclipse.emf.edit.provider.IChangeNotifier;
import org.eclipse.emf.edit.provider.IDisposable;
import org.eclipse.emf.edit.provider.IEditingDomainItemProvider;
import org.eclipse.emf.edit.provider.IItemLabelProvider;
import org.eclipse.emf.edit.provider.IItemPropertySource;
import org.eclipse.emf.edit.provider.INotifyChangedListener;
import org.eclipse.emf.edit.provider.IStructuredItemContentProvider;
import org.eclipse.emf.edit.provider.ITreeItemContentProvider;

import org.omnetpp.scave.model.util.ScaveModelAdapterFactory;

/**
 * This is the factory that is used to provide the interfaces needed to support Viewers.
 * The adapters generated by this factory convert EMF adapter notifications into calls to {@link #fireNotifyChanged fireNotifyChanged}.
 * The adapters also support Eclipse property sheets.
 * Note that most of the adapters are shared among multiple instances.
 * <!-- begin-user-doc -->
 * <!-- end-user-doc -->
 * @generated
 */
public class ScaveModelItemProviderAdapterFactory extends ScaveModelAdapterFactory implements ComposeableAdapterFactory, IChangeNotifier, IDisposable {
    /**
     * This keeps track of the root adapter factory that delegates to this adapter factory.
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @generated
     */
	protected ComposedAdapterFactory parentAdapterFactory;

    /**
     * This is used to implement {@link org.eclipse.emf.edit.provider.IChangeNotifier}.
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @generated
     */
	protected IChangeNotifier changeNotifier = new ChangeNotifier();

    /**
     * This keeps track of all the supported types checked by {@link #isFactoryForType isFactoryForType}.
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @generated
     */
	protected Collection supportedTypes = new ArrayList();

    /**
     * This constructs an instance.
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @generated
     */
	public ScaveModelItemProviderAdapterFactory() {
        supportedTypes.add(IEditingDomainItemProvider.class);
        supportedTypes.add(IStructuredItemContentProvider.class);
        supportedTypes.add(ITreeItemContentProvider.class);
        supportedTypes.add(IItemLabelProvider.class);
        supportedTypes.add(IItemPropertySource.class);		
    }

    /**
     * This keeps track of the one adapter used for all {@link org.omnetpp.scave.model.Dataset} instances.
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @generated
     */
	protected DatasetItemProvider datasetItemProvider;

    /**
     * This creates an adapter for a {@link org.omnetpp.scave.model.Dataset}.
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @generated
     */
	public Adapter createDatasetAdapter() {
        if (datasetItemProvider == null) {
            datasetItemProvider = new DatasetItemProvider(this);
        }

        return datasetItemProvider;
    }

    /**
     * This keeps track of the one adapter used for all {@link org.omnetpp.scave.model.Add} instances.
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @generated
     */
	protected AddItemProvider addItemProvider;

    /**
     * This creates an adapter for a {@link org.omnetpp.scave.model.Add}.
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @generated
     */
	public Adapter createAddAdapter() {
        if (addItemProvider == null) {
            addItemProvider = new AddItemProvider(this);
        }

        return addItemProvider;
    }

    /**
     * This keeps track of the one adapter used for all {@link org.omnetpp.scave.model.Apply} instances.
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @generated
     */
	protected ApplyItemProvider applyItemProvider;

    /**
     * This creates an adapter for a {@link org.omnetpp.scave.model.Apply}.
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @generated
     */
	public Adapter createApplyAdapter() {
        if (applyItemProvider == null) {
            applyItemProvider = new ApplyItemProvider(this);
        }

        return applyItemProvider;
    }

    /**
     * This keeps track of the one adapter used for all {@link org.omnetpp.scave.model.Except} instances.
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @generated
     */
	protected ExceptItemProvider exceptItemProvider;

    /**
     * This creates an adapter for a {@link org.omnetpp.scave.model.Except}.
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @generated
     */
	public Adapter createExceptAdapter() {
        if (exceptItemProvider == null) {
            exceptItemProvider = new ExceptItemProvider(this);
        }

        return exceptItemProvider;
    }

    /**
     * This keeps track of the one adapter used for all {@link org.omnetpp.scave.model.Property} instances.
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @generated
     */
	protected PropertyItemProvider propertyItemProvider;

    /**
     * This creates an adapter for a {@link org.omnetpp.scave.model.Property}.
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @generated
     */
	public Adapter createPropertyAdapter() {
        if (propertyItemProvider == null) {
            propertyItemProvider = new PropertyItemProvider(this);
        }

        return propertyItemProvider;
    }

    /**
     * This keeps track of the one adapter used for all {@link org.omnetpp.scave.model.Group} instances.
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @generated
     */
	protected GroupItemProvider groupItemProvider;

    /**
     * This creates an adapter for a {@link org.omnetpp.scave.model.Group}.
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @generated
     */
	public Adapter createGroupAdapter() {
        if (groupItemProvider == null) {
            groupItemProvider = new GroupItemProvider(this);
        }

        return groupItemProvider;
    }

    /**
     * This keeps track of the one adapter used for all {@link org.omnetpp.scave.model.Discard} instances.
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @generated
     */
	protected DiscardItemProvider discardItemProvider;

    /**
     * This creates an adapter for a {@link org.omnetpp.scave.model.Discard}.
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @generated
     */
	public Adapter createDiscardAdapter() {
        if (discardItemProvider == null) {
            discardItemProvider = new DiscardItemProvider(this);
        }

        return discardItemProvider;
    }

    /**
     * This keeps track of the one adapter used for all {@link org.omnetpp.scave.model.Param} instances.
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @generated
     */
	protected ParamItemProvider paramItemProvider;

    /**
     * This creates an adapter for a {@link org.omnetpp.scave.model.Param}.
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @generated
     */
	public Adapter createParamAdapter() {
        if (paramItemProvider == null) {
            paramItemProvider = new ParamItemProvider(this);
        }

        return paramItemProvider;
    }

    /**
     * This keeps track of the one adapter used for all {@link org.omnetpp.scave.model.ChartSheet} instances.
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @generated
     */
	protected ChartSheetItemProvider chartSheetItemProvider;

    /**
     * This creates an adapter for a {@link org.omnetpp.scave.model.ChartSheet}.
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @generated
     */
	public Adapter createChartSheetAdapter() {
        if (chartSheetItemProvider == null) {
            chartSheetItemProvider = new ChartSheetItemProvider(this);
        }

        return chartSheetItemProvider;
    }

    /**
     * This keeps track of the one adapter used for all {@link org.omnetpp.scave.model.Analysis} instances.
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @generated
     */
	protected AnalysisItemProvider analysisItemProvider;

    /**
     * This creates an adapter for a {@link org.omnetpp.scave.model.Analysis}.
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @generated
     */
	public Adapter createAnalysisAdapter() {
        if (analysisItemProvider == null) {
            analysisItemProvider = new AnalysisItemProvider(this);
        }

        return analysisItemProvider;
    }

    /**
     * This keeps track of the one adapter used for all {@link org.omnetpp.scave.model.Select} instances.
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @generated
     */
	protected SelectItemProvider selectItemProvider;

    /**
     * This creates an adapter for a {@link org.omnetpp.scave.model.Select}.
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @generated
     */
	public Adapter createSelectAdapter() {
        if (selectItemProvider == null) {
            selectItemProvider = new SelectItemProvider(this);
        }

        return selectItemProvider;
    }

    /**
     * This keeps track of the one adapter used for all {@link org.omnetpp.scave.model.Deselect} instances.
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @generated
     */
	protected DeselectItemProvider deselectItemProvider;

    /**
     * This creates an adapter for a {@link org.omnetpp.scave.model.Deselect}.
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @generated
     */
	public Adapter createDeselectAdapter() {
        if (deselectItemProvider == null) {
            deselectItemProvider = new DeselectItemProvider(this);
        }

        return deselectItemProvider;
    }

    /**
     * This keeps track of the one adapter used for all {@link org.omnetpp.scave.model.Inputs} instances.
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @generated
     */
	protected InputsItemProvider inputsItemProvider;

    /**
     * This creates an adapter for a {@link org.omnetpp.scave.model.Inputs}.
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @generated
     */
	public Adapter createInputsAdapter() {
        if (inputsItemProvider == null) {
            inputsItemProvider = new InputsItemProvider(this);
        }

        return inputsItemProvider;
    }

    /**
     * This keeps track of the one adapter used for all {@link org.omnetpp.scave.model.ChartSheets} instances.
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @generated
     */
	protected ChartSheetsItemProvider chartSheetsItemProvider;

    /**
     * This creates an adapter for a {@link org.omnetpp.scave.model.ChartSheets}.
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @generated
     */
	public Adapter createChartSheetsAdapter() {
        if (chartSheetsItemProvider == null) {
            chartSheetsItemProvider = new ChartSheetsItemProvider(this);
        }

        return chartSheetsItemProvider;
    }

    /**
     * This keeps track of the one adapter used for all {@link org.omnetpp.scave.model.Datasets} instances.
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @generated
     */
	protected DatasetsItemProvider datasetsItemProvider;

    /**
     * This creates an adapter for a {@link org.omnetpp.scave.model.Datasets}.
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @generated
     */
	public Adapter createDatasetsAdapter() {
        if (datasetsItemProvider == null) {
            datasetsItemProvider = new DatasetsItemProvider(this);
        }

        return datasetsItemProvider;
    }

    /**
     * This keeps track of the one adapter used for all {@link org.omnetpp.scave.model.InputFile} instances.
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @generated
     */
	protected InputFileItemProvider inputFileItemProvider;

    /**
     * This creates an adapter for a {@link org.omnetpp.scave.model.InputFile}.
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @generated
     */
	public Adapter createInputFileAdapter() {
        if (inputFileItemProvider == null) {
            inputFileItemProvider = new InputFileItemProvider(this);
        }

        return inputFileItemProvider;
    }

    /**
     * This keeps track of the one adapter used for all {@link org.omnetpp.scave.model.Compute} instances.
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @generated
     */
	protected ComputeItemProvider computeItemProvider;

    /**
     * This creates an adapter for a {@link org.omnetpp.scave.model.Compute}.
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @generated
     */
	public Adapter createComputeAdapter() {
        if (computeItemProvider == null) {
            computeItemProvider = new ComputeItemProvider(this);
        }

        return computeItemProvider;
    }

    /**
     * This keeps track of the one adapter used for all {@link org.omnetpp.scave.model.BarChart} instances.
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @generated
     */
	protected BarChartItemProvider barChartItemProvider;

    /**
     * This creates an adapter for a {@link org.omnetpp.scave.model.BarChart}.
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @generated
     */
	public Adapter createBarChartAdapter() {
        if (barChartItemProvider == null) {
            barChartItemProvider = new BarChartItemProvider(this);
        }

        return barChartItemProvider;
    }

    /**
     * This keeps track of the one adapter used for all {@link org.omnetpp.scave.model.LineChart} instances.
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @generated
     */
	protected LineChartItemProvider lineChartItemProvider;

    /**
     * This creates an adapter for a {@link org.omnetpp.scave.model.LineChart}.
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @generated
     */
	public Adapter createLineChartAdapter() {
        if (lineChartItemProvider == null) {
            lineChartItemProvider = new LineChartItemProvider(this);
        }

        return lineChartItemProvider;
    }

    /**
     * This keeps track of the one adapter used for all {@link org.omnetpp.scave.model.HistogramChart} instances.
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @generated
     */
	protected HistogramChartItemProvider histogramChartItemProvider;

    /**
     * This creates an adapter for a {@link org.omnetpp.scave.model.HistogramChart}.
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @generated
     */
	public Adapter createHistogramChartAdapter() {
        if (histogramChartItemProvider == null) {
            histogramChartItemProvider = new HistogramChartItemProvider(this);
        }

        return histogramChartItemProvider;
    }

    /**
     * This returns the root adapter factory that contains this factory.
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @generated
     */
	public ComposeableAdapterFactory getRootAdapterFactory() {
        return parentAdapterFactory == null ? this : parentAdapterFactory.getRootAdapterFactory();
    }

    /**
     * This sets the composed adapter factory that contains this factory.
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @generated
     */
	public void setParentAdapterFactory(ComposedAdapterFactory parentAdapterFactory) {
        this.parentAdapterFactory = parentAdapterFactory;
    }

    /**
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @generated
     */
	public boolean isFactoryForType(Object type) {
        return supportedTypes.contains(type) || super.isFactoryForType(type);
    }

    /**
     * This implementation substitutes the factory itself as the key for the adapter.
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @generated
     */
	public Adapter adapt(Notifier notifier, Object type) {
        return super.adapt(notifier, this);
    }

    /**
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @generated
     */
	public Object adapt(Object object, Object type) {
        if (isFactoryForType(type)) {
            Object adapter = super.adapt(object, type);
            if (!(type instanceof Class) || (((Class)type).isInstance(adapter))) {
                return adapter;
            }
        }

        return null;
    }

    /**
     * This adds a listener.
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @generated
     */
	public void addListener(INotifyChangedListener notifyChangedListener) {
        changeNotifier.addListener(notifyChangedListener);
    }

    /**
     * This removes a listener.
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @generated
     */
	public void removeListener(INotifyChangedListener notifyChangedListener) {
        changeNotifier.removeListener(notifyChangedListener);
    }

    /**
     * This delegates to {@link #changeNotifier} and to {@link #parentAdapterFactory}.
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @generated
     */
	public void fireNotifyChanged(Notification notification) {
        changeNotifier.fireNotifyChanged(notification);

        if (parentAdapterFactory != null) {
            parentAdapterFactory.fireNotifyChanged(notification);
        }
    }

    /**
     * This disposes all of the item providers created by this factory. 
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @generated
     */
	public void dispose() {
        if (datasetItemProvider != null) datasetItemProvider.dispose();
        if (addItemProvider != null) addItemProvider.dispose();
        if (applyItemProvider != null) applyItemProvider.dispose();
        if (exceptItemProvider != null) exceptItemProvider.dispose();
        if (propertyItemProvider != null) propertyItemProvider.dispose();
        if (groupItemProvider != null) groupItemProvider.dispose();
        if (discardItemProvider != null) discardItemProvider.dispose();
        if (paramItemProvider != null) paramItemProvider.dispose();
        if (chartSheetItemProvider != null) chartSheetItemProvider.dispose();
        if (analysisItemProvider != null) analysisItemProvider.dispose();
        if (selectItemProvider != null) selectItemProvider.dispose();
        if (deselectItemProvider != null) deselectItemProvider.dispose();
        if (inputsItemProvider != null) inputsItemProvider.dispose();
        if (chartSheetsItemProvider != null) chartSheetsItemProvider.dispose();
        if (datasetsItemProvider != null) datasetsItemProvider.dispose();
        if (inputFileItemProvider != null) inputFileItemProvider.dispose();
        if (computeItemProvider != null) computeItemProvider.dispose();
        if (barChartItemProvider != null) barChartItemProvider.dispose();
        if (lineChartItemProvider != null) lineChartItemProvider.dispose();
        if (histogramChartItemProvider != null) histogramChartItemProvider.dispose();
    }

}
