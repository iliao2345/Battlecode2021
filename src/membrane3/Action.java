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
		int type_num = 0;
		switch (type) {
		case ENLIGHTENMENT_CENTER: {type_num = 0; break;}
		case POLITICIAN: {type_num = 1; break;}
		case SLANDERER: {type_num = 2; break;}
		case MUCKRAKER: {type_num = 3; break;}
		}
		ECInfo.types = new IntCycler(type_num, ECInfo.types);
		System.out.println(type_num);
		ECInfo.last_build_direction = dir;
	}
}
