package com.lilithsthrone.game.dialogue.utils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import com.lilithsthrone.game.character.GameCharacter;
import com.lilithsthrone.game.character.Quest;
import com.lilithsthrone.game.character.QuestLine;
import com.lilithsthrone.game.character.npc.NPC;
import com.lilithsthrone.game.combat.Attack;
import com.lilithsthrone.game.combat.Combat;
import com.lilithsthrone.game.dialogue.DialogueNodeOld;
import com.lilithsthrone.game.dialogue.MapDisplay;
import com.lilithsthrone.game.dialogue.responses.Response;
import com.lilithsthrone.game.dialogue.responses.ResponseEffectsOnly;
import com.lilithsthrone.game.dialogue.story.CharacterCreation;
import com.lilithsthrone.game.inventory.InventorySlot;
import com.lilithsthrone.game.inventory.ShopTransaction;
import com.lilithsthrone.game.inventory.clothing.AbstractClothing;
import com.lilithsthrone.game.inventory.clothing.BlockedParts;
import com.lilithsthrone.game.inventory.clothing.ClothingType;
import com.lilithsthrone.game.inventory.clothing.CoverableArea;
import com.lilithsthrone.game.inventory.clothing.DisplacementType;
import com.lilithsthrone.game.inventory.item.AbstractItem;
import com.lilithsthrone.game.inventory.item.AbstractItemType;
import com.lilithsthrone.game.inventory.item.ItemType;
import com.lilithsthrone.game.inventory.weapon.AbstractWeapon;
import com.lilithsthrone.game.sex.Sex;
import com.lilithsthrone.game.sex.sexActions.SexActionUtility;
import com.lilithsthrone.main.Main;
import com.lilithsthrone.rendering.RenderingEngine;
import com.lilithsthrone.utils.ClothingZLayerComparator;
import com.lilithsthrone.utils.Colour;
import com.lilithsthrone.utils.Util;
import com.lilithsthrone.world.WorldType;
import com.lilithsthrone.world.places.SlaverAlley;

/**
 * @since 0.1.0
 * @version 0.1.86
 * @author Innoxia
 */
public class InventoryDialogue {
	
	// Welcome to a slightly cleaned-up hell!
	
	private static final int IDENTIFICATION_PRICE = 10;
	
	private static AbstractItem item, itemFloor;
	private static AbstractClothing clothing, clothingFloor;
	private static AbstractWeapon weapon, weaponFloor;
	private static GameCharacter owner;
	
	private static NPC inventoryNPC;
	private static InventoryInteraction interactionType;

	private static StringBuilder inventorySB = new StringBuilder(), responseSB = new StringBuilder();

	private static List<AbstractClothing> jinxedClothing = new ArrayList<>();
	private static List<AbstractClothing> jinxedNPCClothing = new ArrayList<>();

	private static boolean jinxRemovalFromFloor, buyback;

	private static int buyBackPrice, buyBackIndex;

	private static String inventoryView() {
		inventorySB = new StringBuilder();
		
		inventorySB.append(RenderingEngine.ENGINE.getInventoryPanel(inventoryNPC, buyback));
		
		return inventorySB.toString();
	}

	/**
	 * The main DialogueNode. From here, the player can gain access to all parts
	 * of their inventory.
	 */
	public static final DialogueNodeOld INVENTORY_MENU = new DialogueNodeOld("Inventory", "Return to inventory menu.", true) {
		private static final long serialVersionUID = 1L;

		@Override
		public String getLabel() {
			if(!Main.game.isInNewWorld()) {
				return "Evening's Attire";
			}
			
			if (Main.game.getDialogueFlags().quickTrade && !Main.game.isInSex() && !Main.game.isInCombat()) {
				return "Inventory (Quick-Manage is <b style='color:" + Colour.GENERIC_GOOD.toWebHexString() + ";'>ON</b>)";
			} else {
				return "Inventory";
			}
		}

		@Override
		public String getHeaderContent() {
			return inventoryView();
		}

		@Override
		public String getContent() {
			if (inventoryNPC!=null && interactionType == InventoryInteraction.TRADING) {
				return inventoryNPC.getTraderDescription();
			} else {
				return (interactionType==InventoryInteraction.CHARACTER_CREATION?CharacterCreation.getCheckingClothingDescription():"");
			}
		}
		
		@Override
		public Response getResponse(int index) { //TODO sex end
			if (index == 0) {
				return getCloseInventoryResponse();
			}

			switch(interactionType) {
				case COMBAT:
					if(index == 1) {
						return new Response("Take all", "You can't do this during combat!", null);
						
					} else if (index == 2) {
						if(Main.game.getPlayer().getClothingCurrentlyEquipped().isEmpty()) {
							return new Response("Displace all", "You aren't wearing any clothing, so there's nothing to displace!", null);
							
						} else {
							return new Response("Displace all", "Displace as much of your clothing as possible.", Combat.ENEMY_ATTACK){
								@Override
								public void effects(){
									responseSB.setLength(0);
									
									for(AbstractClothing c : Main.game.getPlayer().getClothingCurrentlyEquipped()) {
										for(BlockedParts bp : c.getClothingType().getBlockedPartsList()) {
											if(bp.displacementType != DisplacementType.REMOVE_OR_EQUIP) {
												Main.game.getPlayer().isAbleToBeDisplaced(c, bp.displacementType, true, true, Main.game.getPlayer());
												responseSB.append("<p style='text-align:center;'>"+Main.game.getPlayer().getDisplaceDescription()+"</p>");
											}
										}
									}
									Combat.setPlayerTurnText(responseSB.toString());
									Combat.attackEnemy();
									Combat.setPreviousAction(Attack.NONE);
									Main.mainController.openInventory();
								}
							};
						}
						
					} else if (index == 3) {
						if(Main.game.getPlayer().getClothingCurrentlyEquipped().isEmpty()) {
							return new Response("Replace all", "You aren't wearing any clothing, so there's nothing to replace!", null);
							
						} else {
							return new Response("Replace all", "Replace as much of your clothing as possible.", Combat.ENEMY_ATTACK){
								@Override
								public void effects(){
									responseSB.setLength(0);
									
									List<AbstractClothing> zlayerClothing = new ArrayList<>(Main.game.getPlayer().getClothingCurrentlyEquipped());
									zlayerClothing.sort(new ClothingZLayerComparator().reversed());
									
									for(AbstractClothing c : zlayerClothing) {
										for(BlockedParts bp : c.getClothingType().getBlockedPartsList()) {
											if(bp.displacementType != DisplacementType.REMOVE_OR_EQUIP) {
												Main.game.getPlayer().isAbleToBeReplaced(c, bp.displacementType, true, true, Main.game.getPlayer());
												responseSB.append("<p style='text-align:center;'>"+Main.game.getPlayer().getReplaceDescription()+"</p>");
											}
										}
									}
									
									Combat.setPlayerTurnText(responseSB.toString());
									Combat.attackEnemy();
									Combat.setPreviousAction(Attack.NONE);
									Main.mainController.openInventory();
								}
							};
						}
						
					} else if (index == 4) {
						if(Main.game.getPlayer().getClothingCurrentlyEquipped().isEmpty()) {
							return new Response("Unequip all", "You aren't wearing any clothing, so there's nothing to remove!", null);
							
						} else {
							return new Response("Unequip all", "Remove as much of your clothing as possible.", Combat.ENEMY_ATTACK){
								@Override
								public void effects(){
									responseSB.setLength(0);
									
									List<AbstractClothing> zlayerClothing = new ArrayList<>(Main.game.getPlayer().getClothingCurrentlyEquipped());
									zlayerClothing.sort(new ClothingZLayerComparator());
									
									for(AbstractClothing c : zlayerClothing) { 
										Main.game.getPlayer().unequipClothingIntoInventory(c, true, Main.game.getPlayer());
										responseSB.append("<p style='text-align:center;'>"+Main.game.getPlayer().getUnequipDescription()+"</p>");
									}
									
									Combat.setPlayerTurnText(responseSB.toString());
									Combat.attackEnemy();
									Combat.setPreviousAction(Attack.NONE);
									Main.mainController.openInventory();
								}
							};
						}
						
					} else if (index == 5) {
						if(Main.game.getPlayer().getAllClothingInInventory().isEmpty()) {
							return new Response("Equip all", "You don't have any clothing, so there's nothing to equip!", null);
							
						} else {
							return new Response("Equip all", "Equip as much of the clothing in your inventory as possible.", Combat.ENEMY_ATTACK){
								@Override
								public void effects(){
									responseSB.setLength(0);
									
									List<AbstractClothing> zlayerClothing = new ArrayList<>(Main.game.getPlayer().getAllClothingInInventory());
									zlayerClothing.sort(new ClothingZLayerComparator().reversed());
									Set<InventorySlot> slotsTaken = new HashSet<>();
									
									for(AbstractClothing c : Main.game.getPlayer().getClothingCurrentlyEquipped()) {
										slotsTaken.add(c.getClothingType().getSlot());
									}
									
									for(AbstractClothing c : zlayerClothing) {
										if(!slotsTaken.contains(c.getClothingType().getSlot())) {
											responseSB.append("<p style='text-align:center;'>"+Main.game.getPlayer().equipClothingFromInventory(c, true, Main.game.getPlayer(), Main.game.getPlayer())+"</p>");
											slotsTaken.add(c.getClothingType().getSlot());
										}
									}
									
									Combat.setPlayerTurnText(responseSB.toString());
									Combat.attackEnemy();
									Combat.setPreviousAction(Attack.NONE);
									Main.mainController.openInventory();
								}
							};
						}
						
					} else {
						return null;
					}
					
				case FULL_MANAGEMENT:
					if (index == 1) {
						if(inventoryNPC == null ) {
							if(Main.game.getPlayerCell().getInventory().getInventorySlotsTaken()==0 || Main.game.isInCombat() || Main.game.isInSex()) {
								return new Response("Take all", "Pick up everything on the ground.", null);
								
							} else {
								return new Response("Take all", "Pick up everything on the ground.", INVENTORY_MENU){
									@Override
									public void effects(){
										//TODO if this starts printing it will complain about the player's inventory being full
										//TODO optimize (what if someone stores a thousand panties somewhere?)
										int i = Main.game.getPlayerCell().getInventory().getItemsInInventory().size();
										while(i > 0) {
											Main.game.getPlayer().addItem(Main.game.getPlayerCell().getInventory().getItemsInInventory().get(i-1), true);
											i--;
										}
										
										i = Main.game.getPlayerCell().getInventory().getClothingInInventory().size();
										while(i > 0) {
											Main.game.getPlayer().addClothing(Main.game.getPlayerCell().getInventory().getClothingInInventory().get(i-1), true);
											i--;
										}
										
										i = Main.game.getPlayerCell().getInventory().getWeaponsInInventory().size();
										while(i > 0) {
											Main.game.getPlayer().addWeapon(Main.game.getPlayerCell().getInventory().getWeaponsInInventory().get(i-1), true);
											i--;
										}
									}
								};
							}
							
						} else {
							if(inventoryNPC.getInventorySlotsTaken()==0 || Main.game.isInCombat() || Main.game.isInSex()) {
								return new Response("Take all", UtilText.parse(inventoryNPC, "Take everything from [npc.name]'s inventory."), null);
								
							} else {
								return new Response("Take all", UtilText.parse(inventoryNPC, "Take everything from [npc.name]'s inventory."), INVENTORY_MENU){
									@Override
									public void effects(){
										//TODO if this starts printing it will complain about the player's inventory being full
										//TODO optimize (what if someone stores a thousand panties somewhere?)
										int i = inventoryNPC.getAllItemsInInventory().size();
										while(i > 0) {
											if(!Main.game.getPlayer().isInventoryFull() || Main.game.getPlayer().hasClothing(inventoryNPC.getAllClothingInInventory().get(i-1))) {
												Main.game.getPlayer().addItem(inventoryNPC.getAllItemsInInventory().get(i-1), false);
												inventoryNPC.removeItem(inventoryNPC.getAllItemsInInventory().get(i-1));
											}
											i--;
										}
										
										i = inventoryNPC.getAllClothingInInventory().size();
										while(i > 0) {
											if(!Main.game.getPlayer().isInventoryFull() || Main.game.getPlayer().hasClothing(inventoryNPC.getAllClothingInInventory().get(i-1))) {
												Main.game.getPlayer().addClothing(inventoryNPC.getAllClothingInInventory().get(i-1), true);
												inventoryNPC.removeClothing(inventoryNPC.getAllClothingInInventory().get(i-1));
											}
											i--;
										}
										
										i = inventoryNPC.getAllWeaponsInInventory().size();
										while(i > 0) {
											if(!Main.game.getPlayer().isInventoryFull() || Main.game.getPlayer().hasWeapon(inventoryNPC.getAllWeaponsInInventory().get(i-1))) {
												Main.game.getPlayer().addWeapon(inventoryNPC.getAllWeaponsInInventory().get(i-1), true);
												inventoryNPC.removeWeapon(inventoryNPC.getAllWeaponsInInventory().get(i-1));
											}
											i--;
										}
									}
								};
							}
						}
						
					} else if (index == 2) {
						if(Main.game.getPlayer().getClothingCurrentlyEquipped().isEmpty()) {
							return new Response("Displace all", "You aren't wearing any clothing, so there's nothing to displace!", null);
							
						} else {
							return new Response("Displace all", "Displace as much of your clothing as possible.", INVENTORY_MENU){
								@Override
								public void effects(){
									for(AbstractClothing c : Main.game.getPlayer().getClothingCurrentlyEquipped()) {
										for(BlockedParts bp : c.getClothingType().getBlockedPartsList()) {
											if(bp.displacementType != DisplacementType.REMOVE_OR_EQUIP) {
												Main.game.getPlayer().isAbleToBeDisplaced(c, bp.displacementType, true, true, Main.game.getPlayer());
												Main.game.getTextStartStringBuilder().append("<p style='text-align:center;'>"+Main.game.getPlayer().getDisplaceDescription()+"</p>");
											}
										}
									}
								}
							};
						}
						
					} else if (index == 3) {
						if(Main.game.getPlayer().getClothingCurrentlyEquipped().isEmpty()) {
							return new Response("Replace all", "You aren't wearing any clothing, so there's nothing to replace!", null);
							
						} else {
							return new Response("Replace all", "Replace as much of your clothing as possible.", INVENTORY_MENU){
								@Override
								public void effects(){
									
									List<AbstractClothing> zlayerClothing = new ArrayList<>(Main.game.getPlayer().getClothingCurrentlyEquipped());
									zlayerClothing.sort(new ClothingZLayerComparator().reversed());
									
									for(AbstractClothing c : zlayerClothing) {
										for(BlockedParts bp : c.getClothingType().getBlockedPartsList()) {
											if(bp.displacementType != DisplacementType.REMOVE_OR_EQUIP) {
												Main.game.getPlayer().isAbleToBeReplaced(c, bp.displacementType, true, true, Main.game.getPlayer());
												Main.game.getTextStartStringBuilder().append("<p style='text-align:center;'>"+Main.game.getPlayer().getReplaceDescription()+"</p>");
											}
										}
									}
								}
							};
						}
						
					} else if (index == 4) {
						if(Main.game.getPlayer().getClothingCurrentlyEquipped().isEmpty()) {
							return new Response("Unequip all", "You aren't wearing any clothing, so there's nothing to remove!", null);
							
						} else {
							return new Response("Unequip all", "Remove as much of your clothing as possible.", INVENTORY_MENU){
								@Override
								public void effects(){
									List<AbstractClothing> zlayerClothing = new ArrayList<>(Main.game.getPlayer().getClothingCurrentlyEquipped());
									zlayerClothing.sort(new ClothingZLayerComparator());
									
									for(AbstractClothing c : zlayerClothing) { 
										Main.game.getPlayer().unequipClothingIntoInventory(c, true, Main.game.getPlayer());
										Main.game.getTextStartStringBuilder().append("<p style='text-align:center;'>"+Main.game.getPlayer().getUnequipDescription()+"</p>");
									}
								}
							};
						}
						
					} else if (index == 5) {
						if(Main.game.getPlayer().getAllClothingInInventory().isEmpty()) {
							return new Response("Equip all", "You don't have any clothing, so there's nothing to equip!", null);
							
						} else {
							return new Response("Equip all", "Equip as much of the clothing in your inventory as possible.", INVENTORY_MENU){
								@Override
								public void effects(){
									List<AbstractClothing> zlayerClothing = new ArrayList<>(Main.game.getPlayer().getAllClothingInInventory());
									zlayerClothing.sort(new ClothingZLayerComparator().reversed());
									Set<InventorySlot> slotsTaken = new HashSet<>();
									
									for(AbstractClothing c : Main.game.getPlayer().getClothingCurrentlyEquipped()) {
										slotsTaken.add(c.getClothingType().getSlot());
									}
									
									for(AbstractClothing c : zlayerClothing) {
										if(!slotsTaken.contains(c.getClothingType().getSlot())) {
											Main.game.getTextStartStringBuilder().append("<p style='text-align:center;'>"+Main.game.getPlayer().equipClothingFromInventory(c, true, Main.game.getPlayer(), Main.game.getPlayer())+"</p>");
											slotsTaken.add(c.getClothingType().getSlot());
										}
									}
								}
							};
						}
						
					} else if (index == 6 && inventoryNPC != null) {
						if(inventoryNPC.getClothingCurrentlyEquipped().isEmpty()) {
							return new Response("Displace all (them)", UtilText.parse(inventoryNPC, "[npc.Name] isn't wearing any clothing, so there's nothing to displace!"), null);
							
						} else {
							return new Response("Displace all (them)", UtilText.parse(inventoryNPC, "Displace as much of [npc.name]'s clothing as possible."), INVENTORY_MENU){
								@Override
								public void effects(){
									for(AbstractClothing c : inventoryNPC.getClothingCurrentlyEquipped()) {
										for(BlockedParts bp : c.getClothingType().getBlockedPartsList()) {
											if(bp.displacementType != DisplacementType.REMOVE_OR_EQUIP) {
												inventoryNPC.isAbleToBeDisplaced(c, bp.displacementType, true, true, Main.game.getPlayer());
												Main.game.getTextStartStringBuilder().append("<p style='text-align:center;'>"+inventoryNPC.getDisplaceDescription()+"</p>");
											}
										}
									}
								}
							};
						}
						
					} else if (index == 7 && inventoryNPC != null) {
						if(inventoryNPC.getClothingCurrentlyEquipped().isEmpty()) {
							return new Response("Replace all (them)",  UtilText.parse(inventoryNPC, "[npc.Name] isn't wearing any clothing, so there's nothing to replace!"), null);
							
						} else {
							return new Response("Replace all (them)", UtilText.parse(inventoryNPC, "Replace as much of [npc.name]'s clothing as possible."), INVENTORY_MENU){
								@Override
								public void effects(){
									
									List<AbstractClothing> zlayerClothing = new ArrayList<>(inventoryNPC.getClothingCurrentlyEquipped());
									zlayerClothing.sort(new ClothingZLayerComparator().reversed());
									
									for(AbstractClothing c : zlayerClothing) {
										for(BlockedParts bp : c.getClothingType().getBlockedPartsList()) {
											if(bp.displacementType != DisplacementType.REMOVE_OR_EQUIP) {
												inventoryNPC.isAbleToBeReplaced(c, bp.displacementType, true, true, Main.game.getPlayer());
												Main.game.getTextStartStringBuilder().append("<p style='text-align:center;'>"+inventoryNPC.getReplaceDescription()+"</p>");
											}
										}
									}
								}
							};
						}
						
					} else if (index == 8 && inventoryNPC != null) {
						if(inventoryNPC.getClothingCurrentlyEquipped().isEmpty()) {
							return new Response("Unequip all (them)", UtilText.parse(inventoryNPC, "[npc.Name] isn't wearing any clothing, so there's nothing to remove!"), null);
							
						} else {
							return new Response("Unequip all (them)", UtilText.parse(inventoryNPC, "Remove as much of [npc.name]'s clothing as possible."), INVENTORY_MENU){
								@Override
								public void effects(){
									List<AbstractClothing> zlayerClothing = new ArrayList<>(inventoryNPC.getClothingCurrentlyEquipped());
									zlayerClothing.sort(new ClothingZLayerComparator());
									
									for(AbstractClothing c : zlayerClothing) { 
										inventoryNPC.unequipClothingIntoInventory(c, true, Main.game.getPlayer());
										Main.game.getTextStartStringBuilder().append("<p style='text-align:center;'>"+inventoryNPC.getUnequipDescription()+"</p>");
									}
								}
							};
						}
						
					} else if (index == 10 && !Main.game.isInSex() && !Main.game.isInCombat()) {
						return getQuickTradeResponse();
						
					} else {
						return null;
					}
					
				case CHARACTER_CREATION:
					if (index == 1) {
						if(Main.game.getPlayer().isCoverableAreaExposed(CoverableArea.NIPPLES)
								|| Main.game.getPlayer().isCoverableAreaExposed(CoverableArea.ANUS)
								|| Main.game.getPlayer().isCoverableAreaExposed(CoverableArea.PENIS)
								|| Main.game.getPlayer().isCoverableAreaExposed(CoverableArea.VAGINA)
								|| Main.game.getPlayer().getClothingInSlot(InventorySlot.FOOT)==null) {
							return new Response("To the stage", "You need to be wearing clothing that covers your body, as well as a pair of shoes.", null);
							
						} else {
							return new Response("To the stage", "You're ready to approach the stage now.", CharacterCreation.CHOOSE_BACKGROUND) {
								@Override
								public void effects() {
									CharacterCreation.moveNPCIntoPlayerTile();
								}
							};
						}
						
					} else {
						return null;
					}
					
				case TRADING:
					if (index == 1) {
						if(inventoryNPC != null ||Main.game.getPlayerCell().getInventory().getInventorySlotsTaken()==0 || Main.game.isInCombat() || Main.game.isInSex()) {
							return new Response("Take all", "Pick up everything on the ground.", null);
							
						} else {
							return new Response("Take all", "Pick up everything on the ground.", INVENTORY_MENU){
								@Override
								public void effects(){
									//TODO if this starts printing it will complain about the player's inventory being full
									//TODO optimize (what if someone stores a thousand panties somewhere?)
									int i = Main.game.getPlayerCell().getInventory().getItemsInInventory().size();
									while(i > 0) {
										Main.game.getPlayer().addItem(Main.game.getPlayerCell().getInventory().getItemsInInventory().get(i-1), true);
										i--;
									}
									
									i = Main.game.getPlayerCell().getInventory().getClothingInInventory().size();
									while(i > 0) {
										Main.game.getPlayer().addClothing(Main.game.getPlayerCell().getInventory().getClothingInInventory().get(i-1), true);
										i--;
									}
									
									i = Main.game.getPlayerCell().getInventory().getWeaponsInInventory().size();
									while(i > 0) {
										Main.game.getPlayer().addWeapon(Main.game.getPlayerCell().getInventory().getWeaponsInInventory().get(i-1), true);
										i--;
									}
								}
							};
						}
						
					} else if (index == 2) {
						if(Main.game.getPlayer().getClothingCurrentlyEquipped().isEmpty()) {
							return new Response("Displace all", "You aren't wearing any clothing, so there's nothing to displace!", null);
							
						} else {
							return new Response("Displace all", "Displace as much of your clothing as possible.", INVENTORY_MENU){
								@Override
								public void effects(){
									for(AbstractClothing c : Main.game.getPlayer().getClothingCurrentlyEquipped()) {
										for(BlockedParts bp : c.getClothingType().getBlockedPartsList()) {
											if(bp.displacementType != DisplacementType.REMOVE_OR_EQUIP) {
												Main.game.getPlayer().isAbleToBeDisplaced(c, bp.displacementType, true, true, Main.game.getPlayer());
												Main.game.getTextStartStringBuilder().append("<p style='text-align:center;'>"+Main.game.getPlayer().getDisplaceDescription()+"</p>");
											}
										}
									}
								}
							};
						}
						
					} else if (index == 3) {
						if(Main.game.getPlayer().getClothingCurrentlyEquipped().isEmpty()) {
							return new Response("Replace all", "You aren't wearing any clothing, so there's nothing to replace!", null);
							
						} else {
							return new Response("Replace all", "Replace as much of your clothing as possible.", INVENTORY_MENU){
								@Override
								public void effects(){
									
									List<AbstractClothing> zlayerClothing = new ArrayList<>(Main.game.getPlayer().getClothingCurrentlyEquipped());
									zlayerClothing.sort(new ClothingZLayerComparator().reversed());
									
									for(AbstractClothing c : zlayerClothing) {
										for(BlockedParts bp : c.getClothingType().getBlockedPartsList()) {
											if(bp.displacementType != DisplacementType.REMOVE_OR_EQUIP) {
												Main.game.getPlayer().isAbleToBeReplaced(c, bp.displacementType, true, true, Main.game.getPlayer());
												Main.game.getTextStartStringBuilder().append("<p style='text-align:center;'>"+Main.game.getPlayer().getReplaceDescription()+"</p>");
											}
										}
									}
								}
							};
						}
						
					} else if (index == 4) {
						if(Main.game.getPlayer().getClothingCurrentlyEquipped().isEmpty()) {
							return new Response("Unequip all", "You aren't wearing any clothing, so there's nothing to remove!", null);
							
						} else {
							return new Response("Unequip all", "Remove as much of your clothing as possible.", INVENTORY_MENU){
								@Override
								public void effects(){
									List<AbstractClothing> zlayerClothing = new ArrayList<>(Main.game.getPlayer().getClothingCurrentlyEquipped());
									zlayerClothing.sort(new ClothingZLayerComparator());
									
									for(AbstractClothing c : zlayerClothing) { 
										Main.game.getPlayer().unequipClothingIntoInventory(c, true, Main.game.getPlayer());
										Main.game.getTextStartStringBuilder().append("<p style='text-align:center;'>"+Main.game.getPlayer().getUnequipDescription()+"</p>");
									}
								}
							};
						}
						
					} else if (index == 5) {
						if(Main.game.getPlayer().getAllClothingInInventory().isEmpty()) {
							return new Response("Equip all", "You don't have any clothing, so there's nothing to equip!", null);
							
						} else {
							return new Response("Equip all", "Equip as much of the clothing in your inventory as possible.", INVENTORY_MENU){
								@Override
								public void effects(){
									List<AbstractClothing> zlayerClothing = new ArrayList<>(Main.game.getPlayer().getAllClothingInInventory());
									zlayerClothing.sort(new ClothingZLayerComparator().reversed());
									Set<InventorySlot> slotsTaken = new HashSet<>();
									
									for(AbstractClothing c : Main.game.getPlayer().getClothingCurrentlyEquipped()) {
										slotsTaken.add(c.getClothingType().getSlot());
									}
									
									for(AbstractClothing c : zlayerClothing) {
										if(!slotsTaken.contains(c.getClothingType().getSlot())) {
											Main.game.getTextStartStringBuilder().append("<p style='text-align:center;'>"+Main.game.getPlayer().equipClothingFromInventory(c, true, Main.game.getPlayer(), Main.game.getPlayer())+"</p>");
											slotsTaken.add(c.getClothingType().getSlot());
										}
									}
								}
							};
						}
						
					} else if (index == 9 && inventoryNPC!=null) {
						return getBuybackResponse();
						
					} else if (index == 10 && !Main.game.isInSex() && !Main.game.isInCombat()) {
						return getQuickTradeResponse();
						
					} else {
						return null;
					}
					
				case SEX:
					if(index == 1) {
						return new Response("Take all", "Pick up everything on the ground.", null);
						
					} else if (index == 2) {
						if(Main.game.getPlayer().getClothingCurrentlyEquipped().isEmpty()) {
							return new Response("Displace all", "You aren't wearing any clothing, so there's nothing to displace!", null);
							
						} else {
							return new Response("Displace all", "Displace as much of your clothing as possible.", Sex.SEX_DIALOGUE){
								@Override
								public void effects(){
									responseSB.setLength(0);
									
									for(AbstractClothing c : Main.game.getPlayer().getClothingCurrentlyEquipped()) {
										for(BlockedParts bp : c.getClothingType().getBlockedPartsList()) {
											if(bp.displacementType != DisplacementType.REMOVE_OR_EQUIP) {
												Main.game.getPlayer().isAbleToBeDisplaced(c, bp.displacementType, true, true, Main.game.getPlayer());
												responseSB.append("<p style='text-align:center;'>"+Main.game.getPlayer().getDisplaceDescription()+"</p>");
											}
										}
									}
									
									Sex.setUnequipClothingText(responseSB.toString());
									Main.mainController.openInventory();
									Sex.endSexTurn(SexActionUtility.CLOTHING_REMOVAL);
									Sex.setSexStarted(true);
								}
							};
						}
						
					} else if (index == 3) {
						return new Response("Replace all", "You can't replace clothing in sex!", null);
						
					} else if (index == 4) {
						if(Main.game.getPlayer().getClothingCurrentlyEquipped().isEmpty()) {
							return new Response("Unequip all", "You aren't wearing any clothing, so there's nothing to remove!", null);
							
						} else {
							return new Response("Unequip all", "Remove as much of your clothing as possible.", Sex.SEX_DIALOGUE){
								@Override
								public void effects(){
									responseSB.setLength(0);
									
									List<AbstractClothing> zlayerClothing = new ArrayList<>(Main.game.getPlayer().getClothingCurrentlyEquipped());
									zlayerClothing.sort(new ClothingZLayerComparator());
									
									for(AbstractClothing c : zlayerClothing) { 
										if(!c.getClothingType().getSlot().isJewellery()) {
											Main.game.getPlayer().unequipClothingIntoInventory(c, true, Main.game.getPlayer());
											responseSB.append("<p style='text-align:center;'>"+Main.game.getPlayer().getUnequipDescription()+"</p>");
										}
									}
									
									Sex.setUnequipClothingText(responseSB.toString());
									Main.mainController.openInventory();
									Sex.endSexTurn(SexActionUtility.CLOTHING_REMOVAL);
									Sex.setSexStarted(true);
								}
							};
						}
						
					} else if (index == 5) {
						return new Response("Equip all", "You can't equip clothing in sex!", null);
							
					} else if (index == 6 && inventoryNPC != null) {
						if(inventoryNPC.getClothingCurrentlyEquipped().isEmpty()) {
							return new Response("Displace all (them)", UtilText.parse(inventoryNPC, "[npc.Name] isn't wearing any clothing, so there's nothing to displace!"), null);
							
						} else {
							return new Response("Displace all (them)", UtilText.parse(inventoryNPC, "Displace as much of [npc.name]'s clothing as possible."), Sex.SEX_DIALOGUE){
								@Override
								public void effects(){
									responseSB.setLength(0);
									
									for(AbstractClothing c : inventoryNPC.getClothingCurrentlyEquipped()) {
										for(BlockedParts bp : c.getClothingType().getBlockedPartsList()) {
											if(bp.displacementType != DisplacementType.REMOVE_OR_EQUIP) {
												inventoryNPC.isAbleToBeDisplaced(c, bp.displacementType, true, true, Main.game.getPlayer());
												responseSB.append("<p style='text-align:center;'>"+inventoryNPC.getDisplaceDescription()+"</p>");
											}
										}
									}
									
									Sex.setUnequipClothingText(responseSB.toString());
									Main.mainController.openInventory();
									Sex.endSexTurn(SexActionUtility.CLOTHING_REMOVAL);
									Sex.setSexStarted(true);
								}
							};
						}
						
					} else if (index == 7) {
						return new Response("Replace all (them)", "You can't replace clothing in sex!", null);
						
					} else if (index == 8) {
						if(inventoryNPC.getClothingCurrentlyEquipped().isEmpty()) {
							return new Response("Unequip all (them)", UtilText.parse(inventoryNPC, "[npc.Name] isn't wearing any clothing, so there's nothing to remove!"), null);
							
						} else {
							return new Response("Unequip all (them)", UtilText.parse(inventoryNPC, "Remove as much of [npc.name]'s clothing as possible."), Sex.SEX_DIALOGUE){
								@Override
								public void effects(){
									responseSB.setLength(0);
									
									List<AbstractClothing> zlayerClothing = new ArrayList<>(inventoryNPC.getClothingCurrentlyEquipped());
									zlayerClothing.sort(new ClothingZLayerComparator());
									
									for(AbstractClothing c : zlayerClothing) { 
										if(!c.getClothingType().getSlot().isJewellery()) {
											inventoryNPC.unequipClothingIntoInventory(c, true, Main.game.getPlayer());
											responseSB.append("<p style='text-align:center;'>"+inventoryNPC.getUnequipDescription()+"</p>");
										}
									}
									
									Sex.setUnequipClothingText(responseSB.toString());
									Main.mainController.openInventory();
									Sex.endSexTurn(SexActionUtility.CLOTHING_REMOVAL);
									Sex.setSexStarted(true);
								}
							};
						}
						
					} else {
						return null;
					}
			}
			
			return null;
		}

		@Override
		public MapDisplay getMapDisplay() {
			return MapDisplay.INVENTORY;
		}
	};

