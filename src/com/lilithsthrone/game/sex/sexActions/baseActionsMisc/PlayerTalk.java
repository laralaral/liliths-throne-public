package com.lilithsthrone.game.sex.sexActions.baseActionsMisc;

import java.util.List;

import com.lilithsthrone.game.character.attributes.ArousalLevel;
import com.lilithsthrone.game.character.attributes.CorruptionLevel;
import com.lilithsthrone.game.character.body.types.VaginaType;
import com.lilithsthrone.game.character.effects.Fetish;
import com.lilithsthrone.game.dialogue.utils.UtilText;
import com.lilithsthrone.game.sex.ArousalIncrease;
import com.lilithsthrone.game.sex.OrificeType;
import com.lilithsthrone.game.sex.PenetrationType;
import com.lilithsthrone.game.sex.Sex;
import com.lilithsthrone.game.sex.SexFlags;
import com.lilithsthrone.game.sex.SexPace;
import com.lilithsthrone.game.sex.SexPosition;
import com.lilithsthrone.game.sex.sexActions.SexAction;
import com.lilithsthrone.game.sex.sexActions.SexActionType;
import com.lilithsthrone.main.Main;
import com.lilithsthrone.utils.Util;
import com.lilithsthrone.utils.Util.ListValue;

/**
 * @since 0.1.79
 * @version 0.1.79
 * @author Innoxia
 */
public class PlayerTalk {
	
