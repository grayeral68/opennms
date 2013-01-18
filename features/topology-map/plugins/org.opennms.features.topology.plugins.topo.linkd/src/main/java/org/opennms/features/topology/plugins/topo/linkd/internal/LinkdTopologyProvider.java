/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2012 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2012 The OpenNMS Group, Inc.
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

package org.opennms.features.topology.plugins.topo.linkd.internal;

import java.io.File;
import java.net.MalformedURLException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.bind.JAXB;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.opennms.features.topology.api.SimpleLeafVertex;
import org.opennms.features.topology.api.topo.AbstractEdge;
import org.opennms.features.topology.api.topo.AbstractTopologyProvider;
import org.opennms.features.topology.api.topo.AbstractVertex;
import org.opennms.features.topology.api.topo.Edge;
import org.opennms.features.topology.api.topo.EdgeProvider;
import org.opennms.features.topology.api.topo.GraphProvider;
import org.opennms.features.topology.api.topo.SimpleEdgeProvider;
import org.opennms.features.topology.api.topo.SimpleVertexProvider;
import org.opennms.features.topology.api.topo.Vertex;
import org.opennms.features.topology.api.topo.VertexProvider;
import org.opennms.features.topology.api.topo.WrappedEdge;
import org.opennms.features.topology.api.topo.WrappedGraph;
import org.opennms.features.topology.api.topo.WrappedGroup;
import org.opennms.features.topology.api.topo.WrappedLeafVertex;
import org.opennms.features.topology.api.topo.WrappedVertex;
import org.opennms.netmgt.dao.DataLinkInterfaceDao;
import org.opennms.netmgt.dao.IpInterfaceDao;
import org.opennms.netmgt.dao.NodeDao;
import org.opennms.netmgt.dao.SnmpInterfaceDao;
import org.opennms.netmgt.model.DataLinkInterface;
import org.opennms.netmgt.model.OnmsIpInterface;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.model.OnmsSnmpInterface;
import org.slf4j.LoggerFactory;

public class LinkdTopologyProvider extends AbstractTopologyProvider implements GraphProvider {

    public static final String TOPOLOGY_NAMESPACE_LINKD = "nodes";
    private static final String LINKD_GROUP_ID_PREFIX = "linkdg";
    public static final String GROUP_ICON_KEY = "linkd:group";
    public static final String SERVER_ICON_KEY = "linkd:system";
    
    private static final String HTML_TOOLTIP_TAG_OPEN = "<p>";
    private static final String HTML_TOOLTIP_TAG_END  = "</p>";
    /**
     * Always print at least one digit after the decimal point,
     * and at most three digits after the decimal point.
     */
    private static final DecimalFormat s_oneDigitAfterDecimal = new DecimalFormat("0.0##");
    
    /**
     * Print no digits after the decimal point (heh, nor a decimal point).
     */
    private static final DecimalFormat s_noDigitsAfterDecimal = new DecimalFormat("0");

    /**
     * Do not use directly. Call {@link #getNodeStatusMap 
     * getInterfaceStatusMap} instead.
     */
    private static final Map<Character, String> m_nodeStatusMap;

    static {
        m_nodeStatusMap = new HashMap<Character, String>();
        m_nodeStatusMap.put('A', "Active");
        m_nodeStatusMap.put(' ', "Unknown");
        m_nodeStatusMap.put('D', "Deleted");                        
    }
    
     static final String[] OPER_ADMIN_STATUS = new String[] {
        "&nbsp;",          //0 (not supported)
        "Up",              //1
        "Down",            //2
        "Testing",         //3
        "Unknown",         //4
        "Dormant",         //5
        "NotPresent",      //6
        "LowerLayerDown"   //7
      };

    private boolean addNodeWithoutLink = false;
    
    private DataLinkInterfaceDao m_dataLinkInterfaceDao;
    
    private NodeDao m_nodeDao;
    
    private SnmpInterfaceDao m_snmpInterfaceDao;

    private IpInterfaceDao m_ipInterfaceDao;

    private String m_configurationFile;

//    private TransactionOperations m_transactionTemplate;
    
    public String getConfigurationFile() {
        return m_configurationFile;
    }

    public void setConfigurationFile(String configurationFile) {
        m_configurationFile = configurationFile;
    }

    public SnmpInterfaceDao getSnmpInterfaceDao() {
        return m_snmpInterfaceDao;
    }

