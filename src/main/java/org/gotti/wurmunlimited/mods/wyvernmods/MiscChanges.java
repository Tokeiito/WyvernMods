package org.gotti.wurmunlimited.mods.wyvernmods;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.gotti.wurmunlimited.modloader.ReflectionUtil;
import org.gotti.wurmunlimited.modloader.classhooks.HookException;
import org.gotti.wurmunlimited.modloader.classhooks.HookManager;
import org.gotti.wurmunlimited.modloader.classhooks.InvocationHandlerFactory;
import org.gotti.wurmunlimited.mods.creatures.Charger;
import org.gotti.wurmunlimited.mods.creatures.WyvernBlack;
import org.gotti.wurmunlimited.mods.creatures.WyvernGreen;
import org.gotti.wurmunlimited.mods.creatures.WyvernRed;
import org.gotti.wurmunlimited.mods.creatures.WyvernWhite;

import com.wurmonline.mesh.Tiles;
import com.wurmonline.server.Players;
import com.wurmonline.server.Server;
import com.wurmonline.server.Servers;
import com.wurmonline.server.WurmId;
import com.wurmonline.server.bodys.Wound;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.creatures.CreatureTemplate;
import com.wurmonline.server.creatures.CreatureTemplateFactory;
import com.wurmonline.server.creatures.MovementScheme;
import com.wurmonline.server.items.Item;
import com.wurmonline.server.modifiers.DoubleValueModifier;
import com.wurmonline.server.players.Player;
import com.wurmonline.server.players.PlayerInfo;
import com.wurmonline.server.players.PlayerInfoFactory;
import com.wurmonline.server.skills.NoSuchSkillException;
import com.wurmonline.server.skills.Skill;
import com.wurmonline.server.webinterface.WcKingdomChat;
import com.wurmonline.shared.constants.Enchants;

import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtConstructor;
import javassist.CtMethod;
import javassist.NotFoundException;
import javassist.bytecode.Descriptor;
import javassist.expr.ExprEditor;
import javassist.expr.FieldAccess;
import javassist.expr.MethodCall;

public class MiscChanges {
	public static Logger logger = Logger.getLogger(MiscChanges.class.getName());
	public static byte newGetPlayerRarity(Player p){
		int rarity = 0;
		try{
			byte nextActionRarity = ReflectionUtil.getPrivateField(p, ReflectionUtil.getField(p.getClass(), "nextActionRarity"));
			int windowOfCreation = ReflectionUtil.getPrivateField(p, ReflectionUtil.getField(p.getClass(), "windowOfCreation"));
	        if (Servers.isThisATestServer() && nextActionRarity != 0) {
	            rarity = nextActionRarity;
	            nextActionRarity = 0;
	            ReflectionUtil.setPrivateField(p, ReflectionUtil.getField(p.getClass(), "nextActionRarity"), 0);
	        } else if (windowOfCreation > 0) {
	            windowOfCreation = 0;
	            ReflectionUtil.setPrivateField(p, ReflectionUtil.getField(p.getClass(), "windowOfCreation"), 0);
	            float faintChance = 1.0f;
	            int supPremModifier = 0;
	            if (p.isPaying()) {
	                faintChance = 1.03f;
	                supPremModifier = 3;
	            }
	            if (Server.rand.nextFloat() * 1000.0f <= faintChance) { // 1 in 1000, or [0.1%] chance of Fantastic (from 1 in 10000 [0.01%])
	                rarity = 3;
	            } else if (Server.rand.nextInt(50) <= 0 + supPremModifier) { // 4 in 50, or [8%] chance of Supreme (from 4 in 100 [4%]).
	                rarity = 2;
	            } else { // Anything failing the checks above becomes Rare [100%] (from 1 in 2 [50%]).
	                rarity = 1;
	            }
	        }
	    } catch (IllegalArgumentException | IllegalAccessException | ClassCastException | NoSuchFieldException e) {
			e.printStackTrace();
		}
        return (byte)rarity;
	}

	public static byte newCreatureType(int templateid, byte ctype) throws Exception{
		CreatureTemplate template = CreatureTemplateFactory.getInstance().getTemplate(templateid);
		if(ctype == 0 && (template.isAggHuman() || template.getBaseCombatRating() > 10) && !template.isUnique() && Arena.isTitan(templateid)){
			if(Server.rand.nextInt(5) == 0){
				ctype = (byte) (Server.rand.nextInt(11)+1);
				if(Server.rand.nextInt(50) == 0){
					ctype = 99;
				}
			}
		}
		return ctype;
	}
	