	public static final SexAction PLAYER_DIRTY_TALK = new SexAction(
			SexActionType.PLAYER,
			ArousalIncrease.TWO_LOW,
			ArousalIncrease.TWO_LOW,
			CorruptionLevel.ONE_VANILLA,
			null,
			null) {
		
		@Override
		public CorruptionLevel getCorruptionNeeded(){
			if(Sex.getSexPacePlayer()==SexPace.SUB_RESISTING) {
				return CorruptionLevel.ZERO_PURE;
			} else {
				return CorruptionLevel.ONE_VANILLA;
			}
		}
		
		@Override
		public String getActionTitle() {
			switch(Sex.getSexPacePlayer()) {
				case DOM_GENTLE:
					return "Dirty talk";
				case DOM_NORMAL:
					return "Dirty talk";
				case DOM_ROUGH:
					return "Rough talk";
				case SUB_EAGER:
					return "Dirty talk";
				case SUB_NORMAL:
					return "Dirty talk";
				case SUB_RESISTING:
					return "Beg for [npc.herHim] to stop";
				default:
					return "Dirty talk";
			}
		}

		@Override
		public String getActionDescription() {
			return "Talk dirty to [npc.name].";
		}

		@Override
		public String getDescription() {
			
			if(Sex.getPosition()==SexPosition.DOGGY_PLAYER_ON_ALL_FOURS) {
				
				switch(Sex.getSexPacePlayer()) {
					case SUB_EAGER:
						return UtilText.returnStringAtRandom(
								"Turning your head to look back at [npc.name], [pc.a_moan+] escapes from between your [pc.lips+], ",
								"You turn your head to look back at [npc.name], letting out [pc.a_moan+] before calling out, ")
								+ Sex.getPartner().getPlayerDirtyTalk(Sex.isPlayerDom());
					case SUB_RESISTING:
						return UtilText.returnStringAtRandom(
								"Desperately trying to crawl away from [npc.name], [npc.she] grabs your [pc.hips] and pulls you back as you [pc.sob], ",
								"Trying to crawl away from [npc.name], [npc.she] holds you firmly in place as you let out [pc.a_sob+], ")
								+ Sex.getPartner().getPlayerDirtyTalk(Sex.isPlayerDom());
					default: 
						return UtilText.returnStringAtRandom(
								"Turning your head to look back at [npc.name], [pc.a_moan] escapes from between your [pc.lips+], ",
								"You turn your head to look back at [npc.name], letting out [pc.a_moan] before calling out, ")
								+ Sex.getPartner().getPlayerDirtyTalk(Sex.isPlayerDom());
				}
				
			} else if(Sex.getPosition()==SexPosition.KNEELING_PARTNER_PERFORMING_ORAL) {
				
				switch(Sex.getSexPacePlayer()) {
					case DOM_GENTLE:
						return UtilText.returnStringAtRandom(
								"You look down at [npc.name] as [npc.she] kneels beneath you, ",
								"Looking down at [npc.name], you sigh, ")
								+ Sex.getPartner().getPlayerDirtyTalk(Sex.isPlayerDom());
					case DOM_ROUGH:
						return UtilText.returnStringAtRandom(
								"You grin down at [npc.name] as [npc.she] kneels beneath you, ",
								"Looking down at [npc.name], you growl, ")
								+ Sex.getPartner().getPlayerDirtyTalk(Sex.isPlayerDom());
					default: 
						return UtilText.returnStringAtRandom(
								"You look down at [npc.name] as [npc.she] kneels beneath you, ",
								"Looking down at [npc.name], you [pc.moan], ")
								+ Sex.getPartner().getPlayerDirtyTalk(Sex.isPlayerDom());
				}
				
			} else if(Sex.getPosition()==SexPosition.KNEELING_PLAYER_PERFORMING_ORAL) {
				
				switch(Sex.getSexPacePlayer()) {
					case SUB_EAGER:
						return UtilText.returnStringAtRandom(
								"You glance up at [npc.name] as you [pc.moan] up to [npc.herHim], ",
								"Looking up at [npc.name] standing above you, you [pc.moan] up to [npc.herHim], ")
								+ Sex.getPartner().getPlayerDirtyTalk(Sex.isPlayerDom());
					case SUB_RESISTING:
						return UtilText.returnStringAtRandom(
								"You glance up at [npc.name] before letting out [pc.a_sob+], ",
								"Looking up at [npc.name] standing above you, you let out [pc.a_sob+], ")
								+ Sex.getPartner().getPlayerDirtyTalk(Sex.isPlayerDom());
					default: 
						return UtilText.returnStringAtRandom(
								"You glance up at [npc.name] as you speak up to [npc.herHim], ",
								"Looking up at [npc.name] standing above you, you speak up to [npc.herHim], ")
								+ Sex.getPartner().getPlayerDirtyTalk(Sex.isPlayerDom());
				}
				
			} else if(Sex.getPosition()==SexPosition.SIXTY_NINE_PLAYER_TOP) {
				
				switch(Sex.getSexPacePlayer()) {
					case DOM_GENTLE:
						return UtilText.returnStringAtRandom(
								"You look back at [npc.name] as [npc.she] lies beneath you, and speak down to [npc.herHim], ",
								"Looking back at [npc.name] as [npc.she] lies beneath you, you speak down to [npc.herHim], ")
								+ Sex.getPartner().getPlayerDirtyTalk(Sex.isPlayerDom());
					case DOM_ROUGH:
						return UtilText.returnStringAtRandom(
								"You look back at [npc.name] as [npc.she] lies beneath you, and growl down to [npc.herHim], ",
								"Looking back at [npc.name] as [npc.she] lies beneath you, you growl down to [npc.herHim], ")
								+ Sex.getPartner().getPlayerDirtyTalk(Sex.isPlayerDom());
					default: 
						return UtilText.returnStringAtRandom(
								"You look back at [npc.name] as [npc.she] lies beneath you, and [pc.moanVerb] down to [npc.herHim], ",
								"Looking back at [npc.name] as [npc.she] lies beneath you, you [pc.moanVerb] down to [npc.herHim], ")
								+ Sex.getPartner().getPlayerDirtyTalk(Sex.isPlayerDom());
				}
				
			} else {
			
				switch(Sex.getSexPacePlayer()) {
					case DOM_GENTLE:
						return UtilText.returnStringAtRandom(
								"You let out a soft [pc.moan], ",
								"You gently sigh, ")
								+ Sex.getPartner().getPlayerDirtyTalk(Sex.isPlayerDom());
					case DOM_ROUGH:
						return UtilText.returnStringAtRandom(
								"You let out a rough growl before speaking out loud, ",
								"You make a threatening growling noise before speaking, ")
								+ Sex.getPartner().getPlayerDirtyTalk(Sex.isPlayerDom());
					case SUB_EAGER:
						return UtilText.returnStringAtRandom(
								"A desperate [pc.moan] escapes from between your [pc.lips+], ",
								"You let out a desperate [pc.moan] before addressing [npc.name], ")
								+ Sex.getPartner().getPlayerDirtyTalk(Sex.isPlayerDom());
					case SUB_RESISTING:
						return UtilText.returnStringAtRandom(
								"A protesting whine escapes from between your [pc.lips+] as you struggle against [npc.name], ",
								"You let out a distressed whining noise as you try to shuffle away from [npc.name], ")
								+ Sex.getPartner().getPlayerDirtyTalk(Sex.isPlayerDom());
					default: // DOM_NORMAL, SUB_NORMAL:
						return UtilText.returnStringAtRandom(
								"[pc.A_moan] escapes from between your [pc.lips+], ",
								"You let out [pc.a_moan] before addressing [npc.name], ")
								+ Sex.getPartner().getPlayerDirtyTalk(Sex.isPlayerDom());
				}
			
			}
		}
	};
	
