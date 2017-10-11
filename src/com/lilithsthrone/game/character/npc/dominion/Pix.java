package com.lilithsthrone.game.character.npc.dominion;

import com.lilithsthrone.game.character.NameTriplet;
import com.lilithsthrone.game.character.SexualOrientation;
import com.lilithsthrone.game.character.body.Covering;
import com.lilithsthrone.game.character.body.types.BodyCoveringType;
import com.lilithsthrone.game.character.body.valueEnums.Capacity;
import com.lilithsthrone.game.character.body.valueEnums.CupSize;
import com.lilithsthrone.game.character.effects.Fetish;
import com.lilithsthrone.game.character.gender.Gender;
import com.lilithsthrone.game.character.npc.NPC;
import com.lilithsthrone.game.character.race.RaceStage;
import com.lilithsthrone.game.character.race.RacialBody;
import com.lilithsthrone.game.combat.Attack;
import com.lilithsthrone.game.dialogue.DialogueNodeOld;
import com.lilithsthrone.game.dialogue.responses.Response;
import com.lilithsthrone.game.inventory.CharacterInventory;
import com.lilithsthrone.game.inventory.clothing.AbstractClothing;
import com.lilithsthrone.game.inventory.clothing.AbstractClothingType;
import com.lilithsthrone.game.inventory.clothing.ClothingType;
import com.lilithsthrone.utils.Colour;
import com.lilithsthrone.world.WorldType;
import com.lilithsthrone.world.places.ShoppingArcade;

/**
 * @since 0.1.6x
 * @version 0.1.86
 * @author Innoxia
 */
public class Pix extends NPC {

	private static final long serialVersionUID = 1L;

	private AbstractClothing
			groin = AbstractClothingType.generateClothing(ClothingType.GROIN_BOYSHORTS, Colour.CLOTHING_WHITE, false),
			leg = AbstractClothingType.generateClothing(ClothingType.LEG_YOGA_PANTS, Colour.CLOTHING_PINK_LIGHT, false),
			chest = AbstractClothingType.generateClothing(ClothingType.CHEST_SPORTS_BRA, Colour.CLOTHING_PINK_LIGHT, false),
			socks = AbstractClothingType.generateClothing(ClothingType.SOCK_SOCKS, Colour.CLOTHING_WHITE, false),
			shoes = AbstractClothingType.generateClothing(ClothingType.FOOT_TRAINERS, Colour.CLOTHING_PINK_LIGHT, false);

	public Pix() {
		super(new NameTriplet("Pix"), "An extremely energetic dog-girl. While you're at the gym, Pix is never far away.",
				10, Gender.F_V_B_FEMALE, RacialBody.DOG_MORPH, RaceStage.GREATER,
				new CharacterInventory(10), WorldType.SHOPPING_ARCADE, ShoppingArcade.PIXS_GYM, true);

		this.setSexualOrientation(SexualOrientation.AMBIPHILIC);
		
		this.setEyeCovering(new Covering(BodyCoveringType.EYE_DOG_MORPH, Colour.EYE_BROWN));
		this.setHairCovering(new Covering(BodyCoveringType.HAIR_CANINE_FUR, Colour.COVERING_BROWN_DARK), true);
		this.setSkinCovering(new Covering(BodyCoveringType.CANINE_FUR, Colour.COVERING_BROWN), true);
		this.setSkinCovering(new Covering(BodyCoveringType.HUMAN, Colour.SKIN_OLIVE), true);

		this.setVaginaVirgin(false);
		this.setVaginaCapacity(Capacity.FOUR_LOOSE.getMedianValue());
		this.setBreastSize(CupSize.B.getMeasurement());
		this.setBreastRows(3);

		this.equipClothingFromNowhere(groin, true, this);
		this.equipClothingFromNowhere(chest, true, this);
		this.equipClothingFromNowhere(leg, true, this);
		this.equipClothingFromNowhere(socks, true, this);
		this.equipClothingFromNowhere(shoes, true, this);
		
		this.addFetish(Fetish.FETISH_DENIAL);
		this.addFetish(Fetish.FETISH_DOMINANT);
	}
	
	@Override
	public void changeFurryLevel(){
	}
	
	@Override
	public DialogueNodeOld getEncounterDialogue() {
		return null;
	}

	@Override
	public void endSex(boolean applyEffects) {
	}

	// Combat (you never fight Pix):
	@Override
	public String getCombatDescription() {
		return null;
	}
	@Override
	public String getAttackDescription(Attack attackType, boolean isHit) {
		return null;
	}
	@Override
	public Response endCombat(boolean applyEffects, boolean victory) {
		return null;
	}
	@Override
	public Attack attackType() {
		return null;
	}
	@Override
	public int getExperienceFromVictory() {
		return 0;
	}

}
