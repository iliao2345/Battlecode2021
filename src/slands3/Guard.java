package slands3;
import battlecode.common.*;

public class Guard {
	public static RobotController rc;
	
	public static MapLocation defense_location = null;
	public static double force_dx;
	public static double force_dy;
	public static double normal_dx;
	public static double normal_dy;
	public static RobotInfo target_muckraker;
	public static int defense_layer;
	public static int layer_separation_squared;
	public static boolean see_inner_layer = false;
	public static MapLocation outpost;
	public static boolean reached_outpost = false;
	
	public static void update() throws GameActionException {
		reached_outpost = reached_outpost || Info.loc.isWithinDistanceSquared(outpost, 2);
		int defense_distance_squared = Integer.MAX_VALUE;
		for (int i=Info.n_guards; --i>=0;) {
			if (Clock.getBytecodesLeft()>1700) {
				defense_layer = Math.min(defense_layer, (rc.getFlag(Info.guards[i].ID)>>5)%8);
			}
		}
		defense_layer = Math.min(7, defense_layer+1);
		for (int i=Info.n_tracked_friendly_ecs; --i>=0;) {
			MapLocation tracked_ec_loc = new MapLocation(Info.tracked_friendly_ec_x.data, Info.tracked_friendly_ec_y.data);
			if (Info.loc.distanceSquaredTo(tracked_ec_loc)<defense_distance_squared) {
				defense_distance_squared = Info.loc.distanceSquaredTo(tracked_ec_loc);
				defense_location = tracked_ec_loc;
			}
			Info.tracked_friendly_ec_ids = Info.tracked_friendly_ec_ids.next;
			Info.tracked_friendly_ec_x = Info.tracked_friendly_ec_x.next;
			Info.tracked_friendly_ec_y = Info.tracked_friendly_ec_y.next;
		}
		if (Info.n_friendly_ecs>0) {defense_layer = 0;}
		RobotInfo closest_friendly_slanderer = Info.closest_robot(Info.friendly, RobotType.SLANDERER);
		if (closest_friendly_slanderer!=null) {
			if (Info.loc.distanceSquaredTo(closest_friendly_slanderer.location)<defense_distance_squared) {
				defense_distance_squared = Info.loc.distanceSquaredTo(closest_friendly_slanderer.location);
				defense_location = closest_friendly_slanderer.location;
				defense_layer = 0;
			}
		}
		normal_dx = 0;
		normal_dy = 0;
		force_dx = 0;
		force_dy = 0;
		layer_separation_squared = Integer.MAX_VALUE;
		for (int i=Info.n_guards; --i>=0;) {
			if (Clock.getBytecodesLeft()>1700) {
				MapLocation repel_loc = Info.guards[i].location;
				int layer_diff = (rc.getFlag(Info.guards[i].ID)>>5)%8 - defense_layer;
				if (layer_diff==0) {
					force_dx -= (double)(repel_loc.x-Info.x)/Info.loc.distanceSquaredTo(repel_loc);
					force_dy -= (double)(repel_loc.y-Info.y)/Info.loc.distanceSquaredTo(repel_loc);
				}
				if (layer_diff<0) {
					normal_dx -= (double)(repel_loc.x-Info.x)/Info.loc.distanceSquaredTo(repel_loc);
					normal_dy -= (double)(repel_loc.y-Info.y)/Info.loc.distanceSquaredTo(repel_loc);
					layer_separation_squared = Math.min(layer_separation_squared, Info.loc.distanceSquaredTo(repel_loc));
					see_inner_layer = true;
				}
			}
		}
		for (int i=Info.n_friendly_ecs; --i>=0;) {
			if (Clock.getBytecodesLeft()>1700) {
				MapLocation repel_loc = Info.friendly_ecs[i].location;
				normal_dx -= (double)(repel_loc.x-Info.x)/Info.loc.distanceSquaredTo(repel_loc);
				normal_dy -= (double)(repel_loc.y-Info.y)/Info.loc.distanceSquaredTo(repel_loc);
				layer_separation_squared = Math.min(layer_separation_squared, Info.loc.distanceSquaredTo(repel_loc));
				see_inner_layer = true;
			}
		}
		for (int i=Info.n_friendly_slanderers; --i>=0;) {
			if (Clock.getBytecodesLeft()>1700) {
				MapLocation repel_loc = Info.friendly_slanderers[i].location;
				normal_dx -= (double)(repel_loc.x-Info.x)/Info.loc.distanceSquaredTo(repel_loc);
				normal_dy -= (double)(repel_loc.y-Info.y)/Info.loc.distanceSquaredTo(repel_loc);
				layer_separation_squared = Math.min(layer_separation_squared, Info.loc.distanceSquaredTo(repel_loc));
				see_inner_layer = true;
			}
		}
		int closest_distance_squared = Integer.MAX_VALUE;
		int greatest_kill_size = -1;
		target_muckraker = null;
		for (int i=Info.n_enemy_muckrakers; --i>=0;) {
			int distance_squared = Info.loc.distanceSquaredTo(Info.enemy_muckrakers[i].location);
			int kill_size = Info.enemy_muckrakers[i].conviction;
			if (kill_size>greatest_kill_size && kill_size<(int)((Info.conviction-GameConstants.EMPOWER_TAX)*Info.empower_buff) || kill_size==greatest_kill_size && distance_squared<closest_distance_squared) {
				greatest_kill_size = kill_size;
				closest_distance_squared = distance_squared;
				target_muckraker = Info.enemy_muckrakers[i];
			}
		}
		if (target_muckraker==null) {target_muckraker = Info.closest_robot(Info.enemy, RobotType.MUCKRAKER);}
		if (target_muckraker!=null) {
			defense_layer = 7;
		}
	}
	