	public static final DialogueNodeOld ITEM_INVENTORY = new DialogueNodeOld("Item", "", true) {
		private static final long serialVersionUID = 1L;

		@Override
		public String getLabel() {
			if (Main.game.getDialogueFlags().quickTrade && !Main.game.isInSex() && !Main.game.isInCombat()) {
				return "Inventory (Quick-Manage is <b style='color:" + Colour.GENERIC_GOOD.toWebHexString() + ";'>ON</b>)";
			} else {
				return "Inventory";
			}
		}

		@Override
		public String getHeaderContent() {
			return inventoryView();
		}
		
		@Override
		public String getContent() {
			return getItemDisplayPanel(item.getSVGString(),
					item.getDisplayName(true),
					item.getDescription()
					+ item.getExtraDescription(owner, owner)
					+ (owner!=null && owner.isPlayer()
							? (inventoryNPC != null && interactionType == InventoryInteraction.TRADING
									? inventoryNPC.willBuy(item)
											? "<p>"
												+ inventoryNPC.getName("The") + " will buy it for " + UtilText.formatAsMoney(item.getPrice(inventoryNPC.getBuyModifier())) + "."
											+ "</p>" 
											: inventoryNPC.getName("The") + " doesn't want to buy this."
										: "")
							:(inventoryNPC != null && interactionType == InventoryInteraction.TRADING
								? "<p>"
										+ inventoryNPC.getName("The") + " will sell it for " + UtilText.formatAsMoney(item.getPrice(inventoryNPC.getSellModifier())) + "."
									+ "</p>" 
								: "")));
		}

		@Override
		public Response getResponse(int index) {
			if (index == 0) {
				return getReturnToInventoryMenuResponse();
			}
			// ****************************** ITEM BELONGS TO THE PLAYER ******************************
			if(owner != null && owner.isPlayer()) {
				
				// ****************************** Interacting with the ground ******************************
				if(inventoryNPC == null) {
					boolean areaFull = Main.game.isPlayerTileFull() && !Main.game.getCurrentCell().getInventory().hasItem(item);
					
					if(index == 1) {
						if(owner.getLocationPlace().isItemsDisappear()) {
							if(areaFull) {
								return new Response("Drop (1)", "This area is full, so you can't drop your " + item.getName() + " here!", null);
							} else {
								return new Response("Drop (1)", "Drop your " + item.getName() + ".", INVENTORY_MENU){
									@Override
									public void effects(){
										dropItems(owner, item, 1);
									}
								};
							}
						} else {
							if(areaFull) {
								return new Response("Store (1)", "This area is full, so you can't store your " + item.getName() + " here!", null);
							} else {
								return new Response("Store (1)", "Store the " + item.getName() + " in this area.", INVENTORY_MENU){
									@Override
									public void effects(){
										dropItems(owner, item, 1);
									}
								};
							}
						}
						
					} else if(index == 2) {
						if(owner.getLocationPlace().isItemsDisappear()) {
							if(owner.getItemCount(item) < 5) {
								return new Response("Drop (5)", "You don't have five " + item.getNamePlural() + " to give!", null);
								
							} else if(areaFull) {
								return new Response("Drop (5)", "This area is full, so you can't drop your " + item.getNamePlural() + " here!", null);
								
							} else {
								return new Response("Drop (5)", "Drop five of your " + item.getNamePlural() + ".", INVENTORY_MENU){
									@Override
									public void effects(){
										dropItems(owner, item, 5);
									}
								};
							}
						} else {
							if(owner.getItemCount(item) < 5) {
								return new Response("Store (5)", "You don't have five " + item.getNamePlural() + " to give!", null);
								
							} else if(areaFull) {
								return new Response("Store (5)", "This area is full, so you can't store your " + item.getNamePlural() + " here!", null);
								
							} else {
								return new Response("Store (5)", "Store five of your " + item.getNamePlural() + " in this area.", INVENTORY_MENU){
									@Override
									public void effects(){
										dropItems(owner, item, 5);
									}
								};
							}
						}
						
					} else if(index == 3) {
						if(owner.getLocationPlace().isItemsDisappear()) {
							if(areaFull) {
								return new Response("Drop (All)", "This area is full, so you can't drop your " + item.getNamePlural() + " here!", null);
							} else {
								return new Response("Drop (All)", "Drop all of your " + item.getNamePlural() + ".", INVENTORY_MENU){
									@Override
									public void effects(){
										dropItems(owner, item, owner.getItemCount(item));
									}
								};
							}
						} else {
							if(areaFull) {
								return new Response("Store (All)", "This area is full, so you can't store your " + item.getNamePlural() + " here!", null);
							} else {
								return new Response("Store (All)", "Store all of your " + item.getNamePlural() + " in this area.", INVENTORY_MENU){
									@Override
									public void effects(){
										dropItems(owner, item, owner.getItemCount(item));
									}
								};
							}
						}
						
					} else if(index == 5) {
						if(item.getEnchantmentItemType()==null) {
							return new Response("Enchant", "This item cannot be enchanted!", null);
							
						} else if(Main.game.isDebugMode()) {
							return new Response("Enchant", "Enchant this item.", EnchantmentDialogue.ENCHANTMENT_MENU) {
								@Override
								public void effects() {
									EnchantmentDialogue.effects.clear();
									EnchantmentDialogue.resetEnchantmentVariables();
									EnchantmentDialogue.ingredient = item;
								}
							};
							
						} else if(Main.game.getPlayer().hasQuest(QuestLine.SIDE_ENCHANTMENT_DISCOVERY)) {
							if(Main.game.getPlayer().isQuestProgressGreaterThan(QuestLine.SIDE_ENCHANTMENT_DISCOVERY, Quest.SIDE_ENCHANTMENTS_LILAYA_HELP)) {
								return new Response("Enchant", "Enchant this item.", EnchantmentDialogue.ENCHANTMENT_MENU) {
									@Override
									public void effects() {
										EnchantmentDialogue.effects.clear();
										EnchantmentDialogue.resetEnchantmentVariables();
										EnchantmentDialogue.ingredient = item;
									}
								};
							}
						}
						
						return null;
						
					} else if(index == 6) {
						if (!item.isAbleToBeUsedFromInventory()) {
							return new Response(Util.capitaliseSentence(item.getItemType().getUseName())+" (self)", item.getUnableToBeUsedFromInventoryDescription(), null);
							
						} else if (!item.isAbleToBeUsed(Main.game.getPlayer())) {
							return new Response(Util.capitaliseSentence(item.getItemType().getUseName()) +" (self)", item.getUnableToBeUsedDescription(Main.game.getPlayer()), null);
						} else {
							return new Response(Util.capitaliseSentence(item.getItemType().getUseName()) +" (self)",
									Util.capitaliseSentence(item.getItemType().getUseName()) + " the " + item.getName() + ".", INVENTORY_MENU){
								@Override
								public void effects(){
									Main.game.getTextStartStringBuilder().append("<p style='text-align:center;'>" + Main.game.getPlayer().useItem(item, Main.game.getPlayer(), false) + "</p>");
								}
							};
						}
						
					} else if(index == 7) {
						if (!item.isAbleToBeUsedFromInventory()) {
							return new Response(Util.capitaliseSentence(item.getItemType().getUseName())+" all (self)", item.getUnableToBeUsedFromInventoryDescription(), null);
							
						} else if(!item.isAbleToBeUsed(Main.game.getPlayer())) {
							return new Response(Util.capitaliseSentence(item.getItemType().getUseName())+" all (self)", item.getUnableToBeUsedDescription(Main.game.getPlayer()), null);
							
						} else {
							return new Response(Util.capitaliseSentence(item.getItemType().getUseName())+" all (self)",
									Util.capitaliseSentence(item.getItemType().getUseName()) + " all of the " + item.getName() + " that are currently in your inventory.", INVENTORY_MENU){
								@Override
								public void effects(){
									int itemCount = Main.game.getPlayer().getItemCount(item);
									for(int i=0;i<itemCount;i++) {
										Main.game.getTextStartStringBuilder().append("<p style='text-align:center;'>" + Main.game.getPlayer().useItem(item, Main.game.getPlayer(), false) + "</p>");
									}
								}
							};
						}
						
					} else if (index == 10) {
						return getQuickTradeResponse();
						
					} else if(index == 11) {
						return new Response(Util.capitaliseSentence(item.getItemType().getUseName()) +" (them)", "There's nobody to use your "+item.getName()+" on!", null);
						
					} else if(index == 12) {
						return new Response(Util.capitaliseSentence(item.getItemType().getUseName()) +" all (them)", "There's nobody to use your "+item.getName()+" on!", null);
						
					} else {
						return null;
					}
					
					
				// ****************************** Interacting with an NPC ******************************
				} else {
					switch(interactionType) {
						case COMBAT:
							if(index == 1) {
								return new Response("Give (1)", "You can't give someone items while fighting them!", null);
								
							} else if(index == 2) {
								return new Response("Give (5)", "You can't give someone items while fighting them!", null);
								
							} else if(index == 3) {
								return new Response("Give (All)", "You can't give someone items while fighting them!", null);
								
							} else if(index == 5) {
								return new Response("Enchant", "You can't enchant items while fighting someone!", null);
								
							} else if(index == 6) {
								if (!item.isAbleToBeUsedInCombat()) {
									return new Response(Util.capitaliseSentence(item.getItemType().getUseName())+" (self)", "You cannot use this during combat!", null);
									
								} else if (!item.isAbleToBeUsedFromInventory()) {
									return new Response(Util.capitaliseSentence(item.getItemType().getUseName())+" (self)", item.getUnableToBeUsedFromInventoryDescription(), null);
									
								} else if (!item.isAbleToBeUsed(Main.game.getPlayer())) {
									return new Response(Util.capitaliseSentence(item.getItemType().getUseName()) +" (self)", item.getUnableToBeUsedDescription(Main.game.getPlayer()), null);
									
								} else {
									return new Response(Util.capitaliseSentence(item.getItemType().getUseName()) +" (self)", Util.capitaliseSentence(item.getItemType().getUseName()) + " the " + item.getName() + ".", Combat.ENEMY_ATTACK){
										@Override
										public void effects(){
											Combat.setPlayerTurnText(Main.game.getPlayer().useItem(item, Main.game.getPlayer(), false));
											Combat.attackEnemy();
											Combat.setPreviousAction(Attack.NONE);
											Main.mainController.openInventory();
										}
									};
								}
								
							} else if(index == 7) {
								return new Response(Util.capitaliseSentence(item.getItemType().getUseName())+" all (self)", "You can only use one item at a time during combat!", null);
								
							} else if (index == 10) {
								return getQuickTradeResponse();
								
							} else if(index == 11) {
								if (!item.isAbleToBeUsedInCombat()) {
									return new Response(Util.capitaliseSentence(item.getItemType().getUseName())+" (opponent)", "You cannot use this during combat!", null);
									
								} else if (!item.isAbleToBeUsedFromInventory()) {
									return new Response(Util.capitaliseSentence(item.getItemType().getUseName())+" (opponent)", item.getUnableToBeUsedFromInventoryDescription(), null);
									
								} else if (!item.isAbleToBeUsed(inventoryNPC)) {
									return new Response(Util.capitaliseSentence(item.getItemType().getUseName()) +" (opponent)", item.getUnableToBeUsedDescription(inventoryNPC), null);
									
								} else {
									return new Response(Util.capitaliseSentence(item.getItemType().getUseName()) +" (opponent)",
											"Get "+inventoryNPC.getName("the")+" to "+ item.getItemType().getUseName() + " the " + item.getName() + ".", Combat.ENEMY_ATTACK){
										@Override
										public void effects(){
											Combat.setPlayerTurnText(Main.game.getPlayer().useItem(item, inventoryNPC, false));
											Combat.attackEnemy();
											Combat.setPreviousAction(Attack.NONE);
											Main.mainController.openInventory();
										}
									};
								}
								
							} else if(index == 12) {
								return new Response(Util.capitaliseSentence(item.getItemType().getUseName())+" all (opponent)", "You can only use one item at a time during combat!", null);
								
							} else {
								return null;
							}
							
						case FULL_MANAGEMENT:  case CHARACTER_CREATION:
							boolean inventoryFull = inventoryNPC.isInventoryFull() && !inventoryNPC.hasItem(item);
							
							if(index == 1) {
								if(inventoryFull) {
									return new Response("Give (1)", UtilText.parse(inventoryNPC, "[npc.Name]'s inventory is already full!"), null);
								}
								return new Response("Give (1)", UtilText.parse(inventoryNPC, "Give [npc.name] " + item.getItemType().getDeterminer() + " " + item.getName() + "."), INVENTORY_MENU){
									@Override
									public void effects(){
										transferItems(Main.game.getPlayer(), inventoryNPC, item, 1);
									}
								};
								
							} else if(index == 2) {
								if(inventoryFull) {
									return new Response("Give (5)", UtilText.parse(inventoryNPC, "[npc.Name]'s inventory is already full!"), null);
								}
								if(Main.game.getPlayer().getItemCount(item) >= 5) {
									return new Response("Give (5)", UtilText.parse(inventoryNPC, "Give [npc.name] five of your " + item.getNamePlural() + "."), INVENTORY_MENU){
										@Override
										public void effects(){
											transferItems(Main.game.getPlayer(), inventoryNPC, item, 5);
										}
									};
								} else {
									return new Response("Give (5)", "You don't have five " + item.getNamePlural() + " to give!", null);
								}
								
							} else if(index == 3) {
								if(inventoryFull) {
									return new Response("Give (All)", UtilText.parse(inventoryNPC, "[npc.Name]'s inventory is already full!"), null);
								}
								return new Response("Give (All)", UtilText.parse(inventoryNPC, "Give [npc.name] all of your " + item.getNamePlural() + "."), INVENTORY_MENU){
									@Override
									public void effects(){
										transferItems(Main.game.getPlayer(), inventoryNPC, item, Main.game.getPlayer().getItemCount(item));
									}
								};
								
							} else if(index == 5) {
								if(item.getEnchantmentItemType()==null) {
									return new Response("Enchant", "This item cannot be enchanted!", null);
									
								} else if(Main.game.isDebugMode()) {
									return new Response("Enchant", "Enchant this item.", EnchantmentDialogue.ENCHANTMENT_MENU) {
										@Override
										public void effects() {
											EnchantmentDialogue.effects.clear();
											EnchantmentDialogue.resetEnchantmentVariables();
											EnchantmentDialogue.ingredient = item;
										}
									};
									
								} else if(Main.game.getPlayer().hasQuest(QuestLine.SIDE_ENCHANTMENT_DISCOVERY)) {
									if(Main.game.getPlayer().isQuestProgressGreaterThan(QuestLine.SIDE_ENCHANTMENT_DISCOVERY, Quest.SIDE_ENCHANTMENTS_LILAYA_HELP)) {
										return new Response("Enchant", "Enchant this item.", EnchantmentDialogue.ENCHANTMENT_MENU) {
											@Override
											public void effects() {
												EnchantmentDialogue.effects.clear();
												EnchantmentDialogue.resetEnchantmentVariables();
												EnchantmentDialogue.ingredient = item;
											}
										};
									}
								}
								
								return null;
								
							} else if(index == 6) {
								if (!item.isAbleToBeUsedFromInventory()) {
									return new Response(Util.capitaliseSentence(item.getItemType().getUseName())+" (self)", item.getUnableToBeUsedFromInventoryDescription(), null);
									
								} else if (!item.isAbleToBeUsed(Main.game.getPlayer())) {
									return new Response(Util.capitaliseSentence(item.getItemType().getUseName()) +" (self)", item.getUnableToBeUsedDescription(Main.game.getPlayer()), null);
								} else {
									return new Response(Util.capitaliseSentence(item.getItemType().getUseName()) +" (self)",
											Util.capitaliseSentence(item.getItemType().getUseName()) + " the " + item.getName() + ".", INVENTORY_MENU){
										@Override
										public void effects(){
											Main.game.getTextStartStringBuilder().append("<p style='text-align:center;'>" + Main.game.getPlayer().useItem(item, Main.game.getPlayer(), false) + "</p>");
										}
									};
								}
								
							} else if(index == 7) {
								if (!item.isAbleToBeUsedFromInventory()) {
									return new Response(Util.capitaliseSentence(item.getItemType().getUseName())+" all (self)", item.getUnableToBeUsedFromInventoryDescription(), null);
									
								} else if(!item.isAbleToBeUsed(Main.game.getPlayer())) {
									return new Response(Util.capitaliseSentence(item.getItemType().getUseName())+" all (self)", item.getUnableToBeUsedDescription(Main.game.getPlayer()), null);
									
								} else {
									return new Response(Util.capitaliseSentence(item.getItemType().getUseName())+" all (self)",
											Util.capitaliseSentence(item.getItemType().getUseName()) + " all of the " + item.getName() + " that are currently in your inventory.", INVENTORY_MENU){
										@Override
										public void effects(){
											int itemCount = Main.game.getPlayer().getItemCount(item);
											for(int i=0;i<itemCount;i++) {
												Main.game.getTextStartStringBuilder().append("<p style='text-align:center;'>" + Main.game.getPlayer().useItem(item, Main.game.getPlayer(), false) + "</p>");
											}
										}
									};
								}
								
							} else if (index == 10) {
								return getQuickTradeResponse();
								
							} else if(index == 11) {
								if (!item.isAbleToBeUsedFromInventory()) {
									return new Response(Util.capitaliseSentence(item.getItemType().getUseName())+" (them)", item.getUnableToBeUsedFromInventoryDescription(), null);
									
								} else if (!item.isAbleToBeUsed(inventoryNPC)) {
									return new Response(Util.capitaliseSentence(item.getItemType().getUseName()) +" (them)", item.getUnableToBeUsedDescription(inventoryNPC), null);
									
								} else {
									return new Response(Util.capitaliseSentence(item.getItemType().getUseName()) +" (them)",
											"Get "+inventoryNPC.getName("the")+" to "+ item.getItemType().getUseName() + " the " + item.getName() + ".", INVENTORY_MENU){
										@Override
										public void effects(){
											Main.game.getTextStartStringBuilder().append("<p style='text-align:center;'>" + Main.game.getPlayer().useItem(item, inventoryNPC, false) + "</p>");
										}
									};
								}
							} else if(index == 12) {
								if (!item.isAbleToBeUsedFromInventory()) {
									return new Response(Util.capitaliseSentence(item.getItemType().getUseName())+" all (them)", item.getUnableToBeUsedFromInventoryDescription(), null);
									
								} else if(!item.isAbleToBeUsed(Main.game.getPlayer())) {
									return new Response(Util.capitaliseSentence(item.getItemType().getUseName())+" all (them)", item.getUnableToBeUsedDescription(Main.game.getPlayer()), null);
									
								} else {
									return new Response(Util.capitaliseSentence(item.getItemType().getUseName())+" all (them)",
											"Get "+inventoryNPC.getName("the")+" to "+ item.getItemType().getUseName() + " all of the " + item.getNamePlural() + " in your inventory.", INVENTORY_MENU){
										@Override
										public void effects(){
											int itemCount = Main.game.getPlayer().getItemCount(item);
											for(int i=0;i<itemCount;i++) {
												Main.game.getTextStartStringBuilder().append("<p style='text-align:center;'>" + Main.game.getPlayer().useItem(item, inventoryNPC, false) + "</p>");
											}
										}
									};
								}
								
							} else {
								return null;
							}
							
						case SEX:
							if(index == 1) {
								return new Response("Give (1)", "You can't give someone items while having sex with them!", null);
								
							} else if(index == 2) {
								return new Response("Give (5)", "You can't give someone items while having sex with them!", null);
								
							} else if(index == 3) {
								return new Response("Give (All)", "You can't give someone items while having sex with them!", null);
								
							} else if(index == 5) {
								return new Response("Enchant", "You can't enchant items while having sex with someone!", null);
								
							} else if(index == 6) {
								if (!item.isAbleToBeUsedInSex()) {
									return new Response(Util.capitaliseSentence(item.getItemType().getUseName())+" (self)", "You cannot use this during sex!", null);
									
								} else if (!item.isAbleToBeUsedFromInventory()) {
									return new Response(Util.capitaliseSentence(item.getItemType().getUseName())+" (self)", item.getUnableToBeUsedFromInventoryDescription(), null);
									
								} else if (!item.isAbleToBeUsed(Main.game.getPlayer())) {
									return new Response(Util.capitaliseSentence(item.getItemType().getUseName()) +" (self)", item.getUnableToBeUsedDescription(Main.game.getPlayer()), null);
									
								} else {
									return new Response(Util.capitaliseSentence(item.getItemType().getUseName()) +" (self)",
											Util.capitaliseSentence(item.getItemType().getUseName()) + " the " + item.getName() + ".", Sex.SEX_DIALOGUE){
										@Override
										public void effects(){
											Sex.setUsingItemText(Sex.getPartner().getItemUseEffects(item, Main.game.getPlayer(), Main.game.getPlayer()));
											Main.mainController.openInventory();
											Sex.endSexTurn(SexActionUtility.PLAYER_USE_ITEM);
										}
									};
								}
								
							} else if(index == 7) {
								return new Response(Util.capitaliseSentence(item.getItemType().getUseName())+" all (self)", "You can only use one item at a time during sex!", null);
								
							} else if (index == 10) {
								return getQuickTradeResponse();
								
							} else if(index == 11) {
								if (!item.isAbleToBeUsedInSex()) {
									return new Response(Util.capitaliseSentence(item.getItemType().getUseName())+" (partner)", "You cannot use this during sex!", null);
									
								} else if (!item.isAbleToBeUsedFromInventory()) {
									return new Response(Util.capitaliseSentence(item.getItemType().getUseName())+" (partner)", item.getUnableToBeUsedFromInventoryDescription(), null);
									
								} else if (!item.isAbleToBeUsed(inventoryNPC)) {
									return new Response(Util.capitaliseSentence(item.getItemType().getUseName()) +" (partner)", item.getUnableToBeUsedDescription(inventoryNPC), null);
									
								} else {
									return new Response(Util.capitaliseSentence(item.getItemType().getUseName()) +" (partner)",
											"Get "+inventoryNPC.getName("the")+" to "+ item.getItemType().getUseName() + " the " + item.getName() + ".", Sex.SEX_DIALOGUE){
										@Override
										public void effects(){
											Sex.setUsingItemText(Sex.getPartner().getItemUseEffects(item, Main.game.getPlayer(), inventoryNPC));
											Main.mainController.openInventory();
											Sex.endSexTurn(SexActionUtility.PLAYER_USE_ITEM);
										}
									};
								}
								
							} else if(index == 12) {
								return new Response(Util.capitaliseSentence(item.getItemType().getUseName())+" all (partner)", "You can only use one item at a time during sex!", null);
								
							} else {
								return null;
							}
							
						case TRADING:
							if(index == 1) {
								if (inventoryNPC.willBuy(item)) {
									int sellPrice = item.getPrice(inventoryNPC.getBuyModifier());
									return new Response("Sell (1) (" + UtilText.formatAsMoney(sellPrice, "span") + ")", "Sell the " + item.getName() + " for " + UtilText.formatAsMoney(sellPrice) + ".", INVENTORY_MENU){
										@Override
										public void effects(){
											sellItems(Main.game.getPlayer(), inventoryNPC, item, 1, sellPrice);
										}
									};
								} else {
									return new Response("Sell (1)", inventoryNPC.getName("The") + " doesn't want to buy this.", null);
								}
								
							} else if(index == 2) {
								if(Main.game.getPlayer().getItemCount(item) >= 5) {
									if (inventoryNPC.willBuy(item)) {
										int sellPrice = item.getPrice(inventoryNPC.getBuyModifier());
										return new Response("Sell (1) (" + UtilText.formatAsMoney(sellPrice*5, "span") + ")", "Sell five of your " + item.getNamePlural() + " for " + UtilText.formatAsMoney(sellPrice*5) + ".", INVENTORY_MENU){
											@Override
											public void effects(){
												sellItems(Main.game.getPlayer(), inventoryNPC, item, 5, sellPrice);
											}
										};
									} else {
										return new Response("Sell (5)", inventoryNPC.getName("The") + " doesn't want to buy these.", null);
									}
									
								} else {
									return new Response("Sell (5)", "You don't have five " + item.getNamePlural() + " to sell!", null);
								}
								
							} else if(index == 3) {
								if (inventoryNPC.willBuy(item)) {
									int sellPrice = item.getPrice(inventoryNPC.getBuyModifier());
									return new Response("Sell (All) (" + UtilText.formatAsMoney(sellPrice*Main.game.getPlayer().getItemCount(item), "span") + ")",
											"Sell the " + item.getName() + " for " + UtilText.formatAsMoney(sellPrice*Main.game.getPlayer().getItemCount(item)) + ".", INVENTORY_MENU){
										@Override
										public void effects(){
											sellItems(Main.game.getPlayer(), inventoryNPC, item, Main.game.getPlayer().getItemCount(item), sellPrice);
										}
									};
								} else {
									return new Response("Sell (All)", inventoryNPC.getName("The") + " doesn't want to buy these.", null);
								}
								
							} else if(index == 5) {
								if(item.getEnchantmentItemType()==null) {
									return new Response("Enchant", "This item cannot be enchanted!", null);
									
								} else if(Main.game.isDebugMode()) {
									return new Response("Enchant", "Enchant this item.", EnchantmentDialogue.ENCHANTMENT_MENU) {
										@Override
										public void effects() {
											EnchantmentDialogue.effects.clear();
											EnchantmentDialogue.resetEnchantmentVariables();
											EnchantmentDialogue.ingredient = item;
										}
									};
									
								} else if(Main.game.getPlayer().hasQuest(QuestLine.SIDE_ENCHANTMENT_DISCOVERY)) {
									if(Main.game.getPlayer().isQuestProgressGreaterThan(QuestLine.SIDE_ENCHANTMENT_DISCOVERY, Quest.SIDE_ENCHANTMENTS_LILAYA_HELP)) {
										return new Response("Enchant", "Enchant this item.", EnchantmentDialogue.ENCHANTMENT_MENU) {
											@Override
											public void effects() {
												EnchantmentDialogue.effects.clear();
												EnchantmentDialogue.resetEnchantmentVariables();
												EnchantmentDialogue.ingredient = item;
											}
										};
									}
								}
								
								return null;
								
							} else if(index == 6) {
								if (!item.isAbleToBeUsedFromInventory()) {
									return new Response(Util.capitaliseSentence(item.getItemType().getUseName())+" (self)", item.getUnableToBeUsedFromInventoryDescription(), null);
									
								} else if (!item.isAbleToBeUsed(Main.game.getPlayer())) {
									return new Response(Util.capitaliseSentence(item.getItemType().getUseName()) +" (self)", item.getUnableToBeUsedDescription(Main.game.getPlayer()), null);
									
								} else {
									return new Response(Util.capitaliseSentence(item.getItemType().getUseName()) +" (self)",
											Util.capitaliseSentence(item.getItemType().getUseName()) + " the " + item.getName() + ".", INVENTORY_MENU){
										@Override
										public void effects(){
											Main.game.getTextStartStringBuilder().append("<p style='text-align:center;'>" + Main.game.getPlayer().useItem(item, Main.game.getPlayer(), false) + "</p>");
										}
									};
								}
								
							} else if(index == 7) {
								if (!item.isAbleToBeUsedFromInventory()) {
									return new Response(Util.capitaliseSentence(item.getItemType().getUseName())+" all (self)", item.getUnableToBeUsedFromInventoryDescription(), null);
									
								} else if(!item.isAbleToBeUsed(Main.game.getPlayer())) {
									return new Response(Util.capitaliseSentence(item.getItemType().getUseName())+" all (self)", item.getUnableToBeUsedDescription(Main.game.getPlayer()), null);
									
								} else {
									return new Response(Util.capitaliseSentence(item.getItemType().getUseName())+" all (self)",
											Util.capitaliseSentence(item.getItemType().getUseName()) + " all of the " + item.getName() + " that are currently in your inventory.", INVENTORY_MENU){
										@Override
										public void effects(){
											int itemCount = Main.game.getPlayer().getItemCount(item);
											for(int i=0;i<itemCount;i++) {
												Main.game.getTextStartStringBuilder().append("<p style='text-align:center;'>" + Main.game.getPlayer().useItem(item, Main.game.getPlayer(), false) + "</p>");
											}
										}
									};
								}
								
							} else if (index == 9) {
								return getBuybackResponse();
								
							} else if (index == 10) {
								return getQuickTradeResponse();
								
							} else if(index == 11) {
								return new Response(Util.capitaliseSentence(item.getItemType().getUseName()) +" (them)", UtilText.parse(inventoryNPC, "[npc.Name] doesn't want to use your items."), null);
								
							} else if(index == 12) {
								return new Response(Util.capitaliseSentence(item.getItemType().getUseName()) +" all (them)", UtilText.parse(inventoryNPC, "[npc.Name] doesn't want to use your items."), null);
								
							} else {
								return null;
							}
					}
				}
				
			// ****************************** ITEM DOES NOT BELONG TO PLAYER ****************************** TODO
				
			} else {
				// ****************************** Interacting with the ground ******************************
				if(inventoryNPC == null) {
					boolean inventoryFull = Main.game.getPlayer().isInventoryFull() && !Main.game.getPlayer().hasItem(item);
					
					if(index == 1) {
						if(inventoryFull) {
							return new Response("Take (1)", "Your inventory is already full!", null);
						}
						return new Response("Take (1)", "Take one " + item.getItemType().getDeterminer() + " " + item.getName() + " from the ground.", INVENTORY_MENU){
							@Override
							public void effects(){
								pickUpItems(Main.game.getPlayer(), item, 1);
							}
						};
						
					} else if(index == 2) {
						if(inventoryFull) {
							return new Response("Take (5)", "Your inventory is already full!", null);
						}
						if(Main.game.getCurrentCell().getInventory().getItemCount(item) >= 5) {
							return new Response("Take (5)", "Take five of the " + item.getNamePlural() + " from the ground.", INVENTORY_MENU){
								@Override
								public void effects(){
									pickUpItems(Main.game.getPlayer(), item, 5);
								}
							};
						} else {
							return new Response("Take (5)", "There aren't five " + item.getNamePlural() + " on the ground!", null);
						}
						
					} else if(index == 3) {
						if(inventoryFull) {
							return new Response("Take (All)", "Your inventory is already full!", null);
						}
						return new Response("Take (All)", "Take all of the " + item.getNamePlural() + " from the ground.", INVENTORY_MENU){
							@Override
							public void effects(){
								pickUpItems(Main.game.getPlayer(), item, Main.game.getCurrentCell().getInventory().getItemCount(item));
							}
						};
						
					} else if(index == 5) {
						return new Response("Enchant", "You can't enchant items on the ground!", null);
						
					} else if(index == 6) {
						if (!item.isAbleToBeUsedFromInventory()) {
							return new Response(Util.capitaliseSentence(item.getItemType().getUseName())+" (self)", item.getUnableToBeUsedFromInventoryDescription(), null);
							
						} else if (!item.isAbleToBeUsed(Main.game.getPlayer())) {
							return new Response(Util.capitaliseSentence(item.getItemType().getUseName()) +" (self)", item.getUnableToBeUsedDescription(Main.game.getPlayer()), null);
							
						} else {
							return new Response(Util.capitaliseSentence(item.getItemType().getUseName()) +" (self)",
									Util.capitaliseSentence(item.getItemType().getUseName()) + " the " + item.getName() + ".", INVENTORY_MENU){
								@Override
								public void effects(){
									Main.game.getTextStartStringBuilder().append("<p style='text-align:center;'>" + Main.game.getPlayer().useItem(item, Main.game.getPlayer(), true) + "</p>");
								}
							};
						}
						
					} else if(index == 7) {
						if (!item.isAbleToBeUsedFromInventory()) {
							return new Response(Util.capitaliseSentence(item.getItemType().getUseName())+" all (self)", item.getUnableToBeUsedFromInventoryDescription(), null);
							
						} else if(!item.isAbleToBeUsed(Main.game.getPlayer())) {
							return new Response(Util.capitaliseSentence(item.getItemType().getUseName())+" all (self)", item.getUnableToBeUsedDescription(Main.game.getPlayer()), null);
							
						} else {
							return new Response(Util.capitaliseSentence(item.getItemType().getUseName())+" all (self)",
									Util.capitaliseSentence(item.getItemType().getUseName()) + " all of the " + item.getName() + " that are currently in this area.", INVENTORY_MENU){
								@Override
								public void effects(){
									int itemCount = Main.game.getPlayerCell().getInventory().getItemCount(item);
									for(int i=0;i<itemCount;i++) {
										Main.game.getTextStartStringBuilder().append("<p style='text-align:center;'>" + Main.game.getPlayer().useItem(item, Main.game.getPlayer(), true) + "</p>");
									}
								}
							};
						}
						
					} else if (index == 10) {
						return getQuickTradeResponse();
						
					} else if(index == 11) {
						return new Response(Util.capitaliseSentence(item.getItemType().getUseName()) +" (them)", "There's nobody to use " + item.getItemType().getDeterminer() + " "+item.getName()+" on!", null);
						
					} else if(index == 11) {
						return new Response(Util.capitaliseSentence(item.getItemType().getUseName()) +" all (them)", "There's nobody to use " + item.getItemType().getDeterminer() + " "+item.getName()+" on!", null);
						
					} else {
						return null;
					}
					
				// ****************************** Interacting with an NPC ******************************
				} else {
					boolean inventoryFull = false;
					switch(interactionType) {
						case COMBAT:
							if(index == 1) {
								return new Response("Take (1)", "You can't take someone items while fighting them!", null);
								
							} else if(index == 2) {
								return new Response("Take (5)", "You can't take someone items while fighting them!", null);
								
							} else if(index == 3) {
								return new Response("Take (All)", "You can't take someone items while fighting them!", null);
								
							} else if(index == 5) {
								return new Response("Enchant", "You can't enchant someone else's items, especially not while fighting them!", null);
								
							} else if(index == 6) {
								return new Response(Util.capitaliseSentence(item.getItemType().getUseName()) +" (self)", "You can't use someone else's items while fighting them!", null);
								
							} else if(index == 7) {
								return new Response(Util.capitaliseSentence(item.getItemType().getUseName()) +" all (self)", "You can't use someone else's items while fighting them!", null);
								
							} else if (index == 10) {
								return getQuickTradeResponse();
								
							} else if(index == 11) {
								return new Response(Util.capitaliseSentence(item.getItemType().getUseName()) +" (opponent)", "You can't use make someone use an item while fighting them!", null);
								
							} else if(index == 12) {
								return new Response(Util.capitaliseSentence(item.getItemType().getUseName()) +" all (opponent)", "You can't use make someone use an item while fighting them!", null);
								
							} else {
								return null;
							}
							
						case FULL_MANAGEMENT:  case CHARACTER_CREATION:
							inventoryFull = Main.game.getPlayer().isInventoryFull() && !Main.game.getPlayer().hasItem(item);
						
							if(index == 1) {
								if(inventoryFull) {
									return new Response("Take (1)", "Your inventory is already full!", null);
								}
								return new Response("Take (1)", UtilText.parse(inventoryNPC, "Take " + item.getItemType().getDeterminer() + " " + item.getName() + " from [npc.name]."), INVENTORY_MENU){
									@Override
									public void effects(){
										transferItems(inventoryNPC, Main.game.getPlayer(), item, 1);
									}
								};
								
							} else if(index == 2) {
								if(inventoryFull) {
									return new Response("Take (5)", "Your inventory is already full!", null);
								}
								if(inventoryNPC.getItemCount(item) >= 5) {
									return new Response("Take (5)", UtilText.parse(inventoryNPC, "Take five of  [npc.name]'s " + item.getNamePlural() + "."), INVENTORY_MENU){
										@Override
										public void effects(){
											transferItems(inventoryNPC, Main.game.getPlayer(), item, 5);
										}
									};
								} else {
									return new Response("Take (5)", UtilText.parse(inventoryNPC, "[npc.Name] doesn't have five " + item.getNamePlural() + "!"), null);
								}
								
							} else if(index == 3) {
								if(inventoryFull) {
									return new Response("Take (All)", "Your inventory is already full!", null);
								}
								return new Response("Take (All)", UtilText.parse(inventoryNPC, "Take five of  [npc.name]'s " + item.getNamePlural() + "."), INVENTORY_MENU){
									@Override
									public void effects(){
										transferItems(inventoryNPC, Main.game.getPlayer(), item, inventoryNPC.getItemCount(item));
									}
								};
								
							} else if(index == 5) {
								return new Response("Enchant", "You can't enchant items owned by someone else!", null);
								
							} else if(index == 6) {
								if (!item.isAbleToBeUsedFromInventory()) {
									return new Response(Util.capitaliseSentence(item.getItemType().getUseName())+" (self)", item.getUnableToBeUsedFromInventoryDescription(), null);
									
								} else if (!item.isAbleToBeUsed(Main.game.getPlayer())) {
									return new Response(Util.capitaliseSentence(item.getItemType().getUseName()) +" (self)", item.getUnableToBeUsedDescription(Main.game.getPlayer()), null);
									
								} else {
									return new Response(Util.capitaliseSentence(item.getItemType().getUseName()) +" (self)",
											Util.capitaliseSentence(item.getItemType().getUseName()) + " the " + item.getName() + ".", INVENTORY_MENU){
										@Override
										public void effects(){
											Main.game.getTextStartStringBuilder().append("<p style='text-align:center;'>" + inventoryNPC.useItem(item, Main.game.getPlayer(), false) + "</p>");
										}
									};
								}
								
							} else if(index == 7) {
								if (!item.isAbleToBeUsedFromInventory()) {
									return new Response(Util.capitaliseSentence(item.getItemType().getUseName())+" all (self)", item.getUnableToBeUsedFromInventoryDescription(), null);
									
								} else if(!item.isAbleToBeUsed(Main.game.getPlayer())) {
									return new Response(Util.capitaliseSentence(item.getItemType().getUseName())+" all (self)", item.getUnableToBeUsedDescription(Main.game.getPlayer()), null);
									
								} else {
									return new Response(Util.capitaliseSentence(item.getItemType().getUseName())+" all (self)",
											UtilText.parse(inventoryNPC, Util.capitaliseSentence(item.getItemType().getUseName()) + " all of the " + item.getName() + " that are currently in [npc.name]'s inventory."), INVENTORY_MENU){
										@Override
										public void effects(){
											int itemCount = inventoryNPC.getItemCount(item);
											for(int i=0;i<itemCount;i++) {
												Main.game.getTextStartStringBuilder().append("<p style='text-align:center;'>" + inventoryNPC.useItem(item, Main.game.getPlayer(), false) + "</p>");
											}
										}
									};
								}
								
							} else if (index == 10) {
								return getQuickTradeResponse();
								
							} else if(index == 11) {
								if (!item.isAbleToBeUsedFromInventory()) {
									return new Response(Util.capitaliseSentence(item.getItemType().getUseName())+" (them)", item.getUnableToBeUsedFromInventoryDescription(), null);
									
								} else if (!item.isAbleToBeUsed(inventoryNPC)) {
									return new Response(Util.capitaliseSentence(item.getItemType().getUseName()) +" (them)", item.getUnableToBeUsedDescription(inventoryNPC), null);
									
								} else {
									return new Response(Util.capitaliseSentence(item.getItemType().getUseName()) +" (them)",
											"Get "+inventoryNPC.getName("the")+" to "+ item.getItemType().getUseName() + " the " + item.getName() + ".", INVENTORY_MENU){
										@Override
										public void effects(){
											Main.game.getTextStartStringBuilder().append("<p style='text-align:center;'>" + inventoryNPC.useItem(item, inventoryNPC, false) + "</p>");
										}
									};
								}
								
							} else if(index == 12) {
								if (!item.isAbleToBeUsedFromInventory()) {
									return new Response(Util.capitaliseSentence(item.getItemType().getUseName())+" all (them)", item.getUnableToBeUsedFromInventoryDescription(), null);
									
								} else if(!item.isAbleToBeUsed(inventoryNPC)) {
									return new Response(Util.capitaliseSentence(item.getItemType().getUseName())+" all (them)", item.getUnableToBeUsedDescription(inventoryNPC), null);
									
								} else {
									return new Response(Util.capitaliseSentence(item.getItemType().getUseName())+" all (them)",
											UtilText.parse(inventoryNPC, Util.capitaliseSentence("Get [npc.name] to "+item.getItemType().getUseName()) + " all of the " + item.getName() + " that are currently in [npc.her] inventory."),
											INVENTORY_MENU){
										@Override
										public void effects(){
											int itemCount = inventoryNPC.getItemCount(item);
											for(int i=0;i<itemCount;i++) {
												Main.game.getTextStartStringBuilder().append("<p style='text-align:center;'>" + inventoryNPC.useItem(item, inventoryNPC, false) + "</p>");
											}
										}
									};
								}
								
							} else {
								return null;
							}
							
						case SEX:
							if(index == 1) {
								return new Response("Take (1)", "You can't take someone's items while having sex with them!", null);
								
							} else if(index == 2) {
								return new Response("Take (5)", "You can't take someone's items while having sex with them!", null);
								
							} else if(index == 3) {
								return new Response("Take (All)", "You can't take someone's items while having sex with them!", null);
								
							} else if(index == 5) {
								return new Response("Enchant", "You can't enchant someone else's items, especially not while having sex with them!", null);
								
							} else if(index == 6) {
								if (!item.isAbleToBeUsedInSex()) {
									return new Response(Util.capitaliseSentence(item.getItemType().getUseName())+" (self)", "This cannot be used during sex!", null);
									
								} else if (!item.isAbleToBeUsedFromInventory()) {
									return new Response(Util.capitaliseSentence(item.getItemType().getUseName())+" (self)", item.getUnableToBeUsedFromInventoryDescription(), null);
									
								} else if (!item.isAbleToBeUsed(Main.game.getPlayer())) {
									return new Response(Util.capitaliseSentence(item.getItemType().getUseName()) +" (self)", item.getUnableToBeUsedDescription(Main.game.getPlayer()), null);
									
								} else {
									return new Response(Util.capitaliseSentence(item.getItemType().getUseName()) +" (self)",
											Util.capitaliseSentence(item.getItemType().getUseName()) + " the " + item.getName() + ".", Sex.SEX_DIALOGUE){
										@Override
										public void effects(){
											Sex.setUsingItemText(Sex.getPartner().getItemUseEffects(item, inventoryNPC, Main.game.getPlayer()));
											Main.mainController.openInventory();
											Sex.endSexTurn(SexActionUtility.PLAYER_USE_ITEM);
										}
									};
								}
								
							} else if(index == 7) {
								return new Response(Util.capitaliseSentence(item.getItemType().getUseName())+" all (self)", "You can only use one item at a time during sex!", null);
								
							} else if (index == 10) {
								return getQuickTradeResponse();
								
							} else if(index == 11) {
								if (!item.isAbleToBeUsedInSex()) {
									return new Response(Util.capitaliseSentence(item.getItemType().getUseName())+" (partner)", "This cannot be used during sex!", null);
									
								} else if (!item.isAbleToBeUsedFromInventory()) {
									return new Response(Util.capitaliseSentence(item.getItemType().getUseName())+" (partner)", item.getUnableToBeUsedFromInventoryDescription(), null);
									
								} else if (!item.isAbleToBeUsed(inventoryNPC)) {
									return new Response(Util.capitaliseSentence(item.getItemType().getUseName()) +" (partner)", item.getUnableToBeUsedDescription(inventoryNPC), null);
								} else {
									return new Response(Util.capitaliseSentence(item.getItemType().getUseName()) +" (partner)",
											"Get "+inventoryNPC.getName("the")+" to "+ item.getItemType().getUseName() + " the " + item.getName() + ".", Sex.SEX_DIALOGUE){
										@Override
										public void effects(){
											Sex.setUsingItemText(Sex.getPartner().getItemUseEffects(item, inventoryNPC, inventoryNPC));
											Main.mainController.openInventory();
											Sex.endSexTurn(SexActionUtility.PLAYER_USE_ITEM);
										}
									};
								}
								
							} else if(index == 12) {
								return new Response(Util.capitaliseSentence(item.getItemType().getUseName())+" all (partner)", "You can only use one item at a time during sex!", null);
								
							} else {
								return null;
							}
							
						case TRADING:
							inventoryFull = Main.game.getPlayer().isInventoryFull() && !Main.game.getPlayer().hasItem(item);
							
							if(index == 1) {
								int sellPrice = buyback?Main.game.getPlayer().getBuybackStack().get(buyBackIndex).getPrice():item.getPrice(inventoryNPC.getSellModifier());
								if(inventoryFull) {
									return new Response("Buy (1) ("+UtilText.formatAsMoneyUncoloured(sellPrice, "span")+")", "Your inventory is already full!", null);
								}
								if(Main.game.getPlayer().getMoney() < sellPrice) {
									return new Response("Buy (1) ("+UtilText.formatAsMoneyUncoloured(sellPrice, "span")+")", "You can't afford to buy this!", null);
								}
								return new Response("Buy (1) (" + UtilText.formatAsMoney(sellPrice, "span") + ")", "Buy the " + item.getName() + " for " + UtilText.formatAsMoney(sellPrice) + ".", INVENTORY_MENU){
									@Override
									public void effects(){
										sellItems(inventoryNPC, Main.game.getPlayer(), item, 1, sellPrice);
									}
								};
								
							} else if(index == 2) {
								int sellPrice = buyback?Main.game.getPlayer().getBuybackStack().get(buyBackIndex).getPrice():item.getPrice(inventoryNPC.getSellModifier());
								if(buyback) {
									return new Response("Buy (5) ("+UtilText.formatAsMoneyUncoloured(sellPrice*5, "span")+")", "Cannot use this option in the buyback menu.", null);
								}
								if(inventoryNPC.getItemCount(item) < 5) {
									return new Response("Buy (5) ("+UtilText.formatAsMoneyUncoloured(sellPrice*5, "span")+")", UtilText.parse(inventoryNPC, "[npc.Name] doesn't have five "+item.getNamePlural()+"."), null);
								}
								if(inventoryFull) {
									return new Response("Buy (5) ("+UtilText.formatAsMoneyUncoloured(sellPrice*5, "span")+")", "Your inventory is already full!", null);
								}
								if(Main.game.getPlayer().getMoney() < sellPrice*5) {
									return new Response("Buy (5) ("+UtilText.formatAsMoneyUncoloured(sellPrice*5, "span")+")", "You can't afford to buy this!", null);
								}
								return new Response("Buy (5) (" + UtilText.formatAsMoney(sellPrice*5, "span") + ")", "Buy the " + item.getName() + " for " + UtilText.formatAsMoney(sellPrice*5) + ".", INVENTORY_MENU){
									@Override
									public void effects(){
										sellItems(inventoryNPC, Main.game.getPlayer(), item, 5, sellPrice);
									}
								};
								
							} else if(index == 3) {
								int sellPrice = buyback?Main.game.getPlayer().getBuybackStack().get(buyBackIndex).getPrice():item.getPrice(inventoryNPC.getSellModifier());
								if(buyback) {
									return new Response("Buy (All) ("+UtilText.formatAsMoneyUncoloured(sellPrice*inventoryNPC.getItemCount(item), "span")+")", "Cannot use this option in the buyback menu.", null);
								}
								if(inventoryFull) {
									return new Response("Buy (All) ("+UtilText.formatAsMoneyUncoloured(sellPrice*inventoryNPC.getItemCount(item), "span")+")", "Your inventory is already full!", null);
								}
								if(Main.game.getPlayer().getMoney() < sellPrice*inventoryNPC.getItemCount(item)) {
									return new Response("Buy (All) ("+UtilText.formatAsMoneyUncoloured(sellPrice*inventoryNPC.getItemCount(item), "span")+")", "You can't afford to buy this!", null);
								}
								return new Response("Buy (All) (" + UtilText.formatAsMoney(sellPrice*inventoryNPC.getItemCount(item), "span") + ")",
										"Buy the " + item.getName() + " for " + UtilText.formatAsMoney(sellPrice*inventoryNPC.getItemCount(item)) + ".", INVENTORY_MENU){
									@Override
									public void effects(){
										sellItems(inventoryNPC, Main.game.getPlayer(), item, inventoryNPC.getItemCount(item), sellPrice);
									}
								};
								
							} else if(index == 5) {
								return new Response("Enchant", "You can't enchant someone else's item!", null);
								
							} else if(index == 6) {
								return new Response(Util.capitaliseSentence(item.getItemType().getUseName()) +" (self)", UtilText.parse(inventoryNPC, "[npc.Name] isn't going to let you use [npc.her] items without buying them first."), null);
								
							} else if(index == 7) {
								return new Response(Util.capitaliseSentence(item.getItemType().getUseName()) +" all (self)", UtilText.parse(inventoryNPC, "[npc.Name] isn't going to let you use [npc.her] items without buying them first."), null);
								
							} else if (index == 9) {
								return getBuybackResponse();
								
							} else if (index == 10) {
								return getQuickTradeResponse();
								
							} else if(index == 11) {
								return new Response(Util.capitaliseSentence(item.getItemType().getUseName()) +" (them)", UtilText.parse(inventoryNPC, "[npc.Name] isn't going to use the items that [npc.she]'s trying to sell!"), null);
								
							} else if(index == 12) {
								return new Response(Util.capitaliseSentence(item.getItemType().getUseName()) +" all (them)", UtilText.parse(inventoryNPC, "[npc.Name] isn't going to use the items that [npc.she]'s trying to sell!"), null);
								
							} else {
								return null;
							}
					}
				}
			}
			return null;
		}

		@Override
		public MapDisplay getMapDisplay() {
			return MapDisplay.INVENTORY;
		}
	};
	