    public void setSnmpInterfaceDao(SnmpInterfaceDao snmpInterfaceDao) {
        m_snmpInterfaceDao = snmpInterfaceDao;
    }

    public NodeDao getNodeDao() {
        return m_nodeDao;
    }

    public void setNodeDao(NodeDao nodeDao) {
        m_nodeDao = nodeDao;
    }
    
    public boolean isAddNodeWithoutLink() {
        return addNodeWithoutLink;
    }

    public void setAddNodeWithoutLink(boolean addNodeWithoutLink) {
        this.addNodeWithoutLink = addNodeWithoutLink;
    }

    public DataLinkInterfaceDao getDataLinkInterfaceDao() {
        return m_dataLinkInterfaceDao;
    }

    public void setDataLinkInterfaceDao(DataLinkInterfaceDao dataLinkInterfaceDao) {
        m_dataLinkInterfaceDao = dataLinkInterfaceDao;
    }

    /**
     * Used as an init-method in the OSGi blueprint
     * @throws JAXBException 
     * @throws MalformedURLException 
     */
    public void onInit() throws MalformedURLException, JAXBException {
        log("init: loading topology v1.3");
        loadtopology();
    }
    
    public LinkdTopologyProvider() {
    	super(TOPOLOGY_NAMESPACE_LINKD);
    }

    @Override
    public void load(String filename) throws MalformedURLException, JAXBException {
        if (filename == null) {
            loadtopology();
        } else {
            loadfromfile(filename);
        }
    }

    private static WrappedGraph getGraphFromFile(File file) throws JAXBException, MalformedURLException {
        JAXBContext jc = JAXBContext.newInstance(WrappedGraph.class);
        Unmarshaller u = jc.createUnmarshaller();
        return (WrappedGraph) u.unmarshal(file.toURI().toURL());
    }

    private void loadfromfile(String filename) throws MalformedURLException, JAXBException {
        File file = new File(filename);
        if (file.exists() && file.canRead()) {
            WrappedGraph graph = getGraphFromFile(file);
            addVertices(graph.m_vertices.toArray(new Vertex[0]));
            addEdges(graph.m_edges.toArray(new Edge[0]));
        }
    }

