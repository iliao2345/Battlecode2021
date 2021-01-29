package slands3;
import battlecode.common.*;

public class Math2 {
	public static Direction[] DIAGONAL_DIRECTIONS = new Direction[] {Direction.NORTHEAST, Direction.NORTHWEST, Direction.SOUTHWEST, Direction.SOUTHEAST};
	public static Direction[] UNIT_DIRECTIONS = new Direction[] {Direction.EAST, Direction.NORTHEAST, Direction.NORTH, Direction.NORTHWEST, Direction.WEST, Direction.SOUTHWEST, Direction.SOUTH, Direction.SOUTHEAST};
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
	public static Direction cardinalize_left(Direction dir) {
		if (dir.dx*dir.dx+dir.dy*dir.dy==2) {return dir.rotateLeft();}
		return dir;
	}
	public static Direction cardinalize_right(Direction dir) {
		if (dir.dx*dir.dx+dir.dy*dir.dy==2) {return dir.rotateRight();}
		return dir;
	}
	public static int get_embezzle_income(int conviction) {
		return (int) Math.floor(conviction*(1/50.0+0.03*Math.exp(-0.001*conviction)));
	}
	public static int embezzle_floor(int conviction) {
		while (true) {
			if (Math.floor((conviction-1)*(1/50.0+0.03*Math.exp(-0.001*(conviction-1)))) < Math.floor(conviction*(1/50.0+0.03*Math.exp(-0.001*conviction)))) {
				return conviction;
			}
			conviction--;
		}
	}
	public static int embezzle_ceil(int conviction) {
		while (true) {
			if (Math.floor((conviction-1)*(1/50.0+0.03*Math.exp(-0.001*(conviction-1)))) < Math.floor(conviction*(1/50.0+0.03*Math.exp(-0.001*conviction)))) {
				return conviction;
			}
			conviction++;
		}
	}
}