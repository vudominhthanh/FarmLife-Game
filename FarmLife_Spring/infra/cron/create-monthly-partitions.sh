#!/bin/bash
set -e

NEXT_MONTH_START=$(date -d "$(date +%Y-%m-01) +1 month" +%Y-%m-01)
NEXT_MONTH_END=$(date -d "$(date +%Y-%m-01) +2 months" +%Y-%m-01)
SUFFIX=$(date -d "$(date +%Y-%m-01) +1 month" +%Y_%m)

echo "Creating partitions for period: $NEXT_MONTH_START to $NEXT_MONTH_END (Suffix: $SUFFIX)"

psql -U "$POSTGRES_USER" -h postgres -d social_quest_db -c "
  CREATE TABLE IF NOT EXISTS social_quest.chat_messages_${SUFFIX}
  PARTITION OF social_quest.chat_messages
  FOR VALUES FROM ('${NEXT_MONTH_START}') TO ('${NEXT_MONTH_END}');
"

psql -U "$POSTGRES_USER" -h postgres -d analytics_db -c "
  CREATE TABLE IF NOT EXISTS analytics.events_${SUFFIX}
  PARTITION OF analytics.events
  FOR VALUES FROM ('${NEXT_MONTH_START}') TO ('${NEXT_MONTH_END}');
"

psql -U "$POSTGRES_USER" -h postgres -d ai_db -c "
  CREATE TABLE IF NOT EXISTS ai.llm_call_logs_${SUFFIX}
  PARTITION OF ai.llm_call_logs
  FOR VALUES FROM ('${NEXT_MONTH_START}') TO ('${NEXT_MONTH_END}');
"