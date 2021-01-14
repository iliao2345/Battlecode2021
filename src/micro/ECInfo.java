package micro;
import battlecode.common.*;

public class ECInfo {
	public static RobotController rc;
	public static int max_allowable_burying_enemies;
	
	public static IntCycler ids = null;
	public static IntCycler types = null;  // 0 for EC, 1 for politician, 2 for slanderer, 3 for muckraker
	public static int passive_income;
	public static int[] embezzler_ids;
	public static int[] embezzler_incomes;
	public static int embezzle_income;
	public static int total_income;
	public static double total_income_per_build;
	public static int n_enemies_adjacent;
	public static double unit_price;
	
	public static void initialize() {
		embezzler_ids = new int[GameConstants.EMBEZZLE_NUM_ROUNDS];
		embezzler_incomes = new int[GameConstants.EMBEZZLE_NUM_ROUNDS];
		max_allowable_burying_enemies = (int)(8-11/Info.tile_cost/2);
	}
	
	public static void update() throws GameActionException {
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
				}
				ids = ids.next;
				types = types.next;
			}
		}
		passive_income = (int) Math.ceil(GameConstants.PASSIVE_INFLUENCE_RATIO_ENLIGHTENMENT_CENTER*Math.sqrt(Info.round_num));
		embezzler_ids[Info.round_num%GameConstants.EMBEZZLE_NUM_ROUNDS] = 0;
		embezzler_incomes[Info.round_num%GameConstants.EMBEZZLE_NUM_ROUNDS] = 0;
		embezzle_income = 0;
		for (int i=GameConstants.EMBEZZLE_NUM_ROUNDS; --i>=0;) {
			if (rc.canGetFlag(embezzler_ids[i])) {
				embezzle_income += embezzler_incomes[i];
			}
		}
		total_income = passive_income + embezzle_income;
		total_income_per_build = ECInfo.total_income*RobotType.ENLIGHTENMENT_CENTER.actionCooldown/Info.tile_cost;
		n_enemies_adjacent = 0;
		for (Direction dir:Math2.UNIT_DIRECTIONS) {
			RobotInfo robot = rc.senseRobotAtLocation(Info.loc.add(dir));
			if (robot!=null) {
				if (robot.team==Info.enemy) {
					n_enemies_adjacent++;
				}
			}
		}
		unit_price = Math.max(GameConstants.EMPOWER_TAX+1, total_income_per_build);
	}

}
