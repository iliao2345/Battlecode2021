package membrane;

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

public class Action {
	public static RobotController rc;
	public static boolean acted;
	public static boolean can_still_move = true;

	public static void move(Direction dir) throws GameActionException {
		if (dir!=Direction.CENTER) { 
			Info.last_move_direction = dir;
			rc.move(dir);
			acted = true;
		}
		can_still_move = false;
	}
	public static void expose(RobotInfo robot) throws GameActionException {
		rc.expose(robot.location);
		acted = true;
		can_still_move = false;
	}
	public static void buildRobot(RobotType type, Direction dir, int conviction) throws GameActionException {
		rc.buildRobot(type, dir, conviction);
		acted = true;
		can_still_move = false;
		ECInfo.ids = new IntCycler(rc.senseRobotAtLocation(Info.loc.add(dir)).ID, ECInfo.ids);
		ECInfo.last_build_direction = dir;
	}
}
