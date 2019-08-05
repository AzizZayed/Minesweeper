package com.codingheaven.main;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;

public class MinesweeperBoard {

	public final int CELL_SIZE = 50;
	public final int GRID_WIDTH = 8;
	public final int GRID_HEIGHT = 8;
	public final int WIDTH = CELL_SIZE * GRID_WIDTH;
	public final int HEIGHT = CELL_SIZE * GRID_HEIGHT;

	private int nMines = 9;
	private int nClicks = 0;
	private Cell[][] field;
	private boolean lost = false;
	private boolean won = false;

	private static final int kNUM_IMAGES = 8;
	private BufferedImage[] numberImages;
	private BufferedImage mineImg, flagImg, cellImg, emptyImg;

	private class Cell {
		public boolean isCovered = true;
		public boolean isFlagged = false;
		public boolean isMine = false;
		public int nMine = 0;

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

	public MinesweeperBoard() {
		loadImages();
		createField();
	}

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

	private void createField() {
		field = new Cell[GRID_WIDTH][GRID_HEIGHT];

		for (int i = 0; i < GRID_WIDTH; i++) {
			for (int j = 0; j < GRID_HEIGHT; j++) {
				field[i][j] = new Cell();
			}
		}
	}

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

	private void uncoverAll() {
		for (int i = 0; i < GRID_WIDTH; i++) {
			for (int j = 0; j < GRID_HEIGHT; j++) {
				field[i][j].isCovered = false;
			}
		}
	}

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