	public static final SexAction PLAYER_ASK_FOR_CREAMPIE = new SexAction(
			SexActionType.PLAYER,
			ArousalIncrease.TWO_LOW,
			ArousalIncrease.TWO_LOW,
			CorruptionLevel.ONE_VANILLA,
			null,
			null) {
		@Override
		public String getActionTitle() {
			if(Sex.getPenetrationTypeInOrifice(OrificeType.MOUTH_PLAYER) == PenetrationType.PENIS_PARTNER || Sex.getPenetrationTypeInOrifice(OrificeType.BREAST_PLAYER) == PenetrationType.PENIS_PARTNER) {
				return "Ask for cum";
			} else {
				return "Ask for creampie";
			}
		}

		@Override
		public String getActionDescription() {
			return "You can feel that [npc.name] is fast approaching [npc.her] orgasm. Ask [npc.herHim] to fill you with [npc.her] cum.";
		}

		@Override
		public boolean isBaseRequirementsMet() {
			boolean takingCock = false;
			
			if(Sex.getOngoingPenetrationMap().get(PenetrationType.PENIS_PARTNER) != null) {
				for(OrificeType ot : Sex.getOngoingPenetrationMap().get(PenetrationType.PENIS_PARTNER)) {
					if(ot.isPlayer())
						takingCock=true;
				}
			}
			
			return Sex.getPartner().getArousal() >= ArousalLevel.FOUR_PASSIONATE.getMinimumValue()
					&& takingCock
					&& !SexFlags.playerRequestedCreampie
					&& !SexFlags.playerRequestedPullOut;
		}

		@Override
		public String getDescription() {
			if(Sex.getPenetrationTypeInOrifice(OrificeType.VAGINA_PLAYER) == PenetrationType.PENIS_PARTNER) {
				return "Through your desperate moans and lewd cries, you somehow manage to formulate a sentence as you cry out for [npc.name]'s cum, "
						+(Main.game.getPlayer().isVisiblyPregnant()
								?"[pc.speech(Fuck! Cum in me! I need your cum!)]"
								:"[pc.speech(Breed me! Cum in me! I need your cum!)]");
				
			} else if(Sex.getPenetrationTypeInOrifice(OrificeType.ANUS_PLAYER) == PenetrationType.PENIS_PARTNER) {
				return "Through your desperate moans and lewd cries, you somehow manage to formulate a sentence as you cry out for [npc.name]'s cum,"
						+ " [pc.speech(Fuck! Cum in me! I need your cum!)]";
				
			} else if(Sex.getPenetrationTypeInOrifice(OrificeType.NIPPLE_PLAYER) == PenetrationType.PENIS_PARTNER) {
				return "Through your desperate moans and lewd cries, you somehow manage to formulate a sentence as you cry out for [npc.name]'s cum, "
						+ " [pc.speech(Fuck! Cum in me! I need your cum!)]";
				
			} else if(Sex.getPenetrationTypeInOrifice(OrificeType.BREAST_PLAYER) == PenetrationType.PENIS_PARTNER) {
				return "Through your desperate moans and lewd cries, you somehow manage to formulate a sentence as you cry out for [npc.name]'s cum, "
						+ " [pc.speech(Yes! Cum for me! Cover my tits with your cum!)]";
				
			} else {
				return "Through your desperate moans and lewd cries, you somehow manage to formulate a sentence as you cry out for [npc.name]'s cum, "
						+ " [pc.speech(Cum for me! I want to taste your cum!)]";
			}
		}

		@Override
		public void applyEffects() {
			SexFlags.playerRequestedCreampie = true;
			SexFlags.playerRequestedPullOut = false;
		}
		
		@Override
		public List<Fetish> getFetishesPlayer() {
			if(Sex.getPenetrationTypeInOrifice(OrificeType.VAGINA_PLAYER) == PenetrationType.PENIS_PARTNER) {
				return Util.newArrayListOfValues(new ListValue<>(Fetish.FETISH_CUM_ADDICT), new ListValue<>(Fetish.FETISH_PREGNANCY));
				
			} else if(Sex.getPenetrationTypeInOrifice(OrificeType.ANUS_PLAYER) == PenetrationType.PENIS_PARTNER) {
				return Util.newArrayListOfValues(new ListValue<>(Fetish.FETISH_CUM_ADDICT), new ListValue<>(Fetish.FETISH_ANAL_RECEIVING));
				
			} else if(Sex.getPenetrationTypeInOrifice(OrificeType.NIPPLE_PLAYER) == PenetrationType.PENIS_PARTNER) {
				return Util.newArrayListOfValues(new ListValue<>(Fetish.FETISH_CUM_ADDICT), new ListValue<>(Fetish.FETISH_BREASTS_SELF));
				
			} else {
				return Util.newArrayListOfValues(new ListValue<>(Fetish.FETISH_CUM_ADDICT));
			}
		}
		
		@Override
		public List<Fetish> getFetishesPartner() {
			if(Sex.getPenetrationTypeInOrifice(OrificeType.VAGINA_PLAYER) == PenetrationType.PENIS_PARTNER) {
				return Util.newArrayListOfValues(new ListValue<>(Fetish.FETISH_CUM_STUD), new ListValue<>(Fetish.FETISH_IMPREGNATION));
				
			} else if(Sex.getPenetrationTypeInOrifice(OrificeType.ANUS_PLAYER) == PenetrationType.PENIS_PARTNER) {
				return Util.newArrayListOfValues(new ListValue<>(Fetish.FETISH_CUM_STUD), new ListValue<>(Fetish.FETISH_ANAL_GIVING));
				
			} else if(Sex.getPenetrationTypeInOrifice(OrificeType.NIPPLE_PLAYER) == PenetrationType.PENIS_PARTNER) {
				return Util.newArrayListOfValues(new ListValue<>(Fetish.FETISH_CUM_STUD), new ListValue<>(Fetish.FETISH_BREASTS_OTHERS));
				
			} else {
				return Util.newArrayListOfValues(new ListValue<>(Fetish.FETISH_CUM_STUD));
			}
		}
	};
	
