package buryguard;
import battlecode.common.*;

public class ECInfo {
	public static RobotController rc;
	public static int open_spawn_tiles_required;
	public static RobotType[] ALL_ROBOTTYPES = new RobotType[] {RobotType.ENLIGHTENMENT_CENTER, RobotType.POLITICIAN, RobotType.SLANDERER, RobotType.MUCKRAKER};
//	public static int STOP_ECONOMY_TIME = 1050;
	public static int STOP_ECONOMY_TIME = 1500;
	public static int BID_START_TURN = 300;
	public static int n_spawn_tiles_on_map;
	
	public static IntCycler unclassified_ids = null;
	public static int n_unclassified_ids = 0;
	public static IntCycler relayer_ids = null;
	public static IntCycler relayer_xs = null;
	public static IntCycler relayer_ys = null;
	public static int n_relayer_ids = 0;
	public static IntCycler guard_ids = null;
	public static int n_guard_ids = 0;
	public static IntCycler burier_ids = null;
	public static int n_burier_ids = 0;
	public static IntCycler targetter_ids = null;  // separate system. targetters remain in unclassified_ids and are also in targetter_ids.
	public static IntCycler targetter_indices = null;
	public static int n_targetter_ids = 0;
	public static IntCycler slanderer_ids = null;
	public static int n_slanderer_ids = 0;
	public static int passive_income;
	public static int[] embezzler_ids;
	public static int[] embezzler_incomes;
	public static int embezzle_income;
	public static int total_income;
	public static double total_income_per_build;
	public static int open_spawn_tiles;
	public static double unit_price;
	public static int weakest_ec_influence;  // weakest only among the ones that don't have targetters for them already
	public static MapLocation weakest_ec_loc = new MapLocation(0, 0);
	public static boolean[] targetter_exists = new boolean[4096];
	public static boolean guard_flag;
	public static boolean desired_guard_flag;
	public static boolean enough_guards;
	public static boolean all_ecs_buried = false;
	public static int make_targetter_timer = 0;
	public static MapLocation targetted_loc;
	public static boolean exterminate_flag = false;
	public static Direction last_build_direction = Direction.EAST;
	public static boolean map_controlled;
	public static boolean target_all_ecs_flag;
	public static int bid_power = 1;
	public static boolean last_bid_1 = false;
	public static int max_safe_build_limit;
	public static double min_muckraker_alert_distance = 1000000000;  // the distance to the closest relayer who sees an enemy muckraker
	
	public static void initialize() throws GameActionException {
		embezzler_ids = new int[GameConstants.EMBEZZLE_NUM_ROUNDS];
		embezzler_incomes = new int[GameConstants.EMBEZZLE_NUM_ROUNDS];
		n_spawn_tiles_on_map = 0;
		for (Direction dir:Math2.UNIT_DIRECTIONS) {
			if (rc.onTheMap(rc.getLocation().add(dir))) {
				n_spawn_tiles_on_map++;
			}
		}
		open_spawn_tiles_required = Math.min(n_spawn_tiles_on_map, (int) Math.ceil(11*rc.sensePassability(rc.getLocation())/2)+1);
	}
	
