package buryguard;
import static org.junit.Assert.assertNotNull;

import battlecode.common.*;

public class Slanderer {
	public static double MOMENTUM_CAP = 2;
	public static RobotController rc;
	
	public static double momentum_dx = 0;
	public static double momentum_dy = 0;
	
	public static void act() throws GameActionException{  ////////////////////////////////////////////////////////////////////////// not working correctly
		if (Info.n_enemy_muckrakers>0) {
			RobotInfo closest_enemy_muckraker = Info.closest_robot(Info.enemy, RobotType.MUCKRAKER);
	    	double r = Math.sqrt(Info.loc.distanceSquaredTo(closest_enemy_muckraker.location));
	    	momentum_dx = (Info.x-closest_enemy_muckraker.location.x)/r*MOMENTUM_CAP;
	    	momentum_dy = (Info.y-closest_enemy_muckraker.location.y)/r*MOMENTUM_CAP;
			Pathing.target(closest_enemy_muckraker.location, new boolean[3][3], -1); return;
		}
//		if (Info.n_guards==0) {
//			double dx = 0;
//    		double dy = 0;
//			for (int i=Info.n_friendly_slanderers; --i>=0;) {
//				if (Clock.getBytecodesLeft()>1700) {
//    				MapLocation repel_loc = Info.friendly_slanderers[i].location;
//    				dx -= 1000*(repel_loc.x-Info.x)/Info.loc.distanceSquaredTo(repel_loc);
//    				dy -= 1000*(repel_loc.y-Info.y)/Info.loc.distanceSquaredTo(repel_loc);
//				}
//			}
//			if (dx!=0||dy!=0) {
//		    	double r = Math.sqrt(dx*dx+dy*dy);
//		    	dx = dx/r;
//		    	dy = dy/r;
//			}
//			momentum_dx += dx;
//			momentum_dy += dy;
//	    	double r = Math.sqrt(momentum_dx*momentum_dx+momentum_dy*momentum_dy);
//	    	if (r>MOMENTUM_CAP) {
//		    	momentum_dx = momentum_dx/r*MOMENTUM_CAP;
//		    	momentum_dy = momentum_dy/r*MOMENTUM_CAP;
//	    	}
//			MapLocation target_loc = Info.loc.translate((int)(10*momentum_dx), (int)(10*momentum_dy));
//			Pathing.target(target_loc, new boolean[3][3], 1); return;
//		}
		
		MapLocation ec_location = new MapLocation(Info.tracked_friendly_ec_x.data, Info.tracked_friendly_ec_y.data);
		Direction best_direction = null;  // get to spawn if possible through storage sites, and don't store near spawn
		int lowest_cost = Integer.MAX_VALUE;
		for (Direction dir:Direction.allDirections()) {
			int cost = (ec_location.x-Info.x-dir.dx)*(ec_location.x-Info.x-dir.dx)+(ec_location.y-Info.y-dir.dy)*(ec_location.y-Info.y-dir.dy);
			boolean storage_location = ((Info.x+dir.dx-1)/2+(Info.y+dir.dy-1)/2)%2==1 && !Info.loc.add(dir).isWithinDistanceSquared(ec_location, 10);
			if (storage_location && (rc.canMove(dir)||dir==Direction.CENTER) && (best_direction==null || cost<lowest_cost)) {
				best_direction = dir;
				lowest_cost = cost;
			}
		}
		if (best_direction!=null) {Action.move(best_direction); return;}
		Direction momentum_direction = Info.last_move_direction;  // if cannot avoid odd tiles and friendly EC, try to take an outgoing line
		if (Math.random()<0.5) {momentum_direction = momentum_direction.rotateLeft().rotateLeft();}
		if (Math.random()<0.5) {momentum_direction = momentum_direction.rotateRight().rotateRight();}
		best_direction = null;
		lowest_cost = Integer.MAX_VALUE;
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
	
	public static void pause() {
		
	}

}
