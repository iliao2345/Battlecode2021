package micro2;
import battlecode.common.*;

public class ECInfo {
	public static RobotController rc;
	public static int open_spawn_tiles_required;
	public static RobotType[] ALL_ROBOTTYPES = new RobotType[] {RobotType.ENLIGHTENMENT_CENTER, RobotType.POLITICIAN, RobotType.SLANDERER, RobotType.MUCKRAKER};
	public static int STOP_ECONOMY_TIME = 1050;
	public static int BID_START_TURN = 300;
	public static int n_spawn_tiles_on_map;
	
	public static IntCycler unclassified_ids = null;
	public static int n_unclassified_ids = 0;
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
	public static int[] sampled_bury_guard_influences;
	public static int sampled_bury_guard_influence;
	public static int n_sampled_bury_guard_influences;
	public static int[] sampled_muckraker_influences;
	public static int sampled_muckraker_influence;
	public static int n_sampled_muckraker_influences;
	public static boolean majority_crowded;
	public static boolean target_all_ecs_flag;
	public static int bid_amount = 1;
	public static boolean last_bid_1 = false;
	public static int max_safe_build_limit;
	
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
		sampled_bury_guard_influences = new int[1000];
		n_sampled_bury_guard_influences = 0;
		sampled_muckraker_influences = new int[1000];
		n_sampled_muckraker_influences = 0;
	}
	
	public static void update() throws GameActionException {
		make_targetter_timer = Math.max(0, make_targetter_timer-1);
		if (make_targetter_timer==0) {targetted_loc = null;}
		int n_crowded = 0;
		int n_iterations = Math.min(n_unclassified_ids, 50);
		for (int i=n_iterations; --i>=0;) {
			unclassified_ids = unclassified_ids.next;
			if (!rc.canGetFlag(unclassified_ids.data)) {
				unclassified_ids = unclassified_ids.pop();
				n_unclassified_ids--;
			}
			else {
				int flag = rc.getFlag(unclassified_ids.data);
				if (flag>>23==1) {
					n_crowded += flag%2;
				}
				if (flag>>22==1) {
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
		majority_crowded = n_crowded>5 && n_crowded>0.5*n_iterations;
		n_sampled_muckraker_influences = 0;
		n_iterations = Math.min(n_guard_ids, 50);
		for (int i=n_iterations; --i>=0;) {
			if (rc.canGetFlag(guard_ids.data)) {
				int flag = rc.getFlag(guard_ids.data);
				if (flag>>22==1) {
					guard_ids = guard_ids.next;
					if ((flag>>1)%16!=0) {
						sampled_muckraker_influences[n_sampled_muckraker_influences] = (int) (4*(Math.exp((flag>>1)%16/4.0)-1));
						n_sampled_muckraker_influences++;
					}
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
		if (n_sampled_muckraker_influences>0) {
			sampled_muckraker_influence = sampled_muckraker_influences[(int)(Math.random()*n_sampled_muckraker_influences)];
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
		weakest_ec_influence = 16;
		weakest_ec_loc = null;
		sampled_bury_guard_influence = 0;
		n_sampled_bury_guard_influences = 0;
		for (int i=n_burier_ids; --i>=0;) {
			if (rc.canGetFlag(burier_ids.data)) {
				int flag = rc.getFlag(burier_ids.data);
				if (flag>>21==1) {
					new_all_ecs_buried = new_all_ecs_buried && flag%2==0;
					found_enemy_ec = true;
					if ((flag>>1)%16<weakest_ec_influence && (flag>>1)%16!=0 && make_targetter_timer==0) {
						int index = (flag>>9)%4096;
						if (!targetter_exists[index] || Info.round_num>Targetter.TARGET_ALL_ECS_TIME) {
							weakest_ec_influence = (flag>>1)%16;
							weakest_ec_loc = new MapLocation(Info.x + ((flag>>15)%64*2-Info.x+12800064)%128-64, Info.y + ((flag>>9)%64*2-Info.y+12800064)%128-64);
						}
					}
					if ((flag>>5)%16>0) {
						sampled_bury_guard_influences[n_sampled_bury_guard_influences] = (int) (2*(Math.exp((flag>>5)%16/2.0)-1) + 10);
						n_sampled_bury_guard_influences++;
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
		weakest_ec_influence = (int) (10*(Math.exp(weakest_ec_influence/2.0)-1));
		if (n_sampled_bury_guard_influences>0) {
			sampled_bury_guard_influence = sampled_bury_guard_influences[(int)(Math.random()*n_sampled_bury_guard_influences)];
		}
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
		int min_distance_to_enemy_ec = Integer.MAX_VALUE;
		for (int i=Info.n_relayers; --i>=0;) {
			min_distance_to_enemy_ec = Math.min(min_distance_to_enemy_ec, (rc.getFlag(Info.relayers[i].ID)>>4)%256);
		}
		max_safe_build_limit = Info.conviction - Info.nearby_enemy_power;
		int max_muckraker_warning_level = 0;
		for (int i=Info.n_relayers; --i>=0;) {
			int flag = rc.getFlag(Info.relayers[i].ID);
			max_muckraker_warning_level = Math.max(max_muckraker_warning_level, (flag>>1)%8);
		}
		
		unit_price = total_income_per_build*2;
		exterminate_flag = Info.round_num>Exterminator.EXTERMINATE_START_TIME;
		desired_guard_flag = (max_muckraker_warning_level<=5 && Info.round_num<100 && n_slanderer_ids<=0.1*n_unclassified_ids || Info.round_num>400 && all_ecs_buried && n_unclassified_ids>0.7*min_distance_to_enemy_ec) && Info.round_num<STOP_ECONOMY_TIME && !exterminate_flag;
		guard_flag = (desired_guard_flag || n_slanderer_ids>0) && Info.round_num>400 && !exterminate_flag;
		enough_guards = n_guard_ids>n_slanderer_ids+15 || Info.round_num<400;
		target_all_ecs_flag = Info.round_num>Targetter.TARGET_ALL_ECS_TIME;

		int bids_required = 751-rc.getTeamVotes();
		int rounds_left = 1500-Info.round_num;
		if (bids_required<rounds_left && bids_required>0 && Info.round_num>BID_START_TURN) {
			if (Info.round_num>Exterminator.EXTERMINATE_FINISH_MONEY_TIME) {
				int bid = Info.conviction/rounds_left+passive_income;
				if (rc.canBid(bid)) {rc.bid(bid);}
			}
			else {
				if (Math.random()<0.4 || bid_amount>max_safe_build_limit || Info.conviction<300) {
					if (rc.canBid(1)) {rc.bid(1);}
				}
				else if (rc.canBid(bid_amount)) {
					rc.bid(bid_amount);
				}
				if (!last_bid_1) {
					if (Info.team_votes>Info.last_team_votes) {bid_amount--;}
					else {bid_amount++;}
					bid_amount = Math.max(1, bid_amount);
				}
			}
		}
	}
}
