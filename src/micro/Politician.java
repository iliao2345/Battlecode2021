package micro;
import battlecode.common.*;

public class Politician {
	public static RobotController rc;
	
	public static void act() throws GameActionException {
		CombatInfo.compute_self_empower_gains();
		double best_gains = CombatInfo.optimal_empower_gains;
		RobotInfo largest_nearby_politician = null;
		int largest_conviction = Integer.MIN_VALUE;
		for (int i=Info.n_friendly_politicians; --i>=0;) {
			if (Info.friendly_politicians[i].conviction>largest_conviction) {
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
			for (Direction dir:Direction.allDirections()) {
				if (dir==Direction.CENTER || rc.canMove(dir)) {
					best_gains = Math.max(best_gains, move_gains[dir.dx+1][dir.dy+1]);
					negative_gain_tiles[dir.dx+1][dir.dy+1] = move_gains[dir.dx+1][dir.dy+1]<0;
				}
			}
		}
		else {
			best_gains = Math.max(best_gains, 0);
		}
		if (best_gains==CombatInfo.optimal_empower_gains) {
			System.out.println(CombatInfo.kills_1);
			System.out.println(CombatInfo.kills_2);
			System.out.println(CombatInfo.kills_4);
			System.out.println(CombatInfo.kills_5);
			System.out.println(CombatInfo.kills_8);
			System.out.println(CombatInfo.kills_9);
			System.out.println(CombatInfo.costs_1);
			System.out.println(CombatInfo.costs_2);
			System.out.println(CombatInfo.costs_4);
			System.out.println(CombatInfo.costs_5);
			System.out.println(CombatInfo.costs_8);
			System.out.println(CombatInfo.costs_9);
			rc.empower(CombatInfo.optimal_empower_radius); return;
		}
		if (best_gains!=0) {
			for (Direction dir:Direction.allDirections()) {
				if (dir==Direction.CENTER || rc.canMove(dir)) {
					if (best_gains==move_gains[dir.dx+1][dir.dy+1]) {
						Action.move(dir); return;
					}
				}
			}
		}
		else {
			Role.attach_to_relay_chain();
			if (Info.n_enemy_ecs>0) {
				RelayChain.lock_target(Info.enemy_ecs[0].location);
			}
			if (RelayChain.extend(negative_gain_tiles)) {return;}
			return;
		}
		throw new GameActionException(null, "Best gain action computed but not found!");
	}
	
	public static void pause() {
		
	}

}
