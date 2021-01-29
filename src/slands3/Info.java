package slands3;
import battlecode.common.*;

public class Info {
	public static RobotController rc;
	public static Team friendly;
	public static Team enemy;
	public static int id;
	public static int spawn_round;
	
	public static boolean ready;
	public static MapLocation loc;
	public static int x;
	public static int y;
	public static RobotType type;
	public static int round_num;
	public static int influence;
	public static int conviction;
	public static double empower_buff;
	public static double enemy_empower_buff;
	public static double tile_cost;
	public static Direction last_move_direction = Math2.UNIT_DIRECTIONS[(int)(Math.random()*8)];
	public static int n_sensable_robots;
	public static RobotInfo[] restricted_sensable_robots;
	public static RobotInfo[] friendly_politicians = new RobotInfo[24];
	public static RobotInfo[] friendly_slanderers = new RobotInfo[28];
	public static RobotInfo[] friendly_muckrakers = new RobotInfo[24];
	public static RobotInfo[] friendly_ecs = new RobotInfo[12];
	public static RobotInfo[] enemy_politicians = new RobotInfo[24];
	public static RobotInfo[] enemy_slanderers = new RobotInfo[24];
	public static RobotInfo[] enemy_muckrakers = new RobotInfo[24];
	public static RobotInfo[] enemy_ecs = new RobotInfo[12];
	public static RobotInfo[] neutral_ecs = new RobotInfo[12];
	public static RobotInfo[] relayers = new RobotInfo[28];
	public static RobotInfo[] guards = new RobotInfo[24];
	public static RobotInfo[] buriers = new RobotInfo[24];
	public static RobotInfo[] targetters = new RobotInfo[24];
	public static RobotInfo[] exterminators = new RobotInfo[24];
	public static int n_friendly_politicians;
	public static int n_friendly_slanderers;
	public static int n_friendly_muckrakers;
	public static int n_friendly_ecs;
	public static int n_enemy_politicians;
	public static int n_enemy_slanderers;
	public static int n_enemy_muckrakers;
	public static int n_enemy_ecs;
	public static int n_neutral_ecs;
	public static int n_relayers;
	public static int n_guards;
	public static int n_buriers;
	public static int n_targetters;
	public static int n_exterminators;
	public static RobotInfo closest_friendly_politician;
	public static RobotInfo closest_friendly_slanderer;
	public static RobotInfo closest_friendly_muckraker;
	public static RobotInfo closest_friendly_ec;
	public static RobotInfo closest_enemy_politician;
	public static RobotInfo closest_enemy_slanderer;
	public static RobotInfo closest_enemy_muckraker;
	public static RobotInfo closest_enemy_ec;
	public static RobotInfo closest_neutral_ec;
	public static IntCycler tracked_friendly_ec_ids = null;
	public static IntCycler tracked_friendly_ec_x = null;
	public static IntCycler tracked_friendly_ec_y = null;
	public static int n_tracked_friendly_ecs = 0;
	public static double unit_price = 10;  // retrieved from the EC
	public static boolean ec_needs_guards;
	public static int n_adjacent_robots;
	public static int n_adjacent_tiles_on_map;
	public static double crowdedness;
	public static boolean exterminate;
	public static boolean everything_buried;
	public static RobotInfo weak_burier;
	public static int last_team_votes = 0;
	public static int team_votes = 0;
	public static int nearby_enemy_power;
	public static int low_x_bound;
	public static int high_x_bound;
	public static int low_y_bound;
	public static int high_y_bound;
	public static MapLocation spawn_location;
	public static boolean began_as_slanderer;
	
