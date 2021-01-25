package muckspam;

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

public class Muckraker {
	
	public static RobotController rc;

    public static void act() throws GameActionException {
    	if (!Action.acted && Info.enemy_slanderers.length>0) {
    		Flag.is_gas = false;
    		RobotInfo closest = Info.closest_robot(Info.enemy, RobotType.SLANDERER);
    		if (Info.loc.distanceSquaredTo(closest.location)<=RobotType.MUCKRAKER.actionRadiusSquared) {
    			Action.expose(closest);
    			rc.setIndicatorLine(Info.loc, closest.location, 255, 0, 0);
    		}
    		else if (Action.can_still_move) {
    			Pathing.target(closest.location, 1);
    			rc.setIndicatorLine(Info.loc, closest.location, 128, 0, 0);
    		}
    	}
    	if (Action.can_still_move && Info.enemy_ecs.length>0) {
    		Flag.is_gas = false;
    		RobotInfo closest = Info.closest_robot(Info.enemy, RobotType.ENLIGHTENMENT_CENTER);
    		Pathing.stick(closest.location);
    	}
    	if (Action.can_still_move) {
    		Flag.is_gas = true;
    		rc.setIndicatorDot(Info.loc, 255,255,255);
    		Gas.attack();
    	}
		Flag.set_default_patrol();
    }
    
    public static void pause() throws GameActionException {
		Flag.set_default_patrol();
    }

}