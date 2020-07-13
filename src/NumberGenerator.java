public class NumberGenerator {
    private double carInterArrivalTimeMean = 1.3698,
            carVelocityMean = 120.0721,
            carVelocityVariance = 81.3434,
            callDurationMean = 109.8359;

    private double ExponentialRN(double beta) {
        double U = Math.random();
        double ans = (-beta) * Math.log(1 - U);
        return ans;
    }

    private double UniformRN(double a, double b) {
        double U = Math.random();
        return (b - a) * U + a;
    }

    private double NormalRN(double mean, double dev) {
        double U, sum = 0;
        for (int i = 0; i < 12; i++) {
            U = Math.random();
            sum += U;
        }
        double z = sum - 6;
        return z * dev + mean;
    }

    public double carInterArrival() {
        return ExponentialRN(carInterArrivalTimeMean);
    }

    public int baseStation() {
        return (int) Math.ceil(UniformRN(0, 19));
    }

    public double positionInBaseStation() {
        return UniformRN(0, 2);
    }

    public double callDuration() {
        return 10 + ExponentialRN(callDurationMean);
    }

    public double velocity() {
        return NormalRN(carVelocityMean, carVelocityVariance);
    }
}