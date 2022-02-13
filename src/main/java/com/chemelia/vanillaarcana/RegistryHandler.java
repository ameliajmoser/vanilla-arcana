package com.chemelia.vanillaarcana;

import com.chemelia.vanillaarcana.block.AegisBlock;
import com.chemelia.vanillaarcana.client.renderer.entity.BabyGhastRenderer;
import com.chemelia.vanillaarcana.client.renderer.entity.ThrownBlockRenderer;
import com.chemelia.vanillaarcana.enchantments.AegisEnchantment;
import com.chemelia.vanillaarcana.enchantments.AerothurgeEnchantment;
import com.chemelia.vanillaarcana.enchantments.ConjurationEnchantment;
import com.chemelia.vanillaarcana.enchantments.FrostEnchantment;
import com.chemelia.vanillaarcana.enchantments.LightningEnchantment;
import com.chemelia.vanillaarcana.enchantments.WebEnchantment;
import com.chemelia.vanillaarcana.enchantments.NecromancyEnchantment;
import com.chemelia.vanillaarcana.enchantments.PyrokinesisEnchantment;
import com.chemelia.vanillaarcana.enchantments.SparkEnchantment;
import com.chemelia.vanillaarcana.enchantments.SyphonEnchantment;
import com.chemelia.vanillaarcana.enchantments.TelekinesisEnchantment;
import com.chemelia.vanillaarcana.enchantments.WarpEnchantment;
import com.chemelia.vanillaarcana.entity.monster.TamedZombie;
import com.chemelia.vanillaarcana.entity.monster.TamedBlaze;
import com.chemelia.vanillaarcana.entity.monster.TamedEndermite;
import com.chemelia.vanillaarcana.entity.monster.TamedGhast;
import com.chemelia.vanillaarcana.entity.monster.TamedSkeleton;
import com.chemelia.vanillaarcana.entity.monster.TamedVex;
import com.chemelia.vanillaarcana.entity.monster.TamedWitherSkeleton;
import com.chemelia.vanillaarcana.entity.projectile.WebProjectile;
import com.chemelia.vanillaarcana.entity.projectile.FrostSnowball;
import com.chemelia.vanillaarcana.entity.projectile.LightningProjectile;
import com.chemelia.vanillaarcana.entity.projectile.SyphonSnowball;
import com.chemelia.vanillaarcana.entity.projectile.ThrownBlock;
import com.chemelia.vanillaarcana.item.WandItem;
import com.chemelia.vanillaarcana.item.ChorusSproutItem;
import com.chemelia.vanillaarcana.item.FocusCrystal;
import com.google.common.base.Supplier;

