package slands;
import battlecode.common.*;

public class Slanderer {
	public static RobotController rc;

	public static double momentum_cap = 2;
	public static int pressure = 6;  // path distance measured as passability weighted euclidean distance
	public static double momentum_dx = 0;
	public static double momentum_dy = 0;
	public static double force_dx;
	public static double force_dy;
	public static boolean explore_flag = true;
	public static boolean evade = false;
	public static RobotInfo target_ec = new RobotInfo(0, Team.NEUTRAL, RobotType.ENLIGHTENMENT_CENTER, 0, 0, new MapLocation(0, 0));
	public static int target_ec_kill_conviction;
	
	public static void update() throws GameActionException {
		explore_flag = Info.round_num<450;
    	target_ec = Info.closest_robot(Team.NEUTRAL, RobotType.ENLIGHTENMENT_CENTER);
    	if (target_ec!=null) {
    		target_ec_kill_conviction = target_ec.conviction+1;
    	}
    	else {
    		target_ec = new RobotInfo(0, Team.NEUTRAL, RobotType.ENLIGHTENMENT_CENTER, 0, 0, Info.loc);  // keep the final value at Info.loc so that muck alert works properly
    		target_ec_kill_conviction = 0;
    	}
		if (Info.n_targetters>0) {
			target_ec_kill_conviction = 0;  // override late targetting calls targetter already in sight to prevent EC sending two targetters
		}
		if (Info.n_enemy_muckrakers+Info.n_enemy_ecs>0) {
			evade = true;
			pressure = 200;
			momentum_cap = 30;
			force_dx = 0;
			force_dy = 0;
			for (int i=Info.n_enemy_muckrakers; --i>=0;) {
				if (Clock.getBytecodesLeft()>1700) {
					MapLocation repel_loc = Info.enemy_muckrakers[i].location;
					force_dx -= (double)(repel_loc.x-Info.x)/Info.loc.distanceSquaredTo(repel_loc);
					force_dy -= (double)(repel_loc.y-Info.y)/Info.loc.distanceSquaredTo(repel_loc);
				}
			}
			for (int i=Info.n_enemy_ecs; --i>=0;) {
				if (Clock.getBytecodesLeft()>1700) {
					MapLocation repel_loc = Info.enemy_ecs[i].location;
					force_dx -= (double)(repel_loc.x-Info.x)/Info.loc.distanceSquaredTo(repel_loc);
					force_dy -= (double)(repel_loc.y-Info.y)/Info.loc.distanceSquaredTo(repel_loc);
				}
			}
		}
		if (!evade) {
			for (int i=Info.n_relayers; --i>=0;) {
				if (Clock.getBytecodesLeft()>1700 && rc.getFlag(Info.relayers[i].ID)%2==1) {
					MapLocation repel_loc = Info.relayers[i].location;
					force_dx -= (double)(repel_loc.x-Info.x)/Info.loc.distanceSquaredTo(repel_loc);
					force_dy -= (double)(repel_loc.y-Info.y)/Info.loc.distanceSquaredTo(repel_loc);
					pressure = 7;
				}
			}
		}
		if (pressure<7) {
			for (int i=Info.n_friendly_slanderers; --i>=0;) {
	    		RobotInfo robot = Info.friendly_slanderers[i];
	    		int flag = rc.getFlag(robot.ID);
	    		if (flag%8<pressure) {
	    			pressure = flag%8;
	    		}
	    	}
	    	pressure = Math.min(pressure+1, 7);
	    	if (Info.n_friendly_slanderers==0) {pressure = 0;}
	    	determine_if_surrounded: if (Info.n_friendly_slanderers>0 && rc.senseNearbyRobots(Info.type.sensorRadiusSquared, Info.friendly).length<10) {
	    		int dx = Info.friendly_slanderers[0].location.x - Info.x;
	    		int dy = Info.friendly_slanderers[0].location.y - Info.y;
	    		int original_dx = Info.friendly_slanderers[0].location.x - Info.x;
	    		int original_dy = Info.friendly_slanderers[0].location.y - Info.y;
	    		Info.friendly_slanderers[Info.n_friendly_slanderers+0] = new RobotInfo(0, Info.friendly, Info.type, 0, 0, new MapLocation(2*Info.low_x_bound-Info.x-1, Info.y));
	    		Info.friendly_slanderers[Info.n_friendly_slanderers+1] = new RobotInfo(0, Info.friendly, Info.type, 0, 0, new MapLocation(2*Info.high_x_bound-Info.x+1, Info.y));
	    		Info.friendly_slanderers[Info.n_friendly_slanderers+2] = new RobotInfo(0, Info.friendly, Info.type, 0, 0, new MapLocation(Info.x, 2*Info.low_y_bound-Info.y-1));
	    		Info.friendly_slanderers[Info.n_friendly_slanderers+3] = new RobotInfo(0, Info.friendly, Info.type, 0, 0, new MapLocation(Info.x, 2*Info.high_y_bound-Info.y+1));
	    		for (int iters=0; iters<2; iters++) {
	        		for (int i=Info.n_friendly_slanderers; --i>=0;) {
	        			MapLocation loc = Info.friendly_slanderers[i].location;
	        			if (Info.loc.isWithinDistanceSquared(loc, 30) && (loc.x - Info.x)*dy-dx*(loc.y - Info.y)>0) {
	        				dx = loc.x - Info.x;
	        				dy = loc.y - Info.y;
	        				if (original_dx*dy-dx*original_dy>0) {
	        					break determine_if_surrounded;
	        				}
	        			}
	        		}
	    		}
	    		pressure = 0;
	    	}

			force_dx = 0;
			force_dy = 0;
			for (int i=Info.n_friendly_slanderers; --i>=0;) {
				if (Clock.getBytecodesLeft()>1700 && rc.getFlag(Info.friendly_slanderers[i].ID)%8>=pressure) {
					MapLocation repel_loc = Info.friendly_slanderers[i].location;
					force_dx -= (double)(repel_loc.x-Info.x)/Info.loc.distanceSquaredTo(repel_loc);
					force_dy -= (double)(repel_loc.y-Info.y)/Info.loc.distanceSquaredTo(repel_loc);
				}
			}
//			if ((Info.low_x_bound-Info.x-1)*(Info.low_x_bound-Info.x-1)<=20) {
//				force_dx -= (double)(2*Info.low_x_bound-2*Info.x-2)/(Info.low_x_bound-Info.x-1)/(Info.low_x_bound-Info.x-1);
//			}
//			if ((Info.high_x_bound-Info.x+1)*(Info.high_x_bound-Info.x+1)<=20) {
//				force_dx -= (double)(2*Info.high_x_bound-2*Info.x+2)/(Info.high_x_bound-Info.x+1)/(Info.high_x_bound-Info.x+1);
//			}
//			if ((Info.low_y_bound-Info.y-1)*(Info.low_y_bound-Info.y-1)<=20) {
//				force_dy -= (double)(2*Info.low_y_bound-2*Info.y-2)/(Info.low_y_bound-Info.y-1)/(Info.low_y_bound-Info.y-1);
//			}
//			if ((Info.high_y_bound-Info.y+1)*(Info.high_y_bound-Info.y+1)<=20) {
//				force_dy -= (double)(2*Info.high_y_bound-2*Info.y+2)/(Info.high_y_bound-Info.y+1)/(Info.high_y_bound-Info.y+1);
//			}
	    	double r = Math.sqrt(force_dx*force_dx+force_dy*force_dy);
			if (r>1e-10) {
		    	force_dx = force_dx/r;
		    	force_dy = force_dy/r;
			}
		}
	}
	
