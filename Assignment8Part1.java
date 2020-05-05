package com.shpp.p2p.cs.vshtuka.assignment8;

import acm.graphics.GObject;
import acm.graphics.GOval;
import com.shpp.cs.a.graphics.WindowProgram;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.util.ArrayList;

/**
 * Assignment8Part1 class consists window program in which when we clicked mouse,
 * program draws ball and starts simulation of balls bouncing (it could be many balls),
 * while holding the mouse button for a long time, the ball grows and darkens,
 * when we click on the created one, it bounces to the ceiling
 */
public class Assignment8Part1 extends WindowProgram {
    /**
     * Width and height of application window in pixels
     */
    public static final int APPLICATION_WIDTH = 1000;
    public static final int APPLICATION_HEIGHT = 600;

    /**
     * Maximal diameter of the ball in pixels
     */
    private static final int BALL_MAX_SIZE = 100;

    /**
     * The amount of pixels that ball grows for the one turn of the loop
     */
    private static final int BALL_GROW = 2;

    /**
     * Start number of each rgb component of the ball color
     */
    private static final int BALL_START_COLOR = 250;

    /**
     * Number of decreases of each rgb component of the ball color
     */
    private static final int BALL_COLOR_CHANGE = BALL_START_COLOR / BALL_MAX_SIZE * BALL_GROW;

    /**
     * Gravitational acceleration.
     */
    private static final double GRAVITY = 0.425;

    /**
     * Elasticity.
     */
    private static final double ELASTICITY = 0.75;

    /**
     * The amount of time to pause between frames
     */
    private static final int PAUSE_TIME = 15;

    /**
     * List for storing delta-y for falling balls
     */
    private ArrayList<Double> dyFalling = new ArrayList<>();

    /**
     * List for storing ball direction as 1 (falling) or -1 (jumping)
     */
    private ArrayList<Integer> dirFalling = new ArrayList<>();

    /**
     * List for storing delta-y for jumping balls
     *
    private ArrayList<Double> dyJumping = new ArrayList<>();*/

    /**
     * List for storing all balls that falling down
     */
    private ArrayList<GOval> fallingBalls = new ArrayList<>();

    /**
     * List for storing all balls that jumping to cell
     *
    private ArrayList<GOval> jumpingBalls = new ArrayList<>();*/

    /**
     * Variable for storing ball that growing when we pressed mouse
     */
    private GOval growingBall;

    /**
     * Determines program entry point
     */
    public void run() {
        addMouseListeners();
        bounceAndTransformBall();
    }

    /**
     * Simulates a bouncing ball.
     */
    private void bounceAndTransformBall() {
        double dx = 0.0;

        /* endless loop which controls the state of the ball */
        while (true) {

            /* holding the mouse button increases the size of the ball and changes its color */
            if (growingBall != null && growingBall.getHeight() < BALL_MAX_SIZE) {
                growingBall.setSize(growingBall.getWidth() + BALL_GROW, growingBall.getHeight() + BALL_GROW);
                growingBall.setLocation(growingBall.getX() - BALL_GROW / 2.0, growingBall.getY() - BALL_GROW / 2.0);
                growingBall.setColor(new Color(growingBall.getFillColor().getRed() - BALL_COLOR_CHANGE,
                                                growingBall.getFillColor().getGreen() - BALL_COLOR_CHANGE,
                                                 growingBall.getFillColor().getBlue() - BALL_COLOR_CHANGE));
            }

            /* controls balls falling to the floor */
            for (int i = 0; i < fallingBalls.size(); i++) {
                GOval currentBall = fallingBalls.get(i);
                currentBall.move(dx, dirFalling.get(i) * dyFalling.get(i));
                dyFalling.set(i, dyFalling.get(i) + GRAVITY); // sets delta-y for current ball
                /* if the ball drops below the floor, we turn it around */
                if (((ballBelowFloor(currentBall) && dirFalling.get(i) == 1) ||
                        (ballUnderCell(currentBall) && dirFalling.get(i) == -1)) &&
                        dyFalling.get(i) > 0) {
                    dyFalling.set(i, dyFalling.get(i) * (-ELASTICITY));
                }
            }

            pause(PAUSE_TIME);
        }
    }

    /**
     * Controls the creation of the ball and changing the direction of the ball by mouse
     *
     * @param mouseEvent is mouse
     */
    @Override
    public void mousePressed(MouseEvent mouseEvent) {
        GObject selectedBall = getElementAt(mouseEvent.getX(), mouseEvent.getY());
        int ballIndex = fallingBalls.indexOf(selectedBall);
        if (growingBall == null && selectedBall == null) {
            /* if we click on an empty place */
            createBall(mouseEvent.getX(), mouseEvent.getY());
        } else {
            dirFalling.set(ballIndex, dirFalling.get(ballIndex) * -1);
        }

    }

    /**
     * When mouse is released, adds ball and delta-y of that ball to lists
     *
     * @param mouseEvent is mouse
     */
    public void mouseReleased(MouseEvent mouseEvent) {
        if (growingBall != null) {
            fallingBalls.add(growingBall);
            dyFalling.add(0.0);
            dirFalling.add(1);
            growingBall = null;
        }
    }

    /**
     * Creates an oval-shaped ball and adds it to the canvas
     *
     * @param xPosition is x ball position on the canvas
     * @param yPosition is y ball position on the canvas
     */
    private void createBall(int xPosition, int yPosition) {
        growingBall = new GOval(xPosition, yPosition, 1, 1);
        growingBall.setFilled(true);
        growingBall.setColor(new Color(BALL_START_COLOR, BALL_START_COLOR, BALL_START_COLOR));
        add(growingBall);
    }

    /**
     * Determines whether the ball has jumped above the cell.
     *
     * @param ball The ball to test.
     * @return Whether it's fallen jumped above the cell.
     */
    private boolean ballUnderCell(GOval ball) {
        return ball.getY() <= 0;
    }

    /**
     * Determines whether the ball has dropped below floor level.
     *
     * @param ball The ball to test.
     * @return Whether it's fallen below the floor.
     */
    private boolean ballBelowFloor(GOval ball) {
        return ball.getY() + ball.getHeight() >= getHeight();
    }
}