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

public class Info {
	public static RobotInfo[] sensable_robots;
	public static RobotInfo[] friendly_politicians;
	public static RobotInfo[] friendly_slanderers;
	public static RobotInfo[] friendly_muckrakers;
	public static RobotInfo[] friendly_ecs;
	public static RobotInfo[] enemy_politicians;
	public static RobotInfo[] enemy_slanderers;
	public static RobotInfo[] enemy_muckrakers;
	public static RobotInfo[] enemy_ecs;
	public static RobotInfo[] neutral_ecs;
	public static RobotInfo closest_friendly_politician;
	public static RobotInfo closest_friendly_slanderer;
	public static RobotInfo closest_friendly_muckraker;
	public static RobotInfo closest_friendly_ec;
	public static RobotInfo closest_enemy_politician;
	public static RobotInfo closest_enemy_slanderer;
	public static RobotInfo closest_enemy_muckraker;
	public static RobotInfo closest_enemy_ec;
	public static RobotInfo closest_neutral_ec;
	public static RobotController rc;
	public static Team friendly;
	public static Team enemy;
	public static int spawn_turn;
	public static int min_x;
	public static int max_x;
	public static int min_y;
	public static int max_y;
	public static boolean min_x_known = false;
	public static boolean max_x_known = false;
	public static boolean min_y_known = false;
	public static boolean max_y_known = false;
	public static MapLocation loc;
	public static int x;
	public static int y;
	public static RobotType type;
	public static int detect_r2;
	public static int detect_r;
	public static Direction last_move_direction = Direction.EAST;
	public static int cooldown_turns;
	public static boolean ready;
	public static int id;
	public static int round_num;
	public static int conviction;
	public static int approx_enemy_dist = 0;  // starts from 127 (meaning no distance) and decreases to 0 (maximum distance)  // muckrakers and politicians don't count
	public static double tile_cost;
	public static boolean need_to_relay_enemy_dist = true;  // true iff you can see a friendly unit which is farther from the enemy than self
	public static double empower_buff = 1;  // true iff you can see a friendly unit which is farther from the enemy than self
	public static double enemy_empower_buff = 1;  // true iff you can see a friendly unit which is farther from the enemy than self
	public static RobotInfo[][] adjacent_robots;  // adjacent_robots[1][1] is undefined
	public static boolean[][] on_map;  // on_map[1][1] is undefined
	public static int muckraker_warning_level = 0;  // starts from 0 (no warning) and increases to 7 (maximum warning)
	public static boolean need_to_relay_muckraker_warning = true;  // true iff you can see a friendly unit which is farther from the enemy than self
	public static boolean need_protection = false;  // for slanderer use
	public static boolean began_as_slanderer;
	public static MapLocation spawn_location;
	public static boolean reabsorb;
	public static MapLocation spawn_ec_location = null;
	
	public static void initialize(RobotController rc) {
		Action.rc = rc;
		EnlightenmentCenter.rc = rc;
		Flag.rc = rc;
		Info.rc = rc;
		Muckraker.rc = rc;
		Pathing.rc = rc;
		Politician.rc = rc;
		Slanderer.rc = rc;
		Phase.rc = rc;
		Gas.rc = rc;
		Membrane.rc = rc;
		Combat.rc = rc;
		friendly = rc.getTeam();
		if (friendly==Team.A) {enemy=Team.B;}
		if (friendly==Team.B) {enemy=Team.A;}
		loc = rc.getLocation();
		x = loc.x;
		y = loc.y;
		min_x = x;
		max_x = x;
		min_y = y;
		max_y = y;
		if (rc.getType()==RobotType.ENLIGHTENMENT_CENTER) {
			ECInfo.initialize(rc);
		}
		id = rc.getID();
		Phase.is_gas = rc.getType()!=RobotType.SLANDERER;
		began_as_slanderer = rc.getType()==RobotType.SLANDERER;
		spawn_location = rc.getLocation();
	}
	