	public static void checkEnchantedBreed(Creature creature){
		int tile = Server.surfaceMesh.getTile(creature.getTileX(), creature.getTileY());
        byte type = Tiles.decodeType((int)tile);
        if (type == Tiles.Tile.TILE_ENCHANTED_GRASS.id){
        	logger.info("Creature "+creature.getName()+" was born on enchanted grass, and has a negative trait removed!");
        	Server.getInstance().broadCastAction(creature.getName()+" was born on enchanted grass, and feels more healthy!", creature, 10);
        	creature.removeRandomNegativeTrait();
        }
	}
	
	public static void setNewMoveLimits(Creature cret){
        try {
            Skill strength = cret.getSkills().getSkill(102);
            ReflectionUtil.setPrivateField(cret, ReflectionUtil.getField(cret.getClass(), "moveslow"), strength.getKnowledge()*4000);
            ReflectionUtil.setPrivateField(cret, ReflectionUtil.getField(cret.getClass(), "encumbered"), strength.getKnowledge()*7000);
            ReflectionUtil.setPrivateField(cret, ReflectionUtil.getField(cret.getClass(), "cantmove"), strength.getKnowledge()*14000);
            MovementScheme moveScheme = cret.getMovementScheme();
            DoubleValueModifier stealthMod = ReflectionUtil.getPrivateField(moveScheme, ReflectionUtil.getField(moveScheme.getClass(), "stealthMod"));
            if (stealthMod == null) {
                stealthMod = new DoubleValueModifier((- 80.0 - Math.min(79.0, cret.getBodyControl())) / 100.0);
            } else {
                stealthMod.setModifier((- 80.0 - Math.min(79.0, cret.getBodyControl())) / 100.0);
            }
            ReflectionUtil.setPrivateField(moveScheme, ReflectionUtil.getField(moveScheme.getClass(), "stealthMod"), stealthMod);
        }
        catch (NoSuchSkillException | IllegalArgumentException | IllegalAccessException | ClassCastException | NoSuchFieldException nss) {
            logger.log(Level.WARNING, "No strength skill for " + cret, (Throwable)((Object)nss));
        }
	}
	
	public static boolean shouldBreedName(Creature creature){
		if(creature.getTemplate().getTemplateId() == WyvernBlack.templateId){
			return true;
		}else if(creature.getTemplate().getTemplateId() == WyvernGreen.templateId){
			return true;
		}else if(creature.getTemplate().getTemplateId() == WyvernRed.templateId){
			return true;
		}else if(creature.getTemplate().getTemplateId() == WyvernWhite.templateId){
			return true;
		}else if(creature.getTemplate().getTemplateId() == Charger.templateId){
			return true;
		}
		return creature.isHorse();
	}
	
	public static void doLifeTransfer(Creature creature, Item attWeapon, double defdamage, float armourMod){
		Wound[] w;
		if (attWeapon.getSpellLifeTransferModifier() > 0.0f && defdamage * (double)armourMod * (double)attWeapon.getSpellLifeTransferModifier() / (double)(creature.isChampion() ? 1000.0f : 500.0f) > 500.0 && creature.getBody() != null && creature.getBody().getWounds() != null && (w = creature.getBody().getWounds().getWounds()).length > 0) {
            w[0].modifySeverity(- (int)(defdamage * (double)attWeapon.getSpellLifeTransferModifier() / (double)(creature.isChampion() ? 1000.0f : (creature.getCultist() != null && creature.getCultist().healsFaster() ? 250.0f : 500.0f))));
        }
	}
	
	public static int getWeaponType(Item weapon){
		if(weapon.enchantment == Enchants.ACID_DAM){
			return 10;
		}else if(weapon.enchantment == Enchants.FROST_DAM){
			return 8;
		}else if(weapon.enchantment == Enchants.FIRE_DAM){
			return 4;
		}
		return -1;
	}
	
	public static void sendServerTabMessage(final String message, final int red, final int green, final int blue){
		Runnable r = new Runnable(){
        	public void run(){
		        com.wurmonline.server.Message mess;
		        for(Player rec : Players.getInstance().getPlayers()){
		        	mess = new com.wurmonline.server.Message(rec, (byte)16, "Server", message, red, green, blue);
		        	rec.getCommunicator().sendMessage(mess);
		        }
        	}
        };
        r.run();
	}
	
