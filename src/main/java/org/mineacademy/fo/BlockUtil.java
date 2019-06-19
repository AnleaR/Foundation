package org.mineacademy.fo;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.util.Vector;
import org.mineacademy.fo.remain.CompMaterial;
import org.mineacademy.fo.remain.Remain;

import com.google.common.collect.Sets;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;

/**
 * Utility class for block manipulation.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class BlockUtil {

	/**
	 * Matches all DOUBLE or STEP block names
	 */
	private static final Pattern SLAB_PATTERN = Pattern.compile("(?!DOUBLE).*STEP");

	/**
	 * A list of safe blocks which can be selected as a region point, cornerstone,
	 * monster spawner etc.
	 */
	private static final Set<String> SELECTION_BLOCKS = Sets.newHashSet(
			"STONE", "GRASS", "DIRT", "WOOD", "SAND", "GRAVEL", "BEDROCK",
			"LOG", "LEAVES", "BLOCK", "WOOL", "DOUBLE", "SLAB", "STEP",
			"BRICK", "BOOKSHELF", "SOIL", "ORE", "ICE", "CLAY", "PUMPKIN",
			"NETHERRACK", "OBSIDIAN", "GLASS", "FENCE", "REDSTONE_LAMP",
			"TERRACOTTA", "PRISMARINE", "SEA_LANTERN");

	/**
	 * The vertical gaps when creating locations for a bounding box,
	 * see {@link #getBoundingBox(Location, Location)}
	 */
	public static double BOUNDING_VERTICAL_GAP = 1;

	/**
	 * The horizontal gaps when creating locations for a bounding box,
	 * see {@link #getBoundingBox(Location, Location)}
	 */
	public static double BOUNDING_HORIZONTAL_GAP = 1;

	// ------------------------------------------------------------------------------------------------------------
	// Block manipulation
	// ------------------------------------------------------------------------------------------------------------

	/**
	 * Remove the block if player has creative, otherwise, drop it.
	 *
	 * @param player
	 * @param block
	 */
	public static void destroyBlockSurvival(Player player, Block block) {
		if (player.getGameMode() != GameMode.CREATIVE)
			block.breakNaturally();
		else
			block.setType(Material.AIR);
	}

	// ------------------------------------------------------------------------------------------------------------
	// Cuboid region manipulation
	// ------------------------------------------------------------------------------------------------------------

	/**
	 * Returns true if the given location is within the two cuboid bounds
	 *
	 * @param location
	 * @param primary
	 * @param secondary
	 * @return
	 */
	public static boolean isWithinCuboid(Location location, Location primary, Location secondary) {
		return isWithinCuboid(location, primary.toVector(), secondary.toVector());
	}

	/**
	 * Returns true if the given location is within the two vector cuboid bounds
	 *
	 * @param location
	 * @param primary
	 * @param secondary
	 * @return
	 */
	public static boolean isWithinCuboid(Location location, Vector primary, Vector secondary) {
		final double locX = location.getX();
		final double locY = location.getY();
		final double locZ = location.getZ();

		final int x = primary.getBlockX();
		final int y = primary.getBlockY();
		final int z = primary.getBlockZ();

		final int x1 = secondary.getBlockX();
		final int y1 = secondary.getBlockY();
		final int z1 = secondary.getBlockZ();

		if (locX >= x && locX <= x1 || locX <= x && locX >= x1)
			if (locZ >= z && locZ <= z1 || locZ <= z && locZ >= z1)
				if (locY >= y && locY <= y1 || locY <= y && locY >= y1)
					return true;

		return false;
	}

	/**
	 * Return locations representing the bounding box of a cuboid region,
	 * used when rendering particle effects
	 *
	 * @param primary
	 * @param secondary
	 * @return
	 */
	public static Collection<Location> getBoundingBox(Location primary, Location secondary) {
		final List<VectorHelper> ShapeVectors = new ArrayList<>();

		final VectorHelper min = getMinimumPoint(primary, secondary);
		final VectorHelper max = getMaximumPoint(primary, secondary).add(1, 0, 1);

		final int height = getHeight(primary, secondary);

		final List<VectorHelper> bottomCorners = new ArrayList<>();

		bottomCorners.add(new VectorHelper(min.getX(), min.getY(), min.getZ()));
		bottomCorners.add(new VectorHelper(max.getX(), min.getY(), min.getZ()));
		bottomCorners.add(new VectorHelper(max.getX(), min.getY(), max.getZ()));
		bottomCorners.add(new VectorHelper(min.getX(), min.getY(), max.getZ()));

		for (int i = 0; i < bottomCorners.size(); i++) {
			final VectorHelper p1 = bottomCorners.get(i);
			final VectorHelper p2 = i + 1 < bottomCorners.size() ? (VectorHelper) bottomCorners.get(i + 1) : (VectorHelper) bottomCorners.get(0);

			final VectorHelper p3 = p1.add(0, height, 0);
			final VectorHelper p4 = p2.add(0, height, 0);
			ShapeVectors.addAll(plotLine(p1, p2));
			ShapeVectors.addAll(plotLine(p3, p4));
			ShapeVectors.addAll(plotLine(p1, p3));

			for (double offset = BOUNDING_VERTICAL_GAP; offset < height; offset += BOUNDING_VERTICAL_GAP) {
				final VectorHelper p5 = p1.add(0.0D, offset, 0.0D);
				final VectorHelper p6 = p2.add(0.0D, offset, 0.0D);
				ShapeVectors.addAll(plotLine(p5, p6));
			}
		}

		final List<Location> locations = new ArrayList<>();

		for (final VectorHelper ShapeVector : ShapeVectors)
			locations.add(new Location(primary.getWorld(), ShapeVector.getX(), ShapeVector.getY(), ShapeVector.getZ()));

		return locations;
	}

	private static List<VectorHelper> plotLine(VectorHelper p1, VectorHelper p2) {
		final List<VectorHelper> ShapeVectors = new ArrayList<>();

		final int points = (int) (p1.distance(p2) / BOUNDING_HORIZONTAL_GAP) + 1;
		final double length = p1.distance(p2);
		final double gap = length / (points - 1);

		final VectorHelper gapShapeVector = p2.subtract(p1).normalize().multiply(gap);

		for (int i = 0; i < points; i++) {
			final VectorHelper currentPoint = p1.add(gapShapeVector.multiply(i));

			ShapeVectors.add(currentPoint);
		}

		return ShapeVectors;
	}

	// ------------------------------------------------------------------------------------------------------------
	// Getting blocks within a cuboid
	// ------------------------------------------------------------------------------------------------------------

	/**
	 * Returns all blocks within the two cuboid bounds (may take a while)
	 *
	 * @param primary
	 * @param secondary
	 * @return
	 */
	public static Block[] getBlocks(final Location primary, final Location secondary) {
		Valid.checkNotNull(primary, "Primary region point must be set!");
		Valid.checkNotNull(secondary, "Secondary region point must be set!");

		final List<Block> blocks = new ArrayList<>();

		final int topBlockX = primary.getBlockX() < secondary.getBlockX() ? secondary.getBlockX() : primary.getBlockX();
		final int bottomBlockX = primary.getBlockX() > secondary.getBlockX() ? secondary.getBlockX() : primary.getBlockX();

		final int topBlockY = primary.getBlockY() < secondary.getBlockY() ? secondary.getBlockY() : primary.getBlockY();
		final int bottomBlockY = primary.getBlockY() > secondary.getBlockY() ? secondary.getBlockY() : primary.getBlockY();

		final int topBlockZ = primary.getBlockZ() < secondary.getBlockZ() ? secondary.getBlockZ() : primary.getBlockZ();
		final int bottomBlockZ = primary.getBlockZ() > secondary.getBlockZ() ? secondary.getBlockZ() : primary.getBlockZ();

		for (int x = bottomBlockX; x <= topBlockX; x++)
			for (int z = bottomBlockZ; z <= topBlockZ; z++)
				for (int y = bottomBlockY; y <= topBlockY; y++) {
					final Block block = primary.getWorld().getBlockAt(x, y, z);

					if (block != null)
						blocks.add(block);
				}

		return blocks.toArray(new Block[blocks.size()]);
	}

	/**
	 * Get all the blocks in a specific area centered around the Location passed in
	 *
	 * @param loc    Center of the search area
	 * @param height how many blocks up to check
	 * @param radius of the search (cubic search radius)
	 * @param type   of Material to search for
	 *
	 * @return all the Block with the given Type in the specified radius
	 */
	public static Block[] getBlocks(Location loc, int height, int radius) {
		final List<Block> blocks = new ArrayList<>();

		for (int y = 0; y < height; y++)
			for (int x = -radius; x <= radius; x++)
				for (int z = -radius; z <= radius; z++) {
					final Block checkBlock = loc.getBlock().getRelative(x, y, z);

					if (checkBlock != null && checkBlock.getType() != Material.AIR)
						blocks.add(checkBlock);
				}
		return blocks.toArray(new Block[blocks.size()]);
	}

	// ------------------------------------------------------------------------------------------------------------
	// Block type checkers
	// ------------------------------------------------------------------------------------------------------------

	/**
	 * Returns true whether the block is suitable for setting as cornerstone,
	 * foundation, region point or other key points. Also checks the player click
	 * action.
	 *
	 * @param clicked
	 * @param action
	 * @return
	 */
	public static boolean canSetup(Block clicked, Action action) {
		return (action == Action.LEFT_CLICK_BLOCK || action == Action.RIGHT_CLICK_BLOCK) && isForBlockSelection(clicked.getType());
	}

	/**
	 * Will a FallingBlock which lands on this Material break and drop to the
	 * ground?
	 *
	 * @param material to check
	 *
	 * @return boolean
	 */
	public static boolean isBreakingFallingBlock(Material material) {
		return material.isTransparent() &&
				material != CompMaterial.NETHER_PORTAL.getMaterial() &&
				material != CompMaterial.END_PORTAL.getMaterial() ||
				material == CompMaterial.COBWEB.getMaterial() ||
				material == Material.DAYLIGHT_DETECTOR ||
				CompMaterial.isTrapDoor(material) ||
				material == CompMaterial.SIGN.getMaterial() ||
				CompMaterial.isWallSign(material) ||
				// Match all slabs besides double slab
				SLAB_PATTERN.matcher(material.name()).matches();
	}

	/**
	 * Return true when the given material is a tool, e.g. doesn't stack
	 *
	 * @param material
	 * @return
	 */
	public static boolean isTool(Material material) {
		return material.name().endsWith("AXE") // axe & pickaxe
				|| material.name().endsWith("SPADE")
				|| material.name().endsWith("SWORD")
				|| material.name().endsWith("HOE")
				|| material.name().endsWith("BUCKET") // water, milk, lava,..
				|| material == Material.BOW
				|| material == Material.FISHING_ROD
				|| material == Remain.getMaterial("CLOCK", "WATCH")
				|| material == Material.COMPASS
				|| material == Material.FLINT_AND_STEEL;
	}

	/**
	 * Return true if the material is an armor
	 *
	 * @param material
	 * @return
	 */
	public static boolean isArmor(Material material) {
		return material.name().endsWith("HELMET")
				|| material.name().endsWith("CHESTPLATE")
				|| material.name().endsWith("LEGGINGS")
				|| material.name().endsWith("BOOTS");
	}

	/**
	 * Returns true if block is safe to select, see {@link #SELECTION_BLOCKS}
	 *
	 * @param material the material
	 * @return if block contains the partial name of {@link #SELECTION_BLOCKS}
	 */
	public static boolean isForBlockSelection(Material material) {
		if (!material.isBlock() || material == Material.AIR)
			return false;

		final String name = material.toString().toUpperCase();

		for (final String allowed : SELECTION_BLOCKS)
			if (name.contains(allowed))
				return true;

		return false;
	}

	// ------------------------------------------------------------------------------------------------------------
	// Finding highest blocks
	// ------------------------------------------------------------------------------------------------------------

	/**
	 * Scan the location from top to bottom to find the highest Y coordinate that is not air and not snow.
	 * This will return the free coordinate above the snow layer.
	 *
	 * @param loc
	 * @return
	 */
	public static int findHighestBlockNoSnow(Location loc) {
		return findHighestBlockNoSnow(loc.getWorld(), loc.getBlockX(), loc.getBlockZ());
	}

	/**
	 * Scan the location from top to bottom to find the highest Y coordinate that is not air and not snow.
	 * This will return the free coordinate above the snow layer.
	 *
	 * @param world
	 * @param x
	 * @param z
	 * @return
	 */
	public static int findHighestBlockNoSnow(World world, int x, int z) {
		for (int y = world.getMaxHeight(); y > 0; y--) {
			final Block block = world.getBlockAt(x, y, z);

			if (block != null && !CompMaterial.isAir(block) && block.getType() != Material.SNOW)
				return y + 1;
		}

		return -1;
	}

	// ------------------------------------------------------------------------------------------------------------
	// Helper classes
	// ------------------------------------------------------------------------------------------------------------

	private static VectorHelper getMinimumPoint(Location pos1, Location pos2) {
		return new VectorHelper(Math.min(pos1.getX(), pos2.getX()), Math.min(pos1.getY(), pos2.getY()), Math.min(pos1.getZ(), pos2.getZ()));
	}

	private static VectorHelper getMaximumPoint(Location pos1, Location pos2) {
		return new VectorHelper(Math.max(pos1.getX(), pos2.getX()), Math.max(pos1.getY(), pos2.getY()), Math.max(pos1.getZ(), pos2.getZ()));
	}

	private static int getHeight(Location pos1, Location pos2) {
		final VectorHelper min = getMinimumPoint(pos1, pos2);
		final VectorHelper max = getMaximumPoint(pos1, pos2);

		return (int) (max.getY() - min.getY() + 1.0D);
	}

	@RequiredArgsConstructor
	private final static class VectorHelper {

		@Getter
		protected final double x, y, z;

		public VectorHelper add(VectorHelper other) {
			return add(other.x, other.y, other.z);
		}

		public VectorHelper add(double x, double y, double z) {
			return new VectorHelper(this.x + x, this.y + y, this.z + z);
		}

		public VectorHelper subtract(VectorHelper other) {
			return subtract(other.x, other.y, other.z);
		}

		public VectorHelper subtract(double x, double y, double z) {
			return new VectorHelper(this.x - x, this.y - y, this.z - z);
		}

		public VectorHelper multiply(double n) {
			return new VectorHelper(this.x * n, this.y * n, this.z * n);
		}

		public VectorHelper divide(double n) {
			return new VectorHelper(x / n, y / n, z / n);
		}

		public double length() {
			return Math.sqrt(x * x + y * y + z * z);
		}

		public double distance(VectorHelper other) {
			return Math.sqrt(Math.pow(other.x - x, 2) +
					Math.pow(other.y - y, 2) +
					Math.pow(other.z - z, 2));
		}

		public VectorHelper normalize() {
			return divide(length());
		}

		@Override
		public boolean equals(Object obj) {
			if (!(obj instanceof VectorHelper))
				return false;

			final VectorHelper other = (VectorHelper) obj;
			return other.x == this.x && other.y == this.y && other.z == this.z;
		}

		@Override
		public String toString() {
			return "(" + x + ", " + y + ", " + z + ")";
		}
	}
}