	public static final DialogueNodeOld WEAPON_INVENTORY = new DialogueNodeOld("Weapon", "", true) {
		
		private static final long serialVersionUID = 1L;

		@Override
		public String getLabel() {
			if (Main.game.getDialogueFlags().quickTrade && !Main.game.isInSex() && !Main.game.isInCombat()) {
				return "Inventory (Quick-Manage is <b style='color:" + Colour.GENERIC_GOOD.toWebHexString() + ";'>ON</b>)";
			} else {
				return "Inventory";
			}
		}
		
		@Override
		public String getHeaderContent() {
			return inventoryView();
		}
		
		@Override
		public String getContent() {
			return getItemDisplayPanel(weapon.getSVGString(),
					weapon.getDisplayName(true),
					weapon.getDescription()
					+ (owner!=null && owner.isPlayer()
							? (inventoryNPC != null && interactionType == InventoryInteraction.TRADING
									? inventoryNPC.willBuy(weapon)
											? "<p>"
												+ inventoryNPC.getName("The") + " will buy it for " + UtilText.formatAsMoney(weapon.getPrice(inventoryNPC.getBuyModifier())) + "."
											+ "</p>" 
											: inventoryNPC.getName("The") + " doesn't want to buy this."
										: "")
							:(inventoryNPC != null && interactionType == InventoryInteraction.TRADING
								? "<p>"
										+ inventoryNPC.getName("The") + " will sell it for " + UtilText.formatAsMoney(weapon.getPrice(inventoryNPC.getSellModifier())) + "."
									+ "</p>" 
								: "")));
		}
		
		@Override
		public Response getResponse(int index) {
			if (index == 0) {
				return getReturnToInventoryMenuResponse();
			}
			
			// ****************************** ITEM BELONGS TO THE PLAYER ******************************
			if(owner != null && owner.isPlayer()) {
				
				// ****************************** Interacting with the ground ******************************
				if(inventoryNPC == null) {
					boolean areaFull = Main.game.isPlayerTileFull() && !Main.game.getCurrentCell().getInventory().hasWeapon(weapon);
					
					if(index == 1) {
						if(owner.getLocationPlace().isItemsDisappear()) {
							if(areaFull) {
								return new Response("Drop (1)", "This area is full, so you can't drop your " + weapon.getName() + " here!", null);
							} else {
								return new Response("Drop (1)", "Drop your " + weapon.getName() + ".", INVENTORY_MENU){
									@Override
									public void effects(){
										dropWeapons(owner, weapon, 1);
									}
								};
							}
						} else {
							if(areaFull) {
								return new Response("Store (1)", "This area is full, so you can't store your " + weapon.getName() + " here!", null);
							} else {
								return new Response("Store (1)", "Store the " + weapon.getName() + " in this area.", INVENTORY_MENU){
									@Override
									public void effects(){
										dropWeapons(owner, weapon, 1);
									}
								};
							}
						}
						
					} else if(index == 2) {
						if(owner.getLocationPlace().isItemsDisappear()) {
							if(owner.getWeaponCount(weapon) < 5) {
								return new Response("Drop (5)", "You don't have five " + weapon.getNamePlural() + " to give!", null);
								
							} else if(areaFull) {
								return new Response("Drop (5)", "This area is full, so you can't drop your " + weapon.getNamePlural() + " here!", null);
								
							} else {
								return new Response("Drop (5)", "Drop five of your " + weapon.getNamePlural() + ".", INVENTORY_MENU){
									@Override
									public void effects(){
										dropWeapons(owner, weapon, 5);
									}
								};
							}
						} else {
							if(owner.getWeaponCount(weapon) < 5) {
								return new Response("Store (5)", "You don't have five " + weapon.getNamePlural() + " to give!", null);
								
							} else if(areaFull) {
								return new Response("Store (5)", "This area is full, so you can't store your " + weapon.getNamePlural() + " here!", null);
								
							} else {
								return new Response("Store (5)", "Store five of your " + weapon.getNamePlural() + " in this area.", INVENTORY_MENU){
									@Override
									public void effects(){
										dropWeapons(owner, weapon, 5);
									}
								};
							}
						}
						
					} else if(index == 3) {
						if(owner.getLocationPlace().isItemsDisappear()) {
							if(areaFull) {
								return new Response("Drop (All)", "This area is full, so you can't drop your " + weapon.getNamePlural() + " here!", null);
							} else {
								return new Response("Drop (All)", "Drop all of your " + weapon.getNamePlural() + ".", INVENTORY_MENU){
									@Override
									public void effects(){
										dropWeapons(owner, weapon, owner.getWeaponCount(weapon));
									}
								};
							}
						} else {
							if(areaFull) {
								return new Response("Store (All)", "This area is full, so you can't store your " + weapon.getNamePlural() + " here!", null);
							} else {
								return new Response("Store (All)", "Store all of your " + weapon.getNamePlural() + " in this area.", INVENTORY_MENU){
									@Override
									public void effects(){
										dropWeapons(owner, weapon, owner.getWeaponCount(weapon));
									}
								};
							}
						}
						
					} else if(index == 5) {
						if(weapon.getEnchantmentItemType()==null) {
							return new Response("Enchant", "This weapon cannot be enchanted!", null);
							
						} else if(Main.game.isDebugMode()) {
							return new Response("Enchant", "Enchant this weapon.", EnchantmentDialogue.ENCHANTMENT_MENU) {
								@Override
								public void effects() {
									EnchantmentDialogue.effects.clear();
									EnchantmentDialogue.resetEnchantmentVariables();
									EnchantmentDialogue.ingredient = weapon;
								}
							};
							
						} else if(Main.game.getPlayer().hasQuest(QuestLine.SIDE_ENCHANTMENT_DISCOVERY)) {
							if(Main.game.getPlayer().isQuestProgressGreaterThan(QuestLine.SIDE_ENCHANTMENT_DISCOVERY, Quest.SIDE_ENCHANTMENTS_LILAYA_HELP)) {
								return new Response("Enchant", "Enchant this weapon.", EnchantmentDialogue.ENCHANTMENT_MENU) {
									@Override
									public void effects() {
										EnchantmentDialogue.effects.clear();
										EnchantmentDialogue.resetEnchantmentVariables();
										EnchantmentDialogue.ingredient = weapon;
									}
								};
							}
						}
						
						return null;
						
					} else if(index == 6) {
							return new Response("Equip (self)", "Equip the " + weapon.getName() + ".", INVENTORY_MENU){
								@Override
								public void effects(){
									Main.game.getTextStartStringBuilder().append("<p style='text-align:center;'>"
										+ (weapon.getWeaponType().getSlot()==InventorySlot.WEAPON_MAIN
											?Main.game.getPlayer().equipMainWeaponFromInventory(weapon, Main.game.getPlayer())
											:Main.game.getPlayer().equipOffhandWeaponFromInventory(weapon, Main.game.getPlayer()))
										+ "</p>");
								}
							};
							
					} else if (index==7) {
						if(Main.game.getPlayer().hasQuest(QuestLine.SIDE_JINXED_CLOTHING) && Main.game.getPlayer().isQuestProgressGreaterThan(QuestLine.SIDE_JINXED_CLOTHING, Quest.SIDE_JINXED_LILAYA_HELP)){
							return new Response("Remove jinx", "Proceed to the jinxed clothing choice menu.", REMOVE_JINX){
								@Override
								public void effects() {
									jinxRemovalFromFloor = false;
								}
							};
						}
					
						return null;
						
					} else if (index == 10) {
						return getQuickTradeResponse();
						
					} else if(index == 11) {
						return new Response("Equip (Ground)", "You can't make the ground equip your "+weapon.getName()+"!", null);
						
					} else {
						return null;
					}
					
					
				// ****************************** Interacting with an NPC ******************************
				} else {
					switch(interactionType) {
						case COMBAT:
							if(index == 1) {
								return new Response("Give (1)", "You can't give someone weapons while fighting them!", null);
								
							} else if(index == 2) {
								return new Response("Give (5)", "You can't give someone weapons while fighting them!", null);
								
							} else if(index == 3) {
								return new Response("Give (All)", "You can't give someone weapons while fighting them!", null);
								
							} else if(index == 5) {
								return new Response("Enchant", "You can't enchant weapons while fighting someone!", null);
								
							} else if(index == 6) {
								return new Response("Equip (self)", "Equip the " + weapon.getName() + ".", Combat.ENEMY_ATTACK){
									@Override
									public void effects(){
										Combat.setPlayerTurnText("<p style='text-align:center;'>"
																	+ (weapon.getWeaponType().getSlot()==InventorySlot.WEAPON_MAIN
																	?Main.game.getPlayer().equipMainWeaponFromInventory(weapon, Main.game.getPlayer())
																	:Main.game.getPlayer().equipOffhandWeaponFromInventory(weapon, Main.game.getPlayer()))
																+ "</p>");
										Combat.attackEnemy();
										Combat.setPreviousAction(Attack.NONE);
										Main.mainController.openInventory();
									}
								};
									
							} else if(index == 7) {
								if(Main.game.getPlayer().hasQuest(QuestLine.SIDE_JINXED_CLOTHING) && Main.game.getPlayer().isQuestProgressGreaterThan(QuestLine.SIDE_JINXED_CLOTHING, Quest.SIDE_JINXED_LILAYA_HELP)){
									return new Response("Remove jinx", "You can't remove a jinx while fighting someone!", null);
								}
								return null;
								
							} else if (index == 10) {
								return getQuickTradeResponse();
								
							} else if(index == 11) {
								return new Response("Equip (opponent)", "You can't make your opponent equip a weapon!", null);
								
							} else {
								return null;
							}
							
						case FULL_MANAGEMENT:  case CHARACTER_CREATION:
							boolean inventoryFull = inventoryNPC.isInventoryFull() && !inventoryNPC.hasWeapon(weapon);
							
							if(index == 1) {
								if(inventoryFull) {
									return new Response("Give (1)", UtilText.parse(inventoryNPC, "[npc.Name]'s inventory is already full!"), null);
								}
								return new Response("Give (1)", UtilText.parse(inventoryNPC, "Give [npc.name] " + weapon.getWeaponType().getDeterminer() + " " + weapon.getName() + "."), INVENTORY_MENU){
									@Override
									public void effects(){
										transferWeapons(Main.game.getPlayer(), inventoryNPC, weapon, 1);
									}
								};
								
							} else if(index == 2) {
								if(inventoryFull) {
									return new Response("Give (5)", UtilText.parse(inventoryNPC, "[npc.Name]'s inventory is already full!"), null);
								}
								if(Main.game.getPlayer().getWeaponCount(weapon) >= 5) {
									return new Response("Give (5)", UtilText.parse(inventoryNPC, "Give [npc.name] five of your " + weapon.getNamePlural() + "."), INVENTORY_MENU){
										@Override
										public void effects(){
											transferWeapons(Main.game.getPlayer(), inventoryNPC, weapon, 5);
										}
									};
								} else {
									return new Response("Give (5)", "You don't have five " + weapon.getNamePlural() + " to give!", null);
								}
								
							} else if(index == 3) {
								if(inventoryFull) {
									return new Response("Give (All)", UtilText.parse(inventoryNPC, "[npc.Name]'s inventory is already full!"), null);
								}
								return new Response("Give (All)", UtilText.parse(inventoryNPC, "Give [npc.name] all of your " + weapon.getNamePlural() + "."), INVENTORY_MENU){
									@Override
									public void effects(){
										transferWeapons(Main.game.getPlayer(), inventoryNPC, weapon, Main.game.getPlayer().getWeaponCount(weapon));
									}
								};
								
							} else if(index == 5) {
								if(weapon.getEnchantmentItemType()==null) {
									return new Response("Enchant", "This weapon cannot be enchanted!", null);
									
								} else if(Main.game.isDebugMode()) {
									return new Response("Enchant", "Enchant this weapon.", EnchantmentDialogue.ENCHANTMENT_MENU) {
										@Override
										public void effects() {
											EnchantmentDialogue.effects.clear();
											EnchantmentDialogue.resetEnchantmentVariables();
											EnchantmentDialogue.ingredient = weapon;
										}
									};
									
								} else if(Main.game.getPlayer().hasQuest(QuestLine.SIDE_ENCHANTMENT_DISCOVERY)) {
									if(Main.game.getPlayer().isQuestProgressGreaterThan(QuestLine.SIDE_ENCHANTMENT_DISCOVERY, Quest.SIDE_ENCHANTMENTS_LILAYA_HELP)) {
										return new Response("Enchant", "Enchant this weapon.", EnchantmentDialogue.ENCHANTMENT_MENU) {
											@Override
											public void effects() {
												EnchantmentDialogue.effects.clear();
												EnchantmentDialogue.resetEnchantmentVariables();
												EnchantmentDialogue.ingredient = weapon;
											}
										};
									}
								}
								
								return null;
								
							}  else if(index == 6) {
								return new Response("Equip (self)", "Equip the " + weapon.getName() + ".", INVENTORY_MENU){
										@Override
										public void effects(){
											Main.game.getTextStartStringBuilder().append("<p style='text-align:center;'>"
												+ (weapon.getWeaponType().getSlot()==InventorySlot.WEAPON_MAIN
														?Main.game.getPlayer().equipMainWeaponFromInventory(weapon, Main.game.getPlayer())
														:Main.game.getPlayer().equipOffhandWeaponFromInventory(weapon, Main.game.getPlayer()))
												+ "</p>");
										}
									};
									
							} else if (index==7) {
								if(Main.game.getPlayer().hasQuest(QuestLine.SIDE_JINXED_CLOTHING) && Main.game.getPlayer().isQuestProgressGreaterThan(QuestLine.SIDE_JINXED_CLOTHING, Quest.SIDE_JINXED_LILAYA_HELP)){
									return new Response("Remove jinx", "Proceed to the jinxed clothing choice menu.", REMOVE_JINX){
										@Override
										public void effects() {
											jinxRemovalFromFloor = false;
										}
									};
								}
							
								return null;
								
							} else if (index == 10) {
								return getQuickTradeResponse();
								
							} else if(index == 11) {
								return new Response(UtilText.parse(inventoryNPC, "Equip ([npc.Name])"), UtilText.parse(inventoryNPC, "Make [npc.name] equip the "+weapon.getName()+"!"), INVENTORY_MENU){
									@Override
									public void effects(){
										Main.game.getTextStartStringBuilder().append("<p style='text-align:center;'>"
											+ (weapon.getWeaponType().getSlot()==InventorySlot.WEAPON_MAIN
												?inventoryNPC.equipMainWeaponFromInventory(weapon, Main.game.getPlayer())
												:inventoryNPC.equipOffhandWeaponFromInventory(weapon, Main.game.getPlayer()))
											+ "</p>");
									}
								};
							
							} else {
								return null;
							}
							
						case SEX:
							if(index == 1) {
								return new Response("Give (1)", "You can't give someone weapons while having sex with them!", null);
								
							} else if(index == 2) {
								return new Response("Give (5)", "You can't give someone weapons while having sex with them!", null);
								
							} else if(index == 3) {
								return new Response("Give (All)", "You can't give someone weapons while having sex with them!", null);
								
							} else if(index == 5) {
								return new Response("Enchant", "You can't enchant weapons while having sex with someone!", null);
								
							} else if(index == 6) {
								return new Response("Equip (self)", "You can't equip weapons while having sex with someone!", null);
								
							} else if(index == 7) {
								if(Main.game.getPlayer().hasQuest(QuestLine.SIDE_JINXED_CLOTHING) && Main.game.getPlayer().isQuestProgressGreaterThan(QuestLine.SIDE_JINXED_CLOTHING, Quest.SIDE_JINXED_LILAYA_HELP)){
									return new Response("Remove jinx", "You can't remove a jinx while having sex with someone!", null);
								}
								return null;
								
							} else if (index == 10) {
								return getQuickTradeResponse();
								
							} else if(index == 11) {
								return new Response(UtilText.parse(inventoryNPC, "Equip ([npc.Name])"), "You can't equip weapons while having sex with someone!", null);
								
							} else {
								return null;
							}
							
						case TRADING:
							if(index == 1) {
								if (inventoryNPC.willBuy(weapon)) {
									int sellPrice = weapon.getPrice(inventoryNPC.getBuyModifier());
									return new Response("Sell (1) (" + UtilText.formatAsMoney(sellPrice, "span") + ")", "Sell the " + weapon.getName() + " for " + UtilText.formatAsMoney(sellPrice) + ".", INVENTORY_MENU){
										@Override
										public void effects(){
											sellWeapons(Main.game.getPlayer(), inventoryNPC, weapon, 1, sellPrice);
										}
									};
								} else {
									return new Response("Sell (1)", inventoryNPC.getName("The") + " doesn't want to buy this.", null);
								}
								
							} else if(index == 2) {
								if(Main.game.getPlayer().getWeaponCount(weapon) >= 5) {
									if (inventoryNPC.willBuy(weapon)) {
										int sellPrice = weapon.getPrice(inventoryNPC.getBuyModifier());
										return new Response("Sell (1) (" + UtilText.formatAsMoney(sellPrice*5, "span") + ")", "Sell five of your " + weapon.getNamePlural() + " for " + UtilText.formatAsMoney(sellPrice*5) + ".", INVENTORY_MENU){
											@Override
											public void effects(){
												sellWeapons(Main.game.getPlayer(), inventoryNPC, weapon, 5, sellPrice);
											}
										};
									} else {
										return new Response("Sell (5)", inventoryNPC.getName("The") + " doesn't want to buy these.", null);
									}
									
								} else {
									return new Response("Sell (5)", "You don't have five " + weapon.getNamePlural() + " to sell!", null);
								}
								
							} else if(index == 3) {
								if (inventoryNPC.willBuy(weapon)) {
									int sellPrice = weapon.getPrice(inventoryNPC.getBuyModifier());
									return new Response("Sell (All) (" + UtilText.formatAsMoney(sellPrice*Main.game.getPlayer().getWeaponCount(weapon), "span") + ")",
											"Sell the " + weapon.getName() + " for " + UtilText.formatAsMoney(sellPrice*Main.game.getPlayer().getWeaponCount(weapon)) + ".", INVENTORY_MENU){
										@Override
										public void effects(){
											sellWeapons(Main.game.getPlayer(), inventoryNPC, weapon, Main.game.getPlayer().getWeaponCount(weapon), sellPrice);
										}
									};
								} else {
									return new Response("Sell (All)", inventoryNPC.getName("The") + " doesn't want to buy these.", null);
								}
								
							} else if(index == 5) {
								if(weapon.getEnchantmentItemType()==null) {
									return new Response("Enchant", "This weapon cannot be enchanted!", null);
									
								} else if(Main.game.isDebugMode()) {
									return new Response("Enchant", "Enchant this weapon.", EnchantmentDialogue.ENCHANTMENT_MENU) {
										@Override
										public void effects() {
											EnchantmentDialogue.effects.clear();
											EnchantmentDialogue.resetEnchantmentVariables();
											EnchantmentDialogue.ingredient = weapon;
										}
									};
									
								} else if(Main.game.getPlayer().hasQuest(QuestLine.SIDE_ENCHANTMENT_DISCOVERY)) {
									if(Main.game.getPlayer().isQuestProgressGreaterThan(QuestLine.SIDE_ENCHANTMENT_DISCOVERY, Quest.SIDE_ENCHANTMENTS_LILAYA_HELP)) {
										return new Response("Enchant", "Enchant this weapon.", EnchantmentDialogue.ENCHANTMENT_MENU) {
											@Override
											public void effects() {
												EnchantmentDialogue.effects.clear();
												EnchantmentDialogue.resetEnchantmentVariables();
												EnchantmentDialogue.ingredient = weapon;
											}
										};
									}
								}
								
								return null;
								
							} else if(index == 6) {
								return new Response("Equip (self)", "Equip the " + weapon.getName() + ".", INVENTORY_MENU){
										@Override
										public void effects(){
											Main.game.getTextStartStringBuilder().append("<p style='text-align:center;'>"
												+ (weapon.getWeaponType().getSlot()==InventorySlot.WEAPON_MAIN
													?Main.game.getPlayer().equipMainWeaponFromInventory(weapon, Main.game.getPlayer())
													:Main.game.getPlayer().equipOffhandWeaponFromInventory(weapon, Main.game.getPlayer()))
												+ "</p>");
										}
									};
									
							} else if (index==7) {
								if(Main.game.getPlayer().hasQuest(QuestLine.SIDE_JINXED_CLOTHING) && Main.game.getPlayer().isQuestProgressGreaterThan(QuestLine.SIDE_JINXED_CLOTHING, Quest.SIDE_JINXED_LILAYA_HELP)){
									return new Response("Remove jinx", "Proceed to the jinxed clothing choice menu.", REMOVE_JINX){
										@Override
										public void effects() {
											jinxRemovalFromFloor = false;
										}
									};
								}
							
								return null;
								
							} else if (index == 9) {
								return getBuybackResponse();
								
							} else if (index == 10) {
								return getQuickTradeResponse();
								
							} else if(index == 11) {
								return new Response(UtilText.parse(inventoryNPC, "Equip ([npc.Name])"), UtilText.parse(inventoryNPC, "[npc.Name] doesn't want to use your weapons."), null);
								
							} else {
								return null;
							}
					}
				}
				
			// ****************************** ITEM DOES NOT BELONG TO PLAYER ******************************
				
			} else {
				// ****************************** Interacting with the ground ******************************
				if(inventoryNPC == null) {
					boolean inventoryFull = Main.game.getPlayer().isInventoryFull() && !Main.game.getPlayer().hasWeapon(weapon);
					
					if(index == 1) {
						if(inventoryFull) {
							return new Response("Take (1)", "Your inventory is already full!", null);
						}
						return new Response("Take (1)", "Take one " + weapon.getWeaponType().getDeterminer() + " " + weapon.getName() + " from the ground.", INVENTORY_MENU){
							@Override
							public void effects(){
								pickUpWeapons(Main.game.getPlayer(), weapon, 1);
							}
						};
						
					} else if(index == 2) {
						if(inventoryFull) {
							return new Response("Take (5)", "Your inventory is already full!", null);
						}
						if(Main.game.getCurrentCell().getInventory().getWeaponCount(weapon) >= 5) {
							return new Response("Take (5)", "Take five of the " + weapon.getNamePlural() + " from the ground.", INVENTORY_MENU){
								@Override
								public void effects(){
									pickUpWeapons(Main.game.getPlayer(), weapon, 5);
								}
							};
						} else {
							return new Response("Take (5)", "There aren't five " + weapon.getNamePlural() + " on the ground!", null);
						}
						
					} else if(index == 3) {
						if(inventoryFull) {
							return new Response("Take (All)", "Your inventory is already full!", null);
						}
						return new Response("Take (All)", "Take all of the " + weapon.getNamePlural() + " from the ground.", INVENTORY_MENU){
							@Override
							public void effects(){
								pickUpWeapons(Main.game.getPlayer(), weapon, Main.game.getCurrentCell().getInventory().getWeaponCount(weapon));
							}
						};
						
					} else if(index == 5) {
						return new Response("Enchant", "You can't enchant weapons on the ground!", null);
						
					} else if(index == 6) {
						return new Response("Equip (self)", "Equip the " + weapon.getName() + ".", INVENTORY_MENU){
							@Override
							public void effects(){
								Main.game.getTextStartStringBuilder().append("<p style='text-align:center;'>"
									+ (weapon.getWeaponType().getSlot()==InventorySlot.WEAPON_MAIN
										?Main.game.getPlayer().equipMainWeaponFromFloor(weapon)
										:Main.game.getPlayer().equipOffhandWeaponFromFloor(weapon))
									+ "</p>");
							}
						};
							
					} else if (index==7) {
						if(Main.game.getPlayer().hasQuest(QuestLine.SIDE_JINXED_CLOTHING) && Main.game.getPlayer().isQuestProgressGreaterThan(QuestLine.SIDE_JINXED_CLOTHING, Quest.SIDE_JINXED_LILAYA_HELP)){
							return new Response("Remove jinx", "Proceed to the jinxed clothing choice menu.", REMOVE_JINX){
								@Override
								public void effects() {
									jinxRemovalFromFloor = true;
								}
							};
						}
					
						return null;
						
					} else if (index == 10) {
						return getQuickTradeResponse();
						
					} else if(index == 11) {
						return new Response("Equip (Ground)", "There's nobody to use the "+weapon.getName()+" on!", null);
						
					} else {
						return null;
					}
					
				// ****************************** Interacting with an NPC ******************************
				} else {
					boolean inventoryFull = false;
					switch(interactionType) {
						case COMBAT:
							if(index == 1) {
								return new Response("Take (1)", "You can't take someone weapons while fighting them!", null);
								
							} else if(index == 2) {
								return new Response("Take (5)", "You can't take someone weapons while fighting them!", null);
								
							} else if(index == 3) {
								return new Response("Take (All)", "You can't take someone weapons while fighting them!", null);
								
							} else if(index == 5) {
								return new Response("Enchant", "You can't enchant someone else's weapons, especially not while fighting them!", null);
								
							} else if(index == 6) {
								return new Response("Equip (self)", "You can't use someone else's weapons while fighting them!", null);
								
							} else if(index == 7) {
								if(Main.game.getPlayer().hasQuest(QuestLine.SIDE_JINXED_CLOTHING) && Main.game.getPlayer().isQuestProgressGreaterThan(QuestLine.SIDE_JINXED_CLOTHING, Quest.SIDE_JINXED_LILAYA_HELP)){
									return new Response("Remove jinx", "You can't remove a jinx while fighting someone!", null);
								}
								return null;
								
							} else if (index == 10) {
								return getQuickTradeResponse();
								
							} else if(index == 11) {
								return new Response("Equip (opponent)", "You can't use make someone use a weapon while fighting them!", null);
								
							} else {
								return null;
							}
							
						case FULL_MANAGEMENT:  case CHARACTER_CREATION:
							inventoryFull = Main.game.getPlayer().isInventoryFull() && !Main.game.getPlayer().hasWeapon(weapon);
						
							if(index == 1) {
								if(inventoryFull) {
									return new Response("Take (1)", "Your inventory is already full!", null);
								}
								return new Response("Take (1)", UtilText.parse(inventoryNPC, "Take " + weapon.getWeaponType().getDeterminer() + " " + weapon.getName() + " from [npc.name]."), INVENTORY_MENU){
									@Override
									public void effects(){
										transferWeapons(inventoryNPC, Main.game.getPlayer(), weapon, 1);
									}
								};
								
							} else if(index == 2) {
								if(inventoryFull) {
									return new Response("Take (5)", "Your inventory is already full!", null);
								}
								if(inventoryNPC.getWeaponCount(weapon) >= 5) {
									return new Response("Take (5)", UtilText.parse(inventoryNPC, "Take five of  [npc.name]'s " + weapon.getNamePlural() + "."), INVENTORY_MENU){
										@Override
										public void effects(){
											transferWeapons(inventoryNPC, Main.game.getPlayer(), weapon, 5);
										}
									};
								} else {
									return new Response("Take (5)", UtilText.parse(inventoryNPC, "[npc.Name] doesn't have five " + weapon.getNamePlural() + "!"), null);
								}
								
							} else if(index == 3) {
								if(inventoryFull) {
									return new Response("Take (All)", "Your inventory is already full!", null);
								}
								return new Response("Take (All)", UtilText.parse(inventoryNPC, "Take five of  [npc.name]'s " + weapon.getNamePlural() + "."), INVENTORY_MENU){
									@Override
									public void effects(){
										transferWeapons(inventoryNPC, Main.game.getPlayer(), weapon, inventoryNPC.getWeaponCount(weapon));
									}
								};
								
							} else if(index == 5) {
								return new Response("Enchant", "You can't enchant weapons owned by someone else!", null);
								
							} else if(index == 6) {
								return new Response("Equip (self)", "Equip the " + weapon.getName() + ".", INVENTORY_MENU){
									@Override
									public void effects(){
										Main.game.getTextStartStringBuilder().append("<p style='text-align:center;'>"
											+ (weapon.getWeaponType().getSlot()==InventorySlot.WEAPON_MAIN
												?Main.game.getPlayer().equipMainWeaponFromInventory(weapon, inventoryNPC)
												:Main.game.getPlayer().equipOffhandWeaponFromInventory(weapon, inventoryNPC))
											+ "</p>");
									}
								};
								
							} else if(index == 7) {
								if(Main.game.getPlayer().hasQuest(QuestLine.SIDE_JINXED_CLOTHING) && Main.game.getPlayer().isQuestProgressGreaterThan(QuestLine.SIDE_JINXED_CLOTHING, Quest.SIDE_JINXED_LILAYA_HELP)){
									return new Response("Remove jinx", "You can't remove a jinx while having sex with someone!", null);
								}
								return null;
								
							} else if (index == 10) {
								return getQuickTradeResponse();
								
							} else if(index == 11) {
								return new Response(UtilText.parse(inventoryNPC, "Equip ([npc.Name])"), UtilText.parse(inventoryNPC, "Get [npc.name] to equip the " + weapon.getName() + "."), INVENTORY_MENU){
									@Override
									public void effects(){
										Main.game.getTextStartStringBuilder().append("<p style='text-align:center;'>"
											+ (weapon.getWeaponType().getSlot()==InventorySlot.WEAPON_MAIN
												?inventoryNPC.equipMainWeaponFromInventory(weapon, inventoryNPC)
												:inventoryNPC.equipOffhandWeaponFromInventory(weapon, inventoryNPC))
											+ "</p>");
									}
								};
								
							} else {
								return null;
							}
							
						case SEX:
							if(index == 1) {
								return new Response("Take (1)", "You can't take someone's weapons while having sex with them!", null);
								
							} else if(index == 2) {
								return new Response("Take (5)", "You can't take someone's weapons while having sex with them!", null);
								
							} else if(index == 3) {
								return new Response("Take (All)", "You can't take someone's weapons while having sex with them!", null);
								
							} else if(index == 5) {
								return new Response("Enchant", "You can't enchant someone else's weapons, especially not while having sex with them!", null);
								
							} else if(index == 6) {
								return new Response("Equip (self)", "You can't use someone else's weapons while having sex with them!", null);
								
							} else if(index == 7) {
								if(Main.game.getPlayer().hasQuest(QuestLine.SIDE_JINXED_CLOTHING) && Main.game.getPlayer().isQuestProgressGreaterThan(QuestLine.SIDE_JINXED_CLOTHING, Quest.SIDE_JINXED_LILAYA_HELP)){
									return new Response("Remove jinx", "You can't remove a jinx while having sex with someone!", null);
								}
								return null;
								
							} else if (index == 10) {
								return getQuickTradeResponse();
								
							} else if(index == 11) {
								return new Response("Equip (opponent)", "You can't use make someone use a weapon while having sex with them!", null);
								
							} else {
								return null;
							}
							
						case TRADING:
							inventoryFull = Main.game.getPlayer().isInventoryFull() && !Main.game.getPlayer().hasWeapon(weapon);
							
							if(index == 1) {
								int sellPrice = buyback?Main.game.getPlayer().getBuybackStack().get(buyBackIndex).getPrice():weapon.getPrice(inventoryNPC.getSellModifier());
								if(inventoryFull) {
									return new Response("Buy (1) ("+UtilText.formatAsMoneyUncoloured(sellPrice, "span")+")", "Your inventory is already full!", null);
								}
								if(Main.game.getPlayer().getMoney() < sellPrice) {
									return new Response("Buy (1) ("+UtilText.formatAsMoneyUncoloured(sellPrice, "span")+")", "You can't afford to buy this!", null);
								}
								return new Response("Buy (1) (" + UtilText.formatAsMoney(sellPrice, "span") + ")", "Buy the " + weapon.getName() + " for " + UtilText.formatAsMoney(sellPrice) + ".", INVENTORY_MENU){
									@Override
									public void effects(){
										sellWeapons(inventoryNPC, Main.game.getPlayer(), weapon, 1, sellPrice);
									}
								};
								
							} else if(index == 2) {
								int sellPrice = buyback?Main.game.getPlayer().getBuybackStack().get(buyBackIndex).getPrice():weapon.getPrice(inventoryNPC.getSellModifier());
								if(buyback) {
									return new Response("Buy (5) ("+UtilText.formatAsMoneyUncoloured(sellPrice*5, "span")+")", "Cannot use this option in the buyback menu.", null);
								}
								if(inventoryNPC.getWeaponCount(weapon) < 5) {
									return new Response("Buy (5) ("+UtilText.formatAsMoneyUncoloured(sellPrice*5, "span")+")", UtilText.parse(inventoryNPC, "[npc.Name] doesn't have five "+weapon.getNamePlural()+"."), null);
								}
								if(inventoryFull) {
									return new Response("Buy (5) ("+UtilText.formatAsMoneyUncoloured(sellPrice*5, "span")+")", "Your inventory is already full!", null);
								}
								if(Main.game.getPlayer().getMoney() < sellPrice*5) {
									return new Response("Buy (5) ("+UtilText.formatAsMoneyUncoloured(sellPrice*5, "span")+")", "You can't afford to buy this!", null);
								}
								return new Response("Buy (5) (" + UtilText.formatAsMoney(sellPrice*5, "span") + ")", "Buy the " + weapon.getName() + " for " + UtilText.formatAsMoney(sellPrice*5) + ".", INVENTORY_MENU){
									@Override
									public void effects(){
										sellWeapons(inventoryNPC, Main.game.getPlayer(), weapon, 5, sellPrice);
									}
								};
								
							} else if(index == 3) {
								int sellPrice = buyback?Main.game.getPlayer().getBuybackStack().get(buyBackIndex).getPrice():weapon.getPrice(inventoryNPC.getSellModifier());
								if(buyback) {
									return new Response("Buy (All) ("+UtilText.formatAsMoneyUncoloured(sellPrice*inventoryNPC.getWeaponCount(weapon), "span")+")", "Cannot use this option in the buyback menu.", null);
								}
								if(inventoryFull) {
									return new Response("Buy (All) ("+UtilText.formatAsMoneyUncoloured(sellPrice*inventoryNPC.getWeaponCount(weapon), "span")+")", "Your inventory is already full!", null);
								}
								if(Main.game.getPlayer().getMoney() < sellPrice*inventoryNPC.getWeaponCount(weapon)) {
									return new Response("Buy (All) ("+UtilText.formatAsMoneyUncoloured(sellPrice*inventoryNPC.getWeaponCount(weapon), "span")+")", "You can't afford to buy this!", null);
								}
								return new Response("Buy (All) (" + UtilText.formatAsMoney(sellPrice*inventoryNPC.getWeaponCount(weapon), "span") + ")",
										"Buy the " + weapon.getName() + " for " + UtilText.formatAsMoney(sellPrice*inventoryNPC.getWeaponCount(weapon)) + ".", INVENTORY_MENU){
									@Override
									public void effects(){
										sellWeapons(inventoryNPC, Main.game.getPlayer(), weapon, inventoryNPC.getWeaponCount(weapon), sellPrice);
									}
								};
								
							} else if(index == 5) {
								return new Response("Enchant", "You can't enchant someone else's weapon!", null);
								
							} else if(index == 6) {
								return new Response("Equip (self)", UtilText.parse(inventoryNPC, "[npc.Name] isn't going to let you equip [npc.her] weapons without buying them first."), null);
								
							} else if (index == 9) {
								return getBuybackResponse();
								
							} else if (index == 10) {
								return getQuickTradeResponse();
								
							} else if(index == 11) {
								return new Response(UtilText.parse(inventoryNPC, "Equip ([npc.Name])"), UtilText.parse(inventoryNPC, "[npc.Name] isn't going to equip the weapons that [npc.she]'s trying to sell!"), null);
								
							} else {
								return null;
							}
					}
				}
			}
			return null;
		}
		
		@Override
		public MapDisplay getMapDisplay() {
			return MapDisplay.INVENTORY;
		}
	};
	