	public static long getTimedAffinitySeed(Item item){
		PlayerInfo pinf = PlayerInfoFactory.getPlayerInfoWithName(item.getCreatorName());
		if(pinf != null){
			return pinf.wurmId;
		}
		return 0;
	}

	public static void sendGlobalFreedomChat(final Creature sender, final String message, final int red, final int green, final int blue){
		Runnable r = new Runnable(){
        	public void run(){
		        com.wurmonline.server.Message mess;
		        for(Player rec : Players.getInstance().getPlayers()){
		        	mess = new com.wurmonline.server.Message(sender, (byte)10, "GL-Freedom", "<"+sender.getName()+"> "+message, red, green, blue);
		        	rec.getCommunicator().sendMessage(mess);
		        }
		        if (message.trim().length() > 1) {
                    WcKingdomChat wc = new WcKingdomChat(WurmId.getNextWCCommandId(), sender.getWurmId(), sender.getName(), message, false, (byte) 4, red, green, blue);
                    if (!Servers.isThisLoginServer()) {
                        wc.sendToLoginServer();
                    } else {
                        wc.sendFromLoginServer();
                    }
                }
        	}
        };
        r.run();
	}
	public static void sendImportantMessage(Creature sender, String message, int red, int green, int blue){
		sendServerTabMessage("<"+sender.getName()+"> "+message, red, green, blue);
		sendGlobalFreedomChat(sender, message, red, green, blue);
	}
	
	public static void broadCastDeaths(Creature player, String slayers){
		sendGlobalFreedomChat(player, "slain by "+slayers, 200, 25, 25);
	}
	
