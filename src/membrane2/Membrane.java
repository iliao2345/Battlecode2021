package membrane2;

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

public class Membrane {

	public static int BUFFER_SQUARED = 5;
	public static int BUFFER_MINUS_ONE_SQUARED = 1;
	public static int BUFFER_PLUS_ONE_SQUARED = 13;
	
	public static RobotController rc;
	public static boolean[][] impassable;  // [1][1] is undefined for all arrays
	public static int[][] ids;
	public static int[][] flags;
	public static boolean[][] friendly;
	public static boolean[][] is_membrane;
	public static int[][] layer_nums;
	public static boolean[][] interior;
	public static boolean[][] same_layer;
	public static boolean[][] exterior;
	public static int layer_num = 0;
	public static boolean bugsign = false;
	public static int push_signal = 0;
	public static boolean touching_membrane;
	public static boolean can_only_move_to_exterior;
	public static Direction passable_exterior_dir;
	public static boolean touching_enemy;
	public static int max_enemy_layer_num;
	
	public static void update() throws GameActionException {
		impassable = new boolean[3][3];
		ids = new int[3][3];
		flags = new int[3][3];
		friendly = new boolean[3][3];
		is_membrane = new boolean[3][3];
		layer_nums = new int[3][3];
		layer_num = 7;
		push_signal = 0;
		for (Direction dir:Math2.UNIT_DIRECTIONS) {
			int i = dir.dx+1;
			int j = dir.dy+1;
			RobotInfo robot = Info.adjacent_robots[i][j];
			if (robot!=null) {
				impassable[i][j] = true;
				int flag = rc.getFlag(robot.ID);
				if (robot.team==Info.friendly && (robot.type==RobotType.POLITICIAN || robot.type==RobotType.MUCKRAKER)) {
					friendly[i][j] = true;
					ids[i][j] = robot.ID;
					flags[i][j] = flag;
					if (flag>>23==1) {  // is_membrane
						is_membrane[i][j] = true;
						layer_nums[i][j] = (flag>>20)%8;
						if (layer_nums[i][j]+1<layer_num) {
							layer_num = layer_nums[i][j]+1;
						}
						int robot_push_signal = (flag>>17)%8;
						push_signal = Math.max(push_signal, robot_push_signal-1);
					}
				}
				if (robot.team==Info.friendly && (robot.type==RobotType.ENLIGHTENMENT_CENTER || (flag>>9)%2==1) && (i==1||j==1)) {  // (flag>>9)%2 is the slanderer tag
					layer_nums[i][j] = -3;
					layer_num = -2;
					is_membrane[i][j] = true;
					friendly[i][j] = true;
				}
			}
			if (!Info.on_map[i][j]) {
				impassable[i][j] = true;
				layer_nums[i][j] = layer_num;
				friendly[i][j] = true;
				is_membrane[i][j] = true;
			}
			for (RobotInfo robot2:Info.friendly_ecs) {
				if (robot2.location.isWithinDistanceSquared(Info.loc.add(dir), BUFFER_SQUARED)) {
					layer_nums[i][j] = Math.min(layer_nums[i][j], -1);
					layer_num = Math.min(layer_num, 0);
					is_membrane[i][j] = true;
					friendly[i][j] = true;
				}
				if (robot2.location.isWithinDistanceSquared(Info.loc.add(dir), BUFFER_MINUS_ONE_SQUARED)) {
					layer_nums[i][j] = Math.min(layer_nums[i][j], -2);
					layer_num = Math.min(layer_num, -1);
				}
			}
			for (RobotInfo robot2:Info.friendly_slanderers) {
				if (robot2.location.isWithinDistanceSquared(Info.loc.add(dir), BUFFER_SQUARED)) {
					layer_nums[i][j] = Math.min(layer_nums[i][j], -1);
					layer_num = Math.min(layer_num, 0);
					is_membrane[i][j] = true;
					friendly[i][j] = true;
				}
				if (robot2.location.isWithinDistanceSquared(Info.loc.add(dir), BUFFER_MINUS_ONE_SQUARED)) {
					layer_nums[i][j] = Math.min(layer_nums[i][j], -2);
					layer_num = Math.min(layer_num, -1);
				}
			}
		}
		interior = new boolean[3][3];
		same_layer = new boolean[3][3];
		exterior = new boolean[3][3];
		boolean[][] interior_plus_layer = new boolean[3][3];
		touching_membrane = false;
		for (Direction dir:Math2.UNIT_DIRECTIONS) {
			if (is_membrane[dir.dx+1][dir.dy+1] && layer_nums[dir.dx+1][dir.dy+1]<=layer_num-1) {
				interior[dir.dx+1][dir.dy+1] = true;
				touching_membrane = true;
			}
		}
		if (touching_membrane) {
			interior_plus_layer[0][0] = interior[0][0] || interior[1][0] || interior[0][1];
			interior_plus_layer[2][0] = interior[2][0] || interior[2][1] || interior[1][0];
			interior_plus_layer[0][2] = interior[0][2] || interior[1][2] || interior[0][1];
			interior_plus_layer[2][2] = interior[2][2] || interior[1][2] || interior[2][1];
			interior_plus_layer[1][0] = interior[1][0] || interior[0][0] || interior[2][0] || interior[2][1] || interior[0][1];
			interior_plus_layer[1][2] = interior[1][2] || interior[0][2] || interior[2][2] || interior[2][1] || interior[0][1];
			interior_plus_layer[0][1] = interior[0][1] || interior[0][0] || interior[0][2] || interior[1][0] || interior[1][2];
			interior_plus_layer[2][1] = interior[2][1] || interior[2][0] || interior[2][2] || interior[1][0] || interior[1][2];
		}
		for (Direction dir:Math2.UNIT_DIRECTIONS) {
			same_layer[dir.dx+1][dir.dy+1] = interior_plus_layer[dir.dx+1][dir.dy+1] && !interior[dir.dx+1][dir.dy+1];
			exterior[dir.dx+1][dir.dy+1] = !interior_plus_layer[dir.dx+1][dir.dy+1];
		}
		can_only_move_to_exterior = true;
		passable_exterior_dir = null;
		for (Direction dir:Math2.UNIT_DIRECTIONS) {
			if (!impassable[dir.dx+1][dir.dy+1] && (interior[dir.dx+1][dir.dy+1] || same_layer[dir.dx+1][dir.dy+1])) {
				can_only_move_to_exterior = false;
			}
			if (!impassable[dir.dx+1][dir.dy+1] && exterior[dir.dx+1][dir.dy+1]) {
				passable_exterior_dir = dir;
			}
			if (exterior[dir.dx+1][dir.dy+1]) {
				rc.setIndicatorDot(Info.loc.add(dir), 255, 255, 255);
			}
			if (same_layer[dir.dx+1][dir.dy+1]) {
				rc.setIndicatorDot(Info.loc.add(dir), 128, 128, 128);
			}
			if (interior[dir.dx+1][dir.dy+1]) {
				rc.setIndicatorDot(Info.loc.add(dir), 0, 0, 0);
			}
		}
		if (passable_exterior_dir==null) {can_only_move_to_exterior = false;}
		if (can_only_move_to_exterior && layer_num<0) {push_signal = Math.min(push_signal+3, 7);}
//		if ((layer_num+4)%4==0) {rc.setIndicatorDot(Info.loc, 255, 0, 0);}
//		if ((layer_num+4)%4==1) {rc.setIndicatorDot(Info.loc, 255, 255, 0);}
//		if ((layer_num+4)%4==2) {rc.setIndicatorDot(Info.loc, 0, 255, 0);}
//		if ((layer_num+4)%4==3) {rc.setIndicatorDot(Info.loc, 0, 0, 255);}
		touching_enemy = false;
		max_enemy_layer_num = Integer.MIN_VALUE;
		for (Direction dir:Math2.UNIT_DIRECTIONS) {
			int i = dir.dx+1;
			int j = dir.dy+1;
			RobotInfo robot = Info.adjacent_robots[i][j];
			if (robot!=null) {
				if (robot.team==Info.enemy) {
					touching_enemy = true;
					if (interior[i][j]) {max_enemy_layer_num = Math.max(max_enemy_layer_num, layer_num-1);}
					if (same_layer[i][j]) {max_enemy_layer_num = Math.max(max_enemy_layer_num, layer_num);}
					if (exterior[i][j]) {max_enemy_layer_num = Math.max(max_enemy_layer_num, layer_num+1);}
				}
			}
		}
	}
	public static void advance() throws GameActionException {
		if (!touching_membrane) {
			Phase.evaporate();
		}
		else if (can_only_move_to_exterior && !touching_enemy) {
			Action.move(passable_exterior_dir);
		}
		else {
			heal();
		}
	}
	// Heal through exterior liquefication and solidification at damage site, and removal of muckrakers
	public static void heal() throws GameActionException {
		if (!touching_membrane) {
			Phase.evaporate();
			return;
		}
		if (Info.type==RobotType.POLITICIAN && touching_enemy) {
			boolean enemy_inside = false;
			int distance_squared = 1;
			for (Direction dir:Math2.UNIT_DIRECTIONS) {
				if (layer_num==0 && exterior[dir.dx+1][dir.dy+1]) {continue;}
				if (layer_num>0 && (exterior[dir.dx+1][dir.dy+1] || same_layer[dir.dx+1][dir.dy+1])) {continue;}
				RobotInfo adjacent_robot = Info.adjacent_robots[dir.dx+1][dir.dy+1];
				if (adjacent_robot!=null) {
					if (adjacent_robot.team==Info.enemy) {
						System.out.println(layer_nums[dir.dx+1][dir.dy+1]);
						enemy_inside = true;
						distance_squared = Math.max(distance_squared, dir.dx*dir.dx+dir.dy*dir.dy);
					}
				}
			}
			if (enemy_inside) {
				rc.empower(distance_squared);
			}
		}
		if (layer_num<0 && passable_exterior_dir!=null) {
			Action.move(passable_exterior_dir);
			return;
		}
		Direction interior_dir = null;
		for (Direction dir:Math2.UNIT_DIRECTIONS) {
			if (interior[dir.dx+1][dir.dy+1]) {
				if (!impassable[dir.dx+1][dir.dy+1] && layer_nums[dir.dx+1][dir.dy+1]>=0) {
					layer_num -= 1;
					bugsign = Math.random()<0.5;
					Action.move(dir);
					return;
				}
				interior_dir = dir;
			}
		}
		if (touching_enemy && max_enemy_layer_num>layer_num) {return;}  // don't move if an enemy further away can immediately take your spot
		Direction left = Math2.cardinalize_left(left_to_non_interior(interior_dir));
		Direction right = Math2.cardinalize_right(right_to_non_interior(interior_dir));
		RobotInfo closest_enemy_muckraker = Info.closest_robot(Info.enemy, RobotType.MUCKRAKER);
		if (closest_enemy_muckraker!=null) {
			bugsign = Info.loc.add(left).distanceSquaredTo(closest_enemy_muckraker.location)<Info.loc.add(right).distanceSquaredTo(closest_enemy_muckraker.location);
		}
//		rc.setIndicatorLine(Info.loc, Info.loc.add(left), 255, 0, 0);
//		rc.setIndicatorLine(Info.loc, Info.loc.add(right), 0, 0, 255);
		if (bugsign && rc.canMove(left)) {
			Action.move(left);
		}
		else if (rc.canMove(right)) {
			Action.move(right);
		}
		else if (rc.canMove(left)) {
			Action.move(left);
		}
	}
	public static Direction left_to_interior(Direction dir) {
		if (interior[dir.dx+1][dir.dy+1]) {return dir;}
		dir = dir.rotateLeft();
		if (interior[dir.dx+1][dir.dy+1]) {return dir;}
		dir = dir.rotateLeft();
		if (interior[dir.dx+1][dir.dy+1]) {return dir;}
		dir = dir.rotateLeft();
		if (interior[dir.dx+1][dir.dy+1]) {return dir;}
		dir = dir.rotateLeft();
		if (interior[dir.dx+1][dir.dy+1]) {return dir;}
		dir = dir.rotateLeft();
		if (interior[dir.dx+1][dir.dy+1]) {return dir;}
		dir = dir.rotateLeft();
		if (interior[dir.dx+1][dir.dy+1]) {return dir;}
		dir = dir.rotateLeft();
		return dir;
	}
	public static Direction right_to_interior(Direction dir) {
		if (interior[dir.dx+1][dir.dy+1]) {return dir;}
		dir = dir.rotateRight();
		if (interior[dir.dx+1][dir.dy+1]) {return dir;}
		dir = dir.rotateRight();
		if (interior[dir.dx+1][dir.dy+1]) {return dir;}
		dir = dir.rotateRight();
		if (interior[dir.dx+1][dir.dy+1]) {return dir;}
		dir = dir.rotateRight();
		if (interior[dir.dx+1][dir.dy+1]) {return dir;}
		dir = dir.rotateRight();
		if (interior[dir.dx+1][dir.dy+1]) {return dir;}
		dir = dir.rotateRight();
		if (interior[dir.dx+1][dir.dy+1]) {return dir;}
		dir = dir.rotateRight();
		return dir;
	}
	public static Direction left_to_non_interior(Direction dir) {
		if (!interior[dir.dx+1][dir.dy+1]) {return dir;}
		dir = dir.rotateLeft();
		if (!interior[dir.dx+1][dir.dy+1]) {return dir;}
		dir = dir.rotateLeft();
		if (!interior[dir.dx+1][dir.dy+1]) {return dir;}
		dir = dir.rotateLeft();
		if (!interior[dir.dx+1][dir.dy+1]) {return dir;}
		dir = dir.rotateLeft();
		if (!interior[dir.dx+1][dir.dy+1]) {return dir;}
		dir = dir.rotateLeft();
		if (!interior[dir.dx+1][dir.dy+1]) {return dir;}
		dir = dir.rotateLeft();
		if (!interior[dir.dx+1][dir.dy+1]) {return dir;}
		dir = dir.rotateLeft();
		return dir;
	}
	public static Direction right_to_non_interior(Direction dir) {
		if (!interior[dir.dx+1][dir.dy+1]) {return dir;}
		dir = dir.rotateRight();
		if (!interior[dir.dx+1][dir.dy+1]) {return dir;}
		dir = dir.rotateRight();
		if (!interior[dir.dx+1][dir.dy+1]) {return dir;}
		dir = dir.rotateRight();
		if (!interior[dir.dx+1][dir.dy+1]) {return dir;}
		dir = dir.rotateRight();
		if (!interior[dir.dx+1][dir.dy+1]) {return dir;}
		dir = dir.rotateRight();
		if (!interior[dir.dx+1][dir.dy+1]) {return dir;}
		dir = dir.rotateRight();
		if (!interior[dir.dx+1][dir.dy+1]) {return dir;}
		dir = dir.rotateRight();
		return dir;
	}
}
