package com.lilithsthrone.game.dialogue.places.dominion.harpyNests;

import com.lilithsthrone.game.Weather;
import com.lilithsthrone.game.character.QuestLine;
import com.lilithsthrone.game.character.body.valueEnums.Femininity;
import com.lilithsthrone.game.character.effects.Perk;
import com.lilithsthrone.game.character.effects.StatusEffect;
import com.lilithsthrone.game.dialogue.DialogueNodeOld;
import com.lilithsthrone.game.dialogue.responses.Response;
import com.lilithsthrone.game.dialogue.responses.ResponseCombat;
import com.lilithsthrone.game.dialogue.responses.ResponseSex;
import com.lilithsthrone.game.inventory.item.AbstractItemType;
import com.lilithsthrone.game.inventory.item.ItemEffectType;
import com.lilithsthrone.game.inventory.item.ItemType;
import com.lilithsthrone.game.sex.Sex;
import com.lilithsthrone.game.sex.managers.dominion.harpies.SMHarpyStanding;
import com.lilithsthrone.main.Main;
import com.lilithsthrone.utils.Util;
import com.lilithsthrone.utils.Util.ListValue;

/**
 * @since 0.1.8
 * @version 0.1.8
 * @author Innoxia
 */
public class HarpyNestNympho {

	public static final DialogueNodeOld HARPY_NEST_NYMPHO = new DialogueNodeOld("Harpy nest", ".", false) {
		private static final long serialVersionUID = 1L;

		@Override
		public int getMinutesPassed() {
			return 1;
		}
		
		@Override
		public String getLabel() {
			return "[harpyNympho.Name]'s nest";
		}

		@Override
		public String getContent() {
			if (Main.game.getCurrentWeather()==Weather.MAGIC_STORM) {
				if(Main.game.getDialogueFlags().nymphoEncountered) {
					return "<p>"
								+ "Due to the ongoing arcane storm, [harpyNympho.name]'s nest is completely deserted."
								+ " Her entire flock has retreated into the safety of the upper-floor of the building below, leaving you with no choice but to return at another time if you wanted to speak to her."
							+ "</p>";
				} else {
					return "<p>"
							+ "Due to the ongoing arcane storm, this nest is completely deserted."
							+ " The entire flock has retreated into the safety of the upper-floor of the building below, leaving you with no choice but to return at another time if you wanted to speak to the matriarch of this particular nest."
						+ "</p>";
				}
				
			} else {
				if(Main.game.getDialogueFlags().nymphoEncountered) {
					return "<p>"
								+ "You find yourself standing on the outskirts of [harpyNympho.name]'s nest; one of the most populous of all the nests in Dominion."
								+ " Despite the importance of the matriarch who resides here, her nest is no bigger than any of the minor ones scattered throughout the Harpy Nests,"
									+ " and comprises of a single, large platform built into the rooftops of several houses below."
								+ (Main.game.isDayTime()
										?" What her nest lacks in size, it more than makes up for in population, and you see what must be well over a hundred harpies loitering about on the nest's platform."
										:" What her nest lacks in size, it more than makes up for in population, and under the illumination of bright, arcane-powered lights,"
												+ " you see what must be well over a hundred harpies loitering about on the nest's platform.")
							+ "</p>"
							+ "<p>"
								+ "Taking a closer look at the multi-coloured inhabitants of this particular nest, you notice that something's slightly off about them."
								+ " While still looking very pretty, the vast majority of the flock are rather plain-looking by harpy standards."
								+ " A few of the nest's members stand out as being exceptionally beautiful in comparison, and you realise that those must be the only females of this particular flock."
								+ " Knowing about [harpyNympho.name]'s obsession with sex, you surmise that she's gathered a huge following of males in order to keep her craving for cock satisfied."
							+ "</p>"
							+ "<p>"
								+ "Looking towards the centre of the platform, you see if you can spot the nymphomaniac matriarch herself."
								+ " A flash of exceptionally bright pink feathers reveals her location, and, from her bouncing movements, you can tell that she's busy riding yet another of her flock's members."
								+ " The harpies of this flock seem to be only interested in waiting for a turn with their matriarch, and would allow you to approach [harpyNympho.name] without any trouble."
							+ "</p>";
					
				} else {
					return "<p>"
							+ "You find yourself standing on the outskirts of one of the most populous, and most important, of all the nests in Dominion."
							+ " Despite the high social standing of the matriarch who resides here, her nest is no bigger than any of the minor ones scattered throughout the Harpy Nests,"
								+ " and comprises of a single, large platform built into the rooftops of several houses below."
							+ (Main.game.isDayTime()
									?" What her nest lacks in size, it more than makes up for in population, and you see what must be well over a hundred harpies loitering about on the nest's platform."
									:" What her nest lacks in size, it more than makes up for in population, and under the illumination of bright, arcane-powered lights,"
											+ " you see what must be well over a hundred harpies loitering about on the nest's platform.")
						+ "</p>"
						+ "<p>"
							+ "Taking a closer look at the multi-coloured inhabitants of this particular nest, you notice that something's slightly off about them."
							+ " While still looking very pretty, the vast majority of the flock are rather plain-looking by harpy standards."
							+ " A few of the nest's members stand out as being exceptionally beautiful in comparison, and you realise that those must be the only females of this particular flock."
							+ " For whatever reason, the matriarch of this particular nest has gathered a huge following of males, marking it as one of the most unusual nests in Dominion."
						+ "</p>"
						+ "<p>"
							+ "Looking towards the centre of the platform, you see if you can spot this nest's matriarch."
							+ " A flash of exceptionally bright pink feathers reveals her location, and, from her bouncing movements, it almost looks like she's riding one of her flock's males..."
							+ " The harpies of this flock don't seem to show much interest in you, and, if you had any business with her, would allow you to approach their matriarch without any trouble."
						+ "</p>";
				}
			}
		}

		@Override
		public Response getResponse(int index) {
			if (index == 1) {
				if(!Main.game.getPlayer().hasQuest(QuestLine.SIDE_HARPY_PACIFICATION)) {
					return new Response("Approach [harpyNympho.name]", "You have no need to talk to the matriarch of this nest.", null);
					
				} else if (Main.game.getCurrentWeather()==Weather.MAGIC_STORM) {
					if(Main.game.getDialogueFlags().nymphoEncountered) {
						return new Response("Approach [harpyNympho.name]", "If you want to talk to [harpyNympho.name], you'll have to come back after the arcane storm has passed.", null);
					} else {
						return new Response("Approach matriarch", "If you want to talk to the matriarch, you'll have to come back after the arcane storm has passed.", null);
					}
					
				} else {
					if(Main.game.getDialogueFlags().nymphoEncountered) {
						return new Response("Approach [harpyNympho.name]", "Walk to the centre of the nest and talk to [harpyNympho.name].", HARPY_NEST_NYMPHO_APPROACH) {
							@Override
							public void effects() {
								Main.game.getHarpyNympho().addStatusEffect(StatusEffect.CREAMPIE_VAGINA, 120);
							}
						};
					} else {
						return new Response("Approach matriarch", "Walk to the centre of the nest and talk to the matriarch.", HARPY_NEST_NYMPHO_APPROACH) {
							@Override
							public void effects() {
								Main.game.getHarpyNympho().addStatusEffect(StatusEffect.CREAMPIE_VAGINA, 120);
							}
						};
					}
				}
					
			} else {
				return null;
			}
		}
	};
	
