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
import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import org.opennms.features.jest.client.bulk.BulkRequest;
import org.opennms.features.jest.client.bulk.BulkWrapper;
import org.opennms.features.jest.client.index.IndexStrategy;
import org.opennms.features.jest.client.template.IndexSettings;
import org.opennms.features.bmp.api.BmpRoutePacket;
import org.opennms.features.bmp.api.BmpRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.searchbox.client.JestClient;
import io.searchbox.core.Bulk;
import io.searchbox.core.Index;

public class ElasticBmpRepository implements BmpRepository {

    private static final String INDEX_NAME = "bmp";
    private static final Logger LOG = LoggerFactory.getLogger(ElasticBmpRepository.class);
    private final JestClient client;

    private final IndexStrategy indexStrategy;

    private final IndexSettings indexSettings;

    private final BmpTemplateInitializer initializer;

    private final int bulkRetryCount;


    public ElasticBmpRepository(JestClient client, IndexStrategy indexStrategy,
                                IndexSettings indexSettings,
                                BmpTemplateInitializer initializer,
                                int bulkRetryCount) {
        this.client = client;
        this.indexStrategy = indexStrategy;
        this.indexSettings = indexSettings;
        this.initializer = initializer;
        this.bulkRetryCount = bulkRetryCount;
    }

    @Override
    public void persist(Collection<BmpRoutePacket> packets) {
        List<UnicastPrefixDocument> bmpDocuments = packets.stream().map(this::convertToDocument).collect(Collectors.toList());
        BulkRequest<UnicastPrefixDocument> bulkRequest = new BulkRequest<>(client, bmpDocuments, (documents) -> {
            final Bulk.Builder bulkBuilder = new Bulk.Builder();
            for (UnicastPrefixDocument document : documents) {
                final String index = indexStrategy.getIndex(indexSettings, INDEX_NAME, Instant.ofEpochMilli(document.getTimestamp()));
                final Index.Builder indexBuilder = new Index.Builder(document).index(index);
                bulkBuilder.addAction(indexBuilder.build());
            }
            return new BulkWrapper(bulkBuilder);
        }, bulkRetryCount);

        try {
            bulkRequest.execute();
        } catch (IOException e) {
            LOG.error("Failed to persist bmp documents", e);
        }

    }

    public void start() {
        initializer.initialize();
    }

    private UnicastPrefixDocument convertToDocument(BmpRoutePacket bmpPacket) {
        return UnicastPrefixDocument.from(bmpPacket);
    }
}