	public static final DialogueNodeOld CLOTHING_INVENTORY = new DialogueNodeOld("Clothing", "", true) {
		private static final long serialVersionUID = 1L;

		@Override
		public String getLabel() {
			if(!Main.game.isInNewWorld()) {
				return "Evening's Attire";
			}
			
			if (Main.game.getDialogueFlags().quickTrade && !Main.game.isInSex() && !Main.game.isInCombat()) {
				return "Inventory (Quick-Manage is <b style='color:" + Colour.GENERIC_GOOD.toWebHexString() + ";'>ON</b>)";
			} else {
				return "Inventory";
			}
		}
		
		@Override
		public String getHeaderContent() {
			return inventoryView();
		}

		@Override
		public String getContent() {
			return getItemDisplayPanel(clothing.getSVGString(),
					clothing.getDisplayName(true),
					clothing.getDescription()
					+ clothing.clothingExtraInformation(null)
					+ (owner!=null && owner.isPlayer()
							? (inventoryNPC != null && interactionType == InventoryInteraction.TRADING
									? inventoryNPC.willBuy(clothing)
											? "<p>"
												+ inventoryNPC.getName("The") + " will buy it for " + UtilText.formatAsMoney(clothing.getPrice(inventoryNPC.getBuyModifier())) + "."
											+ "</p>" 
											: inventoryNPC.getName("The") + " doesn't want to buy this."
										: "")
							:(inventoryNPC != null && interactionType == InventoryInteraction.TRADING
								? "<p>"
										+ inventoryNPC.getName("The") + " will sell it for " + UtilText.formatAsMoney(clothing.getPrice(inventoryNPC.getSellModifier())) + "."
									+ "</p>" 
								: "")))
					+(interactionType==InventoryInteraction.CHARACTER_CREATION?CharacterCreation.getCheckingClothingDescription():"");
		}
		
		@Override
		public Response getResponse(int index) {
			
			if (index == 0) {
				return getReturnToInventoryMenuResponse();
			}
			
			// ****************************** ITEM BELONGS TO THE PLAYER ******************************
			if(owner != null && owner.isPlayer()) {
				
				// ****************************** Interacting with the ground ******************************
				if(inventoryNPC == null) {
					boolean areaFull = Main.game.isPlayerTileFull() && !Main.game.getCurrentCell().getInventory().hasClothing(clothing);
					
					if(index == 1) {
						if(owner.getLocationPlace().isItemsDisappear()) {
							if(areaFull) {
								return new Response("Drop (1)", "This area is full, so you can't drop your " + clothing.getName() + " here!", null);
							} else {
								return new Response("Drop (1)", "Drop your " + clothing.getName() + ".", INVENTORY_MENU){
									@Override
									public void effects(){
										dropClothing(owner, clothing, 1);
									}
								};
							}
						} else {
							if(areaFull) {
								return new Response("Store (1)", "This area is full, so you can't store your " + clothing.getName() + " here!", null);
							} else {
								return new Response("Store (1)", "Store the " + clothing.getName() + " in this area.", INVENTORY_MENU){
									@Override
									public void effects(){
										dropClothing(owner, clothing, 1);
									}
								};
							}
						}
						
					} else if(index == 2) {
						if(owner.getLocationPlace().isItemsDisappear()) {
							if(owner.getClothingCount(clothing) < 5) {
								return new Response("Drop (5)", "You don't have five " + clothing.getNamePlural() + " to give!", null);
								
							} else if(areaFull) {
								return new Response("Drop (5)", "This area is full, so you can't drop your " + clothing.getNamePlural() + " here!", null);
								
							} else {
								return new Response("Drop (5)", "Drop five of your " + clothing.getNamePlural() + ".", INVENTORY_MENU){
									@Override
									public void effects(){
										dropClothing(owner, clothing, 5);
									}
								};
							}
						} else {
							if(owner.getClothingCount(clothing) < 5) {
								return new Response("Store (5)", "You don't have five " + clothing.getNamePlural() + " to give!", null);
								
							} else if(areaFull) {
								return new Response("Store (5)", "This area is full, so you can't store your " + clothing.getNamePlural() + " here!", null);
								
							} else {
								return new Response("Store (5)", "Store five of your " + clothing.getNamePlural() + " in this area.", INVENTORY_MENU){
									@Override
									public void effects(){
										dropClothing(owner, clothing, 5);
									}
								};
							}
						}
						
					} else if(index == 3) {
						if(owner.getLocationPlace().isItemsDisappear()) {
							if(areaFull) {
								return new Response("Drop (All)", "This area is full, so you can't drop your " + clothing.getNamePlural() + " here!", null);
							} else {
								return new Response("Drop (All)", "Drop all of your " + clothing.getNamePlural() + ".", INVENTORY_MENU){
									@Override
									public void effects(){
										dropClothing(owner, clothing, owner.getClothingCount(clothing));
									}
								};
							}
						} else {
							if(areaFull) {
								return new Response("Store (All)", "This area is full, so you can't store your " + clothing.getNamePlural() + " here!", null);
							} else {
								return new Response("Store (All)", "Store all of your " + clothing.getNamePlural() + " in this area.", INVENTORY_MENU){
									@Override
									public void effects(){
										dropClothing(owner, clothing, owner.getClothingCount(clothing));
									}
								};
							}
						}
						
					} else if (index==4) {
						if (Main.game.getPlayer().hasItemType(ItemType.DYE_BRUSH)) {
							boolean hasFullInventory = Main.game.getPlayer().isInventoryFull();
							boolean isDyeingStackItem = Main.game.getPlayer().getMapOfDuplicateClothing().get(clothing) > 1;
							boolean canDye = !(isDyeingStackItem && hasFullInventory);
							if (canDye) {
								return new Response("Dye", "Use a dye-brush to dye this item of clothing.", DYE_CLOTHING);
							} else {
								return new Response("Dye", "Your inventory is full, so you can't dye this item of clothing.", null);
							}
						} else {
							return new Response("Dye", "You'll need to find a dye-brush if you want to dye your clothes.", null);
						}
						
					} else if(index == 5) {
						if(clothing.getEnchantmentItemType()==null) {
							return new Response("Enchant", "This clothing cannot be enchanted!", null);
							
						} else if(Main.game.isDebugMode()) {
							return new Response("Enchant", "Enchant this clothing.", EnchantmentDialogue.ENCHANTMENT_MENU) {
								@Override
								public void effects() {
									EnchantmentDialogue.effects.clear();
									EnchantmentDialogue.resetEnchantmentVariables();
									EnchantmentDialogue.ingredient = clothing;
								}
							};
							
						} else if(Main.game.getPlayer().hasQuest(QuestLine.SIDE_ENCHANTMENT_DISCOVERY)) {
							if(Main.game.getPlayer().isQuestProgressGreaterThan(QuestLine.SIDE_ENCHANTMENT_DISCOVERY, Quest.SIDE_ENCHANTMENTS_LILAYA_HELP)) {
								return new Response("Enchant", "Enchant this clothing.", EnchantmentDialogue.ENCHANTMENT_MENU) {
									@Override
									public void effects() {
										EnchantmentDialogue.effects.clear();
										EnchantmentDialogue.resetEnchantmentVariables();
										EnchantmentDialogue.ingredient = clothing;
									}
								};
							}
						}
						
						return null;
						
					} else if(index == 6) {
						return new Response("Equip (self)", "Equip the " + clothing.getName() + ".", INVENTORY_MENU){
							@Override
							public void effects(){
								Main.game.getTextStartStringBuilder().append("<p style='text-align:center;'>" + Main.game.getPlayer().equipClothingFromInventory(clothing, true, Main.game.getPlayer(), Main.game.getPlayer()) + "</p>");
								populateJinxedClothingList();
							}
						};
							
					} else if (index == 10) {
						return getQuickTradeResponse();
						
					} else if(index == 11) {
						return new Response("Equip (Ground)", "You can't make the ground equip your "+clothing.getName()+"!", null);
						
					} else {
						return null;
					}
					
					
				// ****************************** Interacting with an NPC ******************************
				} else {
					switch(interactionType) {
						case COMBAT:
							if(index == 1) {
								return new Response("Give (1)", "You can't give someone clothing while fighting them!", null);
								
							} else if(index == 2) {
								return new Response("Give (5)", "You can't give someone clothing while fighting them!", null);
								
							} else if(index == 3) {
								return new Response("Give (All)", "You can't give someone clothing while fighting them!", null);
								
							} else if(index == 4) {
								return new Response("Dye", "You can't dye your clothing while fighting someone!", null);
								
							} else if(index == 5) {
								return new Response("Enchant", "You can't enchant clothing while fighting someone!", null);
								
							} else if(index == 6) {
								return new Response("Equip (self)", "Equip the " + clothing.getName() + ".", Combat.ENEMY_ATTACK){
									@Override
									public void effects(){
										Combat.setPlayerTurnText(
												"<p style='text-align:center;'>"
														+ Main.game.getPlayer().equipClothingFromInventory(clothing, true, Main.game.getPlayer(), Main.game.getPlayer())
												+ "</p>");
										populateJinxedClothingList();
										Combat.attackEnemy();
										Combat.setPreviousAction(Attack.NONE);
										Main.mainController.openInventory();
									}
								};
									
							} else if (index == 10) {
								return getQuickTradeResponse();
								
							} else if(index == 11) {
								return new Response("Equip (opponent)", "You can't make your opponent equip clothing while fighting them!", null);
								
							} else {
								return null;
							}
							
						case FULL_MANAGEMENT: case CHARACTER_CREATION:
							boolean inventoryFull = inventoryNPC.isInventoryFull() && !inventoryNPC.hasClothing(clothing);
							
							if(index == 1) {
								if(inventoryFull) {
									return new Response("Give (1)", UtilText.parse(inventoryNPC, "[npc.Name]'s inventory is already full!"), null);
								}
								return new Response("Give (1)", UtilText.parse(inventoryNPC, "Give [npc.name] " + clothing.getClothingType().getDeterminer() + " " + clothing.getName() + "."), INVENTORY_MENU){
									@Override
									public void effects(){
										transferClothing(Main.game.getPlayer(), inventoryNPC, clothing, 1);
									}
								};
								
							} else if(index == 2) {
								if(inventoryFull) {
									return new Response("Give (5)", UtilText.parse(inventoryNPC, "[npc.Name]'s inventory is already full!"), null);
								}
								if(Main.game.getPlayer().getClothingCount(clothing) >= 5) {
									return new Response("Give (5)", UtilText.parse(inventoryNPC, "Give [npc.name] five of your " + clothing.getNamePlural() + "."), INVENTORY_MENU){
										@Override
										public void effects(){
											transferClothing(Main.game.getPlayer(), inventoryNPC, clothing, 5);
										}
									};
								} else {
									return new Response("Give (5)", "You don't have five " + clothing.getNamePlural() + " to give!", null);
								}
								
							} else if(index == 3) {
								if(inventoryFull) {
									return new Response("Give (All)", UtilText.parse(inventoryNPC, "[npc.Name]'s inventory is already full!"), null);
								}
								return new Response("Give (All)", UtilText.parse(inventoryNPC, "Give [npc.name] all of your " + clothing.getNamePlural() + "."), INVENTORY_MENU){
									@Override
									public void effects(){
										transferClothing(Main.game.getPlayer(), inventoryNPC, clothing, Main.game.getPlayer().getClothingCount(clothing));
									}
								};
								
							} else if (index==4) {
								if (Main.game.getPlayer().hasItemType(ItemType.DYE_BRUSH)) {
									boolean hasFullInventory = Main.game.getPlayer().isInventoryFull();
									boolean isDyeingStackItem = Main.game.getPlayer().getMapOfDuplicateClothing().get(clothing) > 1;
									boolean canDye = !(isDyeingStackItem && hasFullInventory);
									if (canDye) {
										return new Response("Dye", "Use a dye-brush to dye this item of clothing.", DYE_CLOTHING);
									} else {
										return new Response("Dye", "Your inventory is full, so you can't dye this item of clothing.", null);
									}
								} else {
									return new Response("Dye", "You'll need to find a dye-brush if you want to dye your clothes.", null);
								}
								
							} else if(index == 5) {
								if(clothing.getEnchantmentItemType()==null) {
									return new Response("Enchant", "This clothing cannot be enchanted!", null);
									
								} else if(Main.game.isDebugMode()) {
									return new Response("Enchant", "Enchant this clothing.", EnchantmentDialogue.ENCHANTMENT_MENU) {
										@Override
										public void effects() {
											EnchantmentDialogue.effects.clear();
											EnchantmentDialogue.resetEnchantmentVariables();
											EnchantmentDialogue.ingredient = clothing;
										}
									};
									
								} else if(Main.game.getPlayer().hasQuest(QuestLine.SIDE_ENCHANTMENT_DISCOVERY)) {
									if(Main.game.getPlayer().isQuestProgressGreaterThan(QuestLine.SIDE_ENCHANTMENT_DISCOVERY, Quest.SIDE_ENCHANTMENTS_LILAYA_HELP)) {
										return new Response("Enchant", "Enchant this clothing.", EnchantmentDialogue.ENCHANTMENT_MENU) {
											@Override
											public void effects() {
												EnchantmentDialogue.effects.clear();
												EnchantmentDialogue.resetEnchantmentVariables();
												EnchantmentDialogue.ingredient = clothing;
											}
										};
									}
								}
								
								return null;
								
							}  else if(index == 6) {
								return new Response("Equip (self)", "Equip the " + clothing.getName() + ".", INVENTORY_MENU){
									@Override
									public void effects(){
										Main.game.getTextStartStringBuilder().append("<p style='text-align:center;'>" + Main.game.getPlayer().equipClothingFromInventory(clothing, true, Main.game.getPlayer(), Main.game.getPlayer()) + "</p>");
										populateJinxedClothingList();
									}
								};
									
							} else if (index == 10) {
								return getQuickTradeResponse();
								
							} else if(index == 11) {
								if(clothing.getClothingType().equals(ClothingType.NECK_SLAVE_COLLAR) && inventoryNPC.isAbleToBeEnslaved() && !inventoryNPC.isSlave()) {
									return new Response(UtilText.parse(inventoryNPC, "Equip ([npc.Name])"), UtilText.parse(inventoryNPC, "Make [npc.name] equip the "+clothing.getName()+"!"), INVENTORY_MENU){
										@Override
										public DialogueNodeOld getNextDialogue() {
											return inventoryNPC.getEnslavementDialogue();
										}
										@Override
										public void effects(){
											Main.game.getPlayer().addSlave(inventoryNPC);
											inventoryNPC.setLocation(WorldType.SLAVER_ALLEY, SlaverAlley.SLAVERY_ADMINISTRATION);
											Main.game.getTextStartStringBuilder().append("<p style='text-align:center;'>" + inventoryNPC.equipClothingFromInventory(clothing, true, Main.game.getPlayer(), Main.game.getPlayer()) + "</p>");
											populateJinxedClothingList();
										}
									};
									
								} else {
									return new Response(UtilText.parse(inventoryNPC, "Equip ([npc.Name])"), UtilText.parse(inventoryNPC, "Make [npc.name] equip the "+clothing.getName()+"!"), INVENTORY_MENU){
										@Override
										public void effects(){
											Main.game.getTextStartStringBuilder().append("<p style='text-align:center;'>" + inventoryNPC.equipClothingFromInventory(clothing, true, Main.game.getPlayer(), Main.game.getPlayer()) + "</p>");
											populateJinxedClothingList();
										}
									};
								}
							
							} else {
								return null;
							}
							
						case SEX:
							if(index == 1) {
								return new Response("Give (1)", "You can't give someone clothing while having sex with them!", null);
								
							} else if(index == 2) {
								return new Response("Give (5)", "You can't give someone clothing while having sex with them!", null);
								
							} else if(index == 3) {
								return new Response("Give (All)", "You can't give someone clothing while having sex with them!", null);
								
							} else if(index == 4) {
								return new Response("Dye", "You can't dye your clothing while having sex with someone!", null);
								
							} else if(index == 5) {
								return new Response("Enchant", "You can't enchant clothing while having sex with someone!", null);
								
							} else if(index == 6) {
								if(clothing.getClothingType().isAbleToBeEquippedDuringSex()) {
									if (Main.game.getPlayer().isAbleToEquip(clothing, false, Main.game.getPlayer())) {
										return new Response("Equip (self)", "Equip the " + clothing.getName() + ".", Sex.SEX_DIALOGUE){
											@Override
											public void effects(){
												Main.game.getPlayer().equipClothingFromInventory(clothing, true, Main.game.getPlayer(), Main.game.getPlayer());
												Sex.setUnequipClothingText(Main.game.getPlayer().getUnequipDescription());
												Main.mainController.openInventory();
												Sex.endSexTurn(SexActionUtility.CLOTHING_REMOVAL);
												Sex.setSexStarted(true);
											}
										};
									} else {
										return new Response("Equip (self)", "You can't equip the " + clothing.getName() + ", as other clothing is blocking you from doing so!", null);
									}
								} else {
									return new Response("Equip (self)", "You can't equip clothing while having sex with someone!", null);
								}
								
							} else if (index == 10) {
								return getQuickTradeResponse();
								
							} else if(index == 11) {
								if(clothing.getClothingType().isAbleToBeEquippedDuringSex()) {
									if (inventoryNPC.isAbleToEquip(clothing, false, Main.game.getPlayer())) {
										return new Response("Equip ([npc.Name])", UtilText.parse(inventoryNPC, "Get [npc.Name] to equip the " + clothing.getName() + "."), Sex.SEX_DIALOGUE){
											@Override
											public void effects(){
												inventoryNPC.equipClothingFromInventory(clothing, true, Main.game.getPlayer(), Main.game.getPlayer());
												Sex.setUnequipClothingText(inventoryNPC.getUnequipDescription());
												Main.mainController.openInventory();
												Sex.endSexTurn(SexActionUtility.CLOTHING_REMOVAL);
												Sex.setSexStarted(true);
											}
										};
									} else {
										return new Response(UtilText.parse(inventoryNPC, "Equip ([npc.Name])"),
												UtilText.parse(inventoryNPC, "[npc.Name] can't equip the " + clothing.getName() + ", as other clothing is blocking [npc.her] from doing so!"), null);
									}
								} else {
									return new Response(UtilText.parse(inventoryNPC, "Equip ([npc.Name])"), "You can't equip clothing while having sex with someone!", null);
								}
								
							} else {
								return null;
							}
							
						case TRADING:
							if(index == 1) {
								if (inventoryNPC.willBuy(clothing)) {
									int sellPrice = clothing.getPrice(inventoryNPC.getBuyModifier());
									return new Response("Sell (1) (" + UtilText.formatAsMoney(sellPrice, "span") + ")", "Sell the " + clothing.getName() + " for " + UtilText.formatAsMoney(sellPrice) + ".", INVENTORY_MENU){
										@Override
										public void effects(){
											sellClothing(Main.game.getPlayer(), inventoryNPC, clothing, 1, sellPrice);
										}
									};
								} else {
									return new Response("Sell (1)", inventoryNPC.getName("The") + " doesn't want to buy this.", null);
								}
								
							} else if(index == 2) {
								if(Main.game.getPlayer().getClothingCount(clothing) >= 5) {
									if (inventoryNPC.willBuy(clothing)) {
										int sellPrice = clothing.getPrice(inventoryNPC.getBuyModifier());
										return new Response("Sell (1) (" + UtilText.formatAsMoney(sellPrice*5, "span") + ")", "Sell five of your " + clothing.getNamePlural() + " for " + UtilText.formatAsMoney(sellPrice*5) + ".", INVENTORY_MENU){
											@Override
											public void effects(){
												sellClothing(Main.game.getPlayer(), inventoryNPC, clothing, 5, sellPrice);
											}
										};
									} else {
										return new Response("Sell (5)", inventoryNPC.getName("The") + " doesn't want to buy these.", null);
									}
									
								} else {
									return new Response("Sell (5)", "You don't have five " + clothing.getNamePlural() + " to sell!", null);
								}
								
							} else if(index == 3) {
								if (inventoryNPC.willBuy(clothing)) {
									int sellPrice = clothing.getPrice(inventoryNPC.getBuyModifier());
									return new Response("Sell (All) (" + UtilText.formatAsMoney(sellPrice*Main.game.getPlayer().getClothingCount(clothing), "span") + ")",
											"Sell the " + clothing.getName() + " for " + UtilText.formatAsMoney(sellPrice*Main.game.getPlayer().getClothingCount(clothing)) + ".", INVENTORY_MENU){
										@Override
										public void effects(){
											sellClothing(Main.game.getPlayer(), inventoryNPC, clothing, Main.game.getPlayer().getClothingCount(clothing), sellPrice);
										}
									};
								} else {
									return new Response("Sell (All)", inventoryNPC.getName("The") + " doesn't want to buy these.", null);
								}
								
							} else if (index==4) {
								if (Main.game.getPlayer().hasItemType(ItemType.DYE_BRUSH)) {
									boolean hasFullInventory = Main.game.getPlayer().isInventoryFull();
									boolean isDyeingStackItem = Main.game.getPlayer().getMapOfDuplicateClothing().get(clothing) > 1;
									boolean canDye = !(isDyeingStackItem && hasFullInventory);
									if (canDye) {
										return new Response("Dye", "Use a dye-brush to dye this item of clothing.", DYE_CLOTHING);
									} else {
										return new Response("Dye", "Your inventory is full, so you can't dye this item of clothing.", null);
									}
								} else {
									return new Response("Dye", "You'll need to find a dye-brush if you want to dye your clothes.", null);
								}
								
							} else if(index == 5) {
								if(clothing.getEnchantmentItemType()==null) {
									return new Response("Enchant", "This clothing cannot be enchanted!", null);
									
								} else if(Main.game.isDebugMode()) {
									return new Response("Enchant", "Enchant this clothing.", EnchantmentDialogue.ENCHANTMENT_MENU) {
										@Override
										public void effects() {
											EnchantmentDialogue.effects.clear();
											EnchantmentDialogue.resetEnchantmentVariables();
											EnchantmentDialogue.ingredient = clothing;
										}
									};
									
								} else if(Main.game.getPlayer().hasQuest(QuestLine.SIDE_ENCHANTMENT_DISCOVERY)) {
									if(Main.game.getPlayer().isQuestProgressGreaterThan(QuestLine.SIDE_ENCHANTMENT_DISCOVERY, Quest.SIDE_ENCHANTMENTS_LILAYA_HELP)) {
										return new Response("Enchant", "Enchant this clothing.", EnchantmentDialogue.ENCHANTMENT_MENU) {
											@Override
											public void effects() {
												EnchantmentDialogue.effects.clear();
												EnchantmentDialogue.resetEnchantmentVariables();
												EnchantmentDialogue.ingredient = clothing;
											}
										};
									}
								}
								
								return null;
								
							} else if(index == 6) {
								return new Response("Equip (self)", "Equip the " + clothing.getName() + ".", INVENTORY_MENU){
										@Override
										public void effects(){
											Main.game.getTextStartStringBuilder().append("<p style='text-align:center;'>" + Main.game.getPlayer().equipClothingFromInventory(clothing, true, Main.game.getPlayer(), Main.game.getPlayer()) + "</p>");
											populateJinxedClothingList();
										}
									};
									
							} else if (index == 9) {
								return getBuybackResponse();
								
							} else if (index == 10) {
								return getQuickTradeResponse();
								
							} else if(index == 11) {
								return new Response(UtilText.parse(inventoryNPC, "Equip ([npc.Name])"), UtilText.parse(inventoryNPC, "[npc.Name] doesn't want to wear your clothing."), null);
								
							} else if (index == 14 && !clothing.isEnchantmentKnown()) {
								if(!inventoryNPC.willBuy(clothing)) {
									return new Response("Identify", inventoryNPC.getName("The") + " can't identify clothing!", null);
									
								} else if(Main.game.getPlayer().getMoney() < IDENTIFICATION_PRICE){
									// don't format as money because we don't want to highlight non-selectable choices
									return new Response("Identify (" + UtilText.formatAsMoneyUncoloured(IDENTIFICATION_PRICE, "span") + ")", "You don't have enough money!", null);
									
								}else {
									return new Response("Identify (" + UtilText.formatAsMoney(IDENTIFICATION_PRICE, "span") + ")",
												"Have the " + clothing.getName() + " identified for " + UtilText.formatAsMoney(IDENTIFICATION_PRICE, "span") + ".", INVENTORY_MENU){
										@Override
										public void effects(){
											Main.game.getPlayer().removeClothing(clothing);
											Main.game.getTextStartStringBuilder().append(
													"<p style='text-align:center;'>" + "You hand over " + UtilText.formatAsMoney(IDENTIFICATION_PRICE) + " to "
															+inventoryNPC.getName("the")+", who promptly identifies your "+clothing.getName()+"."
													+ "</p>"
													+clothing.setEnchantmentKnown(true));
											
											Main.game.getPlayer().addClothing(clothing, false);
											Main.game.getPlayer().incrementMoney(-IDENTIFICATION_PRICE);
										}
									};
								}
							} else {
								return null;
							}
					}
				}
				
			// ****************************** ITEM DOES NOT BELONG TO PLAYER ******************************
				
			} else {
				// ****************************** Interacting with the ground ******************************
				if(inventoryNPC == null) {
					
					if(interactionType == InventoryInteraction.CHARACTER_CREATION) {
						if (index == 1) {
							if(Main.game.getPlayer().isCoverableAreaExposed(CoverableArea.NIPPLES)
									|| Main.game.getPlayer().isCoverableAreaExposed(CoverableArea.ANUS)
									|| Main.game.getPlayer().isCoverableAreaExposed(CoverableArea.PENIS)
									|| Main.game.getPlayer().isCoverableAreaExposed(CoverableArea.VAGINA)
									|| Main.game.getPlayer().getClothingInSlot(InventorySlot.FOOT)==null) {
								return new Response("To the stage", "You need to be wearing clothing that covers your body, as well as a pair of shoes.", null);
								
							} else {
								return new Response("To the stage", "You're ready to approach the stage now.", CharacterCreation.CHOOSE_BACKGROUND) {
									@Override
									public void effects() {
										CharacterCreation.moveNPCIntoPlayerTile();
									}
								};
							}
							
						} else if(index == 2) {
							return new Response("Equip", "Equip the " + clothing.getName() + ".", INVENTORY_MENU){
								@Override
								public void effects(){
									Main.game.getPlayer().equipClothingFromGround(clothing, true, Main.game.getPlayer());
								}
							};
								
						} else if(index == 3) {
							return new Response("Change Colour", "Change the colour of this item of clothing.", DYE_CLOTHING_CHARACTER_CREATION);
						} else {
							return null;
						}
					}
					
					boolean inventoryFull = Main.game.getPlayer().isInventoryFull() && !Main.game.getPlayer().hasClothing(clothing);
					
					if(index == 1) {
						if(inventoryFull) {
							return new Response("Take (1)", "Your inventory is already full!", null);
						}
						return new Response("Take (1)", "Take one " + clothing.getClothingType().getDeterminer() + " " + clothing.getName() + " from the ground.", INVENTORY_MENU){
							@Override
							public void effects(){
								pickUpClothing(Main.game.getPlayer(), clothing, 1);
							}
						};
						
					} else if(index == 2) {
						if(inventoryFull) {
							return new Response("Take (5)", "Your inventory is already full!", null);
						}
						if(Main.game.getCurrentCell().getInventory().getClothingCount(clothing) >= 5) {
							return new Response("Take (5)", "Take five of the " + clothing.getNamePlural() + " from the ground.", INVENTORY_MENU){
								@Override
								public void effects(){
									pickUpClothing(Main.game.getPlayer(), clothing, 5);
								}
							};
						} else {
							return new Response("Take (5)", "There aren't five " + clothing.getNamePlural() + " on the ground!", null);
						}
						
					} else if(index == 3) {
						if(inventoryFull) {
							return new Response("Take (All)", "Your inventory is already full!", null);
						}
						return new Response("Take (All)", "Take all of the " + clothing.getNamePlural() + " from the ground.", INVENTORY_MENU){
							@Override
							public void effects(){
								pickUpClothing(Main.game.getPlayer(), clothing, Main.game.getCurrentCell().getInventory().getClothingCount(clothing));
							}
						};
						
					} else if (index==4) {
						if (Main.game.getPlayer().hasItemType(ItemType.DYE_BRUSH)) {
							boolean hasFullInventory = Main.game.getCurrentCell().getInventory().isInventoryFull();
							boolean isDyeingStackItem = Main.game.getCurrentCell().getInventory().getMapOfDuplicateClothing().get(clothing) > 1;
							boolean canDye = !(isDyeingStackItem && hasFullInventory);
							if (canDye) {
								return new Response("Dye", "Use a dye-brush to dye this item of clothing.", DYE_CLOTHING);
							} else {
								return new Response("Dye", "Your inventory is full, so you can't dye this item of clothing.", null);
							}
						} else {
							return new Response("Dye", "You'll need to find a dye-brush if you want to dye your clothes.", null);
						}
						
					} else if(index == 5) {
						return new Response("Enchant", "You can't enchant clothing on the ground!", null);
						
					} else if(index == 6) {
						return new Response("Equip (self)", "Equip the " + clothing.getName() + ".", INVENTORY_MENU){
							@Override
							public void effects(){
								Main.game.getTextStartStringBuilder().append("<p style='text-align:center;'>" + Main.game.getPlayer().equipClothingFromGround(clothing, true, Main.game.getPlayer()) + "</p>");
								populateJinxedClothingList();
							}
						};
							
					} else if (index == 10) {
						return getQuickTradeResponse();
						
					} else if(index == 11) {
						return new Response("Equip (Ground)", "There's nobody to use the "+clothing.getName()+" on!", null);
						
					} else {
						return null;
					}
					
				// ****************************** Interacting with an NPC ******************************
				} else {
					boolean inventoryFull = false;
					switch(interactionType) {
						case COMBAT:
							if(index == 1) {
								return new Response("Take (1)", "You can't take someone clothing while fighting them!", null);
								
							} else if(index == 2) {
								return new Response("Take (5)", "You can't take someone clothing while fighting them!", null);
								
							} else if(index == 3) {
								return new Response("Take (All)", "You can't take someone clothing while fighting them!", null);
								
							} else if(index == 4) {
								return new Response("Dye", "You can't dye someone's clothing while fighting them!", null);
								
							} else if(index == 5) {
								return new Response("Enchant", "You can't enchant someone else's clothing, especially not while fighting them!", null);
								
							} else if(index == 6) {
								return new Response("Equip (self)", "You can't use someone else's clothing while fighting them!", null);
								
							} else if (index == 10) {
								return getQuickTradeResponse();
								
							} else if(index == 11) {
								return new Response("Equip (opponent)", "You can't make someone wear clothing while fighting them!", null);
								
							} else {
								return null;
							}
							
						case FULL_MANAGEMENT: case CHARACTER_CREATION:
							inventoryFull = Main.game.getPlayer().isInventoryFull() && !Main.game.getPlayer().hasClothing(clothing);
						
							if(index == 1) {
								if(inventoryFull) {
									return new Response("Take (1)", "Your inventory is already full!", null);
								}
								return new Response("Take (1)", UtilText.parse(inventoryNPC, "Take " + clothing.getClothingType().getDeterminer() + " " + clothing.getName() + " from [npc.name]."), INVENTORY_MENU){
									@Override
									public void effects(){
										transferClothing(inventoryNPC, Main.game.getPlayer(), clothing, 1);
									}
								};
								
							} else if(index == 2) {
								if(inventoryFull) {
									return new Response("Take (5)", "Your inventory is already full!", null);
								}
								if(inventoryNPC.getClothingCount(clothing) >= 5) {
									return new Response("Take (5)", UtilText.parse(inventoryNPC, "Take five of  [npc.name]'s " + clothing.getNamePlural() + "."), INVENTORY_MENU){
										@Override
										public void effects(){
											transferClothing(inventoryNPC, Main.game.getPlayer(), clothing, 5);
										}
									};
								} else {
									return new Response("Take (5)", UtilText.parse(inventoryNPC, "[npc.Name] doesn't have five " + clothing.getNamePlural() + "!"), null);
								}
								
							} else if(index == 3) {
								if(inventoryFull) {
									return new Response("Take (All)", "Your inventory is already full!", null);
								}
								return new Response("Take (All)", UtilText.parse(inventoryNPC, "Take five of  [npc.name]'s " + clothing.getNamePlural() + "."), INVENTORY_MENU){
									@Override
									public void effects(){
										transferClothing(inventoryNPC, Main.game.getPlayer(), clothing, inventoryNPC.getClothingCount(clothing));
									}
								};
								
							} else if (index==4) {
								if (Main.game.getPlayer().hasItemType(ItemType.DYE_BRUSH)) {
									boolean hasFullInventory = inventoryNPC.isInventoryFull();
									boolean isDyeingStackItem = inventoryNPC.getMapOfDuplicateClothing().get(clothing) > 1;
									boolean canDye = !(isDyeingStackItem && hasFullInventory);
									if (canDye) {
										return new Response("Dye", "Use a dye-brush to dye this item of clothing.", DYE_CLOTHING);
									} else {
										return new Response("Dye", UtilText.parse(inventoryNPC, "[npc.Name]'s inventory is full, so you can't dye this item of clothing."), null);
									}
								} else {
									return new Response("Dye", UtilText.parse(inventoryNPC, "You'll need to find another dye-brush if you want to dye [npc.name]'s clothes."), null);
								}
								
							} else if(index == 5) {
								return new Response("Enchant", "You can't enchant clothing owned by someone else!", null);
								
							} else if(index == 6) {
								return new Response("Equip (self)", "Equip the " + clothing.getName() + ".", INVENTORY_MENU){
									@Override
									public void effects(){
										Main.game.getTextStartStringBuilder().append("<p style='text-align:center;'>" + Main.game.getPlayer().equipClothingFromInventory(clothing, true, Main.game.getPlayer(), inventoryNPC) + "</p>");
										populateJinxedClothingList();
									}
								};
								
							} else if (index == 10) {
								return getQuickTradeResponse();
								
							} else if(index == 11) {

								if(clothing.getClothingType().equals(ClothingType.NECK_SLAVE_COLLAR) && inventoryNPC.isAbleToBeEnslaved() && !inventoryNPC.isSlave()) {
									return new Response(UtilText.parse(inventoryNPC, "Equip ([npc.Name])"), UtilText.parse(inventoryNPC, "Make [npc.name] equip the "+clothing.getName()+"!"), INVENTORY_MENU){
										@Override
										public DialogueNodeOld getNextDialogue() {
											return inventoryNPC.getEnslavementDialogue();
										}
										@Override
										public void effects(){
											Main.game.getPlayer().addSlave(inventoryNPC);
											inventoryNPC.setLocation(WorldType.SLAVER_ALLEY, SlaverAlley.SLAVERY_ADMINISTRATION);
											Main.game.getTextStartStringBuilder().append("<p style='text-align:center;'>" + inventoryNPC.equipClothingFromInventory(clothing, true, Main.game.getPlayer(), inventoryNPC) + "</p>");
											populateJinxedClothingList();
										}
									};
									
								} else {
									return new Response(UtilText.parse(inventoryNPC, "Equip ([npc.Name])"), UtilText.parse(inventoryNPC, "Get [npc.name] to equip the " + clothing.getName() + "."), INVENTORY_MENU){
										@Override
										public void effects(){
											Main.game.getTextStartStringBuilder().append("<p style='text-align:center;'>" + inventoryNPC.equipClothingFromInventory(clothing, true, Main.game.getPlayer(), inventoryNPC) + "</p>");
											populateJinxedClothingList();
										}
									};
								}
								
							} else {
								return null;
							}
							
						case SEX:
							if(index == 1) {
								return new Response("Take (1)", "You can't take someone's clothing while having sex with them!", null);
								
							} else if(index == 2) {
								return new Response("Take (5)", "You can't take someone's clothing while having sex with them!", null);
								
							} else if(index == 3) {
								return new Response("Take (All)", "You can't take someone's clothing while having sex with them!", null);
								
							} else if(index == 4) {
								return new Response("Dye", "You can't dye someone's clothing while having sex with them!", null);
								
							} else if(index == 5) {
								return new Response("Enchant", "You can't enchant someone else's clothing, especially not while having sex with them!", null);
								
							} else if(index == 6) {
								if(clothing.getClothingType().isAbleToBeEquippedDuringSex()) {
									if (Main.game.getPlayer().isAbleToEquip(clothing, false, Main.game.getPlayer())) {
										return new Response("Equip (self)", "Equip the " + clothing.getName() + ".", Sex.SEX_DIALOGUE){
											@Override
											public void effects(){
												Main.game.getPlayer().equipClothingFromInventory(clothing, true, Main.game.getPlayer(), owner);
												Sex.setUnequipClothingText(Main.game.getPlayer().getUnequipDescription());
												Main.mainController.openInventory();
												Sex.endSexTurn(SexActionUtility.CLOTHING_REMOVAL);
												Sex.setSexStarted(true);
											}
										};
									} else {
										return new Response("Equip (self)", "You can't equip the " + clothing.getName() + ", as other clothing is blocking you from doing so!", null);
									}
								} else {
									return new Response("Equip (self)", "You can't equip this clothing while having sex with someone!", null);
								}
								
							} else if (index == 10) {
								return getQuickTradeResponse();
								
							} else if(index == 11) {
								if(clothing.getClothingType().isAbleToBeEquippedDuringSex()) {
									if (inventoryNPC.isAbleToEquip(clothing, false, Main.game.getPlayer())) {
										return new Response("Equip ([npc.Name])", UtilText.parse(inventoryNPC, "Get [npc.Name] to equip the " + clothing.getName() + "."), Sex.SEX_DIALOGUE){
											@Override
											public void effects(){
												inventoryNPC.equipClothingFromInventory(clothing, true, Main.game.getPlayer(), inventoryNPC);
												Sex.setUnequipClothingText(inventoryNPC.getUnequipDescription());
												Main.mainController.openInventory();
												Sex.endSexTurn(SexActionUtility.CLOTHING_REMOVAL);
												Sex.setSexStarted(true);
											}
										};
									} else {
										return new Response(UtilText.parse(inventoryNPC, "Equip ([npc.Name])"),
												UtilText.parse(inventoryNPC, "[npc.Name] can't equip the " + clothing.getName() + ", as other clothing is blocking [npc.her] from doing so!"), null);
									}
								} else {
									return new Response(UtilText.parse(inventoryNPC, "Equip ([npc.Name])"), "You can't equip this clothing while having sex with someone!", null);
								}
								
							} else {
								return null;
							}
							
						case TRADING:
							inventoryFull = Main.game.getPlayer().isInventoryFull() && !Main.game.getPlayer().hasClothing(clothing);
							
							if(index == 1) {
								int sellPrice = buyback?Main.game.getPlayer().getBuybackStack().get(buyBackIndex).getPrice():clothing.getPrice(inventoryNPC.getSellModifier());
								if(inventoryFull) {
									return new Response("Buy (1) ("+UtilText.formatAsMoneyUncoloured(sellPrice, "span")+")", "Your inventory is already full!", null);
								}
								if(Main.game.getPlayer().getMoney() < sellPrice) {
									return new Response("Buy (1) ("+UtilText.formatAsMoneyUncoloured(sellPrice, "span")+")", "You can't afford to buy this!", null);
								}
								return new Response("Buy (1) (" + UtilText.formatAsMoney(sellPrice, "span") + ")", "Buy the " + clothing.getName() + " for " + UtilText.formatAsMoney(sellPrice) + ".", INVENTORY_MENU){
									@Override
									public void effects(){
										sellClothing(inventoryNPC, Main.game.getPlayer(), clothing, 1, sellPrice);
									}
								};
								
							} else if(index == 2) {
								int sellPrice = buyback?Main.game.getPlayer().getBuybackStack().get(buyBackIndex).getPrice():clothing.getPrice(inventoryNPC.getSellModifier());
								if(buyback) {
									return new Response("Buy (5) ("+UtilText.formatAsMoneyUncoloured(sellPrice*5, "span")+")", "Cannot use this option in the buyback menu.", null);
								}
								if(inventoryNPC.getClothingCount(clothing) < 5) {
									return new Response("Buy (5) ("+UtilText.formatAsMoneyUncoloured(sellPrice*5, "span")+")", UtilText.parse(inventoryNPC, "[npc.Name] doesn't have five "+clothing.getNamePlural()+"."), null);
								}
								if(inventoryFull) {
									return new Response("Buy (5) ("+UtilText.formatAsMoneyUncoloured(sellPrice*5, "span")+")", "Your inventory is already full!", null);
								}
								if(Main.game.getPlayer().getMoney() < sellPrice*5) {
									return new Response("Buy (5) ("+UtilText.formatAsMoneyUncoloured(sellPrice*5, "span")+")", "You can't afford to buy this!", null);
								}
								return new Response("Buy (5) (" + UtilText.formatAsMoney(sellPrice*5, "span") + ")", "Buy the " + clothing.getName() + " for " + UtilText.formatAsMoney(sellPrice*5) + ".", INVENTORY_MENU){
									@Override
									public void effects(){
										sellClothing(inventoryNPC, Main.game.getPlayer(), clothing, 5, sellPrice);
									}
								};
								
							} else if(index == 3) {
								int sellPrice = buyback?Main.game.getPlayer().getBuybackStack().get(buyBackIndex).getPrice():clothing.getPrice(inventoryNPC.getSellModifier());
								if(buyback) {
									return new Response("Buy (All) ("+UtilText.formatAsMoneyUncoloured(sellPrice*inventoryNPC.getClothingCount(clothing), "span")+")", "Cannot use this option in the buyback menu.", null);
								}
								if(inventoryFull) {
									return new Response("Buy (All) ("+UtilText.formatAsMoneyUncoloured(sellPrice*inventoryNPC.getClothingCount(clothing), "span")+")", "Your inventory is already full!", null);
								}
								if(Main.game.getPlayer().getMoney() < sellPrice*inventoryNPC.getClothingCount(clothing)) {
									return new Response("Buy (All) ("+UtilText.formatAsMoneyUncoloured(sellPrice*inventoryNPC.getClothingCount(clothing), "span")+")", "You can't afford to buy this!", null);
								}
								return new Response("Buy (All) (" + UtilText.formatAsMoney(sellPrice*inventoryNPC.getClothingCount(clothing), "span") + ")",
										"Buy the " + clothing.getName() + " for " + UtilText.formatAsMoney(sellPrice*inventoryNPC.getClothingCount(clothing)) + ".", INVENTORY_MENU){
									@Override
									public void effects(){
										sellClothing(inventoryNPC, Main.game.getPlayer(), clothing, inventoryNPC.getClothingCount(clothing), sellPrice);
									}
								};
								
							} else if(index == 4) {
								return new Response("Dye", UtilText.parse(inventoryNPC, "[npc.Name] isn't going to let you dye the clothing that [npc.she]'s trying to sell!"), null);
								
							} else if(index == 5) {
								return new Response("Enchant", "You can't enchant someone else's clothing!", null);
								
							} else if(index == 6) {
								return new Response("Equip (self)", UtilText.parse(inventoryNPC, "[npc.Name] isn't going to let you use [npc.her] clothing without buying them first."), null);
								
							} else if (index == 9) {
								return getBuybackResponse();
								
							} else if (index == 10) {
								return getQuickTradeResponse();
								
							} else if(index == 11) {
								return new Response(UtilText.parse(inventoryNPC, "Equip ([npc.Name])"), UtilText.parse(inventoryNPC, "[npc.Name] isn't going to use the clothing that [npc.she]'s trying to sell!"), null);
								
							} else {
								return null;
							}
					}
				}
			}
			return null;
			
		}

		@Override
		public MapDisplay getMapDisplay() {
			return MapDisplay.INVENTORY;
		}
	};
	
