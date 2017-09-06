package com.base.game.inventory.item;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.base.game.character.GameCharacter;
import com.base.game.inventory.AbstractCoreType;
import com.base.game.inventory.Rarity;
import com.base.game.inventory.clothing.AbstractClothing;
import com.base.game.inventory.enchanting.TFEssence;
import com.base.main.Main;
import com.base.utils.Colour;
import com.base.utils.Util;

/**
 * @since 0.1.84
 * @version 0.1.84
 * @author Innoxia
 */
public abstract class AbstractItemType extends AbstractCoreType implements Serializable {

	protected static final long serialVersionUID = 1L;
	
	private String determiner, name, description, pathName;
	private boolean plural;
	private Colour colourShade;
	private int value;
	private Rarity rarity;
	protected String SVGString;
	private TFEssence relatedEssence;
	protected List<ItemEffect> effects;

	public AbstractItemType(
			String determiner,
			boolean plural,
			String name,
			String description,
			String pathName,
			Colour colourShade,
			int value,
			Rarity rarity,
			TFEssence relatedEssence,
			List<ItemEffect> effects) {

		this.determiner = determiner;
		this.plural = plural;
		this.name = name;
		this.description = description;
		this.pathName = pathName;

		this.value = value;
		this.rarity = rarity;
		
		this.relatedEssence = relatedEssence;
		
		if(effects==null) {
			this.effects = new ArrayList<>();
		} else {
			this.effects=effects;
		}

		if (colourShade == null) {
			this.colourShade = com.base.utils.Colour.CLOTHING_BLACK;
		} else {
			this.colourShade = colourShade;
		}
		
		// Set this item's file image:
		try {
			InputStream is = this.getClass().getResourceAsStream("/com/base/res/items/" + pathName + ".svg");
			String s = Util.inputStreamToString(is);

			s = s.replaceAll("#ff2a2a", this.colourShade.getShades()[0]);
			s = s.replaceAll("#ff5555", this.colourShade.getShades()[1]);
			s = s.replaceAll("#ff8080", this.colourShade.getShades()[2]);
			s = s.replaceAll("#ffaaaa", this.colourShade.getShades()[3]);
			s = s.replaceAll("#ffd5d5", this.colourShade.getShades()[4]);
			SVGString = s;

			is.close();

		} catch (IOException e1) {
			e1.printStackTrace();
		}
	}

	public static AbstractItem generateItem(AbstractItemType itemType) {
		return new AbstractItem(itemType) {
			private static final long serialVersionUID = 1L;
		};
	}
	
	public List<ItemEffect> getEffects() {
		return effects;
	}
	
	public boolean canBeSold() {
		return true;
	}
	
	// Enchantments:
	
	public ItemEffectType getEnchantmentEffect() {
		return null;
	}
	
	public TFEssence getRelatedEssence() {
		return relatedEssence;
	}
	
	public AbstractItemType getEnchantmentItemType() {
		return null;
	}
	
	// Getters & setters:

	public String getDeterminer() {
		return (determiner!=null?determiner:"");
	}

	public boolean isPlural() {
		return plural;
	}

	public String getName(boolean displayName) {
		if(displayName) {
			return Util.capitaliseSentence((determiner!=null?determiner:"") + " <span style='color: " + rarity.getColour().toWebHexString() + ";'>" + name + "</span>");
		} else {
			return name;
		}
	}

	public String getDescription() {
		return description;
	}

	public String getDisplayName(boolean withRarityColour) {
		return Util.capitaliseSentence((determiner!=null?determiner:"") + (withRarityColour ? (" <span style='color: " + rarity.getColour().toWebHexString() + ";'>" + name + "</span>") : name));
	}

	public String getPathName() {
		return pathName;
	}

	public Colour getColour() {
		return colourShade;
	}

	public int getValue() {
		return value;
	}

	public String getSVGString() {
		return SVGString;
	}

	public Rarity getRarity() {
		return rarity;
	}

	public String getUseName() {
		return "use";
	}
	
	public String getUseDescription(GameCharacter user, GameCharacter target) {
		return "<p>"
					+ "You use the item."
				+ "</p>";
	}

	public boolean isAbleToBeUsedFromInventory() {
		return true;
	}
	public boolean isAbleToBeUsed(GameCharacter target) {
		if(Main.game.isInCombat() && !target.isPlayer())
			return false;
		else
			return true;
	}
	public boolean isAbleToBeUsedInSex() {
		return true;
	}
	public boolean isAbleToBeUsedInCombat() {
		return true;
	}
	
	public boolean isConsumedOnUse() {
		return true;
	}
	
	public String getUnableToBeUsedFromInventoryDescription() {
		return "This item cannot be used in this way!";
	}
	
	public String getUnableToBeUsedDescription(GameCharacter target) {
		return "This item cannot be used in this way!";
	}
	
	public String getDyeBrushEffects(AbstractClothing clothing, Colour colour) {
		return "<p>"
					+ "As you take hold of the Dye-brush, you see the purple glow around the tip growing in strength."
					+ " The closer you move it to your " + clothing.getName()
					+ ", the brighter the glow becomes, until suddenly, images of different colours start flashing through your mind."
					+ " As you touch the bristles to the " + clothing.getName() + "'s surface, the Dye-brush instantly evaporates!"
					+ " You see that the arcane enchantment has dyed the " + clothing.getName() + " " + colour.getName() + "."
				+ "</p>";
	}
}
