/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jel.hardware;

import com.dalsemi.onewire.adapter.DSPortAdapter;
import java.net.MalformedURLException;
import javax.xml.transform.TransformerException;
import jel.hardware.adapter.IDeviceAdapter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.StringWriter;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.List;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import org.apache.log4j.Logger;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import jel.hardware.adapter.AdapterDescription;
import jel.hardware.adapter.event.AdapterEvent;
import jel.hardware.adapter.event.IAdapterAddedListener;
import jel.hardware.adapter.event.IAdapterRemovedListener;
import jel.server.JelException;
import jel.server.JelException.ExceptionReason;
import jel.server.JelException.ExceptionType;
import jel.server.ServerInformationManager;
import jel.utils.FileUtilities;
import jel.utils.JelUtils;
import org.apache.xpath.XPathAPI;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 *
 * @author trycoon
 */
public final class AdapterManager
{

    private static Logger mLogger = Logger.getLogger(AdapterManager.class);
    private static final String HARDWARE_SETTINGS_FILE = "JelHardware.xml";
    private static final String HARDWARE_SETTINGS_SCHEMA = "JelHardware.xsd";
    public static final String ADAPTER_CATALOG = "adapters";
    private static final String ADAPTER_SUFFIX = "adp";
    private static File mHardwareSchemaFile;
    private static File mHardwareSettingsFile;
    private static List<IDeviceAdapter> mAdapters;
    private List<IAdapterAddedListener> mAdapterAddedListeners = new ArrayList<IAdapterAddedListener>();
    private List<IAdapterRemovedListener> mAdapterRemovedListeners = new ArrayList<IAdapterRemovedListener>();


    /**
     * Creates schema-file, adapter-catalog, settings-file(if one does not exists) and validates settings-file.
     * @throws java.io.IOException When loading/creating files or creating catalogs.
     */
    public void installAdapters() throws IOException {
        mHardwareSchemaFile = new File(ServerInformationManager.getJelHome() + ServerInformationManager.getFileSeparator() + HARDWARE_SETTINGS_SCHEMA);
        mHardwareSettingsFile = new File(ServerInformationManager.getJelHome() + ServerInformationManager.getFileSeparator() + HARDWARE_SETTINGS_FILE);

        // Always save a new one so that users don´t try to create their own.
        mLogger.debug("Creating hardware schema-file.");
        createNewSchemaFile();

        if (!mHardwareSettingsFile.exists()) {
            mLogger.info("Hardware settings-file not found, creating a new one.");
            createNewSettingsFile();
        }

        if (!validateSettingsFile()) {
            throw new JelException("Hardware settingsfile, " + mHardwareSettingsFile.getAbsoluteFile() + ", contains errors. Correct errors or remove file to have a new empty one created.", ExceptionReason.INCORRECT_SETTINGS, ExceptionType.WARN);
        }
        mLogger.debug("Hardware settings-file looking valid.");

        String catalog = ServerInformationManager.getJelHome() + ServerInformationManager.getFileSeparator() + ADAPTER_CATALOG;

        if (!new File(catalog).exists()) {
            if (!new File(catalog).mkdirs()) {
                throw new JelException("Could not create directory " + catalog, ExceptionReason.SERVER_ERROR, ExceptionType.ERROR);
            }
        }
    }


    /**
     * Returns a list of available adapters. These adapters are supported and loaded by the system, but it does'nt mean that they are physically pressent for the moment.
     * @return
     */
    public List<AdapterDescription> getAvailableAdapters() {
        List<AdapterDescription> adapterList = new ArrayList<AdapterDescription>();

        // Add built-in adapters first.
        for (Enumeration<DSPortAdapter> adapter_enum = com.dalsemi.onewire.OneWireAccessProvider.enumerateAllAdapters(); adapter_enum.hasMoreElements();) {
            DSPortAdapter adapter = adapter_enum.nextElement();
            AdapterDescription description = new AdapterDescription();

            List<String> possiblePorts = new ArrayList<String>();
            for (Enumeration<String> port_enum = adapter.getPortNames(); port_enum.hasMoreElements();) {
                possiblePorts.add(port_enum.nextElement());
            }

            description.setName(adapter.getAdapterName());
            description.setVersion(com.dalsemi.onewire.OneWireAccessProvider.getVersion());
            description.setPossiblePorts(possiblePorts.toArray(new String[possiblePorts.size()]));

            adapterList.add(description);
        }
        // End adding builtin adapters.

        List<IDeviceAdapter> fileSystemAdapters = getFilesystemAdapters();

        for (IDeviceAdapter adapter : fileSystemAdapters) {
            AdapterDescription description = new AdapterDescription();

            description.setName(adapter.getName());
            description.setVersion(adapter.getVersion());
            //description.setPossiblePorts(adapter.getPossiblePorts());

            adapterList.add(description);
        }

        mLogger.debug("Total number of adapters found " + adapterList.size() + ".");

        return adapterList;
    }


