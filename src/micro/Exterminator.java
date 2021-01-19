package micro;
import battlecode.common.*;

public class Exterminator {
	public static double MOMENTUM_CAP = 2;
	public static int EXTERMINATE_START_TIME = 1250;
	public static int EXTERMINATE_FINISH_MONEY_TIME = 1350;
	public static RobotController rc;
	
	public static int target_dist = 31;  // path distance measured as passability weighted euclidean distance
	public static MapLocation target_loc = new MapLocation(0, 0);
	public static double momentum_dx;
	public static double momentum_dy;

    public static void update() throws GameActionException {
    	for (int i=Info.n_exterminators; --i>=0;) {
    		RobotInfo robot = Info.exterminators[i];
    		int flag = rc.getFlag(robot.ID);
    		if (flag%32<target_dist) {
    			target_dist = flag%32;
    		}
    	}
    	target_dist = Math.min(target_dist+1, 31);
    }

    public static void lock_target(MapLocation loc) throws GameActionException {
    	target_dist = 0;
    	target_loc = loc;
    }
    
    public static void exterminate() throws GameActionException {
		CombatInfo.compute_self_empower_gains();
		if (Info.loc.distanceSquaredTo(target_loc)==1) {
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
    	if (target_dist>=31) {
    		double dx = 0;
    		double dy = 0;
			for (int i=Info.n_exterminators; --i>=0;) {
				if (Clock.getBytecodesLeft()>1700) {
    				MapLocation repel_loc = Info.exterminators[i].location;
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
			target_loc = Info.loc.translate((int)(10*momentum_dx), (int)(10*momentum_dy));
			Pathing.target(target_loc, new boolean[3][3], 1); return;
    	}
    	else if (Info.loc.distanceSquaredTo(target_loc)<64) {
    		Pathing.target(target_loc, new boolean[3][3], 1); return;
    	}
    	else {
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
    		else {
    			double dx = 0;
        		double dy = 0;
    			for (int i=Info.n_exterminators; --i>=0;) {
    				if (Clock.getBytecodesLeft()>1700) {
        				MapLocation other_loc = Info.exterminators[i].location;
        				int other_target_dist = rc.getFlag(Info.exterminators[i].ID)%32;
        				dx -= (other_target_dist-target_dist)*(other_loc.x-Info.x);
        				dy -= (other_target_dist-target_dist)*(other_loc.y-Info.y);
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
    			target_loc = Info.loc.translate((int)(10*momentum_dx), (int)(10*momentum_dy));
    			boolean[][] illegal_squares = new boolean[3][3];
    			for (Direction dir:Direction.allDirections()) {
    				if (Math2.cardinal_length(Info.loc.add(dir), target_loc)==2) {
    					illegal_squares[dir.dx+1][dir.dy+1] = true;
    				}
    			}
            	Pathing.target(target_loc, illegal_squares, 1); return;
    		}
    	}
    }
}
