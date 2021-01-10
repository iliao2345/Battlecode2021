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

public class Flag {

	public static final int MAX_APPROX_DIST = 255;
	public static final int MAX_APPROX_DIST_PLUS_ONE = 256;
	public static final int LOG_MAX_APPROX_DIST = 8;
	public static final int LOG_MAX_APPROX_DIST_PLUS_ONE = 9;

	public static RobotController rc;
	public static int[] bits = new int[] {24};
	public static int[] values = new int[] {0};
	
	public static void set_default_patrol() throws GameActionException {
		bits = new int[] {1, 2, 2, 2, 1, 1, 6, LOG_MAX_APPROX_DIST, 1};
		int right = 0;
		int left = 0;
		switch (Membrane.right) {
		case NORTH: {right=0; break;}
		case EAST: {right=1; break;}
		case SOUTH: {right=2; break;}
		case WEST: {right=3; break;}
		default: break;
		}
		switch (Membrane.left) {
		case NORTH: {left=0; break;}
		case EAST: {left=1; break;}
		case SOUTH: {left=2; break;}
		case WEST: {left=3; break;}
		default: break;
		}
		values = new int[] {(Phase.is_membrane)?1:0,
							left,
							right,
							Membrane.layer_num,
							(Membrane.left_detached)?1:0,
							(Membrane.right_detached)?1:0,
							0,
							Info.approx_enemy_dist,
							(Phase.is_gas)?1:0
							};
	}
	
	public static void set_ec() throws GameActionException {
		bits = new int[] {9, 14, 1};
		values = new int[] {ECInfo.enemy_data_to_relay, 0, Info.round_num%2};
	}
	
	public static void display() throws GameActionException {
		rc.setFlag(encode(bits, values));
		bits = new int[] {24};
		values = new int[] {0};
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