	public static final DialogueNodeOld WEAPON_EQUIPPED = new DialogueNodeOld("Weapon equipped", "", true) {
		
		private static final long serialVersionUID = 1L;

		@Override
		public String getLabel() {
			if (Main.game.getDialogueFlags().quickTrade && !Main.game.isInSex() && !Main.game.isInCombat()) {
				return "Inventory (Quick-Manage is <b style='color:" + Colour.GENERIC_GOOD.toWebHexString() + ";'>ON</b>)";
			} else {
				return "Inventory";
			}
		}
		
		@Override
		public String getHeaderContent() {
			return inventoryView();
		}

		@Override
		public String getContent() {
			return getItemDisplayPanel(weapon.getSVGString(),
					weapon.getDisplayName(true),
					 weapon.getDescription());
		}
		
		@Override
		public Response getResponse(int index) {
			
			if (index == 0) {
				return getReturnToInventoryMenuResponse();
			}
			
			// ****************************** ITEM BELONGS TO THE PLAYER ****************************** TODO
			if(owner != null && owner.isPlayer()) {
				switch(interactionType) {
					case COMBAT:
						if(index == 1) {
							return new Response("Unequip", "Unequip the " + weapon.getName() + ".", Combat.ENEMY_ATTACK){
								@Override
								public void effects(){
									Combat.setPlayerTurnText(
											"<p style='text-align:center;'>"
												+ (weapon.getWeaponType().getSlot()==InventorySlot.WEAPON_MAIN
													?Main.game.getPlayer().unequipMainWeapon(false)
													:Main.game.getPlayer().unequipOffhandWeapon(false))
											+ "</p>");
									Combat.attackEnemy();
									Combat.setPreviousAction(Attack.NONE);
									Main.mainController.openInventory();
								}
							};
							
						} else if (index == 2) {
							boolean areaFull = Main.game.isPlayerTileFull() && !Main.game.getCurrentCell().getInventory().hasWeapon(weapon);
							if(Main.game.getPlayer().getLocationPlace().isItemsDisappear()) {
								if(areaFull) {
									return new Response("Drop", "This area is full, so you can't drop your " + weapon.getName() + " here!", null);
								} else {
									return new Response("Drop", "Drop your " + weapon.getName() + ".", Combat.ENEMY_ATTACK){
										@Override
										public void effects(){
											Combat.setPlayerTurnText(
													"<p style='text-align:center;'>"
														+ (weapon.getWeaponType().getSlot()==InventorySlot.WEAPON_MAIN
															?Main.game.getPlayer().unequipMainWeapon(true)
															:Main.game.getPlayer().unequipOffhandWeapon(true))
													+ "</p>");
											Combat.attackEnemy();
											Combat.setPreviousAction(Attack.NONE);
											Main.mainController.openInventory();
										}
									};
								}
								
							} else {
								if(areaFull) {
									return new Response("Store", "This area is full, so you can't store your " + weapon.getName() + " here!", null);
								} else {
									return new Response("Store", "Store your " + weapon.getName() + " in this area.", Combat.ENEMY_ATTACK){
										@Override
										public void effects(){
											Combat.setPlayerTurnText(
													"<p style='text-align:center;'>"
														+ (weapon.getWeaponType().getSlot()==InventorySlot.WEAPON_MAIN
															?Main.game.getPlayer().unequipMainWeapon(true)
															:Main.game.getPlayer().unequipOffhandWeapon(true))
													+ "</p>");
											Combat.attackEnemy();
											Combat.setPreviousAction(Attack.NONE);
											Main.mainController.openInventory();
										}
									};
								}
							}
							
						} else if(index == 5) {
							return new Response("Enchant", "You can't enchant equipped weapons!", null);
							
						} else if (index == 10) {
							return getQuickTradeResponse();
							
						} else {
							return null;
						}
						
					case FULL_MANAGEMENT: case TRADING: case CHARACTER_CREATION:
						if(index == 1) {
							return new Response("Unequip", "Unequip the " + weapon.getName() + ".", INVENTORY_MENU){
								@Override
								public void effects(){
									Main.game.getTextStartStringBuilder().append(
											"<p style='text-align:center;'>"
												+ (weapon.getWeaponType().getSlot()==InventorySlot.WEAPON_MAIN
													?Main.game.getPlayer().unequipMainWeapon(false)
													:Main.game.getPlayer().unequipOffhandWeapon(false))
											+ "</p>");
								}
							};
							
						} else if (index == 2) {
							boolean areaFull = Main.game.isPlayerTileFull() && !Main.game.getCurrentCell().getInventory().hasWeapon(weapon);
							if(Main.game.getPlayer().getLocationPlace().isItemsDisappear()) {
								if(areaFull) {
									return new Response("Drop", "This area is full, so you can't drop your " + weapon.getName() + " here!", null);
								} else {
									return new Response("Drop", "Drop your " + weapon.getName() + ".", INVENTORY_MENU){
										@Override
										public void effects(){
											Main.game.getTextStartStringBuilder().append(
													"<p style='text-align:center;'>"
														+ (weapon.getWeaponType().getSlot()==InventorySlot.WEAPON_MAIN
															?Main.game.getPlayer().unequipMainWeapon(true)
															:Main.game.getPlayer().unequipOffhandWeapon(true))
													+ "</p>");
										}
									};
								}
								
							} else {
								if(areaFull) {
									return new Response("Store", "This area is full, so you can't store your " + weapon.getName() + " here!", null);
								} else {
									return new Response("Store", "Store your " + weapon.getName() + " in this area.", INVENTORY_MENU){
										@Override
										public void effects(){
											Main.game.getTextStartStringBuilder().append(
													"<p style='text-align:center;'>"
														+ (weapon.getWeaponType().getSlot()==InventorySlot.WEAPON_MAIN
															?Main.game.getPlayer().unequipMainWeapon(true)
															:Main.game.getPlayer().unequipOffhandWeapon(true))
													+ "</p>");
										}
									};
								}
							}
							
						} else if(index == 5) {
							return new Response("Enchant", "You can't enchant equipped weapons!", null);
							
						} else if (index == 10) {
							return getQuickTradeResponse();
							
						} else {
							return null;
						}
						
					case SEX:
						if(index == 1) {
							return new Response("Unequip", "Unequip the " + weapon.getName() + ".", Sex.SEX_DIALOGUE){
								@Override
								public void effects(){
									Sex.setUnequipClothingText("<p style='text-align:center;'>"
											+ (weapon.getWeaponType().getSlot()==InventorySlot.WEAPON_MAIN
											?Main.game.getPlayer().unequipMainWeapon(false)
											:Main.game.getPlayer().unequipOffhandWeapon(false))
									+ "</p>");
									Main.mainController.openInventory();
									Sex.endSexTurn(SexActionUtility.CLOTHING_REMOVAL);
									Sex.setSexStarted(true);
								}
							};
							
						} else if (index == 2) {
							boolean areaFull = Main.game.isPlayerTileFull() && !Main.game.getCurrentCell().getInventory().hasWeapon(weapon);
							if(Main.game.getPlayer().getLocationPlace().isItemsDisappear()) {
								if(areaFull) {
									return new Response("Drop", "This area is full, so you can't drop your " + weapon.getName() + " here!", null);
								} else {
									return new Response("Drop", "Drop your " + weapon.getName() + ".", Sex.SEX_DIALOGUE){
										@Override
										public void effects(){
											Sex.setUnequipClothingText("<p style='text-align:center;'>"
													+ (weapon.getWeaponType().getSlot()==InventorySlot.WEAPON_MAIN
													?Main.game.getPlayer().unequipMainWeapon(true)
													:Main.game.getPlayer().unequipOffhandWeapon(true))
											+ "</p>");
											Main.mainController.openInventory();
											Sex.endSexTurn(SexActionUtility.CLOTHING_REMOVAL);
											Sex.setSexStarted(true);
										}
									};
								}
								
							} else {
								if(areaFull) {
									return new Response("Store", "This area is full, so you can't store your " + weapon.getName() + " here!", null);
								} else {
									return new Response("Store", "Store your " + weapon.getName() + " in this area.", Sex.SEX_DIALOGUE){
										@Override
										public void effects(){
											Sex.setUnequipClothingText("<p style='text-align:center;'>"
													+ (weapon.getWeaponType().getSlot()==InventorySlot.WEAPON_MAIN
													?Main.game.getPlayer().unequipMainWeapon(true)
													:Main.game.getPlayer().unequipOffhandWeapon(true))
											+ "</p>");
											Main.mainController.openInventory();
											Sex.endSexTurn(SexActionUtility.CLOTHING_REMOVAL);
											Sex.setSexStarted(true);
										}
									};
								}
							}
							
						} else if(index == 5) {
							return new Response("Enchant", "You can't enchant equipped weapons!", null);
							
						} else if (index == 10) {
							return getQuickTradeResponse();
							
						} else {
							return null;
						}
				}
				
			// ****************************** ITEM DOES NOT BELONG TO PLAYER ******************************
				
			} else {
				switch(interactionType) {
					case COMBAT:
						if(index == 1) {
							return new Response("Unequip", "You can't unequip someone's weapon while fighting them!", null);
							
						} else if (index == 2) {
							return new Response("Drop", "You can't make someone drop their weapon while fighting them!", null);
							
						} else if(index == 5) {
							return new Response("Enchant", "You can't enchant someone else's equipped weapon, especially not while fighting them!", null);
							
						} else if (index == 10) {
							return getQuickTradeResponse();
							
						} else {
							return null;
						}
						
					case FULL_MANAGEMENT:  case CHARACTER_CREATION:
						if(index == 1) {
							return new Response("Unequip", "Unequip the " + weapon.getName() + ".", INVENTORY_MENU){
								@Override
								public void effects(){
									Main.game.getTextStartStringBuilder().append(
											"<p style='text-align:center;'>"
												+ (weapon.getWeaponType().getSlot()==InventorySlot.WEAPON_MAIN
													?inventoryNPC.unequipMainWeapon(false)
													:inventoryNPC.unequipOffhandWeapon(false))
											+ "</p>");
								}
							};
							
						} else if (index == 2) {
							boolean areaFull = Main.game.isPlayerTileFull() && !Main.game.getCurrentCell().getInventory().hasWeapon(weapon);
							if(Main.game.getPlayer().getLocationPlace().isItemsDisappear()) {
								if(areaFull) {
									return new Response("Drop", "This area is full, so you can't drop your " + weapon.getName() + " here!", null);
								} else {
									return new Response("Drop", "Drop your " + weapon.getName() + ".", INVENTORY_MENU){
										@Override
										public void effects(){
											Main.game.getTextStartStringBuilder().append(
													"<p style='text-align:center;'>"
														+ (weapon.getWeaponType().getSlot()==InventorySlot.WEAPON_MAIN
															?inventoryNPC.unequipMainWeapon(true)
															:inventoryNPC.unequipOffhandWeapon(true))
													+ "</p>");
										}
									};
								}
								
							} else {
								if(areaFull) {
									return new Response("Store", "This area is full, so you can't store your " + weapon.getName() + " here!", null);
								} else {
									return new Response("Store", "Store your " + weapon.getName() + " in this area.", INVENTORY_MENU){
										@Override
										public void effects(){
											Main.game.getTextStartStringBuilder().append(
													"<p style='text-align:center;'>"
														+ (weapon.getWeaponType().getSlot()==InventorySlot.WEAPON_MAIN
															?inventoryNPC.unequipMainWeapon(true)
															:inventoryNPC.unequipOffhandWeapon(true))
													+ "</p>");
										}
									};
								}
							}
							
						} else if(index == 5) {
							return new Response("Enchant", "You can't enchant equipped weapons!", null);
							
						} else if (index == 10) {
							return getQuickTradeResponse();
							
						} else {
							return null;
						}
						
					case SEX:
						if(index == 1) {
							return new Response("Unequip", "Unequip the " + weapon.getName() + ".", Sex.SEX_DIALOGUE){
								@Override
								public void effects(){
									Sex.setUnequipClothingText("<p style='text-align:center;'>"
											+ (weapon.getWeaponType().getSlot()==InventorySlot.WEAPON_MAIN
											?inventoryNPC.unequipMainWeapon(false)
											:inventoryNPC.unequipOffhandWeapon(false))
									+ "</p>");
									Main.mainController.openInventory();
									Sex.endSexTurn(SexActionUtility.CLOTHING_REMOVAL);
									Sex.setSexStarted(true);
								}
							};
							
						} else if (index == 2) {
							boolean areaFull = Main.game.isPlayerTileFull() && !Main.game.getCurrentCell().getInventory().hasWeapon(weapon);
							if(Main.game.getPlayer().getLocationPlace().isItemsDisappear()) {
								if(areaFull) {
									return new Response("Drop", "This area is full, so you can't drop your " + weapon.getName() + " here!", null);
								} else {
									return new Response("Drop", "Drop your " + weapon.getName() + ".", Sex.SEX_DIALOGUE){
										@Override
										public void effects(){
											Sex.setUnequipClothingText("<p style='text-align:center;'>"
													+ (weapon.getWeaponType().getSlot()==InventorySlot.WEAPON_MAIN
													?inventoryNPC.unequipMainWeapon(true)
													:inventoryNPC.unequipOffhandWeapon(true))
											+ "</p>");
											Main.mainController.openInventory();
											Sex.endSexTurn(SexActionUtility.CLOTHING_REMOVAL);
											Sex.setSexStarted(true);
										}
									};
								}
								
							} else {
								if(areaFull) {
									return new Response("Store", "This area is full, so you can't store your " + weapon.getName() + " here!", null);
								} else {
									return new Response("Store", "Store your " + weapon.getName() + " in this area.", Sex.SEX_DIALOGUE){
										@Override
										public void effects(){
											Sex.setUnequipClothingText("<p style='text-align:center;'>"
													+ (weapon.getWeaponType().getSlot()==InventorySlot.WEAPON_MAIN
													?inventoryNPC.unequipMainWeapon(true)
													:inventoryNPC.unequipOffhandWeapon(true))
											+ "</p>");
											Main.mainController.openInventory();
											Sex.endSexTurn(SexActionUtility.CLOTHING_REMOVAL);
											Sex.setSexStarted(true);
										}
									};
								}
							}
							
						} else if(index == 5) {
							return new Response("Enchant", "You can't enchant equipped weapons!", null);
							
						} else if (index == 10) {
							return getQuickTradeResponse();
							
						} else {
							return null;
						}
						
						case TRADING:
							if(index == 1) {
								return new Response("Unequip", "You can't unequip someone's weapon!", null);
								
							} else if (index == 2) {
								return new Response("Drop", "You can't make someone drop their weapon!", null);
								
							} else if(index == 5) {
								return new Response("Enchant", "You can't enchant someone else's equipped weapon!", null);
								
							} else if (index == 10) {
								return getQuickTradeResponse();
								
							} else {
								return null;
							}
					}
				
				}
				return null;
		}

		@Override
		public MapDisplay getMapDisplay() {
			return MapDisplay.INVENTORY;
		}
	};
	public static final DialogueNodeOld CLOTHING_EQUIPPED = new DialogueNodeOld("Clothing equipped", "", true) {
		private static final long serialVersionUID = 1L;

		@Override
		public String getLabel() {
			if(!Main.game.isInNewWorld()) {
				return "Evening's Attire";
			}
			
			if (Main.game.getDialogueFlags().quickTrade && !Main.game.isInSex() && !Main.game.isInCombat()) {
				return "Inventory (Quick-Manage is <b style='color:" + Colour.GENERIC_GOOD.toWebHexString() + ";'>ON</b>)";
			} else {
				return "Inventory";
			}
		}
		
		@Override
		public String getHeaderContent() {
			return inventoryView();
		}

		@Override
		public String getContent() {
			return getItemDisplayPanel(clothing.getSVGEquippedString(),
					clothing.getDisplayName(true),
					clothing.getDescription()
					+ clothing.clothingExtraInformation((Main.game.isInSex()?owner:Main.game.getPlayer()))
					+ (Main.game.isInSex()||Main.game.isInCombat()?clothing.getDisplacementBlockingDescriptions(owner):""))
					+(interactionType==InventoryInteraction.CHARACTER_CREATION?CharacterCreation.getCheckingClothingDescription():"");
		}
		
		@Override
		public Response getResponse(int index) {
			if (index == 0) {
				return getReturnToInventoryMenuResponse();
			}
			
			// ****************************** ITEM BELONGS TO THE PLAYER ****************************** TODO
			if(owner != null && owner.isPlayer()) {
				switch(interactionType) {
					case COMBAT:
						if (index == 1) {
							boolean areaFull = Main.game.isPlayerTileFull() && !Main.game.getCurrentCell().getInventory().hasClothing(clothing);
							if(Main.game.getPlayer().getLocationPlace().isItemsDisappear()) {
								if(areaFull && !clothing.getClothingType().isDiscardedOnUnequip()) {
									return new Response("Drop", "This area is full, so you can't drop your " + clothing.getName() + " here!", null);
								} else {
									return new Response((clothing.getClothingType().isDiscardedOnUnequip()?"Discard":"Drop"),
											(clothing.getClothingType().isDiscardedOnUnequip()?"Take off your " + clothing.getName() + " and throw it away.":"Drop your " + clothing.getName() + "."),
											Combat.ENEMY_ATTACK){
										@Override
										public void effects(){
											if(clothing.getClothingType().isDiscardedOnUnequip()) {
												owner.unequipClothingIntoVoid(clothing, true, Main.game.getPlayer());
											} else {
												owner.unequipClothingOntoFloor(clothing, true, Main.game.getPlayer());
											}
											Combat.setPlayerTurnText(owner.getUnequipDescription());
											Combat.attackEnemy();
											Combat.setPreviousAction(Attack.NONE);
											Main.mainController.openInventory();
										}
									};
								}
								
							} else {
								if(areaFull && !clothing.getClothingType().isDiscardedOnUnequip()) {
									return new Response("Store", "This area is full, so you can't store your " + clothing.getName() + " here!", null);
								} else {
									return new Response((clothing.getClothingType().isDiscardedOnUnequip()?"Discard":"Store"),
											(clothing.getClothingType().isDiscardedOnUnequip()?"Take off your " + clothing.getName() + " and throw it away.":"Store your " + clothing.getName() + " in this area."),
											Combat.ENEMY_ATTACK){
										@Override
										public void effects(){
											if(clothing.getClothingType().isDiscardedOnUnequip()) {
												owner.unequipClothingIntoVoid(clothing, true, Main.game.getPlayer());
											} else {
												owner.unequipClothingOntoFloor(clothing, true, Main.game.getPlayer());
											}
											Combat.setPlayerTurnText(owner.getUnequipDescription());
											Combat.attackEnemy();
											Combat.setPreviousAction(Attack.NONE);
											Main.mainController.openInventory();
										}
									};
								}
							}
							
						} else if (index==4) {
							return new Response("Dye", "You can't dye your clothes in combat!", null);
							
						} else if(index == 5) {
							return new Response("Enchant", "You can't enchant equipped clothing!", null);
							
						} else if(index == 6 && !clothing.getClothingType().isDiscardedOnUnequip()) {
							if (owner.isAbleToUnequip(clothing, false, Main.game.getPlayer())) {
								return new Response("Unequip", "Unequip the " + clothing.getName() + ".", Combat.ENEMY_ATTACK){
									@Override
									public void effects(){
										if(clothing.getClothingType().isDiscardedOnUnequip()) {
											owner.unequipClothingIntoVoid(clothing, true, Main.game.getPlayer());
										} else {
											owner.unequipClothingIntoInventory(clothing, true, Main.game.getPlayer());
										}
										Combat.setPlayerTurnText(owner.getUnequipDescription());
										Combat.attackEnemy();
										Combat.setPreviousAction(Attack.NONE);
										Main.mainController.openInventory();
									}
								};
							} else {
								return new Response("Unequip", "You can't unequip the " + clothing.getName() + ", as other clothing is blocking you from doing so!", null);
							}
							
						} else if (index == 10) {
							return getQuickTradeResponse();
							
						} else if (index > 10 && index - 11 < clothing.getClothingType().getBlockedPartsKeysAsListWithoutNONE().size()){
							
							if (clothing.getDisplacedList().contains(clothing.getClothingType().getBlockedPartsKeysAsListWithoutNONE().get(index - 11))) {
								
								if(owner.isAbleToBeReplaced(clothing, clothing.getClothingType().getBlockedPartsKeysAsListWithoutNONE().get(index -11), false, false, Main.game.getPlayer())){
								
									return new Response(Util.capitaliseSentence(clothing.getClothingType().getBlockedPartsKeysAsListWithoutNONE().get(index - 11).getOppositeDescription()),
											Util.capitaliseSentence(clothing.getClothingType().getBlockedPartsKeysAsListWithoutNONE().get(index - 11).getOppositeDescription()) + " the " + clothing.getName() + ". "
													+ clothing.getClothingBlockingDescription(clothing.getClothingType().getBlockedPartsKeysAsListWithoutNONE().get(index - 11), Main.game.getPlayer(),
															" <span style='color:" + Colour.GENERIC_GOOD.toWebHexString() + ";'>This will cover your ", ".</span>"),
													Combat.ENEMY_ATTACK){
										@Override
										public void effects(){
											Main.game.getPlayer().isAbleToBeReplaced(clothing, clothing.getClothingType().getBlockedPartsKeysAsListWithoutNONE().get(index - 11), true, true, Main.game.getPlayer());
											Combat.setPlayerTurnText(owner.getReplaceDescription());
											Combat.attackEnemy();
											Combat.setPreviousAction(Attack.NONE);
											Main.mainController.openInventory();
										}
									};
								
								} else {
									return new Response(Util.capitaliseSentence(clothing.getClothingType().getBlockedPartsKeysAsListWithoutNONE().get(index -11).getDescription()),
											"You can't "+clothing.getClothingType().getBlockedPartsKeysAsListWithoutNONE().get(index -11).getDescription() + " the " + clothing.getName() + ", as other clothing is in the way!", null);
								}
								
							} else {
								
								if(owner.isAbleToBeDisplaced(clothing, clothing.getClothingType().getBlockedPartsKeysAsListWithoutNONE().get(index -11), false, false, Main.game.getPlayer())){
								
									return new Response(Util.capitaliseSentence(clothing.getClothingType().getBlockedPartsKeysAsListWithoutNONE().get(index - 11).getDescription()),
											Util.capitaliseSentence(clothing.getClothingType().getBlockedPartsKeysAsListWithoutNONE().get(index - 11).getDescription()) + " the " + clothing.getName() + ". "
													+ clothing.getClothingBlockingDescription(clothing.getClothingType().getBlockedPartsKeysAsListWithoutNONE().get(index - 11), Main.game.getPlayer(),
															" <span style='color:" + Colour.GENERIC_SEX.toWebHexString() + ";'>This will expose your ", ".</span>"),
													Combat.ENEMY_ATTACK){
										@Override
										public void effects(){
											Main.game.getPlayer().isAbleToBeDisplaced(clothing, clothing.getClothingType().getBlockedPartsKeysAsListWithoutNONE().get(index - 11), true, true, Main.game.getPlayer());
											Combat.setPlayerTurnText(owner.getDisplaceDescription());
											Combat.attackEnemy();
											Combat.setPreviousAction(Attack.NONE);
											Main.mainController.openInventory();
										}
									};
									
								} else {
									return new Response(Util.capitaliseSentence(clothing.getClothingType().getBlockedPartsKeysAsListWithoutNONE().get(index -11).getDescription()),
											"You can't "+clothing.getClothingType().getBlockedPartsKeysAsListWithoutNONE().get(index -11).getDescription() + " the " + clothing.getName() + ", as other clothing is in the way!", null);
								}
							}
							
						} else {
							
							return null;
						}
						
					case FULL_MANAGEMENT: case TRADING:
						if (index == 1) {
							boolean areaFull = Main.game.isPlayerTileFull() && !Main.game.getCurrentCell().getInventory().hasClothing(clothing);
							if(Main.game.getPlayer().getLocationPlace().isItemsDisappear()) {
								if(areaFull && !clothing.getClothingType().isDiscardedOnUnequip()) {
									return new Response("Drop", "This area is full, so you can't drop your " + clothing.getName() + " here!", null);
								} else {
									return new Response((clothing.getClothingType().isDiscardedOnUnequip()?"Discard":"Drop"),
											(clothing.getClothingType().isDiscardedOnUnequip()?"Take off your " + clothing.getName() + " and throw it away.":"Drop your " + clothing.getName() + "."),
											INVENTORY_MENU){
										@Override
										public void effects(){
											if(clothing.getClothingType().isDiscardedOnUnequip()) {
												Main.game.getTextStartStringBuilder().append("<p style='text-align:center;'>" + Main.game.getPlayer().unequipClothingIntoVoid(clothing, true, Main.game.getPlayer()) + "</p>");
											} else {
												Main.game.getTextStartStringBuilder().append("<p style='text-align:center;'>" + Main.game.getPlayer().unequipClothingOntoFloor(clothing, true, Main.game.getPlayer()) + "</p>");
											}
											populateJinxedClothingList();
										}
									};
								}
								
							} else {
								if(areaFull && !clothing.getClothingType().isDiscardedOnUnequip()) {
									return new Response("Store", "This area is full, so you can't store your " + clothing.getName() + " here!", null);
								} else {
									return new Response((clothing.getClothingType().isDiscardedOnUnequip()?"Discard":"Store"),
											(clothing.getClothingType().isDiscardedOnUnequip()?"Take off your " + clothing.getName() + " and throw it away.":"Store your " + clothing.getName() + " in this area."),
											INVENTORY_MENU){
										@Override
										public void effects(){
											if(clothing.getClothingType().isDiscardedOnUnequip()) {
												Main.game.getTextStartStringBuilder().append("<p style='text-align:center;'>" + Main.game.getPlayer().unequipClothingIntoVoid(clothing, true, Main.game.getPlayer()) + "</p>");
											} else {
												Main.game.getTextStartStringBuilder().append("<p style='text-align:center;'>" + Main.game.getPlayer().unequipClothingOntoFloor(clothing, true, Main.game.getPlayer()) + "</p>");
											}
											populateJinxedClothingList();
										}
									};
								}
							}
							
						} else if (index==4) {
							if (Main.game.getPlayer().hasItemType(ItemType.DYE_BRUSH)) {
								return new Response("Dye", "Use a dye-brush to dye this item of clothing.", DYE_EQUIPPED_CLOTHING);
							} else {
								return new Response("Dye", "You need a dye-brush in order to dye this item of clothing.", null);
							}
							
						} else if(index == 5) {
							return new Response("Enchant", "You can't enchant equipped clothing!", null);
							
						} else if(index == 6 && !clothing.getClothingType().isDiscardedOnUnequip()) {
							return new Response("Unequip", "Unequip the " + clothing.getName() + ".", INVENTORY_MENU){
								@Override
								public void effects(){
									Main.game.getTextStartStringBuilder().append("<p style='text-align:center;'>" + Main.game.getPlayer().unequipClothingIntoInventory(clothing, true, Main.game.getPlayer()) + "</p>");
									populateJinxedClothingList();
								}
							};
							
						} else if (index == 10) {
							return getQuickTradeResponse();
							
						} else if (index > 10 && index - 11 < clothing.getClothingType().getBlockedPartsKeysAsListWithoutNONE().size()){
							
							if (clothing.getDisplacedList().contains(clothing.getClothingType().getBlockedPartsKeysAsListWithoutNONE().get(index - 11))) {
								return new Response(Util.capitaliseSentence(clothing.getClothingType().getBlockedPartsKeysAsListWithoutNONE().get(index - 11).getOppositeDescription()),
										Util.capitaliseSentence(clothing.getClothingType().getBlockedPartsKeysAsListWithoutNONE().get(index - 11).getOppositeDescription()) + " the " + clothing.getName() + ". "
												+ clothing.getClothingBlockingDescription(clothing.getClothingType().getBlockedPartsKeysAsListWithoutNONE().get(index - 11), Main.game.getPlayer(),
														" <span style='color:" + Colour.GENERIC_GOOD.toWebHexString() + ";'>This will cover your ", ".</span>"),
												CLOTHING_EQUIPPED){
									@Override
									public void effects(){
										Main.game.getPlayer().isAbleToBeReplaced(clothing, clothing.getClothingType().getBlockedPartsKeysAsListWithoutNONE().get(index - 11), true, true, Main.game.getPlayer());
									}
								};
							} else {
								return new Response(Util.capitaliseSentence(clothing.getClothingType().getBlockedPartsKeysAsListWithoutNONE().get(index - 11).getDescription()),
										Util.capitaliseSentence(clothing.getClothingType().getBlockedPartsKeysAsListWithoutNONE().get(index - 11).getDescription()) + " the " + clothing.getName() + ". "
												+ clothing.getClothingBlockingDescription(clothing.getClothingType().getBlockedPartsKeysAsListWithoutNONE().get(index - 11), Main.game.getPlayer(),
														" <span style='color:" + Colour.GENERIC_SEX.toWebHexString() + ";'>This will expose your ", ".</span>"),
												CLOTHING_EQUIPPED){
									@Override
									public void effects(){
										Main.game.getPlayer().isAbleToBeDisplaced(clothing, clothing.getClothingType().getBlockedPartsKeysAsListWithoutNONE().get(index - 11), true, true, Main.game.getPlayer());
									}
								};
							}
							
						} else {
							return null;
						}
						
					case CHARACTER_CREATION:
						if (index == 1) {
							if(Main.game.getPlayer().isCoverableAreaExposed(CoverableArea.NIPPLES)
									|| Main.game.getPlayer().isCoverableAreaExposed(CoverableArea.ANUS)
									|| Main.game.getPlayer().isCoverableAreaExposed(CoverableArea.PENIS)
									|| Main.game.getPlayer().isCoverableAreaExposed(CoverableArea.VAGINA)
									|| Main.game.getPlayer().getClothingInSlot(InventorySlot.FOOT)==null) {
								return new Response("To the stage", "You need to be wearing clothing that covers your body, as well as a pair of shoes.", null);
								
							} else {
								return new Response("To the stage", "You're ready to approach the stage now.", CharacterCreation.CHOOSE_BACKGROUND) {
									@Override
									public void effects() {
										CharacterCreation.moveNPCIntoPlayerTile();
									}
								};
							}
							
						} else if(index == 2) {
							return new Response("Unequip", "Unequip the " + clothing.getName() + ".", INVENTORY_MENU){
								@Override
								public void effects(){
									Main.game.getPlayer().unequipClothingOntoFloor(clothing, true, Main.game.getPlayer());
									populateJinxedClothingList();
								}
							};
								
						} else if(index == 3) {
							return new Response("Change Colour", "Change the colour of this item of clothing.", DYE_EQUIPPED_CLOTHING_CHARACTER_CREATION);
						} else {
							return null;
						}
						
					case SEX:
						if (index == 1) {
							boolean areaFull = Main.game.isPlayerTileFull() && !Main.game.getCurrentCell().getInventory().hasClothing(clothing);
							if(Main.game.getPlayer().getLocationPlace().isItemsDisappear()) {
								if(areaFull && !clothing.getClothingType().isDiscardedOnUnequip()) {
									return new Response("Drop", "This area is full, so you can't drop your " + clothing.getName() + " here!", null);
								} else {
									if (owner.isAbleToUnequip(clothing, false, Main.game.getPlayer())) {
										return new Response((clothing.getClothingType().isDiscardedOnUnequip()?"Discard":"Drop"),
												(clothing.getClothingType().isDiscardedOnUnequip()?"Take off your " + clothing.getName() + " and throw it away.":"Drop your " + clothing.getName() + "."),
												Sex.SEX_DIALOGUE){
											@Override
											public void effects(){
												if(clothing.getClothingType().isDiscardedOnUnequip()) {
													owner.unequipClothingIntoVoid(clothing, true, Main.game.getPlayer());
												} else {
													owner.unequipClothingOntoFloor(clothing, true, Main.game.getPlayer());
												}
												Sex.setUnequipClothingText(owner.getUnequipDescription());
												Main.mainController.openInventory();
												Sex.endSexTurn(SexActionUtility.CLOTHING_REMOVAL);
												Sex.setSexStarted(true);
											}
										};
									} else {
										return new Response("Drop", "You can't unequip the " + clothing.getName() + ", as other clothing is blocking you from doing so!", null);
									}
								}
								
							} else {
								if(areaFull && !clothing.getClothingType().isDiscardedOnUnequip()) {
									return new Response("Store", "This area is full, so you can't store your " + clothing.getName() + " here!", null);
								} else {
									if (owner.isAbleToUnequip(clothing, false, Main.game.getPlayer())) {
										return new Response((clothing.getClothingType().isDiscardedOnUnequip()?"Discard":"Store"),
												(clothing.getClothingType().isDiscardedOnUnequip()?"Take off your " + clothing.getName() + " and throw it away.":"Drop your " + clothing.getName() + "."),
												Sex.SEX_DIALOGUE){
											@Override
											public void effects(){
												if(clothing.getClothingType().isDiscardedOnUnequip()) {
													owner.unequipClothingIntoVoid(clothing, true, Main.game.getPlayer());
												} else {
													owner.unequipClothingOntoFloor(clothing, true, Main.game.getPlayer());
												}
												Sex.setUnequipClothingText(owner.getUnequipDescription());
												Main.mainController.openInventory();
												Sex.endSexTurn(SexActionUtility.CLOTHING_REMOVAL);
												Sex.setSexStarted(true);
											}
										};
									} else {
										return new Response("Store", "You can't unequip the " + clothing.getName() + ", as other clothing is blocking you from doing so!", null);
									}
								}
							}
							
						} else if (index==4) {
							return new Response("Dye", "You can't dye your clothes in sex!", null);
							
						} else if(index == 5) {
							return new Response("Enchant", "You can't enchant equipped clothing!", null);
							
						} else if(index == 6 && !clothing.getClothingType().isDiscardedOnUnequip()) {
							if(!Sex.getSexManager().isPlayerCanRemoveOwnClothes()) {
								return new Response("Unequip", "You can't unequip the " + clothing.getName() + " in this sex scene!", null);
							}
							
							if (owner.isAbleToUnequip(clothing, false, Main.game.getPlayer())) {
								return new Response("Unequip", "Unequip the " + clothing.getName() + ".", Sex.SEX_DIALOGUE){
									@Override
									public void effects(){
										owner.unequipClothingIntoInventory(clothing, true, Main.game.getPlayer());
										Sex.setUnequipClothingText(owner.getUnequipDescription());
										Main.mainController.openInventory();
										Sex.endSexTurn(SexActionUtility.CLOTHING_REMOVAL);
										Sex.setSexStarted(true);
									}
								};
							} else {
								return new Response("Unequip", "You can't unequip the " + clothing.getName() + ", as other clothing is blocking you from doing so!", null);
							}
							
						} else if (index == 10) {
							return getQuickTradeResponse();
							
						} else if (index > 10 && index - 11 < clothing.getClothingType().getBlockedPartsKeysAsListWithoutNONE().size()){
							
							if (clothing.getDisplacedList().contains(clothing.getClothingType().getBlockedPartsKeysAsListWithoutNONE().get(index - 11))) {
								return new Response(Util.capitaliseSentence(clothing.getClothingType().getBlockedPartsKeysAsListWithoutNONE().get(index -11).getDescription()),
										"You can't "+clothing.getClothingType().getBlockedPartsKeysAsListWithoutNONE().get(index -11).getDescription() + " your " + clothing.getName() + " during sex!", null);
								
							} else {
								
								if(!Sex.getSexManager().isPlayerCanRemoveOwnClothes()) {
									return new Response(Util.capitaliseSentence(clothing.getClothingType().getBlockedPartsKeysAsListWithoutNONE().get(index - 11).getDescription()),
											"You can't can't "+clothing.getClothingType().getBlockedPartsKeysAsListWithoutNONE().get(index -11).getDescription() + " your " + clothing.getName() + " in this sex scene!", null);
								}
								
								if(owner.isAbleToBeDisplaced(clothing, clothing.getClothingType().getBlockedPartsKeysAsListWithoutNONE().get(index -11), false, false, Main.game.getPlayer())){
									
									return new Response(Util.capitaliseSentence(clothing.getClothingType().getBlockedPartsKeysAsListWithoutNONE().get(index - 11).getDescription()),
											Util.capitaliseSentence(clothing.getClothingType().getBlockedPartsKeysAsListWithoutNONE().get(index - 11).getDescription()) + " the " + clothing.getName() + ". "
													+ clothing.getClothingBlockingDescription(clothing.getClothingType().getBlockedPartsKeysAsListWithoutNONE().get(index - 11), Main.game.getPlayer(),
															" <span style='color:" + Colour.GENERIC_SEX.toWebHexString() + ";'>This will expose your ", ".</span>"),
													Sex.SEX_DIALOGUE){
										@Override
										public void effects(){
											Main.game.getPlayer().isAbleToBeDisplaced(clothing, clothing.getClothingType().getBlockedPartsKeysAsListWithoutNONE().get(index - 11), true, true, Main.game.getPlayer());
											Sex.setUnequipClothingText(owner.getDisplaceDescription());
											Main.mainController.openInventory();
											Sex.endSexTurn(SexActionUtility.CLOTHING_REMOVAL);
											Sex.setSexStarted(true);
										}
									};
								
								} else {
									return new Response(Util.capitaliseSentence(clothing.getClothingType().getBlockedPartsKeysAsListWithoutNONE().get(index -11).getDescription()),
											"You can't "+clothing.getClothingType().getBlockedPartsKeysAsListWithoutNONE().get(index -11).getDescription() + " the " + clothing.getName() + ", as other clothing is in the way!", null);
								}
							}
							
						} else {
							return null;
						}
				}
				
			// ****************************** ITEM DOES NOT BELONG TO PLAYER ******************************
				
			} else {
				switch(interactionType) {
					case COMBAT:
						if (index == 1) {
							return new Response("Drop", "You can't make someone drop their clothing while fighting them!", null);
							
						} else if (index==4) {
							return new Response("Dye", "You can't dye someone else's equipped clothing while you're fighting them!", null);
							
						} else if(index == 5) {
							return new Response("Enchant", "You can't enchant someone else's equipped clothing, especially not while fighting them!", null);
							
						} else if(index == 6) {
							return new Response("Unequip", "You can't unequip someone's clothing while fighting them!", null);
							
						} else if (index == 10) {
							return getQuickTradeResponse();
							
						} else if (index > 10 && index - 11 < clothing.getClothingType().getBlockedPartsKeysAsListWithoutNONE().size()){
							
							if (clothing.getDisplacedList().contains(clothing.getClothingType().getBlockedPartsKeysAsListWithoutNONE().get(index - 11))) {
								return new Response(Util.capitaliseSentence(clothing.getClothingType().getBlockedPartsKeysAsListWithoutNONE().get(index -11).getDescription()),
											"You can't "+clothing.getClothingType().getBlockedPartsKeysAsListWithoutNONE().get(index -11).getDescription() + " the " + clothing.getName() + " while in a fight!", null);
								
							} else {
								return new Response(Util.capitaliseSentence(clothing.getClothingType().getBlockedPartsKeysAsListWithoutNONE().get(index -11).getDescription()),
										"You can't "+clothing.getClothingType().getBlockedPartsKeysAsListWithoutNONE().get(index -11).getDescription() + " the " + clothing.getName() + " while in a fight!", null);
							}
							
						} else {
							return null;
						}
						
					case FULL_MANAGEMENT: case CHARACTER_CREATION:
						if (index == 1) {
							boolean areaFull = Main.game.isPlayerTileFull() && !Main.game.getCurrentCell().getInventory().hasClothing(clothing);
							if(Main.game.getPlayer().getLocationPlace().isItemsDisappear()) {
								if(areaFull && !clothing.getClothingType().isDiscardedOnUnequip()) {
									return new Response("Drop", UtilText.parse(inventoryNPC, "This area is full, so you can't drop [npc.name]'s " + clothing.getName() + " here!"), null);
								} else {
									return new Response((clothing.getClothingType().isDiscardedOnUnequip()?"Discard":"Drop"),
											(clothing.getClothingType().isDiscardedOnUnequip()
													?UtilText.parse(inventoryNPC, "Take off [npc.name]'s " + clothing.getName() + " and throw it away..")
													:UtilText.parse(inventoryNPC, "Drop [npc.name]'s " + clothing.getName() + ".")),
											INVENTORY_MENU){
										@Override
										public void effects(){
											if(clothing.getClothingType().isDiscardedOnUnequip()) {
												Main.game.getTextStartStringBuilder().append("<p style='text-align:center;'>" + inventoryNPC.unequipClothingIntoVoid(clothing, true, Main.game.getPlayer()) + "</p>");
											} else {
												Main.game.getTextStartStringBuilder().append("<p style='text-align:center;'>" + inventoryNPC.unequipClothingOntoFloor(clothing, true, Main.game.getPlayer()) + "</p>");
											}
											populateJinxedClothingList();
										}
									};
								}
								
							} else {
								if(areaFull && !clothing.getClothingType().isDiscardedOnUnequip()) {
									return new Response("Store", UtilText.parse(inventoryNPC, "This area is full, so you can't store [npc.name]'s " + clothing.getName() + " here!"), null);
								} else {
									return new Response((clothing.getClothingType().isDiscardedOnUnequip()?"Discard":"Store"),
											(clothing.getClothingType().isDiscardedOnUnequip()
													?UtilText.parse(inventoryNPC, "Take off [npc.name]'s " + clothing.getName() + " and throw it away..")
													:UtilText.parse(inventoryNPC, "Store [npc.name]'s " + clothing.getName() + " in this area.")),
											INVENTORY_MENU){
										@Override
										public void effects(){
											if(clothing.getClothingType().isDiscardedOnUnequip()) {
												Main.game.getTextStartStringBuilder().append("<p style='text-align:center;'>" + inventoryNPC.unequipClothingIntoVoid(clothing, true, Main.game.getPlayer()) + "</p>");
											} else {
												Main.game.getTextStartStringBuilder().append("<p style='text-align:center;'>" + inventoryNPC.unequipClothingOntoFloor(clothing, true, Main.game.getPlayer()) + "</p>");
											}
											populateJinxedClothingList();
										}
									};
								}
							}
							
						} else if (index==4) {
							if (Main.game.getPlayer().hasItemType(ItemType.DYE_BRUSH)) {
								return new Response("Dye", "Use a dye-brush to dye this item of clothing.", DYE_EQUIPPED_CLOTHING);
								
							} else {
								return new Response("Dye", UtilText.parse(inventoryNPC, "You'll need to find a dye-brush if you want to dye [npc.name]'s clothes."), null);
							}
							
						} else if(index == 5) {
							return new Response("Enchant", "You can't enchant equipped clothing!", null);
							
						} else if(index == 6 && !clothing.getClothingType().isDiscardedOnUnequip()) {
							return new Response("Unequip", "Unequip the " + clothing.getName() + ".", INVENTORY_MENU){
								@Override
								public void effects(){
									Main.game.getTextStartStringBuilder().append("<p style='text-align:center;'>" + inventoryNPC.unequipClothingIntoInventory(clothing, true, Main.game.getPlayer()) + "</p>");
									populateJinxedClothingList();
								}
							};
							
						} else if (index == 10) {
							return getQuickTradeResponse();
							
						} else if (index > 10 && index - 11 < clothing.getClothingType().getBlockedPartsKeysAsListWithoutNONE().size()){
							
							if (clothing.getDisplacedList().contains(clothing.getClothingType().getBlockedPartsKeysAsListWithoutNONE().get(index - 11))) {
								return new Response(Util.capitaliseSentence(clothing.getClothingType().getBlockedPartsKeysAsListWithoutNONE().get(index - 11).getOppositeDescription()),
										Util.capitaliseSentence(clothing.getClothingType().getBlockedPartsKeysAsListWithoutNONE().get(index - 11).getOppositeDescription()) + " the " + clothing.getName() + ". "
												+ clothing.getClothingBlockingDescription(clothing.getClothingType().getBlockedPartsKeysAsListWithoutNONE().get(index - 11), owner,
														" <span style='color:" + Colour.GENERIC_GOOD.toWebHexString() + ";'>This will cover your ", ".</span>"),
												CLOTHING_EQUIPPED){
									@Override
									public void effects(){
										owner.isAbleToBeReplaced(clothing, clothing.getClothingType().getBlockedPartsKeysAsListWithoutNONE().get(index - 11), true, true, Main.game.getPlayer());
									}
								};
							} else {
								return new Response(Util.capitaliseSentence(clothing.getClothingType().getBlockedPartsKeysAsListWithoutNONE().get(index - 11).getDescription()),
										Util.capitaliseSentence(clothing.getClothingType().getBlockedPartsKeysAsListWithoutNONE().get(index - 11).getDescription()) + " the " + clothing.getName() + ". "
												+ clothing.getClothingBlockingDescription(clothing.getClothingType().getBlockedPartsKeysAsListWithoutNONE().get(index - 11), owner,
														" <span style='color:" + Colour.GENERIC_SEX.toWebHexString() + ";'>This will expose your ", ".</span>"),
												CLOTHING_EQUIPPED){
									@Override
									public void effects(){
										owner.isAbleToBeDisplaced(clothing, clothing.getClothingType().getBlockedPartsKeysAsListWithoutNONE().get(index - 11), true, true, Main.game.getPlayer());
									}
								};
							}
							
						} else {
							return null;
						}
						
					case SEX:
						if (index == 1) {
							boolean areaFull = Main.game.isPlayerTileFull() && !Main.game.getCurrentCell().getInventory().hasClothing(clothing);
							if(Main.game.getPlayer().getLocationPlace().isItemsDisappear()) {
								if(areaFull && !clothing.getClothingType().isDiscardedOnUnequip()) {
									return new Response("Drop", UtilText.parse(inventoryNPC, "This area is full, so you can't drop [npc.name]'s " + clothing.getName() + " here!"), null);
									
								} else {
									if (owner.isAbleToUnequip(clothing, false, Main.game.getPlayer())) {
										return new Response((clothing.getClothingType().isDiscardedOnUnequip()?"Discard":"Drop"),
											(clothing.getClothingType().isDiscardedOnUnequip()
													?UtilText.parse(inventoryNPC, "Take off [npc.name]'s " + clothing.getName() + " and throw it away.")
													:UtilText.parse(inventoryNPC, "Drop [npc.name]'s " + clothing.getName() + ".")),
												Sex.SEX_DIALOGUE){
											@Override
											public void effects(){
												if(clothing.getClothingType().isDiscardedOnUnequip()) {
													inventoryNPC.unequipClothingIntoVoid(clothing, true, Main.game.getPlayer());
												} else {
													inventoryNPC.unequipClothingOntoFloor(clothing, true, Main.game.getPlayer());
												}
												Sex.setUnequipClothingText(inventoryNPC.getUnequipDescription());
												Main.mainController.openInventory();
												Sex.endSexTurn(SexActionUtility.CLOTHING_REMOVAL);
												Sex.setSexStarted(true);
											}
										};
									} else {
										return new Response("Drop", "You can't unequip the " + clothing.getName() + ", as other clothing is blocking you from doing so!", null);
									}
								}
								
							} else {
								if(areaFull && !clothing.getClothingType().isDiscardedOnUnequip()) {
									return new Response("Store", UtilText.parse(inventoryNPC, "This area is full, so you can't store [npc.name]'s " + clothing.getName() + " here!"), null);
								} else {
									if (owner.isAbleToUnequip(clothing, false, Main.game.getPlayer())) {
										return new Response((clothing.getClothingType().isDiscardedOnUnequip()?"Discard":"Store"),
												(clothing.getClothingType().isDiscardedOnUnequip()
														?UtilText.parse(inventoryNPC, "Take off [npc.name]'s " + clothing.getName() + " and throw it away.")
														:UtilText.parse(inventoryNPC, "Store [npc.name]'s " + clothing.getName() + " in this area.")),
												Sex.SEX_DIALOGUE){
											@Override
											public void effects(){
												if(clothing.getClothingType().isDiscardedOnUnequip()) {
													inventoryNPC.unequipClothingIntoVoid(clothing, true, Main.game.getPlayer());
												} else {
													inventoryNPC.unequipClothingOntoFloor(clothing, true, Main.game.getPlayer());
												}
												Sex.setUnequipClothingText(inventoryNPC.getUnequipDescription());
												Main.mainController.openInventory();
												Sex.endSexTurn(SexActionUtility.CLOTHING_REMOVAL);
												Sex.setSexStarted(true);
											}
										};
									} else {
										return new Response("Store", "You can't unequip the " + clothing.getName() + ", as other clothing is blocking you from doing so!", null);
									}
								}
							}
							
						} else if (index==4) {
							return new Response("Dye", UtilText.parse(inventoryNPC, "You can't dye [npc.name]'s clothes in sex!"), null);
							
						} else if(index == 5) {
							return new Response("Enchant", "You can't enchant equipped clothing!", null);
							
						} else if(index == 6 && !clothing.getClothingType().isDiscardedOnUnequip()) {
							if(!Sex.getSexManager().isPlayerCanRemovePartnersClothes()) {
								return new Response("Unequip", "You can't unequip the " + clothing.getName() + " in this sex scene!", null);
							}
							
							if (owner.isAbleToUnequip(clothing, false, Main.game.getPlayer())) {
								return new Response("Unequip", "Unequip the " + clothing.getName() + ".", Sex.SEX_DIALOGUE){
									@Override
									public void effects(){
										inventoryNPC.unequipClothingIntoInventory(clothing, true, Main.game.getPlayer());
										Sex.setUnequipClothingText(inventoryNPC.getUnequipDescription());
										Main.mainController.openInventory();
										Sex.endSexTurn(SexActionUtility.CLOTHING_REMOVAL);
										Sex.setSexStarted(true);
									}
								};
							} else {
								return new Response("Unequip", "You can't unequip the " + clothing.getName() + ", as other clothing is blocking you from doing so!", null);
							}
							
						} else if (index == 10) {
							return getQuickTradeResponse();
							
						} else if (index > 10 && index - 11 < clothing.getClothingType().getBlockedPartsKeysAsListWithoutNONE().size()){
							
							if (clothing.getDisplacedList().contains(clothing.getClothingType().getBlockedPartsKeysAsListWithoutNONE().get(index - 11))) {

								return new Response(Util.capitaliseSentence(clothing.getClothingType().getBlockedPartsKeysAsListWithoutNONE().get(index -11).getDescription()),
										"You can't "+clothing.getClothingType().getBlockedPartsKeysAsListWithoutNONE().get(index -11).getDescription() + " the " + clothing.getName() + " during sex!", null);
								
							} else {

								if(owner.isAbleToBeDisplaced(clothing, clothing.getClothingType().getBlockedPartsKeysAsListWithoutNONE().get(index -11), false, false, Main.game.getPlayer())){
									

									if(!Sex.getSexManager().isPlayerCanRemovePartnersClothes()) {
										return new Response(Util.capitaliseSentence(clothing.getClothingType().getBlockedPartsKeysAsListWithoutNONE().get(index - 11).getDescription()),
												"You "+clothing.getClothingType().getBlockedPartsKeysAsListWithoutNONE().get(index -11).getDescription() + " the " + clothing.getName() + " in this sex scene!", null);
									}
									
									return new Response(Util.capitaliseSentence(clothing.getClothingType().getBlockedPartsKeysAsListWithoutNONE().get(index - 11).getDescription()),
											Util.capitaliseSentence(clothing.getClothingType().getBlockedPartsKeysAsListWithoutNONE().get(index - 11).getDescription()) + " the " + clothing.getName() + ". "
													+ clothing.getClothingBlockingDescription(clothing.getClothingType().getBlockedPartsKeysAsListWithoutNONE().get(index - 11), owner,
															" <span style='color:" + Colour.GENERIC_SEX.toWebHexString() + ";'>This will expose your ", ".</span>"),
													Sex.SEX_DIALOGUE){
										@Override
										public void effects(){
											owner.isAbleToBeDisplaced(clothing, clothing.getClothingType().getBlockedPartsKeysAsListWithoutNONE().get(index - 11), true, true, Main.game.getPlayer());
											Sex.setUnequipClothingText(owner.getDisplaceDescription());
											Main.mainController.openInventory();
											Sex.endSexTurn(SexActionUtility.CLOTHING_REMOVAL);
											Sex.setSexStarted(true);
										}
									};
								
								} else {
									return new Response(Util.capitaliseSentence(clothing.getClothingType().getBlockedPartsKeysAsListWithoutNONE().get(index -11).getDescription()),
											"You can't "+clothing.getClothingType().getBlockedPartsKeysAsListWithoutNONE().get(index -11).getDescription() + " the " + clothing.getName() + ", as other clothing is in the way!", null);
								}
							}
							
						} else {
							return null;
						}
						
						case TRADING:
							if (index == 1) {
								return new Response("Drop", UtilText.parse("You can't make [npc.name] drop [npc.her] clothing!"), null);
								
							} else if (index==4) {
								return new Response("Dye", UtilText.parse(inventoryNPC, "You can't dye [npc.name]'s clothes!"), null);
								
							}  else if(index == 5) {
								return new Response("Enchant", UtilText.parse("You can't enchant [npc.name]'s clothing!"), null);
								
							} else if(index == 6) {
								return new Response("Unequip", UtilText.parse("You can't unequip [npc.name]'s clothing!"), null);
								
							} else if (index == 10) {
								return getQuickTradeResponse();
								
							} else if (index > 10 && index - 11 < clothing.getClothingType().getBlockedPartsKeysAsListWithoutNONE().size()){
								
								if (clothing.getDisplacedList().contains(clothing.getClothingType().getBlockedPartsKeysAsListWithoutNONE().get(index - 11))) {
									return new Response(Util.capitaliseSentence(clothing.getClothingType().getBlockedPartsKeysAsListWithoutNONE().get(index -11).getDescription()),
												"You can't "+clothing.getClothingType().getBlockedPartsKeysAsListWithoutNONE().get(index -11).getDescription() + " the " + clothing.getName() + "!", null);
									
								} else {
									return new Response(Util.capitaliseSentence(clothing.getClothingType().getBlockedPartsKeysAsListWithoutNONE().get(index -11).getDescription()),
											"You can't "+clothing.getClothingType().getBlockedPartsKeysAsListWithoutNONE().get(index -11).getDescription() + " the " + clothing.getName() + "!", null);
								}
								
							} else {
								return null;
							}
					}
				
				}
				return null;
		}

		@Override
		public MapDisplay getMapDisplay() {
			return MapDisplay.INVENTORY;
		}
	};