    private List<IDeviceAdapter> getFilesystemAdapters() {
        List<IDeviceAdapter> adapterList = new ArrayList<IDeviceAdapter>();

        try {
            mLogger.debug("Fetching list of filesystem-adapters...");

            File adapterCatalog = new File(ServerInformationManager.getJelHome() + ServerInformationManager.getFileSeparator() + ADAPTER_CATALOG);

            // Dynamic class loading.
            // TODO: http://java.sun.com/developer/JDCTechTips/2003/tt0819.html

            if (adapterCatalog.exists()) {
                String[] adapterFilesList = adapterCatalog.list(new FilenameFilter()
                {

                    public boolean accept(File dir, String name) {
                        return name.endsWith("." + ADAPTER_SUFFIX);
                    }

                });

                if (adapterFilesList != null) {
                    mLogger.debug("Found " + adapterFilesList.length + " filesystem-adapters.");

                    for (String adapterFileName : adapterFilesList) {
                        String name = adapterFileName.split(".")[0];

                        ClassLoader loader = null;
                        try {
                            loader = new URLClassLoader(new URL[]{new URL(name)});
                            Class adapterClass = loader.loadClass(name);

                            Object instance;
                            try {
                                instance = adapterClass.newInstance();

                                if (IDeviceAdapter.class.isInstance(instance)) {
                                    IDeviceAdapter adapter = IDeviceAdapter.class.cast(instance);

                                    adapterList.add(adapter);
                                    mLogger.debug("Added filesystem-adapter \"" + name + "\"");
                                }
                            } catch (InstantiationException exception) {
                                mLogger.error("Failed to create an instance of adapter \"" + name + "\".", exception);
                            } catch (IllegalAccessException exception) {
                                mLogger.error("Failed to access adapter \"" + name + "\".", exception);
                            }
                        } catch (ClassNotFoundException exception) {
                            mLogger.error("Could not find specified adapter(" + name + ")", exception);
                        } catch (MalformedURLException exception) {
                            mLogger.error("Could not find url to specified adapter(" + name + ")", exception);
                        }
                    }
                }
            }
        } catch (NullPointerException exception) {
            mLogger.error("Failed to get list of available adapters from filesystem.", exception);
        }

        return adapterList;
    }


    public synchronized List<AdapterDescription> getUsedAdapters() {
        List<AdapterDescription> descriptions = new ArrayList<AdapterDescription>();

        if (mAdapters == null) {
            throw new JelException("Adapterlist not loaded, initialize adaptermanager first!", ExceptionReason.SERVER_ERROR, ExceptionType.ERROR);
        }

        for (IDeviceAdapter adapter : mAdapters) {
            AdapterDescription description = new AdapterDescription();

            description.setName(adapter.getName());
            description.setPossiblePorts(adapter.getPossiblePorts());
            description.setSelectedPort(adapter.getSelectedPort());
            description.setVersion(adapter.getVersion());
            description.setIsPressent(adapter.isPressent());

            descriptions.add(description);
        }

        return descriptions;
    }


