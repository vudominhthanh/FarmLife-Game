#!/bin/bash
# Tạo partition tháng sau cho 3 bảng partitioned
NEXT_MONTH=$(date -d "+1 month" +%Y_%m)
NEXT_MONTH_FIRST=$(date -d "+1 month" +%Y-%m-01)
MONTH_AFTER=$(date -d "+2 month" +%Y-%m-01)

psql -U $POSTGRES_USER -d analytics_db <<EOF
CREATE TABLE IF NOT EXISTS analytics.events_${NEXT_MONTH}
  PARTITION OF analytics.events
  FOR VALUES FROM ('${NEXT_MONTH_FIRST}') TO ('${MONTH_AFTER}');
EOF

psql -U $POSTGRES_USER -d social_quest_db <<EOF
CREATE TABLE IF NOT EXISTS social_quest.chat_messages_${NEXT_MONTH}
  PARTITION OF social_quest.chat_messages
  FOR VALUES FROM ('${NEXT_MONTH_FIRST}') TO ('${MONTH_AFTER}');
EOF

psql -U $POSTGRES_USER -d ai_db <<EOF
CREATE TABLE IF NOT EXISTS ai.llm_call_logs_${NEXT_MONTH}
  PARTITION OF ai.llm_call_logs
  FOR VALUES FROM ('${NEXT_MONTH_FIRST}') TO ('${MONTH_AFTER}');
EOF