	public static final SexAction PLAYER_ASK_FOR_PULL_OUT = new SexAction(
			SexActionType.PLAYER,
			ArousalIncrease.TWO_LOW,
			ArousalIncrease.TWO_LOW,
			CorruptionLevel.ZERO_PURE,
			null,
			null) {
		@Override
		public String getActionTitle() {
			return "Ask for pullout";
		}

		@Override
		public String getActionDescription() {
			return "You can feel that [npc.name] is fast approaching [npc.her] orgasm. Ask [npc.herHim] to pull out before [npc.she] cums.";
		}

		@Override
		public boolean isBaseRequirementsMet() {
			boolean takingCock = false;
			
			if(Sex.getOngoingPenetrationMap().get(PenetrationType.PENIS_PARTNER) != null) {
				for(OrificeType ot : Sex.getOngoingPenetrationMap().get(PenetrationType.PENIS_PARTNER)) {
					if(ot.isPlayer())
						takingCock=true;
				}
			}
			
			return Sex.getPartner().getArousal() >= ArousalLevel.FOUR_PASSIONATE.getMinimumValue()
					&& takingCock
					&& !SexFlags.playerRequestedCreampie
					&& !SexFlags.playerRequestedPullOut;
		}

		@Override
		public String getDescription() {
			if(Sex.getPenetrationTypeInOrifice(OrificeType.VAGINA_PLAYER) == PenetrationType.PENIS_PARTNER) {
				return "Through your desperate moans and lewd cries, you somehow manage to formulate a sentence as you cry out to [npc.name], "
						+(Main.game.getPlayer().isVisiblyPregnant()
								?"[pc.speech(Pull out! I don't want you to cum in me!)]"
								:"[pc.speech(Pull out! I don't want to get pregnant!)]");
				
			} else if(Sex.getPenetrationTypeInOrifice(OrificeType.ANUS_PLAYER) == PenetrationType.PENIS_PARTNER) {
				return "Through your desperate moans and lewd cries, you somehow manage to formulate a sentence as you cry out to [npc.name], "
						+ "[pc.speech(Pull out! I don't want you to cum in me!)]";
				
			} else if(Sex.getPenetrationTypeInOrifice(OrificeType.NIPPLE_PLAYER) == PenetrationType.PENIS_PARTNER) {
				return "Through your desperate moans and lewd cries, you somehow manage to formulate a sentence as you cry out to [npc.name], "
						+ "[pc.speech(Pull out! I don't want you to cum in me!)]";
				
			} else {
				return "Through your desperate moans and lewd cries, you somehow manage to cry out around [npc.name]'s [npc.cock], "
						+ "[pc.speech(Pull out! I don't want to taste your cum!)]";
			}
		}

		@Override
		public void applyEffects() {
			SexFlags.playerRequestedCreampie = false;
			SexFlags.playerRequestedPullOut = true;
		}
	};
	