	public static final DialogueNodeOld HARPY_NEST_NYMPHO_APPROACH = new DialogueNodeOld("Harpy nest", ".", true) {
		private static final long serialVersionUID = 1L;

		@Override
		public String getLabel() {
			return "[harpyNympho.Name]'s nest";
		}
		
		@Override
		public String getContent() {
			if(Main.game.getDialogueFlags().nymphoEncountered) {
				if(Main.game.getDialogueFlags().nymphoPacified) {
					return "<p>"
							+ "Deciding to pay [harpyNympho.Name] another visit, you set off towards the flash of pink in the centre of the nest."
							+ " As you make your way towards the sex-obsessed matriarch, you get a good look at the harpies that make up her flock."
							+ " Just as you've seen before, the vast majority of them are rather plain-looking by harpy standards; revealing them as males."
						+ "</p>"
						+ "<p>"
							+ "As you pass the groups of loitering harpies, you overhear snippets of conversation,"
							+ "[style.speechFeminine(...she let me unload in her three times last night...)]</br>"
							+ "[style.speechFeminine(...I love it when she's in the mood to suck cock...)]</br>"
							+ "[style.speechFeminine(...and then she cleaned me off with her mouth...)]</br>"
						+ "</p>"
						+ "<p>"
							+ "Smirking as you push through the crowds, you soon find yourself standing before the harpy they're all talking about."
							+ " Moaning and squealing in delight, [harpyNympho.name] is once again riding her favourite boy-toy, [harpyNymphoCompanion.name]."
							+ " As you come to a halt in front of them, they both suddenly let out an exceptionally loud scream, and you look down to see a slick trail of fresh cum oozing out of [harpyNympho.name]'s pussy."
							+ " After taking a few moments to catch her breath, she lifts herself up, allowing the hot load to drool out of her well-used cunt, and, leaning down to deliver a passionate kiss on her partner's lips, she calls out,"
							+ " [harpyNympho.speech(Who's next?!)]"
						+ "</p>"
						+ "<p>"
							+ "Stepping forwards, you make yourself known,"
							+ " [pc.speech(I think that would be me!)]"
						+ "</p>"
						+ "<p>"
							+ "Upon hearing your voice, [harpyNympho.name] spins round, letting out an exited squeal,"
							+ " [harpyNympho.speech("+(Main.game.getPlayer().isFeminine()?"Mistress":"Master")+"! You're back! Let's have some fun!)]"
						+ "</p>"
						+ "<p>"
							+ "Rushing towards you, [harpyNympho.name] falls to her knees."
							+ " Shuffling forwards, she looks up into your [pc.eyes],"
							+ " [harpyNympho.speech(Please! "+(Main.game.getPlayer().isFeminine()?"Mistress":"Master")+"! Let's fuck!)]"
						+ "</p>";
					
				} else {
					return "<p>"
							+ "Deciding to pay [harpyNympho.Name] another visit, you set off towards the flash of pink in the centre of the nest."
							+ " As you make your way towards the sex-obsessed matriarch, you get a good look at the harpies that make up her flock."
							+ " Just as you've seen before, the vast majority of them are rather plain-looking by harpy standards; revealing them as males."
						+ "</p>"
						+ "<p>"
							+ "As you pass the groups of loitering harpies, you overhear snippets of conversation,"
							+ "[style.speechFeminine(...she let me unload in her three times last night...)]</br>"
							+ "[style.speechFeminine(...I love it when she's in the mood to suck cock...)]</br>"
							+ "[style.speechFeminine(...and then she cleaned me off with her mouth...)]</br>"
						+ "</p>"
						+ "<p>"
							+ "Pushing through the crowds, you soon find yourself standing before the harpy they're all talking about."
							+ " Moaning and squealing in delight, [harpyNympho.name] is once again riding her favourite boy-toy, [harpyNymphoCompanion.name]."
							+ " As you come to a halt in front of them, they both suddenly let out an exceptionally loud scream, and you look down to see a slick trail of fresh cum oozing out of [harpyNympho.name]'s pussy."
							+ " After taking a few moments to catch her breath, she lifts herself up, allowing the hot load to drool out of her well-used cunt, and, leaning down to deliver a passionate kiss on her partner's lips, she calls out,"
							+ " [harpyNympho.speech(Who's next?!)]"
						+ "</p>"
						+ "<p>"
							+ "Stepping forwards, you make yourself known,"
							+ " [pc.speech(I think that would be me!)]"
						+ "</p>"
						+ "<p>"
							+ "Upon hearing your voice, [harpyNympho.name] spins around, and, catching sight of you, she lets out a frustrated sigh,"
								+ (Main.game.getPlayer().getFemininityValue()>Femininity.FEMININE_STRONG.getMinimumFemininity()
									?" [harpyNympho.speech(Eugh! It's <i>you</i> again! You'd better not be trying to steal any of my playthings!)]"
									:" [harpyNympho.speech(Eugh! It's <i>you</i> again?! What do you want this time?!)]")
						+ "</p>"
						+ "<p>"
							+ "Stepping forwards, [harpyNymphoCompanion.name] moves to protect her matriarch,"
							+ " [harpyNymphoCompanion.speech(You heard her! What do you want?!)]"
						+ "</p>";
				}
			
			} else {
				return "<p>"
						+ "Recognising this as one of the nests that you agreed to pacify, you set off towards the flash of pink in the centre of the nest."
						+ " As you make your way across the crowded platform, you get a good look at the harpies that make up this flock."
						+ " Just as you saw from the outskirts, the vast majority of them are rather plain-looking by harpy standards; revealing them as males."
					+ "</p>"
					+ "<p>"
						+ "As you pass the groups of loitering harpies, you overhear snippets of conversation,"
						+ "[style.speechFeminine(...she let me unload in her three times last night...)]</br>"
						+ "[style.speechFeminine(...I love it when she's in the mood to suck cock...)]</br>"
						+ "[style.speechFeminine(...and then she cleaned me off with her mouth...)]</br>"
					+ "</p>"
					+ "<p>"
						+ "Pushing through the crowds, you soon find yourself standing before the harpy they're all talking about."
						+ " Moaning and squealing in delight, you see the matriarch of this nest unashamedly riding one of her boy-toys."
						+ " As you come to a halt in front of them, they both suddenly let out an exceptionally loud scream, and you look down to see a slick trail of fresh cum oozing out of the pink-feathered matriarch's pussy."
						+ " After taking a few moments to catch her breath, she lifts herself up, allowing the hot load to drool out of her well-used cunt, and, leaning down to deliver a passionate kiss on her partner's lips, she moans,"
						+ " [harpyNympho.speech(Ah, [harpyNymphoCompanion.Name]! I love your cock most of all!)]"
					+ "</p>"
					+ "<p>"
						+ "[harpyNymphoCompanion.speech(I love making you happy [harpyNympho.Name],)] [harpyNymphoCompanion.Name] replies,"
						+ " [harpyNymphoCompanion.speech(do you want to go again?)]"
					+ "</p>"
					+ "<p>"
						+ "Deciding that this is an opportune moment to make yourself known, you step forwards and clear your throat,"
						+ " [pc.speech(Before you do, I was hoping to talk with you for a moment.)]"
					+ "</p>"
					+ "<p>"
						+ "Upon hearing your voice, [harpyNympho.name] spins around, and, catching sight of you, she lets out a frustrated sigh,"
							+ (Main.game.getPlayer().getFemininityValue()>Femininity.FEMININE_STRONG.getMinimumFemininity()
								?" [harpyNympho.speech(Who are you?! What are you doing here?! You'd better not be trying to steal any of my playthings!)]"
								:" [harpyNympho.speech(Who are you?! What are you doing here?! What do you want?!)]")
					+ "</p>"
					+ "<p>"
						+ "Stepping forwards, [harpyNymphoCompanion.name] moves to protect her matriarch,"
						+ " [harpyNymphoCompanion.speech(You heard her! What do you want?!)]"
					+ "</p>";
			}
		}

		@Override
		public Response getResponse(int index) {
			if(Main.game.getDialogueFlags().nymphoPacified) {
				if (index == 1) {
					return new ResponseSex("Sex", "Have dominant sex with [harpyNympho.name].", HARPY_NEST_NYMPHO_APPROACH,
							true, false, Main.game.getHarpyNympho(), new SMHarpyStanding(), HARPY_NEST_NYMPHO_AFTER_SEX,
							"<p>"
								+ "Eager to help satisfy [harpyNympho.name]'s craving for sex, you reach down and grab her wings."
								+ " Pulling her to her feet, you step forwards, before planting a deep kiss on her [harpyNympho.lips+]."
							+ "</p>"
							+ "<p>"
								+ "[harpyNympho.Name] responds to your dominant move by letting out a excited squeal, and, enthusiastically wrapping her wings around your back, she passionately starts returning your kiss..."
							+ "</p>");
						
				} else if (index == 0) {
					return new Response("Leave", "Tell [harpyNympho.name] that you'll be back later.", HARPY_NEST_NYMPHO) {
						@Override
						public void effects() {
							Main.game.getTextStartStringBuilder().append(
									"<p>"
										+ "Deciding that you don't want to talk to [harpyNympho.name] right now, you turn around and take your leave."
										+ " You hear [harpyNympho.name] sighing to [harpyNymphoCompanion.name] as you walk away,"
										+ " [harpyNympho.speech(Aww! Well, looks like you get to have another go!)]"
									+ "</p>"
									+ "<p>"
										+ "Ignoring her words, you continue back across the platform, quickly finding yourself on the outskirts of the nest once again."
									+ "</p>");
						}
					};
						
				} else {
					return null;
				}
				
			} else {
				if (index == 1) {
					return new Response("Talk", "Try to convince [harpyNympho.name] to calm down.", HARPY_NEST_NYMPHO_TALK) {
						@Override
						public void effects() {
							Main.game.getDialogueFlags().nymphoEncountered = true;
						}
					};
						
				} else if (index == 2) {
					return new Response("Nympho Queen", "You feel sorry for this matriarch, only getting to have sex with the same males over and over again. Tell her how a real nympho behaves!", HARPY_NEST_NYMPHO_QUEEN,
							null, null, Util.newArrayListOfValues(new ListValue<>(Perk.NYMPHOMANIAC)), Femininity.FEMININE_STRONG, null) {
						@Override
						public void effects() {
							Main.game.getDialogueFlags().nymphoEncountered = true;
							Main.game.getDialogueFlags().nymphoPacified = true;
							Main.game.getTextEndStringBuilder().append(Main.game.getPlayer().addItem(AbstractItemType.generateItem(ItemType.HARPY_MATRIARCH_NYMPHO_LOLLIPOP), false));
						}
						@Override
						public QuestLine getQuestLine() {
							return QuestLine.SIDE_HARPY_PACIFICATION;
						}
					};
						
				} else if (index == 3) {
					return new Response("Call her ugly", "You know that this would be a terrible idea...", HARPY_NEST_NYMPHO_UGLY) {
						@Override
						public void effects() {
							Main.game.getDialogueFlags().nymphoEncountered = true;
						}
						@Override
						public boolean isCombatHighlight() {
							return true;
						}
					};
	
				} else if (index == 0) {
					return new Response("Leave", "Tell [harpyNympho.name] that you'll be back later.", HARPY_NEST_NYMPHO) {
						@Override
						public void effects() {
							Main.game.getDialogueFlags().nymphoEncountered = true;
							Main.game.getTextStartStringBuilder().append("");
						}
					};
						
				} else {
					return null;
				}
			}
		}
	};
	
