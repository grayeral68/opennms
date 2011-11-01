/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2010-2011 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2011 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.protocols.xml.collector;

import java.io.File;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import org.apache.regexp.RE;
import org.opennms.core.utils.BeanUtils;
import org.opennms.core.utils.ParameterMap;
import org.opennms.core.utils.ThreadCategory;
import org.opennms.netmgt.collectd.CollectionAgent;
import org.opennms.netmgt.collectd.CollectionException;
import org.opennms.netmgt.collectd.ServiceCollector;
import org.opennms.netmgt.config.collector.AttributeGroupType;
import org.opennms.netmgt.config.collector.CollectionSet;
import org.opennms.netmgt.model.RrdRepository;
import org.opennms.netmgt.model.events.EventProxy;
import org.opennms.protocols.sftp.SftpUrlConnection;
import org.opennms.protocols.sftp.SftpUrlFactory;
import org.opennms.protocols.xml.config.XmlDataCollection;
import org.opennms.protocols.xml.config.XmlGroup;
import org.opennms.protocols.xml.config.XmlObject;
import org.opennms.protocols.xml.config.XmlSource;
import org.opennms.protocols.xml.dao.XmlDataCollectionConfigDao;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * The Class XmlCollector.
 * 
 * @author <a href="mailto:agalue@opennms.org">Alejandro Galue</a>
 */
public class XmlCollector implements ServiceCollector {

    /** The XML Data Collection DAO. */
    private XmlDataCollectionConfigDao m_xmlCollectionDao;

    /** The XML attribute type Map. */
    private HashMap<String, XmlCollectionAttributeType> m_attribTypeList = new HashMap<String, XmlCollectionAttributeType>();

    /**
     * Gets the XML Data Collection DAO.
     *
     * @return the XML Data Collection DAO
     */
    public XmlDataCollectionConfigDao getXmlCollectionDao() {
        return m_xmlCollectionDao;
    }

    /**
     * Sets the XML Data Collection DAO.
     *
     * @param xmlCollectionDao the new XML Data Collection DAO
     */
    public void setXmlCollectionDao(XmlDataCollectionConfigDao xmlCollectionDao) {
        m_xmlCollectionDao = xmlCollectionDao;
    }

    /**
     * Log.
     *
     * @return the thread category
     */
    private ThreadCategory log() {
        return ThreadCategory.getInstance(getClass());
    }

    /**
     * Load attributes.
     *
     * @param collection the collection
     */
    private void loadAttributes(XmlDataCollection collection) {
        for (XmlSource source : collection.getXmlSources()) {
            for (XmlGroup group : source.getXmlGroups()) {
                AttributeGroupType attribGroupType = new AttributeGroupType(group.getName(), group.getIfType());
                for (XmlObject object : group.getXmlObjects()) {
                    XmlCollectionAttributeType attribType = new XmlCollectionAttributeType(object, attribGroupType);
                    m_attribTypeList.put(object.getName(), attribType);
                }
            }
        }
    }

    /* (non-Javadoc)
     * @see org.opennms.netmgt.collectd.ServiceCollector#initialize(java.util.Map)
     */
    @Override
    public void initialize(Map<String, String> parameters) {
        log().debug("initialize: Initializing XML Collector.");
        initialize();
    }

    /* (non-Javadoc)
     * @see org.opennms.netmgt.collectd.ServiceCollector#initialize(org.opennms.netmgt.collectd.CollectionAgent, java.util.Map)
     */
    @Override
    public void initialize(CollectionAgent agent, Map<String, Object> parameters) {        
        log().debug("initialize: Initializing XML collection for agent: " + agent);
        initialize();
    }

    /**
     * Initialize.
     */
    private void initialize() {
        // Retrieve the DAO for our configuration file.
        if (m_xmlCollectionDao == null)
            m_xmlCollectionDao = BeanUtils.getBean("daoContext", "xmlDataCollectionConfigDao", XmlDataCollectionConfigDao.class);

        // If the RRD file repository directory does NOT already exist, create it.
        log().debug("initializeRrdRepository: Initializing RRD repo from XmlCollector...");
        File f = new File(m_xmlCollectionDao.getConfig().getRrdRepository());
        if (!f.isDirectory()) {
            if (!f.mkdirs()) {
                throw new RuntimeException("Unable to create RRD file repository.  Path doesn't already exist and could not make directory: " + m_xmlCollectionDao.getConfig().getRrdRepository());
            }
        }
        try {
            new URL("sftp://localhost/");
        } catch (MalformedURLException e) {
            log().info("Registering SFTP protocol");
            URL.setURLStreamHandlerFactory(new SftpUrlFactory());
        }
    }

    /* (non-Javadoc)
     * @see org.opennms.netmgt.collectd.ServiceCollector#release()
     */
    @Override
    public void release() {
    }

    /* (non-Javadoc)
     * @see org.opennms.netmgt.collectd.ServiceCollector#release(org.opennms.netmgt.collectd.CollectionAgent)
     */
    @Override
    public void release(CollectionAgent agent) {
    }

