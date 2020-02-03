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


import static org.opennms.netmgt.telemetry.protocols.common.utils.BsonUtils.getArray;
import static org.opennms.netmgt.telemetry.protocols.common.utils.BsonUtils.getInt32;
import static org.opennms.netmgt.telemetry.protocols.common.utils.BsonUtils.getInt64;
import static org.opennms.netmgt.telemetry.protocols.common.utils.BsonUtils.getString;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.bson.BsonDocument;
import org.bson.BsonValue;
import org.bson.RawBsonDocument;
import org.opennms.features.bmp.api.BmpRepository;
import org.opennms.features.bmp.api.BmpRoutePacket;
import org.opennms.netmgt.telemetry.api.adapter.Adapter;
import org.opennms.netmgt.telemetry.api.adapter.TelemetryMessageLog;
import org.opennms.netmgt.telemetry.api.adapter.TelemetryMessageLogEntry;
import org.opennms.netmgt.telemetry.config.api.AdapterDefinition;
import org.opennms.netmgt.telemetry.protocols.bmp.parser.proto.bmp.Header;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.codahale.metrics.MetricRegistry;

public class BmpRoutesAdapter implements Adapter {

    private static final Logger LOG = LoggerFactory.getLogger(BmpRoutesAdapter.class);
    private final BmpRepository bmpRepository;

    private final MetricRegistry metricRegistry;

    private final String name;

    public BmpRoutesAdapter(String name, BmpRepository bmpRepository, MetricRegistry metricRegistry) {
        this.name = name;
        this.bmpRepository = bmpRepository;
        this.metricRegistry = metricRegistry;
    }

    @Override
    public void setConfig(AdapterDefinition adapterConfig) {

    }

    @Override
    public void handleMessageLog(TelemetryMessageLog messageLog) {
        List<BmpRoutePacket> bmpRoutesPackets = new ArrayList<>();
        messageLog.getMessageList().forEach(message -> {
            bmpRoutesPackets.addAll(parseMessage(message, messageLog.getSourceAddress()));
        });
        bmpRepository.persist(bmpRoutesPackets);
    }

    @Override
    public void destroy() {

    }

    private List<BmpRoutePacket> parseMessage(TelemetryMessageLogEntry message, String sourceAddress) {
        LOG.trace("Parsing packet: {}", message);
        List<BmpRoutePacket> bmpRoutesPackets = new ArrayList<>();
        final BsonDocument document = new RawBsonDocument(message.getByteArray());

        if (getString(document, "@type")
                .map(type -> Header.Type.ROUTE_MONITORING.name().equals(type))
                .orElse(false)) {
            Optional<Iterable<BsonValue>> withdraw = getArray(document, "withdraw");
            if (withdraw.isPresent()) {
                bmpRoutesPackets.addAll(getPrefixes(sourceAddress, document, withdraw.get(), BmpRoutePacket.Action.del));
            }
            Optional<Iterable<BsonValue>> reachable = getArray(document, "reachable");
            if (reachable.isPresent()) {
                bmpRoutesPackets.addAll(getPrefixes(sourceAddress, document, reachable.get(), BmpRoutePacket.Action.add));
            }

        }
        return bmpRoutesPackets;
    }

    private List<BmpRoutePacket> getPrefixes(String sourceAddress, BsonDocument document,
                                             Iterable<BsonValue> values, BmpRoutePacket.Action action) {
        List<BmpRoutePacket> bmpRoutesPackets = new ArrayList<>();
        values.forEach(value -> {
            bmpRoutesPackets.add(createBmpRoutePacket(sourceAddress, document, value, action));
        });
        return bmpRoutesPackets;
    }

    private BmpRoutePacket createBmpRoutePacket(String sourceAddress, BsonDocument document, BsonValue value, BmpRoutePacket.Action action) {
        BmpRouteBean routeMonitoringBean = new BmpRouteBean();
        Optional<Long> prefixLen = getInt64(value.asDocument(), "len");
        prefixLen.ifPresent(routeMonitoringBean::setPrefixLen);
        Optional<String> prefix = getString(value.asDocument(), "prefix");
        prefix.ifPresent(routeMonitoringBean::setPrefix);
        setPeerValues(document, routeMonitoringBean);
        routeMonitoringBean.setRouterIp(sourceAddress);
        routeMonitoringBean.setAction(action.name());
        return routeMonitoringBean;
    }

    private void setPeerValues(BsonDocument document, BmpRouteBean routeMonitoringBean) {
        final Instant timestamp = Instant.ofEpochSecond(getInt64(document, "peer", "timestamp", "epoch").get(),
                getInt64(document, "peer", "timestamp", "nanos").orElse(0L));
        routeMonitoringBean.setTimeStamp(timestamp.toEpochMilli());
        String peerAddress = getString(document, "peer", "address").orElse(null);
        routeMonitoringBean.setPeerAddress(peerAddress);
        Long peerId = getInt64(document, "peer", "id").orElse(null);
        routeMonitoringBean.setPeerId(peerId);
        Long peerAsn = getInt64(document, "peer", "as").orElse(null);
        routeMonitoringBean.setPeerAsn(peerAsn);
        Integer ipVersion = getInt32(document, "peer", "ip_version").orElse(null);
        if (ipVersion != null && ipVersion == 4) {
            routeMonitoringBean.setIpv4(true);
        }
        if (ipVersion != null && ipVersion == 6) {
            routeMonitoringBean.setIpv4(false);
        }
        Long distinguisher = getInt64(document, "peer", "distinguisher").orElse(null);
        routeMonitoringBean.setDistinguisher(distinguisher);
    }
}
