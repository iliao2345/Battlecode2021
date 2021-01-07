package explore_test;

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
		Flag.set_default_patrol();
    	if (Info.enemy_muckrakers.length>0) {
    		Pathing.target(Info.enemy, RobotType.MUCKRAKER, -1);
    	}
    	else {
    		Pathing.cluster();
    	}
    }
    
    public static void pause() throws GameActionException {
		Flag.set_default_patrol();
    }

}
