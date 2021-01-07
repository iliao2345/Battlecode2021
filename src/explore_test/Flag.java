package explore_test;

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

public class Flag {

	public static final int MAX_APPROX_DIST = 63;
	public static final int MAX_APPROX_DIST_PLUS_ONE = 64;
	public static final int LOG_MAX_APPROX_DIST = 6;
	public static final int LOG_MAX_APPROX_DIST_PLUS_ONE = 7;

	public static RobotController rc;
	public static int[] bits = new int[] {24};
	public static int[] values = new int[] {0};
	public static boolean spreading = false;
	
	public static void set_default_patrol() throws GameActionException {
		bits = new int[] {23-2*LOG_MAX_APPROX_DIST, LOG_MAX_APPROX_DIST, LOG_MAX_APPROX_DIST, 1};
		values = new int[] {0, Info.approx_enemy_dist, Info.approx_home_dist, (spreading)?1:0};
		if (spreading) {rc.setIndicatorDot(Info.loc, 0, 255, 255);}
	}
	
	public static void set_ec() throws GameActionException {
		bits = new int[] {8, 15, 1};
		values = new int[] {0,(Info.round_num%2==0)?Info.x:Info.y, Info.round_num%2};
	}
	
	public static void display() throws GameActionException {
		rc.setFlag(encode(bits, values));
		bits = new int[] {24};
		values = new int[] {0};
		spreading = false;
	}
	public static int encode(int[] bits, int[] values) {
		int flag = 0;
		for (int i=0; i<bits.length; i++) {
			flag = (flag<<bits[i]) + values[i];
		}
		return flag;
	}
	public static int[] decode(int[] bits, int flag) {
		int[] values = new int[bits.length];
		for (int i=bits.length-1; i>=0; i--) {
			values[i] = flag%(1<<bits[i]);
			flag = flag>>bits[i];
		}
		return values;
	}
	public static int sample(int num, int start, int length) {
		return (num>>start) % (1<<length);
	}
}