	public static final DialogueNodeOld HARPY_NEST_NYMPHO_TALK = new DialogueNodeOld("Harpy nest", ".", true) {
		private static final long serialVersionUID = 1L;

		@Override
		public String getLabel() {
			return "[harpyNympho.Name]'s nest";
		}
		
		@Override
		public String getContent() {
			return "<p>"
						+ "[pc.speech(What I want,)] you say,"
						+ " [pc.speech(is for you to get your nest under control. The enforcers are struggling to keep the peace, and it's members of your nest who are responsible!)]"
					+ "</p>"
					+ "<p>"
						+ "[harpyNympho.speech(Well it's not <i>my</i> fault if some of my playthings want to have a bit of extra fun elsewhere!)] [harpyNympho.name] replies,"
						+ " [harpyNympho.speech(And anyway, you're kind of interrupting things here, so how about you turn around and go bother someone else! [harpyNymphoCompanion.Name]! Why don't you show this rude [pc.race] the way out!)]"
					+ "</p>"
					+ "<p>"
						+ "As [harpyNymphoCompanion.name] moves forwards, you realise that [harpyNympho.name] isn't going to listen to you."
						+ " You'll either have to think of another way to convince these harpies to calm down, or make them calm down by using force."
					+ "</p>";
		}

		@Override
		public Response getResponse(int index) {
			if (index == 1) {
				return new Response("Nympho Queen", "You feel sorry for this matriarch, only getting to have sex with the same males over and over again. Tell her how a real nympho behaves!", HARPY_NEST_NYMPHO_QUEEN,
						null, null, Util.newArrayListOfValues(new ListValue<>(Perk.NYMPHOMANIAC)), Femininity.FEMININE_STRONG, null) {
					@Override
					public void effects() {
						Main.game.getDialogueFlags().nymphoPacified = true;
						Main.game.getTextEndStringBuilder().append(Main.game.getPlayer().addItem(AbstractItemType.generateItem(ItemType.HARPY_MATRIARCH_NYMPHO_LOLLIPOP), false));
					}
					@Override
					public QuestLine getQuestLine() {
						return QuestLine.SIDE_HARPY_PACIFICATION;
					}
				};
					
			} else if (index == 2) {
				return new Response("Force compliance", "If you want these harpies to chill out, it looks as though you'll have to do it by force...", HARPY_NEST_NYMPHO_FIGHT) {
					@Override
					public boolean isCombatHighlight() {
						return true;
					}
				};
					
			} else if (index == 0) {
				return new Response("Leave", "Tell [harpyNympho.name] that you'll be back later.", HARPY_NEST_NYMPHO) {
					@Override
					public void effects() {
						Main.game.getTextStartStringBuilder().append(
								"<p>"
									+ "Deciding that now isn't the best time to start a confrontation, you turn around and take your leave."
									+ " You hear [harpyNympho.name] complaining to [harpyNymphoCompanion.name] as you walk away,"
									+ " [harpyNympho.speech(Well that was a waste of our time! Come on [harpyNymphoCompanion.name], this time I want you to use my mouth!)]"
								+ "</p>"
								+ "<p>"
									+ "Ignoring her words, you continue back across the platform, quickly finding yourself on the outskirts of the nest once again."
								+ "</p>");
					}
				};
					
			} else {
				return null;
			}
		}
	};
	