	public static String jinxedClothingView() { //TODO
		inventorySB = new StringBuilder("");

		inventorySB.append("<p style='text-align: center;'>" + "<b>Jinxed clothing:</b>"
				+ "</p>");

		inventorySB.append("<div class='inventory-not-equipped'>");

		if (!jinxedClothing.isEmpty()) {
			for (int i = 0; i < jinxedClothing.size(); i++) {
				inventorySB.append(
						"<div class='inventory-item-slot'>"
							+ "<div class='inventory-icon-content'>"
								+ jinxedClothing.get(i).getSVGString()
							+ "</div>"
							+"<div class='overlay' id='JINXED_" + i + "'></div>"
						+ "</div>");
			}
		}
		inventorySB.append("</div>");
		
		if(inventoryNPC!=null) {
			inventorySB.append("<p style='text-align: center;'>" + "<b>[npc.Name]'s jinxed clothing:</b>"
					+ "</p>");

			inventorySB.append("<div class='inventory-not-equipped'>");

			if (!jinxedNPCClothing.isEmpty()) {
				for (int i = 0; i < jinxedNPCClothing.size(); i++) {
					inventorySB.append(
							"<div class='inventory-item-slot'>"
								+ "<div class='inventory-icon-content'>"
									+ jinxedNPCClothing.get(i).getSVGString()
								+ "</div>"
								+"<div class='overlay' id='JINXED_NPC_" + i + "'></div>"
							+ "</div>");
				}
			}
			inventorySB.append("</div>");
		}

		return inventorySB.toString();
	}