	public static void update() throws GameActionException {
		type = rc.getType();
		detect_r2 = type.detectionRadiusSquared;
		detect_r = (int) Math.sqrt(type.detectionRadiusSquared);
		loc = rc.getLocation();
		x = loc.x;
		y = loc.y;
		if (!min_x_known) {
			if (x-(min_x-1)<=detect_r) {
				if (rc.canDetectLocation(loc.translate((min_x-1)-x, 0))) {min_x -= 1;}
				else {min_x_known = true;}
			}
		}
		if (!max_x_known) {
			if ((max_x+1)-x<=detect_r) {
				if (rc.canDetectLocation(loc.translate((max_x+1)-x, 0))) {max_x += 1;}
				else {max_x_known = true;}
			}
		}
		if (!min_y_known) {
			if (y-(min_y-1)<=detect_r) {
				if (rc.canDetectLocation(loc.translate(0, (min_y-1)-y))) {min_y -= 1;}
				else {min_y_known = true;}
			}
		}
		if (!max_y_known) {
			if ((max_y+1)-y<=detect_r) {
				if (rc.canDetectLocation(loc.translate(0, (max_y+1)-y))) {max_y += 1;}
				else {max_y_known = true;}
			}
		}
		sensable_robots = rc.senseNearbyRobots();
		if (sensable_robots.length>36 && type!=RobotType.SLANDERER) {sensable_robots = rc.senseNearbyRobots(17);}
		if (sensable_robots.length>36 && type!=RobotType.SLANDERER) {sensable_robots = rc.senseNearbyRobots(10);}
		if (sensable_robots.length>24 && type==RobotType.SLANDERER) {sensable_robots = rc.senseNearbyRobots(12);}
		if (sensable_robots.length>24 && type==RobotType.SLANDERER) {sensable_robots = rc.senseNearbyRobots(8);}
		int n_friendly_politicians = 0;
		int n_friendly_slanderers = 0;
		int n_friendly_muckrakers = 0;
		int n_friendly_ecs = 0;
		int n_enemy_politicians = 0;
		int n_enemy_slanderers = 0;
		int n_enemy_muckrakers = 0;
		int n_enemy_ecs = 0;
		int n_neutral_ecs = 0;
		for (RobotInfo robot:sensable_robots) {
			if (robot.getTeam()==friendly) {
				switch (robot.getType()) {
				case POLITICIAN:
					if ((rc.getFlag(robot.ID)>>9)%2==1) {n_friendly_slanderers++;}
					else {
						n_friendly_politicians++;
					}
					break;
				case SLANDERER:
					n_friendly_slanderers++;
					break;
				case MUCKRAKER:
					n_friendly_muckrakers++;
					break;
				case ENLIGHTENMENT_CENTER:
					n_friendly_ecs++;
					break;
				}
			}
			else if (robot.getTeam()==enemy) {
				switch (robot.getType()) {
				case POLITICIAN:
					n_enemy_politicians++;
					break;
				case SLANDERER:
					n_enemy_slanderers++;
					break;
				case MUCKRAKER:
					n_enemy_muckrakers++;
					break;
				case ENLIGHTENMENT_CENTER:
					n_enemy_ecs++;
					break;
				}
			}
			else {
				n_neutral_ecs++;
			}
		}
		friendly_politicians = new RobotInfo[n_friendly_politicians];
		friendly_slanderers = new RobotInfo[n_friendly_slanderers];
		friendly_muckrakers = new RobotInfo[n_friendly_muckrakers];
		friendly_ecs = new RobotInfo[n_friendly_ecs];
		enemy_politicians = new RobotInfo[n_enemy_politicians];
		enemy_slanderers = new RobotInfo[n_enemy_slanderers];
		enemy_muckrakers = new RobotInfo[n_enemy_muckrakers];
		enemy_ecs = new RobotInfo[n_enemy_ecs];
		neutral_ecs = new RobotInfo[n_neutral_ecs];
		n_friendly_politicians = 0;
		n_friendly_slanderers = 0;
		n_friendly_muckrakers = 0;
		n_friendly_ecs = 0;
		n_enemy_politicians = 0;
		n_enemy_slanderers = 0;
		n_enemy_muckrakers = 0;
		n_enemy_ecs = 0;
		n_neutral_ecs = 0;
		tile_cost = 1/rc.sensePassability(loc);
		approx_enemy_dist = Math.max(approx_enemy_dist-1, 0);
		if (round_num>2700 && enemy_ecs.length+enemy_politicians.length+enemy_slanderers.length+enemy_muckrakers.length>0) {  // override pathfinding for final extermination
			approx_enemy_dist = 255;
		}
		muckraker_warning_level = Math.max(muckraker_warning_level-((round_num%5==0)?1:0), 0);  // decreases by 1 every 5 turns
		if (enemy_muckrakers.length>0) {muckraker_warning_level = 7;}
		need_to_relay_enemy_dist = true;
		need_to_relay_muckraker_warning = true;
		for (RobotInfo robot:sensable_robots) {
			if (robot.getTeam()==friendly) {
				switch (robot.getType()) {
				case POLITICIAN: {
					int flag = rc.getFlag(robot.getID());
					if ((flag>>9)%2==1) {
						friendly_slanderers[n_friendly_slanderers] = robot;
						n_friendly_slanderers++;
					}
					else {
						friendly_politicians[n_friendly_politicians] = robot;
						n_friendly_politicians++;
						int robot_enemy_dist = (flag>>1)%Flag.MAX_APPROX_DIST_PLUS_ONE;
						int additional_cost = (int) (tile_cost*(Math2.length(loc, robot.location)+1));
						if (robot_enemy_dist-additional_cost>approx_enemy_dist) {approx_enemy_dist = robot_enemy_dist-additional_cost;}
						if (robot_enemy_dist<approx_enemy_dist && flag%2==1) {need_to_relay_enemy_dist = false;}
						muckraker_warning_level = Math.max(muckraker_warning_level, (flag>>10)%8-1);
						if ((flag>>10)%8<muckraker_warning_level && flag%2==1) {need_to_relay_muckraker_warning = false;}
					}
					break;
				}
				case SLANDERER: {
					friendly_slanderers[n_friendly_slanderers] = robot;
					n_friendly_slanderers++;
					break;
				}
				case MUCKRAKER: {
					friendly_muckrakers[n_friendly_muckrakers] = robot;
					n_friendly_muckrakers++;
					int flag = rc.getFlag(robot.getID());
					int robot_enemy_dist = (flag>>1)%Flag.MAX_APPROX_DIST_PLUS_ONE;
					int additional_cost = (int) (tile_cost*(Math2.length(loc, robot.location)+1));
					if (robot_enemy_dist-additional_cost>approx_enemy_dist) {approx_enemy_dist = robot_enemy_dist-additional_cost;}
					if (robot_enemy_dist<approx_enemy_dist && flag%2==1) {need_to_relay_enemy_dist = false;}
					muckraker_warning_level = Math.max(muckraker_warning_level, (flag>>10)%8-1);
					if ((flag>>10)%8<muckraker_warning_level && flag%2==1) {need_to_relay_muckraker_warning = false;}
					break;
				}
				case ENLIGHTENMENT_CENTER: {
					friendly_ecs[n_friendly_ecs] = robot;
					n_friendly_ecs++;
					break;
				}
				}
			}
			else if (robot.getTeam()==enemy) {
				switch (robot.getType()) {
				case POLITICIAN:
					enemy_politicians[n_enemy_politicians] = robot;
					n_enemy_politicians++;
					break;
				case SLANDERER:
					enemy_slanderers[n_enemy_slanderers] = robot;
					n_enemy_slanderers++;
					break;
				case MUCKRAKER:
					enemy_muckrakers[n_enemy_muckrakers] = robot;
					n_enemy_muckrakers++;
					break;
				case ENLIGHTENMENT_CENTER:
					enemy_ecs[n_enemy_ecs] = robot;
					n_enemy_ecs++;
					break;
				}
			}
			else {
				neutral_ecs[n_neutral_ecs] = robot;
				n_neutral_ecs++;
			}
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
		round_num = rc.getRoundNum();
		cooldown_turns = (int) rc.getCooldownTurns();
		ready = cooldown_turns==0;
		Action.acted = false;
		Action.can_still_move = true;
		conviction = rc.getConviction();
		closest_enemy_politician = closest_robot(enemy, RobotType.POLITICIAN);
		closest_enemy_slanderer = closest_robot(enemy, RobotType.SLANDERER);
		closest_enemy_ec = closest_robot(enemy, RobotType.ENLIGHTENMENT_CENTER);
//		if (closest_enemy_slanderer!=null) {
//			approx_enemy_dist = Math.max(approx_enemy_dist, Flag.MAX_APPROX_DIST-(int) (tile_cost*Math2.length(loc, closest_enemy_slanderer.location)));
//		}
//		if (closest_enemy_ec!=null) {
//			approx_enemy_dist = Math.max(approx_enemy_dist, Flag.MAX_APPROX_DIST-(int) (tile_cost*Math2.length(loc, closest_enemy_ec.location)));
//		}
		empower_buff = rc.getEmpowerFactor(Info.friendly, 0);
		enemy_empower_buff = rc.getEmpowerFactor(Info.friendly, 0);
		on_map = new boolean[3][3];
		adjacent_robots = new RobotInfo[3][3];
		for (Direction dir:Math2.UNIT_DIRECTIONS) {
			if (rc.onTheMap(Info.loc.add(dir))) {
				int i = dir.dx+1;
				int j = dir.dy+1;
				on_map[i][j] = true;
				RobotInfo robot = rc.senseRobotAtLocation(Info.loc.add(dir));
				adjacent_robots[i][j] = robot;
			}
		}
		need_protection = type==RobotType.SLANDERER && round_num>1000;  // for slanderers only
		reabsorb = type==RobotType.POLITICIAN && (began_as_slanderer && Info.round_num<2700 ||
				friendly_ecs.length>0 && enemy_politicians.length+enemy_muckrakers.length==0 && conviction*rc.getEmpowerFactor(Info.friendly, 0)-GameConstants.EMPOWER_TAX > conviction);
		if (spawn_ec_location==null && friendly_ecs.length>0) {spawn_ec_location = friendly_ecs[0].location;}
		
		// This should always be last no matter what is added above
		if (type==RobotType.ENLIGHTENMENT_CENTER) {
			ECInfo.update();
		}
		if (type==RobotType.POLITICIAN || type==RobotType.MUCKRAKER) {
			Membrane.update();
		}
	}
	
	public static RobotInfo closest_robot(Team relevant_team, RobotType relevant_type) {
		RobotInfo[] relevant_robots;
		if (relevant_team==friendly) {
	        if (relevant_type==RobotType.POLITICIAN) {
	        	if (closest_friendly_politician!=null) {return closest_friendly_politician;}
	        	relevant_robots = Info.friendly_politicians;
	        }
	        else if (relevant_type==RobotType.SLANDERER) {
	        	if (closest_friendly_slanderer!=null) {return closest_friendly_slanderer;}
	        	relevant_robots = Info.friendly_slanderers;
	        }
	        else if (relevant_type==RobotType.MUCKRAKER) {
	        	if (closest_friendly_muckraker!=null) {return closest_friendly_muckraker;}
	        	relevant_robots = Info.friendly_muckrakers;
	        }
	        else {
	        	if (closest_friendly_ec!=null) {return closest_friendly_ec;}
	        	relevant_robots = Info.friendly_ecs;
	        }
		}
		else if (relevant_team==enemy) {
	        if (relevant_type==RobotType.POLITICIAN) {
	        	if (closest_enemy_politician!=null) {return closest_enemy_politician;}
	        	relevant_robots = Info.enemy_politicians;
	        }
	        else if (relevant_type==RobotType.SLANDERER) {
	        	if (closest_enemy_slanderer!=null) {return closest_enemy_slanderer;}
	        	relevant_robots = Info.enemy_slanderers;
	        }
	        else if (relevant_type==RobotType.MUCKRAKER) {
	        	if (closest_enemy_muckraker!=null) {return closest_enemy_muckraker;}
	        	relevant_robots = Info.enemy_muckrakers;
	        }
	        else {
	        	if (closest_enemy_ec!=null) {return closest_enemy_ec;}
	        	relevant_robots = Info.enemy_ecs;
	        }
		}
		else {
        	if (closest_neutral_ec!=null) {return closest_neutral_ec;}
	        relevant_robots = Info.neutral_ecs;
		}
        RobotInfo closest_robot = null;
        int closest_distance = Integer.MAX_VALUE;
        for (RobotInfo robot:relevant_robots) {
        	int distance = Math2.length(Info.loc, robot.location);
        	if (closest_robot==null || distance<closest_distance) {
        		closest_robot = robot;
        		closest_distance = distance;
        	}
        }
        if (relevant_team==friendly) {
	        if (relevant_type==RobotType.POLITICIAN) {closest_friendly_politician = closest_robot;}
	        else if (relevant_type==RobotType.SLANDERER) {closest_friendly_slanderer = closest_robot;}
	        else if (relevant_type==RobotType.MUCKRAKER) {closest_friendly_muckraker = closest_robot;}
	        else {closest_friendly_ec = closest_robot;}
		}
		else if (relevant_team==enemy) {
	        if (relevant_type==RobotType.POLITICIAN) {closest_enemy_politician = closest_robot;}
	        else if (relevant_type==RobotType.SLANDERER) {closest_enemy_slanderer = closest_robot;}
	        else if (relevant_type==RobotType.MUCKRAKER) {closest_enemy_muckraker = closest_robot;}
	        else {closest_enemy_ec = closest_robot;}
		}
		else {closest_neutral_ec = closest_robot;}
        return closest_robot;
	}
	public static RobotInfo closest_marked_robot(RobotType relevant_type, boolean[] markings) {
		RobotInfo[] relevant_robots;
		if (relevant_type==RobotType.POLITICIAN) {relevant_robots = Info.friendly_politicians;}
		else if (relevant_type==RobotType.SLANDERER) {relevant_robots = Info.friendly_slanderers;}
		else if (relevant_type==RobotType.MUCKRAKER) {relevant_robots = Info.friendly_muckrakers;}
		else {relevant_robots = Info.friendly_ecs;}
		
        RobotInfo closest_robot = null;
        int closest_distance = Integer.MAX_VALUE;
        for (int i=0; i<relevant_robots.length; i++) {
            RobotInfo robot = relevant_robots[i];
        	int distance = Math2.length(Info.loc, robot.location);
        	if (closest_robot==null || distance<closest_distance && markings[i]) {
        		closest_robot = robot;
        		closest_distance = distance;
        	}
        }
        return closest_robot;
	}
}
