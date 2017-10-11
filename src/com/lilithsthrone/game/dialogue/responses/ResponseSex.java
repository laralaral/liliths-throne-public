package com.lilithsthrone.game.dialogue.responses;

import java.util.List;

import com.lilithsthrone.game.character.attributes.CorruptionLevel;
import com.lilithsthrone.game.character.body.valueEnums.Femininity;
import com.lilithsthrone.game.character.effects.Fetish;
import com.lilithsthrone.game.character.effects.Perk;
import com.lilithsthrone.game.character.npc.NPC;
import com.lilithsthrone.game.character.race.Race;
import com.lilithsthrone.game.dialogue.DialogueNodeOld;
import com.lilithsthrone.game.sex.Sex;
import com.lilithsthrone.game.sex.SexPace;
import com.lilithsthrone.game.sex.managers.SexManagerInterface;

/**
 * @since 0.1.69
 * @version 0.1.86
 * @author Innoxia
 */
public class ResponseSex extends Response {
	
	private boolean consensual;
	private boolean canResist;
	private NPC partner;
	private SexManagerInterface sexManager;
	private DialogueNodeOld postSexDialogue;
	private String sexStartDescription;
	protected SexPace sexPacePlayer, sexPacePartner;

	public ResponseSex(String title, String tooltipText, DialogueNodeOld nextDialogue,
			boolean consensual, boolean canResist, NPC partner, SexManagerInterface sexManager, DialogueNodeOld postSexDialogue) {
		this(title, tooltipText, nextDialogue,
				 consensual, canResist, partner, sexManager, postSexDialogue, "");
	}
	
	public ResponseSex(String title, String tooltipText, DialogueNodeOld nextDialogue,
			boolean consensual, boolean canResist, NPC partner, SexManagerInterface sexManager, DialogueNodeOld postSexDialogue, String sexStartDescription) {
		this(title, tooltipText, nextDialogue,
				null, null, null,
				null, null, null,
				consensual, canResist, partner, sexManager, postSexDialogue, sexStartDescription);
	}
	
	public ResponseSex(String title, String tooltipText, DialogueNodeOld nextDialogue,
			List<Fetish> fetishesForUnlock, List<Fetish> fetishesBlocking, CorruptionLevel corruptionBypass,
			List<Perk> perksRequired, Femininity femininityRequired, Race raceRequired,
			boolean consensual, boolean canResist, NPC partner, SexManagerInterface sexManager, DialogueNodeOld postSexDialogue, String sexStartDescription
			) {
		super(title, tooltipText, nextDialogue,
				fetishesForUnlock, corruptionBypass,
				perksRequired, femininityRequired, raceRequired);
		
		this.consensual = consensual;
		this.canResist = canResist;
		this.partner = partner;
		this.sexManager = sexManager;
		this.postSexDialogue = postSexDialogue;
		this.sexStartDescription = sexStartDescription;
	}
	
	@Override
	public boolean isSexHighlight() {
		return true;
	}
	
	public DialogueNodeOld initSex() {
		return Sex.SEX.initialiseSex(consensual, canResist, partner, sexManager, postSexDialogue, sexStartDescription, sexPacePlayer, sexPacePartner);
	}
	

	@Override
	public boolean disabledOnNullDialogue(){
		return false;
	}
	
}
