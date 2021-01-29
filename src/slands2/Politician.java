package slands2;
import battlecode.common.*;

public class Politician {
	public static RobotController rc;
	
	public static void act() throws GameActionException {
		if (Info.round_num>=1490) {
			CombatInfo.compute_self_empower_gains();
			int best_costs = CombatInfo.costs_9;
			int best_radius = 9;
			if (CombatInfo.costs_8>best_costs) {best_costs = CombatInfo.costs_8; best_radius = 8;}
			if (CombatInfo.costs_5>best_costs) {best_costs = CombatInfo.costs_5; best_radius = 5;}
			if (CombatInfo.costs_4>best_costs) {best_costs = CombatInfo.costs_4; best_radius = 4;}
			if (CombatInfo.costs_2>best_costs) {best_costs = CombatInfo.costs_2; best_radius = 2;}
			if (CombatInfo.costs_1>best_costs) {best_costs = CombatInfo.costs_1; best_radius = 1;}
			rc.empower(best_radius); Clock.yield(); return;
		}
		RobotInfo closest_enemy_ec = Info.closest_robot(Info.enemy, RobotType.ENLIGHTENMENT_CENTER);
		RobotInfo closest_neutral_ec = Info.closest_robot(Team.NEUTRAL, RobotType.ENLIGHTENMENT_CENTER);
		if (Role.is_targetter) {  // override micro code to disable self-empower
			Targetter.target(); return;
		}
		else if (Role.is_exterminator || Info.exterminate && Info.conviction>10) {  // override micro code to disable self-empower
			Role.exterminate();
		}
		else if (Role.is_guard && (Info.ec_needs_guards || !Guard.reached_outpost)) {
		}
		else if (Info.n_friendly_ecs+Info.n_friendly_slanderers>0 && Info.ec_needs_guards && Info.conviction>10 && Info.conviction<34) {
			Role.guard(Info.loc);
		}
		else if ((closest_enemy_ec!=null || closest_neutral_ec!=null) && (!Info.everything_buried || Role.is_burier) && !Info.exterminate) {
			Role.bury();
		}
		else {
			Role.attach_to_relay_chain();
		}
		if (Role.is_exterminator) {
			if (Info.n_enemy_muckrakers>0) {Exterminator.lock_target(Info.enemy_muckrakers[0].location);}
			if (Info.n_enemy_slanderers>0) {Exterminator.lock_target(Info.enemy_slanderers[0].location);}
			if (Info.n_enemy_politicians>0) {Exterminator.lock_target(Info.enemy_politicians[0].location);}
			if (Info.n_enemy_ecs>0) {Exterminator.lock_target(Info.enemy_ecs[0].location);}
			Exterminator.exterminate();  // override micro code to disable self-empower
			return;
		}
		CombatInfo.compute_self_empower_gains();
		double best_gains = (CombatInfo.optimal_empower_gains>0)?CombatInfo.optimal_empower_gains:Integer.MIN_VALUE;  // don't use conditional if we want to force suicide to save friendly units or disallow conversions
		RobotInfo largest_nearby_politician = null;
		int largest_conviction = Integer.MIN_VALUE;
		for (int i=Info.n_friendly_politicians; --i>=0;) {
			if (Info.friendly_politicians[i].conviction>largest_conviction && (!Role.is_burier || rc.getFlag(Info.friendly_politicians[i].ID)>>20==1)) {
				largest_nearby_politician = Info.friendly_politicians[i];
				largest_conviction = largest_nearby_politician.conviction;
			}
		}
		for (int i=Info.n_enemy_politicians; --i>=0;) {
			if (Info.enemy_politicians[i].conviction>largest_conviction) {
				largest_nearby_politician = Info.enemy_politicians[i];
				largest_conviction = largest_nearby_politician.conviction;
			}
		}
		double[][] move_gains = new double[3][3];
		boolean[][] negative_gain_tiles = new boolean[3][3];
		if (largest_nearby_politician!=null) {
			move_gains = CombatInfo.compute_move_gains(largest_nearby_politician);
		}
		else {
			best_gains = Math.max(best_gains, 0);
		}
		for (Direction dir:Direction.allDirections()) {
			if (dir==Direction.CENTER || rc.canMove(dir)) {
				best_gains = Math.max(best_gains, move_gains[dir.dx+1][dir.dy+1]);
				negative_gain_tiles[dir.dx+1][dir.dy+1] = move_gains[dir.dx+1][dir.dy+1]<0;
			}
		}
		if (best_gains==CombatInfo.optimal_empower_gains && best_gains>0) {  // use != if we want to force suicide to save friendly units or disallow conversions
			rc.empower(CombatInfo.optimal_empower_radius); Clock.yield(); return;
		}
		if (best_gains>Double.MIN_VALUE || best_gains<-Double.MIN_VALUE) {
			for (Direction dir:Direction.allDirections()) {
				if (dir==Direction.CENTER || rc.canMove(dir)) {
					if (best_gains==move_gains[dir.dx+1][dir.dy+1]) {
						Action.move(dir); return;
					}
				}
			}
		}
		if (best_gains==CombatInfo.optimal_empower_gains && best_gains<0) {  // use != if we want to force suicide to save friendly units or disallow conversions
			return;
		}
		if (Role.is_relay_chain) {  // check if seen a burier
			Role.attach_to_relay_chain();
			if (!Info.everything_buried && Info.n_buriers>0 && !Info.exterminate) {RelayChain.lock_target(Info.weak_burier.location);}
			if (RelayChain.extend(negative_gain_tiles)) {return;}
			return;
		}
		else if (Role.is_guard) {
			Guard.defend(); return;
		}
		else if (Role.is_burier) {
			Burier.bury(negative_gain_tiles); return;
		}
	}
	
	public static void pause() throws GameActionException {
		if (Info.round_num == Info.spawn_round) {
			for (int i=Info.n_friendly_ecs; --i>=0;) {
				if (!Info.loc.isAdjacentTo(Info.friendly_ecs[i].location)) {continue;}
				int flag = rc.getFlag(Info.friendly_ecs[i].ID);
				if ((flag>>17)%4==1) {
					Role.target(new MapLocation(Info.x + ((flag>>11)%64*2-Info.x+12800064)%128-64, Info.y + ((flag>>5)%64*2-Info.y+12800064)%128-64));
					break;
				}
				else if ((flag>>17)%4==3) {
					Role.guard(new MapLocation(Info.x + ((flag>>11)%64*2-Info.x+12800064)%128-64, Info.y + ((flag>>5)%64*2-Info.y+12800064)%128-64));
					break;
				}
			}
		}
	}

}
