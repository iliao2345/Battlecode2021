package membrane;

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
	
	public static RobotController rc;
	public static boolean[][] empty;  // [1][1] is undefined for all arrays
	public static int[][] ids;
	public static int[][] flags;
	public static boolean[][] friendly;
	public static boolean[][] is_membrane;
	public static int[][] layer_nums;
	public static Direction[][] lefts;
	public static Direction[][] rights;
	public static Direction left = Direction.EAST;
	public static Direction right = Direction.WEST;
	public static int layer_num = 0;
	public static boolean left_detached = false;
	public static boolean right_detached = false;
	public static boolean punctured = false;
	public static boolean last_moved_left = false;
	public static Direction next_layer_direction = null;
	
	public static void update() throws GameActionException {
		empty = new boolean[3][3];
		ids = new int[3][3];
		flags = new int[3][3];
		friendly = new boolean[3][3];
		is_membrane = new boolean[3][3];
		layer_nums = new int[3][3];
		lefts = new Direction[3][3];
		rights = new Direction[3][3];
		for (Direction dir:Math2.UNIT_DIRECTIONS) {
			int i = dir.dx+1;
			int j = dir.dy+1;
			RobotInfo robot = Info.adjacent_robots[i][j];
			if (robot!=null) {
				if (robot.team==Info.friendly) {
					friendly[i][j] = true;
					ids[i][j] = robot.ID;
					int flag = rc.getFlag(robot.ID);
					flags[i][j] = flag;
					if (flag>>23==1) {  // is_membrane
						is_membrane[i][j] = true;
						lefts[i][j] = Direction.cardinalDirections()[(flag>>21)%4];
						rights[i][j] = Direction.cardinalDirections()[(flag>>19)%4];
						layer_nums[i][j] = (flag>>17)%4;
					}
				}
			}
			else {
				empty[i][j] = true;
			}
		}
		int original_layer_num = layer_num;
		int prev_layer_num = (layer_num+3)%4;
		int next_layer_num = (layer_num+1)%4;
		next_layer_direction = null;  // find the next layer
		Direction test_direction = left.rotateRight().rotateRight();  // find the next layer
		for (int i=0; i<8; i++) {
			if (is_membrane[test_direction.dx+1][test_direction.dy+1] && layer_nums[test_direction.dx+1][test_direction.dy+1]==next_layer_num
					&& (flags[test_direction.dx+1][test_direction.dy+1]>>15)%4!=3) {
				next_layer_direction = test_direction;
				break;
			}
			test_direction = test_direction.rotateLeft();
		}
		punctured = false;
		left = right.rotateLeft().rotateLeft();  // find the most exterior fork of next layer trying to connect
		if (layer_nums[left.dx+1][left.dy+1]!=next_layer_num || rights[left.dx+1][left.dy+1]!=left.opposite()) {left = left.rotateLeft().rotateLeft();}
		if (layer_nums[left.dx+1][left.dy+1]!=next_layer_num || rights[left.dx+1][left.dy+1]!=left.opposite()) {left = left.rotateLeft().rotateLeft();}
		if (layer_nums[left.dx+1][left.dy+1]!=next_layer_num || rights[left.dx+1][left.dy+1]!=left.opposite()) {  // found no permission to connect to next layer
			left = right.rotateLeft().rotateLeft();  // find the most exterior fork of current layer trying to connect
			if (layer_nums[left.dx+1][left.dy+1]!=original_layer_num || rights[left.dx+1][left.dy+1]!=left.opposite()) {left = left.rotateLeft().rotateLeft();}
			if (layer_nums[left.dx+1][left.dy+1]!=original_layer_num || rights[left.dx+1][left.dy+1]!=left.opposite()) {left = left.rotateLeft().rotateLeft();}
			if (layer_nums[left.dx+1][left.dy+1]!=original_layer_num || rights[left.dx+1][left.dy+1]!=left.opposite()) {  // found no permission to connect to current layer
				if (next_layer_direction==null) {  // cannot see next layer; allowed to connect to the previous layer
					left = right.rotateLeft().rotateLeft();
					if (layer_nums[left.dx+1][left.dy+1]!=prev_layer_num || !is_membrane[left.dx+1][left.dy+1]) {left = left.rotateLeft().rotateLeft();}
					if (layer_nums[left.dx+1][left.dy+1]!=prev_layer_num || !is_membrane[left.dx+1][left.dy+1]) {left = left.rotateLeft().rotateLeft();}
					if (layer_nums[left.dx+1][left.dy+1]!=prev_layer_num || !is_membrane[left.dx+1][left.dy+1]) {  // found no connection to steal from previous layer
						boolean special_case = is_membrane[right.dx+1][right.dy+1] && lefts[right.dx+1][right.dy+1]!=right.opposite();  // right is connected to something else
						left = right.rotateLeft().rotateLeft();  // find most exterior fork of current layer regardless of permission
						if (!is_membrane[left.dx+1][left.dy+1] || layer_nums[left.dx+1][left.dy+1]!=original_layer_num) {left = left.rotateLeft().rotateLeft();}
						if (!is_membrane[left.dx+1][left.dy+1] || layer_nums[left.dx+1][left.dy+1]!=original_layer_num) {left = left.rotateLeft().rotateLeft();}
						if ((!is_membrane[left.dx+1][left.dy+1] || layer_nums[left.dx+1][left.dy+1]!=original_layer_num) && !special_case) {  // not touching anything
							punctured = true;
							left_detached = true;
							left = null;
	//						int nearest_distance = Integer.MAX_VALUE;
	//						for (RobotInfo robot:Info.friendly_muckrakers) {  // look for other end of damaged membrane to build towards
	//							int flag = rc.getFlag(robot.ID);
	//							if (flag>>23==1 && (flag>>15)%2==1 && (flag>>17)%4==layer_num && (left==null || Info.loc.distanceSquaredTo(robot.location)<nearest_distance)) {  // found the other end
	//								left = Info.loc.directionTo(robot.location);
	//								nearest_distance = Info.loc.distanceSquaredTo(robot.location);
	//							}
	//						}
	//						for (RobotInfo robot:Info.friendly_politicians) {  // look for other end of damaged membrane to build towards
	//							int flag = rc.getFlag(robot.ID);
	//							if (flag>>23==1 && (flag>>15)%2==1 && (flag>>17)%4==layer_num && (left==null || Info.loc.distanceSquaredTo(robot.location)<nearest_distance)) {  // found the other end
	//								left = Info.loc.directionTo(robot.location);
	//								nearest_distance = Info.loc.distanceSquaredTo(robot.location);
	//							}
	//						}
							if (left==null) {  // cannot see other end of damaged membrane, try to build straight
								if (rights[right.dx+1][right.dy+1]!=null) {left = rights[right.dx+1][right.dy+1].opposite();}  // straighten puncture
								else {left = right.opposite();}
								if (!empty[left.dx+1][left.dy+1] && !friendly[left.dx+1][left.dy+1] && left.rotateLeft().rotateLeft()!=right) {left = left.rotateLeft().rotateLeft();}
								if (!empty[left.dx+1][left.dy+1] && !friendly[left.dx+1][left.dy+1] && left.rotateLeft().rotateLeft()!=right) {left = left.rotateLeft().rotateLeft();}
							}
							else if (left.dx*left.dx+left.dy*left.dy==2) {  // other end is diagonal, curl inwards
								left = left.rotateLeft();
							}
						}
						else if (special_case) {
							left_detached = true;
							left = right.rotateLeft().rotateLeft();
							layer_num = prev_layer_num;
						}
						else {  // found connection in current membrane without permission
							Direction test_left = left.rotateRight();
							if (is_membrane[test_left.dx+1][test_left.dy+1] && layer_nums[test_left.dx+1][test_left.dy+1]==original_layer_num && test_left!=right) {test_left = test_left.rotateRight();}
							if (is_membrane[test_left.dx+1][test_left.dy+1] && layer_nums[test_left.dx+1][test_left.dy+1]==original_layer_num && test_left!=right) {test_left = test_left.rotateRight();}
							if (is_membrane[test_left.dx+1][test_left.dy+1] && layer_nums[test_left.dx+1][test_left.dy+1]==original_layer_num && test_left!=right) {test_left = test_left.rotateRight();}
							if (is_membrane[test_left.dx+1][test_left.dy+1] && layer_nums[test_left.dx+1][test_left.dy+1]==original_layer_num && test_left!=right) {test_left = test_left.rotateRight();}
							if (is_membrane[test_left.dx+1][test_left.dy+1] && layer_nums[test_left.dx+1][test_left.dy+1]==original_layer_num && test_left!=right) {test_left = test_left.rotateRight();}
							if (is_membrane[test_left.dx+1][test_left.dy+1] && layer_nums[test_left.dx+1][test_left.dy+1]==original_layer_num && test_left!=right) {test_left = test_left.rotateRight();}
							if (test_left==right) {  // not bordering outside
								if (is_membrane[left.dx+1][left.dy+1] && layer_nums[left.dx+1][left.dy+1]==original_layer_num && left.rotateLeft().rotateLeft()!=right) {left = left.rotateLeft().rotateLeft();}
								if (is_membrane[left.dx+1][left.dy+1] && layer_nums[left.dx+1][left.dy+1]==original_layer_num && left.rotateLeft().rotateLeft()!=right) {left = left.rotateLeft().rotateLeft();}
								left_detached = true;
								layer_num = prev_layer_num;
							}
							else {  // bordering outside
								left_detached = false;
							}
						}
					}
					else {  // found connection in previous membrane
						left_detached = false;
					}
				}
				else {  // too close to the next layer to connect to potential previous layer
					rc.setIndicatorLine(Info.loc, Info.loc.add(next_layer_direction), 0, 255, 0);
					left = next_layer_direction.rotateLeft().rotateLeft();  // point along next layer's edge
					if (next_layer_direction.dx*next_layer_direction.dx+next_layer_direction.dy*next_layer_direction.dy==2) {  // next layer is diagonally situated
						left = next_layer_direction.rotateLeft();
					}
					if (is_membrane[left.dx+1][left.dy+1] && layer_nums[left.dx+1][left.dy+1]==next_layer_num) {left = left.rotateLeft().rotateLeft();}
					if (is_membrane[left.dx+1][left.dy+1] && layer_nums[left.dx+1][left.dy+1]==next_layer_num) {left = left.rotateLeft().rotateLeft();}
					if (is_membrane[left.dx+1][left.dy+1] && layer_nums[left.dx+1][left.dy+1]==next_layer_num) {left = left.rotateLeft().rotateLeft();}
					if (left==right) {left = left.rotateRight().rotateRight();}
					if (is_membrane[left.dx+1][left.dy+1] && (layer_nums[left.dx+1][left.dy+1]==original_layer_num || layer_nums[left.dx+1][left.dy+1]==prev_layer_num)) {  // next point along next layer's edge is in current or previous layer
						left_detached = false;
					}
					else {  // next point along next layer's edge is empty or enemy
						left_detached = true;
					}
				}
			}
			else {  // found permission to connect to current layer
				left_detached = false;
			}
		}
		else {  // found permission to connect to next layer
			left_detached = false;
			layer_num = next_layer_num;
		}
		right = left.rotateRight().rotateRight();  // find the most exterior fork of next layer trying to connect
		if (layer_nums[right.dx+1][right.dy+1]!=next_layer_num || lefts[right.dx+1][right.dy+1]!=right.opposite()) {right = right.rotateRight().rotateRight();}
		if (layer_nums[right.dx+1][right.dy+1]!=next_layer_num || lefts[right.dx+1][right.dy+1]!=right.opposite()) {right = right.rotateRight().rotateRight();}
		if (layer_nums[right.dx+1][right.dy+1]!=next_layer_num || lefts[right.dx+1][right.dy+1]!=right.opposite()) {  // found no permission to connect to next layer
			rc.setIndicatorLine(Info.loc, Info.loc.translate(-30, 4), 0,0,0);
			right = left.rotateRight().rotateRight();  // find the most exterior fork of current layer trying to connect
			if (layer_nums[right.dx+1][right.dy+1]!=original_layer_num || lefts[right.dx+1][right.dy+1]!=right.opposite()) {right = right.rotateRight().rotateRight();}
			if (layer_nums[right.dx+1][right.dy+1]!=original_layer_num || lefts[right.dx+1][right.dy+1]!=right.opposite()) {right = right.rotateRight().rotateRight();}
			if (layer_nums[right.dx+1][right.dy+1]!=original_layer_num || lefts[right.dx+1][right.dy+1]!=right.opposite()) {  // found no permission to connect to current layer
				rc.setIndicatorLine(Info.loc, Info.loc.translate(-30, 3), 0,0,0);
				if (next_layer_direction==null) {  // cannot see next layer; allowed to connect to the previous layer
					rc.setIndicatorLine(Info.loc, Info.loc.translate(-30, 2), 0,0,0);
					right = left.rotateRight().rotateRight();
					if (layer_nums[right.dx+1][right.dy+1]!=prev_layer_num || !is_membrane[right.dx+1][right.dy+1]) {right = right.rotateRight().rotateRight();}
					if (layer_nums[right.dx+1][right.dy+1]!=prev_layer_num || !is_membrane[right.dx+1][right.dy+1]) {right = right.rotateRight().rotateRight();}
					if (layer_nums[right.dx+1][right.dy+1]!=prev_layer_num || !is_membrane[right.dx+1][right.dy+1]) {  // found no connection to steal from previous layer
						rc.setIndicatorLine(Info.loc, Info.loc.translate(-30, 1), 0,0,0);
						boolean special_case = is_membrane[left.dx+1][left.dy+1] && rights[left.dx+1][left.dy+1]!=left.opposite();  // left is connected to something else
						right = left.rotateRight().rotateRight();  // find most exterior fork of current layer regardless of permission
						if (!is_membrane[right.dx+1][right.dy+1] || layer_nums[right.dx+1][right.dy+1]!=original_layer_num) {right = right.rotateRight().rotateRight();}
						if (!is_membrane[right.dx+1][right.dy+1] || layer_nums[right.dx+1][right.dy+1]!=original_layer_num) {right = right.rotateRight().rotateRight();}
						if ((!is_membrane[right.dx+1][right.dy+1] || layer_nums[right.dx+1][right.dy+1]!=original_layer_num) && !special_case) {  // not touching anything
							rc.setIndicatorLine(Info.loc, Info.loc.translate(-30, 0), 0,0,0);
							punctured = true;
							right_detached = true;
							right = null;
							//						int nearest_distance = Integer.MAX_VALUE;
							//						for (RobotInfo robot:Info.friendly_muckrakers) {  // look for other end of damaged membrane to build towards
							//							int flag = rc.getFlag(robot.ID);
							//							if (flag>>23==1 && (flag>>16)%2==1 && (flag>>17)%4==layer_num && (right==null || Info.loc.distanceSquaredTo(robot.location)<nearest_distance)) {  // found the other end
							//								right = Info.loc.directionTo(robot.location);
							//								nearest_distance = Info.loc.distanceSquaredTo(robot.location);
							//							}
							//						}
							//						for (RobotInfo robot:Info.friendly_politicians) {  // look for other end of damaged membrane to build towards
							//							int flag = rc.getFlag(robot.ID);
							//							if (flag>>23==1 && (flag>>16)%2==1 && (flag>>17)%4==layer_num && (right==null || Info.loc.distanceSquaredTo(robot.location)<nearest_distance)) {  // found the other end
							//								right = Info.loc.directionTo(robot.location);
							//								nearest_distance = Info.loc.distanceSquaredTo(robot.location);
							//							}
							//						}
							if (right==null) {  // cannot see other end of damaged membrane, try to build straight
								if (lefts[left.dx+1][left.dy+1]!=null) {right = lefts[left.dx+1][left.dy+1].opposite();}  // straighten puncture
								else {right = left.opposite();}
								if (!empty[right.dx+1][right.dy+1] && !friendly[right.dx+1][right.dy+1] && right.rotateRight().rotateRight()!=left) {right = right.rotateRight().rotateRight();}
								if (!empty[right.dx+1][right.dy+1] && !friendly[right.dx+1][right.dy+1] && right.rotateRight().rotateRight()!=left) {right = right.rotateRight().rotateRight();}
							}
							else if (right.dx*right.dx+right.dy*right.dy==2) {  // other end is diagonal, curl inwards
								right = right.rotateLeft();
							}
						}
						else if (special_case) {
							right_detached = true;
							right = left.rotateRight().rotateRight();
							layer_num = prev_layer_num;
						}
						else {  // found connection in current membrane without permission
							Direction test_right = left.rotateLeft();
							if (is_membrane[test_right.dx+1][test_right.dy+1] && layer_nums[test_right.dx+1][test_right.dy+1]==original_layer_num && test_right!=left) {test_right = test_right.rotateLeft();}
							if (is_membrane[test_right.dx+1][test_right.dy+1] && layer_nums[test_right.dx+1][test_right.dy+1]==original_layer_num && test_right!=left) {test_right = test_right.rotateLeft();}
							if (is_membrane[test_right.dx+1][test_right.dy+1] && layer_nums[test_right.dx+1][test_right.dy+1]==original_layer_num && test_right!=left) {test_right = test_right.rotateLeft();}
							if (is_membrane[test_right.dx+1][test_right.dy+1] && layer_nums[test_right.dx+1][test_right.dy+1]==original_layer_num && test_right!=left) {test_right = test_right.rotateLeft();}
							if (is_membrane[test_right.dx+1][test_right.dy+1] && layer_nums[test_right.dx+1][test_right.dy+1]==original_layer_num && test_right!=left) {test_right = test_right.rotateLeft();}
							if (is_membrane[test_right.dx+1][test_right.dy+1] && layer_nums[test_right.dx+1][test_right.dy+1]==original_layer_num && test_right!=left) {test_right = test_right.rotateLeft();}
							if (test_right==left) {  // not bordering outside
								if (is_membrane[right.dx+1][right.dy+1] && layer_nums[right.dx+1][right.dy+1]==original_layer_num && right.rotateRight().rotateRight()!=left) {right = right.rotateRight().rotateRight();}
								if (is_membrane[right.dx+1][right.dy+1] && layer_nums[right.dx+1][right.dy+1]==original_layer_num && right.rotateRight().rotateRight()!=left) {right = right.rotateRight().rotateRight();}
								if (left==right) {right = right.rotateLeft().rotateLeft();}
								right_detached = true;
								layer_num = prev_layer_num;
							}
							else {  // bordering outside
								right_detached = false;
							}
						}
					}
					else {  // found connection in previous membrane
						right_detached = false;
					}
				}
				else {  // too close to the next layer to connect to potential previous layer
					rc.setIndicatorLine(Info.loc, Info.loc.add(next_layer_direction).add(next_layer_direction), 0, 0, 0);
					right = next_layer_direction.rotateRight().rotateRight();  // point along next layer's edge
					if (next_layer_direction.dx*next_layer_direction.dx+next_layer_direction.dy*next_layer_direction.dy==2) {  // next layer is diagonally situated
						right = next_layer_direction.rotateRight();
					}
					if (is_membrane[right.dx+1][right.dy+1] && layer_nums[right.dx+1][right.dy+1]==next_layer_num) {right = right.rotateRight().rotateRight();}
					if (is_membrane[right.dx+1][right.dy+1] && layer_nums[right.dx+1][right.dy+1]==next_layer_num) {right = right.rotateRight().rotateRight();}
					if (is_membrane[right.dx+1][right.dy+1] && layer_nums[right.dx+1][right.dy+1]==next_layer_num) {right = right.rotateRight().rotateRight();}
					if (left==right) {right = right.rotateLeft().rotateLeft();}
					if (is_membrane[right.dx+1][right.dy+1] && (layer_nums[right.dx+1][right.dy+1]==original_layer_num || layer_nums[right.dx+1][right.dy+1]==prev_layer_num)) {  // next point along next layer's edge is in current or previous layer
						right_detached = false;
					}
					else {  // next point along next layer's edge is empty or enemy
						right_detached = true;
					}
				}
			}
			else {  // found permission to connect to current layer
				right_detached = false;
			}
		}
		else {  // found permission to connect to next layer
			right_detached = false;
			layer_num = next_layer_num;
		}
		rc.setIndicatorLine(Info.loc, Info.loc.add(left), 255, 0, 0);
		rc.setIndicatorLine(Info.loc, Info.loc.add(right), 0, 0, 255);
		if (layer_num==0) {rc.setIndicatorDot(Info.loc, 255, 0, 0);}
		if (layer_num==1) {rc.setIndicatorDot(Info.loc, 255, 255, 0);}
		if (layer_num==2) {rc.setIndicatorDot(Info.loc, 0, 255, 0);}
		if (layer_num==3) {rc.setIndicatorDot(Info.loc, 0, 0, 255);}
	}
	public static void advance() throws GameActionException {
		if (right.rotateLeft().rotateLeft()==left) {
			if (rc.canMove(right.rotateLeft())) {
				Action.move(right.rotateLeft());
				left = left.rotateLeft().rotateLeft();
				right = right.rotateRight().rotateRight();
			}
		}
	}
	// Heal through interior liquefication and solidification at damage site.
	public static void heal() throws GameActionException {
		if (!Info.touching_membrane && rc.onTheMap(Info.loc.add(Direction.EAST))) {
			Phase.evaporate();
			return;
		}
		int prev_layer_num = (layer_num+3)%4;
		int next_layer_num = (layer_num+1)%4;
		if (punctured && (left_detached ^ right_detached)) {  // shift the puncture to try to narrow it
			if (left==right.opposite()) {return;}
			Direction move_direction = Info.loc.directionTo(Info.loc.add(left).add(right));
			if (rc.canMove(move_direction)) {
				left = Info.loc.directionTo(Info.loc.add(left).add(move_direction.opposite()));
				right = Info.loc.directionTo(Info.loc.add(right).add(move_direction.opposite()));
				Action.move(move_direction);
			}
			return;
		}
		if (next_layer_direction==null) {
			return;
		}
		Direction move_direction = null;
		if (left_detached && right_detached) {
			if (last_moved_left && rc.canMove(left)) {
				move_direction = left;
				last_moved_left = true;
			}
			else if (rc.canMove(right)) {
				move_direction = right;
				last_moved_left = false;
			}
			else if (rc.canMove(left)) {
				move_direction = left;
				last_moved_left = true;
			}
		}
		else if (left_detached && !right_detached) {
			move_direction = left;
			last_moved_left = true;
		}
		else if (right_detached && !left_detached) {
			move_direction = right;
			last_moved_left = false;
		}
		if (move_direction == null) {return;}
		if (!rc.canMove(move_direction)) {return;}
		Direction back_left = move_direction.opposite().rotateRight();
		Direction back_right = move_direction.opposite().rotateLeft();
		Direction back = move_direction.opposite();
		if (move_direction == left) {  // move left
			Direction old_left = left;
			right = left.opposite();
			left = right.rotateLeft().rotateLeft();
			if (!rc.onTheMap(Info.loc.add(old_left).add(left))) {left = left.rotateLeft().rotateLeft();}
			else {
				RobotInfo robot = rc.senseRobotAtLocation(Info.loc.add(old_left).add(left));  // get new left direction after moving
				if (robot!=null) {
					if (robot.team==Info.friendly) {
						int flag = rc.getFlag(robot.ID);
						if (flag>>23==1 && (flag>>17)%4==next_layer_num) {  // is_membrane and robot's layer_num is next_layer_num
							left = left.rotateLeft().rotateLeft();
						}
					}
				}
			}
			if (!rc.onTheMap(Info.loc.add(old_left).add(left))) {left = left.rotateLeft().rotateLeft();}
			else {
				RobotInfo robot = rc.senseRobotAtLocation(Info.loc.add(old_left).add(left));  // get new left direction after moving
				if (robot!=null) {
					if (robot.team==Info.friendly) {
						int flag = rc.getFlag(robot.ID);
						if (flag>>23==1 && (flag>>17)%4==next_layer_num) {  // is_membrane and robot's layer_num is next_layer_num
							left = left.rotateLeft().rotateLeft();
						}
					}
				}
			}
			if (lefts[old_left.rotateLeft().dx+1][old_left.rotateLeft().dy+1]==old_left.rotateRight().rotateRight()
					&& layer_nums[old_left.rotateLeft().dx+1][old_left.rotateLeft().dy+1]==next_layer_num) {  // attach to layer
				right = old_left.rotateLeft().rotateLeft();
				if (left==right) {left = right.opposite();}
				layer_num = layer_nums[old_left.rotateLeft().dx+1][old_left.rotateLeft().dy+1];
			}
			if (lefts[old_left.rotateRight().dx+1][old_left.rotateRight().dy+1]==old_left.rotateLeft().rotateLeft()
					&& layer_nums[old_left.rotateRight().dx+1][old_left.rotateRight().dy+1]==next_layer_num) {
				right = old_left.rotateRight().rotateRight();
				if (left==right) {left = right.opposite();}
				layer_num = layer_nums[old_left.rotateRight().dx+1][old_left.rotateRight().dy+1];
			}
			if (rights[old_left.rotateLeft().dx+1][old_left.rotateLeft().dy+1]==old_left.rotateRight().rotateRight()
					&& layer_nums[old_left.rotateLeft().dx+1][old_left.rotateLeft().dy+1]==next_layer_num) {
				left = old_left.rotateLeft().rotateLeft();
				if (left==right) {right = left.opposite();}
				layer_num = layer_nums[old_left.rotateLeft().dx+1][old_left.rotateLeft().dy+1];
			}
			if (rights[old_left.rotateRight().dx+1][old_left.rotateRight().dy+1]==old_left.rotateLeft().rotateLeft()
					&& layer_nums[old_left.rotateRight().dx+1][old_left.rotateRight().dy+1]==next_layer_num) {
				left = old_left.rotateRight().rotateRight();
				if (left==right) {right = left.opposite();}
				layer_num = layer_nums[old_left.rotateRight().dx+1][old_left.rotateRight().dy+1];
			}
			Action.move(old_left);
		}
		else {  // move right
			Direction old_right = right;
			left = right.opposite();
			right = left.rotateRight().rotateRight();
			if (!rc.onTheMap(Info.loc.add(old_right).add(right))) {right = right.rotateRight().rotateRight();}
			else {
				RobotInfo robot = rc.senseRobotAtLocation(Info.loc.add(old_right).add(right));  // get new right direction after moving
				if (robot!=null) {
					if (robot.team==Info.friendly) {
						int flag = rc.getFlag(robot.ID);
						if (flag>>23==1 && (flag>>17)%4==next_layer_num) {  // is_membrane and robot's layer_num is next_layer_num
							right = right.rotateRight().rotateRight();
						}
					}
				}
			}
			if (!rc.onTheMap(Info.loc.add(old_right).add(right))) {right = right.rotateRight().rotateRight();}
			else {
				RobotInfo robot = rc.senseRobotAtLocation(Info.loc.add(old_right).add(right));  // get new right direction after moving
				if (robot!=null) {
					if (robot.team==Info.friendly) {
						int flag = rc.getFlag(robot.ID);
						if (flag>>23==1 && (flag>>17)%4==next_layer_num) {  // is_membrane and robot's layer_num is next_layer_num
							right = right.rotateRight().rotateRight();
						}
					}
				}
			}
			if (lefts[old_right.rotateLeft().dx+1][old_right.rotateLeft().dy+1]==old_right.rotateRight().rotateRight()
					&& layer_nums[old_right.rotateLeft().dx+1][old_right.rotateLeft().dy+1]==next_layer_num) {  // attach to layer
				right = old_right.rotateLeft().rotateLeft();
				if (left==right) {left = right.opposite();}
				layer_num = layer_nums[old_right.rotateLeft().dx+1][old_right.rotateLeft().dy+1];
			}
			if (lefts[old_right.rotateRight().dx+1][old_right.rotateRight().dy+1]==old_right.rotateLeft().rotateLeft()
					&& layer_nums[old_right.rotateRight().dx+1][old_right.rotateRight().dy+1]==next_layer_num) {
				right = old_right.rotateRight().rotateRight();
				if (left==right) {left = right.opposite();}
				layer_num = layer_nums[old_right.rotateRight().dx+1][old_right.rotateRight().dy+1];
			}
			if (rights[old_right.rotateLeft().dx+1][old_right.rotateLeft().dy+1]==old_right.rotateRight().rotateRight()
					&& layer_nums[old_right.rotateLeft().dx+1][old_right.rotateLeft().dy+1]==next_layer_num) {
				left = old_right.rotateLeft().rotateLeft();
				if (left==right) {right = left.opposite();}
				layer_num = layer_nums[old_right.rotateLeft().dx+1][old_right.rotateLeft().dy+1];
			}
			if (rights[old_right.rotateRight().dx+1][old_right.rotateRight().dy+1]==old_right.rotateLeft().rotateLeft()
					&& layer_nums[old_right.rotateRight().dx+1][old_right.rotateRight().dy+1]==next_layer_num) {
				left = old_right.rotateRight().rotateRight();
				if (left==right) {right = left.opposite();}
				layer_num = layer_nums[old_right.rotateRight().dx+1][old_right.rotateRight().dy+1];
			}
			Action.move(old_right);
		}
	}
}