import net.minecraft.client.renderer.entity.BlazeRenderer;
import net.minecraft.client.renderer.entity.EndermiteRenderer;
import net.minecraft.client.renderer.entity.LlamaSpitRenderer;
import net.minecraft.client.renderer.entity.SkeletonRenderer;
import net.minecraft.client.renderer.entity.ThrownItemRenderer;
import net.minecraft.client.renderer.entity.VexRenderer;
import net.minecraft.client.renderer.entity.WitherSkeletonRenderer;
import net.minecraft.client.renderer.entity.ZombieRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
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
        //registerBlockItem(name, toReturn);
        return toReturn;
    }
    
    
    // private static <T extends Block> void registerBlockItem(String name, RegistryObject<T> block) {
    //     ITEMS.register(name, () -> new BlockItem(block .get(),
    //             new Item.Properties().tab(CreativeModeTab.TAB_MISC)));
    // }

    public static final RegistryObject<Block> AEGIS_BLOCK = registerBlock("aegis_block", ()-> new AegisBlock(BlockBehaviour.Properties
        .of(Material.GLASS)
        .strength(15F, 0.1F)
        .sound(SoundType.AMETHYST)
        .noOcclusion()
        .noDrops()
        .lightLevel((val)->{return 2;})));
        
    //Items
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, VanillaArcana.MOD_ID);

    public static final RegistryObject<Item> CHORUS_SPROUT = ITEMS.register("chorus_sprout", () -> new ChorusSproutItem(new Item.Properties()
        .stacksTo(1)
        .tab(CreativeModeTab.TAB_TOOLS)));

    public static final RegistryObject<Item> AMETHYST_WAND = ITEMS.register("amethyst_wand", () -> new WandItem(new Item.Properties()
        .stacksTo(1)
        .tab(CreativeModeTab.TAB_TOOLS), 15));
    public static final RegistryObject<Item> NETHER_WAND = ITEMS.register("nether_wand", () -> new WandItem(new Item.Properties()
        .stacksTo(1)
        .tab(CreativeModeTab.TAB_TOOLS), 30));
    public static final RegistryObject<Item> END_WAND = ITEMS.register("end_wand", () -> new WandItem(new Item.Properties()
        .stacksTo(1)
        .tab(CreativeModeTab.TAB_TOOLS), 40));
    public static final RegistryObject<Item> NECROTIC_WAND = ITEMS.register("necrotic_wand", () -> new WandItem(new Item.Properties()
        .stacksTo(1)
        .tab(CreativeModeTab.TAB_TOOLS), 20));
    public static final RegistryObject<Item> PRISMARINE_WAND = ITEMS.register("prismarine_wand", () -> new WandItem(new Item.Properties()
        .stacksTo(1)
        .tab(CreativeModeTab.TAB_TOOLS), 20));

    public static final RegistryObject<Item> ARCANE_FOCUS = ITEMS.register("arcane_focus", () -> new FocusCrystal(new Item.Properties()
        .stacksTo(1)
        .tab(CreativeModeTab.TAB_TOOLS)
        .durability(128)
    ));

    //Entities
    public static final DeferredRegister<EntityType<?>> ENTITIES = DeferredRegister.create(ForgeRegistries.ENTITIES, VanillaArcana.MOD_ID);

    public static final RegistryObject<EntityType<ThrownBlock>> THROWN_BLOCK = ENTITIES.register("thrown_block", () -> EntityType.Builder.<ThrownBlock>of(ThrownBlock::new, MobCategory.MISC)
        .setCustomClientFactory(ThrownBlock::new)
        .sized(0.5F, 0.5F)
        .clientTrackingRange(4)
        .updateInterval(20)
        .build(VanillaArcana.MOD_ID + ":thrown_block"));

        public static final RegistryObject<EntityType<SyphonSnowball>> SYPHON_SNOWBALL = registerProjectile("syphon_snowball", SyphonSnowball::new, 0.25F, 0.25F);
        public static final RegistryObject<EntityType<FrostSnowball>> FROST_SNOWBALL = registerProjectile("frost_snowball", FrostSnowball::new, 0.25F, 0.25F);
        public static final RegistryObject<EntityType<WebProjectile>> WEB_PROJECTILE = registerProjectile("web_projectile", WebProjectile::new, 0.25F, 0.25F);
        public static final RegistryObject<EntityType<LightningProjectile>> LIGHTNING_PROJECTILE = registerProjectile("lightning_projectile", LightningProjectile::new, 0.25F, 0.25F);

    private static <T extends Projectile> RegistryObject<EntityType<T>> registerProjectile(String name, EntityType.EntityFactory<T> factory, float width, float height){
        ResourceLocation location = new ResourceLocation(VanillaArcana.MOD_ID, name);
        EntityType<T> entity = EntityType.Builder.of(factory, MobCategory.MISC)
        .sized(width, height)
        .clientTrackingRange(4)
        .updateInterval(10)
        .build(location.toString());
        return ENTITIES.register(name, ()-> entity);
    }

    public static final RegistryObject<EntityType<TamedZombie>> TAMED_ZOMBIE = createTamedMonster("tamed_zombie", TamedZombie::new, 0.6F, 1.95F);
    public static final RegistryObject<EntityType<TamedSkeleton>> TAMED_SKELETON = createTamedMonster("tamed_skeleton", TamedSkeleton::new, 0.6F, 1.99F);
    public static final RegistryObject<EntityType<TamedWitherSkeleton>> TAMED_WITHER_SKELETON = createTamedMonster("tamed_wither_skeleton", TamedWitherSkeleton::new, 0.7F, 2.4F);
    //public static final RegistryObject<EntityType<TamedPhantom>> TAMED_PHANTOM = createTamedMonster("tamed_phantom", TamedPhantom::new, 0.9F, 0.5F);
    public static final RegistryObject<EntityType<TamedBlaze>> TAMED_BLAZE = createTamedMonster("tamed_blaze", TamedBlaze::new, 0.6F, 1.8F);
    public static final RegistryObject<EntityType<TamedEndermite>> TAMED_ENDERMITE = createTamedMonster("tamed_endermite", TamedEndermite::new, 0.4F, 0.3F);
    public static final RegistryObject<EntityType<TamedVex>> TAMED_VEX = createTamedMonster("tamed_vex", TamedVex::new, 0.4F, 0.8F);
    public static final RegistryObject<EntityType<TamedGhast>> TAMED_GHAST = createTamedMonster("tamed_ghast", TamedGhast::new, 4.0F, 4.0F);


    private static <T extends LivingEntity> RegistryObject<EntityType<T>> createTamedMonster(String name, EntityType.EntityFactory<T> factory, float width, float height){
        ResourceLocation location = new ResourceLocation(VanillaArcana.MOD_ID, name);
        EntityType<T> entity = EntityType.Builder.of(factory, MobCategory.MONSTER)
            .sized(width,height)
            .clientTrackingRange(8)
            .fireImmune()
            .immuneTo(Blocks.WITHER_ROSE)
            .build(location.toString());
        return ENTITIES.register(name, ()->entity);
    }

    @SubscribeEvent
    public static void addEntityAttributes(EntityAttributeCreationEvent event) {
        event.put(TAMED_ZOMBIE.get(), TamedZombie.createAttributes().build());
        event.put(TAMED_SKELETON.get(), TamedSkeleton.createAttributes().build());
        //event.put(TAMED_PHANTOM.get(), TamedWitherSkeleton.createAttributes().build());
        event.put(TAMED_WITHER_SKELETON.get(), TamedWitherSkeleton.createAttributes().build());
        event.put(TAMED_BLAZE.get(), TamedBlaze.createAttributes().build());
        event.put(TAMED_VEX.get(), TamedVex.createAttributes().build());
        event.put(TAMED_ENDERMITE.get(), TamedEndermite.createAttributes().build());
        event.put(TAMED_GHAST.get(), TamedGhast.createAttributes().build());

    }

    @SubscribeEvent
    public static void registerEntityRenderers(EntityRenderersEvent.RegisterRenderers event) {
       event.registerEntityRenderer(TAMED_ZOMBIE.get(), ZombieRenderer::new);
       event.registerEntityRenderer(TAMED_SKELETON.get(), SkeletonRenderer::new);
       //event.registerEntityRenderer(TAMED_PHANTOM.get(), PhantomRenderer::new);
       event.registerEntityRenderer(TAMED_WITHER_SKELETON.get(), WitherSkeletonRenderer::new);
       event.registerEntityRenderer(TAMED_BLAZE.get(), BlazeRenderer::new);
       event.registerEntityRenderer(TAMED_ENDERMITE.get(), EndermiteRenderer::new);
       event.registerEntityRenderer(TAMED_VEX.get(), VexRenderer::new);
       event.registerEntityRenderer(TAMED_GHAST.get(), BabyGhastRenderer::new);

       event.registerEntityRenderer(WEB_PROJECTILE.get(), ThrownItemRenderer::new);
       event.registerEntityRenderer(LIGHTNING_PROJECTILE.get(), LlamaSpitRenderer::new);
       event.registerEntityRenderer(THROWN_BLOCK.get(), ThrownBlockRenderer::new);
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
    public static final RegistryObject<Enchantment> WEB = ENCHANTMENTS.register("web", WebEnchantment::new);
    public static final RegistryObject<Enchantment> CONJURATION = ENCHANTMENTS.register("conjuration", ConjurationEnchantment::new);
    public static final RegistryObject<Enchantment> TELEKINESIS = ENCHANTMENTS.register("telekinesis", TelekinesisEnchantment::new);
    public static final RegistryObject<Enchantment> LIGHTNING = ENCHANTMENTS.register("lightning", LightningEnchantment::new);
    //public static final RegistryObject<Enchantment> SPARK = ENCHANTMENTS.register("spark", SparkEnchantment::new);


    
    public static void register(IEventBus eventBus) { //Add the list of our items to the deferred register
        SOUNDS.register(eventBus);
        BLOCKS.register(eventBus);
        ITEMS.register(eventBus);
        ENTITIES.register(eventBus);
        ENCHANTMENTS.register(eventBus);
    }
}
