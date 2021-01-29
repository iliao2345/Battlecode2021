package last_working_bot;
import battlecode.common.*;

public class Exterminator {
	public static double MOMENTUM_CAP = 2;
//	public static int EXTERMINATE_START_TIME = 1250;
	public static int EXTERMINATE_START_TIME = 1500;
//	public static int EXTERMINATE_FINISH_MONEY_TIME = 1350;
	public static int EXTERMINATE_FINISH_MONEY_TIME = 1500;
	public static RobotController rc;
	
	public static int pressure = 15;  // path distance measured as passability weighted euclidean distance
	public static MapLocation target_loc = new MapLocation(0, 0);
	public static double momentum_dx;
	public static double momentum_dy;
	public static double force_dx;
	public static double force_dy;

    public static void update() throws GameActionException {
    	double passability = rc.sensePassability(Info.loc);
    	for (int i=Info.n_exterminators; --i>=0;) {
    		RobotInfo robot = Info.exterminators[i];
    		int flag = rc.getFlag(robot.ID);
    		if ((flag>>1)%16<pressure) {
    			pressure = (flag>>1)%16;
    		}
    	}
    	pressure = Math.min(pressure+1, 15);  /////////////////////////// perhaps add 1/passibility rather than 1?
    	if (Info.n_exterminators==0) {pressure = 0;}
    	determine_if_surrounded: if (Info.n_exterminators>0) {
    		int dx = Info.exterminators[0].location.x - Info.x;
    		int dy = Info.exterminators[0].location.y - Info.y;
    		int original_dx = Info.exterminators[0].location.x - Info.x;
    		int original_dy = Info.exterminators[0].location.y - Info.y;
    		Info.exterminators[Info.n_exterminators+0] = new RobotInfo(0, Info.friendly, Info.type, 0, 0, new MapLocation(2*Info.low_x_bound-Info.x-1, Info.y));
    		Info.exterminators[Info.n_exterminators+1] = new RobotInfo(0, Info.friendly, Info.type, 0, 0, new MapLocation(2*Info.high_x_bound-Info.x+1, Info.y));
    		Info.exterminators[Info.n_exterminators+2] = new RobotInfo(0, Info.friendly, Info.type, 0, 0, new MapLocation(Info.x, 2*Info.low_y_bound-Info.y-1));
    		Info.exterminators[Info.n_exterminators+3] = new RobotInfo(0, Info.friendly, Info.type, 0, 0, new MapLocation(Info.x, 2*Info.high_y_bound-Info.y+1));
    		for (int iters=0; iters<2; iters++) {
        		for (int i=Info.n_exterminators+4; --i>=0;) {
        			MapLocation loc = Info.exterminators[i].location;
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
		for (int i=Info.n_exterminators; --i>=0;) {
			if (Clock.getBytecodesLeft()>1700 && (rc.getFlag(Info.exterminators[i].ID)>>1)%16>=pressure) {
				MapLocation repel_loc = Info.exterminators[i].location;
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
		target_loc = null;
    }

    public static void lock_target(MapLocation loc) throws GameActionException {
    	pressure = 0;
    	target_loc = loc;
    }
    
    public static void exterminate() throws GameActionException {
		CombatInfo.compute_self_empower_gains();
		if (target_loc!=null && Info.loc.distanceSquaredTo(target_loc)==1) {
	    	int max_kills = -1;
			int best_radius_squared = 0;
			if (CombatInfo.kills_9>max_kills) {max_kills = CombatInfo.kills_9; best_radius_squared = 9;}
			if (CombatInfo.kills_8>max_kills) {max_kills = CombatInfo.kills_8; best_radius_squared = 8;}
			if (CombatInfo.kills_5>max_kills) {max_kills = CombatInfo.kills_5; best_radius_squared = 5;}
			if (CombatInfo.kills_4>max_kills) {max_kills = CombatInfo.kills_4; best_radius_squared = 4;}
			if (CombatInfo.kills_2>max_kills) {max_kills = CombatInfo.kills_2; best_radius_squared = 2;}
			if (CombatInfo.kills_1>max_kills) {max_kills = CombatInfo.kills_1; best_radius_squared = 1;}
			if (max_kills>-1) {
				rc.empower(best_radius_squared); Clock.yield(); return;
			}
			rc.empower(1); Clock.yield(); return;
		}
		if (Info.crowdedness>0.9) {
			if (Math.random()<0.001) {  // suicide with low probability if clogging
				int best_costs = Integer.MIN_VALUE;
				int best_radius_squared = 9;
				if (CombatInfo.costs_9>best_costs) {best_costs = CombatInfo.costs_9; best_radius_squared = 9;}
				if (CombatInfo.costs_8>best_costs) {best_costs = CombatInfo.costs_8; best_radius_squared = 8;}
				if (CombatInfo.costs_5>best_costs) {best_costs = CombatInfo.costs_5; best_radius_squared = 5;}
				if (CombatInfo.costs_4>best_costs) {best_costs = CombatInfo.costs_4; best_radius_squared = 4;}
				if (CombatInfo.costs_2>best_costs) {best_costs = CombatInfo.costs_2; best_radius_squared = 2;}
				if (CombatInfo.costs_1>best_costs) {best_costs = CombatInfo.costs_1; best_radius_squared = 1;}
				rc.empower(best_radius_squared); Clock.yield(); return;
			}
		}
    	if (target_loc==null) {
    		momentum_dx += force_dx;
    		momentum_dy += force_dy;
        	double r = Math.sqrt(momentum_dx*momentum_dx+momentum_dy*momentum_dy);
        	if (r>MOMENTUM_CAP) {
    	    	momentum_dx = momentum_dx/r*MOMENTUM_CAP;
    	    	momentum_dy = momentum_dy/r*MOMENTUM_CAP;
        	}
        	MapLocation pseudotarget = Info.loc.translate((int)(10*momentum_dx), (int)(10*momentum_dy));
    		Pathing.approach(pseudotarget, new boolean[3][3]); return;
    	}
    	else {
    		Pathing.approach(target_loc, new boolean[3][3]); return;
    	}
    }
}