    /**
     * Load and setup list of adapters that user has specified to be used. This however does'nt mean that they are physically pressent for the moment.
     */
    public synchronized void loadAdapters() {
        List<IDeviceAdapter> usedAdapters = new ArrayList<IDeviceAdapter>();

        if (validateSettingsFile()) {
            try {
                DocumentBuilderFactory xmlFactory = DocumentBuilderFactory.newInstance();
                //xmlFactory.setNamespaceAware(true);
                DocumentBuilder documentBuilder = xmlFactory.newDocumentBuilder();
                Document document = documentBuilder.parse(mHardwareSettingsFile);
                NodeList adapterNodes = XPathAPI.selectNodeList(document, "//adapters/adapter");

                for (int index = 0; index < adapterNodes.getLength(); index++) {
                    Node adapterNode = adapterNodes.item(index);

                    NamedNodeMap attributes = adapterNode.getAttributes();

                    String name = attributes.getNamedItem("name").getNodeValue();
                    String type = attributes.getNamedItem("type").getNodeValue();
                    String port = attributes.getNamedItem("port").getNodeValue();

                    if (!JelUtils.isNullOrEmpty(name) && !JelUtils.isNullOrEmpty(type) && !JelUtils.isNullOrEmpty(port)) {

                        IDeviceAdapter newAdapter = getAdapterFromType(type);

                        if (newAdapter != null) {
                            newAdapter.setName(name);
                            newAdapter.setSelectedPort(port);

                            if (isAdapterPortAlreadyUsed(usedAdapters, newAdapter)) {
                                mLogger.warn("Port(" + port + ") is already occupied. Skipping adapter(" + name + "), and please remove duplicate from settings-file!");
                            } else {
                                usedAdapters.add(newAdapter);
                                mLogger.debug("Adapter \"" + name + "\" of type \"" + type + "\" successfully loaded.");
                            }
                        } else {
                            mLogger.warn("Not an valid adapter \"" + type + "\", skipping adapter.");
                        }
                    }
                }
            } catch (TransformerException exception) {
                throw new JelException("Failed to get list of used adapters. Please check hardware settingsfile, " + mHardwareSettingsFile.getAbsoluteFile() + ".", ExceptionReason.INCORRECT_SETTINGS, ExceptionType.WARN, exception);
            } catch (SAXException exception) {
                throw new JelException("Failed to get list of used adapters. Please check hardware settingsfile, " + mHardwareSettingsFile.getAbsoluteFile() + ".", ExceptionReason.INCORRECT_SETTINGS, ExceptionType.WARN, exception);
            } catch (IOException exception) {
                throw new JelException("Failed to get list of used adapters. Please check hardware settingsfile, " + mHardwareSettingsFile.getAbsoluteFile() + ".", ExceptionReason.INCORRECT_SETTINGS, ExceptionType.WARN, exception);
            } catch (ParserConfigurationException exception) {
                throw new JelException("Failed to get list of used adapters. Please check hardware settingsfile, " + mHardwareSettingsFile.getAbsoluteFile() + ".", ExceptionReason.INCORRECT_SETTINGS, ExceptionType.WARN, exception);
            }
        } else {
            throw new JelException("Hardware settingsfile, " + mHardwareSettingsFile.getAbsoluteFile() + ", contains errors. Correct errors or remove file to have a new empty one created.", ExceptionReason.INCORRECT_SETTINGS, ExceptionType.WARN);
        }

        mAdapters = usedAdapters;
    }


    public synchronized void initUsedAdapters() {
        if (mAdapters == null) {
            throw new JelException("Adapters must be loaded before they are initialized.", ExceptionReason.SERVER_ERROR, ExceptionType.ERROR);
        }

        for (int index = 0; index < mAdapters.size(); index++) {
            IDeviceAdapter adapter = mAdapters.get(index);
            try {
                mLogger.debug("Initializing adapter \"" + adapter.getName() + "\" on port \"" + adapter.getSelectedPort() + "\".");
                adapter.init();
            } catch (Throwable exception) {
                try {
                    mLogger.warn("Failed to initialize adapter(name=\"" + adapter.getName() + "\", port=\"" + adapter.getSelectedPort() + "\"), ignoring adapter.", exception);
                } catch (Throwable exception2) { // Ignore these errors
                }
                mAdapters.remove(adapter);
                --index;
            }
        }
    }


    public synchronized void shutdownUsedAdapters() {
        if (mAdapters != null) {
            for (IDeviceAdapter adapter : mAdapters) {
                try {
                    mLogger.debug("Shuting down adapter \"" + adapter.getName() + "\" on port \"" + adapter.getSelectedPort() + "\".");
                    adapter.shutdown();
                } catch (Throwable exception) {
                    try {
                        mLogger.warn("Failed to shut down adapter(name=\"" + adapter.getName() + "\", port=\"" + adapter.getSelectedPort() + "\"), ignoring adapter.", exception);
                    } catch (Throwable exception2) { // Ignore these errors
                    }
                }
            }
            mAdapters.clear();
        }
    }


    private boolean isAdapterPortAlreadyUsed(List<IDeviceAdapter> existingAdapters, IDeviceAdapter newAdapter) {
        if (existingAdapters != null && existingAdapters.size() > 0 && newAdapter != null) {
            for (IDeviceAdapter testAdapter : existingAdapters) {
                if (newAdapter.getSelectedPort().equals(testAdapter.getSelectedPort())) {
                    return true;
                }

            }
        }

        return false;
    }


    private void createNewSchemaFile() throws FileNotFoundException, IOException {
        FileUtilities.copyFile(this.getClass().getClassLoader().getResourceAsStream("jel/hardware/" + HARDWARE_SETTINGS_SCHEMA), ServerInformationManager.getJelHome() + ServerInformationManager.getFileSeparator() + HARDWARE_SETTINGS_SCHEMA);
    }


