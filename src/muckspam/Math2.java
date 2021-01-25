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
}
