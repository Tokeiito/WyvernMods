package org.gotti.wurmunlimited.mods.creatures;

import org.gotti.wurmunlimited.modsupport.CreatureTemplateBuilder;
import org.gotti.wurmunlimited.modsupport.creatures.EncounterBuilder;
import org.gotti.wurmunlimited.modsupport.creatures.ModCreature;
import org.gotti.wurmunlimited.modsupport.vehicles.ModVehicleBehaviour;
import org.gotti.wurmunlimited.modsupport.vehicles.VehicleFacade;

import com.wurmonline.mesh.Tiles;
import com.wurmonline.server.Servers;
import com.wurmonline.server.behaviours.Vehicle;
import com.wurmonline.server.bodys.BodyTemplate;
import com.wurmonline.server.bodys.Wound;
import com.wurmonline.server.combat.ArmourTypes;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.creatures.CreatureTypes;
import com.wurmonline.server.items.Item;
import com.wurmonline.server.items.ItemList;
import com.wurmonline.server.items.Materials;
import com.wurmonline.server.skills.SkillList;

public class WyvernWhite implements ModCreature, CreatureTypes {
	public static int templateId;
	@Override
	public CreatureTemplateBuilder createCreateTemplateBuilder() {
		// {C_TYPE_MOVE_LOCAL, C_TYPE_VEHICLE, C_TYPE_ANIMAL, C_TYPE_LEADABLE, C_TYPE_GRAZER, C_TYPE_OMNIVORE, C_TYPE_DOMINATABLE, C_TYPE_AGG_HUMAN, C_TYPE_NON_NEWBIE, C_TYPE_BURNING}; - Hell Horse
		int[] types = {
			CreatureTypes.C_TYPE_CARNIVORE,
			CreatureTypes.C_TYPE_MOVE_GLOBAL,
			CreatureTypes.C_TYPE_VEHICLE,
			CreatureTypes.C_TYPE_REGENERATING,
			CreatureTypes.C_TYPE_AGG_HUMAN,
			CreatureTypes.C_TYPE_SWIMMING,
			CreatureTypes.C_TYPE_HUNTING,
			CreatureTypes.C_TYPE_DOMINATABLE,
			CreatureTypes.C_TYPE_MONSTER,
			CreatureTypes.C_TYPE_NON_NEWBIE,
			CreatureTypes.C_TYPE_ANIMAL
		};
		
		//public CreatureTemplateBuilder(final String identifier, final String name, final String description,
		//       final String modelName, final int[] types, final byte bodyType, final short vision, final byte sex, final short centimetersHigh, final short centimetersLong, final short centimetersWide,
		//       final String deathSndMale, final String deathSndFemale, final String hitSndMale, final String hitSndFemale,
		//       final float naturalArmour, final float handDam, final float kickDam, final float biteDam, final float headDam, final float breathDam, final float speed, final int moveRate,
		//       final int[] itemsButchered, final int maxHuntDist, final int aggress) {
		CreatureTemplateBuilder builder = new CreatureTemplateBuilder("mod.creature.wyvern.white", "White wyvern", "A battle-hardened wyvern with scales as white as the moon.",
				"model.creature.drake.white", types, BodyTemplate.TYPE_DRAGON, (short) 10, (byte) 0, (short) 350, (short) 100, (short) 60,
				"sound.death.dragon", "sound.death.dragon", "sound.combat.hit.dragon", "sound.combat.hit.dragon",
				Servers.localServer.PVPSERVER ? 0.7f : 0.3f, 17.0f, 20.0f, 0.0f, 0.0f, 0.0f, 1.4f, 900,
				new int[]{ItemList.animalHide, ItemList.tail, ItemList.eye, ItemList.gland, ItemList.tooth}, 40, 70, Materials.MATERIAL_MEAT_DRAGON);
		
		builder.skill(SkillList.BODY_STRENGTH, Servers.localServer.PVPSERVER ? 43f : 58f);
		builder.skill(SkillList.BODY_STAMINA, 50.0f);
		builder.skill(SkillList.BODY_CONTROL, 50.0f);
		builder.skill(SkillList.MIND_LOGICAL, 50.0f);
		builder.skill(SkillList.MIND_SPEED, 50.0f);
		builder.skill(SkillList.SOUL_STRENGTH, 50.0f);
		builder.skill(SkillList.SOUL_DEPTH, 50.0f);
		builder.skill(SkillList.WEAPONLESS_FIGHTING, 70.0f);
		builder.skill(SkillList.GROUP_FIGHTING, 70.0f);
		
		builder.boundsValues(-0.5f, -1.0f, 0.5f, 1.42f);
		builder.handDamString("bite");
		builder.kickDamString("wingbuff");
		builder.maxAge(200);
		builder.armourType(Servers.localServer.PVPSERVER ? ArmourTypes.ARMOUR_CLOTH : ArmourTypes.ARMOUR_SCALE_DRAGON);
		builder.baseCombatRating(25.0f);
		builder.combatDamageType(Wound.TYPE_COLD);
		builder.maxGroupAttackSize(10);

		templateId = builder.getTemplateId();
		return builder;
	}
	
	public ModVehicleBehaviour getVehicleBehaviour() {

		return new ModVehicleBehaviour() {

			@Override
			public void setSettingsForVehicle(Item item, Vehicle vehicle) {
			}

			@Override
			public void setSettingsForVehicle(Creature creature, Vehicle v) {
				VehicleFacade vehicle = wrap(v);

				vehicle.createPassengerSeats(0);
				vehicle.setSeatFightMod(0, 0.8f, 1.1f);
				vehicle.setSeatOffset(0, 0.0f, 0.0f, 0.0f);
				vehicle.setCreature(true);
				vehicle.setSkillNeeded(Servers.localServer.PVPSERVER ? 33f : 40f);
				vehicle.setName(creature.getName());
				vehicle.setMaxHeightDiff(Servers.localServer.PVPSERVER ? 0.15f : 0.10f);
				vehicle.setMaxDepth(-50f);
				vehicle.setMaxSpeed(53.0f);
				vehicle.setCommandType((byte) 3);
				vehicle.setCanHaveEquipment(true);
			}
		};
	}
	
	@Override
	public void addEncounters() {
		if (templateId == 0)
			return;

		new EncounterBuilder(Tiles.Tile.TILE_SNOW.id)
			.addCreatures(templateId, 1)
			.build(1);
		
		if(Servers.localServer.PVPSERVER){
			new EncounterBuilder(Tiles.Tile.TILE_TUNDRA.id)
				.addCreatures(templateId, 1)
				.build(1);
		}
	}
}