	public static void update() throws GameActionException {
		make_targetter_timer = Math.max(0, make_targetter_timer-1);
		if (make_targetter_timer==0) {targetted_loc = null;}
		int n_iterations = Math.min(n_unclassified_ids, 30);
		for (int i=n_iterations; --i>=0;) {
			unclassified_ids = unclassified_ids.next;
			if (!rc.canGetFlag(unclassified_ids.data)) {
				unclassified_ids = unclassified_ids.pop();
				n_unclassified_ids--;
			}
			else {
				int flag = rc.getFlag(unclassified_ids.data);
				if (flag>>23==1) {
					relayer_ids = new IntCycler(unclassified_ids.data, relayer_ids);
					relayer_xs = new IntCycler(((flag>>18)%32*4+1-Info.x+12800064)%128-64, relayer_xs);
					relayer_ys = new IntCycler(((flag>>13)%32*4+1-Info.x+12800064)%128-64, relayer_ys);
					n_relayer_ids++;
					unclassified_ids = unclassified_ids.pop();
					n_unclassified_ids--;
				}
				else if (flag>>22==1) {
					guard_ids = new IntCycler(unclassified_ids.data, guard_ids);
					n_guard_ids++;
					unclassified_ids = unclassified_ids.pop();
					n_unclassified_ids--;
				}
				else if (flag>>21==1) {
					burier_ids = new IntCycler(unclassified_ids.data, burier_ids);
					n_burier_ids++;
					unclassified_ids = unclassified_ids.pop();
					n_unclassified_ids--;
				}
			}
		}
		min_muckraker_alert_distance++;
		n_iterations = Math.min(n_relayer_ids, 30);
		for (int i=n_iterations; --i>=0;) {
			if (rc.canGetFlag(relayer_ids.data)) {
				int flag = rc.getFlag(relayer_ids.data);
				if (flag>>23==1) {
					relayer_xs.data = ((flag>>18)%32*4+1-Info.x+12800064)%128-64;
					relayer_ys.data = ((flag>>13)%32*4+1-Info.y+12800064)%128-64;
					relayer_ids = relayer_ids.next;
					relayer_xs = relayer_xs.next;
					relayer_ys = relayer_ys.next;
					if (flag%2==1) {
						min_muckraker_alert_distance = Math.min(min_muckraker_alert_distance, Math.sqrt((Info.x-relayer_xs.data)*(Info.x-relayer_xs.data)+(Info.y-relayer_ys.data)*(Info.y-relayer_ys.data)));
					}
					continue;
				}
				else {  // role change
					unclassified_ids = new IntCycler(relayer_ids.data, unclassified_ids);
					n_unclassified_ids++;
					relayer_ids = relayer_ids.pop();
					relayer_xs = relayer_xs.pop();
					relayer_ys = relayer_ys.pop();
					n_relayer_ids--;
				}
			}
			else {  // death, conversion, or empowerment
				relayer_ids = relayer_ids.pop();
				relayer_xs = relayer_xs.pop();
				relayer_ys = relayer_ys.pop();
				n_relayer_ids--;
			}
		}
		if (Info.n_enemy_muckrakers>0) {min_muckraker_alert_distance = 0;}
		n_iterations = Math.min(n_guard_ids, 30);
		for (int i=n_iterations; --i>=0;) {
			if (rc.canGetFlag(guard_ids.data)) {
				int flag = rc.getFlag(guard_ids.data);
				if (flag>>22==1) {
					guard_ids = guard_ids.next;
					continue;
				}
				else {
					unclassified_ids = new IntCycler(guard_ids.data, unclassified_ids);
					n_unclassified_ids++;
					guard_ids = guard_ids.pop();
					n_guard_ids--;
				}
			}
			else {
				guard_ids = guard_ids.pop();
				n_guard_ids--;
			}
		}
		if (make_targetter_timer==0) {  // give new targetters some time to set their flags, don't remove them immediately
			for (int i=n_targetter_ids; --i>=0;) {
				if (rc.canGetFlag(targetter_ids.data)) {
					int flag = rc.getFlag(targetter_ids.data);
					if (flag>>20==1) {
						targetter_ids = targetter_ids.next;
						targetter_indices = targetter_indices.next;
						continue;
					}
				}
				targetter_exists[targetter_indices.data] = false; 
				targetter_ids = targetter_ids.pop();
				targetter_indices = targetter_indices.pop();
				n_targetter_ids--;
			}
		}
		if (n_guard_ids==0) {enough_guards = false;}
		boolean new_all_ecs_buried = true;
		boolean found_enemy_ec = false;
		weakest_ec_influence = 64;
		weakest_ec_loc = null;
		for (int i=n_burier_ids; --i>=0;) {
			if (rc.canGetFlag(burier_ids.data)) {
				int flag = rc.getFlag(burier_ids.data);
				if (flag>>21==1) {
					new_all_ecs_buried = new_all_ecs_buried && flag%2==0;
					found_enemy_ec = true;
					int influence = (flag>>13)%64;
					MapLocation loc = new MapLocation(Info.x + ((flag>>7)%64*2-Info.x+12800064)%128-64, Info.y + ((flag>>1)%64*2-Info.y+12800064)%128-64);
					if ((influence<weakest_ec_influence || influence==weakest_ec_influence && Info.loc.distanceSquaredTo(weakest_ec_loc)>Info.loc.distanceSquaredTo(loc)) && influence%64!=0 && make_targetter_timer==0) {
						int index = (flag>>1)%4096;
						if (!targetter_exists[index] || Info.round_num>Targetter.TARGET_ALL_ECS_TIME) {
							weakest_ec_influence = influence;
							weakest_ec_loc = loc;
						}
					}
					burier_ids = burier_ids.next;
					continue;
				}
				else {
					unclassified_ids = new IntCycler(burier_ids.data, unclassified_ids);
					n_unclassified_ids++;
					burier_ids = burier_ids.pop();
					n_burier_ids--;
				}
			}
			else {
				burier_ids = burier_ids.pop();
				n_burier_ids--;
			}
		}
		all_ecs_buried = found_enemy_ec? new_all_ecs_buried : all_ecs_buried;
		weakest_ec_influence = (int) Math.ceil(10*(Math.exp(weakest_ec_influence/8.0)-1));
		if (weakest_ec_loc!=null) {
			System.out.println(weakest_ec_influence);
			System.out.println(weakest_ec_loc.x);
			System.out.println(weakest_ec_loc.y);
		}
		int min_pressure = Integer.MAX_VALUE;
		for (int i=Info.n_relayers; --i>=0;) {
			min_pressure = Math.min(min_pressure, (rc.getFlag(Info.relayers[i].ID)>>1)%32);
		}
		map_controlled = min_pressure>5 && Info.n_relayers>0;
		for (int i=n_slanderer_ids; --i>=0;) {
			if (!rc.canGetFlag(slanderer_ids.data)) {
				slanderer_ids = slanderer_ids.pop();
				n_slanderer_ids--;
			}
			else {
				slanderer_ids = slanderer_ids.next;
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
		open_spawn_tiles = 0;
		for (Direction dir:Math2.UNIT_DIRECTIONS) {
			if (rc.canSenseLocation(Info.loc.add(dir))) {
				RobotInfo robot = rc.senseRobotAtLocation(Info.loc.add(dir));
				if (robot==null || robot.team==Info.friendly) {
					open_spawn_tiles++;
				}
			}
		}
		max_safe_build_limit = Info.conviction - Info.nearby_enemy_power;
		
		unit_price = total_income_per_build*2;
		exterminate_flag = Info.round_num>Exterminator.EXTERMINATE_START_TIME;
		desired_guard_flag = (min_muckraker_alert_distance>15 && Info.round_num<=25 && 8.9*n_slanderer_ids<=Info.round_num && Info.conviction<0.8*weakest_ec_influence || Info.round_num>400 && map_controlled) && Info.round_num<STOP_ECONOMY_TIME && !exterminate_flag;
		guard_flag = (desired_guard_flag || n_slanderer_ids>0) && Info.round_num>400 && !exterminate_flag;
		enough_guards = n_guard_ids>1.5*n_slanderer_ids+15 || Info.round_num<400 && min_muckraker_alert_distance>15;
		target_all_ecs_flag = Info.round_num>Targetter.TARGET_ALL_ECS_TIME;
	}
}
