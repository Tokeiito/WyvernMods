# WyvernMods
## Creator: Sindusk

Wyvern Mods is the source code for basically all of the modifications that currently run on Wyvern Reborn. I've tried to make a full extensive list of what it encompasses in the first post of the server thread, but it doesn't really cover all of it. This is not a single mod by itself. This is source code for a gigantic collection of mods. Some highlights: 
  
* Titan System - Raid bosses that use special AoE abilities and summon minions. 
* Supply Depots - A depot appears in a random location with a beam of red light marking it's location for all players to see, and can award custom loot to players if they manage to successfully capture it. 
* Chaos & Enchanters Crystals - Custom items that can either greatly enhance or absolutely devastate an item they're used on. Meant as "late-game gambling" for those who don't want to settle for anything less than perfect. 
* Custom Bounty - Bounty that rewards based on a creature strength algorithm instead of a flat amount per creature. Beyond that, you can award players based on damage dealt, participation, or simply edit what drops on the corpse of any given creature. 
* Custom armour & weapons - Knuckles, Warhammers, and Clubs are new weapons. Spectral armour and glimmerscale for an "upgraded" version of drake and scale to make players even more powerful if they're willing to invest in it. 
* Treasure Boxes - A custom item which rewards random treasures. Simple by design, but ultra effective in practice. Great for rewards after unique slayings and other events. 
* So much more... 
  
## The Goal of Posting This 
  
My goal with releasing this source code is to give a "foundation" of sorts for other modders, allowing them to learn from what I've created so far, as it's pretty easy to see the cause -> effect in the modifications here. Beyond that, I'm hoping some of it is found useful by other server owners in creating their own custom content. You can piggyback your own ideas onto the systems I've created, making your own raid bosses with the Titan system and your own randomly placed "treasure hunting" events with the Supply Depots (perhaps even a public version of rifts!). Make your own custom creatures using mine as a template. Create your own items with fun interactions with my Chaos Crystals and Enchanters Crystals as a starting point. I think with a little bit of tweaking, other modders and server owners should be able to create some extremely interesting new content with what's provided here. 
  
The final reason I'm putting this to the public is because I feel as though I don't need to "keep" my content to myself in order to maintain a playerbase. I don't see servers as a competition of who can get the most players. Once people begin playing on a server, they are most likely going to stay there for as long as it has a playerbase. I don't feel it necessary to "entice" players to come to Wyvern Reborn because it has unique custom content. I think the server stability, community, and server setup (2k PvE map and 1k Arena) are more important than that - and those are the factors that will keep people playing. My hope is that other modders and server owners will feel similar, and share some of the content they've created as well. 
  
## AntiCheat and Miscellaneous Changes 
  
If you download the archive, you'll see that AntiCheat is part of WyvernMods. It has two components: Hiding hidden ores from LiveMap and XRay, and countering the advantage of ESP by hiding players, mounts, and lead creatures from local if they do not have vision of them (this component can cause sever lagg on larger servers and is currently a "beta" form of mod). The way this AntiCheat is handled is not counterable by client-side modding. The server culls information before the client receives it, meaning there's no way to adjust your client to counteract the change. 
  
I'd like to request anyone who is up for the challenge to split this out of WyvernMods and make a public anti-cheat available. I've been planning to do it myself, but the issues with the ESP counter have prevented me from going forward with it, since it could potentially destabilize a server that isn't aware of how it might affect them. I'm hoping someone can iterate on what I've done and create something truly remarkable that all servers can use to enhance the experience a bit further. 
  
Finally, there is a gigantic class called MiscChanges in WyvernMods. This is a collection of basically a year of QoL tweaks. I was planning to merge this into ServerTweaks to make it more enticing, but again, haven't had the time to do so. If anyone would like to split some of MiscChanges into a configurable mod for other server owners, it would probably go much appreciated. 
  
## Usage on Other Servers 
  
So by now, if you're a server owner, you're probably asking yourself "can I use this on my server, and if so, how much?" - If you're asking about consent from me, I give it fully. You're welcome to use absolutely anything provided here on your server and modify it in whatever way you like. Unconditionally. I don't need to be credited for anything or have any other conditions for use. Just take whatever looks interesting and go crazy. I'm curious to see what you can do with it. 