	public static void initialize(RobotController rc) throws GameActionException {
		Info.rc = rc;
		CombatInfo.rc = rc;
		Flag.rc = rc;
		Action.rc = rc;
		EnlightenmentCenter.rc = rc;
		Muckraker.rc = rc;
		Politician.rc = rc;
		Slanderer.rc = rc;
		Pathing.rc = rc;
		Role.rc = rc;
		RelayChain.rc = rc;
		Guard.rc = rc;
		Burier.rc = rc;
		Targetter.rc = rc;
		Exterminator.rc = rc;
		ECInfo.rc = rc;
		friendly = rc.getTeam();
		if (friendly==Team.A) {enemy=Team.B;}
		if (friendly==Team.B) {enemy=Team.A;}
		id = rc.getID();
		if (rc.getType()==RobotType.ENLIGHTENMENT_CENTER) {
			ECInfo.initialize();
		}
		spawn_round = rc.getRoundNum();
		low_x_bound = rc.getLocation().x;
		high_x_bound = rc.getLocation().x;
		low_y_bound = rc.getLocation().y;
		high_y_bound = rc.getLocation().y;
		for (int i=0; i<5; i++) {
			if (rc.canSenseLocation(new MapLocation(low_x_bound-1, rc.getLocation().y))) {low_x_bound--;}
			if (rc.canSenseLocation(new MapLocation(high_x_bound+1, rc.getLocation().y))) {high_x_bound++;}
			if (rc.canSenseLocation(new MapLocation(rc.getLocation().x, low_y_bound-1))) {low_y_bound--;}
			if (rc.canSenseLocation(new MapLocation(rc.getLocation().x, high_y_bound+1))) {high_y_bound++;}
		}
		spawn_location = rc.getLocation();
		began_as_slanderer = rc.getType()==RobotType.SLANDERER;
	}
	
