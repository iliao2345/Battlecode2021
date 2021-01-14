package micro;
import battlecode.common.*;

public class Action {
	public static RobotController rc;
	public static boolean acted;

	public static void move(Direction dir) throws GameActionException {
		if (dir!=Direction.CENTER) { 
			Info.last_move_direction = dir;
			rc.move(dir);
			acted = true;
		}
	}
	public static void expose(RobotInfo robot) throws GameActionException {
		rc.expose(robot.location);
		acted = true;
	}
	public static void buildRobot(RobotType type, Direction dir, int conviction) throws GameActionException {
		rc.buildRobot(type, dir, conviction);
		acted = true;
		ECInfo.ids = new IntCycler(rc.senseRobotAtLocation(Info.loc.add(dir)).ID, ECInfo.ids);
		int type_num = 0;
		switch (type) {
		case ENLIGHTENMENT_CENTER: {type_num = 0; break;}
		case POLITICIAN: {type_num = 1; break;}
		case SLANDERER: {type_num = 2; break;}
		case MUCKRAKER: {type_num = 3; break;}
		}
		ECInfo.types = new IntCycler(type_num, ECInfo.types);
		if (type==RobotType.SLANDERER) {
			ECInfo.embezzler_ids[Info.round_num%GameConstants.EMBEZZLE_NUM_ROUNDS] = rc.senseRobotAtLocation(Info.loc.add(dir)).ID;
			ECInfo.embezzler_incomes[Info.round_num%GameConstants.EMBEZZLE_NUM_ROUNDS] = Math2.get_embezzle_income(conviction);
		}
	}
}