    //@Transactional
    private void loadtopology() throws MalformedURLException, JAXBException {
        log("loadtopology: Clear " + VertexProvider.class.getSimpleName());
        clearVertices();
        log("loadtopology: Clear " + EdgeProvider.class.getSimpleName());
        clearVertices();

        Map<String, Vertex> vertexes = new HashMap<String,Vertex>();
        List<Edge> edges = new ArrayList<Edge>();
        for (DataLinkInterface link: m_dataLinkInterfaceDao.findAll()) {
            log("loadtopology: parsing link: " + link.getDataLinkInterfaceId());

            OnmsNode node = m_nodeDao.get(link.getNode().getId());
            //OnmsNode node = link.getNode();
            log("loadtopology: found node: " + node.getLabel());
            String sourceId = node.getNodeId();
            Vertex source;
            if ( vertexes.containsKey(sourceId)) {
                source = vertexes.get(sourceId);
            } else {
                log("loadtopology: adding source as vertex: " + node.getLabel());
                source = getVertex(node);
                vertexes.put(sourceId, source);
            }

            OnmsNode parentNode = m_nodeDao.get(link.getNodeParentId());
            log("loadtopology: found parentnode: " + parentNode.getLabel());
                       String targetId = parentNode.getNodeId();
            Vertex target;
            if (vertexes.containsKey(targetId)) {
                target = vertexes.get(targetId);
            } else {
                log("loadtopology: adding target as vertex: " + parentNode.getLabel());
                target = getVertex(parentNode);
                vertexes.put(targetId, target);
            }
            AbstractEdge edge = new AbstractEdge(TOPOLOGY_NAMESPACE_LINKD, link.getDataLinkInterfaceId(),source,target); 
            edge.setTooltipText(getEdgeTooltipText(link,source,target));
            edges.add(edge);
        }
        
        log("loadtopology: isAddNodeWithoutLink: " + isAddNodeWithoutLink());
        if (isAddNodeWithoutLink()) {
            for (OnmsNode onmsnode: m_nodeDao.findAll()) {
                log("loadtopology: parsing link less node: " + onmsnode.getLabel());
                String nodeId = onmsnode.getNodeId();
                if (!vertexes.containsKey(nodeId)) {
                    log("loadtopology: adding link less node: " + onmsnode.getLabel());
                    vertexes.put(nodeId,getVertex(onmsnode));
                }                
            }
        }
        
        log("Found " + vertexes.size() + " vertices");
        log("Found " + edges.size() + " edges");

        addVertices(vertexes.values().toArray(new Vertex[0]));
        addEdges(edges.toArray(new Edge[0]));
 
        log("loadtopology: loading topology: configFile:" + m_configurationFile);
        File configFile = new File(m_configurationFile);

        if (configFile.exists() && configFile.canRead()) {
            log("loadtopology: loading topology from configuration file: " + m_configurationFile);
            m_groupCounter = 0;
            WrappedGraph graph = getGraphFromFile(configFile);

            String namespace = graph.m_namespace == null ? TOPOLOGY_NAMESPACE_LINKD : graph.m_namespace;
            if (getVertexNamespace() != namespace) { 
                LoggerFactory.getLogger(this.getClass()).info("Creating new vertex provider with namespace {}", namespace);
                m_vertexProvider = new SimpleVertexProvider(namespace);
            }
            if (getEdgeNamespace() != namespace) { 
                LoggerFactory.getLogger(this.getClass()).info("Creating new edge provider with namespace {}", namespace);
                m_edgeProvider = new SimpleEdgeProvider(namespace);
            }

            // Add all groups to the topology
            int numberOfGroups = 0;
            for (WrappedVertex vertex: graph.m_vertices) {
                if (!vertex.leaf) {
                    log("loadtopology: adding group to topology: " + vertex.id);
                    // Find the highest index group number and start the index for new groups above it
                    try {
                        int groupNumber = Integer.parseInt(vertex.id.substring(LINKD_GROUP_ID_PREFIX.length()));
                        if (m_groupCounter <= groupNumber) {
                            m_groupCounter = groupNumber + 1;
                        }
                    } catch (NumberFormatException e) {
                        // Ignore this group ID since it doesn't conform to our pattern for auto-generated IDs
                    }
                    AbstractVertex newVertex = addGroup(vertex.id, vertex.iconKey, vertex.label);
                    newVertex.setIpAddress(vertex.ipAddr);
                    newVertex.setLocked(vertex.locked);
                    if (vertex.nodeID != null) newVertex.setNodeID(vertex.nodeID);
                    newVertex.setParent(vertex.parent);
                    newVertex.setSelected(vertex.selected);
                    newVertex.setStyleName(vertex.styleName);
                    newVertex.setTooltipText(vertex.tooltipText);
                    if (vertex.x != null) newVertex.setX(vertex.x);
                    if (vertex.y != null) newVertex.setY(vertex.y);
                    numberOfGroups++;
                }
            }
            
            for (Vertex vertex: getVertices()) {
                if (vertex.getParent() != null) {
                    log("loadtopology: setting parent of " + vertex + " to " + vertex.getParent());
                    setParent(vertex, vertex.getParent());
                }
            }
            log("Found " + numberOfGroups + " groups");
        }
    }

    private AbstractVertex getVertex(OnmsNode onmsnode) {
        OnmsIpInterface ip = getAddress(onmsnode);
        AbstractVertex vertex = new SimpleLeafVertex(TOPOLOGY_NAMESPACE_LINKD, onmsnode.getNodeId(), 0, 0);
        vertex.setIconKey(getIconName(onmsnode));
        vertex.setLabel(onmsnode.getLabel());
        vertex.setIpAddress(ip == null ? null : ip.getIpAddress().getHostAddress());
        vertex.setNodeID(Integer.parseInt(onmsnode.getNodeId()));
        vertex.setTooltipText(getNodeTooltipText(onmsnode, vertex, ip));
        return vertex;
    }

    private OnmsIpInterface getAddress(OnmsNode node) {
        //OnmsIpInterface ip = node.getPrimaryInterface();
        OnmsIpInterface ip = m_ipInterfaceDao.findPrimaryInterfaceByNodeId(node.getId());
        if ( ip == null) {
//            for (OnmsIpInterface iterip: node.getIpInterfaces()) {
            for (OnmsIpInterface iterip: m_ipInterfaceDao.findByNodeId(node.getId())) {
                ip = iterip;
                break;
            }
        }
        return ip;
    }
    

