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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.opennms.core.test.elastic.ElasticSearchRule;
import org.opennms.core.test.elastic.ElasticSearchServerConfig;
import org.opennms.elasticsearch.plugin.DriftPlugin;
import org.opennms.features.bmp.api.BmpRoutePacket;
import org.opennms.features.jest.client.RestClientFactory;
import org.opennms.features.jest.client.index.IndexStrategy;
import org.opennms.features.jest.client.template.IndexSettings;

import io.searchbox.client.JestClient;

public class ElasticBmpRepositoryIT {

    private JestClient jestClient;
    @Rule
    public ElasticSearchRule elasticSearchRule = new ElasticSearchRule(new ElasticSearchServerConfig()
            .withPlugins(DriftPlugin.class));

    private ElasticBmpRepository bmpRepository;

    @Before
    public void setUp() throws IOException {
        RestClientFactory restClientFactory = new RestClientFactory(elasticSearchRule.getUrl());
        jestClient = restClientFactory.createClient();
    }

    @Test
    public void testPersist() {
        BmpTemplateInitializer templateInitializer = new BmpTemplateInitializer(jestClient);
        ElasticBmpRepository elasticBmpRepository = new ElasticBmpRepository(jestClient, IndexStrategy.MONTHLY,
                new IndexSettings(), templateInitializer, 2);
        List<BmpRoutePacket> bmpPackets = new ArrayList<>();
        bmpPackets.add(generateBmpPacket());
        elasticBmpRepository.persist(bmpPackets);
    }

    private BmpRoutePacket generateBmpPacket() {
        return new BmpRoutePacket() {
            @Override
            public String getRouterIp() {
                return "192.168.0.1";
            }

            @Override
            public Type getType() {
                return Type.UNICAST_PREFIX;
            }

            @Override
            public long getTimeStamp() {
                return System.currentTimeMillis();
            }

            @Override
            public String getAction() {
                return "add";
            }

            @Override
            public String getPeerAddress() {
                return "192.168.0.5";
            }

            @Override
            public Long getPeerId() {
                return new Random().nextLong();
            }

            @Override
            public Long getPeerAsn() {
                return new Random().nextLong();
            }

            @Override
            public String getPrefix() {
                return "192.168.1.10";
            }

            @Override
            public Long getPrefixLen() {
                return new Integer("192.168.1.10".length()).longValue();
            }

            @Override
            public Boolean isIpv4() {
                return true;
            }

            @Override
            public Long getDistinguisher() {
                return  new Random().nextLong();
            }

            @Override
            public String getOrigin() {
                return null;
            }

            @Override
            public String getNextHopIp() {
                return null;
            }
        };
    }
}