	public static final DialogueNodeOld HARPY_NEST_NYMPHO_UGLY = new DialogueNodeOld("Harpy nest", ".", true) {
		private static final long serialVersionUID = 1L;

		@Override
		public String getLabel() {
			return "[harpyNympho.Name]'s nest";
		}
		
		@Override
		public String getContent() {
			return "<p>"
						+ "Realising that you're going to have to force [harpyNympho.name] to get her nest under control, you decide to draw her into a confrontation,"
						+ " [pc.speech(Well, if you aren't going to calm your nest down, can you at least agree to put up some walls around your nest?"
						+ " It's not nice having to see a harpy as ugly as you every time I walk past your nest.)]" //Innoxia's next-level witticisms!
					+ "</p>"
					+ "<p>"
						+ "[harpyNympho.Name] lets out a furious scream, and, flapping her wings, she shouts out to her companion, "
						+ "[harpyNympho.speech([harpyNymphoCompanion.Name]! Get [pc.herHim]! Get [pc.herHim]! That fucker! [pc.She] said I was ugly!)]"
					+ "</p>"
					+ "<p>"
						+ "Looking almost as furious as [harpyNympho.name] does, [harpyNymphoCompanion.name] jumps forwards, eager to please her matriarch as she launches into a furious assault."
					+ "</p>";
		}

		@Override
		public Response getResponse(int index) {
			if (index == 1) {
				return new ResponseCombat("Fight", "[harpyNymphoCompanion.Name] rushes to do her matriarch's bidding!", HARPY_NEST_NYMPHO_UGLY, Main.game.getHarpyNymphoCompanion());
					
			} else {
				return null;
			}
		}
	};
	
