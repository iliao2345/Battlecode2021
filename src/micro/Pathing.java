package micro;
import battlecode.common.*;

public class Pathing {
	public static RobotController rc;
	
    public static boolean target(MapLocation target, boolean[][] illegal_tiles, int chase_flee_sign) throws GameActionException {  // chase_flee_sign is 1 to chase target and -1 to flee target
    	target = get_closer_target(target, Math.min(2, Math2.length(Info.loc, target)-1));
    	Direction best_dir = null;
    	double best_cost = Integer.MAX_VALUE;
    	for (Direction dir:Math2.UNIT_DIRECTIONS) {
    		if (rc.canMove(dir)) {
    			MapLocation adjacent = Info.loc.add(dir);
    			double cost = 1/rc.sensePassability(adjacent) + chase_flee_sign*Math.sqrt(adjacent.distanceSquaredTo(target));
    			if (cost<best_cost && !illegal_tiles[dir.dx+1][dir.dy+1]) {
    				best_dir = dir;
    				best_cost = cost;
    			}
    		}
    	}
    	if (best_dir!=null) {
    		Action.move(best_dir);
    		return true;
    	}
    	return false;
    }
    public static boolean stick(MapLocation target, boolean[][] illegal_tiles) throws GameActionException {
    	int initial_distance_squared = Info.loc.distanceSquaredTo(target);
    	Direction best_dir = null;
    	double best_passability = 0.1;
    	int best_distance = Integer.MAX_VALUE;
    	for (Direction dir:Direction.allDirections()) {
    		if ((rc.canMove(dir) || dir==Direction.CENTER) && !illegal_tiles[dir.dx+1][dir.dy+1]) {
    			MapLocation adjacent = Info.loc.add(dir);
    			int distance = Math2.length(adjacent, target);
    			double passability = rc.sensePassability(adjacent);
    			if (distance < best_distance || distance==best_distance && passability>best_passability) {
    				best_dir = dir;
    				best_distance = distance;
    				best_passability = passability;
    			}
    		}
    	}
    	if (best_dir==null) {return false;}
    	if (best_distance>Math2.length(Info.loc, target)) {  // can only go farther, can't even stay at same distance legally
    		Action.move(best_dir); return true;
    	}
    	if (best_distance==Math2.length(Info.loc, target)) {  // bug around if can't approach target legally, and stick to any enemies to trap them in
    		Direction dir = Info.loc.directionTo(target);
    		Direction left = Math2.cardinalize_right(dir).rotateLeft().rotateLeft();
    		if (Math2.length(Info.loc.add(left), target)<best_distance) {left = left.rotateLeft().rotateLeft();}
    		Direction right = Math2.cardinalize_left(dir).rotateRight().rotateRight();
    		if (Math2.length(Info.loc.add(right), target)<best_distance) {right = right.rotateRight().rotateRight();}
    		if (!illegal_tiles[left.dx+1][left.dy+1] && rc.canMove(left)) {
    			MapLocation blocking_loc = Info.loc.add(left.rotateRight().rotateRight().rotateRight());
    			if (!rc.onTheMap(blocking_loc)) {
	    	    	Action.move(left); return true;
    			}
    			RobotInfo robot = rc.senseRobotAtLocation(blocking_loc);
    			if (robot==null) {
	    	    	Action.move(left); return true;
    			}
    			if (robot.team!=Info.enemy) {
	    	    	Action.move(left); return true;
    			}
    		}
    		if (!Action.acted && !illegal_tiles[right.dx+1][right.dy+1] && rc.canMove(right)) {
    			MapLocation blocking_loc = Info.loc.add(right.rotateLeft().rotateLeft().rotateLeft());
    			if (!rc.onTheMap(blocking_loc)) {
	    	    	Action.move(right); return true;
    			}
    			RobotInfo robot = rc.senseRobotAtLocation(blocking_loc);
    			if (robot==null) {
	    	    	Action.move(right); return true;
    			}
    			if (robot.team!=Info.enemy) {
	    	    	Action.move(right); return true;
    			}
    		}
    		Action.move(best_dir); return true;
    	}
    	else {  // can approach target legally
    		Action.move(best_dir); return true;
    	}
    }
    public static MapLocation get_closer_target(MapLocation target, int iterations) throws GameActionException {  // computes how the target would approach us
    	if (!rc.canSenseLocation(target)) {return target;}
    	for (int i=0; i<iterations; i++) {
	    	Direction best_dir = null;
	    	double best_cost = Integer.MAX_VALUE;
	    	for (Direction dir:Math2.UNIT_DIRECTIONS) {
				MapLocation adjacent = target.add(dir);
	    		if (rc.canSenseLocation(adjacent)) {
	    			double cost = 1/rc.sensePassability(adjacent) + Math2.length(adjacent, Info.loc)-0.05*adjacent.distanceSquaredTo(target);
	    			if (best_dir==null || cost<best_cost) {
	    				best_dir = dir;
	    				best_cost = cost;
	    			}
	    		}
	    	}
	    	target = target.add(best_dir);
    	}
    	return target;
    }
}