	public static final SexAction PLAYER_REQUEST_VAGINAL = new SexAction(
			SexActionType.PLAYER,
			ArousalIncrease.ONE_MINIMUM,
			ArousalIncrease.ONE_MINIMUM,
			CorruptionLevel.ONE_VANILLA,
			null,
			null) {
		@Override
		public String getActionTitle() {
			return "Request vaginal";
		}

		@Override
		public String getActionDescription() {
			return "Tell [npc.name] that you'd like [npc.herHim] to use your pussy.";
		}

		@Override
		public boolean isBaseRequirementsMet() {
			return Main.game.getPlayer().hasVagina()
					&& !Sex.getPlayerPenetrationRequests().contains(OrificeType.VAGINA_PLAYER)
					&& !SexFlags.requestsBlockedPlayer
					&& !Sex.isPlayerDom();
		}

		@Override
		public String getDescription() {
			return "Putting on the most pleading expression you can muster, you cry out to [npc.name], "
							+UtilText.returnStringAtRandom(
									"[pc.speech(Please, use my pussy!)]",
									"[pc.speech(Use my little pussy, please!)]");
		}

		@Override
		public void applyEffects() {
			Sex.clearPlayerPenetrationRequests();
			Sex.addPlayerPenetrationRequest(OrificeType.VAGINA_PLAYER);
		}
	};
	
