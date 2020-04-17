package com.codingheaven.main;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;

/**
 * the actual game class
 * 
 * @author Zayed
 *
 */
public class MinesweeperBoard {

	private final int CELL_SIZE = 50; // size of a single cell in pixels
	private final int GRID_WIDTH = 8; // width of a board in cells
	private final int GRID_HEIGHT = 8; // height of a board in cells
	private final int WIDTH = CELL_SIZE * GRID_WIDTH; // width of the board in pixels
	private final int HEIGHT = CELL_SIZE * GRID_HEIGHT;// height of the board in pixels

	private int nMines = 9; // number of mines
	private int nClicks = 0; // number of clicks done by the user
	private Cell[][] field; // data structure to store properties of each cell
	private boolean lost = false; // did the player lose?
	private boolean won = false; // did the player win?

	private static final int kNUM_IMAGES = 8; // number of images of the digits from 1 to 8
	private BufferedImage[] numberImages; // all the images of the digits from 1 to 8
	private BufferedImage mineImg, flagImg, cellImg, emptyImg; // other images

	/**
	 * Class to contain properties and methods for each cell
	 * 
	 * @author Zayed
	 *
	 */
	private class Cell {
		public boolean isCovered = true; // if it is clicked
		public boolean isFlagged = false; // if it is flagged
		public boolean isMine = false; // if it is a mine
		public int nMine = 0; // the number of neighboring mines

		/**
		 * Draw the cell
		 * 
		 * @param g - tool to draw
		 * @param i - cell horizontal position in data structure (array)
		 * @param j - cell vertical position in data structure (array)
		 */
		public void draw(Graphics g, int i, int j) {
			BufferedImage img;

			int x = i * CELL_SIZE;
			int y = j * CELL_SIZE;

			if (isCovered) {

				if (isFlagged)
					img = flagImg;
				else
					img = cellImg;

			} else {
				g.drawImage(emptyImg, x, y, CELL_SIZE, CELL_SIZE, null);

				if (isMine) {
					img = mineImg;
				} else {
					if (nMine <= 0 || nMine > kNUM_IMAGES)
						return;
					img = numberImages[nMine - 1];
				}
			}

			g.drawImage(img, x, y, CELL_SIZE, CELL_SIZE, null);

		}
	}

	/**
	 * Constructor
	 */
	public MinesweeperBoard() {
		loadImages();
		createField();
	}

	/**
	 * @return the WIDTH
	 */
	public int getWidth() {
		return WIDTH;
	}

	/**
	 * @return the HEIGHT
	 */
	public int getHeight() {
		return HEIGHT;
	}

