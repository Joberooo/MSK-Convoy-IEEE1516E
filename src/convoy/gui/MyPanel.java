package convoy.gui;

import convoy.config.Config;

import java.awt.*;

import java.awt.event.*;
import java.util.ArrayList;
import javax.swing.*;

public class MyPanel extends JPanel implements ActionListener{

    final int PANEL_WIDTH = Config.PANEL_WIDTH;
    final int PANEL_HEIGHT = Config.PANEL_HEIGHT;
    Timer timer;
    int vehicleH = 10;
    int vehicleW = 10;
    ArrayList<Float> vehiclesPositionList = new ArrayList<>();
    float proportion;

    MyPanel(){
        this.setPreferredSize(new Dimension(PANEL_WIDTH,PANEL_HEIGHT));
        this.setBackground(Color.WHITE);
        timer = new Timer(10, this);
        timer.start();
    }

    public void paint(Graphics g) {
        super.paint(g);

        Graphics2D g2D = (Graphics2D) g;

        for (float pos : vehiclesPositionList) {
            g2D.setColor(Color.BLACK);
            g2D.fillRect(PANEL_WIDTH / 2, PANEL_HEIGHT - Math.round(pos / proportion), vehicleW, vehicleH);
        }
    }

    public void setVehiclesPosition(ArrayList<Float> positions, float proportion){
        this.vehiclesPositionList = positions;
        this.proportion = proportion;
        this.repaint();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        this.repaint();
    }
}