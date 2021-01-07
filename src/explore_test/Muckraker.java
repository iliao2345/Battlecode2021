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

public class Muckraker {
	
	public static RobotController rc;

    public static void act() throws GameActionException {
    	if (Info.enemy_ecs.length>0) {
    		Flag.spreading = false;
    		RobotInfo closest = Info.closest_robot(Info.enemy, RobotType.ENLIGHTENMENT_CENTER);
    		Pathing.stick(closest.location);
    	}
    	if (!Action.acted && Info.enemy_slanderers.length>0) {
    		Flag.spreading = false;
    		RobotInfo closest = Info.closest_robot(Info.enemy, RobotType.SLANDERER);
    		if (Info.loc.distanceSquaredTo(closest.location)<=RobotType.MUCKRAKER.actionRadiusSquared) {
    			Action.expose(closest);
    			rc.setIndicatorLine(Info.loc, closest.location, 255, 0, 0);
    		}
    		else if (Action.can_still_move) {
    			Pathing.target(closest.location, 1);
    			rc.setIndicatorLine(Info.loc, closest.location, 128, 0, 0);
    		}
    	}
    	if (Action.can_still_move) {
    		Flag.spreading = true;
    		//Pathing.spread();
    		//spread_marked();
    		go_to_enemy();
    	}
		Flag.set_default_patrol();
    }
    public static void spread_marked() throws GameActionException {  // ignores approximate bfs distances
    	boolean[] markings = new boolean[Info.friendly_muckrakers.length];
    	for (int i=0; i<Info.friendly_muckrakers.length; i++) {
    		markings[i] = rc.getFlag(Info.friendly_muckrakers[i].getID())%2==1;
    	}
    	RobotInfo closest_spreading_robot = Info.closest_marked_robot(Info.type, markings);
    	if (closest_spreading_robot==null) {
        	Pathing.target(Info.loc.add(Info.last_move_direction), 1);
        }
        else {
        	Pathing.target(Info.loc.add(closest_spreading_robot.location.directionTo(Info.loc)), 1);
        }
    }
    public static void go_to_enemy() throws GameActionException {  // ignores spreadmarks
    	int dx = 0;
    	int dy = 0;
    	for (RobotInfo robot:Info.friendly_muckrakers) {
			int enemy_ddist = (rc.getFlag(robot.getID())>>Flag.LOG_MAX_APPROX_DIST_PLUS_ONE)%Flag.MAX_APPROX_DIST_PLUS_ONE - Info.approx_enemy_dist;
    		dx += enemy_ddist*(robot.location.x-Info.x);
    		dy += enemy_ddist*(robot.location.y-Info.y);
    	}
    	for (RobotInfo robot:Info.friendly_politicians) {
			int enemy_ddist = (rc.getFlag(robot.getID())>>Flag.LOG_MAX_APPROX_DIST_PLUS_ONE)%Flag.MAX_APPROX_DIST_PLUS_ONE - Info.approx_enemy_dist;
    		dx += enemy_ddist*(robot.location.x-Info.x);
    		dy += enemy_ddist*(robot.location.y-Info.y);
    	}
    	if (dx==0 && dy==0) {
    		spread_marked();
        }
        else {
        	Pathing.target(Info.loc.add(Info.loc.directionTo(Info.loc.translate(dx, dy))), 1);
        }
    }
    
    public static void pause() throws GameActionException {
		Flag.set_default_patrol();
    }

}