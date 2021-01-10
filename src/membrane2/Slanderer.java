package membrane2;

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
    	if (Info.enemy_muckrakers.length>0) {
    		RobotInfo closest_enemy_muckraker = Info.closest_robot(Info.enemy, RobotType.MUCKRAKER);
    		if (Info.loc.distanceSquaredTo(closest_enemy_muckraker.location)<=Membrane.BUFFER_PLUS_ONE_SQUARED) {
        		Pathing.target(Info.enemy, RobotType.MUCKRAKER, -1);
    		}
    	}
    	if (Action.can_still_move) {
	    	RobotInfo closest_friendly_ec = Info.closest_robot(Info.friendly, RobotType.ENLIGHTENMENT_CENTER);
	    	if (closest_friendly_ec!=null) {
	    		if (Info.loc.isWithinDistanceSquared(closest_friendly_ec.location, 2)) {
	        		Pathing.target(closest_friendly_ec.location, -1);
	        	}
	    	}
    	}
    	if (Action.can_still_move) {
    		herd();
    	}
    	
		Flag.set_default_patrol();
    }
    
    public static void herd() throws GameActionException {
		int dx = 0;
		int dy = 0;
		int n = 0;
		for (RobotInfo robot:Info.friendly_slanderers) {
			dx += robot.location.x - Info.x;
			dy += robot.location.y - Info.y;
			n += 1;
		}
		if ((dx*dx+dy*dy)<=2*n*n*n+5) {return;}
		for (RobotInfo robot:Info.friendly_ecs) {
			dx += robot.location.x - Info.x;
			dy += robot.location.y - Info.y;
		}
		Direction best_direction = null;
		int lowest_cost = Integer.MAX_VALUE;
		for (Direction dir:Math2.UNIT_DIRECTIONS) {
			int cost = -dx*dir.dx-dy*dir.dy;
			boolean blocking_spawn = false;
			for (RobotInfo robot:Info.friendly_ecs) {
				if (Info.loc.add(dir).isWithinDistanceSquared(robot.location, 2)) {
					blocking_spawn = true;
				}
			}
			if (!blocking_spawn && rc.canMove(dir) && (best_direction==null || lowest_cost>cost)) {
				best_direction = dir;
				lowest_cost = cost;
			}
		}
		if (best_direction!=null) {Action.move(best_direction);}
    }
    
    public static void pause() throws GameActionException {
		Flag.set_default_patrol();
    }

}