	public static final DialogueNodeOld HARPY_NEST_NYMPHO_QUEEN = new DialogueNodeOld("Harpy nest", ".", true) {
		private static final long serialVersionUID = 1L;

		@Override
		public String getLabel() {
			return "[harpyNympho.Name]'s nest";
		}
		
		@Override
		public String getContent() { // Mock her for only having one partner at once, she gets flustered, you describe multiple harpies fucking her, she breaks and falls to her knees admitting that you're better than she is
			return "<p>"
					+ "[pc.speech(So, do you only fuck one of your little boy-toys here at a time?)] you ask in a derisory tone,"
					+ " [pc.speech(Because you know you could be doing a lot more than that, right?)]"
				+ "</p>"
				+ "<p>"
					+ "For a moment, [harpyNympho.name] seems to be caught off guard, and stumbles over her words as she responds,"
					+ " [harpyNympho.speech(W-Well, I mean, I like to make each harpy feel special!)]"
				+ "</p>"
				+ "<p>"
					+ "The murmurs of conversation that had been surrounding you up until this point start to fade away, and you realise that the crowds of male harpies all around you are listening in closely."
					+ " From the moment you stepped foot inside [harpyNympho.name]'s nest, you could feel her flock's gaze resting on your beautiful figure, and now, loudly critiquing their matriarch's sexual skills,"
						+ " you're getting their full attention."
				+ "</p>"
				+ "<p>"
					+ "Striking the most sensual pose you can muster, you continue,"
					+ " [pc.speech(You know, you're hoarding all these gorgeous harpies here for your own personal use, but you're only willing to fuck them one at a time?!"
					+ " That's kind of unfair, don't you all agree?)]"
				+ "</p>"
				+ "<p>"
					+ "Turning to address the crowd as you ask that last question, the entire flock responds by calling out in agreement."
					+ " Your beautiful features and potent arcane aura are obviously having a strong effect on these harpies, which, combined with talking about sex in such an open manner, has turned them over to your side."
				+ "</p>"
				+ "<p>"
					+ "[harpyNympho.speech(But, w-what should I be doing then?)] [harpyNympho.name] nervously asks,"
					+ " [harpyNympho.speech(I didn't realise I was being unfair...)]"
				+ "</p>"
				+ "<p>"
					+ "[pc.speech(Oh, there's lots of things you could be doing,)] you say, walking closer to her,"
					+ " [pc.speech(like having your pussy, ass and mouth all used at the same time. Or allowing your nest to run a train on your slutty little cunt here.)]"
				+ "</p>"
				+ "<p>"
					+ "As you say that, you reach down and grab [harpyNympho.name]'s exposed pussy."
					+ " She lets out a little squeak, and, leaning into you, moans,"
					+ " [harpyNympho.speech(Yes... I'll do that... What else?)]"
				+ "</p>"
				+ "<p>"
					+ "[pc.speech(I think it's obvious by now what you need to do,)] you moan into her ear,"
					+ " [pc.speech(kneel down and accept that I'm the one in charge here.)]"
				+ "</p>"
				+ "<p>"
					+ "Realising that her flock has been mesmerised by your words, [harpyNympho.name] falls down onto her knees before you."
					+ " Looking up with big, lust-filled eyes, she moans, [harpyNympho.speech("+(Main.game.getPlayer().isFeminine()?"Mistress":"Master")+", please teach me!"
							+ " I'll calm the flock down for you! Please "+(Main.game.getPlayer().isFeminine()?"Mistress":"Master")+", tell me what to do!)]"
				+ "</p>"
				+ "<p>"
					+ "You can't help but let out a little laugh as the matriarch submits to you,"
					+ " [pc.speech(Good girl! You'll do exactly as I say from now on, understood?!)]"
				+ "</p>"
				+ "<p>"
					+ "[harpyNympho.Name] shuffles closer, continuing to let out little moaning noises as she responds,"
					+ " [harpyNympho.speech(Yes "+(Main.game.getPlayer().isFeminine()?"Mistress":"Master")+"! I'll do what you say!"
							+ " Please, "+(Main.game.getPlayer().isFeminine()?"Mistress":"Master")+", I have something for you!)]"
				+ "</p>"
				+ "<p>"
					+ "Producing a cock-shaped lollipop, [harpyNympho.name] holds it out towards you,"
					+ " [harpyNympho.speech("+(Main.game.getPlayer().isFeminine()?"Mistress":"Master")+", this will make you look like me! I-If you want to look like me, that is...)]"
					+ " You take the lollipop in recognition of [harpyNympho.name]'s submission, but you're unsure whether you'll actually use it..."
				+ "</p>"
				+ "<p>"
					+ "[pc.speech(Good girl!)] you say, [pc.speech(Now, you're going to get your nest to calm down, aren't you?)]"
				+ "</p>"
				+ "<p>"
					+ "[harpyNympho.speech(Yes "+(Main.game.getPlayer().isFeminine()?"Mistress":"Master")+"! I'll be able to relieve all their stress if I start letting them use me all at once!)]"
				+ "</p>"
				+ "<p>"
					+ "Thanks to your own obsession with sex, your powerful arcane aura, and your good looks, you've been able to subdue [harpyNympho.name]'s nest without fighting!"
					+ " Looking down at the still-moaning matriarch, you wonder if you should give her some one-to-one tuition..."
				+ "</p>";
		}

		@Override
		public Response getResponse(int index) {
			 if (index == 1) {
				return new ResponseSex("Sex", "Have dominant sex with [harpyNympho.name].", HARPY_NEST_NYMPHO_QUEEN,
						true, false, Main.game.getHarpyNympho(), new SMHarpyStanding(), HARPY_NEST_NYMPHO_AFTER_SEX,
						"<p>"
							+ "Eager to help satisfy [harpyNympho.name]'s craving for sex, you reach down and grab her wings."
							+ " Pulling her to her feet, you step forwards, before planting a deep kiss on her [harpyNympho.lips+]."
						+ "</p>"
						+ "<p>"
							+ "[harpyNympho.Name] responds to your dominant move by letting out a excited squeal, and, enthusiastically wrapping her wings around your back, she passionately starts returning your kiss..."
						+ "</p>");
						
			} else if (index == 0) {
				return new Response("Leave", "Decide to take your leave.", HARPY_NEST_NYMPHO) {
					@Override
					public void effects() {
						Main.game.getTextStartStringBuilder().append(
								"<p>"
									+ "Deciding that your work here is done, you turn around and start to walk back the way you came."
									+ " You hear [harpyNympho.name] calling out to her harpies as you leave,"
									+ " [harpyNympho.speech(You heard our new leader! Let's try starting with three of you at once!)]"
								+ "</p>"
								+ "<p>"
									+ "Smirking as you hear that, you continue back across the main platform, quickly finding yourself on the outskirts of the nest once again."
								+ "</p>");
					}
				};
					
			} else {
				return null;
			}
		}
	};
	
	public static final DialogueNodeOld HARPY_NEST_NYMPHO_FIGHT = new DialogueNodeOld("Harpy nest", ".", true) {
		private static final long serialVersionUID = 1L;

		@Override
		public String getLabel() {
			return "[harpyNympho.Name]'s nest";
		}
		
		@Override
		public String getContent() {
			return "<p>"
					+ "Realising that [harpyNympho.name] isn't going to listen to reason, you make your final demand,"
					+ " [pc.speech(You're either going to get your nest to calm down right now, or I'm going to make you!)]"
				+ "</p>"
				+ "<p>"
					+ "[harpyNympho.Name] lets out an angry cry, and she shouts out to her companion,"
					+ " [harpyNympho.speech([harpyNymphoCompanion.Name], throw this [pc.race] out already! <i>Nobody</i> talks to me like that and gets away with it!)]"
				+ "</p>"
				+ "<p>"
					+ "[harpyNymphoCompanion.name] immediately runs forwards, eager to please her matriarch as she launches into a furious assault."
				+ "</p>";
		}

		@Override
		public Response getResponse(int index) {
			if (index == 1) {
				return new ResponseCombat("Fight", "[harpyNymphoCompanion.Name] rushes to do her matriarch's bidding!", HARPY_NEST_NYMPHO_UGLY, Main.game.getHarpyNymphoCompanion());
					
			} else {
				return null;
			}
		}
	};
	
