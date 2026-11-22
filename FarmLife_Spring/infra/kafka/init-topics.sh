#!/bin/bash
set -e

TOPICS=(
  "farmlife.identity.player-created.v1"
  "farmlife.identity.playstyle-updated.v1"
  "farmlife.world.crop-harvested.v1"
  "farmlife.world.item-reserve-requested.v1"
  "farmlife.economy.reservation-resolved.v1"
  "farmlife.economy.inventory-updated.v1"
  "farmlife.economy.trade-completed.v1"
  "farmlife.economy.market-price-tick.v1"
  "farmlife.media.asset-ready.v1"
  "farmlife.ai.quest-assigned.v1"
  "farmlife.ai.npc-state-changed.v1"
  "farmlife.ai.market-rumor-published.v1"
)

for topic in "${TOPICS[@]}"; do
  /opt/kafka/bin/kafka-topics.sh --bootstrap-server kafka:29092 \
    --create --if-not-exists --topic "$topic" --partitions 6 --replication-factor 1
done

echo ">>> All 12 Kafka topics created successfully!"