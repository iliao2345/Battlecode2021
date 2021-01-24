package nerfedbuff;
import battlecode.common.*;

public class Guard {
	public static double MOMENTUM_CAP = 2;
	public static int DEFENSE_RADIUS_SQUARED = 25;
	public static int KILL_DISTANCE_SQUARED = 100;
	public static RobotController rc;
	
	public static MapLocation last_seen_defense_location = null;
	public static boolean too_dense;
	public static double guard_pressure_dx;
	public static double guard_pressure_dy;
	public static MapLocation closest_defense_location;
	public static int closest_defense_distance_squared;
	public static RobotInfo closest_enemy_muckraker;
	public static double momentum_dx;
	public static double momentum_dy;
	
	public static void update() throws GameActionException {
		closest_defense_distance_squared = Integer.MAX_VALUE;
		closest_defense_location = null;
		for (int i=Info.n_tracked_friendly_ecs; --i>=0;) {
			MapLocation tracked_ec_loc = new MapLocation(Info.tracked_friendly_ec_x.data, Info.tracked_friendly_ec_y.data);
			if (Info.loc.distanceSquaredTo(tracked_ec_loc)<closest_defense_distance_squared) {
				closest_defense_distance_squared = Info.loc.distanceSquaredTo(tracked_ec_loc);
				closest_defense_location = tracked_ec_loc;
				last_seen_defense_location = closest_defense_location;
			}
			Info.tracked_friendly_ec_ids = Info.tracked_friendly_ec_ids.next;
			Info.tracked_friendly_ec_x = Info.tracked_friendly_ec_x.next;
			Info.tracked_friendly_ec_y = Info.tracked_friendly_ec_y.next;
		}
		RobotInfo closest_friendly_slanderer = Info.closest_robot(Info.friendly, RobotType.SLANDERER);
		if (closest_friendly_slanderer!=null) {
			if (Info.loc.distanceSquaredTo(closest_friendly_slanderer.location)<closest_defense_distance_squared) {
				closest_defense_distance_squared = Info.loc.distanceSquaredTo(closest_friendly_slanderer.location);
				closest_defense_location = closest_friendly_slanderer.location;
				last_seen_defense_location = closest_defense_location;
			}
		}
		if (closest_defense_location==null) {
			if (last_seen_defense_location==null) {Role.attach_to_relay_chain(); return;}
			else {
				closest_defense_distance_squared = Info.loc.distanceSquaredTo(last_seen_defense_location);
				closest_defense_location = last_seen_defense_location;
			}
		}
		int n_guards = 0;
		guard_pressure_dx = 0;
		guard_pressure_dy = 0;
		for (int i=Info.n_guards; --i>=0;) {
			if (Clock.getBytecodesLeft()>1700) {
				MapLocation repel_loc = Info.guards[i].location;
				guard_pressure_dx -= 1000*(repel_loc.x-Info.x)/Info.loc.distanceSquaredTo(repel_loc);
				guard_pressure_dy -= 1000*(repel_loc.y-Info.y)/Info.loc.distanceSquaredTo(repel_loc);
				n_guards++;
			}
		}
		too_dense = n_guards >= 4;
		closest_enemy_muckraker = Info.closest_robot(Info.enemy, RobotType.MUCKRAKER);
		if (closest_enemy_muckraker!=null && !closest_defense_location.isWithinDistanceSquared(closest_enemy_muckraker.location, KILL_DISTANCE_SQUARED)) {
			closest_enemy_muckraker = null;
		}
	}
	
