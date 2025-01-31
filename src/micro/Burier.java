package micro;
import battlecode.common.*;

public class Burier {
	public static RobotController rc;
	
	public static boolean need_support;
	public static int sampled_politician_conviction;
	public static RobotInfo target_ec;
	public static int target_ec_kill_conviction;
	
	public static void update() throws GameActionException {
    	target_ec = Info.closest_robot(Info.enemy, RobotType.ENLIGHTENMENT_CENTER);
    	if (target_ec==null) {
        	target_ec = Info.closest_robot(Team.NEUTRAL, RobotType.ENLIGHTENMENT_CENTER);
        	if (target_ec==null) {
        		Role.attach_to_relay_chain(); return;
        	}
        	else {
        		target_ec_kill_conviction = target_ec.conviction;
        	}
    	}
    	else {
    		target_ec_kill_conviction = target_ec.conviction*3;
    	}
		need_support = false;
		if (target_ec.team==Info.enemy) {
			loop1: for (Direction dir:Math2.UNIT_DIRECTIONS) {
				MapLocation check_loc = target_ec.location.add(dir);
				if (rc.canSenseLocation(check_loc)) {
					RobotInfo robot = rc.senseRobotAtLocation(check_loc);
					if (robot==null) {
						need_support = true;
						break loop1;
					}
					else if (robot.team==Info.enemy) {
						for (Direction dir2:Math2.UNIT_DIRECTIONS) {
							MapLocation check_loc2 = check_loc.add(dir2);
							if (!target_ec.location.isAdjacentTo(check_loc2) && rc.canSenseLocation(check_loc2)) {
								RobotInfo robot2 = rc.senseRobotAtLocation(check_loc2);
								if (robot2==null || robot2.team==Info.enemy) {
									need_support = true;
									break loop1;
								}
							}
						}
					}
				}
			}
		}
		if (Info.n_enemy_politicians>0) {
			sampled_politician_conviction = Info.enemy_politicians[(int)(Math.random()*Info.n_enemy_politicians)].conviction;
		}
		else {
			sampled_politician_conviction = 0;
		}
    }
	
	public static boolean bury(boolean[][] illegal_tiles) throws GameActionException {
		if (Info.crowdedness>0.5 && Info.exterminate && Info.conviction<=10 && Info.type==RobotType.POLITICIAN) {
    		rc.empower(1); Clock.yield(); return true;
    	}
		boolean[][] illegal_or_near_targetter_tiles = new boolean[3][3];
		for (Direction dir:Direction.allDirections()) {  // try to get more than 2 away from targetters
			MapLocation adjacent = Info.loc.add(dir);
			illegal_or_near_targetter_tiles[dir.dx+1][dir.dy+1] = illegal_tiles[dir.dx+1][dir.dy+1];
			for (int i=Info.n_targetters; --i>=0;) {
				illegal_or_near_targetter_tiles[dir.dx+1][dir.dy+1] = illegal_or_near_targetter_tiles[dir.dx+1][dir.dy+1]
						|| adjacent.isWithinDistanceSquared(Info.targetters[i].location, 2);
			}
		}
    	if (Pathing.stick(target_ec.location, illegal_or_near_targetter_tiles)) {return true;}
		illegal_or_near_targetter_tiles = new boolean[3][3];
		for (Direction dir:Direction.allDirections()) {  // try to get more than 1 away from targetters
			MapLocation adjacent = Info.loc.add(dir);
			illegal_or_near_targetter_tiles[dir.dx+1][dir.dy+1] = illegal_tiles[dir.dx+1][dir.dy+1];
			for (int i=Info.n_targetters; --i>=0;) {
				illegal_or_near_targetter_tiles[dir.dx+1][dir.dy+1] = illegal_or_near_targetter_tiles[dir.dx+1][dir.dy+1]
						|| adjacent.isWithinDistanceSquared(Info.targetters[i].location, 1);
			}
		}
    	return Pathing.stick(target_ec.location, illegal_or_near_targetter_tiles);
    }

}