	public static final DialogueNodeOld HARPY_NEST_NYMPHO_FIGHT_LOSE = new DialogueNodeOld("Harpy nest", ".", true) {
		private static final long serialVersionUID = 1L;

		@Override
		public String getLabel() {
			return "[harpyNympho.Name]'s nest";
		}
		
		@Override
		public String getContent() {
			return "<p>"
					+ "You fall to the floor, totally beaten."
					+ " As you collapse, [harpyNymphoCompanion.name] jumps down on top of you, pinning you to the floor as she calls out to her matriarch,"
					+ " [harpyNymphoCompanion.speech([harpyNympho.Name]! I did it!)]"
				+ "</p>"
				+ "<p>"
					+ "[harpyNympho.speech(Well done [harpyNymphoCompanion.name]!)] you hear [harpyNympho.name] respond,"
					+ " [harpyNympho.speech(You're going to get a reward for this!)]"
				+ "</p>"
				+ "<p>"
					+ "As [harpyNymphoCompanion.name] continues holding you down, [harpyNympho.name] calls out for her flock to come and help."
					+ " Rushing to obey their matriarch, several harpies dash forwards and pin your [pc.arms] and [pc.legs] to the floor."
				+ "</p>"
				+ "<p>"
					+ "[harpyNympho.speech(Keep on holding [pc.herHim] still!)] [harpyNympho.name] calls out, pulling [harpyNymphoCompanion.name] off of you,"
					+ " [harpyNympho.speech(I've got a fun little punishment planned!)]"
				+ "</p>";
		}

		@Override
		public Response getResponse(int index) {
			if (index == 1) {
				return new Response("Punished", "[harpyNympho.Name] proceeds with her punishment...", HARPY_NEST_NYMPHO_FIGHT_LOSE_PUNISHMENT){
					@Override
					public void effects() {
						if(Main.game.isForcedTFEnabled()) {
							Main.game.getTextEndStringBuilder().append(ItemEffectType.NYMPHO_LOLLIPOP.applyEffect(null, null, null, 0, Main.game.getHarpyNympho(), Main.game.getPlayer()));
						}
					}
				};
					
			} else {
				return null;
			}
		}
	};
	
	public static final DialogueNodeOld HARPY_NEST_NYMPHO_FIGHT_BEAT_BF = new DialogueNodeOld("Harpy nest", ".", true) {
		private static final long serialVersionUID = 1L;

		@Override
		public String getLabel() {
			return "[harpyNympho.Name]'s nest";
		}
		
		@Override
		public String getContent() {
			return "<p>"
					+ "[harpyNympho.Name] lets out an angry wail as [harpyNymphoCompanion.name] falls to the floor, defeated."
					+ " You notice a lot of the surrounding harpies glancing nervously at each other, and a few start to shuffle around to your side of the platform."
					+ " It looks as though they're getting ready to support you if you manage to defeat their matriarch."
				+ "</p>"
				+ "<p>"
					+ "You don't have too much time to ponder on these harpies' fickle nature, as [harpyNympho.Name] suddenly rushes forwards, shouting,"
					+ " [harpyNympho.speech(You're going to pay for this!)]"
				+ "</p>";
		}

		@Override
		public Response getResponse(int index) {
			if (index == 1) {
				return new ResponseCombat("Fight", "[harpyNympho.Name] looks furious as she launches her attack on you!", HARPY_NEST_NYMPHO_FIGHT_BEAT_BF, Main.game.getHarpyNympho());
					
			} else {
				return null;
			}
		}
	};
	
	public static final DialogueNodeOld HARPY_NEST_NYMPHO_FIGHT_LOSE_TO_MATRIARCH = new DialogueNodeOld("Harpy nest", ".", true) {
		private static final long serialVersionUID = 1L;

		@Override
		public String getLabel() {
			return "[harpyNympho.Name]'s nest";
		}
		
		@Override
		public String getContent() {
			return "<p>"
					+ "You fall to the floor, totally beaten."
					+ " As you collapse, you hear [harpyNympho.name] ordering her flock to retrain you, and, rushing to obey their matriarch, you're quickly pinned to the floor by a group of her obedient harpies."
				+ "</p>"
				+ "<p>"
					+ "[harpyNympho.speech([nymphoHarpyCompanion.name], are you ok?!)] you hear [nymphoHarpy.name] asking her boy-toy,"
					+ " [harpyNympho.speech(Come on, I'll make you feel better!)]"
				+ "</p>"
				+ "<p>"
					+ "As the members of [harpyNympho.name]'s flock continue to hold you down, you hear the matriarch and her companion walking towards you."
					+ " Some of the harpies start laughing as you try to wriggle free, but you're too weak from the fight to offer any real resistance."
				+ "</p>"
				+ "<p>"
					+ "[harpyNympho.speech(Keep on holding [pc.herHim] still!)] [harpyNympho.name] calls out,"
					+ " [harpyNympho.speech(I've got a fun little punishment planned!)]"
				+ "</p>";
		}

		@Override
		public Response getResponse(int index) {
			if (index == 1) {
				return new Response("Punished", "[harpyNympho.Name] proceeds with her punishment...", HARPY_NEST_NYMPHO_FIGHT_LOSE_PUNISHMENT){
					@Override
					public void effects() {
						if(Main.game.isForcedTFEnabled()) {
							Main.game.getTextEndStringBuilder().append(ItemEffectType.NYMPHO_LOLLIPOP.applyEffect(null, null, null, 0, Main.game.getHarpyNympho(), Main.game.getPlayer()));
						}
					}
				};
					
			} else {
				return null;
			}
		}
	};
	
