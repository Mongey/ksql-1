/*
 * Copyright 2018 Confluent Inc.
 *
 * Licensed under the Confluent Community License (the "License"); you may not use
 * this file except in compliance with the License.  You may obtain a copy of the
 * License at
 *
 * http://www.confluent.io/confluent-community-license
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OF ANY KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations under the License.
 */

package io.confluent.ksql.planner.plan;

import com.google.common.collect.ImmutableList;
import io.confluent.ksql.execution.builder.KsqlQueryBuilder;
import io.confluent.ksql.execution.context.QueryContext.Stacker;
import io.confluent.ksql.execution.expression.tree.Expression;
import io.confluent.ksql.schema.ksql.LogicalSchema;
import io.confluent.ksql.services.KafkaTopicClient;
import io.confluent.ksql.structured.SchemaKStream;
import java.util.List;
import java.util.Objects;

public class FilterNode extends PlanNode {

  private final PlanNode source;
  private final Expression predicate;

  public FilterNode(
      final PlanNodeId id,
      final PlanNode source,
      final Expression predicate
  ) {
    super(id, source.getNodeOutputType(), source.getSourceName());

    this.source = Objects.requireNonNull(source, "source");
    this.predicate = Objects.requireNonNull(predicate, "predicate");
  }

  public Expression getPredicate() {
    return predicate;
  }

  @Override
  public LogicalSchema getSchema() {
    return source.getSchema();
  }

  @Override
  public List<PlanNode> getSources() {
    return ImmutableList.of(source);
  }

  public PlanNode getSource() {
    return source;
  }

  @Override
  protected int getPartitions(final KafkaTopicClient kafkaTopicClient) {
    return source.getPartitions(kafkaTopicClient);
  }

  @Override
  public SchemaKStream<?> buildStream(final KsqlQueryBuilder builder) {
    final Stacker contextStacker = builder.buildNodeContext(getId().toString());

    return source.buildStream(builder)
        .filter(
            getPredicate(),
            contextStacker
        );
  }
}
