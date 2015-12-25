GS.MonsterAttackTypes = {
	Melee: 0,
	Ranged: 1,
};

GS.Monsters = {
	nom: {
		size: new THREE.Vector3(16 * 0.45, 16 * 0.5,  16 * 0.45),
		scale: new THREE.Vector3(0.9, 0.9, 0.9),
		offset: new THREE.Vector3(16 * 0.45 * 0.5, 16 * 0.5 + 0.03, 16 * 0.45 * 0.5),
		speed: 0.75,
		rotationOffset: Math.PI,

		painChance: 0.25,
		attackType: GS.MonsterAttackTypes.Melee,
		maxHealth: 160,
		meleeDamage: 10,
		meleeAttackMaxCooldown: GS.msToFrames(500),
		meleeRange: 15,
		walkDelay: 5,
		bloodColor: new THREE.Color().setRGB(1, 0, 0).getHex(),

		roarSound: "monster_roar",
		meleeAttackSound: "monster_bite",
		painSound: "monster_pain",
		deathSound: "monster_death",
	},
	eye: {
		size: new THREE.Vector3(16 * 0.35, 16 * 0.5,  16 * 0.35),
		scale: new THREE.Vector3(0.5, 0.5, 0.5),
		offset: new THREE.Vector3(16 * 0.35 * 0.5, 16 * 0.5 + 0.03, 16 * 0.35 * 0.5),
		speed: 0.25,
		rotationOffset: Math.PI,

		painChance: 0.75,
		attackType: GS.MonsterAttackTypes.Ranged,
		maxHealth: 50,
		meleeRange: 15,
		rangedAttackMaxCooldown: GS.msToFrames(1500),
		rangedAttackCooldownRandomModifier: GS.msToFrames(1000),
		rangedAttackRange: 200,
		rangedAttackProjectile: "eye_bolt",
		rangedAttackChargeMaxCooldown: GS.msToFrames(500),
		preferredMaxDistance: 100,
		walkDelay: 15,
		bloodColor: new THREE.Color().setRGB(0, 1, 1).getHex(),

		roarSound: "monster_roar",
		rangedAttackChargeUpSound: "eye_charging",
		painSound: "monster_pain",
		deathSound: "monster_death",
	},
};