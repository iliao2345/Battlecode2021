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

public class Politician {
	
	public static RobotController rc;

    public static void act() throws GameActionException {
		RobotInfo nearest_ec = Info.closest_robot(Info.friendly, RobotType.ENLIGHTENMENT_CENTER);
//		if (nearest_ec!=null) {
//			int r2 = Info.loc.distanceSquaredTo(nearest_ec.location);
//			if (rc.detectNearbyRobots(r2).length==1) {
//				rc.empower(r2);
//			}
//			else {
//				Pathing.stick(nearest_ec.location);
//			}
//		}
    	if (Info.enemy_ecs.length>0) {
    		Phase.is_gas = false;
    		RobotInfo closest = Info.closest_robot(Info.enemy, RobotType.ENLIGHTENMENT_CENTER);
    		Pathing.stick(closest.location);
    	}
    	if (Action.can_still_move &&  Info.neutral_ecs.length>0) {  // try to convert neutral ecs
    		Phase.is_gas = false;
    		RobotInfo closest = Info.closest_robot(Team.NEUTRAL, RobotType.ENLIGHTENMENT_CENTER);
    		if (!Info.loc.isWithinDistanceSquared(closest.location, RobotType.POLITICIAN.actionRadiusSquared)) {
    			Pathing.stick(closest.location);
    		}
    		if (!Action.acted && rc.senseNearbyRobots(Info.loc.distanceSquaredTo(closest.location)).length==1) {
    			RobotInfo[] friendly_robots = rc.senseNearbyRobots(closest.location, RobotType.POLITICIAN.actionRadiusSquared, Info.friendly);
    			RobotInfo[] enemy_robots = rc.senseNearbyRobots(closest.location, RobotType.POLITICIAN.actionRadiusSquared, Info.friendly);
    			int friendly_conviction = 0;
    			int enemy_conviction = 0;
    			for (RobotInfo robot:friendly_robots) {
    				if (robot.type == RobotType.POLITICIAN) {
    					friendly_conviction += Math.max(0, (int)(Info.empower_buff*robot.conviction-GameConstants.EMPOWER_TAX));
    				}
    			}
    			for (RobotInfo robot:enemy_robots) {
    				if (robot.type == RobotType.POLITICIAN) {
    					enemy_conviction += Math.max(0, (int)(Info.enemy_empower_buff*robot.conviction-GameConstants.EMPOWER_TAX));
    				}
    			}
    			if (friendly_conviction > enemy_conviction + closest.conviction) {
    				rc.empower(Info.loc.distanceSquaredTo(closest.location));
    			}
    		}
    		if (Action.can_still_move) {
        		Pathing.stick(closest.location);
    		}
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