    /* (non-Javadoc)
     * @see org.opennms.netmgt.collectd.ServiceCollector#collect(org.opennms.netmgt.collectd.CollectionAgent, org.opennms.netmgt.model.events.EventProxy, java.util.Map)
     */
    public CollectionSet collect(CollectionAgent agent, EventProxy eproxy, Map<String, Object> parameters) throws CollectionException {
        // Create a new collection set.
        XmlCollectionSet collectionSet = new XmlCollectionSet(agent);
        collectionSet.setCollectionTimestamp(new Date());
        collectionSet.setStatus(ServiceCollector.COLLECTION_UNKNOWN);

        if (parameters == null) {
            log().error("Null parameters is now allowed in XML Collector!!");
            return collectionSet;
        }

        // FIXME Instantiate the parser and execute it
        try {            
            String collectionName = ParameterMap.getKeyedString(parameters, "collection", null);
            if (collectionName == null) {
                //Look for the old configuration style:
                collectionName = ParameterMap.getKeyedString(parameters, "xml-collection", null);
            }

            log().debug("collect: collecting XML data using collection " + collectionName);
            XmlDataCollection collection = m_xmlCollectionDao.getDataCollectionByName(collectionName);

            // Load the attribute group types.
            loadAttributes(collection);

            for (XmlSource source : collection.getXmlSources()) {
                // Retrieve the XML data
                Document doc = getXmlDocument(agent, source);

                // Cycle through all of the queries for this collection
                XPath xpath = XPathFactory.newInstance().newXPath();
                for (XmlGroup group : source.getXmlGroups()) {
                    log().debug("collect: getting resources for XML group " + group.getName() + " using XPATH " + group.getResourceXpath());
                    NodeList resourceList = (NodeList) xpath.evaluate(group.getResourceXpath(), doc, XPathConstants.NODESET);
                    for (int j = 0; j < resourceList.getLength(); j++) {
                        Node resource = resourceList.item(j);
                        Node resourceName = (Node) xpath.evaluate(group.getKeyXpath(), resource, XPathConstants.NODE);
                        log().debug("collect: processing XML resource " + resourceName);
                        XmlCollectionResource collectionResource = getCollectionResource(agent, resourceName.getNodeValue(), group.getResourceType());
                        for (XmlObject object : group.getXmlObjects()) {
                            String value = (String) xpath.evaluate(object.getXpath(), resource, XPathConstants.STRING);
                            collectionResource.setAttributeValue(m_attribTypeList.get(object.getName()), value);
                        }
                        collectionSet.getCollectionResources().add(collectionResource);
                    }
                }
            }
            collectionSet.setStatus(ServiceCollector.COLLECTION_SUCCEEDED);
            return collectionSet;
        } catch (Exception e) {
            collectionSet.setStatus(ServiceCollector.COLLECTION_FAILED);
            throw new CollectionException("Can't collect XML data.", e);
        }
    }

    /**
     * Gets the collection resource.
     *
     * @param agent the collection agent
     * @param instance the resource instance
     * @param resourceType the resource type
     * @return the collection resource
     */
    private XmlCollectionResource getCollectionResource(CollectionAgent agent, String instance, String resourceType) {
        XmlCollectionResource resource = null;
        if (resourceType.toLowerCase().equals("node")) {
            resource = new XmlSingleInstanceCollectionResource(agent);
        } else {
            resource = new XmlMultiInstanceCollectionResource(agent, instance, resourceType);
        }
        return resource;
    }

    /* (non-Javadoc)
     * @see org.opennms.netmgt.collectd.ServiceCollector#getRrdRepository(java.lang.String)
     */
    public RrdRepository getRrdRepository(String collectionName) {
        return m_xmlCollectionDao.getConfig().buildRrdRepository(collectionName);
    }

    /**
     * Gets the XML document.
     *
     * @param agent the agent
     * @param source the source
     * @return the XML document
     */
    public Document getXmlDocument(CollectionAgent agent, XmlSource source) {
        // The URL format could have some variables that will be replaced online to support dynamic URLs with timestamps.
        HashMap<String, String> translationMap = new HashMap<String, String>();
        translationMap.put("{ipaddr}", agent.getHostAddress());
        String urlStr = source.getUrl();
        for (Map.Entry<String, String> translation : translationMap.entrySet()) {
            RE re = new RE(translation.getKey());
            urlStr = re.subst(urlStr, translation.getValue());
        }
        // Create Document
        try {
            URL url = new URL(urlStr);
            URLConnection c = url.openConnection();
            c.connect();
            InputStream is = c.getInputStream();
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setIgnoringComments(true);
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(is);
            if (c instanceof SftpUrlConnection) // We need to be sure to close the connections for SFTP
                ((SftpUrlConnection)c).disconnect();
            return doc;
        } catch (Exception e) {
            throw new XmlCollectorException("Can't retrieve data from " + urlStr);
        }
    }

}
