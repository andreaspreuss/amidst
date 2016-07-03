package amidst.mojangapi.world.filter;

import java.util.List;

import amidst.mojangapi.world.World;

public class WorldFilter_MatchAll extends WorldFilter {
	private final List<WorldFilter> filters;

	public WorldFilter_MatchAll(long worldFilterDistance, List<WorldFilter> filters) {
		super(worldFilterDistance);
		this.filters = filters;
	}

	@Override
	protected boolean isValid(World world, short[][] region) {
		for (WorldFilter filter : filters) {
			if (!filter.isValid(world)) {
				return false;
			}
		}
		return true;
	}
}
