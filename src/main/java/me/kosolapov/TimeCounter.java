package me.kosolapov;

/**
 * TimeCounter interface to provide time in form of long values
 * and compare values.
 */
interface TimeCounter {

    /**
     * Gets long value of current timestamp.
     *
     * @return long value of current timestamp
     */
    long getTime();

    /**
     * Determines whether the two timestamps provided lie inside one second period.
     *
     * @param first first timestamp
     * @param second second timestamp
     * @return true if timestamps lie inside one second period, else false
     */
    boolean insideSecond(long first, long second);

    /**
     * Get second as time. The long value that represent a second period.
     */
    long getSecondAsTime();
}