	public static void act() throws GameActionException {
		if (evade) {
	    	momentum_dx = force_dx*momentum_cap;
	    	momentum_dy = force_dy*momentum_cap;
	    	MapLocation pseudotarget = Info.loc.translate((int)(1000*momentum_dx), (int)(1000*momentum_dy));
			Pathing.approach(pseudotarget, new boolean[3][3]); return;
		}
		else if (explore_flag) {
			momentum_dx += force_dx;
			momentum_dy += force_dy;
	    	double r = Math.sqrt(momentum_dx*momentum_dx+momentum_dy*momentum_dy);
	    	if (r>momentum_cap) {
		    	momentum_dx = momentum_dx/r*momentum_cap;
		    	momentum_dy = momentum_dy/r*momentum_cap;
	    	}
	    	MapLocation pseudotarget = Info.loc.translate((int)(1000*momentum_dx), (int)(1000*momentum_dy));
	    	boolean[][] blocking_ec = new boolean[3][3];
	    	for (Direction dir:Direction.allDirections()) {
	    		for (int i=Info.n_friendly_ecs; --i>=0;) {
	    			if (Info.loc.add(dir).isAdjacentTo(Info.friendly_ecs[i].location)) {
	    				blocking_ec[dir.dx+1][dir.dy+1] = true;
	    				break;
	    			}
	    		}
	    	}
			Pathing.approach(pseudotarget, blocking_ec); return;
		}
		else {
			MapLocation ec_location = new MapLocation(Info.tracked_friendly_ec_x.data, Info.tracked_friendly_ec_y.data);
			Direction best_direction = null;  // get to spawn if possible through storage sites, and don't store near spawn
			int lowest_cost = Integer.MAX_VALUE;
			for (Direction dir:Direction.allDirections()) {
				int cost = (ec_location.x-Info.x-dir.dx)*(ec_location.x-Info.x-dir.dx)+(ec_location.y-Info.y-dir.dy)*(ec_location.y-Info.y-dir.dy);
				boolean storage_location = ((Info.x+dir.dx-1)/2+(Info.y+dir.dy-1)/2)%2==1 && !Info.loc.add(dir).isWithinDistanceSquared(ec_location, 10);
				if (storage_location && (rc.canMove(dir)||dir==Direction.CENTER) && (best_direction==null || cost<lowest_cost)) {
					best_direction = dir;
					lowest_cost = cost;
				}
			}
			if (best_direction!=null) {Action.move(best_direction); return;}
			Direction momentum_direction = Info.last_move_direction;  // if cannot avoid odd tiles and friendly EC, try to take an outgoing line
			if (Math.random()<0.5) {momentum_direction = momentum_direction.rotateLeft().rotateLeft();}
			if (Math.random()<0.5) {momentum_direction = momentum_direction.rotateRight().rotateRight();}
			best_direction = null;
			lowest_cost = Integer.MAX_VALUE;
			for (Direction dir:Math2.UNIT_DIRECTIONS) {
				int cost = -dir.dx*momentum_direction.dx-dir.dy*momentum_direction.dy;
				boolean storage_location = ((Info.x+dir.dx-1)/2+(Info.y+dir.dy-1)/2)%2==1 && Info.loc.add(dir).isWithinDistanceSquared(ec_location, 10);
				boolean outgoing_line = ((Info.x+dir.dx-1)/2+(Info.y+dir.dy-1)/2)%2==0;
				if (outgoing_line && !storage_location && (rc.canMove(dir)||dir==Direction.CENTER) && (best_direction==null || cost<lowest_cost)) {
					best_direction = dir;
					lowest_cost = cost;
				}
			}
			if (best_direction!=null) {Action.move(best_direction); return;}
			int n_possible_directions = 0;
			best_direction = null;  // worst case move randomly
			for (Direction dir:Math2.UNIT_DIRECTIONS) {
				if (rc.canMove(dir) && Math.random()<1/(n_possible_directions+1)) {
					best_direction = dir;
				}
			}
			if (best_direction!=null) {Action.move(best_direction); return;}
		}
	}
	
	public static void pause() {
		
	}

}
