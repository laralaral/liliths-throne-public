package com.lilithsthrone.game.inventory.weapon;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.lilithsthrone.game.character.GameCharacter;
import com.lilithsthrone.game.character.attributes.Attribute;
import com.lilithsthrone.game.combat.DamageLevel;
import com.lilithsthrone.game.combat.DamageType;
import com.lilithsthrone.game.combat.DamageVariance;
import com.lilithsthrone.game.combat.Spell;
import com.lilithsthrone.game.inventory.InventorySlot;
import com.lilithsthrone.game.inventory.Rarity;
import com.lilithsthrone.utils.Util;
import com.lilithsthrone.utils.Util.ListValue;
import com.lilithsthrone.utils.Util.Value;

/**
 * @since 0.1.84
 * @version 0.1.84
 * @author Innoxia
 */
public class WeaponType {
	
	public static AbstractWeaponType MELEE_CHAOS_RARE = new AbstractWeaponType("an",
			"it",
			"opaque demonstone",
			"opaque demonstones",
			"A common type of demonstone, the power of which can be harnessed as a weapon."
					+ " Demonstones are rumoured to be crystallised essences of a Lilin's orgasm.",
			InventorySlot.WEAPON_MAIN,
			"meleeCrystal1",
			Rarity.RARE,
			Util.newArrayListOfValues(new ListValue<DamageType>(DamageType.PHYSICAL), new ListValue<DamageType>(DamageType.FIRE), new ListValue<DamageType>(DamageType.ICE), new ListValue<DamageType>(DamageType.POISON)),
			DamageLevel.POOR,
			DamageVariance.MEDIUM,
			null,
			null) {
		
		private static final long serialVersionUID = 1L;
				
		@Override
		public String equipText(GameCharacter character) {
			return "You focus on the energy in the crystal. As you do so, it dissolves and flows into your body, granting you the ability to perform magical attacks.";
		}

		@Override
		public String unequipText(GameCharacter character) {
			return "You focus on the energy inside of you, forcing it out from your body. As you do so, it reforms back into a demonstone.";
		}

		@Override
		public String getAttackDescription(GameCharacter character, GameCharacter target, boolean isHit) {
			return genericMeleeAttackDescription(character, target, isHit);
		}
	};
	
	public static AbstractWeaponType MELEE_CHAOS_EPIC = new AbstractWeaponType("a",
			"it",
			"misty demonstone",
			"misty demonstones",
			"A powerful demonstone, the power of which can be harnessed as a weapon."
					+ " Demonstones are rumoured to be crystallised essences of a Lilin's orgasm.",
			InventorySlot.WEAPON_MAIN,
			"meleeCrystal2",
			Rarity.EPIC,
			Util.newArrayListOfValues(new ListValue<DamageType>(DamageType.PHYSICAL), new ListValue<DamageType>(DamageType.FIRE), new ListValue<DamageType>(DamageType.ICE), new ListValue<DamageType>(DamageType.POISON)),
			DamageLevel.NORMAL,
			DamageVariance.MEDIUM,
			null,
			null) {

		private static final long serialVersionUID = 1L;

		@Override
		public String equipText(GameCharacter character) {
			return "You focus on the energy in the crystal. As you do so, it dissolves and flows into your body, granting you the ability to perform magical attacks.";
		}

		@Override
		public String unequipText(GameCharacter character) {
			return "You focus on the energy inside of you, forcing it out from your body. As you do so, it reforms back into a demonstone.";
		}

		@Override
		public String getAttackDescription(GameCharacter character, GameCharacter target, boolean isHit) {
			return genericMeleeAttackDescription(character, target, isHit);
		}
	};
	
	public static AbstractWeaponType MELEE_CHAOS_LEGENDARY = new AbstractWeaponType("a",
			"it",
			"clear demonstone",
			"clear demonstones",
			"An extremely powerful demonstone, the power of which can be harnessed as a weapon."
					+ " Demonstones are rumoured to be crystallised essences of a Lilin's orgasm.",
			InventorySlot.WEAPON_MAIN,
			"meleeCrystal3",
			Rarity.LEGENDARY,
			Util.newArrayListOfValues(new ListValue<DamageType>(DamageType.FIRE)),
			DamageLevel.HIGH,
			DamageVariance.LOW,
			Util.newHashMapOfValues(new Value<Attribute, Integer>(Attribute.STRENGTH, 5)),
			Util.newArrayListOfValues(
					new ListValue<Spell>(Spell.FIREBALL_1),
					new ListValue<Spell>(Spell.FIRE_SHIELD),
					new ListValue<Spell>(Spell.FIRE_INFERNO))) {

		private static final long serialVersionUID = 1L;

		@Override
		public String equipText(GameCharacter character) {
			return "You focus on the energy in the crystal. As you do so, it dissolves and flows into your body, granting you the ability to perform magical attacks.";
		}

		@Override
		public String unequipText(GameCharacter character) {
			return "You focus on the energy inside of you, forcing it out from your body. As you do so, it reforms back into a demonstone.";
		}

		@Override
		public String getAttackDescription(GameCharacter character, GameCharacter target, boolean isHit) {
			return genericMeleeAttackDescription(character, target, isHit);
		}
	};