	public static void populateJinxedClothingList() {
		jinxedClothing.clear();
		for (AbstractClothing c : Main.game.getPlayer().getClothingCurrentlyEquipped()) {
			if (c.isSealed() || c.isBadEnchantment()) {
				jinxedClothing.add(c);
			}
		}
		jinxedNPCClothing.clear();
		if(inventoryNPC!=null) {
			for (AbstractClothing c : inventoryNPC.getClothingCurrentlyEquipped()) {
				if (c.isSealed() || c.isBadEnchantment()) {
					jinxedNPCClothing.add(c);
				}
			}
		}
	}

	public static final DialogueNodeOld REMOVE_JINX = new DialogueNodeOld("Choose a jinxed item", "", true) {
		/**
		 */
		private static final long serialVersionUID = 1L;
		
		@Override
		public String getHeaderContent() {
			return jinxedClothingView();
		}
		
		@Override
		public String getContent() {
			return "<p>Choose an item to remove its jinx.</p>"
					+ "<p><b style='color:" + Colour.GENERIC_TERRIBLE.toWebHexString() + ";'>The " + (jinxRemovalFromFloor ? weaponFloor.getName() : weapon.getName())
					+ " will be lost if you do this!</b></p>";
		}

		@Override
		public Response getResponse(int index) {
			if (index == 0) {
				return new Response("Back", "Return to the previous menu.", INVENTORY_MENU);
			} else {
				return null;
			}
		}

		@Override
		public MapDisplay getMapDisplay() {
			return MapDisplay.INVENTORY;
		}
	};

