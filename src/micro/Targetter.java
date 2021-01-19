package micro;
import battlecode.common.*;

public class Targetter {
	public static int TARGET_ALL_ECS_TIME = 1100;
	public static RobotController rc;

	public static MapLocation target_loc;
	public static Team target_team = null;
	
	public static void update() throws GameActionException {
		if (Info.loc.isWithinDistanceSquared(target_loc, 5)) {
			MapLocation best_target_loc = null;
			int best_target_size = Integer.MIN_VALUE;
			for (int i=Info.n_enemy_ecs; --i>=0;) {
				if (Info.enemy_ecs[i].conviction>best_target_size) {
					best_target_loc = Info.enemy_ecs[i].location;
					best_target_size = Info.enemy_ecs[i].conviction;
					target_team = Info.enemy;
				}
			}
			if (best_target_loc==null) {
				best_target_size = Integer.MAX_VALUE;
				for (int i=Info.n_friendly_ecs; --i>=0;) {
					if (Info.friendly_ecs[i].conviction<best_target_size) {
						best_target_loc = Info.friendly_ecs[i].location;
						best_target_size = Info.friendly_ecs[i].conviction;
						target_team = Info.friendly;
					}
				}
			}
			if (best_target_loc==null) {
				best_target_size = Integer.MAX_VALUE;
				for (int i=Info.n_neutral_ecs; --i>=0;) {
					if (Info.neutral_ecs[i].conviction<best_target_size) {
						best_target_loc = Info.neutral_ecs[i].location;
						best_target_size = Info.neutral_ecs[i].conviction;
						target_team = Team.NEUTRAL;
					}
				}
			}
			if (best_target_loc==null) {
//				target_loc = best_target_loc;
//				throw new GameActionException(null, "Targetter was sent to destroy nothing!");
			}
			else {
				target_loc = best_target_loc;
			}
		}
	}
		
	public static void target() throws GameActionException {
		CombatInfo.compute_self_empower_gains();
		if (CombatInfo.kills_1>CombatInfo.NEUTRAL_EC_UNIT_EQUIVALENT-2) {rc.empower(1); return;}
		if (CombatInfo.kills_2>CombatInfo.NEUTRAL_EC_UNIT_EQUIVALENT-2) {rc.empower(2); return;}
		if (CombatInfo.kills_4>CombatInfo.NEUTRAL_EC_UNIT_EQUIVALENT-2) {rc.empower(4); return;}
		if (CombatInfo.kills_5>CombatInfo.NEUTRAL_EC_UNIT_EQUIVALENT-2) {rc.empower(5); return;}
		if (CombatInfo.kills_8>CombatInfo.NEUTRAL_EC_UNIT_EQUIVALENT-2) {rc.empower(8); return;}
		if (CombatInfo.kills_9>CombatInfo.NEUTRAL_EC_UNIT_EQUIVALENT-2) {rc.empower(9); return;}
		if (Info.loc.isWithinDistanceSquared(target_loc, 1)) {
			if (target_team==Info.enemy) {rc.empower(1); Clock.yield(); return;}
			else if (rc.senseNearbyRobots(1).length==1) {rc.empower(1); Clock.yield(); return;}
		}
		else if (Info.loc.isWithinDistanceSquared(target_loc, 36)) {
			boolean[][] illegal_or_near_targetter_tiles = new boolean[3][3];
			for (Direction dir:Direction.allDirections()) {  // try to get more than 2 away from targetters
				MapLocation adjacent = Info.loc.add(dir);
				for (int i=Info.n_targetters; --i>=0;) {
					illegal_or_near_targetter_tiles[dir.dx+1][dir.dy+1] = illegal_or_near_targetter_tiles[dir.dx+1][dir.dy+1]
							|| adjacent.isWithinDistanceSquared(Info.targetters[i].location, 2);
				}
			}
	    	if (Pathing.stick(target_loc, illegal_or_near_targetter_tiles)) {return;}
			illegal_or_near_targetter_tiles = new boolean[3][3];
			for (Direction dir:Direction.allDirections()) {  // try to get more than 1 away from targetters
				MapLocation adjacent = Info.loc.add(dir);
				for (int i=Info.n_targetters; --i>=0;) {
					illegal_or_near_targetter_tiles[dir.dx+1][dir.dy+1] = illegal_or_near_targetter_tiles[dir.dx+1][dir.dy+1]
							|| adjacent.isWithinDistanceSquared(Info.targetters[i].location, 1);
				}
			}
			Pathing.stick(target_loc, illegal_or_near_targetter_tiles);
		}
		else {
			Pathing.stick(target_loc, new boolean[3][3]);
		}
	}
}
