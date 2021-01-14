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

public class Politician {
	
	public static RobotController rc;

    public static void act() throws GameActionException {
    	if (!Phase.is_membrane && !Phase.is_gas) {  // set phase to gas by default or if converted from slanderer
    		Phase.is_gas = true;
    	}
		RobotInfo closest_friendly_ec = Info.closest_robot(Info.friendly, RobotType.ENLIGHTENMENT_CENTER);
		if (closest_friendly_ec!=null && Info.conviction>10) {
			int flag = rc.getFlag(closest_friendly_ec.ID);
			if ((flag>>1)%2==1) {  // friendly EC needs help unblocking spawn tiles covered by enemies
				Phase.is_gas = false;
				Phase.is_membrane = false;
				RobotInfo nearest_blocking_enemy = null;
				int lowest_distance = Integer.MAX_VALUE;
				for (Direction dir:Math2.UNIT_DIRECTIONS) {
					MapLocation loc = closest_friendly_ec.location.add(dir);
					if (rc.canSenseLocation(loc)) {
						RobotInfo robot = rc.senseRobotAtLocation(loc);
						if (robot!=null) {
							if (robot.team==Info.enemy && (nearest_blocking_enemy==null || Math2.cardinal_length(Info.loc, loc)<lowest_distance)) {
								nearest_blocking_enemy = robot;
								lowest_distance = Math2.cardinal_length(Info.loc, loc);
							}
						}
					}
				}
				if (lowest_distance==1) {  // kill blocking enemies if nearby
					rc.empower(Combat.compute_optimal_empower_radius());
				}
				else if (nearest_blocking_enemy!=null){  // chase blocking enemies if not nearby
					cardinal_stick(nearest_blocking_enemy.location);
				}
				else {  // find blocking enemies if unseen
					Pathing.stick(closest_friendly_ec.location);
				}
			}
		}
    	if (Action.can_still_move && Info.reabsorb) {  // reabsorb into friendly EC
    		Phase.is_gas = false;
    		if (closest_friendly_ec==null) {
    			travel_to_reabsorption_site(Info.spawn_location);
    		}
	    	else {
				int r2 = Info.loc.distanceSquaredTo(closest_friendly_ec.location);
				if (rc.detectNearbyRobots(r2).length==1) {
					rc.empower(r2);
				}
				else {
					travel_to_reabsorption_site(closest_friendly_ec.location);
				}
    		}
    	}
    	if (Action.can_still_move && Info.round_num>2700) {  // perform final extermination if full health
    		if (rc.senseNearbyRobots(1, Info.enemy).length>0) {
    			rc.empower(Combat.compute_optimal_empower_radius());
    		}
    	}
    	if (Action.can_still_move && Info.enemy_ecs.length>0) {  // stick to enemy EC
    		Phase.is_gas = false;
    		RobotInfo closest = Info.closest_robot(Info.enemy, RobotType.ENLIGHTENMENT_CENTER);
    		Pathing.stick(closest.location);
    	}
    	if (Action.can_still_move && Info.neutral_ecs.length>0) {  // try to convert neutral EC
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
    	if (Phase.is_gas && Membrane.touching_membrane && Info.round_num<2700) {
    		Phase.condense();
    	}
    	if (Phase.is_membrane) {
    		Membrane.outgas();
    	}
        if (Phase.is_membrane && Action.can_still_move) {
    		if (Membrane.push_signal>0) {
    			Membrane.advance();
    		}
    		else {
        		Membrane.heal();
    		}
    	}
    	if (Phase.is_gas && Action.can_still_move) {
    		Gas.attack();
    	}
		Flag.set_default_patrol();
    }
    public static void cardinal_stick(MapLocation target) throws GameActionException {
    	Direction best_dir = null;
    	double best_passability = 0.1;
    	int best_distance = Integer.MAX_VALUE;
    	for (Direction dir:Math2.UNIT_DIRECTIONS) {
    		if (rc.canMove(dir)) {
    			MapLocation adjacent = Info.loc.add(dir);
    			int distance = Math2.cardinal_length(adjacent, target);
    			double passability = rc.sensePassability(adjacent);
    			if (best_dir==null || distance < best_distance || distance==best_distance && passability>best_passability) {  // don't block other politicians which try to reabsorb
    				best_dir = dir;
    				best_distance = distance;
    				best_passability = passability;
    			}
    		}
    	}
    	if (best_distance>Math2.cardinal_length(Info.loc, target)) {
    		best_dir = Direction.CENTER;
    	}
    	if (best_dir!=null) {Action.move(best_dir);}
    }
    public static void travel_to_reabsorption_site(MapLocation target) throws GameActionException {
    	MapLocation ec_location = (Info.spawn_ec_location==null)? Info.spawn_location : Info.spawn_ec_location;
    	if (Info.loc.isWithinDistanceSquared(ec_location, 1)) {return;}
    	Direction best_dir = null;
    	int best_distance = Integer.MAX_VALUE;
    	for (Direction dir:Math2.UNIT_DIRECTIONS) {
    		if (rc.canMove(dir)) {
    			MapLocation adjacent = Info.loc.add(dir);
    			int distance = Math2.cardinal_length(adjacent, target);
    			boolean ingoing_line = (Math.abs((Info.x+dir.dx+4)%8-4)==Math.abs((Info.y+dir.dy)%8-4) || Info.loc.add(dir).isWithinDistanceSquared(ec_location, 10)) && distance!=2;
    			if (ingoing_line && (best_dir==null || distance < best_distance)) {  // don't block other politicians which try to reabsorb
    				best_dir = dir;
    				best_distance = distance;
    			}
    		}
    	}
    	if (best_dir!=null) {Action.move(best_dir);}
    	//  stay if already in ingoing line
    	if ((Math.abs((Info.x+4)%8-4)==Math.abs((Info.y)%8-4) || Info.loc.isWithinDistanceSquared(ec_location, 10)) && Math2.cardinal_length(Info.loc, ec_location)!=2) {return;}
    	int n_possible_directions = 0;
		best_dir = null;  // worst case move randomly
		for (Direction dir:Math2.UNIT_DIRECTIONS) {
			if (rc.canMove(dir) && Math.random()<1/(n_possible_directions+1)) {
				best_dir = dir;
			}
		}
		if (best_dir!=null) {Action.move(best_dir); return;}
    }
    
    public static void pause() throws GameActionException {
		Flag.set_default_patrol();
    }

}