	public static void defend() throws GameActionException {
		if (target_muckraker!=null) {
			if (Info.loc.isWithinDistanceSquared(target_muckraker.location, 1)) {
				CombatInfo.compute_self_empower_gains();
				int best_kills = -Integer.MAX_VALUE;
				int best_costs = -Integer.MAX_VALUE;
				int best_radius_squared = 1;
				boolean can_kill = false;
				if (CombatInfo.kills_9>=best_kills || CombatInfo.kills_9==best_kills && CombatInfo.costs_9>=best_costs && CombatInfo.damage_9>target_muckraker.conviction) {best_radius_squared = 9; best_kills = CombatInfo.kills_9; best_costs = CombatInfo.costs_9; can_kill = true;}
				if (CombatInfo.kills_8>=best_kills || CombatInfo.kills_8==best_kills && CombatInfo.costs_8>=best_costs && CombatInfo.damage_8>target_muckraker.conviction) {best_radius_squared = 8; best_kills = CombatInfo.kills_8; best_costs = CombatInfo.costs_8; can_kill = true;}
				if (CombatInfo.kills_5>=best_kills || CombatInfo.kills_5==best_kills && CombatInfo.costs_5>=best_costs && CombatInfo.damage_5>target_muckraker.conviction) {best_radius_squared = 5; best_kills = CombatInfo.kills_5; best_costs = CombatInfo.costs_5; can_kill = true;}
				if (CombatInfo.kills_4>=best_kills || CombatInfo.kills_4==best_kills && CombatInfo.costs_4>=best_costs && CombatInfo.damage_4>target_muckraker.conviction) {best_radius_squared = 4; best_kills = CombatInfo.kills_4; best_costs = CombatInfo.costs_4; can_kill = true;}
				if (CombatInfo.kills_2>=best_kills || CombatInfo.kills_2==best_kills && CombatInfo.costs_2>=best_costs && CombatInfo.damage_2>target_muckraker.conviction) {best_radius_squared = 2; best_kills = CombatInfo.kills_2; best_costs = CombatInfo.costs_2; can_kill = true;}
				if (CombatInfo.kills_1>=best_kills || CombatInfo.kills_1==best_kills && CombatInfo.costs_1>=best_costs && CombatInfo.damage_1>target_muckraker.conviction) {best_radius_squared = 1; best_kills = CombatInfo.kills_1; best_costs = CombatInfo.costs_1; can_kill = true;}
				if (can_kill) {
					rc.empower(best_radius_squared); return;
				}
				if (CombatInfo.kills_9>=best_kills || CombatInfo.kills_9==best_kills && CombatInfo.costs_9>=best_costs) {best_radius_squared = 9; best_kills = CombatInfo.kills_9; best_costs = CombatInfo.costs_9;}
				if (CombatInfo.kills_8>=best_kills || CombatInfo.kills_8==best_kills && CombatInfo.costs_8>=best_costs) {best_radius_squared = 8; best_kills = CombatInfo.kills_8; best_costs = CombatInfo.costs_8;}
				if (CombatInfo.kills_5>=best_kills || CombatInfo.kills_5==best_kills && CombatInfo.costs_5>=best_costs) {best_radius_squared = 5; best_kills = CombatInfo.kills_5; best_costs = CombatInfo.costs_5;}
				if (CombatInfo.kills_4>=best_kills || CombatInfo.kills_4==best_kills && CombatInfo.costs_4>=best_costs) {best_radius_squared = 4; best_kills = CombatInfo.kills_4; best_costs = CombatInfo.costs_4;}
				if (CombatInfo.kills_2>=best_kills || CombatInfo.kills_2==best_kills && CombatInfo.costs_2>=best_costs) {best_radius_squared = 2; best_kills = CombatInfo.kills_2; best_costs = CombatInfo.costs_2;}
				if (CombatInfo.kills_1>=best_kills || CombatInfo.kills_1==best_kills && CombatInfo.costs_1>=best_costs) {best_radius_squared = 1; best_kills = CombatInfo.kills_1; best_costs = CombatInfo.costs_1;}
				rc.empower(best_radius_squared); return;
			}
			rc.setIndicatorLine(Info.loc, target_muckraker.location, 0, 255, 0);
			Pathing.approach(target_muckraker.location, new boolean[3][3]); return;
		}
		else if (!reached_outpost) {
			rc.setIndicatorLine(Info.loc, outpost, 0, 255, 0);
			Pathing.approach(outpost, new boolean[3][3]); return;
		}
		else if (layer_separation_squared<=17) {  // too close
			MapLocation target_loc = Info.loc.translate((int)(1000*(force_dx+normal_dx)), (int)(1000*(force_dy+normal_dy)));
			Pathing.approach(target_loc, new boolean[3][3]); return;
		}
		else if (see_inner_layer) {  // too far, can see others though
			MapLocation target_loc = Info.loc.translate((int)(1000*(force_dx-normal_dx)), (int)(1000*(force_dy-normal_dy)));
			Pathing.approach(target_loc, new boolean[3][3]); return;
		}
		else if (defense_location!=null) {  // too far, can't see others
			Pathing.target(defense_location, new boolean[3][3], 1); return;
		}
		else {
			Role.attach_to_relay_chain();
		}
	}
}
