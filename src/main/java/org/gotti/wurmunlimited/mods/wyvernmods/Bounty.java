package org.gotti.wurmunlimited.mods.wyvernmods;

import java.util.HashMap;
import java.util.logging.Logger;

import org.gotti.wurmunlimited.modloader.classhooks.HookException;
import org.gotti.wurmunlimited.modloader.classhooks.HookManager;

import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;
import javassist.NotFoundException;
import javassist.bytecode.Descriptor;
import javassist.expr.ExprEditor;
import javassist.expr.MethodCall;

public class Bounty {
	private static Logger logger = Logger.getLogger(Bounty.class.getName());
	protected static WyvernMods mod;
	public static HashMap<String, Integer> reward = new HashMap<String, Integer>();
	/*private int[] usefulItems = {ItemList.axeSmall, ItemList.shieldMedium, ItemList.hatchet, ItemList.knifeCarving,
			ItemList.pickaxe, ItemList.swordLong, ItemList.saw, ItemList.shovel, ItemList.rake, ItemList.hammerMetal,
			ItemList.hammerWood, ItemList.anvilSmall, ItemList.cheeseDrill, ItemList.swordShort, ItemList.swordTwoHander,
			ItemList.shieldSmallWood, ItemList.shieldSmallMetal, ItemList.shieldMediumWood, ItemList.shieldLargeWood,
			ItemList.shieldLargeMetal, ItemList.axeHuge, ItemList.axeMedium, ItemList.knifeButchering,
			ItemList.fishingRodIronHook, ItemList.stoneChisel, ItemList.leatherGlove, ItemList.leatherJacket,
			ItemList.leatherBoot, ItemList.leatherSleeve, ItemList.leatherCap, ItemList.leatherHose, ItemList.clothGlove,
			ItemList.clothShirt, ItemList.clothSleeve, ItemList.clothJacket, ItemList.clothHose, ItemList.clothShoes,
			ItemList.studdedLeatherSleeve, ItemList.studdedLeatherBoot, ItemList.studdedLeatherCap,
			ItemList.studdedLeatherHose, ItemList.studdedLeeatherGlove, ItemList.studdedLeatherJacket,
			ItemList.spindle, ItemList.flintSteel, ItemList.fishingRodWoodenHook, ItemList.stoneOven, ItemList.forge,
			ItemList.anvilLarge, ItemList.cartSmall, ItemList.needleIron, ItemList.loom, ItemList.sickle, ItemList.scythe,
			ItemList.gloveSteel, ItemList.chainBoot, ItemList.chainHose, ItemList.chainJacket, ItemList.chainSleeve,
			ItemList.chainGlove, ItemList.chainCoif, ItemList.plateBoot, ItemList.plateHose, ItemList.plateJacket,
			ItemList.plateSleeve, ItemList.plateGauntlet, ItemList.helmetBasinet, ItemList.helmetGreat, ItemList.helmetOpen,
			ItemList.maulLarge, ItemList.maulSmall, ItemList.maulMedium, ItemList.whetstone, ItemList.pelt, ItemList.ropeTool,
			ItemList.guardTower, ItemList.file, ItemList.awl, ItemList.leatherKnife, ItemList.scissors, ItemList.clayShaper,
			ItemList.spatula, ItemList.bowShort, ItemList.bowMedium, ItemList.bowLong, ItemList.dragonLeatherSleeve,
			ItemList.dragonLeatherBoot, ItemList.dragonLeatherCap, ItemList.dragonLeatherHose, ItemList.dragonLeatherGlove,
			ItemList.dragonLeatherJacket, ItemList.dragonScaleBoot, ItemList.dragonScaleHose, ItemList.dragonScaleJacket,
			ItemList.dragonScaleSleeve, ItemList.dragonScaleGauntlet, ItemList.boatRowing, ItemList.boatSailing,
			ItemList.trowel, ItemList.statuetteFo, ItemList.statuetteLibila, ItemList.statuetteMagranon,
			ItemList.statuetteVynora, ItemList.cartLarge, ItemList.cog, ItemList.corbita, ItemList.knarr, ItemList.caravel,
			ItemList.saddle, ItemList.horseShoe, ItemList.meditationRugOne, ItemList.meditationRugTwo,
			ItemList.meditationRugThree, ItemList.meditationRugFour, ItemList.groomingBrush, ItemList.spearLong,
			ItemList.halberd, ItemList.spearSteel, ItemList.clothHood, ItemList.wagon, ItemList.boneCollar,
			ItemList.spinningWheel, ItemList.smelter, ItemList.halerRope};*/
	
	public static void preInit(WyvernMods mod){
		Bounty.mod = mod;
		/*reward.put("black wolf", 750);*/
    }
	
