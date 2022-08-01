package me.zailer.plotcubic.plot;

import me.zailer.plotcubic.PlotManager;

import javax.annotation.Nullable;

public record PlotID(int x, int z) {
    public static final char DELIMITER = ';';
    public static final String PLOT_ID_REGEX = "^(-?\\d+)" + DELIMITER + "(-?\\d+)$";

    @Nullable
    public static PlotID of(String plotIdString) {
        if (!isValid(plotIdString))
            return null;

        String[] plotIdArray = plotIdString.split(String.valueOf(DELIMITER));
        int x = Integer.parseInt(plotIdArray[0]);
        int z = Integer.parseInt(plotIdArray[1]);
        return new PlotID(x, z);
    }

    @Nullable
    public static PlotID ofBlockPos(int x, int z) {
        if (!PlotManager.getInstance().isPlot(x, z))
            return null;

        return new PlotID(ofBlockPos(x), ofBlockPos(z));
    }

    private static int ofBlockPos(int pos) {
        int size = PlotManager.getInstance().getTotalSize();

        pos = (int) Math.ceil(pos / (float) size);

        return pos;
    }

    public static boolean isDifferentPlot(int x, int z, int x2, int z2) {
        return isDifferentPlot(PlotID.ofBlockPos(x, z), x2, z2);
    }

    public static boolean isDifferentPlot(@Nullable PlotID plotId, int x2, int z2) {
        PlotID plotId2 = ofBlockPos(x2, z2);

        return plotId == null || plotId2 == null || !plotId.equals(plotId2);
    }

    public static boolean isValid(String plotIdString) {
        return plotIdString.matches(PlotID.PLOT_ID_REGEX);
    }

    /**
     * @return Returns the coordinate of the plot border to the southwest (negative X and Z)
     */
    public int getXPos() {
        return this.getPos(this.x - 1);
    }

    /**
     * @return Returns the coordinate of the plot border to the southwest (negative X and Z)
     */
    public int getZPos() {
        return this.getPos(this.z - 1);
    }

    private int getPos(int id) {
        return id * PlotManager.getInstance().getTotalSize();
    }

    public float getSpawnOfX() {
        float halfRoad = PlotManager.getInstance().getRoadSize() / 2f;
        return this.getSpawnPos(this.x, 0.5f) - halfRoad;
    }

    public float getSpawnOfY() {
        return PlotManager.getInstance().getMaxTerrainHeight() + 2;
    }

    public float getSpawnOfZ() {
        return this.getSpawnPos(this.z, 0f);
    }

    private float getSpawnPos(int id, float inPercentageOfPlot) {
        int size = PlotManager.getInstance().getTotalSize();

        return this.getPos(--id) + size * inPercentageOfPlot;
    }

    public boolean equals(PlotID plotID) {
        return this.x == plotID.x && this.z == plotID.z;
    }

    @Override
    public String toString() {
        return x + String.valueOf(DELIMITER) + z;
    }
}
