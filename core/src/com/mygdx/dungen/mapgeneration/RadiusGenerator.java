package com.mygdx.dungen.mapgeneration;


import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.Array;
import org.apache.commons.math3.distribution.NormalDistribution;
import org.apache.commons.math3.random.RandomDataGenerator;

public class RadiusGenerator {

    public RadiusGenerator() {}

    /**
     * Generate a number of Radiuses from a normal distribution according to a threshold and clamped to a min/max
     * @param radiusCount Number of samples desired
     * @param minRoomCountMultiplier Min value of sample above threshold
     * @param maxRoomCountMultiplier Max value of sample above threshold
     * @param radiusMultiplier Multiplier to scale to game units
     * @param mean Mean used in the normal distribution
     * @param standardDeviation Standard deviation used in the normal distribution
     * @param threshold Threshold used in the normal distribution (in raw form and not the percentile you want)
     */
    public Array<Radius> generateRadiuses(int radiusCount, float minRoomCountMultiplier, float maxRoomCountMultiplier, float radiusMultiplier, double mean, double standardDeviation, float threshold) throws MapGenException {

        //Setting up MIN and MAX number of rooms
        int minRoomCount = (int) (radiusCount * minRoomCountMultiplier);
        int maxRoomCount = (int) (radiusCount * maxRoomCountMultiplier);

//        if (radiusMultiplier <= 0.00f) throw new IllegalArgumentException("Radius multiplier should be > 0");
        if ((minRoomCount > radiusCount) || (minRoomCount < 0) || (maxRoomCount > radiusCount) || (maxRoomCount < 0)) {
            throw new MapGenException("Impossible min/max room count");
        }

        // Creating normal distribution
        NormalDistribution distribution = new NormalDistribution(mean, standardDeviation);

        int randomRoomCount = new RandomDataGenerator().nextInt(minRoomCount, maxRoomCount);
        int roomCount = 0;

        Array<Radius> radiuses = new Array<Radius>(radiusCount);

        float sample;

        while (roomCount < randomRoomCount) {
            sample = Math.abs((float) distribution.sample());
            boolean isAboveThreshold = sample >= threshold;

            if (isAboveThreshold) {
                roomCount++;
                radiuses.add(new Radius(sample * radiusMultiplier, isAboveThreshold));
            }
        }

        while (radiuses.size < radiusCount) {
            sample = Math.abs((float) distribution.sample());
            boolean isAboveThreshold = sample >= threshold;

            if (!isAboveThreshold) {
                radiuses.add(new Radius(sample * radiusMultiplier, isAboveThreshold));
            }
        }

        Gdx.app.log("RadiusGenerator", "-------- Now generating radiuses --------");
        Gdx.app.log("RadiusGenerator", "Desired number of radiuses: " + radiusCount);
        Gdx.app.log("RadiusGenerator", "Between " + minRoomCount + " and " + maxRoomCount + " will be above threshold");
        Gdx.app.log("RadiusGenerator", "-- Generation DONE -- Radiuses above threshold: " + roomCount);

        return radiuses;
    }

    /**
     * Generate a number of Radiuses from a normal distribution according to a threshold and clamped to a min/max
     * @param radiusCount Number of samples desired
     * @param minRoomCountMultiplier Min value of sample above threshold
     * @param maxRoomCountMultiplier Max value of sample above threshold
     * @param mean Mean used in the normal distribution
     * @param standardDeviation Standard deviation used in the normal distribution
     * @param threshold Threshold used in the normal distribution (in raw form and not the percentile you want)
     */
    public Array<Radius> generateRadiuses(int radiusCount, float minRoomCountMultiplier, float maxRoomCountMultiplier, double mean, double standardDeviation, float threshold) throws MapGenException {
        return this.generateRadiuses(radiusCount, minRoomCountMultiplier, maxRoomCountMultiplier, 1f, mean, standardDeviation, threshold);
    }
}