	public static void update() throws GameActionException {
		loc = rc.getLocation();
		x = loc.x;
		y = loc.y;
		type = rc.getType();
		round_num = rc.getRoundNum();
		ready = rc.isReady();
		if (round_num==spawn_round && type==RobotType.SLANDERER) {ready = false; return;}  // initialize some large arrays
		influence = rc.getInfluence();
		conviction = rc.getConviction();
		empower_buff = rc.getEmpowerFactor(Info.friendly, 0);
		enemy_empower_buff = rc.getEmpowerFactor(Info.friendly, 0);
		tile_cost = 1/rc.sensePassability(loc);
		last_team_votes = team_votes;
		team_votes = rc.getTeamVotes();
		if (rc.canSenseLocation(new MapLocation(low_x_bound-1, y))) {low_x_bound--;}
		if (rc.canSenseLocation(new MapLocation(high_x_bound+1, y))) {high_x_bound++;}
		if (rc.canSenseLocation(new MapLocation(x, low_y_bound-1))) {low_y_bound--;}
		if (rc.canSenseLocation(new MapLocation(x, high_y_bound+1))) {high_y_bound++;}
		restricted_sensable_robots = rc.senseNearbyRobots();
		n_sensable_robots = restricted_sensable_robots.length;
		if (type!=RobotType.SLANDERER) {
			if (restricted_sensable_robots.length>24) {restricted_sensable_robots = rc.senseNearbyRobots(17);}
			if (restricted_sensable_robots.length>24) {restricted_sensable_robots = rc.senseNearbyRobots(8);}
		}
		else {
			if (restricted_sensable_robots.length>12) {restricted_sensable_robots = rc.senseNearbyRobots(10);}
			if (restricted_sensable_robots.length>12) {restricted_sensable_robots = rc.senseNearbyRobots(4);}
		}
		closest_friendly_politician = null;
		closest_friendly_slanderer = null;
		closest_friendly_muckraker = null;
		closest_friendly_ec = null;
		closest_enemy_politician = null;
		closest_enemy_slanderer = null;
		closest_enemy_muckraker = null;
		closest_enemy_ec = null;
		closest_neutral_ec = null;
		n_friendly_politicians = 0;
		n_friendly_slanderers = 0;
		n_friendly_muckrakers = 0;
		n_friendly_ecs = 0;
		n_enemy_politicians = 0;
		n_enemy_slanderers = 0;
		n_enemy_muckrakers = 0;
		n_enemy_ecs = 0;
		n_neutral_ecs = 0;
		n_relayers = 0;
		n_guards = 0;
		n_buriers = 0;
		n_targetters = 0;
		n_exterminators = 0;
		everything_buried = true;
		weak_burier = null;
		nearby_enemy_power = 0;
		for (RobotInfo robot:restricted_sensable_robots) {
			if (robot.getTeam()==friendly) {
				switch (robot.getType()) {
				case POLITICIAN: {
					friendly_politicians[n_friendly_politicians] = robot;
					n_friendly_politicians++;
					int flag = rc.getFlag(robot.ID);
					if (flag>>23==1) {
						relayers[n_relayers] = robot;
						n_relayers++;
//						rc.setIndicatorDot(robot.location, 0, 0, 255);
						break;
					}
					else if (flag>>22==1) {
						guards[n_guards] = robot;
						n_guards++;
//						rc.setIndicatorDot(robot.location, 0, 255, 0);
						break;
					}
					else if (flag>>21==1) {
						buriers[n_buriers] = robot;
						n_buriers++;
//						rc.setIndicatorDot(robot.location, 0, 0, 0);
						if (flag%2==1) {everything_buried = false; weak_burier = robot;}
						break;
					}
					else if (flag>>20==1) {
						targetters[n_targetters] = robot;
						n_targetters++;
//						rc.setIndicatorDot(robot.location, 255, 0, 0);
						break;
					}
					else if (flag>>19==1) {
						exterminators[n_exterminators] = robot;
						n_exterminators++;
//						rc.setIndicatorDot(robot.location, 128, 0, 0);
						break;
					}
					else if (flag>>18==1) {
						friendly_slanderers[n_friendly_slanderers] = robot;
						n_friendly_slanderers++;
//						rc.setIndicatorDot(robot.location, 255, 255, 0);
						break;
					}
					break;}
				case SLANDERER: {
					friendly_slanderers[n_friendly_slanderers] = robot;
					n_friendly_slanderers++;
//					rc.setIndicatorDot(robot.location, 255, 255, 0);
					break;}
				case MUCKRAKER: {
					friendly_muckrakers[n_friendly_muckrakers] = robot;
					n_friendly_muckrakers++;
					int flag = rc.getFlag(robot.ID);
					if (flag>>23==1) {
						relayers[n_relayers] = robot;
						n_relayers++;
//						rc.setIndicatorDot(robot.location, 0, 0, 255);
						break;
					}
					else if (flag>>21==1) {
						buriers[n_buriers] = robot;
						n_buriers++;
//						rc.setIndicatorDot(robot.location, 0, 0, 0);
						if (flag%2==1) {everything_buried = false; weak_burier = robot;}
						break;
					}
					break;}
				case ENLIGHTENMENT_CENTER: {
					friendly_ecs[n_friendly_ecs] = robot;
					n_friendly_ecs++;
					boolean found = false;
					for (int i=n_tracked_friendly_ecs; --i>=0;) {
						if (tracked_friendly_ec_ids.data==robot.ID) {
							found = true; break;
						}
						tracked_friendly_ec_ids = tracked_friendly_ec_ids.next;
						tracked_friendly_ec_x = tracked_friendly_ec_x.next;
						tracked_friendly_ec_y = tracked_friendly_ec_y.next;
					}
					if (!found) {
						n_tracked_friendly_ecs++;
						tracked_friendly_ec_ids = new IntCycler(robot.ID, tracked_friendly_ec_ids);
						tracked_friendly_ec_x = new IntCycler(robot.location.x, tracked_friendly_ec_x);
						tracked_friendly_ec_y = new IntCycler(robot.location.y, tracked_friendly_ec_y);
					}
					break;}
				}
			}
			else if (robot.getTeam()==enemy) {
				switch (robot.getType()) {
				case POLITICIAN: {
					enemy_politicians[n_enemy_politicians] = robot;
					n_enemy_politicians++;
					nearby_enemy_power += Math.max(0, enemy_empower_buff*robot.conviction-GameConstants.EMPOWER_TAX);
					break;}
				case SLANDERER: {
					enemy_slanderers[n_enemy_slanderers] = robot;
					n_enemy_slanderers++;
					break;}
				case MUCKRAKER: {
					enemy_muckrakers[n_enemy_muckrakers] = robot;
					n_enemy_muckrakers++;
					break;}
				case ENLIGHTENMENT_CENTER: {
					enemy_ecs[n_enemy_ecs] = robot;
					n_enemy_ecs++;
					break;}
				}
			}
			else {
				neutral_ecs[n_neutral_ecs] = robot;
				n_neutral_ecs++;
			}
		}
//		if (round_num==spawn_round && type==RobotType.POLITICIAN && n_friendly_ecs==0) {ready = false; return;}  // initialize some large arrays if just got converted
		if (n_buriers==0) {everything_buried = false;}
		double new_total_unit_price = 0;
		ec_needs_guards = false;
		exterminate = round_num>Exterminator.EXTERMINATE_START_TIME;
		for (int i=n_tracked_friendly_ecs; --i>=0;) {
			if (!rc.canGetFlag(tracked_friendly_ec_ids.data)) {
				if (n_tracked_friendly_ecs>1) {
					tracked_friendly_ec_ids.next.last = tracked_friendly_ec_ids.last;
					tracked_friendly_ec_ids.last.next = tracked_friendly_ec_ids.next;
					tracked_friendly_ec_ids = tracked_friendly_ec_ids.next;
					tracked_friendly_ec_x.next.last = tracked_friendly_ec_x.last;
					tracked_friendly_ec_x.last.next = tracked_friendly_ec_x.next;
					tracked_friendly_ec_x = tracked_friendly_ec_x.next;
					tracked_friendly_ec_y.next.last = tracked_friendly_ec_y.last;
					tracked_friendly_ec_y.last.next = tracked_friendly_ec_y.next;
					tracked_friendly_ec_y = tracked_friendly_ec_y.next;
				}
				else {
					tracked_friendly_ec_ids = null;
					tracked_friendly_ec_x = null;
					tracked_friendly_ec_y = null;
				}
				n_tracked_friendly_ecs--;
			}
			else {
				int flag = rc.getFlag(tracked_friendly_ec_ids.data);
				new_total_unit_price += 4*(Math.exp((flag%32)/4.0)-1);
				ec_needs_guards = ec_needs_guards || flag>>23==1;
				exterminate = exterminate || (flag>>22)%2==1;
				tracked_friendly_ec_ids = tracked_friendly_ec_ids.next;
				tracked_friendly_ec_x = tracked_friendly_ec_x.next;
				tracked_friendly_ec_y = tracked_friendly_ec_y.next;
			}
		}
		if (n_tracked_friendly_ecs>0) {
			unit_price = new_total_unit_price / n_tracked_friendly_ecs;
		}
		int n_directions_restricted = 0;
		if (!rc.onTheMap(loc.add(Direction.EAST)) || !rc.onTheMap(loc.add(Direction.WEST))) {n_directions_restricted++;}
		if (!rc.onTheMap(loc.add(Direction.NORTH)) || !rc.onTheMap(loc.add(Direction.SOUTH))) {n_directions_restricted++;}
		n_adjacent_robots = rc.senseNearbyRobots(2).length;
		n_adjacent_tiles_on_map = (n_directions_restricted>0)?((n_directions_restricted==2)?3:5):8;
		crowdedness = n_adjacent_robots/(double)n_adjacent_tiles_on_map;
		
		//  This must be last
		if (rc.getType()==RobotType.ENLIGHTENMENT_CENTER) {
			ECInfo.update();
		}
		if (Role.is_relay_chain) {
			RelayChain.update();
		}
		else if (Role.is_guard) {
			Guard.update();
		}
		else if (Role.is_burier) {
			Burier.update();
		}
		else if (Role.is_targetter) {
			Targetter.update();
		}
		else if (Role.is_exterminator) {
			Exterminator.update();
		}
		else if (type==RobotType.SLANDERER) {
			Slanderer.update();
		}
	}
	public static RobotInfo closest_robot(Team relevant_team, RobotType relevant_type) {
    	RobotInfo closest_robot = null;
        int closest_distance = Integer.MAX_VALUE;
		if (relevant_team==friendly) {
	        if (relevant_type==RobotType.POLITICIAN) {
	        	if (closest_friendly_politician==null) {
		            for (int i=n_friendly_politicians; --i>=0;) {
		            	if (loc.distanceSquaredTo(friendly_politicians[i].location)<closest_distance) {
		            		closest_robot = friendly_politicians[i];
		            		closest_distance = loc.distanceSquaredTo(friendly_politicians[i].location);
		            	}
		            }
		            closest_friendly_politician = closest_robot;
	        	}
	            return closest_friendly_politician;
	        }
	        else if (relevant_type==RobotType.SLANDERER) {
	        	if (closest_friendly_slanderer==null) {
		            for (int i=n_friendly_slanderers; --i>=0;) {
		            	if (loc.distanceSquaredTo(friendly_slanderers[i].location)<closest_distance) {
		            		closest_robot = friendly_slanderers[i];
		            		closest_distance = loc.distanceSquaredTo(friendly_slanderers[i].location);
		            	}
		            }
		            closest_friendly_slanderer = closest_robot;
	        	}
	            return closest_friendly_slanderer;
	        }
	        else if (relevant_type==RobotType.MUCKRAKER) {
	        	if (closest_friendly_muckraker==null) {
		            for (int i=n_friendly_muckrakers; --i>=0;) {
		            	if (loc.distanceSquaredTo(friendly_muckrakers[i].location)<closest_distance) {
		            		closest_robot = friendly_muckrakers[i];
		            		closest_distance = loc.distanceSquaredTo(friendly_muckrakers[i].location);
		            	}
		            }
		            closest_friendly_muckraker = closest_robot;
	        	}
	            return closest_friendly_muckraker;
	        }
	        else {
	        	if (closest_friendly_ec==null) {
		            for (int i=n_friendly_ecs; --i>=0;) {
		            	if (loc.distanceSquaredTo(friendly_ecs[i].location)<closest_distance) {
		            		closest_robot = friendly_ecs[i];
		            		closest_distance = loc.distanceSquaredTo(friendly_ecs[i].location);
		            	}
		            }
		            closest_friendly_ec = closest_robot;
	        	}
	            return closest_friendly_ec;
	        }
		}
		else if (relevant_team==enemy) {
			if (relevant_type==RobotType.POLITICIAN) {
	        	if (closest_enemy_politician==null) {
		            for (int i=n_enemy_politicians; --i>=0;) {
		            	if (loc.distanceSquaredTo(enemy_politicians[i].location)<closest_distance) {
		            		closest_robot = enemy_politicians[i];
		            		closest_distance = loc.distanceSquaredTo(enemy_politicians[i].location);
		            	}
		            }
		            closest_enemy_politician = closest_robot;
	        	}
	            return closest_enemy_politician;
	        }
	        else if (relevant_type==RobotType.SLANDERER) {
	        	if (closest_enemy_slanderer==null) {
		            for (int i=n_enemy_slanderers; --i>=0;) {
		            	if (loc.distanceSquaredTo(enemy_slanderers[i].location)<closest_distance) {
		            		closest_robot = enemy_slanderers[i];
		            		closest_distance = loc.distanceSquaredTo(enemy_slanderers[i].location);
		            	}
		            }
		            closest_enemy_slanderer = closest_robot;
	        	}
	            return closest_enemy_slanderer;
	        }
	        else if (relevant_type==RobotType.MUCKRAKER) {
	        	if (closest_enemy_muckraker==null) {
		            for (int i=n_enemy_muckrakers; --i>=0;) {
		            	if (loc.distanceSquaredTo(enemy_muckrakers[i].location)<closest_distance) {
		            		closest_robot = enemy_muckrakers[i];
		            		closest_distance = loc.distanceSquaredTo(enemy_muckrakers[i].location);
		            	}
		            }
		            closest_enemy_muckraker = closest_robot;
	        	}
	            return closest_enemy_muckraker;
	        }
	        else {
	        	if (closest_enemy_ec==null) {
		            for (int i=n_enemy_ecs; --i>=0;) {
		            	if (loc.distanceSquaredTo(enemy_ecs[i].location)<closest_distance) {
		            		closest_robot = enemy_ecs[i];
		            		closest_distance = loc.distanceSquaredTo(enemy_ecs[i].location);
		            	}
		            }
		            closest_enemy_ec = closest_robot;
	        	}
	            return closest_enemy_ec;
	        }
		}
		else {
        	if (closest_neutral_ec==null) {
	            for (int i=n_neutral_ecs; --i>=0;) {
	            	if (loc.distanceSquaredTo(neutral_ecs[i].location)<closest_distance) {
	            		closest_robot = neutral_ecs[i];
	            		closest_distance = loc.distanceSquaredTo(neutral_ecs[i].location);
	            	}
	            }
	            closest_neutral_ec = closest_robot;
        	}
            return closest_neutral_ec;
		}
	}
}
