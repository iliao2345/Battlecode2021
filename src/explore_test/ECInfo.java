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

public class ECInfo {
	public static RobotController rc;
	public static int last_muckraker_power;

	public static void initialize(RobotController rc) {
		ECInfo.rc = rc;
		last_muckraker_power = -1;
	}
	
	public static void update() {
		
	}
}
