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
    	if (Phase.is_gas && Membrane.touching_membrane) {
    		Phase.condense();
    	}
    	if (Phase.is_gas && Action.can_still_move) {
    		Gas.attack();
    	}
    	if (Phase.is_membrane && Action.can_still_move) {
    		if (Membrane.push_signal>0) {
    			Membrane.advance();
    		}
    		else {
        		Membrane.heal();
    		}
    	}
		Flag.set_default_patrol();
    }
    
    public static void pause() throws GameActionException {
		Flag.set_default_patrol();
    }

}