	public static final DialogueNodeOld HARPY_NEST_NYMPHO_FIGHT_BEAT_NYMPHO = new DialogueNodeOld("Harpy nest", ".", true) {
		private static final long serialVersionUID = 1L;

		@Override
		public String getLabel() {
			return "[harpyNympho.Name]'s nest";
		}
		
		@Override
		public String getContent() {
			return "<p>"
					+ "[harpyNympho.Name]'s flock, just a moment ago cheering and shouting support for their matriarch, falls completely silent as they see her slump to the floor, defeated."
					+ " Walking forwards, you tower over the panting mass of pink feathers, and you hear an erotic moan slip out from between [harpyNympho.name]'s lips as she pushes herself up onto her knees,"
					+ " [harpyNympho.speech(Y-You're so... powerful...)]"
				+ "</p>"
				+ "<p>"
					+ "[pc.speech(That's right,)] you respond,"
					+ " [pc.speech(I don't care how you normally determine a flock's leader up here in the Nests; I'm in charge now!)]"
				+ "</p>"
				+ "<p>"
					+ "Your arcane aura is clearly having a strong effect on [harpyNympho.name], and you hear her letting out another lewd moan as she responds,"
					+ " [harpyNympho.speech(Y-Yes "+(Main.game.getPlayer().isFeminine()?"mistress":"master")+"! I'll do as you ask!)]"
				+ "</p>"
				+ "<p>"
					+ "Seeing their matriarch submit to you, the rest of the harpies fall to their knees, bowing down to you as you issue your orders,"
					+ " [pc.speech(You're all going to calm down, understand?! No more feuds, and no more attacking travellers through the Harpy Nests!)]"
				+ "</p>"
				+ "<p>"
					+ "A chorus of eager agreements rise up from the surrounding crowd, and you find yourself smirking as you look back down at the defeated matriarch at your feet."
					+ " Shuffling closer, [harpyNympho.name] holds up a pink cock-shaped lollipop,"
					+ " [harpyNympho.speechNoEffects("+(Main.game.getPlayer().isFeminine()?"Mistress":"Master")+"! Please, have one of my special lollipops! This will make you look like me! I-If you want to look like me, that is...)]"
					+ " You take the lollipop in recognition of [harpyNympho.name]'s submission, but you're unsure whether you'll actually use it..."
				+ "</p>"
				+ "<p>"
					+ "[pc.speech(Good girl!)] you say, [pc.speech(Now, you're going to get your nest to calm down, aren't you?)]"
				+ "</p>"
				+ "<p>"
					+ "[harpyNympho.speech(Yes "+(Main.game.getPlayer().isFeminine()?"Mistress":"Master")+"! I'll keep them under control!)]"
				+ "</p>"
				+ "<p>"
					+ "Thanks to your victory over [harpyNympho.name], and the power of your arcane aura, you've been able to subdue this once-troublesome nest!"
					+ " Looking down at the matriarch, you wonder if you should publicly prove to all these harpies who's in control here..."
				+ "</p>";
		}

		@Override
		public Response getResponse(int index) {
			 if (index == 1) {
				return new ResponseSex("Sex", "Have dominant sex with [harpyNympho.name].", HARPY_NEST_NYMPHO_FIGHT_BEAT_NYMPHO,
						true, false, Main.game.getHarpyNympho(), new SMHarpyStanding(), HARPY_NEST_NYMPHO_AFTER_SEX,
						"<p>"
							+ "Eager to show this nest who's in charge, you reach down and grab [harpyNympho.name]'s wings."
							+ " Pulling her to her feet, you step forwards, before planting a deep kiss on her [harpyNympho.lips+]."
						+ "</p>"
						+ "<p>"
							+ "[harpyNympho.Name] responds to your dominant move by letting out a excited squeal, and, enthusiastically wrapping her wings around your back, she passionately starts returning your kiss..."
						+ "</p>");
							
				} else if (index == 0) {
					return new Response("Leave", "Decide to take your leave.", HARPY_NEST_NYMPHO) {
						@Override
						public void effects() {
							Main.game.getTextStartStringBuilder().append(
								"<p>"
									+ "Deciding that your work here is done, you turn around and start to walk back the way you came."
									+ " You hear [harpyNympho.name] calling out to her harpies as you leave,"
									+ " [harpyNympho.speech(That's our new leader now! Show some respect!)]"
								+ "</p>"
								+ "<p>"
									+ "Smirking as you hear that, you continue back across the main platform, quickly finding yourself on the outskirts of the nest once again."
								+ "</p>");
					}
				};
					
			} else {
				return null;
			}
		}
	};
	