	public static final SexAction PLAYER_REQUEST_ANAL = new SexAction(
			SexActionType.PLAYER,
			ArousalIncrease.ONE_MINIMUM,
			ArousalIncrease.ONE_MINIMUM,
			CorruptionLevel.ONE_VANILLA,
			null,
			null) {
		
		@Override
		public String getActionTitle() {
			return "Request anal";
		}

		@Override
		public String getActionDescription() {
			return "Tell [npc.name] that you'd like [npc.herHim] to use your ass.";
		}

		@Override
		public boolean isBaseRequirementsMet() {
			return !Sex.getPlayerPenetrationRequests().contains(OrificeType.ANUS_PLAYER)
					&& !SexFlags.requestsBlockedPlayer
					&& !Sex.isPlayerDom();
		}

		@Override
		public String getDescription() {
			return "Putting on the most pleading expression you can muster, you cry out to [npc.name], "
							+UtilText.returnStringAtRandom(
									"[pc.speech(Please, use my ass!)]",
									"[pc.speech(Use my ass, please!)]");
		}

		@Override
		public void applyEffects() {
			Sex.clearPlayerPenetrationRequests();
			Sex.addPlayerPenetrationRequest(OrificeType.ANUS_PLAYER);
		}
		
		@Override
		public List<Fetish> getFetishesPlayer() {
			if(Main.game.getPlayer().getVaginaType()!=VaginaType.NONE) {
				return Util.newArrayListOfValues(new ListValue<>(Fetish.FETISH_ANAL_RECEIVING), new ListValue<>(Fetish.FETISH_PURE_VIRGIN));
				
			} else {
				return Util.newArrayListOfValues(new ListValue<>(Fetish.FETISH_ANAL_RECEIVING));
			}
		}
		
		@Override
		public List<Fetish> getFetishesPartner() {
			return Util.newArrayListOfValues(new ListValue<>(Fetish.FETISH_ANAL_GIVING));
		}
	};
	
	public static final SexAction PLAYER_REQUEST_ORAL = new SexAction(
			SexActionType.PLAYER,
			ArousalIncrease.ONE_MINIMUM,
			ArousalIncrease.ONE_MINIMUM,
			CorruptionLevel.ONE_VANILLA,
			null,
			null) {
		@Override
		public String getActionTitle() {
			return "Request oral";
		}

		@Override
		public String getActionDescription() {
			return "Tell [npc.name] that you'd like [npc.herHim] to use your mouth.";
		}

		@Override
		public boolean isBaseRequirementsMet() {
			return !Sex.getPlayerPenetrationRequests().contains(OrificeType.MOUTH_PLAYER)
					&& !SexFlags.requestsBlockedPlayer
					&& !Sex.isPlayerDom();
		}

		@Override
		public String getDescription() {
			return "Putting on the most pleading expression you can muster, you cry out to [npc.name], "
							+UtilText.returnStringAtRandom(
									"[pc.speech(Please, use my mouth!)]",
									"[pc.speech(Use my mouth, please!)]");
		}

		@Override
		public void applyEffects() {
			Sex.clearPlayerPenetrationRequests();
			Sex.addPlayerPenetrationRequest(OrificeType.MOUTH_PLAYER);
		}
		
		@Override
		public List<Fetish> getFetishesPlayer() {
			return Util.newArrayListOfValues(new ListValue<>(Fetish.FETISH_ORAL_GIVING));
		}
		
		@Override
		public List<Fetish> getFetishesPartner() {
			return Util.newArrayListOfValues(new ListValue<>(Fetish.FETISH_ORAL_RECEIVING));
		}
	};
	
	public static final SexAction PLAYER_REQUEST_NIPPLE = new SexAction(
			SexActionType.PLAYER,
			ArousalIncrease.ONE_MINIMUM,
			ArousalIncrease.ONE_MINIMUM,
			CorruptionLevel.ONE_VANILLA,
			null,
			null) {
		@Override
		public String getActionTitle() {
			return "Request nipple-sex";
		}

		@Override
		public String getActionDescription() {
			return "Tell [npc.name] that you'd like [npc.herHim] to use your [pc.nipples+].";
		}

		@Override
		public boolean isBaseRequirementsMet() {
			return !Sex.getPlayerPenetrationRequests().contains(OrificeType.NIPPLE_PLAYER)
					&& Main.game.getPlayer().isBreastFuckableNipplePenetration()
					&& !SexFlags.requestsBlockedPlayer
					&& !Sex.isPlayerDom();
		}

		@Override
		public String getDescription() {
			return "Putting on the most pleading expression you can muster, you cry out to [npc.name], "
							+UtilText.returnStringAtRandom(
									"[pc.speech(Please, fuck my nipples!)]",
									"[pc.speech(Fuck my nipples, please!)]");
		}

		@Override
		public void applyEffects() {
			Sex.clearPlayerPenetrationRequests();
			Sex.addPlayerPenetrationRequest(OrificeType.NIPPLE_PLAYER);
		}
		
		@Override
		public List<Fetish> getFetishesPlayer() {
			return Util.newArrayListOfValues(new ListValue<>(Fetish.FETISH_BREASTS_SELF));
		}
		
		@Override
		public List<Fetish> getFetishesPartner() {
			return Util.newArrayListOfValues(new ListValue<>(Fetish.FETISH_BREASTS_OTHERS));
		}
	};
	
