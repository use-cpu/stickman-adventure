package com.stickman.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.stickman.common.BusinessException;
import com.stickman.entity.Character;
import com.stickman.entity.CharacterItem;
import com.stickman.entity.ShopItem;
import com.stickman.mapper.CharacterItemMapper;
import com.stickman.mapper.ShopItemMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 商店服务 - 购买装备和药水
 */
@Service
@RequiredArgsConstructor
public class ShopService {

    private final ShopItemMapper shopItemMapper;
    private final CharacterItemMapper characterItemMapper;
    private final CharacterService characterService;
    private final GameSaveService gameSaveService;

    /**
     * 获取商店商品列表(含该存档的购买状态)
     */
    public Map<String, Object> listItems(Long saveId) {
        gameSaveService.getById(saveId); // 校验存档归属
        List<ShopItem> items = shopItemMapper.selectList(
                new LambdaQueryWrapper<ShopItem>().orderByAsc(ShopItem::getSortOrder));

        // 查询该存档已购买的唯一商品
        List<CharacterItem> purchased = characterItemMapper.selectList(
                new LambdaQueryWrapper<CharacterItem>().eq(CharacterItem::getSaveId, saveId));
        Map<Long, Long> purchasedMap = purchased.stream()
                .collect(Collectors.toMap(CharacterItem::getItemId, CharacterItem::getId, (a, b) -> a));

        Character ch = characterService.getBySaveId(saveId);

        Map<String, Object> result = new HashMap<>();
        result.put("gold", ch.getGold());
        result.put("items", items.stream().map(item -> {
            Map<String, Object> m = new HashMap<>();
            m.put("id", item.getId());
            m.put("name", item.getName());
            m.put("category", item.getCategory());
            m.put("price", item.getPrice());
            m.put("icon", item.getIcon());
            m.put("description", item.getDescription());
            m.put("statType", item.getStatType());
            m.put("statValue", item.getStatValue());
            m.put("isUnique", item.getIsUnique() == 1);
            m.put("purchased", item.getIsUnique() == 1 && purchasedMap.containsKey(item.getId()));
            return m;
        }).collect(Collectors.toList()));
        return result;
    }

    /**
     * 获取该存档已购买的物品(背包)
     */
    public List<Map<String, Object>> getInventory(Long saveId) {
        gameSaveService.getById(saveId); // 校验存档归属
        List<CharacterItem> purchased = characterItemMapper.selectList(
                new LambdaQueryWrapper<CharacterItem>().eq(CharacterItem::getSaveId, saveId));
        if (purchased.isEmpty()) {
            return Collections.emptyList();
        }
        List<Long> itemIds = purchased.stream().map(CharacterItem::getItemId).collect(Collectors.toList());
        List<ShopItem> items = shopItemMapper.selectBatchIds(itemIds);
        // 保持购买顺序
        Map<Long, ShopItem> itemMap = items.stream()
                .collect(Collectors.toMap(ShopItem::getId, i -> i, (a, b) -> a));
        List<Map<String, Object>> result = new ArrayList<>();
        for (CharacterItem ci : purchased) {
            ShopItem item = itemMap.get(ci.getItemId());
            if (item == null) continue;
            Map<String, Object> m = new HashMap<>();
            m.put("id", item.getId());
            m.put("name", item.getName());
            m.put("category", item.getCategory());
            m.put("price", item.getPrice());
            m.put("icon", item.getIcon());
            m.put("description", item.getDescription());
            m.put("statType", item.getStatType());
            m.put("statValue", item.getStatValue());
            m.put("purchaseTime", ci.getCreateTime());
            result.add(m);
        }
        return result;
    }

    /**
     * 购买商品
     */
    @Transactional
    public Map<String, Object> buy(Long saveId, Long itemId) {
        gameSaveService.getById(saveId); // 校验归属
        ShopItem item = shopItemMapper.selectById(itemId);
        if (item == null) {
            throw BusinessException.of("商品不存在");
        }

        // 唯一商品校验: 已购买则不可重复购买
        if (item.getIsUnique() == 1) {
            Long count = characterItemMapper.selectCount(
                    new LambdaQueryWrapper<CharacterItem>()
                            .eq(CharacterItem::getSaveId, saveId)
                            .eq(CharacterItem::getItemId, itemId));
            if (count != null && count > 0) {
                throw BusinessException.of("该装备已购买, 不可重复购买");
            }
        }

        Character ch = characterService.getBySaveId(saveId);
        if (ch.getGold() < item.getPrice()) {
            throw BusinessException.of("金币不足");
        }

        // 扣金币
        ch.setGold(ch.getGold() - item.getPrice());

        // 应用属性效果
        applyEffect(ch, item);

        characterService.saveCharacter(ch);

        // 记录购买
        CharacterItem ci = new CharacterItem();
        ci.setSaveId(saveId);
        ci.setItemId(itemId);
        characterItemMapper.insert(ci);

        Map<String, Object> result = new HashMap<>();
        result.put("gold", ch.getGold());
        result.put("character", ch);
        result.put("message", "购买成功: " + item.getName());
        return result;
    }

    /**
     * 应用商品效果到角色
     */
    private void applyEffect(Character ch, ShopItem item) {
        switch (item.getStatType().toUpperCase()) {
            case "ATTACK":
                ch.setAttack(ch.getAttack() + item.getStatValue());
                break;
            case "DEFENSE":
                ch.setDefense(ch.getDefense() + item.getStatValue());
                break;
            case "SPEED":
                ch.setSpeed(ch.getSpeed() + item.getStatValue());
                break;
            case "MAX_HP":
                ch.setMaxHp(ch.getMaxHp() + item.getStatValue());
                ch.setHp(ch.getHp() + item.getStatValue());
                break;
            case "MAX_MP":
                ch.setMaxMp(ch.getMaxMp() + item.getStatValue());
                ch.setMp(ch.getMp() + item.getStatValue());
                break;
            case "SKILL_DAMAGE":
                ch.setSkillBonus((ch.getSkillBonus() == null ? 0 : ch.getSkillBonus()) + item.getStatValue());
                break;
            case "HP":
                int newHp = Math.min(ch.getMaxHp(), ch.getHp() + item.getStatValue());
                ch.setHp(newHp);
                break;
            case "MP":
                int newMp = Math.min(ch.getMaxMp(), ch.getMp() + item.getStatValue());
                ch.setMp(newMp);
                break;
            default:
                throw BusinessException.of("未知属性类型: " + item.getStatType());
        }
    }
}
