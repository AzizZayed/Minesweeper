package com.codingheaven.main;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JFrame;
import javax.swing.JPanel;

public class GameManager extends JPanel {

	private static final long serialVersionUID = 1L;

	private MinesweeperBoard board;

	public GameManager() {

		initialize();
		canvasSetup();

	}

	private void initialize() {
		board = new MinesweeperBoard();
	}

	private void canvasSetup() {
		Dimension size = new Dimension(board.WIDTH, board.HEIGHT);

		this.setPreferredSize(size);
		this.setMaximumSize(size);
		this.setMinimumSize(size);

		this.setFocusable(true);

		this.addMouseListener(new MouseAdapter() {

			@Override
			public void mouseClicked(MouseEvent e) {
				int x = (int) (e.getPoint().getX());
				int y = (int) (e.getPoint().getY());

				board.clicked(x, y, e.getButton());

				repaint();
			}
		});
		this.addKeyListener(new KeyAdapter() {

			@Override
			public void keyPressed(KeyEvent e) {
				char key = e.getKeyChar();
				
				if (key == 'r')
					initialize();
				
				repaint();
				
			}
		});
	}

	@Override
	public void paintComponent(Graphics g) {
		drawBackground(g);
		drawGame(g);
	}

	private void drawBackground(Graphics g) {
		g.setColor(Color.white);
		g.fillRect(0, 0, board.WIDTH, board.HEIGHT);
	}

	private void drawGame(Graphics g) {
		board.draw(g);
	}

	public static void main(String args[]) {
		JFrame frame = new JFrame("Minesweeper");
		GameManager game = new GameManager();

		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setResizable(false);
		frame.add(game);
		frame.pack();
		frame.setLocationRelativeTo(null);
		frame.setVisible(true);
		game.setDoubleBuffered(true);

		game.repaint();
	}
}
