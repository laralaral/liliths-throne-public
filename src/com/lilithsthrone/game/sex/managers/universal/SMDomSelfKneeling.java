package com.lilithsthrone.game.sex.managers.universal;

import com.lilithsthrone.game.sex.Sex;
import com.lilithsthrone.game.sex.SexPace;
import com.lilithsthrone.game.sex.SexPosition;
import com.lilithsthrone.game.sex.managers.SexManagerDefault;
import com.lilithsthrone.game.sex.sexActions.baseActionsMisc.GenericActions;
import com.lilithsthrone.game.sex.sexActions.baseActionsMisc.GenericPositioning;
import com.lilithsthrone.game.sex.sexActions.baseActionsMisc.PartnerTalk;
import com.lilithsthrone.game.sex.sexActions.baseActionsMisc.PlayerTalk;
import com.lilithsthrone.game.sex.sexActions.baseActionsPartner.PartnerPenisBreasts;
import com.lilithsthrone.game.sex.sexActions.baseActionsPartner.PartnerPenisMouth;
import com.lilithsthrone.game.sex.sexActions.baseActionsPartner.PartnerPenisNipple;
import com.lilithsthrone.game.sex.sexActions.baseActionsPlayer.PlayerFingerAnus;
import com.lilithsthrone.game.sex.sexActions.baseActionsPlayer.PlayerFingerUrethra;
import com.lilithsthrone.game.sex.sexActions.baseActionsPlayer.PlayerFingerVagina;
import com.lilithsthrone.game.sex.sexActions.baseActionsPlayer.PlayerTongueVagina;
import com.lilithsthrone.game.sex.sexActions.baseActionsSelfPartner.PartnerSelfFingerAnus;
import com.lilithsthrone.game.sex.sexActions.baseActionsSelfPartner.PartnerSelfFingerMouth;
import com.lilithsthrone.game.sex.sexActions.baseActionsSelfPartner.PartnerSelfFingerNipple;
import com.lilithsthrone.game.sex.sexActions.baseActionsSelfPartner.PartnerSelfFingerVagina;
import com.lilithsthrone.game.sex.sexActions.baseActionsSelfPartner.PartnerSelfNoPen;
import com.lilithsthrone.game.sex.sexActions.baseActionsSelfPartner.PartnerSelfTailAnus;
import com.lilithsthrone.game.sex.sexActions.baseActionsSelfPartner.PartnerSelfTailMouth;
import com.lilithsthrone.game.sex.sexActions.baseActionsSelfPartner.PartnerSelfTailNipple;
import com.lilithsthrone.game.sex.sexActions.baseActionsSelfPartner.PartnerSelfTailVagina;
import com.lilithsthrone.game.sex.sexActions.baseActionsSelfPlayer.PlayerSelfFingerAnus;
import com.lilithsthrone.game.sex.sexActions.baseActionsSelfPlayer.PlayerSelfFingerMouth;
import com.lilithsthrone.game.sex.sexActions.baseActionsSelfPlayer.PlayerSelfFingerNipple;
import com.lilithsthrone.game.sex.sexActions.baseActionsSelfPlayer.PlayerSelfFingerVagina;
import com.lilithsthrone.game.sex.sexActions.baseActionsSelfPlayer.PlayerSelfNoPen;
import com.lilithsthrone.game.sex.sexActions.baseActionsSelfPlayer.PlayerSelfTailAnus;
import com.lilithsthrone.game.sex.sexActions.baseActionsSelfPlayer.PlayerSelfTailMouth;
import com.lilithsthrone.game.sex.sexActions.baseActionsSelfPlayer.PlayerSelfTailNipple;
import com.lilithsthrone.game.sex.sexActions.baseActionsSelfPlayer.PlayerSelfTailVagina;
import com.lilithsthrone.game.sex.sexActions.universal.GenericOrgasms;

/**
 * @since 0.1.82
 * @version 0.1.82
 * @author Sinstrudel, Innoxia
 */
public class SMDomSelfKneeling extends SexManagerDefault {

	public SMDomSelfKneeling() {
		super(// Self actions:

				PlayerSelfFingerAnus.class,
				PlayerSelfFingerMouth.class,
				PlayerSelfFingerNipple.class,
				PlayerSelfFingerVagina.class,

				PlayerSelfTailAnus.class,
				PlayerSelfTailMouth.class,
				PlayerSelfTailNipple.class,
				PlayerSelfTailVagina.class,

				PlayerSelfNoPen.class,


				PartnerSelfFingerAnus.class,
				PartnerSelfFingerMouth.class,
				PartnerSelfFingerNipple.class,
				PartnerSelfFingerVagina.class,

				PartnerSelfTailAnus.class,
				PartnerSelfTailMouth.class,
				PartnerSelfTailNipple.class,
				PartnerSelfTailVagina.class,

				PartnerSelfNoPen.class,

				// Scene-specific:

				PartnerPenisMouth.class,
				PartnerPenisNipple.class,
				PartnerPenisBreasts.class,

				PlayerFingerUrethra.class,
				PlayerFingerVagina.class,
				PlayerFingerAnus.class,
				PlayerTongueVagina.class,

				// Positioning:

				GenericPositioning.class,

				// Universal:

				PlayerTalk.class,
				PartnerTalk.class,
				GenericActions.class,
				GenericOrgasms.class);
	}

	@Override
	public SexPosition getPosition() {
		return SexPosition.KNEELING_PLAYER_PERFORMING_ORAL;
	}

	@Override
	public String getStartSexDescription() {
		return "";
	}

	@Override
	public boolean isPlayerAbleToStopSex() {
		return true;
	}

	@Override
	public boolean isPlayerCanRemoveOwnClothes(){
		return true;
	}

	@Override
	public boolean isPlayerCanRemovePartnersClothes(){
		return true;
	}

	@Override
	public boolean isPartnerCanRemoveOwnClothes(){
		return true;
	}

	@Override
	public boolean isPartnerCanRemovePlayersClothes(){
		return false;
	}

	@Override
	public SexPace getStartingSexPacePlayer() {
		return SexPace.DOM_NORMAL;
	}

	@Override
	public SexPace getStartingSexPacePartner() {
		return Sex.getPartner().getSexPaceSubPreference();
	}
}