	public static final DialogueNodeOld HARPY_NEST_NYMPHO_FIGHT_LOSE_PUNISHMENT = new DialogueNodeOld("Harpy nest", ".", true) {
		private static final long serialVersionUID = 1L;

		@Override
		public String getLabel() {
			return "[harpyNympho.Name]'s nest";
		}
		
		@Override
		public String getContent() {
			if(Main.game.isForcedTFEnabled()) {
				return "<p>"
						+ "[harpyNympho.speech([harpyNymphoCompanion.name], fetch me one of my lollipops!)]"
						+ " [harpyNympho.name] laughs, stepping forwards to tower over you,"
						+ " [harpyNympho.speech(I know what your problem is; you just wish you were me! Well, my special little lollipops can help with that!)]"
					+ "</p>"
					+ "<p>"
						+ "[pc.speechNoEffects(Let me go!)] you shout, struggling against the harpies holding you down."
						+ " You're too weak to shake them off, however, and they easily continue to pin you to the floor, holding you quite still as [harpyNympho.name] stoops down next to your face."
					+ "</p>"
					+ "<p>"
						+ "[harpyNympho.speech(Shhh! Don't struggle so much,)]"
						+ " she moans,"
						+ " [harpyNympho.speech(this will all be over soon!)]"
					+ "</p>"
					+ "<p>"
						+ "[harpyNymphoCompanion.name] comes running over, and you see her hand a pink, cock-shaped lollipop to [harpyNympho.name]."
						+ " With a mischievous grin on her face, the pink-feathered matriarch orders her boy-toy to hold your mouth open."
						+ " Not being able to break free, you're left powerless as [harpyNymphoCompanion.name] forces you to open your mouth, and [harpyNympho.name] instantly shoves the lollipop past your lips."
					+ "</p>"
					+ "<p>"
						+ "[harpyNympho.speech(Good [pc.girl]! You like it?!)]"
					+ "</p>"
					+ "<p>"
						+ "You want to make a response, but the intense, sweet flavour of the lollipop is all you can focus on."
						+ " Before you know what you're doing, you're eagerly sucking and licking on the delicious candy, letting out little whining noises as you find yourself unable to stop..."
					+ "</p>";
				
			} else {
				return "<p>"
						+ "[harpyNympho.speech(Come on [harpyNymphoCompanion.name]! Let's carry on from where we left off!)]"
						+ " [harpyNympho.name] laughs, dropping down onto all fours directly over your face,"
						+ " [harpyNympho.speech(This naughty [pc.girl] can be on cleanup duty!)]"
					+ "</p>"
					+"<p>"
						+ "[harpyNymphoCompanion.name] lets out a little laugh,"
						+ " [harpyNymphoCompanion.speech(That sounds fun!)]"
					+ "</p>"
					+ "<p>"
						+ "With [harpyNympho.name]'s pussy hovering above your face, you're held in position and forced to watch as [harpyNymphoCompanion.name] lifts up her skirt and pulls down her underwear."
						+ " Stepping forwards, you see her tiny little avian cock standing to attention, and with a thrust of her hips, she slides into [harpyNympho.name]'s wet cunt."
						+ " Some of the cum from [harpyNympho.name]'s previous creampie oozes out around [harpyNymphoCompanion.name]'s little cock, and you flinch as the salty jizz drips down onto your face."
					+ "</p>"
					+ "<p>"
						+ "For the next few hours, you're forced to act as [harpyNympho.name]'s personal pussy-cleaner."
						+ " One after another, the harpies of [harpyNympho.name]'s flock take turns fucking their sex-crazed matriarch."
						+ " She insists that each one finish inside of her, and, after receiving each creampie, she lowers herself down onto your face, forcing you to lick her clean before the next excited harpy's turn."
					+ "</p>"
					+ "<p>"
						+ "You eventually lose count of how many times you're forced to endure this humiliation, but, after what seems like an eternity, [harpyNympho.name] declares that she needs a break."
						+ " Standing up, she orders her harpies to release you, before grinning down at your cum-coated face,"
						+ " [harpyNympho.speech(I think you've learnt your lesson, haven't you?! Now, get out my sight!)]"
					+ "</p>";
			}
		}

		@Override
		public Response getResponse(int index) {
			if (index == 1) {
				if(Main.game.isForcedTFEnabled()) {
					return new Response("Transformed...", "Having had their fun, you're quickly thrown out of the nest.", HARPY_NEST_NYMPHO) {
						@Override
						public void effects() {
							Main.game.getTextStartStringBuilder().append(
									"<p>"
										+ "As the lollipop's transformative effects come to an end, the harpies' grip on your [pc.arms] and [pc.legs] loosens."
										+ " Blinking slowly a few times, you let out a lewd moan,"
										+ " [pc.speech(Oh, fuck! I need to fuck something, right now!)]"
									+ "</p>"
									+ "<p>"
										+ "The flock of harpies bursts out into laughter, and the ones who were holding you down finally release you before backing off."
										+ " Scrambling to your knees, you find yourself looking up at [harpyNympho.Name] as she continues towering over you."
									+ "</p>"
									+ "<p>"
										+ "[harpyNympho.speech(Well, you're not taking any of <i>my</i> playthings! Now, once you apologise, you can run off and get fucked in some other place!)]"
									+ "</p>"
									+ "<p>"
										+ "With the effects of the lollipop still lingering in your mind, you find yourself blurting out,"
										+ " [pc.speech(I'm sorry! Please, I need to go!)]"
									+ "</p>"
									+ "<p>"
										+ "[harpyNympho.Name] grins down at you, and, to the background noise of dozens of harpies laughing at you, she shouts,"
										+ " [harpyNympho.speech(Good girl! Now go enjoy your new body!)]"
									+ "</p>"
									+ "<p>"
										+ "Scrambling to your feet, you do as [harpyNympho.name] commands."
										+ " You run away across the platform, leaving the harpies' mocking laughter behind as you quickly find yourself on the outskirts of the nest once again..."
									+ "</p>"
									+ "</br>");
						}
					};
					
				} else {
					return new Response("Thrown out", "Having had their fun, you're quickly thrown out of the nest.", HARPY_NEST_NYMPHO) {
						@Override
						public void effects() {
							Main.game.getTextStartStringBuilder().append(
									"<p>"
										+ "Finally given an opportunity to escape, you do as [harpyNympho.name] commands."
										+ " Jumping to your feet, you run away across the platform, leaving the harpies' mocking laughter behind as you quickly find yourself on the outskirts of the nest once again..."
									+ "</p>");
						}
					};
				}
					
			} else {
				return null;
			}
		}
	};
	
	public static final DialogueNodeOld HARPY_NEST_NYMPHO_AFTER_SEX = new DialogueNodeOld("Harpy nest", ".", true) {
		private static final long serialVersionUID = 1L;

		@Override
		public String getLabel() {
			return "[harpyNympho.Name]'s nest";
		}
		
		@Override
		public String getContent() {
			if(Sex.getNumberOfPartnerOrgasms() >= 1) {
				return "<p>"
							+ "As you step back from [harpyNympho.name], she sinks to the floor, totally worn out from her orgasm"+(Sex.getNumberOfPartnerOrgasms() > 1?"s":"")+"."
							+ " The surrounding harpies, having watched the whole thing, kneel in submission as you finish with their matriarch."
						+ "</p>";
			} else {
				return "<p>"
							+ "As you step back from [harpyNympho.name], she sinks to the floor, letting out a desperate whine as she realises that you've finished with her."
							+ " Her feathered hands dart down between her legs, and she frantically starts masturbating as she seeks to finish what you started."
							+ " The surrounding harpies, having watched the whole thing, kneel in submission as you finish with their matriarch."
						+ "</p>";
			}
		}

		@Override
		public Response getResponse(int index) {
			if (index == 0) {
				return new Response("Leave", "Having had your fun, you decide to leave.", HARPY_NEST_NYMPHO) {
					@Override
					public void effects() {
						Main.game.getTextStartStringBuilder().append(
								"<p>"
									+ "Having put [harpyNympho.name] in her place, you walk back across the platform, quickly finding yourself on the outskirts of the nest once again."
								+ "</p>");
					}
				};
					
			} else {
				return null;
			}
		}
	};
}
