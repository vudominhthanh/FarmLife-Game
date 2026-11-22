-- ============ Seed NPC agents ============
INSERT INTO ai.npc_agents
    (npc_code, display_name, role, persona_prompt, favorite_items, disliked_items,
     llm_model, fsm_state, current_map_code, schedule_json)
VALUES
    ('npc_merchant_lily', 'Lily', 'merchant',
     'Bạn là Lily, chủ cửa hàng hạt giống 28 tuổi. Thân thiện, hay cười, thích thương lượng nhẹ nhàng. Trả lời ngắn gọn 1-2 câu, cá nhân hóa theo trí nhớ người chơi.',
     '{seed_pumpkin,seed_strawberry}', '{crop_withered}',
     'qwen2.5:7b', 'IDLE', 'town_square',
     '{"08:00":"TRADING","12:00":"IDLE","18:00":"SLEEPING"}'),
    ('npc_blacksmith_gorn', 'Gorn', 'artisan',
     'Bạn là Gorn, thợ rèn 45 tuổi. Trầm tính, nói ít, chỉ nói về dụng cụ và chế tạo. Không thích nói chuyện xã giao.',
     '{material_iron_ore,recipe_tool_hoe_gold}', '{food_cake}',
     'qwen2.5:7b', 'WORKING', 'smithy',
     '{"07:00":"WORKING","17:00":"IDLE","21:00":"SLEEPING"}'),
    ('npc_mayor_elder', 'Elder Thomas', 'mayor',
     'Bạn là Elder Thomas, thị trưởng 65 tuổi. Nghiêm trang, quan tâm đến phát triển nông trại và an sinh cộng đồng.',
     '{}', '{}',
     'qwen2.5:7b', 'IDLE', 'town_hall',
     '{"09:00":"WORKING","12:00":"IDLE","20:00":"SLEEPING"}')
ON CONFLICT (npc_code) DO NOTHING;

-- ============ Seed quest generation guardrails ============
INSERT INTO ai.quest_generation_rules
    (rule_name, priority, min_player_lv, max_player_lv, play_style_tags,
     allowed_goals, min_target_qty, max_target_qty,
     max_coin_reward, max_xp_reward, allowed_items)
VALUES
    ('newbie_harvest', 10, 1, 5, '{}',
     '{harvest_crop}', 3, 10, 200, 50,
     '{crop_pumpkin,crop_strawberry,crop_corn}'),
    ('casual_social', 20, 1, 30, '{casual,social}',
     '{deliver_item,friend_visit}', 1, 5, 300, 80,
     '{food_pie,food_jam,decor_flower}'),
    ('trader_sell', 30, 5, 50, '{trader}',
     '{sell_market,deliver_item}', 5, 30, 800, 150,
     '{crop_pumpkin,crop_corn,crop_strawberry,food_cheese}'),
    ('crafting_advanced', 40, 10, 60, '{hardcore,collector}',
     '{craft_product}', 1, 5, 1200, 300,
     '{recipe_cheese,recipe_jam,recipe_wine}'),
    ('animal_care', 50, 3, 40, '{casual,collector}',
     '{feed_animal,collect_product}', 2, 8, 500, 120,
     '{food_hay,product_milk,product_egg}')
ON CONFLICT DO NOTHING;
