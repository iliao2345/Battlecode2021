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

public class ECInfo {
	public static RobotController rc;
	public static IntCycler ids = null;
	public static int enemy_data_to_relay;
	public static Direction last_build_direction = Direction.EAST;
	public static int passive_income;
	public static int[] embezzle_incomes;
	public static int embezzle_income;
	public static int total_income;
	public static int target_stockpile;

	public static void initialize(RobotController rc) {
		ECInfo.rc = rc;
		embezzle_incomes = new int[GameConstants.EMBEZZLE_NUM_ROUNDS];
		target_stockpile = 150;
	}
	
	public static void update() throws GameActionException {
		enemy_data_to_relay = 0;
		if (ids!=null) {
			for (int i=0; i<20; i++) {
				if (!rc.canGetFlag(ids.data)) {
					if (ids.next==ids) {
						ids = null;
						break;
					}
					ids.last.next = ids.next;
					ids.next.last = ids.last;
				}
				else {
					int flag = rc.getFlag(ids.data);
				}
				ids = ids.next;
			}
		}
		passive_income = (int) Math.ceil(GameConstants.PASSIVE_INFLUENCE_RATIO_ENLIGHTENMENT_CENTER*Math.sqrt(Info.round_num));
		embezzle_incomes[Info.round_num%GameConstants.EMBEZZLE_NUM_ROUNDS] = 0;
		embezzle_income = 0;
		for (int i=0; i<GameConstants.EMBEZZLE_NUM_ROUNDS; i++) {
			embezzle_income += embezzle_incomes[i];
		}
		total_income = passive_income + embezzle_income;
		target_stockpile += total_income/2;
	}
}