	public static void defend() throws GameActionException {
		if (closest_enemy_muckraker!=null) {
			if (Info.loc.isWithinDistanceSquared(closest_enemy_muckraker.location, 9)) {
				CombatInfo.compute_self_empower_gains();
				if (CombatInfo.damage_9 > closest_enemy_muckraker.conviction) {rc.empower(9); return;}
				if (CombatInfo.damage_8 > closest_enemy_muckraker.conviction) {rc.empower(8); return;}
				if (CombatInfo.damage_5 > closest_enemy_muckraker.conviction) {rc.empower(5); return;}
				if (CombatInfo.damage_4 > closest_enemy_muckraker.conviction) {rc.empower(4); return;}
				if (CombatInfo.damage_2 > closest_enemy_muckraker.conviction) {rc.empower(2); return;}
				if (CombatInfo.damage_1 > closest_enemy_muckraker.conviction) {rc.empower(1); return;}
				if (Info.loc.isWithinDistanceSquared(closest_enemy_muckraker.location, 1)) {
					rc.empower(1); return;
				}
//				if (closest_defense_location.isWithinDistanceSquared(closest_enemy_muckraker.location, 20)) {
//					rc.empower(Info.loc.distanceSquaredTo(closest_enemy_muckraker.location)); return;
//				}
			}
			if (closest_enemy_muckraker.location.isWithinDistanceSquared(closest_defense_location, KILL_DISTANCE_SQUARED)) {
				Pathing.approach(closest_enemy_muckraker.location, new boolean[3][3]); return;
			}
		}
		if (closest_defense_distance_squared<DEFENSE_RADIUS_SQUARED) {  // too close
			if (closest_defense_distance_squared<=1) {  // within slanderer lattice
				int closest_ec_distance = Integer.MAX_VALUE;
				MapLocation ec_location = new MapLocation(0, 0);  // guaranteed to be far from everything else
				for (int i=Info.n_tracked_friendly_ecs; --i>=0;) {
					MapLocation tracked_ec_loc = new MapLocation(Info.tracked_friendly_ec_x.data, Info.tracked_friendly_ec_y.data);
					if (Info.loc.distanceSquaredTo(tracked_ec_loc)<closest_ec_distance) {
						closest_ec_distance = Info.loc.distanceSquaredTo(tracked_ec_loc);
						ec_location = tracked_ec_loc;
					}
					Info.tracked_friendly_ec_ids = Info.tracked_friendly_ec_ids.next;
					Info.tracked_friendly_ec_x = Info.tracked_friendly_ec_x.next;
					Info.tracked_friendly_ec_y = Info.tracked_friendly_ec_y.next;
				}
				Direction momentum_direction = Info.last_move_direction;  // if cannot avoid odd tiles and friendly EC, try to take an outgoing line
				if (Math.random()<0.5) {momentum_direction = momentum_direction.rotateLeft().rotateLeft();}
				if (Math.random()<0.5) {momentum_direction = momentum_direction.rotateRight().rotateRight();}
				Direction best_direction = null;
				int lowest_cost = Integer.MAX_VALUE;
				for (Direction dir:Math2.UNIT_DIRECTIONS) {
					int cost = -dir.dx*momentum_direction.dx-dir.dy*momentum_direction.dy;
					boolean storage_location = ((Info.x+dir.dx-1)/2+(Info.y+dir.dy-1)/2)%2==1 && Info.loc.add(dir).isWithinDistanceSquared(ec_location, 10);
					boolean outgoing_line = ((Info.x+dir.dx-1)/2+(Info.y+dir.dy-1)/2)%2==0;
					if (outgoing_line && !storage_location && (rc.canMove(dir)||dir==Direction.CENTER) && (best_direction==null || cost<lowest_cost)) {
						best_direction = dir;
						lowest_cost = cost;
					}
				}
				if (best_direction!=null) {Action.move(best_direction); return;}
				int n_possible_directions = 0;
				best_direction = null;  // worst case move randomly
				for (Direction dir:Math2.UNIT_DIRECTIONS) {
					if (rc.canMove(dir) && Math.random()<1/(n_possible_directions+1)) {
						best_direction = dir;
					}
				}
				if (best_direction!=null) {Action.move(best_direction); return;}
			}
			else {
				Pathing.target(closest_defense_location, new boolean[3][3], -1); return;
			}
		}
		else if (too_dense) {  // too close
			if (guard_pressure_dx!=0||guard_pressure_dy!=0) {
		    	double r = Math.sqrt(guard_pressure_dx*guard_pressure_dx+guard_pressure_dy*guard_pressure_dy);
		    	guard_pressure_dx = guard_pressure_dx/r;
		    	guard_pressure_dy = guard_pressure_dy/r;
			}
			momentum_dx += guard_pressure_dx;
			momentum_dy += guard_pressure_dy;
	    	double r = Math.sqrt(momentum_dx*momentum_dx+momentum_dy*momentum_dy);
	    	if (r>MOMENTUM_CAP) {
		    	momentum_dx = momentum_dx/r*MOMENTUM_CAP;
		    	momentum_dy = momentum_dy/r*MOMENTUM_CAP;
	    	}
			MapLocation target_loc = Info.loc.translate((int)(10*momentum_dx), (int)(10*momentum_dy));
			Pathing.target(target_loc, new boolean[3][3], 1); return;
		}
		else  {  // too far, approach but rotate counterclockwise
			Pathing.target(closest_defense_location.translate(Info.x-closest_defense_location.x, closest_defense_location.y-Info.y), new boolean[3][3], 1); return;
		}
	}
}