    private void createNewSettingsFile() throws FileNotFoundException, IOException {
        FileUtilities.copyFile(this.getClass().getClassLoader().getResourceAsStream("jel/hardware/" + HARDWARE_SETTINGS_FILE), ServerInformationManager.getJelHome() + ServerInformationManager.getFileSeparator() + HARDWARE_SETTINGS_FILE);
    }


    private boolean validateSettingsFile() {
        SchemaFactory factory = SchemaFactory.newInstance("http://www.w3.org/2001/XMLSchema");

        Schema schema = null;
        try {
            schema = factory.newSchema(mHardwareSchemaFile);
        } catch (SAXException exception) {
            mLogger.error("Failed to parse hardwaresettings schemafile \"" + mHardwareSchemaFile.getName() + "\"", exception);
            return false;
        }

        Validator validator = schema.newValidator();
        DocumentBuilderFactory domFactory = DocumentBuilderFactory.newInstance();
        domFactory.setNamespaceAware(true); // never forget this

        DocumentBuilder builder = null;
        try {
            builder = domFactory.newDocumentBuilder();
        } catch (ParserConfigurationException exception) {
            mLogger.error("Failed to create an documentbuilder when parsing hardwaresettings-file \"" + mHardwareSettingsFile.getName() + "\"", exception);
            return false;
        }

        Document doc = null;
        try {
            doc = builder.parse(mHardwareSettingsFile);
        } catch (SAXException exception) {
            mLogger.error("An error occured when trying to parse file \"" + mHardwareSettingsFile.getName() + "\"", exception);
            return false;
        } catch (IOException exception) {
            mLogger.error("An error occured when trying to parse file \"" + mHardwareSettingsFile.getName() + "\"", exception);
            return false;
        }

        DOMSource source = new DOMSource(doc);
        DOMResult result = new DOMResult();

        try {
            validator.validate(source, result);
            Document augmented = (Document) result.getNode();
            return true;

        } catch (IOException exception) {
            mLogger.error("An error occured when trying to validate file \"" + mHardwareSettingsFile.getName() + "\"", exception);
        } catch (SAXException exception) {
            mLogger.error("Hardwaresettings-file did not pass validation, please correct errors in file \"" + mHardwareSettingsFile.getName() + "\"", exception);
        }

        return false;
    }


    private IDeviceAdapter getAdapterFromType(String typeName) {
        if (JelUtils.isNullOrEmpty(typeName)) {
            return null;
        }

        //
        // http://docstore.mik.ua/orelly/java-ent/jnut/ch04_07.htm
        // http://en.wikibooks.org/wiki/Java_Programming/Reflection/Dynamic_Class_Loading
        //
        // Dynamic class loading.
        // TODO: http://java.sun.com/developer/JDCTechTips/2003/tt0819.html

        try {
            //TODO: Make this one better so that we could support adapters added to the "adapters"-catalog.
            Class adapterClass = Class.forName("jel.hardware.adapter." + typeName);
            Object instance = adapterClass.newInstance();
            if (IDeviceAdapter.class.isInstance(instance)) {
                IDeviceAdapter adapter = IDeviceAdapter.class.cast(instance);

                return adapter;
            }
        } catch (InstantiationException exception) {
            mLogger.error("Not an valid adapter \"" + typeName + "\", skipping adapter.", exception);
        } catch (IllegalAccessException exception) {
            mLogger.error("Not an valid adapter \"" + typeName + "\", skipping adapter.", exception);
        } catch (ExceptionInInitializerError exception) {
            mLogger.error("Failed to initialize instance of adapter \"" + typeName + "\", skipping adapter.", exception);
        } catch (LinkageError exception) {
            mLogger.error("Failed to link instance of adapter \"" + typeName + "\", skipping adapter.", exception);
        } catch (ClassNotFoundException exception) {
            mLogger.error("Could not find any adapter of type \"" + typeName + "\", skipping adapter.");
        }

        return null;
    }


    public synchronized AdapterDescription addUsedAdapter(String name, String selectedPort) {
        //TODO:
        // Check if adapter already exists.
        // Initialize adapter.
        // Add adapter to mAdapters.
        // notifyAddedAdapter(adapter);
        return null; //TODO: return adapter
    }