	public static void init(){
		try {
        	ClassPool classPool = HookManager.getInstance().getClassPool();
            CtClass ctCreature = classPool.get("com.wurmonline.server.creatures.Creature");
            
            /*CtMethod ctCheckBounty = CtMethod.make((String)
            		  "public void checkBounty(com.wurmonline.server.players.Player player, com.wurmonline.server.creatures.Creature mob){"
            		+ "  if(!mod.sin.wyvernmods.bounty.MethodsBounty.isCombatant(this.attackers, player.getWurmId()) || mob.isPlayer() || mob.isReborn()){"
            		+ "    return;"
            		+ "  }"
            		+ (mod.bDebug ? "logger.info(player.getName()+\" killed \"+mob.getName());" : "")
            		+ "  mod.sin.wyvernmods.bounty.MethodsBounty.checkPlayerReward(player, mob);"
            		+ "}", ctCreature);
          ctCreature.addMethod(ctCheckBounty);*/

          ctCreature.getDeclaredMethod("modifyFightSkill").instrument(new ExprEditor(){
                public void edit(MethodCall m) throws CannotCompileException {
                    if (m.getMethodName().equals("checkCoinAward")) {
                        m.replace("mod.sin.wyvernmods.bounty.MethodsBounty.checkBounty(player, this);"
                        		+ "$_ = $proceed($$);");
                        logger.info("Instrumented checkCoinAward to call checkBounty as well.");
                        return;
                    }
                }
          });
          ctCreature.getDeclaredMethod("die").instrument(new ExprEditor(){
              public void edit(MethodCall m) throws CannotCompileException {
                  if (m.getMethodName().equals("setRotation")) {
                      m.replace("$_ = $proceed($$);"
                    		  + "mod.sin.wyvernmods.bounty.MethodsBounty.checkLootTable(this, corpse);");
                      logger.info("Instrumented setRotation to call insertCorpseItems as well.");
                      return;
                  }
              }
          });

          // Debugging to show all new creatures created.
          CtMethod ctDoNew = ctCreature.getMethod("doNew", "(IZFFFILjava/lang/String;BBBZB)Lcom/wurmonline/server/creatures/Creature;");
          ctDoNew.insertBefore("logger.info(\"Creating new creature: \"+templateid+\" - \"+(aPosX/4)+\", \"+(aPosY/4)+\" [\"+com.wurmonline.server.creatures.CreatureTemplateFactory.getInstance().getTemplate(templateid).getName()+\"]\");");
          // Modify new creatures
          ctDoNew.instrument(new ExprEditor(){
              public void edit(MethodCall m) throws CannotCompileException {
                  if (m.getMethodName().equals("sendToWorld")) {
                      m.replace("$_ = $proceed($$);"
                      		+ "mod.sin.wyvernmods.bestiary.MethodsBestiary.modifyNewCreature($1);");
                      return;
                  }
              }
          });
          
          // -- Enable adjusting size for creatures -- //
          CtClass ctCreatureStatus = classPool.get("com.wurmonline.server.creatures.CreatureStatus");
          ctCreatureStatus.getDeclaredMethod("getSizeMod").setBody("{return mod.sin.wyvernmods.bestiary.MethodsBestiary.getAdjustedSizeMod(this);}");
          
          // -- Enable adjusting color for creatures -- //
          CtClass ctCreatureTemplate = classPool.get("com.wurmonline.server.creatures.CreatureTemplate");
          ctCreatureTemplate.getDeclaredMethod("getColorRed").insertBefore("if(mod.sin.wyvernmods.bestiary.MethodsBestiary.checkColorTemplate(this)){"
            		+ "  return mod.sin.wyvernmods.bestiary.MethodsBestiary.getCreatureColorRed(this);"
            		+ "}");
          ctCreatureTemplate.getDeclaredMethod("getColorGreen").insertBefore("if(mod.sin.wyvernmods.bestiary.MethodsBestiary.checkColorTemplate(this)){"
            		+ "  return mod.sin.wyvernmods.bestiary.MethodsBestiary.getCreatureColorGreen(this);"
            		+ "}");
          ctCreatureTemplate.getDeclaredMethod("getColorBlue").insertBefore("if(mod.sin.wyvernmods.bestiary.MethodsBestiary.checkColorTemplate(this)){"
          			+ "  return mod.sin.wyvernmods.bestiary.MethodsBestiary.getCreatureColorBlue(this);"
          			+ "}");
          
          CtClass[] params = {
        		  classPool.get("com.wurmonline.server.creatures.Creature"),
        		  classPool.get("com.wurmonline.server.creatures.Creature"),
        		  CtClass.byteType,
        		  CtClass.intType,
        		  CtClass.doubleType,
        		  CtClass.floatType,
        		  classPool.get("java.lang.String"),
        		  classPool.get("com.wurmonline.server.combat.Battle"),
        		  CtClass.floatType,
        		  CtClass.floatType,
        		  CtClass.booleanType,
        		  CtClass.booleanType
          };
          String descriptor = Descriptor.ofMethod(CtClass.booleanType, params);
          CtClass ctCombatEngine = classPool.get("com.wurmonline.server.combat.CombatEngine");
          ctCombatEngine.getMethod("addWound", descriptor).insertBefore("if($1 != null && $2 != null){mod.sin.wyvernmods.bounty.MethodsBounty.addDealtDamage($2.getWurmId(), $1.getWurmId(), $5);}");
          
      }
      catch (CannotCompileException | NotFoundException e) {
          throw new HookException((Throwable)e);
      }
	}
}
