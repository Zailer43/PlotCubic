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
        PlotManager plotManager = PlotManager.getInstance();
        int roadSize = plotManager.getSettings().getRoadSize() + 1;
        int plotSize = plotManager.getSettings().getPlotSize() + 1;
        int offset = roadSize / 2;
        int size = plotSize + roadSize;

        pos -= offset;
        pos = (int) Math.ceil(pos / (float) size);

        return pos;
    }

    public static boolean isValid(String plotIdString) {
        return plotIdString.matches(PlotID.PLOT_ID_REGEX);
    }

    public int getXPos() {
        return this.getPos(this.x);
    }

    public int getZPos() {
        return this.getPos(this.z);
    }

    private int getPos(int id) {
        PlotManager plotManager = PlotManager.getInstance();
        int roadSize = plotManager.getSettings().getRoadSize() + 1;
        int plotSize = plotManager.getSettings().getPlotSize() + 1;
        int size = plotSize + roadSize;

        return id * size;
    }

    public float getSpawnOfX() {
        return this.getSpawnPos(this.x, 0.5f);
    }

    public float getSpawnOfY() {
        return PlotManager.getInstance().getSettings().getMaxHeight() + 1;
    }

    public float getSpawnOfZ() {
        return this.getSpawnPos(this.z, 0f);
    }

    private float getSpawnPos(int id, float inPercentageOfPlot) {
        PlotManager plotManager = PlotManager.getInstance();
        int roadSize = plotManager.getSettings().getRoadSize() + 1;
        int plotSize = plotManager.getSettings().getPlotSize() + 1;
        int size = plotSize + roadSize;

        return this.getPos(--id) + size * inPercentageOfPlot + roadSize - 1;
    }

    public boolean equals(PlotID plotID) {
        return this.x == plotID.x && this.z == plotID.z;
    }

    @Override
    public String toString() {
        return x + String.valueOf(DELIMITER) + z;
    }
}
