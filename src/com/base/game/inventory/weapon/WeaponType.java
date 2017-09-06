package com.base.game.inventory.weapon;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import com.base.game.character.GameCharacter;
import com.base.game.character.attributes.Attribute;
import com.base.game.combat.DamageLevel;
import com.base.game.combat.DamageType;
import com.base.game.combat.DamageVariance;
import com.base.game.combat.Spell;
import com.base.game.inventory.InventorySlot;
import com.base.game.inventory.Rarity;
import com.base.utils.Util;
import com.base.utils.Util.ListValue;
import com.base.utils.Util.Value;

/**
 * @since 0.1.84
 * @version 0.1.84
 * @author Innoxia
 */
public class WeaponType {
	
	public static AbstractWeaponType MELEE_CHAOS_RARE = new AbstractWeaponType("an",
			"it",
			"opaque demonstone",
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
		public String getAttackDescription(GameCharacter character, GameCharacter target) {
			return genericMeleeAttackDescription(character, target);
		}
	};
	
	public static AbstractWeaponType MELEE_CHAOS_EPIC = new AbstractWeaponType("a",
			"it",
			"misty demonstone",
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
		public String getAttackDescription(GameCharacter character, GameCharacter target) {
			return genericMeleeAttackDescription(character, target);
		}
	};
	
	public static AbstractWeaponType MELEE_CHAOS_LEGENDARY = new AbstractWeaponType("a",
			"it",
			"clear demonstone",
			"An extremely powerful demonstone, the power of which can be harnessed as a weapon."
					+ " Demonstones are rumoured to be crystallised essences of a Lilin's orgasm.",
			InventorySlot.WEAPON_MAIN,
			"meleeCrystal3",
			Rarity.LEGENDARY,
			Util.newArrayListOfValues(new ListValue<DamageType>(DamageType.FIRE)),
			DamageLevel.HIGH,
			DamageVariance.LOW,
			Util.newHashMapOfValues(new Value<Attribute, Integer>(Attribute.STRENGTH, 5)),
			Util.newArrayListOfValues(new ListValue<Spell>(Spell.FIREBALL_1), new ListValue<Spell>(Spell.FIREBALL_1), new ListValue<Spell>(Spell.FIREBALL_1))) {

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
		public String getAttackDescription(GameCharacter character, GameCharacter target) {
			return genericMeleeAttackDescription(character, target);
		}
	};

	// OFFHAND
	public static AbstractWeaponType OFFHAND_CHAOS_RARE = new AbstractWeaponType("a",
			"it",
			"chaos feather",
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
		public String getAttackDescription(GameCharacter character, GameCharacter target) {
			return genericRangedAttackDescription(character, target);
		}
	};
	
	public static AbstractWeaponType OFFHAND_CHAOS_EPIC = new AbstractWeaponType("a",
			"it",
			"chaos feather",
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
		public String getAttackDescription(GameCharacter character, GameCharacter target) {
			return genericRangedAttackDescription(character, target);
		}
	};

	public static List<AbstractWeaponType> rareWeapons = new ArrayList<>(), allweapons = new ArrayList<>();

	static {
		
		Field[] fields = WeaponType.class.getFields();
		
		for(Field f : fields){
			
			if (AbstractWeaponType.class.isAssignableFrom(f.getType())) {
				
				AbstractWeaponType weapon;
				try {
					weapon = ((AbstractWeaponType) f.get(null));
					
					allweapons.add(weapon);
					
					if (weapon.getRarity() == Rarity.RARE) {
						rareWeapons.add(weapon);
					}
					
				} catch (IllegalArgumentException | IllegalAccessException e) {
					e.printStackTrace();
				}
			}
		}
	}
}
