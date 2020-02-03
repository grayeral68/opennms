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

package org.opennms.netmgt.telemetry.protocols.bmp.adapter.routes;

import org.opennms.features.bmp.api.BmpRoutePacket;

public class BmpRouteBean implements BmpRoutePacket {

    private String action;

    private String routerIp;

    private Long prefixLen;

    private long timeStamp;

    private String peerAddress;

    private Long peerId;

    private Long peerAsn;

    private String prefix;

    private Boolean isIpv4;

    private String origin;

    private String nextHopIp;

    private Long distinguisher;

    @Override
    public String getRouterIp() {
        return routerIp;
    }

    @Override
    public Type getType() {
        return Type.UNICAST_PREFIX;
    }

    @Override
    public long getTimeStamp() {
        return timeStamp;
    }

    @Override
    public String getAction() {
        return action;
    }

    @Override
    public String getPeerAddress() {
        return peerAddress;
    }

    @Override
    public Long getPeerId() {
        return peerId;
    }

    @Override
    public Long getPeerAsn() {
        return peerAsn;
    }

    @Override
    public String getPrefix() {
        return prefix;
    }

    @Override
    public Long getPrefixLen() {
        return prefixLen;
    }

    @Override
    public Boolean isIpv4() {
        return isIpv4;
    }

    @Override
    public Long getDistinguisher() {
        return distinguisher;
    }

    @Override
    public String getOrigin() {
        return origin;
    }

    @Override
    public String getNextHopIp() {
        return nextHopIp;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public void setPrefixLen(Long prefixLen) {
        this.prefixLen = prefixLen;
    }

    public void setTimeStamp(long timeStamp) {
        this.timeStamp = timeStamp;
    }

    public void setPeerAddress(String peerAddress) {
        this.peerAddress = peerAddress;
    }

    public void setPeerId(Long peerId) {
        this.peerId = peerId;
    }

    public void setPeerAsn(Long peerAsn) {
        this.peerAsn = peerAsn;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    public void setIpv4(Boolean ipv4) {
        isIpv4 = ipv4;
    }

    public void setOrigin(String origin) {
        this.origin = origin;
    }

    public void setNextHopIp(String nextHopIp) {
        this.nextHopIp = nextHopIp;
    }

    public void setDistinguisher(Long distinguisher) {
        this.distinguisher = distinguisher;
    }

    public void setRouterIp(String routerIp) {
        this.routerIp = routerIp;
    }
}
