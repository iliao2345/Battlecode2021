package micro;
import battlecode.common.*;

public class Info {
	public static RobotController rc;
	public static Team friendly;
	public static Team enemy;
	public static int id;
	
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
	public static RobotInfo[] sensable_robots;
	public static RobotInfo[] friendly_politicians = new RobotInfo[96];
	public static RobotInfo[] friendly_slanderers = new RobotInfo[96];
	public static RobotInfo[] friendly_muckrakers = new RobotInfo[96];
	public static RobotInfo[] friendly_ecs = new RobotInfo[20];
	public static RobotInfo[] enemy_politicians = new RobotInfo[96];
	public static RobotInfo[] enemy_slanderers = new RobotInfo[96];
	public static RobotInfo[] enemy_muckrakers = new RobotInfo[96];
	public static RobotInfo[] enemy_ecs = new RobotInfo[20];
	public static RobotInfo[] neutral_ecs = new RobotInfo[20];
	public static int n_friendly_politicians;
	public static int n_friendly_slanderers;
	public static int n_friendly_muckrakers;
	public static int n_friendly_ecs;
	public static int n_enemy_politicians;
	public static int n_enemy_slanderers;
	public static int n_enemy_muckrakers;
	public static int n_enemy_ecs;
	public static int n_neutral_ecs;
	public static RobotInfo closest_friendly_politician;
	public static RobotInfo closest_friendly_slanderer;
	public static RobotInfo closest_friendly_muckraker;
	public static RobotInfo closest_friendly_ec;
	public static RobotInfo closest_enemy_politician;
	public static RobotInfo closest_enemy_slanderer;
	public static RobotInfo closest_enemy_muckraker;
	public static RobotInfo closest_enemy_ec;
	public static RobotInfo closest_neutral_ec;
	public static IntCycler friendly_ec_ids = null;
	public static int n_friendly_ec_ids = 0;
	public static double unit_price = GameConstants.EMPOWER_TAX+1;  // retrieved from the EC
	
	public static void initialize(RobotController rc) {
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
		ECInfo.rc = rc;
		friendly = rc.getTeam();
		if (friendly==Team.A) {enemy=Team.B;}
		if (friendly==Team.B) {enemy=Team.A;}
		id = rc.getID();
		if (rc.getType()==RobotType.ENLIGHTENMENT_CENTER) {
			ECInfo.initialize();
		}
	}
	
	public static void update() throws GameActionException {
		ready = rc.isReady();
		loc = rc.getLocation();
		x = loc.x;
		y = loc.y;
		type = rc.getType();
		round_num = rc.getRoundNum();
		influence = rc.getInfluence();
		conviction = rc.getConviction();
		empower_buff = rc.getEmpowerFactor(Info.friendly, 0);
		enemy_empower_buff = rc.getEmpowerFactor(Info.friendly, 0);
		tile_cost = 1/rc.sensePassability(loc);
		sensable_robots = rc.senseNearbyRobots();
		n_friendly_politicians = 0;
		n_friendly_slanderers = 0;
		n_friendly_muckrakers = 0;
		n_friendly_ecs = 0;
		n_enemy_politicians = 0;
		n_enemy_slanderers = 0;
		n_enemy_muckrakers = 0;
		n_enemy_ecs = 0;
		n_neutral_ecs = 0;
		for (RobotInfo robot:sensable_robots) {
			if (robot.getTeam()==friendly) {
				switch (robot.getType()) {
				case POLITICIAN: {
					friendly_politicians[n_friendly_politicians] = robot;
					n_friendly_politicians++;
					break;}
				case SLANDERER: {
					friendly_slanderers[n_friendly_slanderers] = robot;
					n_friendly_slanderers++;
					break;}
				case MUCKRAKER: {
					friendly_muckrakers[n_friendly_muckrakers] = robot;
					n_friendly_muckrakers++;
					break;}
				case ENLIGHTENMENT_CENTER: {
					friendly_ecs[n_friendly_ecs] = robot;
					n_friendly_ecs++;
					boolean found = false;
					for (int i=n_friendly_ec_ids; --i>=0;) {
						if (friendly_ec_ids.data==robot.ID) {
							found = true; break;
						}
						friendly_ec_ids = friendly_ec_ids.next;
					}
					if (!found) {n_friendly_ec_ids++; friendly_ec_ids = new IntCycler(robot.ID, friendly_ec_ids);}
					break;}
				}
			}
			else if (robot.getTeam()==enemy) {
				switch (robot.getType()) {
				case POLITICIAN: {
					enemy_politicians[n_enemy_politicians] = robot;
					n_enemy_politicians++;
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
		double new_total_unit_price = 0;
		for (int i=n_friendly_ec_ids; --i>=0;) {
			if (!rc.canGetFlag(friendly_ec_ids.data)) {
				if (n_friendly_ec_ids>1) {
					friendly_ec_ids.next.last = friendly_ec_ids.last;
					friendly_ec_ids.last.next = friendly_ec_ids.next;
					friendly_ec_ids = friendly_ec_ids.next;
				}
				else {
					friendly_ec_ids = null;
				}
				n_friendly_ec_ids--;
			}
			else {
				int flag = rc.getFlag(friendly_ec_ids.data);
				new_total_unit_price += flag&32;
			}
		}
		if (n_friendly_ec_ids>0) {
			unit_price = new_total_unit_price / n_friendly_ec_ids;
		}
		
		//  This must be last
		if (rc.getType()==RobotType.ENLIGHTENMENT_CENTER) {
			ECInfo.update();
		}
		if (Role.is_relay_chain) {
			RelayChain.update();
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
