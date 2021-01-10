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

public class Muckraker {
	
	public static RobotController rc;

    public static void act() throws GameActionException {
    	if (!Action.acted && Info.enemy_slanderers.length>0) {
    		Phase.is_gas = false;
    		Phase.is_membrane = false;
    		RobotInfo closest = Info.closest_robot(Info.enemy, RobotType.SLANDERER);
    		if (Info.loc.distanceSquaredTo(closest.location)<=RobotType.MUCKRAKER.actionRadiusSquared) {
    			Action.expose(closest);
    		}
    		else if (Action.can_still_move) {
    			Pathing.target(closest.location, 1);
    		}
    	}
    	if (Action.can_still_move && Info.enemy_ecs.length>0) {
    		Phase.is_gas = false;
    		Phase.is_membrane = false;
    		RobotInfo closest = Info.closest_robot(Info.enemy, RobotType.ENLIGHTENMENT_CENTER);
    		Pathing.stick(closest.location);
    	}
    	if (Phase.is_gas && Info.touching_membrane) {
    		Direction membrane_direction = null;
    		for (Direction dir:Math2.UNIT_DIRECTIONS) {
    			int i = dir.dx+1;
    			int j = dir.dy+1;
    			RobotInfo robot = Info.adjacent_robots[i][j];
    			if (robot!=null) {
    				if (robot.team==Info.friendly && (rc.getFlag(robot.ID))>>23==1 && (rc.getFlag(robot.ID)>>15)%4!=3) {
    					membrane_direction = dir;
    					break;
    				}
    			}
    		}
    		Phase.condense(membrane_direction, 1);
    	}
    	if (Phase.is_gas && !rc.onTheMap(Info.loc.add(Direction.EAST))) {
    		Phase.condense(Direction.EAST, 1);
    	}
    	if (Phase.is_gas && Action.can_still_move) {
    		Gas.attack();
    	}
    	if (Phase.is_membrane && Action.can_still_move) {
    		Membrane.heal();
    	}
		Flag.set_default_patrol();
    }
    
    public static void pause() throws GameActionException {
		Flag.set_default_patrol();
    }

}