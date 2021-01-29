package last_working_bot;
import battlecode.common.*;

public class RelayChain {
	public static double MOMENTUM_CAP = 2;
	public static RobotController rc;
	
	public static int pressure = 31;  // path distance measured as passability weighted euclidean distance
	public static double momentum_dx;
	public static double momentum_dy;
	public static double force_dx;
	public static double force_dy;

    public static void update() throws GameActionException {
    	for (int i=Info.n_relayers; --i>=0;) {
    		RobotInfo robot = Info.relayers[i];
    		int flag = rc.getFlag(robot.ID);
    		if ((flag>>1)%32<pressure) {
    			pressure = (flag>>1)%32;
    		}
    	}
    	pressure = Math.min(pressure+((Info.crowdedness>0.9)?3:1), 31);
    	if (Info.n_relayers==0) {pressure = 0;}
    	determine_if_surrounded: if (Info.n_relayers>0 && rc.senseNearbyRobots(Info.type.sensorRadiusSquared, Info.friendly).length<10) {
    		int dx = Info.relayers[0].location.x - Info.x;
    		int dy = Info.relayers[0].location.y - Info.y;
    		int original_dx = Info.relayers[0].location.x - Info.x;
    		int original_dy = Info.relayers[0].location.y - Info.y;
    		Info.relayers[Info.n_relayers+0] = new RobotInfo(0, Info.friendly, Info.type, 0, 0, new MapLocation(2*Info.low_x_bound-Info.x-1, Info.y));
    		Info.relayers[Info.n_relayers+1] = new RobotInfo(0, Info.friendly, Info.type, 0, 0, new MapLocation(2*Info.high_x_bound-Info.x+1, Info.y));
    		Info.relayers[Info.n_relayers+2] = new RobotInfo(0, Info.friendly, Info.type, 0, 0, new MapLocation(Info.x, 2*Info.low_y_bound-Info.y-1));
    		Info.relayers[Info.n_relayers+3] = new RobotInfo(0, Info.friendly, Info.type, 0, 0, new MapLocation(Info.x, 2*Info.high_y_bound-Info.y+1));
    		for (int iters=0; iters<2; iters++) {
        		for (int i=Info.n_relayers+4; --i>=0;) {
        			MapLocation loc = Info.relayers[i].location;
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
		for (int i=Info.n_relayers; --i>=0;) {
			if (Clock.getBytecodesLeft()>1700 && ((rc.getFlag(Info.relayers[i].ID)>>1)%32>=pressure) || Info.exterminate) {
				MapLocation repel_loc = Info.relayers[i].location;
				force_dx -= (double)(repel_loc.x-Info.x)/Info.loc.distanceSquaredTo(repel_loc);
				force_dy -= (double)(repel_loc.y-Info.y)/Info.loc.distanceSquaredTo(repel_loc);
			}
		}
		if ((Info.low_x_bound-Info.x-1)*(Info.low_x_bound-Info.x-1)<=30) {
			force_dx -= (double)(2*Info.low_x_bound-2*Info.x-2)/(Info.low_x_bound-Info.x-1)/(Info.low_x_bound-Info.x-1);
		}
		if ((Info.high_x_bound-Info.x+1)*(Info.high_x_bound-Info.x+1)<=30) {
			force_dx -= (double)(2*Info.high_x_bound-2*Info.x+2)/(Info.high_x_bound-Info.x+1)/(Info.high_x_bound-Info.x+1);
		}
		if ((Info.low_y_bound-Info.y-1)*(Info.low_y_bound-Info.y-1)<=30) {
			force_dy -= (double)(2*Info.low_y_bound-2*Info.y-2)/(Info.low_y_bound-Info.y-1)/(Info.low_y_bound-Info.y-1);
		}
		if ((Info.high_y_bound-Info.y+1)*(Info.high_y_bound-Info.y+1)<=30) {
			force_dy -= (double)(2*Info.high_y_bound-2*Info.y+2)/(Info.high_y_bound-Info.y+1)/(Info.high_y_bound-Info.y+1);
		}
    	double r = Math.sqrt(force_dx*force_dx+force_dy*force_dy);
		if (r>1e-10) {
	    	force_dx = force_dx/r;
	    	force_dy = force_dy/r;
		}
    }
    
    public static void lock_target(MapLocation loc) throws GameActionException {
    	pressure = 0;
    }
    
    public static boolean extend(boolean[][] illegal_tiles) throws GameActionException {
    	if (Info.crowdedness>0.5 && Info.exterminate && Info.conviction<=10 && Info.type==RobotType.POLITICIAN) {
    		rc.empower(1); Clock.yield(); return true;
    	}
		boolean[][] illegal_or_near_targetter_tiles = new boolean[3][3];
		for (Direction dir:Direction.allDirections()) {  // try to get more than 1 away from targetters
			MapLocation adjacent = Info.loc.add(dir);
			illegal_or_near_targetter_tiles[dir.dx+1][dir.dy+1] = illegal_tiles[dir.dx+1][dir.dy+1];
			for (int i=Info.n_targetters; --i>=0;) {
				illegal_or_near_targetter_tiles[dir.dx+1][dir.dy+1] = illegal_or_near_targetter_tiles[dir.dx+1][dir.dy+1]
						|| adjacent.isWithinDistanceSquared(Info.targetters[i].location, 1) && (rc.getFlag(Info.targetters[i].ID)>>19)%2==1;
			}
		}
		illegal_tiles = illegal_or_near_targetter_tiles;
		momentum_dx += force_dx;
		momentum_dy += force_dy;
    	double r = Math.sqrt(momentum_dx*momentum_dx+momentum_dy*momentum_dy);
    	if (r>MOMENTUM_CAP) {
	    	momentum_dx = momentum_dx/r*MOMENTUM_CAP;
	    	momentum_dy = momentum_dy/r*MOMENTUM_CAP;
    	}
    	MapLocation pseudotarget = Info.loc.translate((int)(1000*momentum_dx), (int)(1000*momentum_dy));
		return Pathing.approach(pseudotarget, illegal_tiles);
    }

}
