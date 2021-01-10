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

public class Math2 {
	public static Direction[] DIAGONAL_DIRECTIONS = new Direction[] {Direction.NORTHEAST, Direction.NORTHWEST, Direction.SOUTHWEST, Direction.SOUTHEAST};
	public static Direction[] UNIT_DIRECTIONS = new Direction[] {Direction.EAST, Direction.NORTHEAST, Direction.NORTH, Direction.NORTHWEST, Direction.WEST, Direction.SOUTHWEST, Direction.SOUTH, Direction.SOUTHEAST};
	public static double SQRT_5 = Math.sqrt(5);
	public static double GOLDEN_RATIO = (1 + SQRT_5)/2;
	public static double FIBONACCI_SUM_RATIO = 1/(1-1/GOLDEN_RATIO);  // sum of first n fibonacci numbers is about this many times larger than the nth fibonacci number
	public static int length(int x, int y) {
		return Math.max(Math.abs(x), Math.abs(y));
	}
	public static int length(int x1, int y1, int x2, int y2) {
		return Math.max(Math.abs(x1-x2), Math.abs(y1-y2));
	}
	public static int length(MapLocation loc1, MapLocation loc2) {
		return Math.max(Math.abs(loc1.x-loc2.x), Math.abs(loc1.y-loc2.y));
	}
	public static int cardinal_length(int x, int y) {
		return Math.abs(x)+Math.abs(y);
	}
	public static int cardinal_length(int x1, int y1, int x2, int y2) {
		return Math.abs(x1-x2)+Math.abs(y1-y2);
	}
	public static int cardinal_length(MapLocation loc1, MapLocation loc2) {
		return Math.abs(loc1.x-loc2.x)+Math.abs(loc1.y-loc2.y);
	}
	public static int cardinal_length(Direction dir) {
		return Math.abs(dir.dx)+Math.abs(dir.dy);
	}
	public static int fibonacci(int i) {
		return (int) Math.round(Math.pow(GOLDEN_RATIO, i+1)/SQRT_5);
	}
	public static Direction cardinalize_left(Direction dir) {
		if (dir.dx*dir.dx+dir.dy*dir.dy==2) {return dir.rotateLeft();}
		return dir;
	}
	public static Direction cardinalize_right(Direction dir) {
		if (dir.dx*dir.dx+dir.dy*dir.dy==2) {return dir.rotateRight();}
		return dir;
	}
	public static int get_embezzle_income(int conviction) {
		return (int) Math.floor(conviction*(1/50+0.03*Math.exp(-0.001*conviction)));
	}
	public static int embezzle_floor(int conviction) {
		if (conviction<21) {return 0;}
		if (conviction<41) {return 21;}
		if (conviction<63) {return 41;}
		if (conviction<85) {return 63;}
		if (conviction<107) {return 85;}
		if (conviction<130) {return 107;}
		if (conviction<154) {return 130;}
		if (conviction<178) {return 154;}
		if (conviction<203) {return 178;}
		if (conviction<228) {return 203;}
		if (conviction<255) {return 228;}
		if (conviction<282) {return 255;}
		if (conviction<310) {return 282;}
		if (conviction<339) {return 310;}
		if (conviction<368) {return 339;}
		if (conviction<399) {return 368;}
		if (conviction<431) {return 399;}
		if (conviction<463) {return 431;}
		if (conviction<497) {return 463;}
		if (conviction<532) {return 497;}
		if (conviction<568) {return 532;}
		if (conviction<605) {return 568;}
		if (conviction<643) {return 605;}
		if (conviction<683) {return 643;}
		if (conviction<724) {return 683;}
		if (conviction<766) {return 724;}
		if (conviction<810) {return 766;}
		if (conviction<855) {return 810;}
		if (conviction<902) {return 855;}
		if (conviction<949) {return 902;}
		return 949;
	}
	public static int embezzle_ceil(int conviction) {
		if (conviction<21) {return 21;}
		if (conviction<41) {return 41;}
		if (conviction<63) {return 63;}
		if (conviction<85) {return 85;}
		if (conviction<107) {return 107;}
		if (conviction<130) {return 130;}
		if (conviction<154) {return 154;}
		if (conviction<178) {return 178;}
		if (conviction<203) {return 203;}
		if (conviction<228) {return 228;}
		if (conviction<255) {return 255;}
		if (conviction<282) {return 282;}
		if (conviction<310) {return 310;}
		if (conviction<339) {return 339;}
		if (conviction<368) {return 368;}
		if (conviction<399) {return 399;}
		if (conviction<431) {return 431;}
		if (conviction<463) {return 463;}
		if (conviction<497) {return 497;}
		if (conviction<532) {return 532;}
		if (conviction<568) {return 568;}
		if (conviction<605) {return 605;}
		if (conviction<643) {return 643;}
		if (conviction<683) {return 683;}
		if (conviction<724) {return 724;}
		if (conviction<766) {return 766;}
		if (conviction<810) {return 810;}
		if (conviction<855) {return 855;}
		if (conviction<902) {return 902;}
		return 949;
	}
}
