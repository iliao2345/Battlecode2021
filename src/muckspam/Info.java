package muckspam;
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
	public static int approx_home_dist = 0;  // starts from 127 and decreases to 0
	public static int approx_enemy_dist = 0;  // starts from 127 and decreases to 0  // muckrakers don't count
	public static double tile_cost;
	public static int passive_income;
	public static boolean see_farther_friendly_unit = false;  // true iff you can see a friendly unit which is farther from the enemy than self
	public static double empower_buff = 1;  // true iff you can see a friendly unit which is farther from the enemy than self
	public static double enemy_empower_buff = 1;  // true iff you can see a friendly unit which is farther from the enemy than self
	
	public static void initialize(RobotController rc) {
		Action.rc = rc;
		EnlightenmentCenter.rc = rc;
		Flag.rc = rc;
		Info.rc = rc;
		Muckraker.rc = rc;
		Pathing.rc = rc;
		Politician.rc = rc;
		Slanderer.rc = rc;
		Gas.rc = rc;
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
		if (sensable_robots.length>30) {sensable_robots = rc.senseNearbyRobots(17);}
		if (sensable_robots.length>30) {sensable_robots = rc.senseNearbyRobots(8);}
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
					n_friendly_politicians++;
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
		for (RobotInfo robot:sensable_robots) {
			if (robot.getTeam()==friendly) {
				switch (robot.getType()) {
				case POLITICIAN:
					friendly_politicians[n_friendly_politicians] = robot;
					n_friendly_politicians++;
					break;
				case SLANDERER:
					friendly_slanderers[n_friendly_slanderers] = robot;
					n_friendly_slanderers++;
					break;
				case MUCKRAKER:
					friendly_muckrakers[n_friendly_muckrakers] = robot;
					n_friendly_muckrakers++;
					break;
				case ENLIGHTENMENT_CENTER:
					friendly_ecs[n_friendly_ecs] = robot;
					n_friendly_ecs++;
					break;
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
		tile_cost = 1/rc.sensePassability(loc);
		approx_home_dist = Math.max(approx_home_dist-1, 0);
		approx_enemy_dist = Math.max(approx_enemy_dist-1, 0);
		see_farther_friendly_unit = false;
		for (RobotInfo robot:friendly_politicians) {
			int flag = rc.getFlag(robot.getID());
			int robot_enemy_dist = (flag>>Flag.LOG_MAX_APPROX_DIST_PLUS_ONE)%Flag.MAX_APPROX_DIST_PLUS_ONE;
			int robot_home_dist = (flag>>1)%Flag.MAX_APPROX_DIST_PLUS_ONE;
			int additional_cost = (int) (tile_cost*(Math2.length(loc, robot.location)+1));
			if (robot_home_dist-additional_cost>approx_home_dist) {approx_home_dist = robot_home_dist-additional_cost;}
			if (robot_enemy_dist-additional_cost>approx_enemy_dist) {approx_enemy_dist = robot_enemy_dist-additional_cost;}
			if (robot_enemy_dist<approx_enemy_dist) {see_farther_friendly_unit = true;}
		}
		for (RobotInfo robot:friendly_muckrakers) {
			int flag = rc.getFlag(robot.getID());
			int robot_enemy_dist = (flag>>Flag.LOG_MAX_APPROX_DIST_PLUS_ONE)%Flag.MAX_APPROX_DIST_PLUS_ONE;
			int robot_home_dist = (flag>>1)%Flag.MAX_APPROX_DIST_PLUS_ONE;
			int additional_cost = (int) (tile_cost*(Math2.length(loc, robot.location)+1));
			if (robot_home_dist-additional_cost>approx_home_dist) {approx_home_dist = robot_home_dist-additional_cost;}
			if (robot_enemy_dist-additional_cost>approx_enemy_dist) {approx_enemy_dist = robot_enemy_dist-additional_cost;}
			if (robot_enemy_dist<approx_enemy_dist) {see_farther_friendly_unit = true;}
		}
		closest_enemy_politician = closest_robot(enemy, RobotType.POLITICIAN);
		closest_enemy_slanderer = closest_robot(enemy, RobotType.SLANDERER);
		closest_enemy_ec = closest_robot(enemy, RobotType.ENLIGHTENMENT_CENTER);
		if (closest_enemy_politician!=null) {
			int additional_cost = (int) (tile_cost*Math2.length(loc, closest_enemy_politician.location));
			if (Flag.MAX_APPROX_DIST-additional_cost>approx_enemy_dist) {approx_enemy_dist = Flag.MAX_APPROX_DIST-additional_cost;}
		}
		if (closest_enemy_slanderer!=null) {
			int additional_cost = (int) (tile_cost*Math2.length(loc, closest_enemy_slanderer.location));
			if (Flag.MAX_APPROX_DIST-additional_cost>approx_enemy_dist) {approx_enemy_dist = Flag.MAX_APPROX_DIST-additional_cost;}
		}
		if (closest_enemy_ec!=null) {
			int additional_cost = (int) (tile_cost*Math2.length(loc, closest_enemy_ec.location));
			if (Flag.MAX_APPROX_DIST-additional_cost>approx_enemy_dist) {approx_enemy_dist = Flag.MAX_APPROX_DIST-additional_cost;}
		}
		closest_friendly_ec = closest_robot(friendly, RobotType.ENLIGHTENMENT_CENTER);
		closest_friendly_slanderer = closest_robot(friendly, RobotType.SLANDERER);
		if (closest_friendly_ec!=null) {
			int additional_cost = (int) (tile_cost*Math2.length(loc, closest_friendly_ec.location));
			if (Flag.MAX_APPROX_DIST-additional_cost>approx_home_dist) {approx_home_dist = Flag.MAX_APPROX_DIST-additional_cost;}
		}
		if (closest_friendly_slanderer!=null) {
			int additional_cost = (int) (tile_cost*Math2.length(loc, closest_friendly_slanderer.location));
			if (Flag.MAX_APPROX_DIST-additional_cost>approx_home_dist) {approx_home_dist = Flag.MAX_APPROX_DIST-additional_cost;}
		}
		passive_income = (int) Math.ceil(GameConstants.PASSIVE_INFLUENCE_RATIO_ENLIGHTENMENT_CENTER*Math.sqrt(round_num));
		empower_buff = rc.getEmpowerFactor(Info.friendly, 0);
		enemy_empower_buff = rc.getEmpowerFactor(Info.friendly, 0);
		
		// This should always be last no matter what is added above
		if (type==RobotType.ENLIGHTENMENT_CENTER) {
			ECInfo.update();
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
