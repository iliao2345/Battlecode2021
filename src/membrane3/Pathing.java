package membrane3;

import battlecode.common.Clock;
import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.GameActionExceptionType;
import battlecode.common.GameConstants;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;
import battlecode.common.RobotInfo;
import battlecode.common.RobotType;
import battlecode.common.Team;

public class Pathing {
	public static RobotController rc;
	
    public static void spread() throws GameActionException {
    	target(Info.friendly, Info.type, -1);
    }
    public static void cluster() throws GameActionException {
    	target(Info.friendly, Info.type, 1);
    }
    public static void target(Team relevant_team, RobotType relevant_type, int chase_flee_sign) throws GameActionException {
    	RobotInfo closest_robot = Info.closest_robot(relevant_team, relevant_type);
        if (closest_robot==null) {
        	target(Info.loc.add(Info.last_move_direction), 1);
        }
        else {
        	target(closest_robot.location, -1);
        }
    }
    public static void target(MapLocation target, int chase_flee_sign) throws GameActionException {  // chase_flee_sign is 1 to chase target and -1 to flee target
    	target = get_closer_target(target, Math.min(2, Math2.length(Info.loc, target)-1));
    	rc.setIndicatorDot(target, 255, 0, 0);
    	Direction best_dir = null;
    	double best_cost = Integer.MAX_VALUE;
    	for (Direction dir:Direction.cardinalDirections()) {
    		if (rc.canMove(dir)) {
    			MapLocation adjacent = Info.loc.add(dir);
    			double cost = 1/rc.sensePassability(adjacent) + chase_flee_sign*(Math2.length(adjacent, target)+0.05*adjacent.distanceSquaredTo(target));
    			if (best_dir==null || cost<best_cost) {
    				best_dir = dir;
    				best_cost = cost;
    			}
    		}
    	}
    	for (Direction dir:Math2.DIAGONAL_DIRECTIONS) {
    		if (rc.canMove(dir)) {
    			MapLocation adjacent = Info.loc.add(dir);
    			double cost = 1/rc.sensePassability(adjacent) + chase_flee_sign*(Math2.length(adjacent, target)+0.05*adjacent.distanceSquaredTo(target));
    			if (best_dir==null || cost<best_cost) {
    				best_dir = dir;
    				best_cost = cost;
    			}
    		}
    	}
    	if (best_dir!=null) {
    		Action.move(best_dir);
    	}
    }
    public static void stick(MapLocation target) throws GameActionException {
    	int initial_distance_squared = Info.loc.distanceSquaredTo(target);
    	if (initial_distance_squared>2) {
	    	Direction best_dir = null;
	    	double best_passability = 0.1;
	    	int best_distance = Integer.MAX_VALUE;
	    	for (Direction dir:Math2.UNIT_DIRECTIONS) {
	    		if (rc.canMove(dir)) {
	    			MapLocation adjacent = Info.loc.add(dir);
	    			int distance = Math2.length(adjacent, target);
	    			double passability = rc.sensePassability(adjacent);
	    			if (best_dir==null || distance < best_distance || distance==best_distance && passability>best_passability) {
	    				best_dir = dir;
	    				best_distance = distance;
	    				best_passability = passability;
	    			}
	    		}
	    	}
	    	if (best_distance>Math2.length(Info.loc, target)) {
	    		best_dir = Direction.CENTER;
	    	}
	    	else if (best_distance==Math2.length(Info.loc, target)) {  // bug around if can't approach target, and stick to any enemies to trap them in
	    		Direction dir = Info.loc.directionTo(target);
	    		Direction left = Math2.cardinalize_right(dir).rotateLeft().rotateLeft();
	    		if (Math2.length(Info.loc.add(left), target)<best_distance) {left = left.rotateLeft().rotateLeft();}
	    		Direction right = Math2.cardinalize_left(dir).rotateRight().rotateRight();
	    		if (Math2.length(Info.loc.add(right), target)<best_distance) {right = right.rotateRight().rotateRight();}
	    		tryMoveLeft: if (rc.canMove(left)) {
	    			MapLocation blocking_loc = Info.loc.add(left.rotateRight().rotateRight().rotateRight());
	    			if (!rc.onTheMap(blocking_loc)) {
		    	    	Action.move(left);
		    	    	break tryMoveLeft;
	    			}
	    			RobotInfo robot = rc.senseRobotAtLocation(blocking_loc);
	    			if (robot==null) {
		    	    	Action.move(left);
		    	    	break tryMoveLeft;
	    			}
	    			if (robot.team!=Info.enemy) {
		    	    	Action.move(left);
		    	    	break tryMoveLeft;
	    			}
	    		}
	    		tryMoveRight: if (!Action.acted && rc.canMove(right)) {
	    			MapLocation blocking_loc = Info.loc.add(right.rotateLeft().rotateLeft().rotateLeft());
	    			if (!rc.onTheMap(blocking_loc)) {
		    	    	Action.move(right);
		    	    	break tryMoveRight;
	    			}
	    			RobotInfo robot = rc.senseRobotAtLocation(blocking_loc);
	    			if (robot==null) {
		    	    	Action.move(right);
		    	    	break tryMoveRight;
	    			}
	    			if (robot.team!=Info.enemy) {
		    	    	Action.move(right);
		    	    	break tryMoveRight;
	    			}
	    		}
	    	}
	    	else {
	    		Action.move(best_dir);
	    	}
    	}
    	else {  // bug around to make space for other things to stick
    		Direction dir = Info.loc.directionTo(target);
    		Direction left = Math2.cardinalize_right(dir).rotateLeft().rotateLeft();
    		Direction right = Math2.cardinalize_left(dir).rotateRight().rotateRight();
    		if (rc.canMove(left)) {
    	    	Action.move(left);
    		}
    		else if (rc.canMove(right)) {
    	    	Action.move(right);
    		}
    	}
    }
    public static MapLocation get_closer_target(MapLocation target, int iterations) throws GameActionException {  // computes how the target would approach us
    	if (!rc.canSenseLocation(target)) {return target;}
    	for (int i=0; i<iterations; i++) {
	    	Direction best_dir = null;
	    	double best_cost = Integer.MAX_VALUE;
	    	for (Direction dir:Direction.cardinalDirections()) {
				MapLocation adjacent = target.add(dir);
	    		if (rc.canSenseLocation(adjacent)) {
	    			double cost = 1/rc.sensePassability(adjacent) + Math2.length(adjacent, Info.loc)-0.05*adjacent.distanceSquaredTo(target);
	    			if (best_dir==null || cost<best_cost) {
	    				best_dir = dir;
	    				best_cost = cost;
	    			}
	    		}
	    	}
	    	for (Direction dir:Math2.DIAGONAL_DIRECTIONS) {
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
