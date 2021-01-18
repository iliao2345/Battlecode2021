package micro;
import battlecode.common.*;

public class Targetter {
	public static RobotController rc;

	public static MapLocation target_loc;
	
	public static void update() throws GameActionException {
		if (Info.loc.isWithinDistanceSquared(target_loc, 5)) {
			MapLocation best_target_loc = null;
			int best_target_size = Integer.MIN_VALUE;
			for (int i=Info.n_enemy_ecs; --i>=0;) {
				if (Info.enemy_ecs[i].conviction>best_target_size) {
					best_target_loc = Info.enemy_ecs[i].location;
					best_target_size = Info.enemy_ecs[i].conviction;
				}
			}
			if (best_target_loc==null) {
				best_target_size = Integer.MAX_VALUE;
				for (int i=Info.n_friendly_ecs; --i>=0;) {
					if (Info.friendly_ecs[i].conviction<best_target_size) {
						best_target_loc = Info.friendly_ecs[i].location;
						best_target_size = Info.friendly_ecs[i].conviction;
					}
				}
			}
			if (best_target_loc==null) {
				best_target_size = Integer.MAX_VALUE;
				for (int i=Info.n_neutral_ecs; --i>=0;) {
					if (Info.neutral_ecs[i].conviction<best_target_size) {
						best_target_loc = Info.neutral_ecs[i].location;
						best_target_size = Info.neutral_ecs[i].conviction;
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
		if (Info.loc.isWithinDistanceSquared(target_loc, 1) && rc.senseNearbyRobots(1).length==1) {
			rc.empower(1);
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
			Pathing.target(target_loc, new boolean[3][3], 1);
		}
	}
}
