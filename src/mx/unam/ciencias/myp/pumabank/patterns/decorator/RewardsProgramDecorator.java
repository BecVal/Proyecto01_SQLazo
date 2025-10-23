package mx.unam.ciencias.myp.pumabank.patterns.decorator;

import mx.unam.ciencias.myp.pumabank.model.IAccount;

/**
 * Decorator class that adds a rewards program to an {@link IAccount}.
 * <p>
 * This class extends {@link AccountDecorator} and implements a rewards program where users earn points for deposits and withdrawals.
 * It also applies a monthly fee for the rewards program.
 * </p>
 */
public class RewardsProgramDecorator extends AccountDecorator {
    
    
    private static final double REWARDS_FEE = 30.0;
    private static final double POINTS_RATE = 0.01;
    private int rewardPoints;

    /**
     * Constructs a {@code RewardsProgramDecorator} that wraps the specified {@link IAccount}.
     *
     * @param decoratedAccount the {@link IAccount} instance to be decorated
     */
    public RewardsProgramDecorator(IAccount decoratedAccount) {
        super(decoratedAccount);
        this.rewardPoints = 0;
    }

    /**
     * Deposits the specified amount into the account and adds reward points.
     *
     * @param amount the amount to deposit
     */
    @Override
    public void deposit(double amount, String pin) {
        super.deposit(amount, pin);
        addRewardPoints(amount);
    }

    /**
     * Withdraws the specified amount from the account and adds reward points.
     *
     * @param amount the amount to withdraw
     * @param pin    the PIN for authentication
     */
    @Override
    public void withdraw(double amount, String pin) {
        super.withdraw(amount, pin);
        addRewardPoints(amount);
    }

    /**
     * Processes the monthly operations, including applying the rewards program fee.
     */
    @Override
    public void processMonth() {
        notify(String.format("SERVICE_FEE_PENDING: Rewards Program - $%.2f | Current Points: %d", 
            REWARDS_FEE, rewardPoints));
        
        super.withdraw(REWARDS_FEE, "SYSTEM");

        recordFee(REWARDS_FEE);

        addHistory("Rewards program fee applied: $" + REWARDS_FEE);
        
        notify(String.format("SERVICE_FEE_APPLIED: Rewards Program - $%.2f | Points Balance: %d", 
            REWARDS_FEE, rewardPoints));
        notify("Rewards program: Monthly fee applied. Current points: " + rewardPoints);
        
        super.processMonth();
    }

    /**
     * Adds reward points based on the transaction amount.
     *
     * @param transactionAmount the amount of the transaction
     */
    private void addRewardPoints(double transactionAmount) {
        int pointsEarned = (int) (transactionAmount * POINTS_RATE);
        rewardPoints += pointsEarned;
        addHistory("Reward points earned: " + pointsEarned + " | Total: " + rewardPoints);
        
        if (pointsEarned > 0) {
            notify(String.format("REWARDS: Earned %d points from transaction of $%.2f | Total: %d points", 
                pointsEarned, transactionAmount, rewardPoints));
        }
    }

    /**
     * Redeems the specified number of reward points for cash.
     *
     * @param points the number of points to redeem
     */
    public void redeemPoints(int points) {
        if (points <= rewardPoints) {
            double cashValue = points * 0.1;
            super.deposit(cashValue, "SYSTEM");
            rewardPoints -= points;
            addHistory("Points redeemed: " + points + " for $" + cashValue);
            notify(String.format("REWARDS_REDEMPTION: %d points redeemed for $%.2f", 
                points, cashValue));
        } else {
            notify(String.format("REWARDS_ERROR: Insufficient points. Available: %d, Requested: %d", 
                rewardPoints, points));
        }
    }

    /**
     * Returns the current number of reward points.
     *
     * @return the current reward points
     */
    public int getRewardPoints() {
        return rewardPoints;
    }
}