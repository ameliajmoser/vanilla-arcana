package com.chemelia.vanillaarcana;

import com.chemelia.vanillaarcana.enchantments.AegisEnchantment;
import com.chemelia.vanillaarcana.enchantments.AerothurgeEnchantment;
import com.chemelia.vanillaarcana.enchantments.FrostEnchantment;
import com.chemelia.vanillaarcana.enchantments.NecromancyEnchantment;
import com.chemelia.vanillaarcana.enchantments.PyrokinesisEnchantment;
import com.chemelia.vanillaarcana.enchantments.SyphonEnchantment;
import com.chemelia.vanillaarcana.enchantments.WarpEnchantment;
import com.chemelia.vanillaarcana.entity.monster.TamedZombie;
import com.chemelia.vanillaarcana.entity.projectile.FrostSnowball;
import com.chemelia.vanillaarcana.entity.projectile.SyphonSnowball;
import com.chemelia.vanillaarcana.item.WandItem;
import com.google.common.base.Supplier;

import net.minecraft.client.renderer.entity.ZombieRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.Material;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

@Mod.EventBusSubscriber(modid = VanillaArcana.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class RegistryHandler {


    //Sounds
    public static final DeferredRegister<SoundEvent> SOUNDS = DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, VanillaArcana.MOD_ID);
    public static final RegistryObject<SoundEvent> SPELL_CAST = SOUNDS.register("item.wand.cast", 
        () -> new SoundEvent(new ResourceLocation(VanillaArcana.MOD_ID, "item.wand.cast")));
    public static final RegistryObject<SoundEvent> SPELL_FAIL = SOUNDS.register("item.wand.fail", 
        () -> new SoundEvent(new ResourceLocation(VanillaArcana.MOD_ID, "item.wand.fail")));
    public static final RegistryObject<SoundEvent> SPELL_SAP = SOUNDS.register("item.wand.sap", 
        () -> new SoundEvent(new ResourceLocation(VanillaArcana.MOD_ID, "item.wand.sap")));

    //Blocks
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, VanillaArcana.MOD_ID);
    
    private static <T extends Block>RegistryObject<T> registerBlock(String name, Supplier<T> block){
        RegistryObject<T> toReturn = BLOCKS.register(name, block);
        registerBlockItem(name, toReturn);
        return toReturn;
    }
    
    private static <T extends Block> void registerBlockItem(String name, RegistryObject<T> block) {
        ITEMS.register(name, () -> new BlockItem(block .get(),
                new Item.Properties().tab(CreativeModeTab.TAB_MISC)));
    }

    public static final RegistryObject<Block> AEGIS_BLOCK = registerBlock("aegis_block", ()-> new Block(BlockBehaviour.Properties
        .of(Material.GLASS)
        .strength(15F, 0.1F)
        .sound(SoundType.GLASS)
        .noOcclusion()));

    //Items
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, VanillaArcana.MOD_ID);

    public static final RegistryObject<Item> AMETHYST_WAND = ITEMS.register("amethyst_wand", () -> new WandItem(new Item.Properties()
        .stacksTo(1)
        .tab(CreativeModeTab.TAB_TOOLS)));
    public static final RegistryObject<Item> NETHER_WAND = ITEMS.register("nether_wand", () -> new WandItem(new Item.Properties()
        .stacksTo(1)
        .tab(CreativeModeTab.TAB_TOOLS)));

    //Entities
    public static final DeferredRegister<EntityType<?>> ENTITIES = DeferredRegister.create(ForgeRegistries.ENTITIES, VanillaArcana.MOD_ID);

    public static final EntityType<SyphonSnowball> SYPHON_SNOWBALL= EntityType.Builder
        .<SyphonSnowball>of(SyphonSnowball::new, MobCategory.MISC)
        .sized(0.25F, 0.25F)
        .clientTrackingRange(4)
        .updateInterval(10)
        .build(VanillaArcana.MOD_ID + ":syphon_projectile");

    public static final EntityType<FrostSnowball> FROST_SNOWBALL = EntityType.Builder
        .<FrostSnowball>of(FrostSnowball::new, MobCategory.MISC)
        .sized(0.25F, 0.25F)
        .clientTrackingRange(4)
        .updateInterval(10)
        .build(VanillaArcana.MOD_ID + ":frost_projectile");

    public static final RegistryObject<EntityType<TamedZombie>> TAMED_ZOMBIE = createTamedMonster("tamed_zombie", TamedZombie::new, 0.6F, 1.95F);

    private static <T extends LivingEntity> RegistryObject<EntityType<T>> createTamedMonster(String name, EntityType.EntityFactory<T> factory, float width, float height){
        ResourceLocation location = new ResourceLocation(VanillaArcana.MOD_ID, name);
        EntityType<T> entity = EntityType.Builder.of(factory, MobCategory.MONSTER).sized(width,height).setTrackingRange(15).setUpdateInterval(2).build(location.toString());
        return ENTITIES.register(name, ()->entity);
    }

    @SubscribeEvent
    public static void addEntityAttributes(EntityAttributeCreationEvent event) {
        event.put(TAMED_ZOMBIE.get(), TamedZombie.createAttributes().build());
    }

    @SubscribeEvent
    public static void registerEntityRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(TAMED_ZOMBIE.get(), ZombieRenderer::new);
    }

    //Enchantments
    public static final DeferredRegister<Enchantment> ENCHANTMENTS = DeferredRegister.create(ForgeRegistries.ENCHANTMENTS, VanillaArcana.MOD_ID);
    public static final EnchantmentCategory WAND_CATEGORY = EnchantmentCategory.create("wand", item -> item instanceof WandItem);

    //public static final RegistryObject<Enchantment> ALACRITY = ENCHANTMENTS.register("alacrity", AlacrityEnchantment::new);

    public static final RegistryObject<Enchantment> AEGIS = ENCHANTMENTS.register("aegis",AegisEnchantment::new);
    public static final RegistryObject<Enchantment> AEROTHURGE = ENCHANTMENTS.register("aerothurge", AerothurgeEnchantment::new);
    public static final RegistryObject<Enchantment> PYROKINESIS = ENCHANTMENTS.register("pyrokinesis", PyrokinesisEnchantment::new);
    public static final RegistryObject<Enchantment> SYPHON = ENCHANTMENTS.register("syphon", SyphonEnchantment::new);
    public static final RegistryObject<Enchantment> WARP = ENCHANTMENTS.register("warp", WarpEnchantment::new);
    public static final RegistryObject<Enchantment> NECROMANCY = ENCHANTMENTS.register("necromancy", NecromancyEnchantment::new);
    public static final RegistryObject<Enchantment> FROST = ENCHANTMENTS.register("frost", FrostEnchantment::new);
    
    public static void register(IEventBus eventBus) { //Add the list of our items to the deferred register
        SOUNDS.register(eventBus);
        BLOCKS.register(eventBus);
        ITEMS.register(eventBus);
        ENTITIES.register(eventBus);
        ENCHANTMENTS.register(eventBus);
        
    }
}