    public synchronized void removeUsedAdapter(String name, String selectedPort) {
        //TODO: remove adapter.
        if (JelUtils.isNullOrEmpty(name) && JelUtils.isNullOrEmpty(selectedPort)) {
            throw new JelException("Adapter could not be removed, insufficient number of parameters provided.", ExceptionReason.WRONG_PARAMETERS, ExceptionType.INFO);
        }

        IDeviceAdapter selectedAdapter = null;

        for (IDeviceAdapter adapter : mAdapters) {
            if (name.equals(adapter.getName()) && selectedPort.equals(adapter.getSelectedPort())) {
                selectedAdapter = adapter;
                break;
            }
        }

        if (selectedAdapter == null) {
            throw new JelException("No used adapter with specified name could be found.", ExceptionReason.WRONG_PARAMETERS, ExceptionType.INFO);
        }

        mAdapters.remove(selectedAdapter);
        selectedAdapter.shutdown();
        notifyRemovedAdapter(selectedAdapter);

        saveAdapterList();
    }


    private void saveAdapterList() {
        try {
            //mLogger.info("Saving drivers settings to \"" + DRIVER_SETTINGS + "\".");
            DocumentBuilderFactory xmlFactory = DocumentBuilderFactory.newInstance();
            //xmlFactory.setNamespaceAware(true);
            DocumentBuilder documentBuilder = xmlFactory.newDocumentBuilder();
            Document document = documentBuilder.parse(mHardwareSettingsFile);
            NodeList oldAdapters = document.getElementsByTagName("adapter");
            System.out.println("nodes before:" + oldAdapters.getLength());
            // Remove old adapters first...
            while (oldAdapters.getLength() > 0) {
                Element oldAdapter = (Element) oldAdapters.item(0);
                oldAdapter.getParentNode().removeChild(oldAdapter);
            }

            Element adaptersElement = (Element) XPathAPI.selectSingleNode(document, "//adapters");

            // Then add new adapters.
            for (IDeviceAdapter newAdapter : mAdapters) {
                Element newNode = document.createElement("adapter");
                newNode.setAttribute("name", newAdapter.getName());
                newNode.setAttribute("port", newAdapter.getSelectedPort());

                adaptersElement.appendChild(newNode);
            }
            //document.normalize();
            System.out.println("nodes after2:" + adaptersElement.getChildNodes().getLength());
            // Save to file.

            Transformer transformer = TransformerFactory.newInstance().newTransformer();
            //  transformer.setOutputProperty(OutputKeys.INDENT, "yes");

//initialize StreamResult with File object to save to file
            StreamResult result = new StreamResult(new StringWriter());
            DOMSource source = new DOMSource(document);
            transformer.transform(source, result);

            String xmlString = result.getWriter().toString();
            System.out.println(xmlString);


        /* DOMSource source = new DOMSource(document);
        mHardwareSettingsFile = new File("test.xml");
        StreamResult result = new StreamResult(mHardwareSettingsFile);
        transformer.transform(source, result);*/

        } catch (Exception exception) {
            System.err.print(exception);
        }
    }

    /////////////////////////////////////////////////////////////////////
    /// EVENTLISTNERS ///////////////////////////////////////////////////
    /////////////////////////////////////////////////////////////////////

    public void addAddedAdapterListener(IAdapterAddedListener listener) {
        if (listener != null && !mAdapterAddedListeners.contains(listener)) {
            mAdapterAddedListeners.add(listener);
        }
    }


    public void removeAddedAdapterListener(IAdapterAddedListener listener) {
        mAdapterAddedListeners.remove(listener);
    }


    private void notifyAddedAdapter(IDeviceAdapter adapter) {
        try {
            for (IAdapterAddedListener listener : mAdapterAddedListeners) {
                listener.adapterAdded(new AdapterEvent(this, adapter, new Date()));
            }
        } catch (Throwable exception) { /* Ignore */ }
    }


    public void addRemovedAdapterListener(IAdapterRemovedListener listener) {
        if (listener != null && !mAdapterRemovedListeners.contains(listener)) {
            mAdapterRemovedListeners.add(listener);
        }
    }


    public void removeRevmoedAdapterListener(IAdapterRemovedListener listener) {
        mAdapterRemovedListeners.remove(listener);
    }


    private void notifyRemovedAdapter(IDeviceAdapter adapter) {
        try {
            for (IAdapterRemovedListener listener : mAdapterRemovedListeners) {
                listener.adapterRemoved(new AdapterEvent(this, adapter, new Date()));
            }
        } catch (Throwable exception) { /* Ignore */ }
    }
    /////////////////////////////////////////////////////////////////////
    /// EVENTLISTENERS END //////////////////////////////////////////////
    /////////////////////////////////////////////////////////////////////
}