	public static final SexAction PLAYER_REQUEST_PAIZURI = new SexAction(
			SexActionType.PLAYER,
			ArousalIncrease.ONE_MINIMUM,
			ArousalIncrease.ONE_MINIMUM,
			CorruptionLevel.ONE_VANILLA,
			null,
			null) {
		@Override
		public String getActionTitle() {
			return "Request paizuri";
		}

		@Override
		public String getActionDescription() {
			return "Tell [npc.name] that you'd like [npc.herHim] to fuck your [pc.breasts+].";
		}

		@Override
		public boolean isBaseRequirementsMet() {
			return !Sex.getPlayerPenetrationRequests().contains(OrificeType.BREAST_PLAYER)
					&& Main.game.getPlayer().isBreastFuckablePaizuri()
					&& !SexFlags.requestsBlockedPlayer
					&& !Sex.isPlayerDom();
		}

		@Override
		public String getDescription() {
			return "Putting on the most pleading expression you can muster, you cry out to [npc.name], "
							+UtilText.returnStringAtRandom(
									"[pc.speech(Please, fuck my tits!)]",
									"[pc.speech(Fuck my breasts, please!)]");
		}

		@Override
		public void applyEffects() {
			Sex.clearPlayerPenetrationRequests();
			Sex.addPlayerPenetrationRequest(OrificeType.BREAST_PLAYER);
		}
		
		@Override
		public List<Fetish> getFetishesPlayer() {
			return Util.newArrayListOfValues(new ListValue<>(Fetish.FETISH_BREASTS_SELF));
		}
		
		@Override
		public List<Fetish> getFetishesPartner() {
			return Util.newArrayListOfValues(new ListValue<>(Fetish.FETISH_BREASTS_OTHERS));
		}
	};
	
	public static final SexAction PLAYER_REQUEST_NAIZURI = new SexAction(
			SexActionType.PLAYER,
			ArousalIncrease.ONE_MINIMUM,
			ArousalIncrease.ONE_MINIMUM,
			CorruptionLevel.ONE_VANILLA,
			null,
			null) {
		@Override
		public String getActionTitle() {
			return "Request naizuri";
		}

		@Override
		public String getActionDescription() {
			return "Tell [npc.name] that you'd like [npc.herHim] to grind [npc.her] [npc.cock] up against your chest.";
		}

		@Override
		public boolean isBaseRequirementsMet() {
			return !Sex.getPlayerPenetrationRequests().contains(OrificeType.BREAST_PLAYER)
					&& !Main.game.getPlayer().isBreastFuckablePaizuri()
					&& !SexFlags.requestsBlockedPlayer
					&& !Sex.isPlayerDom();
		}

		@Override
		public String getDescription() {
			return "Putting on the most pleading expression you can muster, you cry out to [npc.name], "
							+UtilText.returnStringAtRandom(
									"[pc.speech(Please, grind your cock up against my chest!)]",
									"[pc.speech(Grind on my chest, please!)]");
		}

		@Override
		public void applyEffects() {
			Sex.clearPlayerPenetrationRequests();
			Sex.addPlayerPenetrationRequest(OrificeType.BREAST_PLAYER);
		}
		
		@Override
		public List<Fetish> getFetishesPlayer() {
			return Util.newArrayListOfValues(new ListValue<>(Fetish.FETISH_BREASTS_SELF));
		}
		
		@Override
		public List<Fetish> getFetishesPartner() {
			return Util.newArrayListOfValues(new ListValue<>(Fetish.FETISH_BREASTS_OTHERS));
		}
	};
}
