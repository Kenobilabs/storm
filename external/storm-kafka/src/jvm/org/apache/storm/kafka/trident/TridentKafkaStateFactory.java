/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.storm.kafka.trident;

import java.util.Map;
import java.util.Properties;

import org.apache.storm.kafka.trident.mapper.TridentTupleToKafkaMapper;
import org.apache.storm.kafka.trident.selector.KafkaTopicSelector;
import org.apache.storm.task.IMetricsContext;
import org.apache.storm.task.TopologyContext;
import org.apache.storm.trident.state.State;
import org.apache.storm.trident.state.StateFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.symantec.KAFKACONSTANTS;

public class TridentKafkaStateFactory implements StateFactory {

    private static final Logger LOG = LoggerFactory.getLogger(TridentKafkaStateFactory.class);

    private TridentTupleToKafkaMapper mapper;
    private KafkaTopicSelector topicSelector;
    private Properties producerProperties = new Properties();
    private String name;
    
    public TridentKafkaStateFactory withTridentTupleToKafkaMapper(TridentTupleToKafkaMapper mapper) {
        this.mapper = mapper;
        return this;
    }

    public TridentKafkaStateFactory withKafkaTopicSelector(KafkaTopicSelector selector) {
        this.topicSelector = selector;
        return this;
    }

    public TridentKafkaStateFactory withProducerProperties(Properties props) {
        this.producerProperties = props;
        return this;
    }

    @Override
    public State makeState(Map conf, IMetricsContext metrics, int partitionIndex, int numPartitions) {
        LOG.info("makeState(partitonIndex={}, numpartitions={}", partitionIndex, numPartitions);
        TridentKafkaState state = new TridentKafkaState()
                .withKafkaTopicSelector(this.topicSelector)
                .withTridentTupleToKafkaMapper(this.mapper);
        TopologyContext context = (TopologyContext) metrics;
        this.name = "KafkaBolt";
        if (conf != null && conf.containsKey(KAFKACONSTANTS.STATSD)
            && "YES".compareToIgnoreCase(conf.get(KAFKACONSTANTS.STATSD).toString()) == 0) {
          context.setTaskData(KAFKACONSTANTS.STATSD, this.name);
          state.withStatsD(conf, context);
        }
        state.prepare(producerProperties);
        return state;
    }
}