	public static final DialogueNodeOld DYE_CLOTHING = new DialogueNodeOld("Dye clothing", "", true) {
		private static final long serialVersionUID = 1L;

		@Override
		public String getContent() {
			inventorySB = new StringBuilder(
					"<div class='inventoryImage'>"
							+ "<div class='inventoryImage-content'>"
								+ clothing.getSVGString()
							+ "</div>"
						+ "</div>"
						+ "<p><b>"+clothing.getDisplayName(true)+"</b></p>"
						+ clothing.getDescription()
						+ clothing.clothingExtraInformation(null)
						+ "<p>"
							+ "Available colours for this item (hover over to view preview):"
						+ "</p>");

			for (Colour c : clothing.getClothingType().getAvailableColours()) {
				inventorySB.append("<div class='phone-item-colour' id='" + (clothing.getClothingType().hashCode() + "_" + c.toString()) + "' style='background-color:" + c.toWebHexString() + ";'></div>");
			}
			
			return inventorySB.toString();
		}
		
		@Override
		public Response getResponse(int index) {
			if (index == 0) {
				return new Response("Back", "Return to the previous menu.", INVENTORY_MENU);

			} else if (index - 1 < clothing.getClothingType().getAvailableColours().size()) {
				return new Response("Dye: " + Util.capitaliseSentence(clothing.getClothingType().getAvailableColours().get(index - 1).getName()),
						"Dye the " + clothing.getName() + " " + Util.capitaliseSentence(clothing.getClothingType().getAvailableColours().get(index - 1).getName())
							+ ". This action is permanent, and you'll need another dye-brush if you want to change its colour again.", INVENTORY_MENU){
					@Override
					public void effects(){
						Main.game.getPlayer().useItem(AbstractItemType.generateItem(ItemType.DYE_BRUSH), owner, false);
						Main.game.getTextEndStringBuilder().append(
								"<p style='text-align:center;'>"
									+ ItemType.DYE_BRUSH.getDyeBrushEffects(clothing, clothing.getClothingType().getAvailableColours().get(index - 1))
								+ "</p>"
								+ "<p>"
									+ "<b>Your " + clothing.getName() + " " + (clothing.getClothingType().isPlural() ? "have been" : "has been") + " dyed</b> <b style='color:"
										+ clothing.getClothingType().getAvailableColours().get(index - 1).toWebHexString() + ";'>" + clothing.getClothingType().getAvailableColours().get(index - 1).getName() + "</b>!"
								+ "</p>"
								+ "<p>"
									+ (Main.game.getPlayer().hasItemType(ItemType.DYE_BRUSH)
											?"You have <b>" + Main.game.getPlayer().getMapOfDuplicateItems().get(AbstractItemType.generateItem(ItemType.DYE_BRUSH))
													+ "</b> dye-brush" + (Main.game.getPlayer().getMapOfDuplicateItems().get(AbstractItemType.generateItem(ItemType.DYE_BRUSH)) == 1 ? "" : "es") + " left!"
											:"You have <b>0</b> dye-brushes left!")
								+ "</p>");
						owner.removeClothing(clothing);
						clothing.setColour(clothing.getClothingType().getAvailableColours().get(index - 1));
						owner.addClothing(clothing, false);
					}
				};

			} else
				return null;
		}

		@Override
		public MapDisplay getMapDisplay() {
			return MapDisplay.INVENTORY;
		}
	};

	public static final DialogueNodeOld DYE_EQUIPPED_CLOTHING = new DialogueNodeOld("Dye clothing", "", true) {
		private static final long serialVersionUID = 1L;

		@Override
		public String getContent() {
			inventorySB = new StringBuilder(
					"<div class='inventoryImage'>"
						+ "<div class='inventoryImage-content'>"
							+ clothing.getSVGString()
						+ "</div>"
					+ "</div>"
					+ "<p><b>"+clothing.getDisplayName(true)+"</b></p>"
					+ clothing.getDescription()
					+ clothing.clothingExtraInformation(Main.game.getPlayer())
					+ "<p>"
						+ "Available colours for this item (hover over to view preview):"
					+ "</p>");

			for (Colour c : clothing.getClothingType().getAvailableColours())
				inventorySB.append("<div class='phone-item-colour' id='" + (clothing.getClothingType().hashCode() + "_" + c.toString()) + "' style='background-color:" + c.toWebHexString() + ";'></div>");

			return inventorySB.toString();
		}
		
		@Override
		public Response getResponse(int index) {
			if (index == 0) {
				return new Response("Back", "Return to the previous menu.", INVENTORY_MENU);

			} else if (index - 1 < clothing.getClothingType().getAvailableColours().size()) {
				return new Response("Dye: " + Util.capitaliseSentence(clothing.getClothingType().getAvailableColours().get(index - 1).getName()),
						"Dye the " + clothing.getName() + " " + Util.capitaliseSentence(clothing.getClothingType().getAvailableColours().get(index - 1).getName())
						+ ". This action is permanent, and you'll need another dye-brush if you want to change its colour again.", INVENTORY_MENU){
					@Override
					public void effects(){
						Main.game.getPlayer().useItem(AbstractItemType.generateItem(ItemType.DYE_BRUSH), Main.game.getPlayer(), false);
						Main.game.getTextEndStringBuilder().append(
								"<p style='text-align:center;'>"
									+ ItemType.DYE_BRUSH.getDyeBrushEffects(clothing, clothing.getClothingType().getAvailableColours().get(index - 1))
								+ "</p>"
								+ "<p>"
									+ "<b>Your " + clothing.getName() + " " + (clothing.getClothingType().isPlural() ? "have been" : "has been") + " dyed</b> <b style='color:"
										+ clothing.getClothingType().getAvailableColours().get(index - 1).toWebHexString() + ";'>" + clothing.getClothingType().getAvailableColours().get(index - 1).getName() + "</b>!"
								+ "</p>"
								+ "<p>"
									+ (Main.game.getPlayer().hasItemType(ItemType.DYE_BRUSH)
											?"You have <b>" + Main.game.getPlayer().getMapOfDuplicateItems().get(AbstractItemType.generateItem(ItemType.DYE_BRUSH))
													+ "</b> dye-brush" + (Main.game.getPlayer().getMapOfDuplicateItems().get(AbstractItemType.generateItem(ItemType.DYE_BRUSH)) == 1 ? "" : "es") + " left!"
											:"You have <b>0</b> dye-brushes left!")
								+ "</p>");
						clothing.setColour(clothing.getClothingType().getAvailableColours().get(index - 1));
					}
				};

			} else
				return null;
		}

		@Override
		public MapDisplay getMapDisplay() {
			return MapDisplay.INVENTORY;
		}
	};
	
	
	public static final DialogueNodeOld DYE_CLOTHING_CHARACTER_CREATION = new DialogueNodeOld("Choose Colour", "", true) {
		private static final long serialVersionUID = 1L;

		@Override
		public String getContent() {
			inventorySB = new StringBuilder(
					"<div class='inventoryImage'>"
							+ "<div class='inventoryImage-content'>"
								+ clothing.getSVGString()
							+ "</div>"
						+ "</div>"
						+ "<p><b>"+clothing.getDisplayName(true)+"</b></p>"
						+ clothing.getDescription()
						+ clothing.clothingExtraInformation(null)
						+ "<p>"
							+ "Available colours for this item (hover over to view preview):"
						+ "</p>");

			for (Colour c : clothing.getClothingType().getAvailableColours()) {
				inventorySB.append("<div class='phone-item-colour' id='" + (clothing.getClothingType().hashCode() + "_" + c.toString()) + "' style='background-color:" + c.toWebHexString() + ";'></div>");
			}
			
			return inventorySB.toString();
		}
		
		@Override
		public Response getResponse(int index) {
			if (index == 0) {
				return new Response("Back", "Return to the previous menu.", CLOTHING_INVENTORY);

			} else if (index - 1 < clothing.getClothingType().getAvailableColours().size()) {
				return new Response(Util.capitaliseSentence(clothing.getClothingType().getAvailableColours().get(index - 1).getName()),
						"Change the colour of the " + clothing.getName() + " to " + Util.capitaliseSentence(clothing.getClothingType().getAvailableColours().get(index - 1).getName())+".", CLOTHING_INVENTORY){
					@Override
					public void effects(){
						Main.game.getPlayerCell().getInventory().removeClothing(clothing);
						clothing.setColour(clothing.getClothingType().getAvailableColours().get(index - 1));
						Main.game.getPlayerCell().getInventory().addClothing(clothing);
					}
				};

			} else
				return null;
		}

		@Override
		public MapDisplay getMapDisplay() {
			return MapDisplay.INVENTORY;
		}
	};

	public static final DialogueNodeOld DYE_EQUIPPED_CLOTHING_CHARACTER_CREATION = new DialogueNodeOld("Choose Colour", "", true) {
		private static final long serialVersionUID = 1L;

		@Override
		public String getContent() {
			inventorySB = new StringBuilder(
					"<div class='inventoryImage'>"
							+ "<div class='inventoryImage-content'>"
								+ clothing.getSVGString()
							+ "</div>"
						+ "</div>"
						+ "<p><b>"+clothing.getDisplayName(true)+"</b></p>"
						+ clothing.getDescription()
						+ clothing.clothingExtraInformation(null)
						+ "<p>"
							+ "Available colours for this item (hover over to view preview):"
						+ "</p>");

			for (Colour c : clothing.getClothingType().getAvailableColours()) {
				inventorySB.append("<div class='phone-item-colour' id='" + (clothing.getClothingType().hashCode() + "_" + c.toString()) + "' style='background-color:" + c.toWebHexString() + ";'></div>");
			}
			
			return inventorySB.toString();
		}
		
		@Override
		public Response getResponse(int index) {
			if (index == 0) {
				return new Response("Back", "Return to the previous menu.", CLOTHING_EQUIPPED);

			} else if (index - 1 < clothing.getClothingType().getAvailableColours().size()) {
				return new Response(Util.capitaliseSentence(clothing.getClothingType().getAvailableColours().get(index - 1).getName()),
						"Change the colour of the " + clothing.getName() + " to " + Util.capitaliseSentence(clothing.getClothingType().getAvailableColours().get(index - 1).getName())+".", CLOTHING_EQUIPPED){
					@Override
					public void effects(){
						clothing.setColour(clothing.getClothingType().getAvailableColours().get(index - 1));
					}
				};

			} else
				return null;
		}

		@Override
		public MapDisplay getMapDisplay() {
			return MapDisplay.INVENTORY;
		}
	};

	// Utility methods:
	
	private static String getItemDisplayPanel(String SVGString, String title, String description) {
		return "<div class='inventoryImage'>"
				+ "<div class='inventoryImage-content'>"
					+ SVGString
				+ "</div>"
			+ "</div>"
			+ "<h4><b>"+title+"</b></h4>"
			+ description;
	}
	
	private static Response getCloseInventoryResponse() {
		if(interactionType == InventoryInteraction.CHARACTER_CREATION) {
			return new Response("Back", "Return to looking in the mirror at your appearance.", CharacterCreation.CHOOSE_ADVANCED_APPEARANCE);
			
		} else {
			return new ResponseEffectsOnly("Close Inventory", "Close the Inventory menu."){
				@Override
				public void effects(){
					Main.mainController.openInventory();
				}
			};
		}
	}
	
	private static Response getReturnToInventoryMenuResponse() { 
//		return getCloseInventoryResponse();
		return new Response("Back", "Return to the main inventory options.", INVENTORY_MENU);
	}
	
	private static Response getBuybackResponse() {
		if (buyback) {
			return new Response("Normal trade", "Switch back to the normal trade menu.", INVENTORY_MENU){
				@Override
				public void effects(){
					buyback = !buyback;
				}
			};
		} else {
			return new Response("Buyback", "Switch to viewing the buyback menu.", INVENTORY_MENU){
				@Override
				public void effects(){
					buyback = !buyback;
				}
			};
		}
	}
	
	private static Response getQuickTradeResponse() {
		
		return null;
		
//		if (Main.game.getDialogueFlags().quickTrade) {
//			return new Response("Quick-Manage: <b style='color:" + Colour.GENERIC_GOOD.toWebHexString() + ";'>ON</b>",
//					"Quick-Manage is turned <b style='color:" + Colour.GENERIC_GOOD.toWebHexString() + ";'>ON</b>!</br>"
//							+ "That means you can buy and sell items with a single click when trading, and pick-up and drop items with a single click when in normal inventory mode.", INVENTORY_MENU){
//				
//				@Override
//				public DialogueNodeOld getNextDialogue() {
//					return Main.game.getCurrentDialogueNode();
//				}
//				
//				@Override
//				public void effects(){
//					Main.game.getDialogueFlags().quickTrade = !Main.game.getDialogueFlags().quickTrade;
//				}
//			};
//			
//		} else {
//			return new Response("Quick-Manage: <b style='color:" + Colour.TEXT_GREY.toWebHexString() + ";'>OFF</b>",
//					"Quick-Manage is turned <b style='color:" + Colour.TEXT_GREY.toWebHexString() + ";'>OFF</b>.</br>"
//							+ "That means when you click on an item, you get a detailed view of the item before deciding whether to buy/sell or pick-up/drop it.", INVENTORY_MENU){
//
//				@Override
//				public DialogueNodeOld getNextDialogue() {
//					return Main.game.getCurrentDialogueNode();
//				}
//				
//				@Override
//				public void effects(){
//					Main.game.getDialogueFlags().quickTrade = !Main.game.getDialogueFlags().quickTrade;
//				}
//			};
//		}
	}

	// Items:
	
	private static void transferItems(GameCharacter from, GameCharacter to, AbstractItem item, int count) {
		if (!to.isInventoryFull() || to.hasItem(item)) {
			
			List<AbstractItem> items = from.getAllItemsInInventory().stream()
				.filter(item::equals)
				.collect(Collectors.toList());
			
			for(int i = 0 ; i<count; i++) {
				to.addItem(items.get(i), false);
				from.removeItem(items.get(i));
			}
		}
	}
	
	private static void dropItems(GameCharacter from, AbstractItem item, int count) {
		if (!Main.game.getCurrentCell().getInventory().isInventoryFull() || Main.game.getCurrentCell().getInventory().hasItem(item)) {
			
			List<AbstractItem> items = from.getAllItemsInInventory().stream()
				.filter(item::equals)
				.collect(Collectors.toList());
			
			for(int i = 0 ; i<count; i++) {
				Main.game.getCurrentCell().getInventory().addItem(items.get(i));
				from.removeItem(items.get(i));
			}
		}
	}
	
	private static void pickUpItems(GameCharacter to, AbstractItem item, int count) {
		if (!Main.game.getCurrentCell().getInventory().isInventoryFull() || Main.game.getCurrentCell().getInventory().hasItem(item)) {
			
			List<AbstractItem> items = Main.game.getCurrentCell().getInventory().getAllItemsInInventory().stream()
				.filter(item::equals)
				.collect(Collectors.toList());
			
			for(int i = 0 ; i<count; i++) {
				to.addItem(items.get(i), true);
			}
		}
	}
	
	private static void sellItems(GameCharacter from, GameCharacter to, AbstractItem item, int count, int itemPrice) {
		if (to.isPlayer()?(!to.isInventoryFull() || to.hasItem(item)):true) {
			
			if(buyback && to.isPlayer()) {
				Main.game.getPlayer().incrementMoney(-itemPrice);
				from.incrementMoney(itemPrice);
				Main.game.getPlayer().addItem(item, false);
				Main.game.getPlayer().getBuybackStack().remove(buyBackIndex);
				
			} else {
				List<AbstractItem> items = from.getAllItemsInInventory().stream()
					.filter(item::equals)
					.collect(Collectors.toList());
				
				for(int i = 0 ; i<count; i++) {
					if(from.isPlayer()) {
						Main.game.getPlayer().getBuybackStack().push(new ShopTransaction(item, itemPrice));
					} else {
						to.addItem(items.get(i), false);
					}
					from.incrementMoney(itemPrice);
					to.incrementMoney(-itemPrice);
					from.removeItem(items.get(i));
				}
			}
			
			if(to.isPlayer()) {
				((NPC) from).handleSellingEffects(item, count, itemPrice);
			}
		}
	}
	
	
	
	// Weapons:
	
	private static void transferWeapons(GameCharacter from, GameCharacter to, AbstractWeapon weapon, int count) {
		if (!to.isInventoryFull() || to.hasWeapon(weapon)) {
			
			List<AbstractWeapon> weapons = from.getAllWeaponsInInventory().stream()
				.filter(weapon::equals)
				.collect(Collectors.toList());
			
			for(int i = 0 ; i<count; i++) {
				to.addWeapon(weapons.get(i), false);
				from.removeWeapon(weapons.get(i));
			}
		}
	}
	
	private static void dropWeapons(GameCharacter from, AbstractWeapon weapon, int count) {
		if (!Main.game.getCurrentCell().getInventory().isInventoryFull() || Main.game.getCurrentCell().getInventory().hasWeapon(weapon)) {
			
			List<AbstractWeapon> weapons = from.getAllWeaponsInInventory().stream()
				.filter(weapon::equals)
				.collect(Collectors.toList());
			
			for(int i = 0 ; i<count; i++) {
				Main.game.getCurrentCell().getInventory().addWeapon(weapons.get(i));
				from.removeWeapon(weapons.get(i));
			}
		}
	}
	
	private static void pickUpWeapons(GameCharacter to, AbstractWeapon weapon, int count) {
		if (!Main.game.getCurrentCell().getInventory().isInventoryFull() || Main.game.getCurrentCell().getInventory().hasWeapon(weapon)) {
			
			List<AbstractWeapon> weapons = Main.game.getCurrentCell().getInventory().getAllWeaponsInInventory().stream()
				.filter(weapon::equals)
				.collect(Collectors.toList());
			
			for(int i = 0 ; i<count; i++) {
				to.addWeapon(weapons.get(i), true);
			}
		}
	}
	
	private static void sellWeapons(GameCharacter from, GameCharacter to, AbstractWeapon weapon, int count, int itemPrice) {
		if (!to.isInventoryFull() || to.hasWeapon(weapon)) {
			
			if(buyback && to.isPlayer()) {
				Main.game.getPlayer().incrementMoney(-itemPrice);
				from.incrementMoney(itemPrice);
				Main.game.getPlayer().addWeapon(weapon, false);
				Main.game.getPlayer().getBuybackStack().remove(buyBackIndex);
				
			} else {
				List<AbstractWeapon> weapons = from.getAllWeaponsInInventory().stream()
					.filter(weapon::equals)
					.collect(Collectors.toList());
				
				for(int i = 0 ; i<count; i++) {
					if(from.isPlayer()) {
						Main.game.getPlayer().getBuybackStack().push(new ShopTransaction(weapon, itemPrice));
					} else {
						to.addWeapon(weapons.get(i), false);
					}
					from.incrementMoney(itemPrice);
					to.incrementMoney(-itemPrice);
					from.removeWeapon(weapons.get(i));
				}
			}
			
			if(to.isPlayer()) {
				((NPC) from).handleSellingEffects(weapon, count, itemPrice);
			}
		}
	}
	
	
	// Clothing:
	
	private static void transferClothing(GameCharacter from, GameCharacter to, AbstractClothing clothing, int count) {
		if (!to.isInventoryFull() || to.hasClothing(clothing)) {
			
			List<AbstractClothing> clothings = from.getAllClothingInInventory().stream()
				.filter(clothing::equals)
				.collect(Collectors.toList());
			
			for(int i = 0 ; i<count; i++) {
				to.addClothing(clothings.get(i), false);
				from.removeClothing(clothings.get(i));
			}
		}
	}
	
	private static void dropClothing(GameCharacter from, AbstractClothing clothing, int count) {
		if (!Main.game.getCurrentCell().getInventory().isInventoryFull() || Main.game.getCurrentCell().getInventory().hasClothing(clothing)) {
			
			List<AbstractClothing> clothings = from.getAllClothingInInventory().stream()
				.filter(clothing::equals)
				.collect(Collectors.toList());
			
			for(int i = 0 ; i<count; i++) {
				Main.game.getCurrentCell().getInventory().addClothing(clothings.get(i));
				from.removeClothing(clothings.get(i));
			}
		}
	}
	
	private static void pickUpClothing(GameCharacter to, AbstractClothing clothing, int count) {
		if (!Main.game.getCurrentCell().getInventory().isInventoryFull() || Main.game.getCurrentCell().getInventory().hasClothing(clothing)) {
			
			List<AbstractClothing> clothings = Main.game.getCurrentCell().getInventory().getAllClothingInInventory().stream()
				.filter(clothing::equals)
				.collect(Collectors.toList());
			
			for(int i = 0 ; i<count; i++) {
				to.addClothing(clothings.get(i), true);
			}
		}
	}
	
	private static void sellClothing(GameCharacter from, GameCharacter to, AbstractClothing clothing, int count, int itemPrice) {
		if (!to.isInventoryFull() || to.hasClothing(clothing)) {
			
			if(buyback && to.isPlayer()) {
				Main.game.getPlayer().incrementMoney(-itemPrice);
				from.incrementMoney(itemPrice);
				Main.game.getPlayer().addClothing(clothing, false);
				Main.game.getPlayer().getBuybackStack().remove(buyBackIndex);
				
			} else {
				List<AbstractClothing> clothings = from.getAllClothingInInventory().stream()
					.filter(clothing::equals)
					.collect(Collectors.toList());
				
				for(int i = 0 ; i<count; i++) {
					if(from.isPlayer()) {
						Main.game.getPlayer().getBuybackStack().push(new ShopTransaction(clothing, itemPrice));
					} else {
						to.addClothing(clothings.get(i), false);
					}
					from.incrementMoney(itemPrice);
					to.incrementMoney(-itemPrice);
					from.removeClothing(clothings.get(i));
				}
			}
			
			if(to.isPlayer()) {
				((NPC) from).handleSellingEffects(clothing, count, itemPrice);
			}
		}
	}
	

	public static AbstractItem getItem() {
		return item;
	}

	public static void setItem(AbstractItem item) {
		InventoryDialogue.item = item;
	}

	public static AbstractWeapon getWeapon() {
		return weapon;
	}

	public static void setWeapon(AbstractWeapon weapon) {
		InventoryDialogue.weapon = weapon;
	}

	public static AbstractClothing getClothing() {
		return clothing;
	}

	public static void setClothing(AbstractClothing clothing) {
		InventoryDialogue.clothing = clothing;
	}

	public static AbstractItem getItemFloor() {
		return itemFloor;
	}

	public static void setItemFloor(AbstractItem itemFloor) {
		InventoryDialogue.itemFloor = itemFloor;
	}

	public static AbstractWeapon getWeaponFloor() {
		return weaponFloor;
	}

	public static void setWeaponFloor(AbstractWeapon weaponFloor) {
		InventoryDialogue.weaponFloor = weaponFloor;
	}

	public static AbstractClothing getClothingFloor() {
		return clothingFloor;
	}

	public static void setClothingFloor(AbstractClothing clothingFloor) {
		InventoryDialogue.clothingFloor = clothingFloor;
	}

	public static List<AbstractClothing> getJinxedClothing() {
		return jinxedClothing;
	}
	
	public static List<AbstractClothing> getJinxedNPCClothing() {
		return jinxedNPCClothing;
	}

	public static boolean isJinxRemovalFromFloor() {
		return jinxRemovalFromFloor;
	}

	public static boolean isBuyback() {
		return buyback;
	}

	public static void setBuyback(boolean buyback) {
		InventoryDialogue.buyback = buyback;
	}

	public static int getBuyBackPrice() {
		return buyBackPrice;
	}

	public static void setBuyBackPrice(int buyBackPrice) {
		InventoryDialogue.buyBackPrice = buyBackPrice;
	}

	public static int getBuyBackIndex() {
		return buyBackIndex;
	}

	public static void setBuyBackIndex(int buyBackIndex) {
		InventoryDialogue.buyBackIndex = buyBackIndex;
	}

	public static GameCharacter getOwner() {
		return owner;
	}

	public static void setOwner(GameCharacter owner) {
		InventoryDialogue.owner = owner;
	}

	public static NPC getInventoryNPC() {
		return inventoryNPC;
	}

	public static void setInventoryNPC(NPC inventoryNPC) {
		InventoryDialogue.inventoryNPC = inventoryNPC;
	}

	public static InventoryInteraction getNPCInventoryInteraction() {
		return interactionType;
	}

	public static void setNPCInventoryInteraction(InventoryInteraction nPCInventoryInteraction) {
		interactionType = nPCInventoryInteraction;
	}

}
