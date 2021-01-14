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

public class Gas {
	public static RobotController rc;
	
    public static void spread() throws GameActionException {  // ignores approximate bfs distances
    	int dx = 0;
    	int dy = 0;
    	for (RobotInfo robot:Info.friendly_muckrakers) {
//    		if (rc.getFlag(robot.getID())%2==1 && Info.loc.isWithinDistanceSquared(robot.location, 17)) {
//    			dx += Info.x-robot.location.x;
//    			dy += Info.y-robot.location.y;
//    		}
    		if (rc.getFlag(robot.getID())%2==1) {  // is_gas
    			dx += Info.x-robot.location.x;
    			dy += Info.y-robot.location.y;
    		}
    	}
    	for (RobotInfo robot:Info.friendly_politicians) {
//    		if (rc.getFlag(robot.getID())%2==1 && Info.loc.isWithinDistanceSquared(robot.location, 17)) {
//    			dx += Info.x-robot.location.x;
//    			dy += Info.y-robot.location.y;
//    		}
    		if (rc.getFlag(robot.getID())%2==1) {  // is_gas
    			dx += Info.x-robot.location.x;
    			dy += Info.y-robot.location.y;
    		}
    	}
    	if (dx==0 && dy==0) {
        	Pathing.target(Info.loc.add(Info.last_move_direction), 1);
        }
        else {
        	Pathing.target(Info.loc.add(Info.loc.directionTo(Info.loc.translate(dx, dy))), 1);
        }
    }
    public static void attack() throws GameActionException {
    	int dx = 0;
    	int dy = 0;
    	for (RobotInfo robot:Info.friendly_muckrakers) {
			int enemy_ddist = (rc.getFlag(robot.getID())>>1)%Flag.MAX_APPROX_DIST_PLUS_ONE - Info.approx_enemy_dist;
    		dx += enemy_ddist*(robot.location.x-Info.x);
    		dy += enemy_ddist*(robot.location.y-Info.y);
    	}
    	for (RobotInfo robot:Info.friendly_politicians) {
			int enemy_ddist = (rc.getFlag(robot.getID())>>1)%Flag.MAX_APPROX_DIST_PLUS_ONE - Info.approx_enemy_dist;
    		dx += enemy_ddist*(robot.location.x-Info.x);
    		dy += enemy_ddist*(robot.location.y-Info.y);
    	}
    	if (dx==0 && dy==0) {
    		spread();
        }
        else {
        	if (Info.need_to_relay_enemy_dist) {  // notify the others
//            if (Info.need_to_relay_enemy_dist || Info.need_to_relay_muckraker_warning) {  // notify the others
        		dx = -dx;
        		dy = -dy;
        	}
        	Pathing.target(Info.loc.add(Info.loc.directionTo(Info.loc.translate(dx, dy))), 1);
        }
    }

}
