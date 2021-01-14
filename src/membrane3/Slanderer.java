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

public class Slanderer {
	
	public static RobotController rc;

    public static void act() throws GameActionException {
    	if (Info.enemy_muckrakers.length>0) {  // flee from enemy muckrakers
    		RobotInfo closest_enemy_muckraker = Info.closest_robot(Info.enemy, RobotType.MUCKRAKER);
    		if (Info.loc.distanceSquaredTo(closest_enemy_muckraker.location)<=Membrane.BUFFER_PLUS_ONE_SQUARED) {
        		Pathing.target(Info.enemy, RobotType.MUCKRAKER, -1);
    		}
    	}
    	if (Action.can_still_move) {  // don't block the friendly EC
	    	RobotInfo closest_friendly_ec = Info.closest_robot(Info.friendly, RobotType.ENLIGHTENMENT_CENTER);
	    	if (closest_friendly_ec!=null) {
	    		if (Info.loc.isWithinDistanceSquared(closest_friendly_ec.location, 4)) {
	        		Pathing.target(closest_friendly_ec.location, -1);
	        	}
	    	}
    	}
    	if (Action.can_still_move && Info.need_protection) {  // form a bubble with membrane if need protection
    		herd();
    	}
    	if (Action.can_still_move && !Info.need_protection) {  // evade through gas if don't need protection
    		evade();
    	}
    	
		Flag.set_default_patrol();
    }
    
    public static void herd() throws GameActionException {
    	MapLocation ec_location = (Info.spawn_ec_location==null)? Info.spawn_location : Info.spawn_ec_location;
		Direction best_direction = null;  // get to spawn if possible through storage sites, and don't store near spawn
		int lowest_cost = Integer.MAX_VALUE;
		for (Direction dir:Direction.allDirections()) {
			int cost = (ec_location.x-Info.x-dir.dx)*(ec_location.x-Info.x-dir.dx)+(ec_location.y-Info.y-dir.dy)*(ec_location.y-Info.y-dir.dy);
			boolean storage_location = ((Info.x+dir.dx-1)/2+(Info.y+dir.dy-1)/2)%2==1 && !Info.loc.add(dir).isWithinDistanceSquared(ec_location, 10);
			if (storage_location && (rc.canMove(dir)||dir==Direction.CENTER) && (best_direction==null || cost<lowest_cost)) {
				best_direction = dir;
				lowest_cost = cost;
			}
			if (storage_location) {rc.setIndicatorDot(Info.loc.add(dir), 0, 0, 0);}
			else {rc.setIndicatorDot(Info.loc.add(dir), 255, 255, 255);}
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
			boolean outgoing_line = Math.abs((Info.x+dir.dx)%8-4)==Math.abs((Info.y+dir.dy)%8-4);
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
    
    public static void evade() throws GameActionException {
    	int dx = 0;
    	int dy = 0;
    	for (RobotInfo robot:Info.friendly_muckrakers) {
			int differential_warning_level = (rc.getFlag(robot.getID())>>10)%8 - Info.muckraker_warning_level;  // subtract 1 to induce a base attraction factor, so it doesn't wander away from the gas
    		dx += differential_warning_level*(robot.location.x-Info.x);
    		dy += differential_warning_level*(robot.location.y-Info.y);
    	}
    	for (RobotInfo robot:Info.friendly_politicians) {
			int differential_warning_level = (rc.getFlag(robot.getID())>>10)%8 - Info.muckraker_warning_level;  // subtract 1 to induce a base attraction factor, so it doesn't wander away from the gas
    		dx += differential_warning_level*(robot.location.x-Info.x);
    		dy += differential_warning_level*(robot.location.y-Info.y);
    	}
    	if (dx==0 && dy==0) {
//    		Pathing.cluster();
        }
        else {
        	Pathing.target(Info.loc.add(Info.loc.directionTo(Info.loc.translate(-dx, -dy))), 1);
        }
    }
    
    public static void pause() throws GameActionException {
		Flag.set_default_patrol();
    }

}
