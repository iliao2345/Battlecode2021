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

public class EnlightenmentCenter {
	
	public static RobotController rc;
	
	public static void act() throws GameActionException {
		Flag.set_ec();
		System.out.println(Info.conviction);
		if (false) {
		//if (Info.conviction*rc.getEmpowerFactor(Info.friendly, 11)-GameConstants.EMPOWER_TAX > Info.conviction) {
			for (Direction dir : Direction.cardinalDirections()) {
	            if (rc.canBuildRobot(RobotType.POLITICIAN, dir, Info.conviction)) {
	                Action.buildRobot(RobotType.POLITICIAN, dir, Info.conviction);
	                break;
	            }
	        }
		}
		else {
			boolean space_to_build = false;
			for (int i=0; i<8; i++) {
				ECInfo.last_build_direction = ECInfo.last_build_direction.rotateLeft().rotateLeft().rotateLeft();
				if (rc.canBuildRobot(RobotType.MUCKRAKER, ECInfo.last_build_direction, 1)) {
					space_to_build = true;
	                break;
				}
			}
			if (space_to_build) {
				build_distribution(ECInfo.last_build_direction);
			}
		}
    }
    
    public static void pause() throws GameActionException {
		Flag.set_ec();
    }
	public static void build_distribution(Direction dir) throws GameActionException {
		double income = ECInfo.total_income*RobotType.ENLIGHTENMENT_CENTER.actionCooldown/Info.tile_cost;
		if (Info.round_num<2) {
			Action.buildRobot(RobotType.SLANDERER, dir, Math2.embezzle_floor(Info.conviction));
			return;
		}
		int spare_funds = Info.conviction-ECInfo.target_stockpile;
		if (spare_funds > 20) {
			Action.buildRobot(RobotType.POLITICIAN, dir, 20);
			return;
		}
		if (ECInfo.embezzle_income==0 && Info.conviction>ECInfo.target_stockpile+2) {
			int conviction = Math2.embezzle_floor((int) (ECInfo.target_stockpile));
			if (conviction>0) {
				Action.buildRobot(RobotType.SLANDERER, dir, conviction);
				return;
			}
		}
		Action.buildRobot(RobotType.MUCKRAKER, dir, 1);
	}
}
