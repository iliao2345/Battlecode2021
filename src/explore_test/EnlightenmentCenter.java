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

public class EnlightenmentCenter {
	
	public static RobotController rc;
	
	public static void act() throws GameActionException {
		Flag.set_ec();
		System.out.println(Info.conviction);
		if (false) {
		//if (Info.conviction*rc.getEmpowerFactor(Info.friendly, 1)-GameConstants.EMPOWER_TAX > Info.conviction) {
			for (Direction dir : Direction.cardinalDirections()) {
	            if (rc.canBuildRobot(RobotType.POLITICIAN, dir, Info.conviction)) {
	                Action.buildRobot(RobotType.POLITICIAN, dir, Info.conviction);
	                break;
	            }
	        }
		}
		else {
			Direction valid_direction = null;
			for (Direction dir : Direction.allDirections()) {
				if (rc.canBuildRobot(RobotType.MUCKRAKER, dir, 1)) {
					valid_direction = dir;
	                break;
				}
			}
			if (valid_direction!=null) {
				int conviction = next_muckraker_conviction();
				Action.buildRobot(RobotType.MUCKRAKER, valid_direction, conviction);
			}
		}
    }
    
    public static void pause() throws GameActionException {
		Flag.set_ec();
    }
	public static int next_muckraker_conviction() throws GameActionException {
		ECInfo.last_muckraker_power++;
		int conviction = Math2.fibonacci(ECInfo.last_muckraker_power);
		if (conviction*Math2.FIBONACCI_SUM_RATIO>Info.passive_income*ECInfo.last_muckraker_power*Info.tile_cost*RobotType.ENLIGHTENMENT_CENTER.actionCooldown) {
			ECInfo.last_muckraker_power = 0;
			conviction = Math2.fibonacci(ECInfo.last_muckraker_power);
		}
		return conviction;
	}

}
