package mod.sin.wyvern;

import org.gotti.wurmunlimited.modsupport.actions.ModActions;

import mod.sin.items.actions.SoulstealAction;

public class Soulstealing {
	public static void registerActions(){
		ModActions.registerAction(new SoulstealAction());
	}
}
