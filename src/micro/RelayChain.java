package micro;
import battlecode.common.*;

public class RelayChain {
	public static double MOMENTUM_CAP = 2;
	public static RobotController rc;
	
	public static RobotInfo robot_to_source;
	public static RobotInfo last_robot_to_source;
	public static RobotInfo closest_sourceward_robot;
	public static int source_dist = 31;  // measured as # of robots
	public static boolean source_side_connected;
	public static double target_dist = 255;  // path distance measured as passability weighted euclidean distance
	public static MapLocation target_loc;
	public static double momentum_dx;
	public static double momentum_dy;

    public static void update() throws GameActionException {
    	RobotInfo robot_to_target = null;
    	robot_to_source = null;
    	source_dist++;
    	double passability = rc.sensePassability(Info.loc);
    	for (int i=Info.n_relayers; --i>=0;) {
    		RobotInfo robot = Info.relayers[i];
    		int flag = rc.getFlag(robot.ID);
    		if ((flag>>4)%256+Math.sqrt(Info.loc.distanceSquaredTo(robot.location))/passability<target_dist) {
    			robot_to_target = robot;
    			target_dist = (flag>>4)%256+Math.sqrt(Info.loc.distanceSquaredTo(robot.location))/passability;
    		}
    		if ((flag>>12)%32<source_dist) {
    			source_dist = (flag>>12)%32;
    			robot_to_source = robot;
    		}
    	}
    	target_dist = Math.min(target_dist+((Info.crowdedness>0.26)?255:1), 255);
    	source_dist = Math.min(source_dist+1, 31);
    	if (Info.n_friendly_ecs>0) {
    		source_dist = 0;
    		robot_to_source = Info.friendly_ecs[0];
    	}
    	closest_sourceward_robot = null;
    	int closest_distance_squared = Integer.MAX_VALUE;
    	for (int i=Info.n_relayers; --i>=0;) {
    		RobotInfo robot = Info.relayers[i];
    		int flag = rc.getFlag(robot.ID);
    		if ((flag>>12)%32<=source_dist && Info.loc.distanceSquaredTo(robot.location)<closest_distance_squared) {
    			closest_distance_squared = Info.loc.distanceSquaredTo(robot.location);
        		closest_sourceward_robot = robot;
    		}
    	}
    	for (int i=Info.n_friendly_ecs; --i>=0;) {
    		RobotInfo robot = Info.friendly_ecs[i];
    		if (Info.loc.distanceSquaredTo(robot.location)<closest_distance_squared) {
    			closest_distance_squared = Info.loc.distanceSquaredTo(robot.location);
        		closest_sourceward_robot = robot;
    		}
    	}
    	if (robot_to_target!=null) {
    		target_loc = robot_to_target.location;
    	}
    	if (robot_to_source!=null) {
    		source_side_connected = true;
    		last_robot_to_source = robot_to_source;
    	}
    	else {
    		source_side_connected = false;
    	}
    }
    
    public static void lock_target(MapLocation loc) throws GameActionException {
    	target_dist = Math.sqrt(Info.loc.distanceSquaredTo(loc))/rc.sensePassability(Info.loc);
    	target_dist = Math.min(target_dist+((Info.crowdedness>0.26)?255:1), 254);
    	target_loc = loc;
    }
    
    public static boolean extend(boolean[][] illegal_tiles) throws GameActionException {
    	if (Info.crowdedness>0.5 && Info.exterminate && Info.conviction<=10 && Info.type==RobotType.POLITICIAN) {
    		rc.empower(1); return true;
    	}
    	if (target_dist>=255 || Info.exterminate) {
    		double dx = 0;
    		double dy = 0;
			for (int i=Info.n_relayers; --i>=0;) {
				if (Clock.getBytecodesLeft()>1700) {
    				MapLocation repel_loc = Info.relayers[i].location;
    				dx -= 1000*(repel_loc.x-Info.x)/Info.loc.distanceSquaredTo(repel_loc);
    				dy -= 1000*(repel_loc.y-Info.y)/Info.loc.distanceSquaredTo(repel_loc);
				}
			}
			if (dx!=0||dy!=0) {
		    	double r = Math.sqrt(dx*dx+dy*dy);
		    	dx = dx/r;
		    	dy = dy/r;
			}
			momentum_dx += dx;
			momentum_dy += dy;
	    	double r = Math.sqrt(momentum_dx*momentum_dx+momentum_dy*momentum_dy);
	    	if (r>MOMENTUM_CAP) {
		    	momentum_dx = momentum_dx/r*MOMENTUM_CAP;
		    	momentum_dy = momentum_dy/r*MOMENTUM_CAP;
	    	}
			MapLocation target_loc = Info.loc.translate((int)(10*momentum_dx), (int)(10*momentum_dy));
			return Pathing.target(target_loc, illegal_tiles, 1);
    	}
    	if (source_side_connected) {
    		boolean[][] illegal_or_disconnected_tiles = new boolean[3][3];
    		for (Direction dir:Direction.allDirections()) {
    			illegal_or_disconnected_tiles[dir.dx+1][dir.dy+1] = illegal_tiles[dir.dx+1][dir.dy+1]
    					|| !Info.loc.add(dir).isWithinDistanceSquared(closest_sourceward_robot.location, Math.min(closest_sourceward_robot.type.sensorRadiusSquared, Info.type.sensorRadiusSquared));
    		}
        	if (Pathing.stick(target_loc, illegal_or_disconnected_tiles)) {  // path to target, reconnect to sourceward robot if disconnected from previous source robot
            	if (!rc.getLocation().isWithinDistanceSquared(robot_to_source.location, Math.min(robot_to_source.type.sensorRadiusSquared, Info.type.sensorRadiusSquared))) {
            		robot_to_source = closest_sourceward_robot;
            		if (closest_sourceward_robot.type==RobotType.ENLIGHTENMENT_CENTER) {
            			source_dist = 0;
            		}
            		else {
            			source_dist = Math.min(31, (rc.getFlag(closest_sourceward_robot.ID)>>12)%32+1);
            		}
            	}
            	return true;
        	}
        	else {  // no legal tiles which remain connected, try to stay near connected tiles
        		return Pathing.target(last_robot_to_source.location, illegal_tiles, 1);
        	}
    	}
    	else {  // can't see the source
    		if (last_robot_to_source!=null) {  // go to where a source was last seen
    			return Pathing.stick(last_robot_to_source.location, illegal_tiles);
    		}
    		else {
    			MapLocation target_loc = Info.loc.translate((int)(10*momentum_dx), (int)(10*momentum_dy));
    			return Pathing.target(target_loc, illegal_tiles, 1);
    		}
    	}
    }

}
