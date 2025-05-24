package net.mattseq.echoes_of_time.item;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;

public class TotemOfEchoesItem extends Item {
    public TotemOfEchoesItem(Properties p_41383_) {
        super(new Item.Properties()
                .stacksTo(1)
                .durability(4) // Set durability here
                .rarity(Rarity.RARE)
        );
    }
}
