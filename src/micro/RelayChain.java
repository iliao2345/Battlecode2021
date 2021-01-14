package micro;
import battlecode.common.*;

public class RelayChain {
	public static RobotController rc;
	public static RobotInfo robot_to_source;
	public static RobotInfo last_robot_to_source;
	public static RobotInfo closest_sourceward_robot;
	public static int source_dist;  // measured as # of robots
	public static boolean source_side_connected;
	public static double target_angle;  // tangent direction
	public static double target_dist;  // path distance measured as passability weighted euclidean distance
	public static double target_dx;  // tangent direction, approx constant length
	public static double target_dy;  // tangent direction, approx constant length

    public static void update() throws GameActionException {
    	target_dist = Math.min(target_dist+1, 191.99);
    	source_dist = Math.min(source_dist+1, 32);
    	RobotInfo robot_to_target = null;
    	robot_to_source = null;
    	for (int i=Info.n_friendly_politicians; --i>=0;) {
    		RobotInfo robot = Info.friendly_politicians[i];
    		int flag = rc.getFlag(robot.ID);
    		if (flag>>23==1) {
        		if ((flag>>4)%64*3+Math.sqrt(Info.loc.distanceSquaredTo(robot.location))/rc.sensePassability(Info.loc)<target_dist) {
        			robot_to_target = robot;
        			target_dist = (flag>>4)%64*3+Math.sqrt(Info.loc.distanceSquaredTo(robot.location))/rc.sensePassability(Info.loc);
        		}
        		if ((flag>>10)%32+1<source_dist) {
        			source_dist = (flag>>10)%32+1;
        			robot_to_source = robot;
        		}
    		}
    	}
    	for (int i=Info.n_friendly_muckrakers; --i>=0;) {
    		RobotInfo robot = Info.friendly_muckrakers[i];
    		int flag = rc.getFlag(robot.ID);
    		if (flag>>23==1) {
        		if ((flag>>4)%64*3+Math.sqrt(Info.loc.distanceSquaredTo(robot.location))/rc.sensePassability(Info.loc)<target_dist) {
        			robot_to_target = robot;
        			target_dist = (flag>>4)%64*3+Math.sqrt(Info.loc.distanceSquaredTo(robot.location))/rc.sensePassability(Info.loc);
        		}
        		if ((flag>>10)%32+1<source_dist) {
        			source_dist = (flag>>10)%32+1;
        			robot_to_source = robot;
        		}
    		}
    	}
    	if (Info.n_friendly_ecs>0) {
    		source_dist = 0;
    		robot_to_source = Info.friendly_ecs[0];
    	}
    	closest_sourceward_robot = null;
    	int closest_distance_squared = Integer.MAX_VALUE;
    	for (int i=Info.n_friendly_politicians; --i>=0;) {
    		RobotInfo robot = Info.friendly_politicians[i];
    		int flag = rc.getFlag(robot.ID);
    		if (flag>>23==1) {
        		if ((flag>>10)%32<=source_dist && Info.loc.distanceSquaredTo(robot.location)<closest_distance_squared) {
        			closest_distance_squared = Info.loc.distanceSquaredTo(robot.location);
            		closest_sourceward_robot = robot;
        		}
    		}
    	}
    	for (int i=Info.n_friendly_muckrakers; --i>=0;) {
    		RobotInfo robot = Info.friendly_muckrakers[i];
    		int flag = rc.getFlag(robot.ID);
    		if (flag>>23==1) {
        		if ((flag>>10)%32<=source_dist && Info.loc.distanceSquaredTo(robot.location)<closest_distance_squared) {
        			closest_distance_squared = Info.loc.distanceSquaredTo(robot.location);
            		closest_sourceward_robot = robot;
        		}
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
    		target_dx = robot_to_target.location.x-Info.x+Math.sqrt(robot_to_target.type.sensorRadiusSquared)*Math.cos(rc.getFlag(robot_to_target.ID)%16);
    		target_dy = robot_to_target.location.y-Info.y+Math.sqrt(robot_to_target.type.sensorRadiusSquared)*Math.sin(rc.getFlag(robot_to_target.ID)%16);
        	target_angle = Math.atan2(target_dy, target_dx);
    		target_dx = Math.sqrt(Info.type.sensorRadiusSquared)*Math.cos(target_angle);
    		target_dy = Math.sqrt(Info.type.sensorRadiusSquared)*Math.sin(target_angle);
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
    	if (!rc.canSenseLocation(loc)) {target_dist = Math.sqrt(Info.loc.distanceSquaredTo(loc))/0.2;}
    	target_dist = Math.sqrt(Info.loc.distanceSquaredTo(loc))/rc.sensePassability(Info.loc);
    	target_angle = Math.atan2(loc.y-Info.y, loc.x-Info.x);
		target_dx = Math.sqrt(Info.type.sensorRadiusSquared)*Math.cos(target_angle);
		target_dy = Math.sqrt(Info.type.sensorRadiusSquared)*Math.sin(target_angle);
    }
    
    public static boolean extend(boolean[][] illegal_tiles) throws GameActionException {
    	if (source_side_connected) {
    		MapLocation target_loc = Info.loc.translate((int)(target_dx+0.5), (int)(target_dy+0.5));
    		target_loc = Pathing.get_closer_target(target_loc, Math.min(2, Math2.length(Info.loc, target_loc)-1));
    		if (target_dist>=189) {  // if don't see target, repel neighbors but stay connected to source
    			int dx = 0;
    			int dy = 0;
    			for (int i=Info.n_friendly_muckrakers; --i>=0;) {
    				MapLocation repel_loc = Info.friendly_muckrakers[i].location;
    				dx += 1000*(repel_loc.x-Info.x)/Info.loc.distanceSquaredTo(repel_loc);
    				dy += 1000*(repel_loc.y-Info.y)/Info.loc.distanceSquaredTo(repel_loc);
    			}
    			target_loc = Info.loc.translate(-dx, -dy);
    		}
        	double best_distance = Double.MAX_VALUE;
        	Direction best_dir = null;
        	for (Direction dir:Math2.UNIT_DIRECTIONS) {
        		MapLocation adjacent = Info.loc.add(dir);
        		double adjacent_distance = Math.sqrt(adjacent.distanceSquaredTo(target_loc));
        		if (rc.canMove(dir) && adjacent.isWithinDistanceSquared(closest_sourceward_robot.location, Math.min(closest_sourceward_robot.type.sensorRadiusSquared, Info.type.sensorRadiusSquared))
        				&& !illegal_tiles[dir.dx+1][dir.dy+1] && 1/rc.sensePassability(adjacent)+adjacent_distance<best_distance) {
        			best_dir = dir;
        			best_distance = 1/rc.sensePassability(adjacent)+adjacent_distance;
        		}
        	}
        	if (best_dir!=null) {
            	if (!Info.loc.add(best_dir).isWithinDistanceSquared(robot_to_source.location, Math.min(robot_to_source.type.sensorRadiusSquared, Info.type.sensorRadiusSquared))) {
            		robot_to_source = closest_sourceward_robot;
            		if (closest_sourceward_robot.type==RobotType.ENLIGHTENMENT_CENTER) {
            			source_dist = 0;
            		}
            		else {
            			source_dist = Math.min(31, (rc.getFlag(closest_sourceward_robot.ID)>>10)%32+1);
            		}
            	}
            	Action.move(best_dir); return true;
        	}
        	else {  // no legal tiles which remain connected, try to stay near connected tiles
        		return Pathing.stick(last_robot_to_source.location, illegal_tiles);
        	}
    	}
    	else {
    		if (last_robot_to_source!=null) {
    			return Pathing.stick(last_robot_to_source.location, illegal_tiles);
    		}
    		else {
    			MapLocation target_loc = Info.loc.translate((int)(target_dx+0.5), (int)(target_dy+0.5));
    			return Pathing.target(target_loc, illegal_tiles, 1);
    		}
    	}
    }

}
