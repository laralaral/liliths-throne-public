package com.lilithsthrone.controller.eventListeners;

import org.w3c.dom.events.Event;
import org.w3c.dom.events.EventListener;

import com.lilithsthrone.game.character.GameCharacter;
import com.lilithsthrone.game.character.QuestLine;
import com.lilithsthrone.game.dialogue.responses.Response;
import com.lilithsthrone.game.dialogue.utils.InventoryDialogue;
import com.lilithsthrone.game.dialogue.utils.UtilText;
import com.lilithsthrone.main.Main;
import com.lilithsthrone.utils.Colour;

/**
 * @since 0.1.0
 * @version 0.1.86
 * @author Innoxia
 */
public class InventoryRemoveJinx implements EventListener {
	private GameCharacter owner;
	private int index;

	@Override
	public void handleEvent(Event event) {
		if(owner.isPlayer()) {
			Main.game.getTextStartStringBuilder()
			.append("<p>You concentrate on the magical energy in the " + (InventoryDialogue.isJinxRemovalFromFloor() ? InventoryDialogue.getWeaponFloor().getName() : InventoryDialogue.getWeapon().getName()) + ", channelling it into your "
					+ InventoryDialogue.getJinxedClothing().get(index).getName() + ".</p>" + "<p><b style='color:" + Colour.GENERIC_ARCANE.toWebHexString() + ";'>With a sudden pink flash, the "
					+ (InventoryDialogue.isJinxRemovalFromFloor() ? InventoryDialogue.getWeaponFloor().getName() : InventoryDialogue.getWeapon().getName()) + " vanishes, removing the jinx in the process.</b></p>");

			if (InventoryDialogue.isJinxRemovalFromFloor()) {
				Main.game.getActiveWorld().getCell(Main.game.getPlayer().getLocation()).getInventory().removeWeapon(InventoryDialogue.getWeaponFloor());
			} else {
				Main.game.getPlayer().removeWeapon(InventoryDialogue.getWeapon());
			}
			
//			if(Main.game.getPlayer().getClothingCurrentlyEquipped().contains(InventoryDialogue.getJinxedClothing().get(index))) {
//				for(Entry<Attribute, Integer> entry : InventoryDialogue.getJinxedClothing().get(index).getAttributeModifiers().entrySet()) {
//					Main.game.getPlayer().incrementBonusAttribute(entry.getKey(), -entry.getValue());
//				}
//			}
			
			InventoryDialogue.getJinxedClothing().get(index).setSealed(false);
//			InventoryDialogue.getJinxedClothing().get(index).getAttributeModifiers().clear();
//			InventoryDialogue.getJinxedClothing().get(index).removeBadEnchantment();
	
		} else { // NPC clothing:
			Main.game.getTextStartStringBuilder().append(
					UtilText.parse(owner,
						"<p>"
							+ "You concentrate on the magical energy in the " + (InventoryDialogue.isJinxRemovalFromFloor() ? InventoryDialogue.getWeaponFloor().getName() : InventoryDialogue.getWeapon().getName()) + ", channelling it into [npc.name]'s "
							+ InventoryDialogue.getJinxedClothing().get(index).getName() + ".</p>"
							+ "<p><b style='color:" + Colour.GENERIC_ARCANE.toWebHexString() + ";'>With a sudden pink flash, the "
								+ (InventoryDialogue.isJinxRemovalFromFloor() ? InventoryDialogue.getWeaponFloor().getName() : InventoryDialogue.getWeapon().getName()) + " vanishes, removing the jinx in the process.</b>"
						+ "</p>"));

			if (InventoryDialogue.isJinxRemovalFromFloor()) {
				Main.game.getActiveWorld().getCell(Main.game.getPlayer().getLocation()).getInventory().removeWeapon(InventoryDialogue.getWeaponFloor());
			} else {
				Main.game.getPlayer().removeWeapon(InventoryDialogue.getWeapon());
			}
			
//			if(owner.getClothingCurrentlyEquipped().contains(InventoryDialogue.getJinxedNPCClothing().get(index))) {
//				for(Entry<Attribute, Integer> entry : InventoryDialogue.getJinxedNPCClothing().get(index).getAttributeModifiers().entrySet()) {
//					owner.incrementBonusAttribute(entry.getKey(), -entry.getValue());
//				}
//			}
			
			InventoryDialogue.getJinxedNPCClothing().get(index).setSealed(false);
//			InventoryDialogue.getJinxedNPCClothing().get(index).getAttributeModifiers().clear();
//			InventoryDialogue.getJinxedNPCClothing().get(index).removeBadEnchantment();
		}
		

		InventoryDialogue.populateJinxedClothingList();

		Main.game.setContent(new Response("", "", InventoryDialogue.INVENTORY_MENU){
			@Override
			public QuestLine getQuestLine() {
				if(Main.game.getPlayer().hasQuest(QuestLine.SIDE_JINXED_CLOTHING) && !Main.game.getPlayer().isQuestCompleted(QuestLine.SIDE_JINXED_CLOTHING)) {
					return QuestLine.SIDE_JINXED_CLOTHING;
				}
				return null;
			}	
		});
	}

	public InventoryRemoveJinx setJinxIndex(int i, GameCharacter owner) {
		index = i;
		this.owner = owner;
		return this;
	}
}