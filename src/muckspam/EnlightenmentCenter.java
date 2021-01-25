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
				int conviction = next_conviction();
				RobotType build_type = RobotType.MUCKRAKER;
				if (conviction>33 && Math.random()<0.6) {
					build_type = RobotType.POLITICIAN;
				}
				Action.buildRobot(build_type, ECInfo.last_build_direction, 1);
			}
		}
    }
    
    public static void pause() throws GameActionException {
		Flag.set_ec();
    }
	public static int next_conviction() throws GameActionException {
		ECInfo.last_build_power++;
		int conviction = Math2.fibonacci(ECInfo.last_build_power);
		if (conviction*Math2.FIBONACCI_SUM_RATIO>Info.passive_income*ECInfo.last_build_power*Info.tile_cost*RobotType.ENLIGHTENMENT_CENTER.actionCooldown) {
			ECInfo.last_build_power = 0;
			conviction = Math2.fibonacci(ECInfo.last_build_power);
		}
		return conviction;
	}

}
