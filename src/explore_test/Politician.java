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

public class Politician {
	
	public static RobotController rc;

    public static void act() throws GameActionException {
		Flag.set_default_patrol();
		RobotInfo nearest_ec = Info.closest_robot(Info.friendly, RobotType.ENLIGHTENMENT_CENTER);
		if (nearest_ec==null) {
	    	Pathing.spread();
		}
		else {
			int r2 = Info.loc.distanceSquaredTo(nearest_ec.location);
			if (rc.detectNearbyRobots(r2).length==1) {
				rc.empower(r2);
			}
			else {
				Pathing.target(Info.friendly, RobotType.ENLIGHTENMENT_CENTER, 1);
			}
		}
    }
    
    public static void pause() throws GameActionException {
		Flag.set_default_patrol();
    }

}