	/**
	 * load all the images from the res folder
	 */
	private void loadImages() {
		numberImages = new BufferedImage[kNUM_IMAGES];

		for (int i = 0; i < kNUM_IMAGES; i++) {
			try {
				numberImages[i] = ImageIO.read(new File("res/numbers/num" + (i + 1) + ".png"));
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		try {
			flagImg = ImageIO.read(new File("res/sprites/flag.png"));
			mineImg = ImageIO.read(new File("res/sprites/mine.png"));
			cellImg = ImageIO.read(new File("res/sprites/tile.png"));
			emptyImg = ImageIO.read(new File("res/sprites/emptytile.png"));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * create the mine field, initialize each cell in the data structure
	 */
	private void createField() {
		field = new Cell[GRID_WIDTH][GRID_HEIGHT];

		for (int i = 0; i < GRID_WIDTH; i++) {
			for (int j = 0; j < GRID_HEIGHT; j++) {
				field[i][j] = new Cell();
			}
		}
	}

	/**
	 * Create the field after the first click, to make sure you don't click a mine
	 * on your first try
	 *
	 * @param k - horizontal position of the first click
	 * @param w - vertical position of the first click
	 */
	private void initializeField(int k, int w) {
		field[k][w].nMine = 0;

		int i = 0, j = 0, mines = 0;
		while (mines < nMines) {
			i = (int) (Math.random() * GRID_WIDTH);
			j = (int) (Math.random() * GRID_HEIGHT);

			Cell cell = field[i][j];
			if (!cell.isMine && i != k && j != w) {
				cell.isMine = true;
				cell.nMine = -1;
				mines++;
			}
		}

		for (i = 0; i < GRID_WIDTH; i++) {
			for (j = 0; j < GRID_HEIGHT; j++) {
				Cell cell = field[i][j];
				if (!cell.isMine)
					cell.nMine = countNeighbors(i, j);
			}
		}
	}

	/**
	 * Count how many neighbors of the current cell are mines
	 * 
	 * @param i, current cell horizontal position
	 * @param j, current cell vertical position
	 * @return the number of neighboring mines
	 */
	private int countNeighbors(int i, int j) {
		int n = 0;

		for (int a = -1; a <= 1; a++) {
			for (int b = -1; b <= 1; b++) {
				int c = i + a;
				int d = j + b;
				if (c >= 0 && d >= 0 && c < GRID_WIDTH && d < GRID_HEIGHT)
					if (field[c][d].isMine)
						n++;
			}
		}

		return n;
	}

	/**
	 * the board is clicked, process the click
	 * 
	 * @param x           - pixel position of mouse click
	 * @param y           - pixel position of mouse click
	 * @param mouseButton - info on which button is clicked
	 */
	public void clicked(int x, int y, int mouseButton) {
		if (lost || won)
			return;

		nClicks++;

		int i = x / CELL_SIZE;
		int j = y / CELL_SIZE;

		Cell cell = field[i][j];

		if (mouseButton == MouseEvent.BUTTON1) {
			// left click
			if (nClicks == 1)
				initializeField(i, j);
			if (cell.isMine && !cell.isFlagged) {
				uncoverAll();
				lost = true;
			} else {
				uncover(i, j);
			}

		} else if (mouseButton == MouseEvent.BUTTON3) {
			// right click
			cell.isFlagged = !cell.isFlagged;
		}

		checkWin();

	}

	/**
	 * Check if the player won
	 */
	private void checkWin() {
		int nFlaggedMines = 0;
		int nVisible = 0;

		for (int i = 0; i < GRID_WIDTH; i++) {
			for (int j = 0; j < GRID_HEIGHT; j++) {
				Cell cell = field[i][j];

				if (cell.isMine && cell.isFlagged)
					nFlaggedMines++;

				if (!cell.isMine && !cell.isCovered)
					nVisible++;
			}
		}

		won = (nFlaggedMines == nMines && nVisible == GRID_WIDTH * GRID_HEIGHT - nFlaggedMines);
	}

	/**
	 * Uncover all the cells
	 */
	private void uncoverAll() {
		for (int i = 0; i < GRID_WIDTH; i++) {
			for (int j = 0; j < GRID_HEIGHT; j++) {
				field[i][j].isCovered = false;
			}
		}
	}

	/**
	 * Uncover all the non-mine cells around a current cell, recursive algorithm
	 * 
	 * @param i - current cell horizontal position
	 * @param j - current cell vertical position
	 */
	private void uncover(int i, int j) {
		field[i][j].isCovered = false;

		if (field[i][j].nMine == 0) {
			for (int a = -1; a <= 1; a++) {
				for (int b = -1; b <= 1; b++) {
					int c = i + a;
					int d = j + b;
					if (c >= 0 && d >= 0 && c < GRID_WIDTH && d < GRID_HEIGHT) {
						Cell cell = field[c][d];

						if (cell.isCovered)
							uncover(c, d);
					}

				}
			}
		}
	}

	/**
	 * Draw the game
	 * 
	 * @param g - tool to draw
	 */
	public void draw(Graphics g) {

		// draw field
		for (int i = 0; i < GRID_WIDTH; i++) {
			for (int j = 0; j < GRID_HEIGHT; j++) {
				field[i][j].draw(g, i, j);
			}
		}

		if (!lost && !won)
			return;

		// draw lose or win string
		String message = "";
		if (lost && won)
			message = "Problem.";
		if (lost) {
			message = "You died.";
		} else if (won) {
			message = "You won!";
		}

		int size = CELL_SIZE * 2;
		Font font = new Font("Times New Roman", Font.BOLD, size);
		int strW = g.getFontMetrics(font).stringWidth(message);

		g.setColor(new Color(150, 150, 150, 150));
		g.fillRect(0, 0, WIDTH, HEIGHT);

		g.setColor(Color.BLACK);
		g.setFont(font);
		g.drawString(message, WIDTH / 2 - strW / 2, HEIGHT / 2 + size / 2);
	}

}
