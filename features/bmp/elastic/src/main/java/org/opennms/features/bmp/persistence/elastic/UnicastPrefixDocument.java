/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2020 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2020 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.features.bmp.persistence.elastic;

import org.opennms.features.bmp.api.BmpRoutePacket;

import com.google.gson.annotations.SerializedName;

public class UnicastPrefixDocument {

    @SerializedName("action")
    private String action;

    @SerializedName("sequence")
    private Long sequence;

    @SerializedName("hash")
    private String hash;

    @SerializedName("router_hash")
    private String routerHash;

    @SerializedName("router_ip")
    private String routerIp;

    @SerializedName("base_attr_hash")
    private String baseAttrHash;

    @SerializedName("peer_hash")
    private String peerHash;

    @SerializedName("peer_ip")
    private String peerIp;

    @SerializedName("peer_asn")
    private Integer peerAsn;

    @SerializedName("timestamp")
    private Long timestamp;

    @SerializedName("prefix")
    private String prefix;

    @SerializedName("length")
    private Integer length;

    @SerializedName("is_ipv4")
    private Boolean ipv4;

    @SerializedName("origin")
    private String origin;

    @SerializedName("as_path")
    private String asPath;

    @SerializedName("as_path_count")
    private Integer asPathCount;

    @SerializedName("origin_as")
    private Integer originAs;

    @SerializedName("next_hop")
    private String nextHop;

    @SerializedName("med")
    private Integer med;

    @SerializedName("local_pref")
    private Integer localPref;

    @SerializedName("aggregator")
    private String aggregator;

    @SerializedName("community_list")
    private String communityList;

    @SerializedName("ext_community_list")
    private String extCommunityList;

    @SerializedName("cluster_list")
    private String clusterList;

    @SerializedName("is_atomic_agg")
    private boolean atomicAgg;

    @SerializedName("is_next_hop_ipv4")
    private boolean nextHopIpv4;

    @SerializedName("originator_id")
    private String originatorId;

    @SerializedName("path_id")
    private int pathId;

    @SerializedName("labels")
    private String labels;

    @SerializedName("is_pre_policy")
    private boolean prePolicy;

    @SerializedName("is_adj_in")
    private boolean adjIn;

    @SerializedName("large_community_list")
    private String largeCommunityList;

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public Long getSequence() {
        return sequence;
    }

    public void setSequence(Long sequence) {
        this.sequence = sequence;
    }

    public String getHash() {
        return hash;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }

    public String getRouterHash() {
        return routerHash;
    }

    public void setRouterHash(String routerHash) {
        this.routerHash = routerHash;
    }

    public String getRouterIp() {
        return routerIp;
    }

    public void setRouterIp(String routerIp) {
        this.routerIp = routerIp;
    }

    public String getBaseAttrHash() {
        return baseAttrHash;
    }

    public void setBaseAttrHash(String baseAttrHash) {
        this.baseAttrHash = baseAttrHash;
    }

    public String getPeerHash() {
        return peerHash;
    }

    public void setPeerHash(String peerHash) {
        this.peerHash = peerHash;
    }

    public String getPeerIp() {
        return peerIp;
    }

    public void setPeerIp(String peerIp) {
        this.peerIp = peerIp;
    }

    public Integer getPeerAsn() {
        return peerAsn;
    }

    public void setPeerAsn(Integer peerAsn) {
        this.peerAsn = peerAsn;
    }

    public Long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }

    public String getPrefix() {
        return prefix;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    public Integer getLength() {
        return length;
    }

    public void setLength(Integer length) {
        this.length = length;
    }

    public Boolean isIpv4() {
        return ipv4;
    }

    public void setIpv4(Boolean ipv4) {
        this.ipv4 = ipv4;
    }

    public String getOrigin() {
        return origin;
    }

    public void setOrigin(String origin) {
        this.origin = origin;
    }

    public String getAsPath() {
        return asPath;
    }

    public void setAsPath(String asPath) {
        this.asPath = asPath;
    }

    public Integer getAsPathCount() {
        return asPathCount;
    }

    public void setAsPathCount(Integer asPathCount) {
        this.asPathCount = asPathCount;
    }

    public Integer getOriginAs() {
        return originAs;
    }

    public void setOriginAs(Integer originAs) {
        this.originAs = originAs;
    }

    public String getNextHop() {
        return nextHop;
    }

    public void setNextHop(String nextHop) {
        this.nextHop = nextHop;
    }

    public Integer getMed() {
        return med;
    }

    public void setMed(Integer med) {
        this.med = med;
    }

    public Integer getLocalPref() {
        return localPref;
    }

    public void setLocalPref(Integer localPref) {
        this.localPref = localPref;
    }

    public String getAggregator() {
        return aggregator;
    }

    public void setAggregator(String aggregator) {
        this.aggregator = aggregator;
    }

    public Boolean getIpv4() {
        return ipv4;
    }

    public String getCommunityList() {
        return communityList;
    }

    public void setCommunityList(String communityList) {
        this.communityList = communityList;
    }

    public String getExtCommunityList() {
        return extCommunityList;
    }

    public void setExtCommunityList(String extCommunityList) {
        this.extCommunityList = extCommunityList;
    }

    public String getClusterList() {
        return clusterList;
    }

    public void setClusterList(String clusterList) {
        this.clusterList = clusterList;
    }

    public boolean isAtomicAgg() {
        return atomicAgg;
    }

    public void setAtomicAgg(boolean atomicAgg) {
        this.atomicAgg = atomicAgg;
    }

    public boolean isNextHopIpv4() {
        return nextHopIpv4;
    }

    public void setNextHopIpv4(boolean nextHopIpv4) {
        this.nextHopIpv4 = nextHopIpv4;
    }

    public String getOriginatorId() {
        return originatorId;
    }

    public void setOriginatorId(String originatorId) {
        this.originatorId = originatorId;
    }

    public int getPathId() {
        return pathId;
    }

    public void setPathId(int pathId) {
        this.pathId = pathId;
    }

    public String getLabels() {
        return labels;
    }

    public void setLabels(String labels) {
        this.labels = labels;
    }

    public boolean isPrePolicy() {
        return prePolicy;
    }

    public void setPrePolicy(boolean prePolicy) {
        this.prePolicy = prePolicy;
    }

    public boolean isAdjIn() {
        return adjIn;
    }

    public void setAdjIn(boolean adjIn) {
        this.adjIn = adjIn;
    }

    public String getLargeCommunityList() {
        return largeCommunityList;
    }

    public void setLargeCommunityList(String largeCommunityList) {
        this.largeCommunityList = largeCommunityList;
    }

    static UnicastPrefixDocument from(BmpRoutePacket bmpPacket) {
        UnicastPrefixDocument document = new UnicastPrefixDocument();
        document.setAction(bmpPacket.getAction());
        document.setRouterIp(bmpPacket.getRouterIp());
        document.setTimestamp(bmpPacket.getTimeStamp());
        document.setPeerIp(bmpPacket.getPeerAddress());
        document.setPeerHash(Long.toString(bmpPacket.getPeerId()));
        document.setIpv4(bmpPacket.isIpv4());
        document.setPrefix(bmpPacket.getPrefix());
        document.setLength(bmpPacket.getPrefixLen().intValue());
        return document;
    }
}
