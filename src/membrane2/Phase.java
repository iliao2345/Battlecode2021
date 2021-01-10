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

public class Phase {
	
	public static RobotController rc;
	public static boolean is_gas = true;
	public static boolean is_membrane = false;
	
	public static void evaporate() throws GameActionException {
		is_gas = true;
		is_membrane = false;
	}
	
	public static void condense() throws GameActionException {
		is_gas = false;
		is_membrane = true;
		Membrane.bugsign = Math.random()<0.5;
	}
}
