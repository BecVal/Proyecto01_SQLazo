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
    public void deposit(double amount) {
        super.deposit(amount);
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
        super.withdraw(REWARDS_FEE, "0000");
        addHistory("Rewards program fee applied: $" + REWARDS_FEE);
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
            notify("Rewards: Earned " + pointsEarned + " points! Total: " + rewardPoints);
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
            super.deposit(cashValue);
            rewardPoints -= points;
            addHistory("Points redeemed: " + points + " for $" + cashValue);
            notify("Rewards: Redeemed " + points + " points for $" + cashValue);
        } else {
            notify("Rewards: Insufficient points. Available: " + rewardPoints);
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