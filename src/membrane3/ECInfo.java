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

public class ECInfo {
	public static RobotController rc;
	public static IntCycler ids = null;
	public static IntCycler types = null;  // 0 for EC, 1 for politician, 2 for slanderer, 3 for muckraker
	public static Direction last_build_direction = Direction.EAST;
	public static int passive_income;
	public static int[] embezzle_incomes;
	public static int embezzle_income;
	public static int total_income;
	public static double total_income_per_build;
	public static boolean form_membrane = true;
	public static int n_enemies_adjacent;
	public static Direction standard_available_build_direction;  // a direction to build that's not next to reabsorbing politicians
	public static double approx_membrane_thickness = 0.0;
	public static double approx_membrane_politician_ratio = 0.0;
	public static int maximum_safe_build_conviction;

	public static void initialize(RobotController rc) {
		ECInfo.rc = rc;
		embezzle_incomes = new int[GameConstants.EMBEZZLE_NUM_ROUNDS];
	}
	
	public static void update() throws GameActionException {
		passive_income = (int) Math.ceil(GameConstants.PASSIVE_INFLUENCE_RATIO_ENLIGHTENMENT_CENTER*Math.sqrt(Info.round_num));
		embezzle_incomes[Info.round_num%GameConstants.EMBEZZLE_NUM_ROUNDS] = 0;
		embezzle_income = 0;
		for (int i=0; i<GameConstants.EMBEZZLE_NUM_ROUNDS; i++) {
			embezzle_income += embezzle_incomes[i];
		}
		total_income = passive_income + embezzle_income;
		total_income_per_build = ECInfo.total_income*RobotType.ENLIGHTENMENT_CENTER.actionCooldown/Info.tile_cost;
		n_enemies_adjacent = 0;
		for (Direction dir:Math2.UNIT_DIRECTIONS) {
			RobotInfo robot = Info.adjacent_robots[dir.dx+1][dir.dy+1];
			if (robot!=null) {
				if (robot.team==Info.enemy) {
					n_enemies_adjacent++;
				}
			}
		}
		int total_enemy_politician_potential_damage = 0;
		for (Direction dir:Math2.UNIT_DIRECTIONS) {  // compute net convictions to see where enemies will remain after nearby empowers finish
			RobotInfo robot = Info.adjacent_robots[dir.dx+1][dir.dy+1];
			if (robot!=null) {
				if (robot.team==Info.enemy && robot.type==RobotType.POLITICIAN) {
					total_enemy_politician_potential_damage += robot.conviction-GameConstants.EMPOWER_TAX;
				}
			}
		}
		maximum_safe_build_conviction = Info.conviction - total_enemy_politician_potential_damage + 1;  // add 1 just to be sure
		// WARNING: reabsorption threshold must be sufficiently separated from the threshold for building politicians for the membrane
		// WARNING: don't overflow the integers
		
		standard_available_build_direction = ECInfo.last_build_direction;  // find a direction to build that's not next to reabsorbing politicians
		boolean space_to_build = false;
		for (int i=0; i<8; i++) {
			standard_available_build_direction = standard_available_build_direction.rotateLeft().rotateLeft().rotateLeft();
			if (!rc.canBuildRobot(RobotType.MUCKRAKER, standard_available_build_direction, 1)) {continue;}
			RobotInfo left_robot = Info.adjacent_robots[standard_available_build_direction.rotateLeft().dx+1][standard_available_build_direction.rotateLeft().dy+1];
			if (left_robot!=null) {  // skip if left robot is a reabsorbing politician
				if (left_robot.team==Info.friendly && left_robot.type==RobotType.POLITICIAN && (rc.getFlag(left_robot.ID)>>13)%2==1) {continue;}
			}
			RobotInfo right_robot = Info.adjacent_robots[standard_available_build_direction.rotateRight().dx+1][standard_available_build_direction.rotateRight().dy+1];
			if (right_robot!=null) {  // skip if left robot is a reabsorbing politician
				if (right_robot.team==Info.friendly && right_robot.type==RobotType.POLITICIAN && (rc.getFlag(right_robot.ID)>>13)%2==1) {continue;}
			}
			space_to_build = true;
            break;
		}
		if (!space_to_build) {
			standard_available_build_direction = null;
		}
		form_membrane = Info.round_num>1000;
		
		if (ids!=null) {
			for (int i=0; i<50; i++) {
				if (!rc.canGetFlag(ids.data)) {
					if (ids.next==ids) {
						ids = null;
						types = null;
						break;
					}
					ids.last.next = ids.next;
					ids.next.last = ids.last;
					types.last.next = types.next;
					types.next.last = types.last;
				}
				else {
					int flag = rc.getFlag(ids.data);
					if (flag>>23==1) {
						approx_membrane_thickness += 0.01*(((flag>>20)%8+0.5)*2 - approx_membrane_thickness);
						approx_membrane_politician_ratio += 0.01*((types.data==1||types.data==2)?1:0-approx_membrane_politician_ratio);
					}
				}
				ids = ids.next;
				types = types.next;
			}
		}
	}
}