    private String getEdgeTooltipText(DataLinkInterface link,
            Vertex source, Vertex target) {
        StringBuffer tooltipText = new StringBuffer();

        OnmsSnmpInterface sourceInterface = m_snmpInterfaceDao.findByNodeIdAndIfIndex(Integer.parseInt(source.getId()), link.getIfIndex());
        OnmsSnmpInterface targetInterface = m_snmpInterfaceDao.findByNodeIdAndIfIndex(Integer.parseInt(target.getId()), link.getParentIfIndex());
        
        tooltipText.append(HTML_TOOLTIP_TAG_OPEN);
        if (sourceInterface != null && targetInterface != null
         && sourceInterface.getNetMask() != null && !sourceInterface.getNetMask().isLoopbackAddress() 
         && targetInterface.getNetMask() != null && !targetInterface.getNetMask().isLoopbackAddress()) {
            tooltipText.append("Type of Link: Layer3/Layer2");
        } else {
            tooltipText.append("Type of Link: Layer2");
        }
        tooltipText.append(HTML_TOOLTIP_TAG_END);

        tooltipText.append(HTML_TOOLTIP_TAG_OPEN);
        tooltipText.append( "Name: &lt;endpoint1 " + source.getLabel());
        if (sourceInterface != null ) 
            tooltipText.append( ":"+sourceInterface.getIfName());
        tooltipText.append( " ---- endpoint2 " + target.getLabel());
        if (targetInterface != null) 
            tooltipText.append( ":"+targetInterface.getIfName());
        tooltipText.append("&gt;");
        tooltipText.append(HTML_TOOLTIP_TAG_END);
        
        if ( targetInterface != null) {
            if (targetInterface.getIfSpeed() != null) {
                tooltipText.append(HTML_TOOLTIP_TAG_OPEN);
                tooltipText.append( "Bandwidth: " + getHumanReadableIfSpeed(targetInterface.getIfSpeed()));
                tooltipText.append(HTML_TOOLTIP_TAG_END);
            }
            if (targetInterface.getIfOperStatus() != null) {
                tooltipText.append(HTML_TOOLTIP_TAG_OPEN);
                tooltipText.append( "Link status: " + getIfStatusString(targetInterface.getIfOperStatus()));
                tooltipText.append(HTML_TOOLTIP_TAG_END);
            }
        } else if (sourceInterface != null) {
            if (sourceInterface.getIfSpeed() != null) {
                tooltipText.append(HTML_TOOLTIP_TAG_OPEN);
                tooltipText.append( "Bandwidth: " + getHumanReadableIfSpeed(sourceInterface.getIfSpeed()));
                tooltipText.append(HTML_TOOLTIP_TAG_END);
            }
            if (sourceInterface.getIfOperStatus() != null) {
                tooltipText.append(HTML_TOOLTIP_TAG_OPEN);
                tooltipText.append( "Link status: " + getIfStatusString(sourceInterface.getIfOperStatus()));
                tooltipText.append(HTML_TOOLTIP_TAG_END);
            }
        }

        tooltipText.append(HTML_TOOLTIP_TAG_OPEN);
        tooltipText.append( "End Point 1: " + source.getLabel() + ", " + source.getIpAddress());
        tooltipText.append(HTML_TOOLTIP_TAG_END);
        
        tooltipText.append(HTML_TOOLTIP_TAG_OPEN);
        tooltipText.append( "End Point 2: " + target.getLabel() + ", " + target.getIpAddress());
        tooltipText.append(HTML_TOOLTIP_TAG_END);

        log("getEdgeTooltipText\n" + tooltipText);
        return tooltipText.toString();
    }

    private static String getNodeTooltipText(OnmsNode node, AbstractVertex vertex, OnmsIpInterface ip) {
        StringBuffer tooltipText = new StringBuffer();

        /*
        if (node.getSysDescription() != null && node.getSysDescription().length() >0) {
            tooltipText.append(HTML_TOOLTIP_TAG_OPEN);
            tooltipText.append("Description: " + node.getSysDescription());
            tooltipText.append(HTML_TOOLTIP_TAG_END);
        }
        */

        tooltipText.append(HTML_TOOLTIP_TAG_OPEN);
        tooltipText.append( "Management IP and Name: " + vertex.getIpAddress() + " (" + vertex.getLabel() + ")");
        tooltipText.append(HTML_TOOLTIP_TAG_END);
        
        if (node.getSysLocation() != null && node.getSysLocation().length() >0) {
            tooltipText.append(HTML_TOOLTIP_TAG_OPEN);
            tooltipText.append("Location: " + node.getSysLocation());
            tooltipText.append(HTML_TOOLTIP_TAG_END);
        }
        
        tooltipText.append(HTML_TOOLTIP_TAG_OPEN);
        tooltipText.append( "Status: " +getNodeStatusString(node.getType().charAt(0)));
        if (ip != null && ip.isManaged()) {
            tooltipText.append( " / Managed");
        } else {
            tooltipText.append( " / Unmanaged");
        }
        tooltipText.append(HTML_TOOLTIP_TAG_END);

        log("getNodeTooltipText:\n" + tooltipText);
        
        return tooltipText.toString();

    }
    
