package slands2;
import battlecode.common.*;

public class Targetter {
//	public static int TARGET_ALL_ECS_TIME = 1100;
	public static int TARGET_ALL_ECS_TIME = 1500;
	public static RobotController rc;

	public static MapLocation target_loc;
	public static Team target_team = null;
	public static int target_size;
	public static boolean move_out_of_the_way_flag;
	
	public static void update() throws GameActionException {
		if (Info.loc.isWithinDistanceSquared(target_loc, 8)) {
			move_out_of_the_way_flag = true;
			MapLocation best_target_loc = null;
			target_size = Integer.MAX_VALUE;
			boolean can_capture_enemy = false;
			for (int i=Info.n_enemy_ecs; --i>=0;) {
				if (Info.enemy_ecs[i].conviction<target_size) {
					best_target_loc = Info.enemy_ecs[i].location;
					target_size = Info.enemy_ecs[i].conviction;
					target_team = Info.enemy;
					can_capture_enemy = can_capture_enemy || Info.conviction>3*target_size;
				}
			}
			if (best_target_loc==null || !can_capture_enemy) {
				target_size = Integer.MAX_VALUE;
				for (int i=Info.n_neutral_ecs; --i>=0;) {
					if (Info.neutral_ecs[i].conviction<target_size) {
						best_target_loc = Info.neutral_ecs[i].location;
						target_size = Info.neutral_ecs[i].conviction;
						target_team = Team.NEUTRAL;
					}
				}
			}
			if (best_target_loc==null) {
				target_size = Integer.MAX_VALUE;
				for (int i=Info.n_friendly_ecs; --i>=0;) {
					if (Info.friendly_ecs[i].conviction<target_size) {
						best_target_loc = Info.friendly_ecs[i].location;
						target_size = Info.friendly_ecs[i].conviction;
						target_team = Info.friendly;
					}
				}
			}
			if (best_target_loc==null) {
				target_team = null;
			}
			else {
				target_loc = best_target_loc;
			}
		}
		else {
			move_out_of_the_way_flag = false;
		}
	}
		
	public static void target() throws GameActionException {
		CombatInfo.compute_self_empower_gains();
		if (target_team==Info.enemy) {
			if (CombatInfo.kills_1>CombatInfo.EC_UNIT_EQUIVALENT-2) {rc.empower(1); return;}
			if (CombatInfo.kills_2>CombatInfo.EC_UNIT_EQUIVALENT-2) {rc.empower(2); return;}
			if (CombatInfo.kills_4>CombatInfo.EC_UNIT_EQUIVALENT-2) {rc.empower(4); return;}
			if (CombatInfo.kills_5>CombatInfo.EC_UNIT_EQUIVALENT-2) {rc.empower(5); return;}
			if (CombatInfo.kills_8>CombatInfo.EC_UNIT_EQUIVALENT-2) {rc.empower(8); return;}
			if (CombatInfo.kills_9>CombatInfo.EC_UNIT_EQUIVALENT-2) {rc.empower(9); return;}
			if (Info.loc.isWithinDistanceSquared(target_loc, 1)) {rc.empower(1); Clock.yield(); return;}
		}
		else if (target_team==Team.NEUTRAL) {
			if (CombatInfo.kills_1>CombatInfo.NEUTRAL_EC_UNIT_EQUIVALENT-2 && CombatInfo.damage_1-target_size>=Info.nearby_enemy_power) {rc.empower(1); return;}
			if (CombatInfo.kills_2>CombatInfo.NEUTRAL_EC_UNIT_EQUIVALENT-2 && CombatInfo.damage_2-target_size>=Info.nearby_enemy_power) {rc.empower(2); return;}
			if (CombatInfo.kills_4>CombatInfo.NEUTRAL_EC_UNIT_EQUIVALENT-2 && CombatInfo.damage_4-target_size>=Info.nearby_enemy_power) {rc.empower(4); return;}
			if (CombatInfo.kills_5>CombatInfo.NEUTRAL_EC_UNIT_EQUIVALENT-2 && CombatInfo.damage_5-target_size>=Info.nearby_enemy_power) {rc.empower(5); return;}
			if (CombatInfo.kills_8>CombatInfo.NEUTRAL_EC_UNIT_EQUIVALENT-2 && CombatInfo.damage_8-target_size>=Info.nearby_enemy_power) {rc.empower(8); return;}
			if (CombatInfo.kills_9>CombatInfo.NEUTRAL_EC_UNIT_EQUIVALENT-2 && CombatInfo.damage_9-target_size>=Info.nearby_enemy_power) {rc.empower(9); return;}
		}
		else if (target_team==Info.friendly) {  // friendly target
			int distance_squared = Info.loc.distanceSquaredTo(target_loc);
			if (CombatInfo.costs_1>0) {rc.empower(1); return;}  // extra cases to help out with self-empower
			else if (CombatInfo.costs_2>0) {rc.empower(2); return;}
			else if (CombatInfo.costs_4>0) {rc.empower(4); return;}
			else if (CombatInfo.costs_5>0) {rc.empower(5); return;}
			else if (CombatInfo.costs_8>0) {rc.empower(8); return;}
			else if (CombatInfo.costs_9>0) {rc.empower(9); return;}
			else if (rc.senseNearbyRobots(distance_squared).length==1 && distance_squared<=9) {rc.empower(distance_squared); Clock.yield(); return;}
		}
		if (Info.loc.isWithinDistanceSquared(target_loc, 36)) {
			boolean[][] illegal_or_near_targetter_tiles = new boolean[3][3];
			for (Direction dir:Direction.allDirections()) {  // try to get more than 1 away from targetters
				MapLocation adjacent = Info.loc.add(dir);
				for (int i=Info.n_targetters; --i>=0;) {
					illegal_or_near_targetter_tiles[dir.dx+1][dir.dy+1] = illegal_or_near_targetter_tiles[dir.dx+1][dir.dy+1]
							|| adjacent.isWithinDistanceSquared(Info.targetters[i].location, 1) && (rc.getFlag(Info.targetters[i].ID)>>19)%2==1;
				}
			}
			Pathing.approach(target_loc, illegal_or_near_targetter_tiles);
			move_out_of_the_way_flag = true;
		}
		else {
			Pathing.approach(target_loc, new boolean[3][3]);
			move_out_of_the_way_flag = rc.getLocation().distanceSquaredTo(target_loc)==Info.loc.distanceSquaredTo(target_loc);
		}
	}
}