	public static void preInit(){
		try{
			ClassPool classPool = HookManager.getInstance().getClassPool();

			// - Create Server tab with initial messages - //
        	CtClass ctPlayers = classPool.get("com.wurmonline.server.Players");
            CtMethod m = ctPlayers.getDeclaredMethod("sendStartGlobalKingdomChat");
            String infoTabTitle = "Server";
            // Initial messages:
            String[] infoTabLine = {"Server Thread: https://forum.wurmonline.com/index.php?/topic/155981-wyvern-reborn-reborn/",
            		"Server Discord: https://discordapp.com/invite/wxEeS7d",
            		"Twin Peaks Map: https://www.sarcasuals.com/mapviewer/"};
            String str = "{"
                    + "        com.wurmonline.server.Message mess;";
            for(int i = 0; i < infoTabLine.length; i++){
            	str = str + "        mess = new com.wurmonline.server.Message(player, (byte)16, \"" + infoTabTitle + "\",\"" + infoTabLine[i] + "\", 0, 255, 0);"
            			  + "        player.getCommunicator().sendMessage(mess);";
            }
            str = str + "}";
            m.insertAfter(str);

            // - Enable bridges to be built inside/over/through houses - //
            CtClass ctPlanBridgeChecks = classPool.get("com.wurmonline.server.structures.PlanBridgeChecks");
            ctPlanBridgeChecks.getDeclaredMethod("checkForBuildings").setBody("{"
            		+ "  return new com.wurmonline.server.structures.PlanBridgeCheckResult(false);"
            		+ "}");

            // - Allow mailboxes and bell towers to be loaded - //
            CtClass ctItemTemplate = classPool.get("com.wurmonline.server.items.ItemTemplate");
            ctItemTemplate.getDeclaredMethod("isTransportable").setBody("{"
            		+ "  return this.isTransportable || (this.getTemplateId() >= 510 && this.getTemplateId() <= 513) || this.getTemplateId() == 722 || this.getTemplateId() == 670;"
            		+ "}");
            // But don't let mailboxes them be used while loaded...
            CtClass ctItem = classPool.get("com.wurmonline.server.items.Item");
            ctItem.getDeclaredMethod("moveToItem").instrument(new ExprEditor(){
                public void edit(MethodCall m) throws CannotCompileException {
                    if (m.getMethodName().equals("getOwnerId")) {
                        m.replace("$_ = $proceed($$);"
                        		+ "com.wurmonline.server.items.Item theTarget = com.wurmonline.server.Items.getItem(targetId);"
                        		+ "if(theTarget != null && theTarget.getTemplateId() >= 510 && theTarget.getTemplateId() <= 513){"
                        		+ "  if(theTarget.getTopParent() != theTarget.getWurmId()){"
                        		+ "    mover.getCommunicator().sendNormalServerMessage(\"Mailboxes cannot be used while loaded.\");"
                        		+ "    return false;"
                        		+ "  }"
                        		+ "}");
                        return;
                    }
                }
            });

            // - Enable creature custom colors - (Used for creating custom color creatures eg. Lilith) - //
            CtClass ctCreature = classPool.get("com.wurmonline.server.creatures.Creature");
            ctCreature.getDeclaredMethod("hasCustomColor").setBody("{ return true; }");

            // - Increase the amount of checks for new unique spawns by 5x - //
            CtClass ctServer = classPool.get("com.wurmonline.server.Server");
            ctServer.getDeclaredMethod("run").instrument(new ExprEditor(){
                public void edit(MethodCall m) throws CannotCompileException {
                    if (m.getMethodName().equals("checkDens")) {
                        m.replace("for(int i = 0; i < 5; i++){$_ = $proceed($$);}");
                        return;
                    }
                }
            });
            
            // - Change rarity odds when a player obtains a rarity window - //
            CtClass ctPlayer = classPool.get("com.wurmonline.server.players.Player");
            ctPlayer.getDeclaredMethod("getRarity").setBody("{ return org.gotti.wurmunlimited.mods.wyvernmods.MiscChanges.newGetPlayerRarity(this); }");
            
            // - Add Facebreyker to the list of spawnable uniques - //
            CtClass ctDens = classPool.get("com.wurmonline.server.zones.Dens");
            ctDens.getDeclaredMethod("checkDens").insertAt(0, "com.wurmonline.server.zones.Dens.checkTemplate(2147483643, whileRunning);");

            // - Announce player titles in the Server tab - //
            ctPlayer.getDeclaredMethod("addTitle").instrument(new ExprEditor(){
                public void edit(MethodCall m) throws CannotCompileException {
                    if (m.getMethodName().equals("sendNormalServerMessage")) {
                        m.replace("$_ = $proceed($$);"
                        		+ "if(!com.wurmonline.server.Servers.localServer.PVPSERVER){"
                        		+ "  org.gotti.wurmunlimited.mods.wyvernmods.MiscChanges.sendServerTabMessage(this.getName()+\" just earned the title of \"+title.getName(this.isNotFemale())+\"!\", 200, 100, 0);"
                        		+ "}");
                        return;
                    }
                }
            });

            CtClass ctMethodsItems = classPool.get("com.wurmonline.server.behaviours.MethodsItems");
            // - Make leather not suck even after it's able to be combined. - //
            ctMethodsItems.getDeclaredMethod("improveItem").instrument(new ExprEditor(){
                public void edit(MethodCall m) throws CannotCompileException {
                    if (m.getMethodName().equals("isCombine")) {
                        m.replace("if(com.wurmonline.server.behaviours.MethodsItems.getImproveTemplateId(target) != 72){"
                        		+ "  $_ = $proceed($$);"
                        		+ "}else{"
                        		+ "  $_ = false;"
                        		+ "}");
                        return;
                    }
                }
            });
            
            // - Check new improve materials - //
            ctMethodsItems.getDeclaredMethod("getImproveTemplateId").insertBefore(""
            		+ "int temp = org.gotti.wurmunlimited.mods.wyvernmods.ItemMod.getModdedImproveTemplateId($1);"
            		+ "if(temp != -10){"
            		+ "  return temp;"
            		+ "}");
            
            // - Make food/drink affinities based on Item ID instead of creature ID - //
            CtClass ctAffinitiesTimed = classPool.get("com.wurmonline.server.skills.AffinitiesTimed");
            ctAffinitiesTimed.getDeclaredMethod("getTimedAffinitySkill").instrument(new ExprEditor(){
                public void edit(MethodCall m) throws CannotCompileException {
                    if (m.getMethodName().equals("setSeed")) {
                        m.replace("if(item.getCreatorName() != null){"
                        		+ "  $_ = $proceed(org.gotti.wurmunlimited.mods.wyvernmods.MiscChanges.getTimedAffinitySeed(item));"
                        		+ "}else{"
                        		+ "  $_ = $proceed($$);"
                        		+ "}");
                        return;
                    }
                }
            });
            CtClass ctItemBehaviour = classPool.get("com.wurmonline.server.behaviours.ItemBehaviour");
            ctItemBehaviour.getDeclaredMethod("handleRecipe").instrument(new ExprEditor(){
                public void edit(MethodCall m) throws CannotCompileException {
                    if (m.getMethodName().equals("createItem")) {
                        m.replace("$_ = $proceed($1, $2, $3, $4, performer.getName());");
                        logger.info("Instrumented createItem in handleRecipe");
                        return;
                    }
                }
            });
            ctItem.getDeclaredMethod("pollFermenting").instrument(new ExprEditor(){
                public void edit(MethodCall m) throws CannotCompileException {
                    if (m.getMethodName().equals("createItem")) {
                        m.replace("$_ = $proceed($1, $2, $3, $4, com.wurmonline.server.players.PlayerInfoFactory.getPlayerName(lastowner));");
                        logger.info("Instrumented createItem in pollFermenting");
                        return;
                    }
                }
            });
            ctItem.getDeclaredMethod("pollDistilling").instrument(new ExprEditor(){
                public void edit(MethodCall m) throws CannotCompileException {
                    if (m.getMethodName().equals("createItem")) {
                        m.replace("$_ = $proceed($1, $2, $3, $4, com.wurmonline.server.players.PlayerInfoFactory.getPlayerName(lastowner));");
                        logger.info("Instrumented createItem in pollDistilling");
                        return;
                    }
                }
            });
            CtClass ctTempStates = classPool.get("com.wurmonline.server.items.TempStates");
            ctTempStates.getDeclaredMethod("checkForChange").instrument(new ExprEditor(){
                public void edit(MethodCall m) throws CannotCompileException {
                    if (m.getMethodName().equals("createItem")) {
                        m.replace("$_ = $proceed($1, $2, $3, $4, com.wurmonline.server.players.PlayerInfoFactory.getPlayerName(lastowner));");
                        logger.info("Instrumented createItem in TempStates");
                        return;
                    }
                }
            });
            
            // - Fix de-priesting when gaining faith below 30 - //
            CtClass ctDbPlayerInfo = classPool.get("com.wurmonline.server.players.DbPlayerInfo");
            ctDbPlayerInfo.getDeclaredMethod("setFaith").instrument(new ExprEditor(){
                public void edit(MethodCall m) throws CannotCompileException {
                    if (m.getMethodName().equals("min")) {
                        m.replace("if($2 == 20.0f && $1 < 30){"
                        		+ "  $_ = $proceed(30.0f, lFaith);"
                        		+ "}else{"
                        		+ "  $_ = $proceed($$);"
                        		+ "}");
                        return;
                    }
                }
            });
            ctDbPlayerInfo.getDeclaredMethod("setFaith").instrument(new ExprEditor(){
                public void edit(MethodCall m) throws CannotCompileException {
                    if (m.getMethodName().equals("setPriest")) {
                        m.replace("$_ = $proceed(true);");
                        return;
                    }
                }
            });
            ctDbPlayerInfo.getDeclaredMethod("setFaith").instrument(new ExprEditor(){
                public void edit(MethodCall m) throws CannotCompileException {
                    if (m.getMethodName().equals("sendAlertServerMessage")) {
                        m.replace("$_ = null;");
                        return;
                    }
                }
            });
            
            // - Removal of eye/face shots to headshots instead - //
            HookManager.getInstance().registerHook("com.wurmonline.server.combat.Armour", "getArmourPosForPos", "(I)I", new InvocationHandlerFactory() {
            	 
                @Override
                public InvocationHandler createInvocationHandler() {
                    return new InvocationHandler() {

						@Override
						public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
							int pos = (int) args[0];
						     
                            if (pos == 18 || pos == 19 || pos == 20 || pos == 17) {
                                args[0] = 34;
                                System.out.println("changed eye or face shot into headshot");
                            }
     
                            return method.invoke(proxy, args);
						}
                    };
                }
            });
            
            // - Remove requirement to bless for Libila taming - //
            CtClass ctMethodsCreatures = classPool.get("com.wurmonline.server.behaviours.MethodsCreatures");
            ctMethodsCreatures.getDeclaredMethod("tame").instrument(new ExprEditor(){
                public void edit(MethodCall m) throws CannotCompileException {
                    if (m.getMethodName().equals("isPriest")) {
                        m.replace("$_ = false;");
                        return;
                    }
                }
            });
            
            // - Remove fatiguing actions requiring you to be on the ground - //
            CtClass ctAction = classPool.get("com.wurmonline.server.behaviours.Action");
            CtConstructor[] ctActionConstructors = ctAction.getConstructors();
            for(CtConstructor constructor : ctActionConstructors){
            	constructor.instrument(new ExprEditor(){
                    public void edit(MethodCall m) throws CannotCompileException {
                        if (m.getMethodName().equals("isFatigue")) {
                            m.replace("$_ = false;");
                            return;
                        }
                    }
                });
            }
            
            // - Allow all creatures to be displayed in the Mission Ruler - //
            CtClass ctMissionManager = classPool.get("com.wurmonline.server.questions.MissionManager");
            ctMissionManager.getDeclaredMethod("sendManageTriggerEffect").instrument(new ExprEditor() {
                @Override
                public void edit(FieldAccess fieldAccess) throws CannotCompileException {
                    if (Objects.equals("baseCombatRating", fieldAccess.getFieldName()))
                        fieldAccess.replace("$_ = 1.0f;");
                }
            });
            
            // - Fix Portal issues - //
            CtClass ctPortal = classPool.get("com.wurmonline.server.questions.PortalQuestion");
        	ctPortal.getDeclaredMethod("sendQuestion").instrument(new ExprEditor() {
                public void edit(MethodCall m) throws CannotCompileException {            	
                	if (m.getMethodName().equals("willLeaveServer")) {
                        m.replace("$_ = true;");
                        return;
                    } // if
                    
                    if (m.getMethodName().equals("getKnowledge")) {
                        m.replace("$_ = true;");
                        return;
                    } // if
                } // edit
            }); // getDeclaredMethod

        	// - Disable the minimum 0.01 damage on shield damage, allowing damage modifiers to rule. - //
        	CtClass ctCombatHandler = classPool.get("com.wurmonline.server.creatures.CombatHandler");
        	ctCombatHandler.getDeclaredMethod("checkShield").instrument(new ExprEditor(){
                public void edit(MethodCall m) throws CannotCompileException {
                    if (m.getMethodName().equals("max")) {
                        m.replace("if($1 < 0.5f){"
                        		+ "  $_ = $proceed((float) 0, (float) $2);"
                        		+ "}else{"
                        		+ "  $_ = $proceed($$);"
                        		+ "}");
                        return;
                    }
                }
            });

        	// - Allow ghost creatures to breed (Chargers) - //
        	ctMethodsCreatures.getDeclaredMethod("breed").instrument(new ExprEditor(){
                public void edit(MethodCall m) throws CannotCompileException {
                    if (m.getMethodName().equals("isGhost")) {
                        m.replace("$_ = false;");
                        return;
                    }
                }
            });
        	
        	// - Allow Life Transfer to stack with Rotting Touch - //
        	ctCombatHandler.getDeclaredMethod("setDamage").instrument(new ExprEditor(){
                public void edit(MethodCall m) throws CannotCompileException {
                    if (m.getMethodName().equals("isWeaponCrush")) {
                        m.replace("org.gotti.wurmunlimited.mods.wyvernmods.MiscChanges.doLifeTransfer(this.creature, attWeapon, defdamage, armourMod);"
                        		+ "$_ = $proceed($$);");
                        return;
                    }
                }
            });
        	
        	// - Fix dragon armour dropping on logout - //
        	ctItem.getDeclaredMethod("sleep").instrument(new ExprEditor(){
                public void edit(MethodCall m) throws CannotCompileException {
                    if (m.getMethodName().equals("isDragonArmour")) {
                        m.replace("$_ = false;");
                        return;
                    }
                }
            });
        	ctItem.getDeclaredMethod("sleepNonRecursive").instrument(new ExprEditor(){
                public void edit(MethodCall m) throws CannotCompileException {
                    if (m.getMethodName().equals("isDragonArmour")) {
                        m.replace("$_ = false;");
                        return;
                    }
                }
            });
        	
        	// - Type creatures randomly in the wild - //
        	CtMethod ctDoNew = ctCreature.getMethod("doNew", "(IZFFFILjava/lang/String;BBBZB)Lcom/wurmonline/server/creatures/Creature;");
            ctDoNew.insertBefore("$10 = org.gotti.wurmunlimited.mods.wyvernmods.MiscChanges.newCreatureType($1, $10);");
            
            // - Allow custom creatures to be given special names when bred - //
        	ctCreature.getDeclaredMethod("checkPregnancy").instrument(new ExprEditor(){
                public void edit(MethodCall m) throws CannotCompileException {
                    if (m.getMethodName().equals("isHorse")) {
                        m.replace("$_ = org.gotti.wurmunlimited.mods.wyvernmods.MiscChanges.shouldBreedName(this);");
                        return;
                    }
                }
            });
        	
            // - Auto-Genesis a creature born on enchanted grass - //
        	ctCreature.getDeclaredMethod("checkPregnancy").instrument(new ExprEditor(){
                public void edit(MethodCall m) throws CannotCompileException {
                    if (m.getMethodName().equals("saveCreatureName")) {
                        m.replace("org.gotti.wurmunlimited.mods.wyvernmods.MiscChanges.checkEnchantedBreed(newCreature);"
                        		+ "$_ = $proceed($$);");
                        return;
                    }
                }
            });
            
        	// - Mastercraft, enabling rare items and skills 90+/99+ to reduce skill check difficulty - //
        	CtClass ctSkill = classPool.get("com.wurmonline.server.skills.Skill");
        	ctSkill.getDeclaredMethod("checkAdvance").insertBefore(""
        			+ "$1 = mod.sin.wyvern.Mastercraft.getNewDifficulty(this, $1, $2);");
        	
        	// - Allow statuettes to be used for casting even when not silver/gold - //
        	String params = Descriptor.ofMethod(CtClass.booleanType, new CtClass[]{});
        	ctItem.getMethod("isHolyItem", params).setBody("return this.template.holyItem;");
        	
        	// - Allow GM's to bypass the 5 second emote sound limit. - //
        	ctPlayer.getDeclaredMethod("mayEmote").insertBefore(""
        			+ "if(this.getPower() > 0){"
        			+ "  return true;"
        			+ "}");
        	
        	// - Allow archery against ghost targets - //
        	CtClass ctArchery = classPool.get("com.wurmonline.server.combat.Archery");
        	CtMethod[] archeryAttacks = ctArchery.getDeclaredMethods("attack");
        	for(CtMethod method : archeryAttacks){
            	method.instrument(new ExprEditor(){
                    public void edit(MethodCall m) throws CannotCompileException {
                        if (m.getMethodName().equals("isGhost")) {
                            m.replace("$_ = false;");
                            return;
                        }
                    }
                });
        	}
        	
        	// - Prevent archery altogether against certain creatures - //
        	CtClass[] params2 = {
        			classPool.get("com.wurmonline.server.creatures.Creature"),
        			classPool.get("com.wurmonline.server.creatures.Creature"),
        			classPool.get("com.wurmonline.server.items.Item"),
        			CtClass.floatType,
        			classPool.get("com.wurmonline.server.behaviours.Action")
        	};
        	String desc = Descriptor.ofMethod(CtClass.booleanType, params2);
        	ctArchery.getMethod("attack", desc).insertBefore("if(mod.sin.wyvernmods.bestiary.MethodsBestiary.isArcheryImmune($1, $2)){"
        			+ "  return true;"
        			+ "}");

        	// - Make creatures wander slightly if they are shot from afar by an arrow - //
        	CtClass ctArrows = classPool.get("com.wurmonline.server.combat.Arrows");
        	ctArrows.getDeclaredMethod("addToHitCreature").instrument(new ExprEditor(){
                public void edit(MethodCall m) throws CannotCompileException {
                    if (m.getMethodName().equals("addAttacker")) {
                        m.replace("if(!defender.isPathing()){"
                        		+ "  defender.startPathing(com.wurmonline.server.Server.rand.nextInt(100));"
                        		+ "}"
                        		+ "$_ = $proceed($$);");
                        return;
                    }
                }
            });
        	
        	// - Prevent losing Libila faith when changing kingdoms/crossing servers - //
        	CtClass[] params3 = {
        			CtClass.byteType,
        			CtClass.booleanType,
        			CtClass.booleanType,
        			CtClass.booleanType
        	};
        	String desc2 = Descriptor.ofMethod(CtClass.booleanType, params3);
        	ctCreature.getMethod("setKingdomId", desc2).instrument(new ExprEditor(){
                public void edit(MethodCall m) throws CannotCompileException {
                    if (m.getMethodName().equals("getKingdomTemplateFor")) {
                        m.replace("$_ = (byte)4;");
                        return;
                    }
                }
            });

        	// - Broadcast Deaths tabs to GL-Freedom - //
        	ctPlayers.getDeclaredMethod("broadCastDeathInfo").insertBefore("org.gotti.wurmunlimited.mods.wyvernmods.MiscChanges.broadCastDeaths($1, $2);");
        	
        	// - Reduce meditation cooldowns - //
        	CtClass ctCultist = classPool.get("com.wurmonline.server.players.Cultist");
        	ctCultist.getDeclaredMethod("mayRefresh").setBody("return this.path == 1 && this.level > 3 && System.currentTimeMillis() - this.cooldown1 > 28800000;");
        	ctCultist.getDeclaredMethod("mayEnchantNature").setBody("return this.path == 1 && this.level > 6 && System.currentTimeMillis() - this.cooldown2 > 28800000;");
        	ctCultist.getDeclaredMethod("mayStartLoveEffect").setBody("return this.path == 1 && this.level > 8 && System.currentTimeMillis() - this.cooldown3 > 14400000;");
        	ctCultist.getDeclaredMethod("mayStartDoubleWarDamage").setBody("return this.path == 2 && this.level > 6 && System.currentTimeMillis() - this.cooldown1 > 21600000;");
        	ctCultist.getDeclaredMethod("mayStartDoubleStructDamage").setBody("return this.path == 2 && this.level > 3 && System.currentTimeMillis() - this.cooldown2 > 14400000;");
        	ctCultist.getDeclaredMethod("mayStartFearEffect").setBody("return this.path == 2 && this.level > 8 && System.currentTimeMillis() - this.cooldown3 > 21600000;");
        	ctCultist.getDeclaredMethod("mayStartNoElementalDamage").setBody("return this.path == 5 && this.level > 8 && System.currentTimeMillis() - this.cooldown1 > 21600000;");
        	ctCultist.getDeclaredMethod("maySpawnVolcano").setBody("return this.path == 5 && this.level > 6 && System.currentTimeMillis() - this.cooldown2 > 28800000;");
        	ctCultist.getDeclaredMethod("mayStartIgnoreTraps").setBody("return this.path == 5 && this.level > 3 && System.currentTimeMillis() - this.cooldown3 > 14400000;");
        	ctCultist.getDeclaredMethod("mayCreatureInfo").setBody("return this.path == 3 && this.level > 3 && System.currentTimeMillis() - this.cooldown1 > 14400000;");
        	ctCultist.getDeclaredMethod("mayInfoLocal").setBody("return this.path == 3 && this.level > 6 && System.currentTimeMillis() - this.cooldown2 > 14400000;");

        	// - Adjust weapon damage type based on the potion/salve applied - //
        	ctCombatHandler.getDeclaredMethod("getType").insertBefore(""
        			+ "int wt = org.gotti.wurmunlimited.mods.wyvernmods.MiscChanges.getWeaponType($1);"
        			+ "if(wt != -1){"
        			+ "  type = wt;"
        			+ "  return wt;"
        			+ "}");

            // - Another attempt to prevent libila from losing faith when crossing servers... - //
            CtClass ctIntraServerConnection = classPool.get("com.wurmonline.server.intra.IntraServerConnection");
            ctIntraServerConnection.getDeclaredMethod("savePlayerToDisk").instrument(new ExprEditor() {
                @Override
                public void edit(FieldAccess fieldAccess) throws CannotCompileException {
                    if (Objects.equals("PVPSERVER", fieldAccess.getFieldName())){
                        fieldAccess.replace("$_ = true;");
                    }
                }
            });

        	// - Affinity Weekend - //
        	CtClass[] params4 = {
        			CtClass.doubleType,
        			CtClass.booleanType,
        			CtClass.floatType,
        			CtClass.booleanType,
        			CtClass.doubleType
        	};
        	String desc4 = Descriptor.ofMethod(CtClass.voidType, params4);
        	ctSkill.getMethod("alterSkill", desc4).instrument(new ExprEditor(){
                public void edit(MethodCall m) throws CannotCompileException {
                    if (m.getMethodName().equals("hasSleepBonus")) {
                        m.replace("int timedAffinity = this.affinity + (com.wurmonline.server.skills.AffinitiesTimed.isTimedAffinity(pid, this.getNumber()) ? 4 : 0);"
                        		+ "advanceMultiplicator *= (double)(1.0f + (float)timedAffinity * 0.1f);"
                        		+ "$_ = $proceed($$);");
                        return;
                    }
                }
            });

        	// - Double the rate at which charcoal piles produce items - //
        	CtClass[] params5 = {
        			CtClass.booleanType,
        			CtClass.booleanType,
        			CtClass.longType
        	};
        	String desc5 = Descriptor.ofMethod(CtClass.booleanType, params5);
        	ctItem.getMethod("poll", desc5).instrument(new ExprEditor(){
                public void edit(MethodCall m) throws CannotCompileException {
                    if (m.getMethodName().equals("createDaleItems")) {
                        m.replace("this.createDaleItems();"
                        		+ "decayed = this.setDamage(this.damage + 1.0f * this.getDamageModifier());"
                        		+ "$_ = $proceed($$);");
                        return;
                    }
                }
            });

        } catch (CannotCompileException | NotFoundException | IllegalArgumentException | ClassCastException e) {
            throw new HookException((Throwable)e);
        }
	}
}