	// OFFHAND
	public static AbstractWeaponType OFFHAND_CHAOS_RARE = new AbstractWeaponType("a",
			"it",
			"chaos feather",
			"chaos feathers",
			"A magical feather, the power of which can be harnessed as a weapon."
					+ " Feathers like this are rumoured to have been plucked from a Lilin's wings.",
			InventorySlot.WEAPON_OFFHAND,
			"rangedFeather1",
			Rarity.RARE,
			Util.newArrayListOfValues(new ListValue<DamageType>(DamageType.PHYSICAL), new ListValue<DamageType>(DamageType.FIRE), new ListValue<DamageType>(DamageType.ICE), new ListValue<DamageType>(DamageType.POISON)),
			DamageLevel.NORMAL,
			DamageVariance.HIGH,
			null,
			null) {

		private static final long serialVersionUID = 1L;

		@Override
		public String equipText(GameCharacter character) {
			return "You focus on the energy in the feather. As you do so, it dissolves and flows into your body, granting you the ability to perform magical attacks at range.";
		}

		@Override
		public String unequipText(GameCharacter character) {
			return "You focus on the energy inside of you, forcing it out from your body. As you do so, it reforms back into a magical feather.";
		}

		@Override
		public String getAttackDescription(GameCharacter character, GameCharacter target, boolean isHit) {
			return genericRangedAttackDescription(character, target, isHit);
		}
	};
	
	public static AbstractWeaponType OFFHAND_CHAOS_EPIC = new AbstractWeaponType("a",
			"it",
			"chaos feather",
			"chaos feathers",
			"A well-preserved magical feather, the power of which can be harnessed as a weapon."
					+ " Feathers like this are rumoured to have been plucked from a Lilin's wings.",
			InventorySlot.WEAPON_OFFHAND,
			"rangedFeather2",
			Rarity.EPIC,
			Util.newArrayListOfValues(new ListValue<DamageType>(DamageType.PHYSICAL), new ListValue<DamageType>(DamageType.FIRE), new ListValue<DamageType>(DamageType.ICE), new ListValue<DamageType>(DamageType.POISON)),
			DamageLevel.HIGH,
			DamageVariance.HIGH,
			null,
			null) {
		
		private static final long serialVersionUID = 1L;

		@Override
		public String equipText(GameCharacter character) {
			return "You focus on the energy in the feather. As you do so, it dissolves and flows into your body, granting you the ability to perform magical attacks at range.";
		}

		@Override
		public String unequipText(GameCharacter character) {
			return "You focus on the energy inside of you, forcing it out from your body. As you do so, it reforms back into a magical feather.";
		}

		@Override
		public String getAttackDescription(GameCharacter character, GameCharacter target, boolean isHit) {
			return genericRangedAttackDescription(character, target,isHit);
		}
	};
	
	// I made this in one of my lunch breaks x_x
	public static AbstractWeaponType MAIN_WESTERN_KKP = new AbstractWeaponType("a",
			"it",
			"Western KKP",
			"Western KKPs",
			"A blowback-operated semi-automatic pistol, featuring an exposed hammer, a traditional double-action trigger mechanism, a single-column magazine, and a fixed barrel that also acts as the guide rod for the recoil spring.",
			InventorySlot.WEAPON_MAIN,
			"western_kkp",
			Rarity.LEGENDARY,
			Util.newArrayListOfValues(new ListValue<DamageType>(DamageType.PHYSICAL)),
			DamageLevel.ABSURD,
			DamageVariance.LOW,
			null,
			null) {
		
		private static final long serialVersionUID = 1L;

		@Override
		public String equipText(GameCharacter character) {
			return "You ready your pistol.";
		}

		@Override
		public String unequipText(GameCharacter character) {
			return "You stow your pistol away.";
		}

		@Override
		public String getAttackDescription(GameCharacter character, GameCharacter target, boolean isHit) {
			return "You just shoot them... Thankfully, there seems to be some kind of arcane force preventing your gun from actually killing them...";
		}
	};

	public static List<AbstractWeaponType> rareWeapons = new ArrayList<>(), allweapons = new ArrayList<>();
	
	public static Map<AbstractWeaponType, String> weaponToIdMap = new HashMap<>();
	public static Map<String, AbstractWeaponType> idToWeaponMap = new HashMap<>();

	static {
		
		Field[] fields = WeaponType.class.getFields();
		
		for(Field f : fields){
			
			if (AbstractWeaponType.class.isAssignableFrom(f.getType())) {
				
				AbstractWeaponType weapon;
				
				try {
					weapon = ((AbstractWeaponType) f.get(null));

					// I feel like this is stupid :thinking:
					weaponToIdMap.put(weapon, f.getName());
					idToWeaponMap.put(f.getName(), weapon);
					
					if(weapon != MAIN_WESTERN_KKP) {
						allweapons.add(weapon);
					
						if (weapon.getRarity() == Rarity.RARE) {
							rareWeapons.add(weapon);
						}
					}
					
				} catch (IllegalArgumentException | IllegalAccessException e) {
					e.printStackTrace();
				}
			}
		}
	}
}
