package SnakeGame;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.File;
import java.util.LinkedList;
import java.util.Queue;

import javax.swing.ImageIcon;
import javax.swing.JPanel;
import javax.swing.Timer;

public class SnakeBoard extends JPanel implements ActionListener {
    private final int B_WIDTH = 30;
    private final int B_HEIGHT = 30;
    private final int IMG_SIZE = 10;// in px
    private final int MARGIN_SIZE = 10;// in px
    private final int DELAY = 200;
    private final int MAX_BODY_LEN = 50;
    
    private enum Direction {up, down, left, right};
    
    private Timer m_timer;
    private boolean m_paused;
	private boolean m_showGrid = true;

    private int m_bodyLen;
    
    // 0 - space, 1 - snake, 2 - food
    byte [][] m_gameBoard;
    Point m_head;
    Queue <Point> m_body;
    Direction m_currD;
    
    public SnakeBoard() {
        addKeyListener(new GameKeyAdapter());
        setBackground(Color.white);
        setFocusable(true);

        setPreferredSize(new Dimension(B_WIDTH * IMG_SIZE + 2 * MARGIN_SIZE, 
        		                       B_HEIGHT * IMG_SIZE + 2 * MARGIN_SIZE));
        initGame();
        m_timer = new Timer(DELAY, this);
        m_timer.start();
    }

    
    private void initGame() {
    	//m_timer = new Timer(DELAY, this);
        //m_timer.start();
        
    	m_paused = true;
    	m_body = new LinkedList<Point>(); 
    	m_bodyLen = 3;
    	m_gameBoard = new byte [B_HEIGHT][B_WIDTH];
    	m_gameBoard[B_HEIGHT/2][B_WIDTH/2] = 1;
    	m_gameBoard[B_HEIGHT/2][B_WIDTH/2 - 1] = 1;
    	m_gameBoard[B_HEIGHT/2][B_WIDTH/2 - 2] = 1;
    	m_body.offer(new Point(B_WIDTH/2 - 2, B_HEIGHT/2));
    	m_body.offer(new Point(B_WIDTH/2 - 1, B_HEIGHT/2));
    	m_body.offer(new Point(B_WIDTH/2, B_HEIGHT/2));
    	
    	m_head = new Point(B_HEIGHT/2, B_WIDTH/2);
    	m_currD = Direction.right;
        locateFood();
    }
    
    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        
        for (int i = 0; i <= B_HEIGHT*IMG_SIZE; i += 10) {
            g.drawLine(MARGIN_SIZE, i + MARGIN_SIZE, B_WIDTH*IMG_SIZE + MARGIN_SIZE, i + MARGIN_SIZE);
        }
        for (int i = 0; i <= B_WIDTH*IMG_SIZE; i += 10) {
            g.drawLine(i + MARGIN_SIZE, MARGIN_SIZE, i + MARGIN_SIZE, B_HEIGHT*IMG_SIZE + MARGIN_SIZE);
        }
        
        for (int i = 0; i < B_HEIGHT; ++ i) {
        	for (int j = 0; j < B_WIDTH; ++ j) {
        		if (m_gameBoard[i][j] == 1) {
        			if (i == m_head.y && j == m_head.x)
        				fillCell(g, j, i, Color.black);
        			else 
        				fillCell(g, j, i, Color.GRAY);
        			
        		} else if (m_gameBoard[i][j] == 2) {
        			fillCell(g, j, i, Color.green);
        		} else {
        			fillCell(g, j, i, Color.white);
        		}
        	}
        }
        Toolkit.getDefaultToolkit().sync();
        
    }
    private void fillCell(Graphics g, int x, int y, Color c) {
    	g.setColor(c);
    	int t = 0;
    	if (m_showGrid) t = 1;
        g.fillRect(x * IMG_SIZE + MARGIN_SIZE + t, y * IMG_SIZE + MARGIN_SIZE + t, 10 - t, 10 - t);
    }

	
	private void locateFood() {
		int t_x, t_y;
		do {
			t_x = (int) (Math.random() * B_WIDTH);
			t_y = (int) (Math.random() * B_HEIGHT);
		} while (t_x >= B_WIDTH || t_y >= B_HEIGHT || m_gameBoard[t_y][t_x] != 0);
		m_gameBoard[t_y][t_x] = 2;
	}
	
    private void checkFood() {
        if (m_gameBoard[m_head.y][m_head.x] == 2) {
            m_bodyLen++;
            if (m_bodyLen >= MAX_BODY_LEN) {
            	m_paused = true;
            	System.out.println("MAX_LEN: " + MAX_BODY_LEN + " reached!");
            	//initGame();
            } else {
            	m_gameBoard[m_head.y][m_head.x] = 0;
            	locateFood();
            }
            
        }
    }
    private boolean willCollide() {
    	if (m_head.y < 0 || m_head.y >= B_HEIGHT || m_head.x < 0 || m_head.x >= B_WIDTH) {
    		System.out.println("Collision1!");
    		return true;
    	}
    	if (m_gameBoard[m_head.y][m_head.x] == 1)
    		System.out.println("Collision2!");
    	return m_gameBoard[m_head.y][m_head.x] == 1;
    }
    private void move() {
        if (m_currD == Direction.right) m_head.x ++;
        else if (m_currD == Direction.left) m_head.x--;
        else if (m_currD == Direction.up) m_head.y ++;
        else if (m_currD == Direction.down) m_head.y --;
        
        if (willCollide()) {
        	System.out.println("Collision!");
        	initGame();
        } else {
        	checkFood();
        	if (m_bodyLen <= m_body.size()) {
            	Point tail = m_body.poll();
            	m_gameBoard[tail.y][tail.x] = 0;
            }
        	m_body.offer(new Point(m_head.x, m_head.y));
        	m_gameBoard[m_head.y][m_head.x] = 1;
        }
    }
	
	@Override
	public void actionPerformed(ActionEvent e) {
		// TODO Auto-generated method stub
		//System.out.println(m_paused);
		if (!m_paused) {
            move();
        }
        repaint();
	}
	
	private class GameKeyAdapter extends KeyAdapter {
        @Override
        public void keyPressed(KeyEvent e) {

            int key = e.getKeyCode();
            if (key == KeyEvent.VK_ENTER)
            	m_paused = false;
            else if (key == KeyEvent.VK_ESCAPE)
            	m_paused = true;
            else if (key == KeyEvent.VK_G)
            	m_showGrid = !m_showGrid;
            else {
	            if ((key == KeyEvent.VK_LEFT) && (m_currD != Direction.right))
	            	m_currD = Direction.left;
	
	            if ((key == KeyEvent.VK_RIGHT) && (m_currD != Direction.left))
	            	m_currD = Direction.right;
	
	            if ((key == KeyEvent.VK_UP) && (m_currD != Direction.up))
	            	m_currD = Direction.down;
	            	
	            if ((key == KeyEvent.VK_DOWN) && (m_currD != Direction.down))
	            	m_currD = Direction.up;
            }
        }
    }
}