    public static String getIconName(OnmsNode node) {
        return node.getSysObjectId() == null ? "linkd:system" : "linkd:system:snmp:"+node.getSysObjectId();
    }
    
    @Override
    public void save(String filename) {
        if (filename == null) 
            filename=m_configurationFile;
        List<WrappedVertex> vertices = new ArrayList<WrappedVertex>();
        for (Vertex vertex : getVertices()) {
            if (vertex.isLeaf()) {
                vertices.add(new WrappedLeafVertex(vertex));
            } else {
                vertices.add(new WrappedGroup(vertex));
            }
        }
        List<WrappedEdge> edges = new ArrayList<WrappedEdge>();
        for (Edge edge : getEdges()) {
            WrappedEdge newEdge = new WrappedEdge(edge, new WrappedLeafVertex(m_vertexProvider.getVertex(edge.getSource().getVertex())), new WrappedLeafVertex(m_vertexProvider.getVertex(edge.getTarget().getVertex())));
            edges.add(newEdge);
        }

        WrappedGraph graph = new WrappedGraph(getEdgeNamespace(), vertices, edges);
        
        JAXB.marshal(graph, new File(filename));
    }

      private static String getIfStatusString(int ifStatusNum) {
          if (ifStatusNum < OPER_ADMIN_STATUS.length) {
              return OPER_ADMIN_STATUS[ifStatusNum];
          } else {
              return "Unknown (" + ifStatusNum + ")";
          }
      }
      
    /**
     * Return the human-readable name for a interface status character, may be
     * null.
     *
     * @param c a char.
     * @return a {@link java.lang.String} object.
     */
    private static String getNodeStatusString(char c) {
        return m_nodeStatusMap.get(c);
    }
    
    /**
     * Method used to convert an integer bits-per-second value to a more
     * readable vale using commonly recognized abbreviation for network
     * interface speeds. Feel free to expand it as necessary to accomodate
     * different values.
     *
     * @param ifSpeed
     *            The bits-per-second value to be converted into a string
     *            description
     * @return A string representation of the speed (&quot;100 Mbps&quot; for
     *         example)
     */
    private static String getHumanReadableIfSpeed(long ifSpeed) {
        DecimalFormat formatter;
        double displaySpeed;
        String units;
        
        if (ifSpeed >= 1000000000L) {
            if ((ifSpeed % 1000000000L) == 0) {
                formatter = s_noDigitsAfterDecimal;
            } else {
                formatter = s_oneDigitAfterDecimal;
            }
            displaySpeed = ((double) ifSpeed) / 1000000000;
            units = "Gbps";
        } else if (ifSpeed >= 1000000L) {
            if ((ifSpeed % 1000000L) == 0) {
                formatter = s_noDigitsAfterDecimal;
            } else {
                formatter = s_oneDigitAfterDecimal;
            }
            displaySpeed = ((double) ifSpeed) / 1000000;
            units = "Mbps";
        } else if (ifSpeed >= 1000L) {
            if ((ifSpeed % 1000L) == 0) {
                formatter = s_noDigitsAfterDecimal;
            } else {
                formatter = s_oneDigitAfterDecimal;
            }
            displaySpeed = ((double) ifSpeed) / 1000;
            units = "kbps";
        } else {
            formatter = s_noDigitsAfterDecimal;
            displaySpeed = (double) ifSpeed;
            units = "bps";
        }
        
        return formatter.format(displaySpeed) + " " + units;
    }

    private static void log(final String string) {
        LoggerFactory.getLogger(LinkdTopologyProvider.class).debug(string);
    }
/*
    public TransactionOperations getTransactionTemplate() {
        return m_transactionTemplate;
    }

    public void setTransactionTemplate(TransactionOperations transactionTemplate) {
        m_transactionTemplate = transactionTemplate;
    }
*/

    public IpInterfaceDao getIpInterfaceDao() {
        return m_ipInterfaceDao;
    }

    public void setIpInterfaceDao(IpInterfaceDao ipInterfaceDao) {
        m_ipInterfaceDao = ipInterfaceDao;
    }
}
