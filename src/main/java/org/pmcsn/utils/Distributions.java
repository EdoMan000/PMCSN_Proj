package org.pmcsn.utils;

import org.pmcsn.libraries.Rngs;

public class Distributions {

    public static double erlang(long k, double b, Rngs rngs)
        /* ==================================================
         * Returns an Erlang distributed positive real number.
         * NOTE: use k > 0 and b > 0.0
         * ==================================================
         */
    {
        long   i;
        double x = 0.0;

        for (i = 0; i < k; i++)
            x += exponential(b, rngs);
        return (x);
    }

    public static double exponential(double m, Rngs rngs)
        /* =========================================================
         * Returns an exponentially distributed positive real number.
         * NOTE: use m > 0.0
         * =========================================================
         */
    {
        return (-m * Math.log(1.0 - rngs.random()));
    }

    public static double uniform(double a, double b, Rngs rngs) {
        /* --------------------------------------------
         * generate a Uniform random variate, use a < b
         * --------------------------------------------
         */
        return (a + (b - a) * rngs.random());
    }

    public static double logNormal(double mean, double stdDev, Rngs rngs)
        /* ====================================================
         * Returns a lognormal distributed positive real number.
         * NOTE: use stdDev > 0.0
         * ====================================================
         */
    {
        double sigma = Math.sqrt(Math.log(1 + Math.pow(stdDev / Math.log(mean), 2)));
        double mu = Math.log(mean) - 0.5 * Math.pow(sigma, 2);
        return Math.exp(mu + sigma * normal(0.0, 1.0, rngs));
    }


    public static double normal(double m, double s, Rngs rngs)
        /* ========================================================================
         * Returns a normal (Gaussian) distributed real number.
         * NOTE: use s > 0.0
         *
         * Uses a very accurate approximation of the normal idf due to Odeh & Evans,
         * J. Applied Statistics, 1974, vol 23, pp 96-97.
         * ========================================================================
         */
    {
        final double p0 = 0.322232431088;     final double q0 = 0.099348462606;
        final double p1 = 1.0;                final double q1 = 0.588581570495;
        final double p2 = 0.342242088547;     final double q2 = 0.531103462366;
        final double p3 = 0.204231210245e-1;  final double q3 = 0.103537752850;
        final double p4 = 0.453642210148e-4;  final double q4 = 0.385607006340e-2;
        double u, t, p, q, z;

        u   = rngs.random();
        if (u < 0.5)
            t = Math.sqrt(-2.0 * Math.log(u));
        else
            t = Math.sqrt(-2.0 * Math.log(1.0 - u));
        p   = p0 + t * (p1 + t * (p2 + t * (p3 + t * p4)));
        q   = q0 + t * (q1 + t * (q2 + t * (q3 + t * q4)));
        if (u < 0.5)
            z = (p / q) - t;
        else
            z = t - (p / q);
        return (m + s * z);
    }


}
