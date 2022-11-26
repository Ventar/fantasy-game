package mro.fantasy.applications.simulator.board;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;


/**
 * JFrame that contains all fields of the board.
 *
 * @author Michael Rodenbuecher
 * @since 2022-08-21
 */
@Component
public class BoardFrame extends JFrame {

    /**
     * Logger.
     */
    private static final Logger LOG = LoggerFactory.getLogger(BoardFrame.class);

    /**
     * The device ID. For real devices this is the MAC address.
     */
    @Value("${game.device.id}")
    private String deviceId;

    /**
     * The X location of the board frame on the screen,measured from theupper left corner of the screen.
     */
    @Value("${board.location.x:0}")
    private int locationX;

    /**
     * The X location of the board frame on the screen,measured from theupper left corner of the screen.
     */
    @Value("${board.location.y:0}")
    private int locationY;

    /**
     * The model data.
     */
    @Autowired
    private BoardModel model;

    /**
     * The last polygon where the mouse pointed to.
     */
    private Polygon lastPolygon;

    /**
     * Creates the Frame and shows it by calling the {@link #createAndShowGUI()} method on the swing dispatcher thread.
     */
    @PostConstruct
    private void postConstruct() {

        try {
            UIManager.setLookAndFeel("com.sun.java.swing.plaf.nimbus.NimbusLookAndFeel");
        } catch (Exception ignored) {}

        SwingUtilities.invokeLater(this::createAndShowGUI);
        setName("BoardModuleFrame");
    }

    /**
     * Creates the frame and shows it.
     */
    private void createAndShowGUI() {

        setTitle("Board Module " + deviceId);
        setResizable(false);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocation(locationX, locationY);
        setPreferredSize(new Dimension(Configuration.COLUMNS * (Configuration.FIELD_SIZE_PX + 2) + 3, Configuration.ROWS * (Configuration.FIELD_SIZE_PX + 2) + 25));
        setContentPane(new PaintPane());

        addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                // LOG.debug("{} - ({}|{})  ({}|{})", frame.getInsets(), e.getX(), e.getY(), e.getPoint().x, e.getPoint().y);

                var polygon = model.resolve(e.getX() - getInsets().left, e.getY() - getInsets().top);

                if (polygon == null && lastPolygon != null) {
                    lastPolygon = null;
                    repaint();
                } else if (polygon != null && !polygon.equals(lastPolygon)) {
                    //                    LOG.debug("New Polygon: {} ", polygon.getBounds());
                    lastPolygon = polygon;
                    repaint();
                }
            }
        });

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {

                var column = (e.getX() - getInsets().left) / (Configuration.FIELD_SIZE_PX);
                var row = (e.getY() - getInsets().top) / (Configuration.FIELD_SIZE_PX);

                LOG.debug("Mouse clicked {}, coordinates ({}|{})", e.getPoint(), column, row);

                model.get(column, row).onClick(new Point(e.getX() - getInsets().left, e.getY() - getInsets().top));
                repaint();
            }


            @Override
            public void mouseExited(MouseEvent e) {
                // If the mouse leaves the frame, we do not want to show the highlighted sensor.
                if (e.getComponent().getName().equals("BoardModuleFrame")) {
                    lastPolygon = null;
                    repaint();
                }
            }
        });

        pack();
        setVisible(true);

    }

    /**
     * Panel to paint the board.
     *
     * @author Michael Rodenbuecher
     * @since 2022-08-20
     */
    private class PaintPane extends JPanel {

        public PaintPane() {
            setBackground(Configuration.FRAME_BACKGROUND);
            setOpaque(true);
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);

            Graphics2D g2d = (Graphics2D) g.create();
            model.stream().forEach(field -> field.paint(g2d, lastPolygon));
            g2d.dispose();
        }
    }

}
